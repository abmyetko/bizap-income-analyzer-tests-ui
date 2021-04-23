package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestAnnualSummaryFrequency;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.STATUS_BAD_REQUEST;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.restoreApplicantDefaults;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.updatePayFrequency;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "EditablePayFrequencyTest")
public class EditablePayFrequencyTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderRestoreDef = createFolderBuilder("IAPayFreq");
    private RestGetResponse originResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderRestoreDef.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderRestoreDef);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderRestoreDef.getBorrowerFullName(),
                        folderBuilderRestoreDef.getBorrowerSSN(),
                        folderBuilderRestoreDef.getBorrowerCollaboratorId(),
                        folderBuilderRestoreDef.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderRestoreDef.getBorrowerFullName(),
                        folderBuilderRestoreDef.getBorrowerSSN(),
                        folderBuilderRestoreDef.getBorrowerCollaboratorId(),
                        folderBuilderRestoreDef.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "01/07/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("10000")
                        .setTwoYearPriorBasePay("10000"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderRestoreDef.getBorrowerFullName(),
                        folderBuilderRestoreDef.getBorrowerSSN(),
                        folderBuilderRestoreDef.getBorrowerCollaboratorId(),
                        folderBuilderRestoreDef.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
                .importDocumentList();

        originResponse = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
    }

    @Test
    @Description("IA-2973 check if changing pay Frequency generates footnote")
    void checkIfChangingPayFrequencyGeneratesFootnote() {
        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestPartIncomeAnnualSummary incomeAnnualSummary = response.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);
        RestAnnualSummaryFrequency annualSummaryFreq = incomeAnnualSummary.getFrequency();

        Assertions.assertEquals(IncomeFrequency.MONTHLY, annualSummaryFreq.getValue());
        Assertions.assertEquals(ValueType.CALCULATED, annualSummaryFreq.getValueType());
        Assertions.assertEquals(0, annualSummaryFreq.getFootnotesIdx().size());

        updatePayFrequency(String.valueOf(response.getId()), annualSummaryFreq.getDocumentId(), IncomeFrequency.BI_WEEKLY);

        RestGetResponse responseAfterUpdate = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestAnnualSummaryFrequency payFrequencyValueAfterUpdate = responseAfterUpdate.getApplicant(folderBuilderRestoreDef
                .getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay()
                .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getFrequency();
        Assertions.assertEquals(IncomeFrequency.BI_WEEKLY, payFrequencyValueAfterUpdate.getValue());
        Assertions.assertEquals(ValueType.MANUAL, payFrequencyValueAfterUpdate.getValueType());
        Assertions.assertEquals(1, payFrequencyValueAfterUpdate.getFootnotesIdx().size());

        String footnote = responseAfterUpdate.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome("W2", folderBuilderRestoreDef.getBorrowerCurrentEmployment())
                .getPart("Base Pay").getFootnotes().get(1);
        Assertions.assertEquals("The pay frequency (bi-weekly) was applied by a business validator instead of being calculated from paystub dates", footnote);
    }

    @Test
    @Description("IA-2972 check if changing pay Frequency leaves low touch state")
    void checkIfChangingPayFrequencyLeavesLowTouchState() {
        Long borrowerId = originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId();
        restoreApplicantDefaults(borrowerId);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestPartIncomeAnnualSummary incomeAnnualSummary = response.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);
        RestAnnualSummaryFrequency annualSummaryFreq = incomeAnnualSummary.getFrequency();

        Assertions.assertTrue(response.getTouchless());
        Assertions.assertEquals(IncomeFrequency.MONTHLY, annualSummaryFreq.getValue());
        Assertions.assertEquals(ValueType.CALCULATED, annualSummaryFreq.getValueType());
        Assertions.assertEquals(0, annualSummaryFreq.getFootnotesIdx().size());

        updatePayFrequency(String.valueOf(response.getId()), annualSummaryFreq.getDocumentId(), IncomeFrequency.BI_WEEKLY);

        RestGetResponse responseAfterUpdate = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestAnnualSummaryFrequency payFrequencyValueAfterUpdate = responseAfterUpdate.getApplicant(folderBuilderRestoreDef
                .getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay()
                .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getFrequency();
        Assertions.assertFalse(responseAfterUpdate.getTouchless());
        Assertions.assertEquals(IncomeFrequency.BI_WEEKLY, payFrequencyValueAfterUpdate.getValue());
        Assertions.assertEquals(ValueType.MANUAL, payFrequencyValueAfterUpdate.getValueType());
        Assertions.assertEquals(1, payFrequencyValueAfterUpdate.getFootnotesIdx().size());

        String footnote = responseAfterUpdate.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome("W2", folderBuilderRestoreDef.getBorrowerCurrentEmployment())
                .getPart("Base Pay").getFootnotes().get(1);
        Assertions.assertEquals("The pay frequency (bi-weekly) was applied by a business validator instead of being calculated from paystub dates", footnote);

        restoreApplicantDefaults(borrowerId);

        RestGetResponse responseAfterRestoreDef = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestAnnualSummaryFrequency payFrequencyValueAfterRestoreDef = responseAfterRestoreDef.getApplicant(folderBuilderRestoreDef
                .getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay()
                .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getFrequency();

        Assertions.assertTrue(responseAfterRestoreDef.getTouchless());
        Assertions.assertEquals(IncomeFrequency.MONTHLY, payFrequencyValueAfterRestoreDef.getValue());
        Assertions.assertEquals(ValueType.CALCULATED, payFrequencyValueAfterRestoreDef.getValueType());
        Assertions.assertEquals(0, payFrequencyValueAfterRestoreDef.getFootnotesIdx().size());
    }

    @Test
    @Description("IA-2972 check if changes pay Frequency for Paystub one year prior is not included in calculation")
    void checkIfChangesPayFrequencyForPayStubOneYearPriorIsNotIncludedInCalculation() {
        restoreApplicantDefaults(originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());
        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestPartIncomeAnnualSummary incomeAnnualSummary = response.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR)
                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);
        RestAnnualSummaryFrequency annualSummaryFreq = incomeAnnualSummary.getFrequency();

        Assertions.assertTrue(response.getTouchless());
        Assertions.assertEquals(IncomeFrequency.MONTHLY, annualSummaryFreq.getValue());
        Assertions.assertEquals(ValueType.CALCULATED, annualSummaryFreq.getValueType());
        Assertions.assertEquals(0, annualSummaryFreq.getFootnotesIdx().size());

        updatePayFrequency(String.valueOf(response.getId()), annualSummaryFreq.getDocumentId(), IncomeFrequency.BI_WEEKLY);

        RestGetResponse responseAfterUpdate = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestAnnualSummaryFrequency payFrequencyValueAfterUpdate = responseAfterUpdate.getApplicant(folderBuilderRestoreDef
                .getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay()
                .getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getFrequency();

        Assertions.assertFalse(responseAfterUpdate.getTouchless());
        Assertions.assertEquals(IncomeFrequency.MONTHLY, payFrequencyValueAfterUpdate.getValue());
        Assertions.assertEquals(ValueType.CALCULATED, payFrequencyValueAfterUpdate.getValueType());
        Assertions.assertEquals(0, payFrequencyValueAfterUpdate.getFootnotesIdx().size());
    }

    @Test
    @Description("IA-3029 check if changes pay frequency for VOE is not allowed")
    void checkIfChangesPayFrequencyForVOEIsNotAllowed() {
        restoreApplicantDefaults(originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());
        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestPartIncomeAnnualSummary incomeAnnualSummary = response.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                .getAnnualSummaryDocument(SummaryDocumentType.VOE);
        RestAnnualSummaryFrequency annualSummaryFreq = incomeAnnualSummary.getFrequency();

        Assertions.assertTrue(response.getTouchless());
        Assertions.assertEquals(IncomeFrequency.MONTHLY, annualSummaryFreq.getValue());
        Assertions.assertEquals(ValueType.EXPLICIT, annualSummaryFreq.getValueType());

        updatePayFrequency(String.valueOf(response.getId()), annualSummaryFreq.getDocumentId(), IncomeFrequency.BI_WEEKLY, STATUS_BAD_REQUEST);

        RestGetResponse responseAfterUpdate = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestAnnualSummaryFrequency payFrequencyValueAfterUpdate = responseAfterUpdate.getApplicant(folderBuilderRestoreDef
                .getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay()
                .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE).getFrequency();

        Assertions.assertTrue(responseAfterUpdate.getTouchless());
        Assertions.assertEquals(IncomeFrequency.MONTHLY, payFrequencyValueAfterUpdate.getValue());
        Assertions.assertEquals(ValueType.EXPLICIT, payFrequencyValueAfterUpdate.getValueType());
        Assertions.assertEquals(0, payFrequencyValueAfterUpdate.getFootnotesIdx().size());
    }

    @Test
    @Description("IA-2971 check if user-selected pay frequency in calculations is calculated with highest precedence")
    void checkIfUserSelectedPayFrequencyInCalculationsIsCalculatedWithHighestPrecedence() {
        restoreApplicantDefaults(originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
            RestPartIncomeAnnualSummary incomeAnnualSummary = response.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                    .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);

            Assertions.assertEquals(bigD(600.00), bigD(incomeAnnualSummary.getMonthlyAmountCalculated().getValue()), "Monthly amount is wrongly calculated");

            RestAnnualSummaryFrequency annualSummaryFreq = incomeAnnualSummary.getFrequency();
            updatePayFrequency(String.valueOf(response.getId()), annualSummaryFreq.getDocumentId(), IncomeFrequency.BI_WEEKLY);

            RestGetResponse responseAfterChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
            RestPartIncomeAnnualSummary incomeAnnualSummaryAfterChange = responseAfterChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                    .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);

            Assertions.assertEquals(bigD(1300.00), bigD(incomeAnnualSummaryAfterChange.getMonthlyAmountCalculated().getValue()), "Monthly amount has not changed");
        });
    }
}
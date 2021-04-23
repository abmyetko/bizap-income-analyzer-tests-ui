package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponseApplicant;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.AVG;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Disabled
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "TouchlessLowTouchTest")
public class TouchlessLowTouchTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilder = createFolderBuilder("TLTTest");
    private RestGetResponse getResponse;

    @BeforeAll
    public void generateDocs() {
        folderBuilder.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "100")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "1200",
                        "3600",
                        "03/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1500")
                        .setPriorYearOvertime("2800")
                        .setTwoYearPriorOvertime("2500"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + TWO_YEARS_PRIOR,
                        "1200",
                        "65",
                        "0",
                        "0"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
    }

    @Test
    @Order(0)
    @Tag("integration")
    @Description("IA-2211 IA-2434 check if selecting and deselecting applicant leaves touchless state, restore calculations using restore button")
    void checkIfSelectDeselectApplicantLeavesTouchlessState() {
        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(0).getId(), false);
        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(0).getId(), true);
        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(1).getId(), false);
        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(1).getId(), true);
        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        assertTrue(getResponse.getTouchless());
        assertTrue(getResponse.getApplicants().get(0).getTouchless());
        assertTrue(getResponse.getApplicants().get(1).getTouchless());
    }

    @Test
    @Order(1)
    @Description("IA-2465 IA-2434 check if applicant has it's own touchless flag, restore calculations using restore button")
    void checkIfApplicantHasHisOwnTouchlessFlagInResponse() {
        getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
        assertTrue(getResponse.getApplicants().get(0).getTouchless());
        assertTrue(getResponse.getApplicants().get(1).getTouchless());
    }

    @Test
    @Order(2)
    @Description("IA-2465 IA-2434 check if touchless flag works correctly when Income Type changes are made, restore calculations using restore button")
    void checkIfIncomeChangeAffectsLowTouchForAnApplicant() {
        restoreDefaultCalculations();
        final RestGetResponseApplicant[] firstApplicant = {getResponse.getApplicant(folderBuilder.getBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomePartType.BASE_PAY, false);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertFalse(getResponse.getApplicants().get(0).getTouchless());
        });
        final RestGetResponseApplicant[] secondApplicant = {getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(secondApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomePartType.BASE_PAY, false);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertFalse(getResponse.getApplicants().get(1).getTouchless());
        });
    }

    @Test
    @Order(3)
    @Description("IA-2465 IA-2434 check if touchless flag works correctly when Historical Averages changes are made, restore calculations using restore button")
    void checkIfHistoricalAverageAffectsLowTouchForAnApplicant() {
        restoreDefaultCalculations();
        final RestGetResponseApplicant[] firstApplicant = {getResponse.getApplicant(folderBuilder.getBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeAvg(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR, true);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertFalse(getResponse.getApplicants().get(0).getTouchless());
        });
        final RestGetResponseApplicant[] secondApplicant = {getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeAvg(secondApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR, true);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertFalse(getResponse.getApplicants().get(1).getTouchless());
        });
    }

    @Test
    @Order(4)
    @Description("IA-2465 IA-2434 check if touchless flag works correctly when Year changes are made, restore calculations using restore button")
    void checkIfYearAndDocumentAmountAffectsLowTouchForAnApplicant() {
        restoreDefaultCalculations();
        RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();
        selectJobAnnualSummary(borrowerBP.getIncome(ONE_YEAR_PRIOR).getId(), borrowerBP.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getDocIds().get(0), AVG);
        getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
        assertFalse(getResponse.getApplicants().get(0).getTouchless());

        RestIncomePart coBorrowerBP = getResponse.getApplicant(folderBuilder.getCoBorrowerFullName()).getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getBasePay();
        selectJobAnnualSummary(coBorrowerBP.getIncome(ONE_YEAR_PRIOR).getId(), coBorrowerBP.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getDocIds().get(0), AVG);
        getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
        assertFalse(getResponse.getApplicants().get(1).getTouchless());
    }

    @Test
    @Order(Integer.MAX_VALUE - 1)
    @Description("IA-2276 Check If Historical Average Selection Defaults To Prior Year When Current Job Ytd Is Deselected")
    void checkIfHaSelectionDefaultsToPriorYearWhenCurrentJobYtdIsDeselected() {
        restoreDefaultCalculations();

        String borrowerPrimaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(borrowerPrimaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getId(), false);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponseApplicant primaryApplicant = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName());

            assertAll("Historical Average Defaults after current job ytd deselection",
                    () -> assertFalse(primaryApplicant.getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getSelected()),
                    () -> assertFalse(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected()),
                    () -> assertTrue(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected())
            );
        });
    }

    @Test
    @Order(Integer.MAX_VALUE)
    @Description("IA-2276 Check If Historical Average Selection Does Not Default To Any Value If Current Job Ytd Has No Documents")
    void checkIfHaSelectionDoesNotDefaultsWhenCurrentJobYtdHasNoDocuments() {
        cleanJsonDocuments(folderBuilder.getFolderId());

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "100")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "1200",
                        "65",
                        "0",
                        "0"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponseApplicant primaryApplicant = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName());

            assertAll("Historical Average Defaults after current job ytd deselection",
                    () -> assertFalse(primaryApplicant.getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getSelected()),
                    () -> assertFalse(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected()),
                    () -> assertFalse(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });
    }

    @Test
    @SuppressWarnings("rawtypes")
    @Order(Integer.MAX_VALUE)
    @Description("IA-2276 Check If Historical Average Selection Does Not Default To Any Value If Current Job Ytd Has No Documents For FHA Loan Type")
    void checkIfHaSelectionDoesNotDefaultsWhenCurrentJobYtdHasNoDocumentsForFhaLoan() {
        IAFolderBuilder iaBuilder = createFolderBuilder("TLTFhaTest");
        iaBuilder.generateLoanDocument().setMortgageAppliedFor(MortgageType.FHA).restBuild();

        DataUploadObject fhaDataUpload = createUploadObject(folderBuilder);
        fhaDataUpload.clearDocuments()
                .addDocument(fhaDataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "100")))
                .addDocument(fhaDataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1100")))
                .addDocument(fhaDataUpload.createCustomPaystub(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1100")))
                .addDocument(fhaDataUpload.createCustomVoePrevious(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "1200",
                        "65",
                        "0",
                        "0"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponseApplicant primaryApplicant = RestGetLoanData.getApplicationData(iaBuilder.getFolderId()).getApplicant(iaBuilder.getBorrowerFullName());

            assertAll("Historical Average Defaults after current job ytd deselection",
                    () -> assertFalse(primaryApplicant.getIncome(iaBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getSelected()),
                    () -> assertFalse(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected()),
                    () -> assertFalse(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(primaryApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });
    }

    void restoreDefaultCalculations() {
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicants().forEach(applicant -> {
            restoreApplicantDefaults(applicant.getId());
            assertTrue(applicant.getTouchless(), String.format("Applicant %s could not be restored to touchless", applicant.getFirstName()));
        }));
    }

}
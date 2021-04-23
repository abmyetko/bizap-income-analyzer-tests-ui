package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestApplicantIncomeCategory;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.AVG;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "RestoreDefaultsTest")
public class RestoreDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderRestoreDef = createFolderBuilder("IARestorDef");
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
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderRestoreDef.getBorrowerFullName(),
                        folderBuilderRestoreDef.getBorrowerSSN(),
                        folderBuilderRestoreDef.getBorrowerCollaboratorId(),
                        folderBuilderRestoreDef.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
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
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderRestoreDef.getCoBorrowerFullName(),
                        folderBuilderRestoreDef.getCoBorrowerSSN(),
                        folderBuilderRestoreDef.getCoBorrowerCollaboratorId(),
                        folderBuilderRestoreDef.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderRestoreDef.getCoBorrowerFullName(),
                        folderBuilderRestoreDef.getCoBorrowerSSN(),
                        folderBuilderRestoreDef.getCoBorrowerCollaboratorId(),
                        folderBuilderRestoreDef.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .importDocumentList();

        originResponse = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
    }

    @Test
    @Order(1)
    @Description("IA-2261 Check If Historical Average Can Be Restored")
    void checkIfHistoricalAverageCanBeRestored() {
        restoreDefaultCalculations();

        RestGetResponse responseBeforeChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        selectIncomeAvg(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId(),
                responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR, true);
        selectIncomeAvg(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getId(),
                responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR, true);

        compareApplicantsFailures(responseBeforeChange, 2, 2);

        restoreApplicantDefaults(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        compareApplicantsFailures(responseBeforeChange, 0, 2);
    }

    @Test
    @Order(2)
    @Description("IA-2261 Check If Year Income Selection Can Be Restored")
    void checkIfYearIncomeSelectionCanBeRestored() {
        restoreDefaultCalculations();

        RestGetResponse responseBeforeChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestIncomePart borrowerBP = responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay();
        RestIncomePart coBorrowerBP = responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncome(folderBuilderRestoreDef.getCoBorrowerCurrentEmployment()).getBasePay();
        selectJobAnnualSummary(borrowerBP.getIncome(YEAR_TO_DATE).getId(), borrowerBP.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getDocIds().get(0), AVG);
        selectJobAnnualSummary(borrowerBP.getIncome(ONE_YEAR_PRIOR).getId(), borrowerBP.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getDocIds().get(0), AVG);
        selectJobAnnualSummary(coBorrowerBP.getIncome(YEAR_TO_DATE).getId(), coBorrowerBP.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0), AVG);

        compareApplicantsFailures(responseBeforeChange, 26, 10);

        restoreApplicantDefaults(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        compareApplicantsFailures(responseBeforeChange, 0, 10);
    }

    @Test
    @Order(3)
    @Description("IA-2261 Check If Employment Year Selection Can Be Restored")
    void checkIfEmploymentYearSelectionCanBeRestored() {
        restoreDefaultCalculations();

        RestGetResponse responseBeforeChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestIncomePart borrowerBP = responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay();
        RestIncomePart coBorrowerBP = responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncome(folderBuilderRestoreDef.getCoBorrowerCurrentEmployment()).getBasePay();

        String borrowerPrimaryGroupId = Long.toString(responseBeforeChange
                .getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getId());
        String coBorrowerPrimaryGroupId = Long.toString(responseBeforeChange
                .getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getCoBorrowerCurrentEmployment()).getId());


        selectIncome(borrowerPrimaryGroupId, borrowerBP.getIncome(ONE_YEAR_PRIOR).getId(), false);
        selectIncome(borrowerPrimaryGroupId, borrowerBP.getIncome(TWO_YEARS_PRIOR).getId(), false);
        selectIncome(coBorrowerPrimaryGroupId, coBorrowerBP.getIncome(ONE_YEAR_PRIOR).getId(), false);

        compareApplicantsFailures(responseBeforeChange, 15, 8);

        restoreApplicantDefaults(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        compareApplicantsFailures(responseBeforeChange, 0, 8);
    }

    @Test
    @Order(4)
    @Description("IA-2261 Check If Months Paid Value Can Be Restored")
    void checkIfMonthsPaidValueCanBeRestored() {
        restoreDefaultCalculations();

        RestGetResponse responseBeforeChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestIncomePart borrowerBP = responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getBasePay();
        RestIncomePart coBorrowerBP = responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncome(folderBuilderRestoreDef.getCoBorrowerCurrentEmployment()).getBasePay();

        String borrowerPrimaryGroupId = Long.toString(responseBeforeChange
                .getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getBorrowerCurrentEmployment()).getId());
        String coBorrowerPrimaryGroupId = Long.toString(responseBeforeChange
                .getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName())
                .getIncome(folderBuilderRestoreDef.getCoBorrowerCurrentEmployment()).getId());

        selectIncomeAndSetMonthsPaid(borrowerPrimaryGroupId, borrowerBP.getIncome(YEAR_TO_DATE).getId(), false, bigD(6));
        selectIncomeAndSetMonthsPaid(coBorrowerPrimaryGroupId, coBorrowerBP.getIncome(YEAR_TO_DATE).getId(), false, bigD(6));

        compareApplicantsFailures(responseBeforeChange, 20, 11);

        restoreApplicantDefaults(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        compareApplicantsFailures(responseBeforeChange, 0, 11);
    }

    @Test
    @Order(5)
    @Description("IA-2261 Check If Income Type Selection Can Be Restored")
    void checkIfIncomeTypeSelectionCanBeRestored() {
        restoreDefaultCalculations();

        RestGetResponse responseBeforeChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        selectIncomeType(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId(),
                responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomePartType.OVERTIME, true);
        selectIncomeType(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId(),
                responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomePartType.COMMISSIONS, true);
        selectIncomeType(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getId(),
                responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomePartType.OVERTIME, true);
        selectIncomeType(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getId(),
                responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomePartType.COMMISSIONS, false);

        RestGetResponse responseAfterChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestApplicantIncomeCategory borrowerIncomeCategories = responseAfterChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncomeCategoryW2();
        RestApplicantIncomeCategory coBorrowerIncomeCategories = responseAfterChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncomeCategoryW2();
        assertAll("Section selection comparison",
                () -> assertTrue(borrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(),
                        "Borrower Overtime section was not selected"),
                () -> assertTrue(borrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(),
                        "Borrower Commission section was not selected"),
                () -> assertTrue(coBorrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(),
                        "CoBorrower Overtime section was not selected"),
                () -> assertFalse(coBorrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(),
                        "CoBorrower Commission section was selected")
        );
        compareApplicantsFailures(responseBeforeChange, 0, 0);

        restoreApplicantDefaults(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        responseAfterChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        RestApplicantIncomeCategory afterChangeBorrowerIncomeCategories = responseAfterChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getIncomeCategoryW2();
        RestApplicantIncomeCategory afterChangeCoBorrowerIncomeCategories = responseAfterChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getIncomeCategoryW2();
        assertAll("After restore section selection comparison",
                () -> assertFalse(afterChangeBorrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(),
                        "Borrower Overtime section was selected after restore"),
                () -> assertFalse(afterChangeBorrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(),
                        "Borrower Commission section was selected after restore"),
                () -> assertTrue(afterChangeCoBorrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(),
                        "CoBorrower Overtime section was not selected after restore"),
                () -> assertFalse(afterChangeCoBorrowerIncomeCategories.getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(),
                        "CoBorrower Commission section was selected after restore")
        );
        compareApplicantsFailures(responseBeforeChange, 0, 0);
    }

    @Test
    @Order(Integer.MAX_VALUE - 1)
    @Description("IA-2261 Check If Income Category W2 Selection Can Be Restored")
    void checkIfIncomeCategoryW2SelectionCanBeRestored() {
        restoreDefaultCalculations();

        RestGetResponse responseBeforeChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        selectIncomeCategory(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId(), IncomePartCategory.W2, false);
        selectIncomeCategory(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getId(), IncomePartCategory.W2, false);

        compareApplicantsFailures(responseBeforeChange, 2, 2);

        restoreApplicantDefaults(responseBeforeChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        compareApplicantsFailures(responseBeforeChange, 0, 2);
    }

    @Test
    @Order(Integer.MAX_VALUE)
    @Description("IA-2261 Check If Applicant Selection Is Not Restored")
    void checkIfApplicantSelectionIsNotRestored() {
        restoreDefaultCalculations();

        selectApplicant(originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId(), false);
        selectApplicant(originResponse.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getId(), false);

        RestGetResponse responseAfterChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        assertFalse(responseAfterChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getSelected());
        assertFalse(responseAfterChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getSelected());

        restoreApplicantDefaults(originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());

        responseAfterChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
        assertFalse(responseAfterChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getSelected());
        assertFalse(responseAfterChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName()).getSelected());
    }

    void compareApplicantsFailures(RestGetResponse responseToCompareTo, int expectedMinNumberOfBorrowerFails, int expectedMinNumberOfCoBorrowerFails) {
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, 5000, () -> {
            RestGetResponse responseAfterChange = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());
            List<FailureComparison> borrowerFailList = new ArrayList<>(responseToCompareTo.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                    .compareTo(responseAfterChange.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())));
            List<FailureComparison> coBorrowerFailList = new ArrayList<>(responseToCompareTo.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName())
                    .compareTo(responseAfterChange.getApplicant(folderBuilderRestoreDef.getCoBorrowerFullName())));

            borrowerFailList.removeIf(failure -> Strings.isNullOrEmpty(failure.getFieldName()));
            coBorrowerFailList.removeIf(failure -> Strings.isNullOrEmpty(failure.getFieldName()));

            assertAll("Failure comparison",
                    () -> assertTrue(expectedMinNumberOfBorrowerFails <= borrowerFailList.size(),
                            String.format("Borrower has different number of fails: %s %n %s",
                                    borrowerFailList.size(), borrowerFailList.toString())),
                    () -> assertTrue(expectedMinNumberOfCoBorrowerFails <= coBorrowerFailList.size(),
                            String.format("CoBorrower has different number of fails: %s %n %s",
                                    coBorrowerFailList.size(), coBorrowerFailList.toString()))
            );
        });
    }

    void restoreDefaultCalculations() {
        Retry.whileTrue(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            restoreApplicantDefaults(originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName()).getId());
            RestGetResponse afterRestoreResponse = RestGetLoanData.getApplicationData(folderBuilderRestoreDef.getFolderId());

            List<FailureComparison> borrowerFailList = new ArrayList<>(originResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())
                    .compareTo(afterRestoreResponse.getApplicant(folderBuilderRestoreDef.getBorrowerFullName())));
            borrowerFailList.removeIf(failure -> Strings.isNullOrEmpty(failure.getFieldName()));
            return borrowerFailList.size() != 0;
        }, "Borrower calculations could not be restored to default");
    }
}


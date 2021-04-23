package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.EVOE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Evoe previous job base pay test")
@Execution(CONCURRENT)
@ResourceLock(value = "EvoePreviousJobBasePayTest")
class EvoePreviousJobBasePayTest extends TestBaseRest {

    private final IAFolderBuilder iaBuilder = createFolderBuilder("IAREvoePrev");

    @BeforeAll
    void createLoan() {
        iaBuilder.generateLoanDocument().restBuild();

        dataUpload = createUploadObject(iaBuilder);
    }

    @BeforeEach
    void clearIncomeData() {
        dataUpload.removeDocumentsFromFolder();
    }

    @Test
    @Description("IA-3041 IA-3040 Data from EVOE Previous Job YTD is shown in every column")
    void checkIfEvoePreviousJobYtdIsShownInEveryColumn() {
        dataUpload.importDocument(dataUpload.createCustomEvoePrevious(
                iaBuilder.getBorrowerFullName(),
                iaBuilder.getBorrowerSSN(),
                iaBuilder.getBorrowerCollaboratorId(),
                iaBuilder.getBorrowerPreviousEmployment(),
                "09/01/" + THREE_YEARS_PRIOR,
                "03/15/" + YEAR_TO_DATE,
                "1203",
                "12345",
                "08/01/" + YEAR_TO_DATE)
                .setPriorYearBasePay("60891.07")
                .setTwoYearPriorBasePay("62748.58")
                .setYtdOvertime("200")
                .setPriorYearOvertime("1800")
                .setTwoYearPriorOvertime("1500"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iaBuilder.getFolderId());

            RestPartIncomeAnnualSummary evoePreviousJobYtdBorrower = getResponse.getApplicant(iaBuilder.getBorrowerFullName())
                    .getIncome(iaBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(EVOE);

            assertAll("Borrower - Base Pay YTD EVOE Previous Job correct values assertion",
                    () -> assertNull(evoePreviousJobYtdBorrower.getFrequency().getValue(),
                            "Borrower base pay YTD Evoe previous job frequency should be null"),
                    () -> assertEquals(bigD(1203.00), bigD(evoePreviousJobYtdBorrower.getDisplayGrossPay().getValue()),
                            "Borrower base pay YTD Evoe previous job gross pay should equal 1203.00"),
                    () -> assertEquals(bigD(485.08), bigD(evoePreviousJobYtdBorrower.getMonthlyAmountAvg().getValue()),
                            "Borrower base pay YTD Evoe previous job monthly amount avg should equal 485.08"),
                    () -> assertEquals(bigD(2.48), bigD(evoePreviousJobYtdBorrower.getMonths().getValue()),
                            "Borrower base pay YTD Evoe previous job months should equal 2.48"),
                    () -> assertEquals("03/15/" + YEAR_TO_DATE, evoePreviousJobYtdBorrower.getPayPeriodEndDate().getValue(),
                            "Borrower base pay YTD Evoe previous job period end date should equal 03/15/" + YEAR_TO_DATE));

            RestPartIncomeAnnualSummary evoePreviousJobOneYearPriorBorrower = getResponse.getApplicant(iaBuilder.getBorrowerFullName())
                    .getIncome(iaBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(EVOE);

            assertAll("Borrower - Base Pay One Year Prior EVOE Previous Job correct values assertion",
                    () -> assertNull(evoePreviousJobOneYearPriorBorrower.getFrequency().getValue(),
                            "Borrower base pay one year prior Evoe previous job frequency should be null"),
                    () -> assertEquals(bigD(60891.07), bigD(evoePreviousJobOneYearPriorBorrower.getDisplayGrossPay().getValue()),
                            "Borrower base pay one year prion Evoe previous job gross pay should equal 60891.07"),
                    () -> assertEquals(bigD(5074.26), bigD(evoePreviousJobOneYearPriorBorrower.getMonthlyAmountAvg().getValue()),
                            "Borrower base pay one year prior Evoe previous job monthly amount avg should equal 5074.26"),
                    () -> assertEquals(bigD(12), bigD(evoePreviousJobOneYearPriorBorrower.getMonths().getValue()),
                            "Borrower base pay one year prior Evoe previous job months should equal 12"),
                    () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, evoePreviousJobOneYearPriorBorrower.getPayPeriodEndDate().getValue(),
                            "Borrower base pay one year prior Evoe previous job period end date should equal 12/31/" + ONE_YEAR_PRIOR));

            RestPartIncomeAnnualSummary evoePreviousJobTwoYearsPriorBorrower = getResponse.getApplicant(iaBuilder.getBorrowerFullName())
                    .getIncome(iaBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(EVOE);

            assertAll("Borrower - Base Pay Two Years Prior EVOE Previous Job correct values assertion",
                    () -> assertNull(evoePreviousJobTwoYearsPriorBorrower.getFrequency().getValue(),
                            "Borrower base pay two years prior Evoe previous job frequency should be null"),
                    () -> assertEquals(bigD(62748.58), bigD(evoePreviousJobTwoYearsPriorBorrower.getDisplayGrossPay().getValue()),
                            "Borrower base pay two years prior Evoe previous gross pay should equal 62748.58"),
                    () -> assertEquals(bigD(5229.05), bigD(evoePreviousJobTwoYearsPriorBorrower.getMonthlyAmountAvg().getValue()),
                            "Borrower base pay two years prior Evoe previous job monthly amount avg should equal 5229.05"),
                    () -> assertEquals(bigD(12), bigD(evoePreviousJobTwoYearsPriorBorrower.getMonths().getValue()),
                            "Borrower base pay two years prior Evoe previous job months should equal 12"),
                    () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, evoePreviousJobTwoYearsPriorBorrower.getPayPeriodEndDate().getValue(),
                            "Borrower base pay two years prior Evoe previous job period end date should equal 12/31/" + TWO_YEARS_PRIOR));
        });
    }

    @Test
    @Description("IA-3041 IA-3040 Data from EVOE Previous Job YTD is shown in two previous years column")
    void checkIfEvoePreviousJobOneYearPriorIsShownInTwoPreviousYearsColumns() {
        dataUpload.importDocument(dataUpload.createCustomEvoePrevious(
                iaBuilder.getBorrowerFullName(),
                iaBuilder.getBorrowerSSN(),
                iaBuilder.getBorrowerCollaboratorId(),
                iaBuilder.getBorrowerPreviousEmployment(),
                "09/01/" + THREE_YEARS_PRIOR,
                "03/15/" + ONE_YEAR_PRIOR,
                "1203",
                "12345",
                "08/01/" + ONE_YEAR_PRIOR)
                .setPriorYearBasePay("60891.07")
                .setTwoYearPriorBasePay("62748.58")
                .setYtdOvertime("200")
                .setPriorYearOvertime("1800")
                .setTwoYearPriorOvertime("1500"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iaBuilder.getFolderId());

            RestPartIncomeAnnualSummary evoePreviousJobOneYearPriorBorrower = getResponse.getApplicant(iaBuilder.getBorrowerFullName())
                    .getIncome(iaBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(EVOE);

            assertAll("Borrower - Base Pay One Year Prior EVOE Previous Job correct values assertion",
                    () -> assertNull(evoePreviousJobOneYearPriorBorrower.getFrequency().getValue(),
                            "Borrower base pay one year prior Evoe previous job frequency should be null"),
                    () -> assertEquals(bigD(1203.00), bigD(evoePreviousJobOneYearPriorBorrower.getDisplayGrossPay().getValue()),
                            "Borrower base pay one year prior Evoe previous gross pay should equal 1203.00"),
                    () -> assertEquals(bigD(485.08), bigD(evoePreviousJobOneYearPriorBorrower.getMonthlyAmountAvg().getValue()),
                            "Borrower base pay one year prior Evoe previous job monthly amount avg should equal 485.08"),
                    () -> assertEquals(bigD(2.48), bigD(evoePreviousJobOneYearPriorBorrower.getMonths().getValue()),
                            "Borrower base pay one year prior Evoe previous job months should equal 2.48"),
                    () -> assertEquals("03/15/" + ONE_YEAR_PRIOR, evoePreviousJobOneYearPriorBorrower.getPayPeriodEndDate().getValue(),
                            "Borrower base pay one year prior Evoe previous job period end date should equal 03/15/" + ONE_YEAR_PRIOR));

            RestPartIncomeAnnualSummary evoePreviousJobTwoYearsPriorBorrower = getResponse.getApplicant(iaBuilder.getBorrowerFullName())
                    .getIncome(iaBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(EVOE);

            assertAll("Borrower - Base Pay Two Years Prior EVOE Previous Job correct values assertion",
                    () -> assertNull(evoePreviousJobTwoYearsPriorBorrower.getFrequency().getValue(),
                            "Borrower base pay two years prior Evoe previous job frequency should be null"),
                    () -> assertEquals(bigD(60891.07), bigD(evoePreviousJobTwoYearsPriorBorrower.getDisplayGrossPay().getValue()),
                            "Borrower base pay two years prior Evoe previous gross pay should equal 60891.07"),
                    () -> assertEquals(bigD(5074.26), bigD(evoePreviousJobTwoYearsPriorBorrower.getMonthlyAmountAvg().getValue()),
                            "Borrower base pay two years prior Evoe previous job monthly amount avg should equal 5074.26"),
                    () -> assertEquals(bigD(12), bigD(evoePreviousJobTwoYearsPriorBorrower.getMonths().getValue()),
                            "Borrower base pay two years prior Evoe previous job months should equal 12"),
                    () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, evoePreviousJobTwoYearsPriorBorrower.getPayPeriodEndDate().getValue(),
                            "Borrower base pay two years prior Evoe previous job period end date should equal 12/31/" + TWO_YEARS_PRIOR));
        });
    }

    @Test
    @Description("IA-3041 IA-3040 Data from EVOE Previous Job YTD is shown in last previous year column")
    void checkIfEvoePreviousJobTwoYearsPriorIsShownInLastPreviousYearColumn() {
        dataUpload.importDocument(dataUpload.createCustomEvoePrevious(
                iaBuilder.getBorrowerFullName(),
                iaBuilder.getBorrowerSSN(),
                iaBuilder.getBorrowerCollaboratorId(),
                iaBuilder.getBorrowerPreviousEmployment(),
                "09/01/" + THREE_YEARS_PRIOR,
                "03/15/" + TWO_YEARS_PRIOR,
                "1203",
                "12345",
                "08/01/" + TWO_YEARS_PRIOR)
                .setPriorYearBasePay("60891.07")
                .setTwoYearPriorBasePay("62748.58")
                .setYtdOvertime("200")
                .setPriorYearOvertime("1800")
                .setTwoYearPriorOvertime("1500"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iaBuilder.getFolderId());

            RestPartIncomeAnnualSummary evoePreviousJobTwoYearsPriorBorrower = getResponse.getApplicant(iaBuilder.getBorrowerFullName())
                    .getIncome(iaBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(EVOE);

            assertAll("Borrower - Base Pay Two Years Prior EVOE Previous Job correct values assertion",
                    () -> assertNull(evoePreviousJobTwoYearsPriorBorrower.getFrequency().getValue(),
                            "Borrower base pay two years prior Evoe previous job frequency should be null"),
                    () -> assertEquals(bigD(1203.00), bigD(evoePreviousJobTwoYearsPriorBorrower.getDisplayGrossPay().getValue()),
                            "Borrower base pay two years prior Evoe previous gross pay should equal 1203.00"),
                    () -> assertEquals(bigD(485.08), bigD(evoePreviousJobTwoYearsPriorBorrower.getMonthlyAmountAvg().getValue()),
                            "Borrower base pay two years prior Evoe previous job monthly amount avg should equal 485.08"),
                    () -> assertEquals(bigD(2.48), bigD(evoePreviousJobTwoYearsPriorBorrower.getMonths().getValue()),
                            "Borrower base pay two years prior Evoe previous job months should equal 2.48"),
                    () -> assertEquals("03/15/" + TWO_YEARS_PRIOR, evoePreviousJobTwoYearsPriorBorrower.getPayPeriodEndDate().getValue(),
                            "Borrower base pay two years prior Evoe previous job period end date should equal 03/15/" + TWO_YEARS_PRIOR));
        });
    }

    @Test
    @Description("IA-3070 Calculate Months Worked from VOE for last prior year ")
    void checkIfMonthsWorkedAreCalculatedProperlyFromEmploymentFromVoeForLastPriorYear() {
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                iaBuilder.getBorrowerFullName(),
                iaBuilder.getBorrowerSSN(),
                iaBuilder.getBorrowerCollaboratorId(),
                iaBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.MONTHLY,
                "01/01/" + TWO_YEARS_PRIOR,
                "03/20/" + TWO_YEARS_PRIOR,
                "1200",
                "70",
                "0",
                "0"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestPartIncomeAnnualSummary voePreviousJobTwoYearsPrior = RestGetLoanData.getApplicationData(iaBuilder.getFolderId())
                    .getApplicant(iaBuilder.getBorrowerFullName()).getIncome(iaBuilder.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE);

            assertEquals(bigD(2.65), bigD(voePreviousJobTwoYearsPrior.getMonths().getValue()),
                    "Borrower base pay voe previous job two years prior months worked should equal 2.65");
        });
    }

    @Test
    @Description("IA-3070 Calculate Months Worked from VOE for one year prior")
    void checkIfMonthsWorkedAreCalculatedProperlyFromEmploymentFromVoeForOneYearPrior() {
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                iaBuilder.getBorrowerFullName(),
                iaBuilder.getBorrowerSSN(),
                iaBuilder.getBorrowerCollaboratorId(),
                iaBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.MONTHLY,
                "02/01/" + ONE_YEAR_PRIOR,
                "03/31/" + ONE_YEAR_PRIOR,
                "1200",
                "70",
                "0",
                "0"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestPartIncomeAnnualSummary voePreviousJobOneYearPrior = RestGetLoanData.getApplicationData(iaBuilder.getFolderId())
                    .getApplicant(iaBuilder.getBorrowerFullName()).getIncome(iaBuilder.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE);

            assertEquals(bigD(2), bigD(voePreviousJobOneYearPrior.getMonths().getValue()),
                    "Borrower base pay voe previous job one year prior months worked should equal 2");
        });
    }

    @Test
    @Description("IA-3070 Calculate Months Worked from VOE for YTD ")
    void checkIfMonthsWorkedAreCalculatedProperlyFromEmploymentFromVoeForYtd() {
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                iaBuilder.getBorrowerFullName(),
                iaBuilder.getBorrowerSSN(),
                iaBuilder.getBorrowerCollaboratorId(),
                iaBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.MONTHLY,
                "05/01/" + TWO_YEARS_PRIOR,
                "03/31/" + YEAR_TO_DATE,
                "1200",
                "70",
                "0",
                "0"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestPartIncomeAnnualSummary voePreviousJobYtd = RestGetLoanData.getApplicationData(iaBuilder.getFolderId())
                    .getApplicant(iaBuilder.getBorrowerFullName()).getIncome(iaBuilder.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE);

            assertEquals(bigD(3), bigD(voePreviousJobYtd.getMonths().getValue()),
                    "Borrower base pay voe previous job YTD prior months worked should equal 3");
        });
    }

    @Test
    @Description("IA-3070 Calculate Months Worked from EVOE and Voe Preserving Docs Precedence")
    void checkIfMonthsWorkedAreCalculatedProperlyFromEmploymentFromEvoeAndVoe() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEvoePrevious(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "09/01/" + TWO_YEARS_PRIOR,
                        "03/15/" + YEAR_TO_DATE,
                        "1203",
                        "12345",
                        "03/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("60891.07")
                        .setTwoYearPriorBasePay("62748.58")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "02/01/" + TWO_YEARS_PRIOR,
                        "04/31/" + YEAR_TO_DATE,
                        "1200",
                        "70",
                        "0",
                        "0"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestIncomePart previousJobBasePay = RestGetLoanData.getApplicationData(iaBuilder.getFolderId()).getApplicant(iaBuilder.getBorrowerFullName())
                    .getIncome(iaBuilder.getBorrowerPreviousEmployment()).getBasePay();

            assertAll("Borrower - EVOE previous job months worked assertion",
                    () -> assertEquals(bigD(2.48), bigD(previousJobBasePay.getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(EVOE).getMonths().getValue()),
                            "[Borrower] base pay EVOE previous job YTD months worked should equal 2.48"),
                    () -> assertEquals(bigD(12), bigD(previousJobBasePay.getIncome(ONE_YEAR_PRIOR).getAllAnnualSummaryDocument(EVOE).getMonths().getValue()),
                            "[Borrower] base pay EVOE previous job ONE_YEAR_PRIOR months worked should equal 12"),
                    () -> assertEquals(bigD(4), bigD(previousJobBasePay.getIncome(TWO_YEARS_PRIOR).getAllAnnualSummaryDocument(EVOE).getMonths().getValue()),
                            "[Borrower] base pay EVOE previous job TWO_YEARS_PRIOR months worked should equal 4"),
                    () -> assertEquals(bigD(4), bigD(previousJobBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonths().getValue()),
                            "[Borrower] base pay VOE previous job YTD prior months worked should equal 4"),
                    () -> assertTrue(previousJobBasePay.getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(VOE).getIncluded(),
                            "[Borrower] base pay VOE previous job YTD should be included: true"),
                    () -> assertFalse(previousJobBasePay.getIncome(ONE_YEAR_PRIOR).getAllAnnualSummaryDocument(VOE).getIncluded(),
                            "[Borrower] base pay VOE previous job One Year Prior should be included: false"),
                    () -> assertFalse(previousJobBasePay.getIncome(TWO_YEARS_PRIOR).getAllAnnualSummaryDocument(VOE).getIncluded(),
                            "[Borrower] base pay VOE previous job Two Years Prior should be included: false"),
                    () -> assertFalse(previousJobBasePay.getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(EVOE).getIncluded(),
                            "[Borrower] base pay EVOE previous job YTD should be included: false"),
                    () -> assertFalse(previousJobBasePay.getIncome(ONE_YEAR_PRIOR).getAllAnnualSummaryDocument(EVOE).getIncluded(),
                            "[Borrower] base pay EVOE previous job One Year Prior should be included: false"),
                    () -> assertFalse(previousJobBasePay.getIncome(TWO_YEARS_PRIOR).getAllAnnualSummaryDocument(EVOE).getIncluded(),
                            "[Borrower] base pay EVOE previous job Two Years Prior should be included: false"));
        });
    }
}
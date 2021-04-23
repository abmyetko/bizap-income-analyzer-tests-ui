package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "SameRateScenarioCappedHoursTest")
class SameRateScenarioCappedHoursTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderSameRateTest = createFolderBuilder("IARSameRate");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilderSameRateTest
                .setBorrowerYearsOnThisJob("4")
                .setSignDate("02/28/" + YEAR_TO_DATE)
                .setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("01/31/" + YEAR_TO_DATE)
                .setCoBorrowerPreviousJobEndDate("01/31/" + YEAR_TO_DATE)
                .generateLoanDocument()
                .restBuild();

        dataUpload = createUploadObject(folderBuilderSameRateTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSameRateTest.getBorrowerFullName(),
                        folderBuilderSameRateTest.getBorrowerSSN(),
                        folderBuilderSameRateTest.getBorrowerCollaboratorId(),
                        folderBuilderSameRateTest.getBorrowerCurrentEmployment(),
                        "11/25/" + YEAR_TO_DATE,
                        "12/08/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "18.50", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "18.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "0.00", "0.00", "0.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSameRateTest.getBorrowerFullName(),
                        folderBuilderSameRateTest.getBorrowerSSN(),
                        folderBuilderSameRateTest.getBorrowerCollaboratorId(),
                        folderBuilderSameRateTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "18.50", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "18.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "0.00", "0.00", "0.00", "2,987.00")))
                .importDocumentList();

        RestGetLoanData.getApplicationData(folderBuilderSameRateTest.getFolderId());
    }

    @Test
    void checkIfBasePayTypeIsHourly() {
        assertEquals(IncomeType.HOURLY.toString(),
                RestGetLoanData.getApplicationData(folderBuilderSameRateTest.getFolderId())
                        .getApplicant(folderBuilderSameRateTest.getBorrowerFullName())
                        .getIncomeCategoryW2()
                        .getPrimaryIncomeGroup()
                        .getIncomeTypeBasePay().getPaymentType());
    }

    @Test
    void checkIfYtdCalculationsAreCorrect() {
        RestPartIncomeAnnualSummary paystubYTD = RestGetLoanData.getApplicationData(folderBuilderSameRateTest.getFolderId())
                .getApplicant(folderBuilderSameRateTest.getBorrowerFullName())
                .getIncome(folderBuilderSameRateTest.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("BI_WEEKLY", paystubYTD.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(1480.00), bigD(paystubYTD.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(3206.67), bigD(paystubYTD.getMonthlyAmountCalculated().getValue())),
                () -> assertEquals(bigD(3279.52), bigD(paystubYTD.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(bigD(72.85), bigD(paystubYTD.getDifference())),
                () -> assertEquals(bigD(2.27), bigD(paystubYTD.getVariance())),
                () -> assertEquals("12/08/" + YEAR_TO_DATE, paystubYTD.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(11.26), bigD(paystubYTD.getMonths().getValue())));
    }

    @Test
    void checkIfOneYearPriorCalculationsAreCorrect() {
        RestPartIncomeAnnualSummary paystubYTD = RestGetLoanData.getApplicationData(folderBuilderSameRateTest.getFolderId()).getApplicant(folderBuilderSameRateTest.getBorrowerFullName()).getIncome(folderBuilderSameRateTest.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Base Pay ONE_YEAR_PRIOR Paystub correct values assertion",
                () -> assertEquals("MONTHLY", paystubYTD.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(1489.28), bigD(paystubYTD.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(3077.28), bigD(paystubYTD.getMonthlyAmountAvg().getValue())),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, paystubYTD.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(12), bigD(paystubYTD.getMonths().getValue())));
    }
}

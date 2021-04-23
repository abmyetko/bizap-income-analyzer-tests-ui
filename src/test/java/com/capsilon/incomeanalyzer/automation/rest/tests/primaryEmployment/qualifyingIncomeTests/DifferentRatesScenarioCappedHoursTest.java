package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.bigD;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings("rawtypes")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "DifferentRatesScenarioCappedHoursTest")
class DifferentRatesScenarioCappedHoursTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderDifferentRateTest = createFolderBuilder("IARDiffRate");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilderDifferentRateTest
                .setBorrowerYearsOnThisJob("4")
                .generateLoanDocument().restBuild();

        dataUpload = createUploadObject(folderBuilderDifferentRateTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderDifferentRateTest.getBorrowerFullName(),
                        folderBuilderDifferentRateTest.getBorrowerSSN(),
                        folderBuilderDifferentRateTest.getBorrowerCollaboratorId(),
                        folderBuilderDifferentRateTest.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "180.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "20.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderDifferentRateTest.getCoBorrowerFullName(),
                        folderBuilderDifferentRateTest.getCoBorrowerSSN(),
                        folderBuilderDifferentRateTest.getCoBorrowerCollaboratorId(),
                        folderBuilderDifferentRateTest.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/07/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.00", "10.00", "0.00", "2,987.00")))
                .importDocumentList();

        RestGetLoanData.getApplicationData(folderBuilderDifferentRateTest.getFolderId());
    }

    @Test
    @Tag("integration")
    @Tag("health")
    @Order(1)
    @Description("IA-1730 IA-2785 Check If Ytd Calculations Are Correct For Monthly Frequency")
    void checkIfYtdCalculationsAreCorrectForMonthlyFrequency() {
        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(folderBuilderDifferentRateTest.getFolderId()).getApplicant(folderBuilderDifferentRateTest.getBorrowerFullName())
                .getIncome(folderBuilderDifferentRateTest.getBorrowerCurrentEmployment()).getBasePay();
        String footnote = incomePartBasePay.getFootnotes().get(1);
        String footnoteIdx = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getFootnotesIdx().get(0);
        RestPartIncomeAnnualSummary paystubYTD = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        String footnoteText = "The maximum hours allowed for this pay frequency (173.34 hours) was used instead of the 254.5 hours reported on the paystub. The lowest rate ($30) reported on the paystub was used.";

        assertAll("Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("MONTHLY", paystubYTD.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(5200.20), bigD(paystubYTD.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(5200.20), bigD(paystubYTD.getMonthlyAmountCalculated().getValue())),
                () -> assertEquals(bigD(12309.14), bigD(paystubYTD.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(bigD(7108.94), bigD(paystubYTD.getDifference())),
                () -> assertEquals(bigD(136.71), bigD(paystubYTD.getVariance())),
                () -> assertEquals("03/31/" + YEAR_TO_DATE, paystubYTD.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(3), bigD(paystubYTD.getMonths().getValue())),
                () -> assertEquals(footnoteIdx, "1"),
                () -> assertEquals(footnoteText, footnote));
    }

    @Test
    @Tag("integration")
    @Order(2)
    @Description("IA-1730 IA-2785 Check If Ytd Calculations Are Correct For Weekly Frequency")
    void checkIfYtdCalculationsAreCorrectForWeeklyFrequency() {
        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(folderBuilderDifferentRateTest.getFolderId()).getApplicant(folderBuilderDifferentRateTest.getCoBorrowerFullName())
                .getIncome(folderBuilderDifferentRateTest.getCoBorrowerCurrentEmployment()).getBasePay();
        String footnote = incomePartBasePay.getFootnotes().get(1);
        String footnoteIdx = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getFootnotesIdx().get(0);
        RestPartIncomeAnnualSummary paystubYTD = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        String footnoteText = "The maximum hours allowed for this pay frequency (40 hours) was used instead of the 51.5 hours reported on the paystub. The lowest rate ($17) reported on the paystub was used.";

        assertAll("Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("WEEKLY", paystubYTD.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(680.00), bigD(paystubYTD.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(2946.67), bigD(paystubYTD.getMonthlyAmountCalculated().getValue())),
                () -> assertEquals(bigD(16559.38), bigD(paystubYTD.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(bigD(13612.71), bigD(paystubYTD.getDifference())),
                () -> assertEquals(bigD(461.97), bigD(paystubYTD.getVariance())),
                () -> assertEquals("03/07/" + YEAR_TO_DATE, paystubYTD.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(2.23), bigD(paystubYTD.getMonths().getValue())),
                () -> assertEquals(footnoteIdx, "1"),
                () -> assertEquals(footnoteText, footnote));
    }

    @Test
    @Tag("integration")
    @Order(3)
    @Description("IA-1730 IA-2785 Check If Ytd Calculations Are Correct For Biweekly Frequency")
    void checkIfYtdCalculationsAreCorrectForBiweeklyFrequency() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderDifferentRateTest.getBorrowerFullName(),
                folderBuilderDifferentRateTest.getBorrowerSSN(),
                folderBuilderDifferentRateTest.getBorrowerCollaboratorId(),
                folderBuilderDifferentRateTest.getBorrowerCurrentEmployment(),
                "04/01/" + YEAR_TO_DATE,
                "04/14/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "16.00", "296.00", "1,308.20"),
                new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "0.00", "2,987.00")));

        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(folderBuilderDifferentRateTest.getFolderId()).getApplicant(folderBuilderDifferentRateTest.getBorrowerFullName())
                .getIncome(folderBuilderDifferentRateTest.getBorrowerCurrentEmployment()).getBasePay();
        String footnote = incomePartBasePay.getFootnotes().get(1);
        String footnoteIdx = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getFootnotesIdx().get(0);
        RestPartIncomeAnnualSummary paystubYTD = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        String footnoteText = "The maximum hours allowed for this pay frequency (80 hours) was used instead of the 90.5 hours reported on the paystub. The lowest rate ($30) reported on the paystub was used.";

        assertAll("Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("BI_WEEKLY", paystubYTD.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(2400.00), bigD(paystubYTD.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(5200.00), bigD(paystubYTD.getMonthlyAmountCalculated().getValue())),
                () -> assertEquals(bigD(10641.90), bigD(paystubYTD.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(bigD(5441.90), bigD(paystubYTD.getDifference())),
                () -> assertEquals(bigD(104.65), bigD(paystubYTD.getVariance())),
                () -> assertEquals("04/14/" + YEAR_TO_DATE, paystubYTD.getPayPeriodEndDate().getValue()),
                () -> assertEquals(footnoteIdx, "1"),
                () -> assertEquals(footnoteText, footnote));
    }

    @Test
    @Tag("integration")
    @Order(4)
    @Description("IA-1730 IA-2785 Check If Ytd Calculations Are Correct For Semi Monthly Frequency")
    void checkIfYtdCalculationsAreCorrectForSemiMonthlyFrequency() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderDifferentRateTest.getCoBorrowerFullName(),
                folderBuilderDifferentRateTest.getCoBorrowerSSN(),
                folderBuilderDifferentRateTest.getCoBorrowerCollaboratorId(),
                folderBuilderDifferentRateTest.getCoBorrowerCurrentEmployment(),
                "04/01/" + YEAR_TO_DATE,
                "04/15/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "16.00", "296.00", "1,308.20"),
                new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "0.00", "2,987.00")));

        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(folderBuilderDifferentRateTest.getFolderId()).getApplicant(folderBuilderDifferentRateTest.getCoBorrowerFullName())
                .getIncome(folderBuilderDifferentRateTest.getCoBorrowerCurrentEmployment()).getBasePay();
        String footnote = incomePartBasePay.getFootnotes().get(1);
        String footnoteIdx = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getFootnotesIdx().get(0);
        RestPartIncomeAnnualSummary paystubYTD = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        String footnoteText = "The maximum hours allowed for this pay frequency (86.67 hours) was used instead of the 90.5 hours reported on the paystub. The lowest rate ($30) reported on the paystub was used.";

        assertAll("Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("SEMI_MONTHLY", paystubYTD.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(2600.10), bigD(paystubYTD.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(5200.20), bigD(paystubYTD.getMonthlyAmountCalculated().getValue())),
                () -> assertEquals(bigD(10550.69), bigD(paystubYTD.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(bigD(5350.49), bigD(paystubYTD.getDifference())),
                () -> assertEquals(bigD(102.89), bigD(paystubYTD.getVariance())),
                () -> assertEquals("04/15/" + YEAR_TO_DATE, paystubYTD.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(3.5), bigD(paystubYTD.getMonths().getValue())),
                () -> assertEquals(footnoteIdx, "1"),
                () -> assertEquals(footnoteText, footnote));
    }
}

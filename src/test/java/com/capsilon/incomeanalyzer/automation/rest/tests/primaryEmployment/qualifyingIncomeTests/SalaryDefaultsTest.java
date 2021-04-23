package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncome;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "SalaryDefaultsTest")
class SalaryDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder loanSalaryDefaults = createFolderBuilder("IARDefSal");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        loanSalaryDefaults
                .setBorrowerYearsOnThisJob("4")
                .generateLoanDocument().restBuild();

        dataUpload = createUploadObject(loanSalaryDefaults);
    }

    @BeforeEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    @Test
    @Order(1)
    @Description("IA-2403 Check If Ytd Paystub Projected Income Is Not Checked By Default When It Is Greater Then Actual")
    void checkDefaultsForYtdForPaystubAreNotCheckedWhenProjectedGreaterThenActual() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                loanSalaryDefaults.getBorrowerFullName(),
                loanSalaryDefaults.getBorrowerSSN(),
                loanSalaryDefaults.getBorrowerCollaboratorId(),
                loanSalaryDefaults.getBorrowerCurrentEmployment(),
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                .setYtdGrossIncomeAmount("1265"));

        RestPartIncomeAnnualSummary paystubYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("No defaults assertion",
                () -> assertFalse(paystubYTD.getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(paystubYTD.getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"));
    }

    @Test
    @Order(2)
    @Description("IA-2403 Check If Ytd Paystub Projected Income Is Checked By Default When It Is Equal To Actual")
    void checkDefaultsForYtdForPaystubAreCheckedWhenProjectedEqualsActual() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                loanSalaryDefaults.getBorrowerFullName(),
                loanSalaryDefaults.getBorrowerSSN(),
                loanSalaryDefaults.getBorrowerCollaboratorId(),
                loanSalaryDefaults.getBorrowerCurrentEmployment(),
                "03/01/" + YEAR_TO_DATE,
                "03/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "400.00", ""))
                .setYtdGrossIncomeAmount("2088"));

        RestPartIncomeAnnualSummary paystubYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertTrue(paystubYTD.getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income should be selected (PAYSTUB)"),
                () -> assertFalse(paystubYTD.getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"));
    }

    @Test
    @Order(3)
    @Description("IA-2403 Check If Ytd Paystub Projected Income Is Checked By Default When It Is Less Than Actual")
    void checkDefaultsForYtdForPaystubAreCheckedForProjectedIsLessThanActual() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                loanSalaryDefaults.getBorrowerFullName(),
                loanSalaryDefaults.getBorrowerSSN(),
                loanSalaryDefaults.getBorrowerCollaboratorId(),
                loanSalaryDefaults.getBorrowerCurrentEmployment(),
                "04/01/" + YEAR_TO_DATE,
                "04/30/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "193.28", ""))
                .setYtdGrossIncomeAmount("5665"));

        RestPartIncomeAnnualSummary paystubYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertTrue(paystubYTD.getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income should be selected (PAYSTUB)"),
                () -> assertFalse(paystubYTD.getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"));
    }

    @Test
    @Order(4)
    @Description("IA-2403 Check If Ytd Paystub Is Not Selected By Default If It Has Only Actual Income Value")
    void checkDefaultsForYtdForPaystubAreNotCheckedForOnlyActualAvg() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                loanSalaryDefaults.getBorrowerFullName(),
                loanSalaryDefaults.getBorrowerSSN(),
                loanSalaryDefaults.getBorrowerCollaboratorId(),
                loanSalaryDefaults.getBorrowerCurrentEmployment(),
                "05/01/" + YEAR_TO_DATE,
                "05/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""))
                .setYtdGrossIncomeAmount("1265"));

        RestPartIncomeAnnualSummary paystubYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("No defaults assertion",
                () -> assertFalse(paystubYTD.getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(paystubYTD.getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"));
    }

    @Test
    @Order(5)
    @Description("IA-2393 Check Defaults For Evoe An Paystub With All Income Data")
    void checkDefaultsForYtdForEvoeAndPaystubWithAllSetsOfData() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "06/01/" + TWO_YEARS_PRIOR,
                        "06/30/" + YEAR_TO_DATE,
                        "2800",
                        "24000",
                        "06/30/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(6)
    @Description("IA-2393 Check Defaults For Voe An Paystub With All Income Data")
    void checkDefaultsForYtdForVoeAndPaystubWithAllSetsOfData() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "07/01/" + TWO_YEARS_PRIOR,
                        "07/15/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "07/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg should be selected (VOE)"));
    }

    @Test
    @Order(7)
    @Description("IA-2427 Check Defaults For Voe Paystub With Only Actual Income When Income On Voe Is Lesser Than On Paystub")
    void checkDefaultsForYtdForVoeAndPaystubWithOnlyActualAverageAndActualOnVoeIsLesserThanPaystub() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "07/01/" + TWO_YEARS_PRIOR,
                        "07/31/" + YEAR_TO_DATE,
                        "",
                        "1200",
                        "07/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg should be selected (VOE)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[Borrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertTrue(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_YR Historical Average should be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(8)
    @Description("IA-2427 Check Defaults For Voe And Paystub With Only Actual Income When Income On Voe Is Equal To That On Paystub")
    void checkDefaultsForYtdForVoeAndPaystubWithOnlyActualAverageAndActualOnVoeIsEqualThanPaystub() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "08/01/" + TWO_YEARS_PRIOR,
                        "08/15/" + YEAR_TO_DATE,
                        "",
                        "1265",
                        "08/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "08/01/" + YEAR_TO_DATE,
                        "08/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg should be selected (VOE)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[Borrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertTrue(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_YR Historical Average should be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(9)
    @Description("IA-2427 Check If There Is No Default For Voe And Paystub With Only Actual Income When Income On Voe Is Greater Than On Paystub")
    void checkNoDefaultsForYtdForVoeAndPaystubWithOnlyActualAverageAndActualOnVoeIsGreaterThanPaystub() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "08/01/" + TWO_YEARS_PRIOR,
                        "08/31/" + YEAR_TO_DATE,
                        "",
                        "1500",
                        "08/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "08/01/" + YEAR_TO_DATE,
                        "08/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn'T be selected (VOE)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[Borrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_YR Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(10)
    @Description("IA-2405 Check Defaults For Voe And Paystub When Paystub Has Only Average Income")
    void checkDefaultsForYtdForVoeAndPaystubWhenPaystubMissingProjected() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "09/01/" + TWO_YEARS_PRIOR,
                        "09/15/" + YEAR_TO_DATE,
                        "1500",
                        "1500",
                        "09/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "09/01/" + YEAR_TO_DATE,
                        "09/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (VOE)"));
    }

    @Test
    @Order(11)
    @Description("IA-2405 Check Defaults For Voe And Paystub When Voe Has Only Average Income")
    void checkDefaultsForYtdForVoeAndPaystubWhenVoeMissingProjected() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "09/01/" + TWO_YEARS_PRIOR,
                        "09/30/" + YEAR_TO_DATE,
                        "",
                        "1500",
                        "09/30/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "09/01/" + YEAR_TO_DATE,
                        "09/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "500", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (VOE)"));
    }

    @Test
    @Order(12)
    @Description("IA-2405 Check Defaults For Voe And Paystub When Lowest Projected Income Is Lesser Than The Only Actual Income")
    void checkDefaultsForYtdForVoeAndPaystubWhenLowestProjectedIsLesserThanOnlyOneActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "10/15/" + YEAR_TO_DATE,
                        "130",
                        "",
                        "10/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "10/01/" + YEAR_TO_DATE,
                        "10/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "500", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income should be selected (VOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (VOE)"));
    }

    @Test
    @Order(13)
    @Description("IA-2405 Check Defaults For Voe And Paystub When Lowest Projected Income Is Equal To The Only Actual Income")
    void checkDefaultsForYtdForVoeAndPaystubWhenLowestProjectedIsEqualToTheOnlyOneActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "10/31/" + YEAR_TO_DATE,
                        "126.50",
                        "",
                        "10/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "10/01/" + YEAR_TO_DATE,
                        "10/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "500", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income should be selected (VOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (VOE)"));
    }

    @Test
    @Order(14)
    @Description("IA-2405 Check Defaults For Voe And Paystub When Lowest Projected Income Is Greater Than The Only Actual Income")
    void checkIfDefaultsForYtdForVoeAndPaystubWhenLowestProjectedIsGreaterThenOnlyOneActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "11/01/" + TWO_YEARS_PRIOR,
                        "11/15/" + YEAR_TO_DATE,
                        "1500",
                        "",
                        "11/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "10/01/" + YEAR_TO_DATE,
                        "11/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "500", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (VOE)"));
    }

    @Test
    @Order(15)
    @Description("IA-2405 Check If No Default Is Made For Voe And Paystub When There Is Only Projected Income On Voe And Actual Income On Paystub")
    void checkNoDefaultsForYtdForVoeAndPaystubWhenOnlyProjectedOnVoeAndActualOnPaystub() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "11/30/" + YEAR_TO_DATE,
                        "1500",
                        "",
                        "11/30/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        "11/01/" + YEAR_TO_DATE,
                        "11/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected monthly income shouldn't be selected (VOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg shouldn't be selected (VOE)"));
    }

    @Test
    @Order(16)
    @Description("IA-2404 Check Defaults For W2 And Paystub With All Income Data When Projected Income Is The Lowest")
    void checkDefaultsForYtdForW2AndPaystubWithAllDataWhenProjectedIsTheLowest() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "7800",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "200", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "200", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "200", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"));
    }

    @Test
    @Order(17)
    @Description("IA-2404 Check Defaults For W2 And Paystub With All Data When Projected Income Is Equal To Min Actual Income")
    void checkDefaultsForYtdForW2AndPaystubWithAllDataWhenProjectedIsEqualToMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "7200",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "200", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "200", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "200", ""))
                        .setYtdGrossIncomeAmount("1900"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"));
    }

    @Test
    @Order(18)
    @Description("IA-2404 Check If No Default Is Made For W2 And Paystub With All Data When Projected Income Is Greater Than Min Actual Income")
    void checkNoDefaultsForYtdForW2AndPaystubWithAllDataWhenProjectedIsGreaterThanMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "7200",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "04/01/" + YEAR_TO_DATE,
                        "04/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "100", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "50", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "100", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"));
    }

    @Test
    @Order(19)
    @Description("IA-2404 Check If No Default Is Made For W2 And Paystub When There Are Only Projected Incomes")
    void checkNoDefaultsForYtdForW2AndPaystubWhenThereAreOnlyTwoProjected() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "7200",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "04/01/" + YEAR_TO_DATE,
                        "04/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"));
    }

    @Test
    @Order(20)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe With All Income Data")
    void checkDefaultsForYtdForW2PaystubAndEvoeForAllData() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "7200",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "500", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "05/01/" + TWO_YEARS_PRIOR,
                        "05/15/" + YEAR_TO_DATE,
                        "2800",
                        "24000",
                        "05/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(21)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe When Evoe Is Missing Projected Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWhenProjectedEvoeMissing() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "7200",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "500", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "05/01/" + TWO_YEARS_PRIOR,
                        "05/31/" + YEAR_TO_DATE,
                        "",
                        "24000",
                        "05/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate(""))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(22)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe When Paystub Is Missing Projected Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWhenProjectedPaystubMissing() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "2400",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "06/01/" + TWO_YEARS_PRIOR,
                        "06/15/" + YEAR_TO_DATE,
                        "5000",
                        "24000",
                        "06/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(23)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe When Evoe Is Missing Projected Income And Min Projected Income Is Lesser Than Min Actual Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWhenEvoeProjectedIsMissingAndMinProjectedIsLesserThanMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "8000",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "10", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "10", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "10", ""))
                        .setYtdGrossIncomeAmount("5000"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "06/01/" + TWO_YEARS_PRIOR,
                        "06/30/" + YEAR_TO_DATE,
                        "5000",
                        "",
                        "06/30/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(24)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe When Evoe Projected Income Is Missing And Min Projected Income Is Equal To Min Actual Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWhenEvoeProjectedIsMissingAndMinProjectedIsEqualToMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "5059.92",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "100", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "100", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "10.83", ""))
                        .setYtdGrossIncomeAmount("14000"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "07/01/" + TWO_YEARS_PRIOR,
                        "07/15/" + YEAR_TO_DATE,
                        "5000",
                        "",
                        "07/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(25)
    @Description("IA-2430 Check If No Default Is Made For W2 Paystub And Evoe When Evoe Projected Income Is Missing And Min Projected Income Is Greater Than Min Actual Income")
    void checkNoDefaultsForYtdForW2PaystubAndEvoeWhenEvoeProjectedIsMissingAndMinProjectedIsGreaterThanMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "2400",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "100", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "100", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "10.83", ""))
                        .setYtdGrossIncomeAmount("1400"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "07/01/" + TWO_YEARS_PRIOR,
                        "07/31/" + YEAR_TO_DATE,
                        "5000",
                        "",
                        "07/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(26)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe When Evoe Projected Income Is Missing And Min Projected Income Is Lesser Than Min Actual Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWhenEvoeProjectedIsMissingAndProjectedIsLesserThanMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "1200",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "08/01/" + YEAR_TO_DATE,
                        "08/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("5000"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "08/01/" + TWO_YEARS_PRIOR,
                        "08/15/" + YEAR_TO_DATE,
                        "5000",
                        "",
                        "08/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("50"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income should be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(27)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe When Evoe Projected Income Is Missing And Min Projected Income Is Equal To Min Actual Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWhenEvoeProjectedIsMissingAndProjectedIsEqualToMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "80000",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "08/01/" + YEAR_TO_DATE,
                        "08/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("14000"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "08/01/" + TWO_YEARS_PRIOR,
                        "08/31/" + YEAR_TO_DATE,
                        "5000",
                        "",
                        "08/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("1750"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "Projected monthly income should be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(28)
    @Description("IA-2430 Check If No Default Is Made For W2 Paystub And Evoe When Evoe Projected Income Is Missing And Min Projected Income Is Greater Than Min Actual Income")
    void checkNoDefaultsForYtdForW2PaystubAndEvoeWhenEvoeProjectedIsMissingAndProjectedIsGreaterThanMinActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "2400",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "09/01/" + YEAR_TO_DATE,
                        "09/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1400"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "09/01/" + TWO_YEARS_PRIOR,
                        "09/15/" + YEAR_TO_DATE,
                        "5000",
                        "",
                        "09/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(29)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe With Only Actual Incomes When Evoe Actual Income Is Lesser Than Paystub Actual Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWithOnlyActualsAndEvoeActualIsLesserThanPaystubActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "2400",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "09/01/" + YEAR_TO_DATE,
                        "09/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("8000"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "09/01/" + TWO_YEARS_PRIOR,
                        "09/30/" + YEAR_TO_DATE,
                        "1000",
                        "5000",
                        "09/30/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate(""))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (EVOE)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[CoBorrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertTrue(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_YR Historical Average should be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(30)
    @Description("IA-2430 Check Defaults For W2 Paystub And Evoe With Only Actual Incomes When Evoe Actual Income Is Equal To Paystub Actual Income")
    void checkDefaultsForYtdForW2PaystubAndEvoeWithOnlyActualsAndEvoeActualIsEqualToPaystubActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "2400",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "10/01/" + YEAR_TO_DATE,
                        "10/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1400,04"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "10/15/" + YEAR_TO_DATE,
                        "1400,04",
                        "1400,04",
                        "10/15/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate(""))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (EVOE)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[CoBorrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertTrue(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_YR Historical Average should be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(31)
    @Description("IA-2430 Check If No Default Is Made For W2 Paystub And Evoe With Only Actual Incomes When Evoe Actual Income Is Greater Than Paystub Actual Income")
    void checkNoDefaultsForYtdForW2PaystubAndEvoeWithOnlyActualsAndEvoeActualIsGreaterThanPaystubActual() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "2400",
                        YEAR_TO_DATE.toString()))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "10/01/" + YEAR_TO_DATE,
                        "10/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "", ""))
                        .setYtdGrossIncomeAmount("1400"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "10/31/" + YEAR_TO_DATE,
                        "5000",
                        "80000",
                        "10/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate(""))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (W2)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[CoBorrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_YR Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(32)
    @Description("IA-2528 Check no default for missing frequency Paystub in YTD")
    void checkNoDefaultsForPaystubMissingFrequencyWithOnlyActualAvg() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                loanSalaryDefaults.getCoBorrowerFullName(),
                loanSalaryDefaults.getCoBorrowerSSN(),
                loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                null,
                "10/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "300", "1500")));

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(
                        dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[CoBorrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_YR Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(33)
    @Description("IA-2528 Check default for Paystub with missing frequency and other document in YTD - default to min")
    void checkDefaultsToLowestForPaystubMissingFrequencyAndOtherDocInYTD() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        null,
                        "10/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "200", "3000000")))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "10/31/" + YEAR_TO_DATE,
                        "5000",
                        "80000",
                        "10/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("6000"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income should be selected (EVOE)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(EVOE).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (EVOE)"));
    }

    @Test
    @Order(34)
    @Description("IA-2528 IA-2513 Check no defaults for Paystub missing frequency in YTD and W2 in prior year")
    void checkNoDefaultsForPaystubMissingFrequencyInYtdAndW2InPriorYear() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        null,
                        "10/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1500", "15000")))
                .addDocument(dataUpload.createCustomW2(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "2400",
                        ONE_YEAR_PRIOR.toString()))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);
        RestPartIncome dataFromOneYearPrior = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromOneYearPrior.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (W2)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[CoBorrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_YR Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(35)
    @Description("IA-2528 IA-2803 Check default to YTD Actual Avg and to HA YTD + Prior for Paystub missing frequency in YTD and year-end Paystub in prior year")
    void checkDefaultsToYtdActualAvgAndHaYtdPlusPriorForPaystubMissingFrequencyInYtdAndYearEndPaystubInPriorYear() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        null,
                        "10/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1500", "15000")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1500", "15000")))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getSelected(), "[CoBorrower] Projected monthly income shouldn't be selected (PAYSTUB)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (PAYSTUB)"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[CoBorrower] YTD_AVG Historical Average shouldn't be selected"),
                () -> assertTrue(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_YR Historical Average should be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }

    @Test
    @Order(36)
    @Description("IA-2528 IA-2803 Check default to YTD Actual Avg and to HA YTD + Prior + Year before prior for Paystub missing frequency in YTD and year-end Paystub in one and two years prior")
    void checkDefaultsToYtdActualAvgAndHaYtdPlusPriorPlusYearBeforePriorForPaystubMissingFrequencyInYtdAndYearEndPaystubInOneAndTwoYearsPrior() {
        loanSalaryDefaults.setMortgageAppliedFor(MortgageType.FHA)
                .generateLoanDocument();
        loanSalaryDefaults.uploadNewLoanDocument();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        null,
                        "10/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1500", "15000")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1500", "15000")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getCoBorrowerFullName(),
                        loanSalaryDefaults.getCoBorrowerSSN(),
                        loanSalaryDefaults.getCoBorrowerCollaboratorId(),
                        loanSalaryDefaults.getCoBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1500", "15000")))
                .importDocumentList();

        Retry.tryRun(3000, 600000, () -> {
            RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName())
                    .getIncome(loanSalaryDefaults.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

            RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getCoBorrowerFullName()).getIncomeCategoryW2().
                    getPrimaryIncomeGroup()
                    .getIncomeTypeBasePay();

            assertAll("Defaults assertion",
                    () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[CoBorrower] Actual YTD Avg should be selected (PAYSTUB)"),
                    () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[CoBorrower] YTD_AVG Historical Average shouldn't be selected"),
                    () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_YR Historical Average shouldn't be selected"),
                    () -> assertTrue(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[CoBorrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average should be selected"));
        });
    }

    @Test
    @Order(37)
    @Description("IA-2791 Check if Historical Default Is Made For Paystub And Voe When Paystub Projected Income Is Missing")
    void checkHistoricalDefaultsToYtdForPaystubAndVoeWhenVoeProjectedIsMissing() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        null,
                        "10/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "6787.91", ""))
                        .setYtdGrossIncomeAmount("67879.1"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanSalaryDefaults.getBorrowerFullName(),
                        loanSalaryDefaults.getBorrowerSSN(),
                        loanSalaryDefaults.getBorrowerCollaboratorId(),
                        loanSalaryDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.ANNUALLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "10/31/" + YEAR_TO_DATE,
                        "70000",
                        "57000",
                        "10/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("60891.07")
                        .setTwoYearPriorBasePay("62748.58")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .importDocumentList();

        RestPartIncome dataFromYTD = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName())
                .getIncome(loanSalaryDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);

        RestIncomeType dataFromHA = RestGetLoanData.getApplicationData(loanSalaryDefaults.getFolderId()).getApplicant(loanSalaryDefaults.getBorrowerFullName()).getIncomeCategoryW2().
                getPrimaryIncomeGroup()
                .getIncomeTypeBasePay();

        assertAll("Defaults assertion",
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD AVG income shouldn't be selected (PAYSTUB)"),
                () -> assertFalse(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "[Borrower] Projected Monthly Income shouldn't be selected (VOE)"),
                () -> assertTrue(dataFromYTD.getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected(), "[Borrower] Actual YTD Avg should be selected (VOE)"),
                () -> assertTrue(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "[Borrower] YTD_AVG Historical Average should be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_YR Historical Average shouldn't be selected"),
                () -> assertFalse(dataFromHA.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "[Borrower] YTD_AVG_PLUS_PREV_TWO_YRS Historical Average shouldn't be selected"));
    }
}
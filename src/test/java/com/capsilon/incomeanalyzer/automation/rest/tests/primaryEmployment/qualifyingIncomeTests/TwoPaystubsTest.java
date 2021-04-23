package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.ONE_YEAR_PRIOR;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.DateUtilities.formatDate;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.MM_DD_YYYY_F_SLASH;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.YYYY_MM_DD_DASH;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "TwoPaystubsTest")
class TwoPaystubsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilder = createFolderBuilder("IARTwoPs");
    private final String newerDocStartDate = "06/01/" + YEAR_TO_DATE;
    private final String newerDocEndDate = "06/30/" + YEAR_TO_DATE;
    private final String olderDocStartDate = "04/01/" + YEAR_TO_DATE;
    private final String olderDocEndDate = "04/30/" + YEAR_TO_DATE;
    private final String yearPriorNewerDocStartDate = "06/01/" + ONE_YEAR_PRIOR;
    private final String yearPriorNewerDocEndDate = "06/30/" + ONE_YEAR_PRIOR;
    private final String yearPriorOlderDocStartDate = "04/01/" + ONE_YEAR_PRIOR;
    private final String yearPriorOlderDocEndDate = "04/30/" + ONE_YEAR_PRIOR;

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilder
                .setBorrowerYearsOnThisJob("4")
                .generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilder);
    }

    @AfterEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    @Test
    @Description("IA-2322 When missing base amount in first Paystub, calculate projected from the second one.\n" +
            "IA-2316 Frequency use correct document dates when used 2nd Paystub")
    void twoPaystubCalculation() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", "6000"))
                        .setYtdGrossIncomeAmount("6000"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1111", "4444"))
                        .setYtdGrossIncomeAmount("4444"))
                .importDocumentList();

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestPartIncomeAnnualSummary paystubAnnualSummary = response.getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(2, paystubAnnualSummary.getDocIds().size(), "Two Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getDisplayGrossPay().getValue().toString().startsWith("1111.00"), "Gross Pay should be taken from second Paystub"),
                () -> assertEquals(
                        formatDate(YYYY_MM_DD_DASH, response.getDocumentById(paystubAnnualSummary.getFrequency().getDocumentId()).getPayPeriodStartDate(), MM_DD_YYYY_F_SLASH),
                        olderDocStartDate, "Frequency period start date should be taken from second Paystub"),
                () -> assertEquals(
                        formatDate(YYYY_MM_DD_DASH, response.getDocumentById(paystubAnnualSummary.getFrequency().getDocumentId()).getPayPeriodEndDate(), MM_DD_YYYY_F_SLASH),
                        olderDocEndDate, "Frequency period end date should be taken from second Paystub"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountCalculated().getValue().toString().startsWith("1111.00"), "Projected income should be calculated from second Paystub."));
    }

    @Test
    @Description("IA-2321 (Base Pay) Actual Avg from the PS with highest YTD Gross Pay (latest PS)\n" +
            "IA-2322 Check is Projected was calculated correctly from latest PS")
    void paystubCalculationOnLatestPs() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000", "6000"))
                        .setYtdGrossIncomeAmount("6000"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2000", ""))
                        .setYtdGrossIncomeAmount("4444"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(1, paystubAnnualSummary.getDocIds().size(), "One Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1000.00"), "Actual Avg should be calculated from latest PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("6.00"), "Months should be calculated from latest PS"),
                () -> assertEquals(newerDocEndDate, paystubAnnualSummary.getPayPeriodEndDate().getValue(), "Period End Date should be calculated from latest PS"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountCalculated().getValue().toString().startsWith("1000.00"), "Projected should be calculated from latest PS"));
    }

    @Test
    @Description("IA-2321 (Base Pay) Actual Avg from the PS with highest YTD Gross Pay (2nd PS)")
    void paystubCalculationOnOlderPsWithBaseAmountInSecondPs() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("4444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000", "6000"))
                        .setYtdGrossIncomeAmount("6000"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(1, paystubAnnualSummary.getDocIds().size(), "One Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1500.00"), "Actual Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("4.00"), "Months should be calculated from older PS"),
                () -> assertEquals(olderDocEndDate, paystubAnnualSummary.getPayPeriodEndDate().getValue(), "Period End Date should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountCalculated().getValue().toString().startsWith("1000.00"), "Projected should be calculated from older PS"));
    }

    @Test
    @Description("IA-2321 (Base Pay) Actual Avg from the PS with highest YTD Gross Pay (2nd PS), but Projected from 1st PS")
    void paystubCalculationOnOlderPsWithoutBaseAmountInSecondPs() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("4444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", "6000"))
                        .setYtdGrossIncomeAmount("6000"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(2, paystubAnnualSummary.getDocIds().size(), "Two Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1000.00"), "Actual Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("6.00"), "Months should be calculated from latest PS"),
                () -> assertEquals(newerDocEndDate, paystubAnnualSummary.getPayPeriodEndDate().getValue(), "Period End Date should be calculated from latest PS"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountCalculated().getValue().toString().startsWith("2000.00"), "Projected should be calculated from latest PS"));
    }

    @Test
    @Description("IA-2321 (Base Pay) CoBorrower, One Year Prior Actual Avg from the PS with highest YTD Gross Pay (2nd PS), but Projected from 1st PS")
    void paystubCalculationOnOlderPsWithoutBaseAmountInSecondPsForCoBorrowerOneYearPrior() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        yearPriorNewerDocStartDate,
                        yearPriorNewerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("4444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        yearPriorOlderDocStartDate,
                        yearPriorOlderDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", "6000"))
                        .setYtdGrossIncomeAmount("6000"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getCoBorrowerFullName())
                .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(2, paystubAnnualSummary.getDocIds().size(), "Two Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1000.00"), "Actual Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("6.00"), "Months should be calculated from latest PS"),
                () -> assertEquals(yearPriorNewerDocEndDate, paystubAnnualSummary.getPayPeriodEndDate().getValue(), "Period End Date should be calculated from latest PS"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountCalculated().getValue().toString().startsWith("2000.00"), "Projected should be calculated from latest PS"));
    }


    @Test
    @Description("IA-2324 (Commissions) Calculated from older PS (with highest YTD Gross Pay), frequency too (newer PS is not BASE)")
    void paystubCalculateCommissionAndFrequencyFromPaystubWithHighestGrossPay() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("4444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "", "6600"))
                        .setYtdGrossIncomeAmount("6600"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getCommissions().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1650.00"), "Actual Commission Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("4.00"), "Months should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getGrossAmount().getValue().toString().startsWith("6600.00"), "Gross Commission should be calculated from older PS"));
    }

    @Test
    @Description("IA-2324 (Commissions) Calculate from the PS with highest YTD Gross Pay (older PS), but frequency from newer")
    void paystubCalculateCommissionsFromOlderPsAndFrequencyFromNewer() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "200", "1000"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("5444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "", "6600"))
                        .setYtdGrossIncomeAmount("6600"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getCommissions().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(2, paystubAnnualSummary.getDocIds().size(), "Two Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1100.00"), "Actual Commission Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("6.00"), "Months should be calculated from latest PS"),
                () -> assertEquals(newerDocEndDate, paystubAnnualSummary.getPayPeriodEndDate().getValue(), "Period End Date should be calculated from latest PS"),
                () -> assertEquals(olderDocStartDate, paystubAnnualSummary.getPayPeriodStartDate().getValue(), "Period Start Date should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getGrossAmount().getValue().toString().startsWith("6600.00"), "Gross Commission should be calculated from older PS"));
    }


    @Test
    @Description("IA-2324 (Commissions) CoBorrower, One year prior - Calculate from the PS with highest YTD Gross Pay (older PS), but frequency from newer")
    void paystubCalculateCommissionsFromOlderPsAndFrequencyFromNewerForCoBorrowerOneYearPrior() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        yearPriorNewerDocStartDate,
                        yearPriorNewerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "200", "1000"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("5444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        yearPriorOlderDocStartDate,
                        yearPriorOlderDocEndDate,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "", "6600"))
                        .setYtdGrossIncomeAmount("6600"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getCoBorrowerFullName())
                .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getCommissions().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(2, paystubAnnualSummary.getDocIds().size(), "Two Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1100.00"), "Actual Commission Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("6.00"), "Months should be calculated from latest PS"),
                () -> assertEquals(yearPriorNewerDocEndDate, paystubAnnualSummary.getPayPeriodEndDate().getValue(), "Period End Date should be calculated from latest PS"),
                () -> assertEquals(yearPriorOlderDocStartDate, paystubAnnualSummary.getPayPeriodStartDate().getValue(), "Period Start Date should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getGrossAmount().getValue().toString().startsWith("6600.00"), "Gross Commission should be calculated from older PS"));
    }

    @Test
    @Description("IA-2467 (Bonus) Calculated from older PS (with highest YTD Gross Pay), frequency too (newer PS is not BASE)")
    void paystubCalculateBonusAndFrequencyFromPaystubWithHighestGrossPay() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("4444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "", "6600"))
                        .setYtdGrossIncomeAmount("6600"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBonus().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1650.00"), "Actual Commission Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("4.00"), "Months should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getGrossAmount().getValue().toString().startsWith("6600.00"), "Gross Commission should be calculated from older PS"));
    }

    @Test
    @Description("IA-2467 (Bonus) Calculate from the PS with highest YTD Gross Pay (older PS), but frequency from newer")
    void paystubCalculateBonusFromOlderPsAndFrequencyFromNewer() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        newerDocStartDate,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "200", "1000"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "2000", "4444"))
                        .setYtdGrossIncomeAmount("5444"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "", "6600"))
                        .setYtdGrossIncomeAmount("6600"))
                .importDocumentList();

        RestPartIncomeAnnualSummary paystubAnnualSummary = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBonus().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Defaults assertion",
                () -> assertEquals(2, paystubAnnualSummary.getDocIds().size(), "Two Paystubs should be used to calculation"),
                () -> assertTrue(paystubAnnualSummary.getMonthlyAmountAvg().getValue().toString().startsWith("1100.00"), "Actual Bonus Avg should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getMonths().getValue().toString().startsWith("6.00"), "Months should be calculated from latest PS"),
                () -> assertEquals(newerDocEndDate, paystubAnnualSummary.getPayPeriodEndDate().getValue(), "Period End Date should be calculated from latest PS"),
                () -> assertEquals(olderDocStartDate, paystubAnnualSummary.getPayPeriodStartDate().getValue(), "Period Start Date should be calculated from older PS"),
                () -> assertTrue(paystubAnnualSummary.getGrossAmount().getValue().toString().startsWith("6600.00"), "Gross Bonus should be calculated from older PS"));
    }

}

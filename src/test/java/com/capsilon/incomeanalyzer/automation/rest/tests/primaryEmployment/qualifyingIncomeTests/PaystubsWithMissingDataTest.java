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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "PaystubsWithMissingDataTest")
class PaystubsWithMissingDataTest extends TestBaseRest {

    private final IAFolderBuilder loanPaystubMissingData = createFolderBuilder("IARMissData");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        loanPaystubMissingData
                .setBorrowerYearsOnThisJob("4")
                .generateLoanDocument().restBuild();

        dataUpload = createUploadObject(loanPaystubMissingData);

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                loanPaystubMissingData.getBorrowerFullName(),
                loanPaystubMissingData.getBorrowerSSN(),
                loanPaystubMissingData.getBorrowerCollaboratorId(),
                loanPaystubMissingData.getBorrowerCurrentEmployment(),
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", "")));

        RestGetLoanData.getApplicationData(loanPaystubMissingData.getFolderId());
    }

    @Test
    @Order(1)
    @Description("IA-2287 IA-2785 Check If Footnotes For Ytd Paystub With Missing Ytd Earnings Is Correct")
    void checkIfFootnotesForYtdForPaystubWithMissingYTDEarningsIsCorrect() {
        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(loanPaystubMissingData.getFolderId()).getApplicant(loanPaystubMissingData.getBorrowerFullName())
                .getIncome(loanPaystubMissingData.getBorrowerCurrentEmployment()).getBasePay();
        String footnote = incomePartBasePay.getFootnotes().get(1);
        String footnoteIdx = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getFootnotesIdx().get(0);
        String footnoteText = "Actual YTD Avg Income was not calculated as there are no YTD base amounts available on the paystub.";

        assertAll("Base Pay correct footnote assertion",
                () -> assertEquals("1", footnoteIdx),
                () -> assertEquals(footnoteText, footnote));
    }

    @Test
    @Order(2)
    @Description("IA-2392 IA-2785 Check If Footnotes For Ytd Paystub With Missing Ytd Earnings Using Gross Ytd Earnings Is Correct")
    void checkIfFootnotesForYtdForPaystubWithMissingYTDEarningsUsingGrossYTDEarningsIsCorrect() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                loanPaystubMissingData.getBorrowerFullName(),
                loanPaystubMissingData.getBorrowerSSN(),
                loanPaystubMissingData.getBorrowerCollaboratorId(),
                loanPaystubMissingData.getBorrowerCurrentEmployment(),
                "06/01/" + YEAR_TO_DATE,
                "06/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                .setYtdGrossIncomeAmount("1265"));

        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(loanPaystubMissingData.getFolderId()).getApplicant(loanPaystubMissingData.getBorrowerFullName())
                .getIncome(loanPaystubMissingData.getBorrowerCurrentEmployment()).getBasePay();
        String footnote = incomePartBasePay.getFootnotes().get(1);
        String footnoteIdx = incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getFootnotesIdx().get(0);

        String footnoteText = "The YTD gross pay, which may contain more than just base income, was used in the calculation since there are no YTD base pay amounts available on the paystub.";

        assertAll("Base Pay footnote assertion",
                () -> assertEquals("1", footnoteIdx),
                () -> assertEquals(footnoteText, footnote));
    }

    @Test
    @Order(3)
    @Description("IA-2391 Check If Calculations For Ytd Paystub With Missing Ytd Earnings Using Gross Ytd Earnings Is Correct")
    void checkIfCalculationsForYtdForPaystubWithMissingYTDEarningsUsingGrossYTDEarningsIsCorrect() {
        RestPartIncomeAnnualSummary paystubYTD = RestGetLoanData.getApplicationData(loanPaystubMissingData.getFolderId()).getApplicant(loanPaystubMissingData.getBorrowerFullName()).getIncome(loanPaystubMissingData.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("MONTHLY", paystubYTD.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(210.83), bigD(paystubYTD.getMonthlyAmountAvg().getValue()), "Actual YTD Average should be equal gross/months (1265/6)"),
                () -> assertEquals(bigD(6), bigD(paystubYTD.getMonths().getValue())));
    }
}

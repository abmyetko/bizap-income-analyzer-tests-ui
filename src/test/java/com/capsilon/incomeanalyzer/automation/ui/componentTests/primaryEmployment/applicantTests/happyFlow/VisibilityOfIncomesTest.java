package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests.happyFlow;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Check visibility of different income types")
@Execution(CONCURRENT)
@ResourceLock(value = "VisibilityOfIncomesTest")
class VisibilityOfIncomesTest extends TestBaseUI {

    private final IAFolderBuilder iaBuilder = createFolderBuilder("IAUIVisInc");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iaBuilder
                .setSignDate("02/28/" + YEAR_TO_DATE)
                .setBorrowerYearsOnThisJob("4")
                .setCoBorrowerYearsOnThisJob("4")
                .generateLoanDocumentWithNoIncome();

        iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "0.10");
        iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "0.10");
        if (RestGetLoanData.getActuatorFeatureToggleValue(COMMISSIONS_TOGGLE.value)) {
            iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "0.10");
            iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "0.10");
        }
        if (RestGetLoanData.getActuatorFeatureToggleValue(OVERTIME_TOGGLE.value)) {
            iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "0.10");
            iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "0.10");
        }
        if (RestGetLoanData.getActuatorFeatureToggleValue(BONUS_TOGGLE.value)) {
            iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "0.10");
            iaBuilder.addLoanDocumentTypeOfIncome(iaBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "0.10");
        }

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iaBuilder.uiBuild());

        dataUpload = createUploadObject(iaBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750"),
                        new PaystubData.PaystubIncomeRow(COMMISSION, "", "", "250", "750"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1200", "3600"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "3000"),
                        new PaystubData.PaystubIncomeRow(COMMISSION, "", "", "250", "750"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750"),
                        new PaystubData.PaystubIncomeRow(COMMISSION, "", "", "250", "750"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdBonus("200")
                        .setPriorYearBonus("1800")
                        .setTwoYearPriorBonus("1500")
                        .setYtdOvertime("1500")
                        .setPriorYearOvertime("2800")
                        .setTwoYearPriorOvertime("2500")
                        .setYtdCommission("1500")
                        .setPriorYearCommission("2800")
                        .setTwoYearPriorCommission("2500"))
                .importDocumentList();
        refreshFolder();
    }

    @Test
    @Order(1)
    @Description("Check visibility of Base Pay Values For Borrower Current Job")
    void checkVisibilityOfBasePayForBorrowerCurrentJob() {
        Map allDataFromBasePayYTD = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromBasePayPriorYear = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromBasePayTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY)
                .BODY.employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        assertAll("Base Pay assertions",
                //Base Pay YTD
                () -> assertEquals("Monthly", allDataFromBasePayYTD.get("Pay Frequency"), "[Paystub YTD]Pay Frequency for Borrower Base Pay should be Monthly"),
                () -> assertEquals("$600.00", allDataFromBasePayYTD.get("Gross Pay"), "[Paystub YTD]Gross Pay for Borrower Base Pay should equal $600.00"),
                () -> assertEquals("—", allDataFromBasePayYTD.get("(E)VOE Avg Hours / Period"), "[Paystub YTD] (E)VOE Avg Hours / Period for Borrower Base Pay should equal —"),
                () -> assertEquals("$600.00", allDataFromBasePayYTD.get("Projected Monthly Income"), "[Paystub YTD] Projected Monthly Income for Borrower Base Pay should equal $600.00"),
                () -> assertEquals("$2,400.00", allDataFromBasePayYTD.get("Actual YTD Avg Income"), "[Paystub YTD] Actual YTD Avg Income for Borrower Base Pay should equal $2,400.00"),
                () -> assertEquals("$1,800.00", allDataFromBasePayYTD.get("Difference"), "[Paystub YTD] Difference for Borrower Base Pay should equal $1,800.00"),
                () -> assertEquals("300%", allDataFromBasePayYTD.get("Variance"), "[Paystub YTD] Variance for Borrower Base Pay should equal 300%"),
                () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromBasePayYTD.get("Period End Date"), "[Paystub YTD] Period End Date for Borrower Base Pay should equal 3/31/" + YEAR_TO_DATE),
                () -> assertEquals("3", allDataFromBasePayYTD.get("Months Worked"), "[Paystub YTD] Months Worked for Borrower Base Pay should equal 3"),
                //Base Pay One Year Prior
                () -> assertEquals("Monthly", allDataFromBasePayPriorYear.get("Pay Frequency"), "[Paystub One Year Prior] Pay Frequency for Borrower Base Pay should be Monthly"),
                () -> assertEquals("$1,200.00", allDataFromBasePayPriorYear.get("Gross Pay"), "[Paystub One Year Prior] Gross Pay for Borrower Base Pay should equal $1,200.00"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("(E)VOE Avg Hours / Period"), "[Paystub One Year Prior] (E)VOE Avg Hours / Period for Borrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("Projected Monthly Income"), "[Paystub One Year Prior] Projected Monthly Income for Borrower Base Pay should equal —"),
                () -> assertEquals("$300.00", allDataFromBasePayPriorYear.get("Actual YTD Avg Income"), "[Paystub One Year Prior] Actual YTD Avg Income for Borrower Base Pay should equal $300.00"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("Difference"), "[Paystub One Year Prior] Difference for Borrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("Variance"), "[Paystub One Year Prior] Variance for Borrower Base Pay should equal —"),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromBasePayPriorYear.get("Period End Date"), "[Paystub One Year Prior] Period End Date for Borrower Base Pay should equal 12/31/" + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromBasePayPriorYear.get("Months Worked"), "[Paystub One Year Prior] Months Worked for Borrower Base Pay should equal 12"),
                //Base Pay Two Years Prior
                () -> assertEquals("Monthly", allDataFromBasePayTwoYearsPrior.get("Pay Frequency"), "[Paystub Two Years Prior] Pay Frequency for Borrower Base Pay should be Monthly"),
                () -> assertEquals("$600.00", allDataFromBasePayTwoYearsPrior.get("Gross Pay"), "[Paystub Two Years Prior] Gross Pay for Borrower Base Pay should equal $600.00"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("(E)VOE Avg Hours / Period"), "[Paystub Two Years Prior] (E)VOE Avg Hours / Period for Borrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("Projected Monthly Income"), "[Paystub Two Years Prior] Projected Monthly Income for Borrower Base Pay should equal —"),
                () -> assertEquals("$600.00", allDataFromBasePayTwoYearsPrior.get("Actual YTD Avg Income"), "[Paystub Two Years Prior] Actual YTD Avg Income for Borrower Base Pay should equal $600.00"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("Difference"), "[Paystub Two Years Prior] Difference for Borrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("Variance"), "[Paystub Two Years Prior] Variance for Borrower Base Pay should equal —"),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, allDataFromBasePayTwoYearsPrior.get("Period End Date"), "[Paystub Two Years Prior] Period End Date for Borrower Base Pay should equal " +
                        "12/31/" + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromBasePayTwoYearsPrior.get("Months Worked"), "[Paystub Two Years Prior] Months Worked for Borrower Base Pay should equal 12"));
    }

    @Test
    @Order(2)
    @Description("Check visibility of Base Pay Values For CoBorrower Current Job")
    void checkVisibilityOfBasePayForCoBorrowerCurrentJob() {
        Map allDataFromBasePayYTD = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromBasePayPriorYear = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromBasePayTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY)
                .BODY.employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Base Pay assertions",
                //Base Pay YTD
                () -> assertEquals("Monthly", allDataFromBasePayYTD.get("Pay Frequency"), "[Paystub YTD] Pay Frequency for CoBorrower Base Pay should be Monthly"),
                () -> assertEquals("$1,200.00", allDataFromBasePayYTD.get("Gross Pay"), "[Paystub YTD] Gross Pay for CoBorrower Base Pay should equal $1,200.00"),
                () -> assertEquals("—", allDataFromBasePayYTD.get("(E)VOE Avg Hours / Period"), "[Paystub YTD] (E)VOE Avg Hours / Period for CoBorrower Base Pay should equal —"),
                () -> assertEquals("$1,200.00", allDataFromBasePayYTD.get("Projected Monthly Income"), "[Paystub YTD] Projected Monthly Income for CoBorrower Base Pay should equal $1,200.00"),
                () -> assertEquals("$1,200.00", allDataFromBasePayYTD.get("Actual YTD Avg Income"), "[Paystub YTD] Actual YTD Avg Income for CoBorrower Base Pay should equal $1,200.00"),
                () -> assertEquals("$0.00", allDataFromBasePayYTD.get("Difference"), "[Paystub YTD] Difference for CoBorrower Base Pay should equal $0.00"),
                () -> assertEquals("0%", allDataFromBasePayYTD.get("Variance"), "[Paystub YTD] Variance for CoBorrower Base Pay should equal 0%"),
                () -> assertEquals("1/31/" + YEAR_TO_DATE, allDataFromBasePayYTD.get("Period End Date"), "[Paystub YTD] Period End Date for CoBorrower Base Pay should equal 1/31/" + YEAR_TO_DATE),
                () -> assertEquals("1", allDataFromBasePayYTD.get("Months Worked"), "[Paystub YTD] Months Worked for CoBorrower Base Pay should equal 1"),
                //Base Pay One Year Prior
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("Pay Frequency"), "[Paystub One Year Prior] Pay Frequency for CoBorrower Base Pay should be —"),
                () -> assertEquals("$14,000.00", allDataFromBasePayPriorYear.get("Gross Pay"), "[Paystub One Year Prior] Gross Pay for CoBorrower Base Pay should equal $14,000.00"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("(E)VOE Avg Hours / Period"), "[Paystub One Year Prior] (E)VOE Avg Hours / Period for CoBorrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("Projected Monthly Income"), "[Paystub One Year Prior] Projected Monthly Income for CoBorrower Base Pay should equal —"),
                () -> assertEquals("$1,166.67", allDataFromBasePayPriorYear.get("Actual YTD Avg Income"), "[Paystub One Year Prior] Actual YTD Avg Income for CoBorrower Base Pay should equal $1,166.67"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("Difference"), "[Paystub One Year Prior] Difference for CoBorrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayPriorYear.get("Variance"), "[Paystub One Year Prior] Variance for CoBorrower Base Pay should equal —"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromBasePayPriorYear.get("Period End Date"),
                        "[Paystub One Year Prior] Period End Date for CoBorrower Base Pay should equal " + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromBasePayPriorYear.get("Months Worked"), "[Paystub One Year Prior] Months Worked for CoBorrower Base Pay should equal 12"),
                //Base Pay Two Years Prior
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("Pay Frequency"), "[Paystub Two Years Prior] Pay Frequency for CoBorrower Base Pay should be —"),
                () -> assertEquals("$13,000.00", allDataFromBasePayTwoYearsPrior.get("Gross Pay"), "[Paystub Two Years Prior] Gross Pay for CoBorrower Base Pay should equal $13,000.00"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("(E)VOE Avg Hours / Period"), "[Paystub Two Years Prior] (E)VOE Avg Hours / Period for CoBorrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("Projected Monthly Income"), "[Paystub Two Years Prior] Projected Monthly Income for CoBorrower Base Pay should equal —"),
                () -> assertEquals("$1,083.33", allDataFromBasePayTwoYearsPrior.get("Actual YTD Avg Income"), "[Paystub Two Years Prior] Actual YTD Avg Income for CoBorrower Base Pay should equal $1,083.33"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("Difference"), "[Paystub Two Years Prior] Difference for CoBorrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPrior.get("Variance"), "[Paystub Two Years Prior] Variance for CoBorrower Base Pay should equal —"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromBasePayTwoYearsPrior.get("Period End Date"), "[Paystub Two Years Prior] Period End Date for CoBorrower Base Pay should equal " + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromBasePayTwoYearsPrior.get("Months Worked"), "[Paystub Two Years Prior] Months Worked for CoBorrower Base Pay should equal 12"));
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Order(3)
    @Description("Check visibility of Overtime Values For Borrower Current Job")
    void checkVisibilityOfOvertimeForBorrowerCurrentJob() {
        Map allDataFromOvertimeYTD = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromOvertimePriorYear = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromOvertimeTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME)
                .BODY.employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        assertAll("Overtime assertions",
                //Overtime YTD
                () -> assertEquals("$750.00", allDataFromOvertimeYTD.get("Gross Pay"), "[Paystub YTD] Gross Overtime for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$250.00", allDataFromOvertimeYTD.get("Actual YTD Avg Income"), "[Paystub YTD] Actual Overtime Avg for Borrower Base Pay should equal $250.00"),
                () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromOvertimeYTD.get("Period End Date"), "[Paystub YTD] Period End Date for Borrower Base Pay should equal 3/31/" + YEAR_TO_DATE),
                () -> assertEquals("3", allDataFromOvertimeYTD.get("Months Worked"), "[Paystub YTD] Months Worked for Borrower Base Pay should equal 3"),
                //Overtime One Year Prior
                () -> assertEquals("$750.00", allDataFromOvertimePriorYear.get("Gross Pay"), "[Paystub One Year Prior] Gross Overtime for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$62.50", allDataFromOvertimePriorYear.get("Actual YTD Avg Income"), "[Paystub One Year Prior] Actual Overtime Avg for Borrower Base Pay should equal $62.50"),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromOvertimePriorYear.get("Period End Date"),
                        "[Paystub One Year Prior] Period End Date for Borrower Base Pay should equal 12/31/" + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromOvertimePriorYear.get("Months Worked"), "[Paystub One Year Prior] Months Worked for Borrower Base Pay should equal 12"),
                //Overtime Two Years Prior
                () -> assertEquals("$750.00", allDataFromOvertimeTwoYearsPrior.get("Gross Pay"), "[Paystub Two Years Prior] Gross Overtime for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$62.50", allDataFromOvertimeTwoYearsPrior.get("Actual YTD Avg Income"), "[Paystub Two Years Prior] Actual Overtime Avg for Borrower Base Pay should equal $$62.50"),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, allDataFromOvertimeTwoYearsPrior.get("Period End Date"), "[Paystub Two Years Prior] Period End Date for Borrower Base Pay should equal " +
                        "12/31/" + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromOvertimeTwoYearsPrior.get("Months Worked"), "[Paystub Two Years Prior] Months Worked for Borrower Base Pay should equal 12"));

    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Order(4)
    @Description("Check visibility of Overtime Values For CoBorrower Current Job")
    void checkVisibilityOfOvertimeForCoBorrowerCurrentJob() {
        Map allDataFromOvertimeYTD = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromOvertimePriorYear = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromOvertimeTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME)
                .BODY.employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Overtime assertions",
                //Overtime YTD
                () -> assertEquals("$1,500.00", allDataFromOvertimeYTD.get("Gross Pay"), "[VOE YTD] Gross Overtime for CoBorrower Base Pay should be $1,500.00"),
                () -> assertEquals("$1,500.00", allDataFromOvertimeYTD.get("Actual YTD Avg Income"), "[VOE YTD] Actual Overtime Avg for CoBorrower Base Pay should equal $1,500.00"),
                () -> assertEquals("1/31/" + YEAR_TO_DATE, allDataFromOvertimeYTD.get("Period End Date"), "[VOE YTD] Period End Date for CoBorrower Base Pay should equal 1/31/" + YEAR_TO_DATE),
                () -> assertEquals("1", allDataFromOvertimeYTD.get("Months Worked"), "[VOE YTD] Months Worked for CoBorrower Base Pay should equal 1"),
                //Overtime One Year Prior
                () -> assertEquals("$2,800.00", allDataFromOvertimePriorYear.get("Gross Pay"), "[VOE One Year Prior] Gross Overtime for CoBorrower Base Pay should be $2,800.00"),
                () -> assertEquals("$233.33", allDataFromOvertimePriorYear.get("Actual YTD Avg Income"), "[VOE One Year Prior] Actual Overtime Avg for CoBorrower Base Pay should equal $$233.33"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromOvertimePriorYear.get("Period End Date"), "[VOE One Year Prior] Period End Date for CoBorrower Base Pay should equal " + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromOvertimePriorYear.get("Months Worked"), "[VOE One Year Prior] Months Worked for CoBorrower Base Pay should equal 12"),
                //Overtime Two Years Prior
                () -> assertEquals("$2,500.00", allDataFromOvertimeTwoYearsPrior.get("Gross Pay"), "[VOE Two Years Prior] Gross Overtime for CoBorrower Base Pay should be $2,500.00"),
                () -> assertEquals("$208.33", allDataFromOvertimeTwoYearsPrior.get("Actual YTD Avg Income"), "[VOE Two Years Prior] Actual Overtime Avg for CoBorrower Base Pay should equal $208.33"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromOvertimeTwoYearsPrior.get("Period End Date"), "[VOE Two Years Prior] Period End Date for CoBorrower Base Pay should equal " + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromOvertimeTwoYearsPrior.get("Months Worked"), "[VOE Two Years Prior] Months Worked for CoBorrower Base Pay should equal 12"));
    }

    @Test
    @EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
    @Order(5)
    @Description("Check visibility of Commission Values For Borrower Current Job")
    void checkVisibilityOfCommissionForBorrowerCurrentJob() {
        Map allDataFromCommissionsYTD = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromCommissionsPriorYear = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromCommissionsTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME)
                .BODY.employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        assertAll("Commission assertions",
                //Commissions YTD
                () -> assertEquals("$750.00", allDataFromCommissionsYTD.get("Gross Pay"), "[Paystub YTD] Gross Commission for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$250.00", allDataFromCommissionsYTD.get("Actual YTD Avg Income"), "[Paystub YTD] Actual Commissions Avg for Borrower Base Pay should equal $250.00"),
                () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromCommissionsYTD.get("Period End Date"), "[Paystub YTD] Period End Date for Borrower Base Pay should equal 3/31/" + YEAR_TO_DATE),
                () -> assertEquals("3", allDataFromCommissionsYTD.get("Months Worked"), "[Paystub YTD] Months Worked for Borrower Base Pay should equal 3"),
                //Commissions One Year Prior
                () -> assertEquals("$750.00", allDataFromCommissionsPriorYear.get("Gross Pay"), "[Paystub One Year Prior] Gross Commission for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$62.50", allDataFromCommissionsPriorYear.get("Actual YTD Avg Income"), "[Paystub One Year Prior] Actual Commissions Avg for Borrower Base Pay should equal $62.50"),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromCommissionsPriorYear.get("Period End Date"), "[Paystub One Year Prior] Period End Date for Borrower Base Pay should equal " +
                        "12/31/" + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromCommissionsPriorYear.get("Months Worked"), "[Paystub One Year Prior] Months Worked for Borrower Base Pay should equal 12"),
                //Commissions Two Years Prior
                () -> assertEquals("$750.00", allDataFromCommissionsTwoYearsPrior.get("Gross Pay"), "[Paystub Two Years Prior] Gross Commission for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$62.50", allDataFromCommissionsTwoYearsPrior.get("Actual YTD Avg Income"), "[Paystub Two Years Prior] Actual Commissions Avg for Borrower Base Pay should equal $$62.50"),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, allDataFromCommissionsTwoYearsPrior.get("Period End Date"), "[Paystub Two Years Prior] Period End Date for Borrower Base Pay should " +
                        "equal 12/31/" + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromCommissionsTwoYearsPrior.get("Months Worked"), "[Paystub Two Years Prior] Months Worked for Borrower Base Pay should equal 12"));
    }

    @Test
    @EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
    @Order(6)
    @Description("Check visibility of Commission Values For CoBorrower Current Job")
    void checkVisibilityOfCommissionsForCoBorrowerCurrentJob() {
        Map allDataFromCommissionsYTD = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromCommissionsPriorYear = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME)
                .BODY.employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromCommissionsTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME)
                .BODY.employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Commission assertions",
                //Commissions YTD
                () -> assertEquals("$1,500.00", allDataFromCommissionsYTD.get("Gross Pay"), "[VOE YTD] Gross Commission for CoBorrower Base Pay should be $1,500.00"),
                () -> assertEquals("$1,500.00", allDataFromCommissionsYTD.get("Actual YTD Avg Income"), "[VOE YTD] Actual Commissions Avg for CoBorrower Base Pay should equal $1,500.00"),
                () -> assertEquals("1/31/" + YEAR_TO_DATE, allDataFromCommissionsYTD.get("Period End Date"), "[VOE YTD] Period End Date for CoBorrower Base Pay should equal 1/31/" + YEAR_TO_DATE),
                () -> assertEquals("1", allDataFromCommissionsYTD.get("Months Worked"), "[VOE YTD] Months Worked for CoBorrower Base Pay should equal 1"),
                //Commissions One Year Prior
                () -> assertEquals("$2,800.00", allDataFromCommissionsPriorYear.get("Gross Pay"), "[VOE One Year Prior] Gross Commission for CoBorrower Base Pay should be $2,800.00"),
                () -> assertEquals("$233.33", allDataFromCommissionsPriorYear.get("Actual YTD Avg Income"), "[VOE One Year Prior] Actual Commissions Avg for CoBorrower Base Pay should equal $233.33"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromCommissionsPriorYear.get("Period End Date"), "[VOE One Year Prior] Period End Date for Coorrower Base Pay should equal " + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromCommissionsPriorYear.get("Months Worked"), "[VOE One Year Prior] Months Worked for CoBorrower Base Pay should equal 12"),
                //Commissions Two Years Prior
                () -> assertEquals("$2,500.00", allDataFromCommissionsTwoYearsPrior.get("Gross Pay"), "[VOE Two Years Prior] Gross Commission for CoBorrower Base Pay should be $2,500.00"),
                () -> assertEquals("$208.33", allDataFromCommissionsTwoYearsPrior.get("Actual YTD Avg Income"), "[VOE Two Years Prior] Actual Commissions Avg for CoBorrower Base Pay should equal $208.33"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromCommissionsTwoYearsPrior.get("Period End Date"), "[VOE Two Years Prior] Period End Date for CoBorrower Base Pay should equal " + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromCommissionsTwoYearsPrior.get("Months Worked"), "[VOE Two Years Prior] Months Worked for CoBorrower Base Pay should equal 12"));
    }


    @Test
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @Order(7)
    @Description("IA-2516 IA-1232 Check visibility of Bonus Values For Borrower Current Job")
    void checkVisibilityOfBonusForBorrowerCurrentJob() {
        Map allDataFromBonusYTD = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromBonusPriorYear = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromBonusTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY
                .employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        assertAll("Bonus assertions",
                //Bonus YTD
                () -> assertEquals("$750.00", allDataFromBonusYTD.get("Gross Pay"), "[Paystub YTD] YTD Bonus for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$62.50", allDataFromBonusYTD.get("Annualized Bonus Avg"), "[Paystub YTD] Annualized Bonus Avg for Borrower Base Pay should equal $62.50"),
                () -> assertEquals("$250.00", allDataFromBonusYTD.get("Actual YTD Avg Income"), "[Paystub YTD] Actual YTD Avg Income for Borrower Base Pay should equal $250.00"),
                () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromBonusYTD.get("Period End Date"), "[Paystub YTD] Period End Date for Borrower Base Pay should equal 3/31/" + YEAR_TO_DATE),
                () -> assertEquals("3", allDataFromBonusYTD.get("Months Worked"), "[Paystub YTD] Months Worked for Borrower Base Pay should equal 3"),
                //Bonus One Year Prior
                () -> assertEquals("$3,000.00", allDataFromBonusPriorYear.get("Gross Pay"), "[Paystub One Year Prior] YTD Bonus for Borrower Base Pay should be $3,000.00"),
                () -> assertEquals("$250.00", allDataFromBonusPriorYear.get("Annualized Bonus Avg"), "[Paystub One Year Prior] Annualized Bonus Avg for Borrower Base Pay should equal $250.00"),
                () -> assertEquals("$250.00", allDataFromBonusPriorYear.get("Actual YTD Avg Income"), "[Paystub One Year Prior] Actual YTD Avg Income for Borrower Base Pay should equal $250.00"),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromBonusPriorYear.get("Period End Date"), "[Paystub One Year Prior] Period End Date for Borrower Base Pay should equal 12/31/" + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromBonusPriorYear.get("Months Worked"), "[Paystub One Year Prior] Months Worked for Borrower Base Pay should equal 12"),
                //Bonus Two Years Prior
                () -> assertEquals("$750.00", allDataFromBonusTwoYearsPrior.get("Gross Pay"), "[Paystub Two Years Prior] YTD Bonus for Borrower Base Pay should be $750.00"),
                () -> assertEquals("$62.50", allDataFromBonusTwoYearsPrior.get("Annualized Bonus Avg"), "[Paystub Two Years Prior] Annualized Bonus Avg for Borrower Base Pay should equal $62.50"),
                () -> assertEquals("$62.50", allDataFromBonusTwoYearsPrior.get("Actual YTD Avg Income"), "[Paystub Two Years Prior] Actual YTD Avg Income for Borrower Base Pay should equal $62.50"),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, allDataFromBonusTwoYearsPrior.get("Period End Date"), "[Paystub Two Years Prior] Period End Date for Borrower Base Pay should equal " +
                        "12/31/" + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromBonusTwoYearsPrior.get("Months Worked"), "[Paystub Two Years Prior] Months Worked for Borrower Base Pay should equal 12"));
    }

    @Test
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @Order(8)
    @Description("IA-2516 IA-1232 Check visibility of Bonus Values For CoBorrower Current Job")
    void checkVisibilityOfBonusForCoBorrowerCurrentJob() {
        Map allDataFromBonusYTD = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromBonusPriorYear = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromBonusTwoYearsPrior = new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY
                .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Bonus assertions",
                //Bonus YTD
                () -> assertEquals("$200.00", allDataFromBonusYTD.get("Gross Pay"), "[VOE YTD] YTD Bonus for CoBorrower Base Pay should be $200.00"),
                () -> assertEquals("$16.67", allDataFromBonusYTD.get("Annualized Bonus Avg"), "[VOE YTD] Annualized Bonus Avg for CoBorrower Base Pay should equal $$16.67"),
                () -> assertEquals("$200.00", allDataFromBonusYTD.get("Actual YTD Avg Income"), "[VOE YTD] Actual YTD Avg Income for CoBorrower Base Pay should equal $200.00"),
                () -> assertEquals("1/31/" + YEAR_TO_DATE, allDataFromBonusYTD.get("Period End Date"), "[VOE YTD] Period End Date for CoBorrower Base Pay should equal 1/31/" + YEAR_TO_DATE),
                () -> assertEquals("1", allDataFromBonusYTD.get("Months Worked"), "[VOE YTD] Months Worked for CoBorrower Base Pay should equal 1"),
                //Bonus One Year Prior
                () -> assertEquals("$1,800.00", allDataFromBonusPriorYear.get("Gross Pay"), "[VOE One Year Prior] YTD Bonus for CoBorrower Base Pay should be $250.00"),
                () -> assertEquals("$150.00", allDataFromBonusPriorYear.get("Annualized Bonus Avg"), "[VOE One Year Prior] Annualized Bonus Avg for CoBorrower Base Pay should equal $150.00"),
                () -> assertEquals("$150.00", allDataFromBonusPriorYear.get("Actual YTD Avg Income"), "[VOE One Year Prior] Actual YTD Avg Income for CoBorrower Base Pay should equal $150.00"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromBonusPriorYear.get("Period End Date"), "[VOE One Year Prior] Period End Date for CoBorrower Base Pay should equal " + ONE_YEAR_PRIOR),
                () -> assertEquals("12", allDataFromBonusPriorYear.get("Months Worked"), "[VOE One Year Prior] Months Worked for CoBorrower Base Pay should equal 12"),
                //Bonus Two Years Prior
                () -> assertEquals("$1,500.00", allDataFromBonusTwoYearsPrior.get("Gross Pay"), "[VOE Two Years Prior] YTD Bonus for CoBorrower Base Pay should be $1,500.00"),
                () -> assertEquals("$125.00", allDataFromBonusTwoYearsPrior.get("Annualized Bonus Avg"), "[VOE Two Years Prior] Annualized Bonus Avg for CoBorrower Base Pay should equal $125.00"),
                () -> assertEquals("$125.00", allDataFromBonusTwoYearsPrior.get("Actual YTD Avg Income"), "[VOE Two Years Prior] Actual YTD Avg Income for CoBorrower Base Pay should equal $125.00"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromBonusTwoYearsPrior.get("Period End Date"), "[VOE Two Years Prior] Period End Date for CoBorrower Base Pay should equal " + TWO_YEARS_PRIOR),
                () -> assertEquals("12", allDataFromBonusTwoYearsPrior.get("Months Worked"), "[VOE Two Years Prior] Months Worked for CoBorrower Base Pay should equal 12"));
    }

    @Test
    @EnableIfToggled(propertyName = TEACHERS_TOGGLE)
    @Order(9)
    @Description("Check visibility of Months Paid in Base Pay for Borrowers current and previous job")
    void checkVisibilityOfMonthsPaidForBorrowersForCurrentAndPreviousJob() {
        Map allDataFromBasePayYTDBorrower = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString())
                .document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromBasePayPriorYearBorrower = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        Map allDataFromBasePayTwoYearsPriorBorrower = new ApplicantView().applicant(iaBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY)
                .BODY.employment(iaBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        assertAll("Months Paid Borrower current job assertions",
                () -> assertEquals("12", allDataFromBasePayYTDBorrower.get("Months Paid Per Year"), "[Paystub YTD] Months Paid Per Year for Borrower Base Pay should equal 12"),
                () -> assertEquals("—", allDataFromBasePayPriorYearBorrower.get("Months Paid Per Year"), "[Paystub One Year Prior] Months Paid Per Year for Borrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPriorBorrower.get("Months Paid Per Year"), "[Paystub Two Years Prior] Months Paid Per Year for Borrower Base Pay should equal —"));

        Map allDataFromBasePayYTDCoBorrower =
                new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                        .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromBasePayPriorYearCoBorrower =
                new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                        .employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        Map allDataFromBasePayTwoYearsPriorCoBorrower =
                new ApplicantView().applicant(iaBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY)
                        .BODY.employment(iaBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Months Paid CoBorrower previous job assertions",
                () -> assertEquals("12", allDataFromBasePayYTDCoBorrower.get("Months Paid Per Year"), "[Paystub YTD] Months Paid Per Year for CoBorrower Base Pay should equal 12"),
                () -> assertEquals("—", allDataFromBasePayPriorYearCoBorrower.get("Months Paid Per Year"), "[Paystub One Year Prior] Months Paid Per Year for CoBorrower Base Pay should equal —"),
                () -> assertEquals("—", allDataFromBasePayTwoYearsPriorCoBorrower.get("Months Paid Per Year"), "[Paystub Two Years Prior] Months Paid Per Year for CoBorrower Base Pay should equal —"));
    }
}

package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests.happyFlow;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.averages.HistoricalAverages;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Happy Path for SalariedCurrentEmployerTest")
@Tag("integration")
@Execution(CONCURRENT)
@ResourceLock(value = "SalariedCurrentEmployerTest")
class SalariedCurrentEmployerTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUISalCET");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iafolderBuilder
                .setSignDate("02/28/" + YEAR_TO_DATE)

                .setBorrowerFirstName("John")
                .setBorrowerLastName("Homeowner")
                .setBorrowerCurrentEmployment("Awesome Computers Inc")
                .setBorrowerYearsOnThisJob("2")
                .setBorrowerPreviousEmployment("Previous Computer Inc")
                .setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("06/30/" + ONE_YEAR_PRIOR)

                .setCoBorrowerFirstName("Mary")
                .setCoBorrowerLastName("Homeowner")
                .setCoBorrowerCurrentEmployment("Blue Younder Airlines Inc")
                .setCoBorrowerYearsOnThisJob("2")
                .setCoBorrowerPreviousEmployment("Previous Blue Inc")
                .setCoBorrowerPreviousJobStartDate("01/01/" + ONE_YEAR_PRIOR)
                .setCoBorrowerPreviousJobEndDate("06/30/" + ONE_YEAR_PRIOR)
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);

        refreshFolder();
    }

    @Test
    @Order(1)
    void projectedMonthlyIncomeShouldBeSelectedFoYTDPaystub() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "15000,3"))
                        .setPayDate("06/30/" + YEAR_TO_DATE));

        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentLocatorsMap().get("Projected Monthly Income")
                .$x("./mat-radio-button").shouldHave(Condition.cssClass("mat-radio-checked"));
    }

    @Test
    @Order(2)
    void checkIfTypeIsSalary() {
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.TAG_TYPE.shouldHave(Condition.text("Salary"));
    }

    @Test
    @Order(3)
    void checkIfDataAndCalculationsForYTDPaystubAreCorrect() {
        ApplicantView applicantView = new ApplicantView();
        Map<String, String> allDataFromYTD = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        assertAll("Ytd paystub field values",
                () -> assertEquals("Monthly", allDataFromYTD.get("Pay Frequency")),
                () -> assertEquals("$2,500.05", allDataFromYTD.get("Gross Pay")),
                () -> assertEquals("$2,500.05", allDataFromYTD.get("Projected Monthly Income")),
                () -> assertEquals("$25,000.50", allDataFromYTD.get("Actual YTD Avg Income")),
                () -> assertEquals("$22,500.45", allDataFromYTD.get("Difference")),
                () -> assertEquals("900%", allDataFromYTD.get("Variance")),
                () -> assertEquals("6/30/" + YEAR_TO_DATE, allDataFromYTD.get("Period End Date")),
                () -> assertEquals("6", allDataFromYTD.get("Months Worked"))
        );


    }

    @Test
    @Order(4)
    void uploadW2sAndCheckdDataAndCalculations() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomW2(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "25000",
                        ONE_YEAR_PRIOR.toString()))
                .addDocument(dataUpload.createCustomW2(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "25000",
                        TWO_YEARS_PRIOR.toString()))
                .importDocumentList();

        ApplicantView applicantView = new ApplicantView();
        Map<String, String> allDataFromPriorYear = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.W2).getDocumentValuesMap();

        assertAll("Prior year W-2 field values",
                () -> assertEquals("$25,000.00", allDataFromPriorYear.get("Gross Pay")),
                () -> assertEquals("$2,083.33", allDataFromPriorYear.get("Actual YTD Avg Income")),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromPriorYear.get("Period End Date")),
                () -> assertEquals("12", allDataFromPriorYear.get("Months Worked"))
        );

        Map<String, String> allDataFromTwoYearsPrior = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.W2).getDocumentValuesMap();

        assertAll("Two years prior W-2 field values",
                () -> assertEquals("$25,000.00", allDataFromTwoYearsPrior.get("Gross Pay")),
                () -> assertEquals("$2,083.33", allDataFromTwoYearsPrior.get("Actual YTD Avg Income")),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromTwoYearsPrior.get("Period End Date")),
                () -> assertEquals("12", allDataFromTwoYearsPrior.get("Months Worked"))
        );
    }

    @Test
    @Order(5)
    void checkHistoricalAveragesForCurrentJob() {
        ApplicantView applicantView = new ApplicantView();
        HistoricalAverages dataFromHistoricalAverages = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.HISTORICAL_AVERAGES;
        dataFromHistoricalAverages.COMPONENT_CONTAINER.scrollIntoView(true);
        //YTD
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG).YEARS_PILLS.get(0).shouldHave(attributeContains("class", "pill active"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG).MONTHS_WORKED.shouldHave(Condition.text("6"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$2,500.05"));

        //PRIOR YEAR
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).YEARS_PILLS.get(0).shouldHave(attributeContains("class", "pill active"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).YEARS_PILLS.get(1).shouldHave(attributeContains("class", "pill active"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).MONTHS_WORKED.shouldHave(Condition.text("18"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$2,222.24"));

        //YEAR BEFORE PRIOR YEAR
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).YEARS_PILLS.get(0).shouldHave(attributeContains("class", "pill active"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).YEARS_PILLS.get(1).shouldHave(attributeContains("class", "pill active"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).YEARS_PILLS.get(2).shouldHave(attributeContains("class", "pill active"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).MONTHS_WORKED.shouldHave(Condition.text("30"));
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$2,166.68"));
    }

    @Test
    @Order(6)
    void uploadVOEAndCheckCalculations() {
        dataUpload.clearDocuments()
                .importDocument((dataUpload.createCustomVoeCurrent(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "2800",
                        "24000",
                        "03/31/" + YEAR_TO_DATE))
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000"));

        ApplicantView applicantView = new ApplicantView();
        Map<String, String> allDataFromYTD = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Ytd VOE field values",
                () -> assertEquals("Monthly", allDataFromYTD.get("Pay Frequency")),
                () -> assertEquals("$2,800.00", allDataFromYTD.get("Gross Pay")),
                () -> assertEquals("$2,800.00", allDataFromYTD.get("Projected Monthly Income")),
                () -> assertEquals("$8,000.00", allDataFromYTD.get("Actual YTD Avg Income")),
                () -> assertEquals("$5,200.00", allDataFromYTD.get("Difference")),
                () -> assertEquals("185.71%", allDataFromYTD.get("Variance")),
                () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromYTD.get("Period End Date")),
                () -> assertEquals("3", allDataFromYTD.get("Months Worked"))
        );

        Map<String, String> allDataFromPriorYear = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Prior year VOE field values",
                () -> assertEquals("$26,000.00", allDataFromPriorYear.get("Gross Pay")),
                () -> assertEquals("$2,166.67", allDataFromPriorYear.get("Actual YTD Avg Income")),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromPriorYear.get("Period End Date")),
                () -> assertEquals("12", allDataFromPriorYear.get("Months Worked"))
        );

        Map<String, String> allDataFromTwoYearsPrior = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertAll("Two years prior VOE field values",
                () -> assertEquals("$20,000.00", allDataFromTwoYearsPrior.get("Gross Pay")),
                () -> assertEquals("$6,666.67", allDataFromTwoYearsPrior.get("Actual YTD Avg Income")),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromTwoYearsPrior.get("Period End Date")),
                () -> assertEquals("3", allDataFromTwoYearsPrior.get("Months Worked"))
        );
    }


    @Test
    @Order(7)
    void checkDefaultsForCurrentEmployment() {
        ApplicantView applicantView = new ApplicantView();
        //YTD
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentLocatorsMap().get("Projected Monthly Income")
                .$x("./mat-radio-button").shouldHave(Condition.cssClass("mat-radio-checked"));
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentLocatorsMap().get("Actual YTD Avg Income")
                .$x("./mat-radio-button").shouldNotHave(Condition.cssClass("mat-radio-checked"));
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentLocatorsMap().get("Projected Monthly Income")
                .$x("./mat-radio-button").shouldNotHave(Condition.cssClass("mat-radio-checked"));
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentLocatorsMap().get("Actual YTD Avg Income")
                .$x("./mat-radio-button").shouldNotHave(Condition.cssClass("mat-radio-checked"));

        //PRIOR YEAR
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentLocatorsMap().get("Actual YTD Avg Income")
                .$x("./mat-radio-button").shouldNotHave(Condition.cssClass("mat-radio-checked"));
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.W2).getDocumentLocatorsMap().get("Actual YTD Avg Income")
                .$x("./mat-radio-button").shouldHave(Condition.cssClass("mat-radio-checked"));

        //YEAR BEFORE PRIOR YEAR
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.W2).getDocumentLocatorsMap().get("Actual YTD Avg Income")
                .$x("./mat-radio-button").shouldNotHave(Condition.cssClass("mat-radio-checked"));
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentLocatorsMap().get("Actual YTD Avg Income")
                .$x("./mat-radio-button").shouldHave(Condition.cssClass("mat-radio-checked"));
    }

    @Test
    @Order(8)
    void checkDefaultsForHistoricalAverages() {
        ApplicantView applicantView = new ApplicantView();
        HistoricalAverages dataFromHistoricalAverages = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.HISTORICAL_AVERAGES;
        //YTD
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG).RADIO_BUTTON_FIELD.$x("./mat-radio-button").shouldHave(Condition.cssClass("mat-radio-checked"));

        //PRIOR YEAR
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).RADIO_BUTTON_FIELD.$x("./mat-radio-button").shouldNotHave(Condition.cssClass("mat-radio-checked"));

        //YEAR BEFORE PRIOR YEAR
        dataFromHistoricalAverages.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).RADIO_BUTTON_FIELD.$x("./mat-radio-button").shouldNotHave(Condition.cssClass("mat-radio-checked"));
    }
}

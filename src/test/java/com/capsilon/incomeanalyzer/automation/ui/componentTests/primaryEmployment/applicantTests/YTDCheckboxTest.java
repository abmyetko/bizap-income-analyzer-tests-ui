package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.averages.HistoricalAverages;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentBody;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.reportportal.Description;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.TEACHERS_TOGGLE;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Ytd Checkbox UI Test")
@EnableIfToggled(propertyName = TEACHERS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "YTDCheckboxTest")
class YTDCheckboxTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIYdChBox");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iafolderBuilder
                .setBorrowerYearsOnThisJob("3")
                .setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("06/30/" + YEAR_TO_DATE)

                .setCoBorrowerYearsOnThisJob("3")
                .setCoBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setCoBorrowerPreviousJobEndDate("06/30/" + YEAR_TO_DATE)
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,000.00", "12,000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,000.00", "12,000.00")))
                .importDocumentList();

        refreshFolder();
    }

    @Test
    @Order(1)
    @Tag("integration")
    @Tag("health")
    @Description("IA-2275 Check If Historical Average Values Have Been Calculated Correctly And Proper Radio Button Has Been Selected")
    void checkCalculationsAndIfCheckboxesAreSelected() {
        ApplicantView applicantView = new ApplicantView();

        //Check if all checkboxes are selected
        EmploymentBody dataFromCurrentEmployment = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

        dataFromCurrentEmployment.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        dataFromCurrentEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        dataFromCurrentEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();

        //Check Historical Averages calculations
        HistoricalAverages allDataFromHistoricalAvg = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.HISTORICAL_AVERAGES;

        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).MONTHS_WORKED.shouldHave(Condition.text("3 months"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,193.28"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).RADIO_BUTTON_FIELD.$x("./mat-radio-button").shouldHave(Condition.cssClass("mat-radio-checked"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).MONTHS_WORKED.shouldHave(Condition.text("15 months"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,038.66"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,021.48"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).MONTHS_WORKED.shouldHave(Condition.text("27 months"));
    }

    @Test
    @Order(2)
    @Description("IA-2275 Check If Historical Average Values Are Calculated Correctly After Deselecting Ytd Income")
    void unselectYtdCheckboxAndCheckCalculations() {
        ApplicantView applicantView = new ApplicantView();

        //Unselect YTD income
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.setCheckboxValue(false);

        //Check Annual Trending calculations
        HistoricalAverages allDataFromHistoricalAvg = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.HISTORICAL_AVERAGES;

        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).MONTHS_WORKED.shouldHave(Condition.text("—"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).AVG_MONTHLY_INCOME.shouldHave(Condition.text("—"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).MONTHS_WORKED.shouldHave(Condition.text("12 months"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,000.00"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).MONTHS_WORKED.shouldHave(Condition.text("24 months"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,000.00"));
    }

    @Test
    @Order(3)
    @Description("IA-2275 Check If Deselecting Employer Ytd Income Does Not Deselect Previous Years")
    void checkIfAfterYTDCheckboxUnselectPreviousYearsCheckboxesAreStillSelected() {
        ApplicantView applicantView = new ApplicantView();

        EmploymentBody dataFromCurrentEmployment = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

        dataFromCurrentEmployment.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        dataFromCurrentEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        dataFromCurrentEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
    }

    @Test
    @Order(4)
    @Description("IA-2275 Check If Historical Average Values Are Calculated Correctly When Employer Ytd Income Has Been Selected Again")
    void selectYtdCheckboxAndCheckCalculations() {
        ApplicantView applicantView = new ApplicantView();

        //Select YTD Checkbox
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.setCheckboxValue(true);

        //Check if all checkboxes are selected
        EmploymentBody dataFromCurrentEmployment = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

        dataFromCurrentEmployment.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        dataFromCurrentEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        dataFromCurrentEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();

        //Check Historical Averages calculations
        HistoricalAverages allDataFromHistoricalAvg = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.HISTORICAL_AVERAGES;

        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).MONTHS_WORKED.shouldHave(Condition.text("3 months"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,193.28"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG).RADIO_BUTTON_FIELD.$x("./mat-radio-button").shouldHave(Condition.cssClass("mat-radio-checked"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).MONTHS_WORKED.shouldHave(Condition.text("15 months"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,038.66"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$1,021.48"));
        allDataFromHistoricalAvg.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).MONTHS_WORKED.shouldHave(Condition.text("27 months"));
    }
}

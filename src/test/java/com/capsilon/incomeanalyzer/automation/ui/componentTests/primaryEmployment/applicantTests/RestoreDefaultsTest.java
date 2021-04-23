package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.RestCommons;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.ApplicantIncomeCategory;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentBody;
import com.capsilon.incomeanalyzer.automation.ui.component.restore.defaults.RestoreDefaults;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import com.codeborne.selenide.ClickOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Check if restore defaults button works fine")
@Execution(CONCURRENT)
@ResourceLock(value = "RestoreDefaultsTest")
class RestoreDefaultsTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIRestore");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iafolderBuilder
                .setSignDate("02/28/" + YEAR_TO_DATE)

                .setBorrowerFirstName("John")
                .setBorrowerLastName("Homeowner")
                .setBorrowerCurrentEmployment("Awesome Computers Inc")
                .setBorrowerYearsOnThisJob("3")
                .setBorrowerPreviousEmployment("Previous Computer Inc")
                .setBorrowerPreviousJobStartDate("01/01/2015")
                .setBorrowerPreviousJobEndDate("06/30/2018")

                .setCoBorrowerFirstName("Mary")
                .setCoBorrowerLastName("Homeowner")
                .setCoBorrowerCurrentEmployment("Blue Younder Airlines Inc")
                .setCoBorrowerYearsOnThisJob("3")
                .setCoBorrowerPreviousEmployment("Previous Blue Inc")
                .setCoBorrowerPreviousJobStartDate("01/01/2015")
                .setCoBorrowerPreviousJobEndDate("06/30/2018")
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);

        refreshFolder();
    }

    @Test
    @Order(1)
    @Description("IA-2422 [Borrower] Check If Restore Defaults Dialog is Correct and after clicking Restore calculations back to defaults")
    void checkMessageAfterClickRestoreDefaultsButtonForBorrower() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "180.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "20.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .importDocumentList();

        ApplicantView applicantView = new ApplicantView();

        EmploymentBody year = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY;

        year.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.setCheckboxValue(false);

        ApplicantIncomeCategory body = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2);

        RestoreDefaults restoreDefaultsDialog = body.HEADER.restoreDefaults();

        assertEquals("Please confirm that you'd like to return to the default calculations for " + iafolderBuilder.getBorrowerFullName() + ". Note that all of your selections will be lost.",
                restoreDefaultsDialog.RESTORE_MESSAGE.getText());

        restoreDefaultsDialog.BUTTON_RESTORE.click();

        year.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
    }

    @Test
    @Order(2)
    @Description("IA-2422 [CoBorrower] Check If Restore Defaults Dialog is Correct and after clicking Cancel calculations don't back to defaults")
    void checkMessageAfterClickCancelDefaultsButtonForCoBorrower() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "180.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "20.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .importDocumentList();

        ApplicantView applicantView = new ApplicantView();

        EmploymentBody year = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getCoBorrowerCurrentEmployment())
                .BODY;

        year.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.setCheckboxValue(false);

        ApplicantIncomeCategory body = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2);

        RestoreDefaults restoreDefaultsDialog = body.HEADER.restoreDefaults();

        assertEquals("Please confirm that you'd like to return to the default calculations for " + iafolderBuilder.getCoBorrowerFullName() + ". Note that all of your selections will be lost.",
                restoreDefaultsDialog.RESTORE_MESSAGE.getText());

        restoreDefaultsDialog.BUTTON_CANCEL.click();

        year.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
    }

    @Test
    @Order(3)
    @Description("IA-2656 Check If Restore Defaults Button Is Disabled For Locked Or Opted-out Loans")
    void checkIfRestoreDefaultsButtonIsDisabledForLockedOrOptedOutLoans() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "04/01/" + YEAR_TO_DATE,
                        "04/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "3990.00")));

        ApplicantView applicantView = new ApplicantView();

        EmploymentBody year = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY;

        year.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentLocatorsMap().get("Actual YTD Avg Income")
                .$x("./mat-radio-button//span[contains(@class,'mat-radio-outer-circle')]").click(ClickOptions.usingJavaScript());
        year.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentLocatorsMap().get("Projected Monthly Income")
                .$x("./mat-radio-button//span[contains(@class,'mat-radio-outer-circle')]").click(ClickOptions.usingJavaScript());
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.RESTORE_DEFAULTS_BUTTON
                .shouldNotHave(attributeContains("disabled", "true")).scrollIntoView(true);

        RestNotIARequests.setCduStatus(iafolderBuilder.getFolderId(), CduStatus.REMOVED, "IA Disable Restore Defaults Button");
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.RESTORE_DEFAULTS_BUTTON
                .shouldHave(attributeContains("disabled", "true"));

        RestNotIARequests.setCduStatus(iafolderBuilder.getFolderId(), CduStatus.IN_PROGRESS, "IA Disable Restore Defaults Button");
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.RESTORE_DEFAULTS_BUTTON
                .shouldNotHave(attributeContains("disabled", "true"));

        if (!RestGetLoanData.getActuatorFeatureToggleValue(String.format(PropertyToggles.AUTOMATIC_LOCK.value, RestCommons.getSiteGuid()))) {
            RestNotIARequests.waiveChecklistRules(iafolderBuilder.getFolderId(),
                    RestNotIARequests.getFailedChecklistRulesIds(iafolderBuilder.getFolderId()), "IA Disable Restore Defaults Button");
            RestNotIARequests.setCduStatus(iafolderBuilder.getFolderId(), CduStatus.LOCKED, "IA Disable Restore Defaults Button");
            applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.RESTORE_DEFAULTS_BUTTON
                    .shouldHave(attributeContains("disabled", "true"));
        }
    }
}

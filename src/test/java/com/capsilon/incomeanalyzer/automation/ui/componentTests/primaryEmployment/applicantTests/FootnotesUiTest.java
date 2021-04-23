package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.reportportal.Description;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Footnotes UI test")
@Execution(CONCURRENT)
@ResourceLock(value = "FootnotesUiTest")
class FootnotesUiTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIFootnot");

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
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "180.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "20.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/07/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .importDocumentList();

        refreshFolder();
    }

    @Test
    @Order(1)
    @Description("IA-1723 Check If Footnote For Capped Hours Different Rate Monthly Is Visible")
    void checkIfFootnoteForCappedHoursDifferentRateMonthlyIsVisible() {
        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("The maximum hours allowed for this pay frequency (173.34 hours) was used instead of the 254.5 hours reported on the paystub. " +
                        "The lowest rate ($30) reported on the paystub was used."));
    }

    @Test
    @Order(2)
    @Description("IA-1723 Check If Footnote For Capped Hours Different Rate Weekly Is Visible")
    void checkIfFootnoteForCappedHoursDifferentRateWeeklyIsVisible() {
        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("The maximum hours allowed for this pay frequency (40 hours) was used instead of the 51.5 hours reported on the paystub. " +
                        "The lowest rate ($17.52) reported on the paystub was used."));
    }

    @Test
    @Order(3)
    void checkIfFootnoteForCappedHoursSameRateWeeklyIsVisible() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                iafolderBuilder.getCoBorrowerFullName(),
                iafolderBuilder.getCoBorrowerSSN(),
                iafolderBuilder.getCoBorrowerCollaboratorId(),
                iafolderBuilder.getCoBorrowerCurrentEmployment(),
                "04/01/" + YEAR_TO_DATE,
                "04/07/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "17.52", "16.00", "296.00", "1,308.20"),
                new PaystubData.PaystubIncomeRow(REGULAR, "17.52", "30.50", "1,193.28", "32,632.21"),
                new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")));

        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("The maximum hours allowed for this pay frequency (40 hours) was used instead of the 56.5 hours reported on the paystub."));
    }

    @Test
    @Order(4)
    @Description("IA-2287 Check If Footnote For Missing Ytd Earning Is Visible")
    void checkIfFootnoteForMissingYTDEarningIsVisible() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                iafolderBuilder.getCoBorrowerFullName(),
                iafolderBuilder.getCoBorrowerSSN(),
                iafolderBuilder.getCoBorrowerCollaboratorId(),
                iafolderBuilder.getCoBorrowerCurrentEmployment(),
                "05/01/" + YEAR_TO_DATE,
                "05/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "100.00", "296.00", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "10.50", "1,193.28", ""),
                new PaystubData.PaystubIncomeRow(PTO, "10.00", "3.00", "20.00", "")));

        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("Actual YTD Avg Income was not calculated as there are no YTD base amounts available on the paystub."));
    }

    @Test
    @Order(5)
    @Description("IA-2392 Check If Footnote For Calculating Actual Average Using Gross Ytd Amount Is Correct")
    void checkIfFootnoteForCalculatingActualAverageUsingGrossYTDAmountIsCorrect() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                iafolderBuilder.getCoBorrowerFullName(),
                iafolderBuilder.getCoBorrowerSSN(),
                iafolderBuilder.getCoBorrowerCollaboratorId(),
                iafolderBuilder.getCoBorrowerCurrentEmployment(),
                "06/01/" + YEAR_TO_DATE,
                "06/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                .setYtdGrossIncomeAmount("1265"));

        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("The YTD gross pay, which may contain more than just base income, was used in the calculation " +
                        "since there are no YTD base pay amounts available on the paystub."));
    }

    @Test
    @Order(6)
    @Description("IA-2670 Check a footnote for income broken down across job for Paystub")
    void checkIfFootnotesForIncomeBrokenDownAcrossJobAreVisibleForPaystub() {
        dataUpload.removeDocumentsFromFolder();

        iafolderBuilder.setBorrowerPreviousEmployment(iafolderBuilder.getBorrowerCurrentEmployment())
                .setBorrowerPreviousJobEndDate("08/01/" + ONE_YEAR_PRIOR)
                .setBorrowerMonthsOnThisJob("1")
                .setBorrowerYearsOnThisJob("0")
                .setSignDate("01/10/" + YEAR_TO_DATE);
        iafolderBuilder.generateLoanDocument().uploadNewLoanDocument();

        refreshFolder();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/16/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .importDocumentList();

        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employmentSameName(iafolderBuilder.getBorrowerCurrentEmployment(), true).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("Income has been calculated based on time worked for the year"));

        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employmentSameName(iafolderBuilder.getBorrowerPreviousEmployment(), false).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("Income has been calculated based on time worked for the year"));
    }

    @Test
    @Order(7)
    @Description("IA-2670 Check a footnote for income broken down across job for W2")
    void checkIfFootnotesForIncomeBrokenDownAcrossJobAreVisibleForW2() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomW2(
                iafolderBuilder.getBorrowerFullName(),
                iafolderBuilder.getBorrowerSSN(),
                iafolderBuilder.getBorrowerCollaboratorId(),
                iafolderBuilder.getBorrowerCurrentEmployment(),
                "12000.00",
                ONE_YEAR_PRIOR.toString()));

        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("Income has been calculated based on time worked for the year"));

        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("Income has been calculated based on time worked for the year"));
    }

    @Test
    @Order(8)
    @Description("IA-2670 Check a footnote for income broken down across job for Paystub an W2")
    void checkIfFootnotesForIncomeBrokenDownAcrossJobAreVisibleForW2AndPaystub() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/16/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .importDocumentList();

        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employmentSameName(iafolderBuilder.getBorrowerCurrentEmployment(), true).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("Income has been calculated based on time worked for the year"));

        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employmentSameName(iafolderBuilder.getBorrowerPreviousEmployment(), false).BODY.FOOTNOTE.get(0).shouldHave(
                Condition.text("Income has been calculated based on time worked for the year"));
    }
}

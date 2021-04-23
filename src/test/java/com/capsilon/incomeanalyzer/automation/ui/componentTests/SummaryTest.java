package com.capsilon.incomeanalyzer.automation.ui.componentTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.Summary;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.summary.Income;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.totalmonthlyincome.ApplicantProgressBar;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import com.capsilon.test.ui.components.Conditions;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.restoreApplicantDefaults;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Summary specification")
@Execution(CONCURRENT)
@ResourceLock(value = "SummaryTest")
class SummaryTest extends TestBaseUI {
    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIBarTest");
    private long borrowerId;
    private long coBorrowerId;

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.generateLoanDocument().uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", "6000")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "500", "6000")))
                .importDocumentList();
        refreshFolder();
        RestGetResponse rsp = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());
        borrowerId = rsp.getApplicant(iafolderBuilder.getBorrowerFullName()).getId();
        coBorrowerId = rsp.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getId();
    }

    @BeforeEach
    void restoreDefaults() {
        restoreApplicantDefaults(borrowerId);
        restoreApplicantDefaults(coBorrowerId);
    }

    @Test
    @Description("Test shows if Both Tutti Frutti Bar are set to 100% when income for both Borrowers is the same")
    void checkIfTuttiFruttiBarsAreEqualIfBothIncomesAreTheSame() {
        IncomeAnalyzerPage.summaryView.summary.goToSummaryTab();
        IncomeAnalyzerPage.summaryView.checkAllApplicantsProgressBarWidthIsHundred();
    }

    @Test
    @Description("Test checks if Tutti Frutti correct bar is set to 50% of it's length when difference between incomes is exactly a half")
    void checkIfTuttiFruttiBarIsAtHalfForDoubleOfIncomeDifference() {
        Summary summary = IncomeAnalyzerPage.summaryView.summary;
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY
                .employmentYear(YEAR_TO_DATE.toString())
                .document(SummaryDocumentType.PAYSTUB).RADIO_BUTTON_FIELDS.get(1)
                .click();
        summary.goToSummaryTab();
        ApplicantProgressBar applicantProgressBar = summary.totalMonthlyIncomeContainer.getApplicantProgressBar(1);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_ONE_MINUTE, () ->
                applicantProgressBar.TUTTI_FRUTTI.BAR.shouldHave(Condition.attribute("style", "width: 50%;")));
        summary.totalMonthlyIncomeContainer.getApplicantProgressBar(0)
                .TUTTI_FRUTTI.BAR.shouldHave(Conditions.attributeContains("style", "100%"));
    }

    @Test
    @Description("Checks if Tutti Frutti bar is exactly at 0% when one of the borrowers has no income")
    void checkIfTuttiFruttiBarIsEmptyForOneHundredPercentOfIncomeDifference() {
        Summary summary = IncomeAnalyzerPage.summaryView.summary;
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(false);
        summary.goToSummaryTab();
        ApplicantProgressBar applicantProgressBar = summary.totalMonthlyIncomeContainer.getApplicantProgressBar(0);
        applicantProgressBar.TUTTI_FRUTTI.EMPTY_BAR.should(Condition.exist);
    }

    @Test
    @Description("Checks if both Tutti Frutti bars have 0% for no income")
    void checkIfBothTuttiFruttiBarsAreEmptyForNoIncome() {
        Summary summary = IncomeAnalyzerPage.summaryView.summary;
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(false);
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(false);
        summary.goToSummaryTab();
        ApplicantProgressBar applicantProgressBar = summary.totalMonthlyIncomeContainer.getApplicantProgressBar(0);
        ApplicantProgressBar coApplicantProgressBar = summary.totalMonthlyIncomeContainer.getApplicantProgressBar(1);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            applicantProgressBar.TUTTI_FRUTTI.EMPTY_BAR.shouldBe(Condition.exist);
            coApplicantProgressBar.TUTTI_FRUTTI.EMPTY_BAR.shouldBe(Condition.exist);
        });
    }

    @Test
    void allPartsOfProgressBarsShouldSumUpToHundredWidthOfEntireBar() {
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_ONE_MINUTE, IncomeAnalyzerPage.summaryView::checkAllApplicantsProgressBarPartsSumIsEqualToHundred);
    }

    @Test
    void atLeastOneProgressBarShouldHaveMaxWidthOrAllShouldHaveZeroWidth() {
        IncomeAnalyzerPage.summaryView.checkAllApplicantsProgressBarWidthIsHundred();
    }

    @Test
    void eachApplicantIncomeValueShouldBeEqualToSumOfAllIncomesVisibleOnTooltips() {
        IncomeAnalyzerPage.summaryView.checkAllApplicantsProgressBarIncomeValueEqualsToSumOfTooltipsIncomes();
    }

    @Test
    void sumOfAllApplicantsIncomesShouldBeEqualToCalculatedTotalAdjustedMonthlyIncome() {
        IncomeAnalyzerPage.summaryView.checkSumOfAllApplicantsIsEqualToSumCalculatedTotalIncomeOfSelectedApplicants();
    }

    @Test
    void applicantsOnAccordionShouldBeEqualAndInTheSameOrderAsNamesOnSideNav() {
        IncomeAnalyzerPage.checkIfSummaryAccordionNamesAreEqualToSideNav();
    }

    @Test
    void totalAdjustedMonthlyIncomeOnSummaryComponentShouldBeEqualToTotalIncomeOnSideNav() {
        IncomeAnalyzerPage.checkIfSideNavTotalAdjustedMonthlyIncomeIsEqualToSummary();
    }

    @Test
    void allIncomePartsNamesShouldBeEqualToAlreadyEstablishedParts() {
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_ONE_MINUTE,
                IncomeAnalyzerPage.summaryView.summary::checkApplicantsIncomePartsNames);

    }

    @Test
    void incomePartsOnAccordionShouldSumUpToJobIncome() {
        Summary summary = IncomeAnalyzerPage.summaryView.summary;
        summary.goToSummaryTab();
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
                    for (int i = 0; i < IncomeAnalyzerPage.getNumberOfApplicants(); i++) {
                        Income incomes = summary.getApplicantAccordion(i).getIncomeTypes();
                        for (int j = 0; j < incomes.getIncomeTypesNumber(); j++) {
                            assertEquals(incomes.getIncomeById(j).getPanel().getNumericalValueOfIncome(),
                                    incomes.getIncomeById(j).getSumOfIncomeParts(), 1D);
                        }
                    }
                }
        );

    }
}

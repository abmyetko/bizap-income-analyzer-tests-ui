package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.Applicant;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.SideNav;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.commons.selenide.ignoreon.IgnoreOn;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TWO_YEARS_PRIOR;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.test.ignoreon.Browser.IE;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("PrimaryJobSelectionsFromLoanDocTest")
@IgnoreOn(IE)
@Execution(CONCURRENT)
@ResourceLock(value = "PrimaryJobSelectionsFromLoanDocTest")
class PrimaryJobSelectionsFromLoanDocTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIPriSel");

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
        refreshFolder();
    }

    @Test
    @Description("IA-2843 IA-2672 Check if W2 and applicant are selected if Base Pay is selected")
    void uploadLoanDocWithBasePayAndCheckIfW2AndApplicantIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();
        Applicant applicant = applicantView.applicant(iafolderBuilder.getBorrowerFullName());
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

        Retry.tryRun(CommonMethods.TIMEOUT_FIFTEEN_SECONDS, () -> {
            //Applicant checkbox
            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsEnabled();

            //W2 checkbox
            applicant.incomeCategory(IncomePartCategory.W2).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
            applicant.incomeCategory(IncomePartCategory.W2).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        });

        //Income types checkboxes
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
    }

    @Test
    @Description("IA-2843 IA-2672 Check if W2 and applicant are unselected if all income type are unselected")
    void uploadLoanDocWithoutAnyIncomeTypesAndCheckIfW2IsUnselected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.0");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();
        Applicant applicant = applicantView.applicant(iafolderBuilder.getBorrowerFullName());
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

        Retry.tryRun(CommonMethods.TIMEOUT_FIFTEEN_SECONDS, () -> {
            //Applicant checkbox
            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsUnselected();
            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsEnabled();

            //W2 checkbox
            applicant.incomeCategory(IncomePartCategory.W2).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
            applicant.incomeCategory(IncomePartCategory.W2).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        });

        //Income types checkboxes
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.OVERTIME).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        applicant.incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
    }
}

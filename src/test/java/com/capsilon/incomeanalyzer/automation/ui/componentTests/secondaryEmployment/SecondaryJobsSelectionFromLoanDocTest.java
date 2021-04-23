package com.capsilon.incomeanalyzer.automation.ui.componentTests.secondaryEmployment;

import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.Applicant;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.SideNav;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.LoanDocumentTypeOfIncome;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME;
import static com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory.W2;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryJobsSelectionFromLoanDocTest")
public class SecondaryJobsSelectionFromLoanDocTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUISecSel");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.generateLoanDocument().uiBuild());
        refreshFolder();
    }

    @Test
    @Description("IA-2577 Autoselect secondary base income if secondary job is selected ")
    void checkIfSecondaryJobBasePayIsSelectedIfSecondaryJobIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerSecondJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "10.00");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();
        Applicant borrower = applicantView.applicant(iafolderBuilder.getBorrowerFullName());

        // Base Pay secondary job and secondary job checkboxes for Borrower
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        Applicant coBorrower = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName());
        // Base Pay secondary job and secondary job checkboxes for CoBorrower
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
    }

    @Test
    @Description("IA-2843 Autoselect W2 if primary job is Selected ")
    void checkIfW2IsSelectedIfPrimaryJobIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerSecondJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();
        Applicant borrower = applicantView.applicant(iafolderBuilder.getBorrowerFullName());

        // Primary job and secondary job checkboxes for Borrower
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        //W2
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        Applicant coBorrower = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName());

        // Primary job and secondary job checkboxes for CoBorrower
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        //W2
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
    }

    @Test
    @Description("IA-2843 Autoselect W2 if secondary job is selected ")
    void checkIfW2IsSelectedIfSecondaryJobIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.10")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerSecondJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.10");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();
        Applicant borrower = applicantView.applicant(iafolderBuilder.getBorrowerFullName());

        //Primary job and secondary job checkboxes for Borrower
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        //W2
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        Applicant coBorrower = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName());

        //Primary job and secondary job checkboxes for CoBorrower
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        //W2
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();
    }

    @Test
    @Description("IA-2672 Autodeselect W2 if primary and secondary is deselected ")
    void checkIfW2IsSDeselectedIfSecondaryJobIsDeselected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerSecondJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();
        Applicant borrower = applicantView.applicant(iafolderBuilder.getBorrowerFullName());

        // Primary job and secondary job checkboxes for Borrower
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();

        //W2
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();

        Applicant coBorrower = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName());

        //Primary job and secondary job checkboxes for CoBorrower
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        coBorrower.incomeCategory(W2).multiCurrentJobView().incomeGroup(SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();

        //W2
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
    }

    @Test
    @Description("IA-2672 Autodeselect applicant if W2 is deselected ")
    void checkIfApplicantIsDeselectedIfW2IsDeselected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerSecondJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();

        //W2
        Applicant borrower = applicantView.applicant(iafolderBuilder.getBorrowerFullName());

        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();

        Applicant coBorrower = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName());

        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsUnselected();
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();

        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

        //Applicant checkbox
        sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsUnselected();
        sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsEnabled();
        sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsUnselected();
        sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsEnabled();
    }

    @Test
    @Description("IA-2672 Autoselect applicant if W2 selected ")
    void checkIfApplicantIsSelectedIfW2IsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.10")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerSecondJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.10");
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        ApplicantView applicantView = new ApplicantView();

        //W2
        Applicant borrower = applicantView.applicant(iafolderBuilder.getBorrowerFullName());

        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        borrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        Applicant coBorrower = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName());
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
        coBorrower.incomeCategory(W2).HEADER.CHECKBOX.checkIfCheckboxIsEnabled();

        //Applicant checkbox
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

        sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
        sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsEnabled();
        sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
        sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsEnabled();
    }
}
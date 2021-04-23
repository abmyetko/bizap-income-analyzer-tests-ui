package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment;

import com.capsilon.automation.bam.ui.container.ContainerPage;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.CduStatus;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.STATUS_FORBIDDEN;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Check if disabled folders work fine")
@Execution(CONCURRENT)
@ResourceLock(value = "DisableTest")
class DisableTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIDis");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iafolderBuilder
                .setSignDate("02/28/" + YEAR_TO_DATE)

                .setBorrowerFirstName("John")
                .setBorrowerLastName("Homeowner")
                .setBorrowerCurrentEmployment("Awesome Computers Inc")
                .setBorrowerYearsOnThisJob("2")
                .setBorrowerPreviousEmployment("Previous Computer Inc")
                .setBorrowerPreviousJobStartDate("01/01/" + FOUR_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("06/30/" + TWO_YEARS_PRIOR)

                .setCoBorrowerFirstName("Mary")
                .setCoBorrowerLastName("Homeowner")
                .setCoBorrowerCurrentEmployment("Blue Younder Airlines Inc")
                .setCoBorrowerYearsOnThisJob("2")
                .setCoBorrowerPreviousEmployment("Previous Blue Inc")
                .setCoBorrowerPreviousJobStartDate("01/01/" + FOUR_YEARS_PRIOR)
                .setCoBorrowerPreviousJobEndDate("06/30/" + TWO_YEARS_PRIOR)
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);

        refreshFolder();
    }

    @Test
    @Order(1)
    void setStatusToDisabledAndCheckIfSideNavIsBlocked() {
        RestNotIARequests.setCduStatus(ContainerPage.utils.getFolderIdFromUrl(), CduStatus.REMOVED, "Data validation not complete");
        IncomeAnalyzerPage.sideNavView.sideNav.checkIFAiwButtonisDisabled();
        IncomeAnalyzerPage.sideNavView.sideNav.checkAllApplicantsCheckboxesDisabled();
    }

    @Test
    @Order(2)
    void checkIfBorrowerIsDisabled() {
        ApplicantView applicantView = new ApplicantView();
        //Disabled checkbox on W2 header
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        //Disabled checkbox on Base Pay header
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.checkIfCheckboxIsDisabled();
        //Disabled checkboxes on Current Job past years
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsDisabled();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsDisabled();
    }

    @Test
    @Order(3)
    void uploadDocumentShouldBeForbiddenForLockedFolder() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "15000,3"))
                                .setPayDate("06/30/" + YEAR_TO_DATE),
                        STATUS_FORBIDDEN); //awaiting fix IA-2502
//                        500);
    }

}

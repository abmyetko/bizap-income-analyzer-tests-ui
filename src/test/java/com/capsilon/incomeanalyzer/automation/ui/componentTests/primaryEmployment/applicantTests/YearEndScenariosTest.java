package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentBody;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Year end scenarios test")
@Execution(CONCURRENT)
@ResourceLock(value = "YearEndScenariosTest")
class YearEndScenariosTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIYrEnd");

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
                .setCoBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setCoBorrowerPreviousJobEndDate("06/30/" + ONE_YEAR_PRIOR)
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);

        refreshFolder();
    }

    @Test
    @Order(1)
    void checkIfYTDAndPreviousYearsHaveCorrectValuesAndYTDIsLAYear() {
        ApplicantView applicantView = new ApplicantView();

        EmploymentBody year = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY;

        year.employmentYear(YEAR_TO_DATE.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(ONE_YEAR_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(TWO_YEARS_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
    }

    @Test
    @Order(2)
    void uploadPaystubForYearBeforePriorYearAndCheckIfYTDIsLAYear() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("12/31/" + TWO_YEARS_PRIOR));

        ApplicantView applicantView = new ApplicantView();
        EmploymentBody year = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY;

        year.employmentYear(YEAR_TO_DATE.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(ONE_YEAR_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(TWO_YEARS_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
    }

    @Test
    @Order(3)
    void uploadPaystubForPriorYearAndCheckEmploymentYearsAndYTDIsPriorYear() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("12/31/" + ONE_YEAR_PRIOR));

        ApplicantView applicantView = new ApplicantView();
        EmploymentBody year = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY;

        year.employmentYear(ONE_YEAR_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(TWO_YEARS_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(THREE_YEARS_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
    }

    @Test
    @Order(4)
    void uploadPaystubForYTDAndCheckEmploymentYears() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("01/15/" + YEAR_TO_DATE));

        ApplicantView applicantView = new ApplicantView();
        EmploymentBody year = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY;

        year.employmentYear(YEAR_TO_DATE.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(ONE_YEAR_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
        year.employmentYear(TWO_YEARS_PRIOR.toString()).HEADER.shouldBe(Condition.visible);
    }
}

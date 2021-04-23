package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeType;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.reportportal.Description;
import com.codeborne.selenide.ElementsCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TWO_YEARS_PRIOR;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("UI Evidence Test")
@Execution(CONCURRENT)
@ResourceLock(value = "IAUIEvidenceTest")
class EvidenceTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIEvidenc");

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

        refreshFolder();
    }

    @Test
    @Description("IA-2766 Check if Months paid and Months worked in Base Pay have class indented")
    void checkIfMonthPaidAndMonthsWorkedInBasePayHaveClassIndented() {
        ApplicantView applicantView = new ApplicantView();
        ElementsCollection labels = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.LABELS;

        assertAll(
                () -> assertEquals("Months Paid Per Year (seasonal/teachers)", labels.get(4).getText(),
                        "[Base Pay] Months Paid Per Year (seasonal/teachers) should be 5 on the list"),
                () -> assertTrue(labels.get(4).getAttribute("class").contains("indent"), "[Base Pay] Months Paid Per Year (seasonal/teachers) should have class indent"),
                () -> assertEquals("Months Worked", labels.get(6).getText(), "[Base Pay] Months Worked should be 7 on the list"),
                () -> assertTrue(labels.get(6).getAttribute("class").contains("indent"), "[Base Pay] Months Worked should have class indent")
        );
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Description("IA-2766 Check Months worked in Overtime have class indented")
    void checkIfMonthsWorkedInOvertimeHaveClassIndented() {
        ApplicantView applicantView = new ApplicantView();
        IncomeCategoryIncomeType primaryJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView();

        assertAll(
                () -> assertEquals("Months Worked", primaryJob.incomeType(IncomePartType.OVERTIME).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.LABELS.get(2).getText(),
                        "[Overtime] Months Worked should be 3 on the list"),
                () -> assertTrue(primaryJob.incomeType(IncomePartType.OVERTIME).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.LABELS.get(2).getAttribute("class")
                        .contains("indent"), "[Overtime] Months Worked should have class indent")
        );
    }

    @Test
    @EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
    @Description("IA-2766 Check Months worked in Commissions have class indented")
    void checkIfMonthsWorkedInCommissionssHaveClassIndented() {
        ApplicantView applicantView = new ApplicantView();
        IncomeCategoryIncomeType primaryJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView();

        assertAll(
                () -> assertEquals("Months Worked", primaryJob.incomeType(IncomePartType.COMMISSIONS).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.LABELS.get(2).getText(),
                        "[Commissions] Months Worked should be 3 on the list"),
                () -> assertTrue(primaryJob.incomeType(IncomePartType.COMMISSIONS).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.LABELS.get(2).getAttribute("class")
                        .contains("indent"), "[Commissions] Months Worked should have class indent")
        );
    }

    @Test
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @Description("IA-2766 Check Months worked in Bonus have class indented")
    void checkIfMonthsWorkedInBonusHaveClassIndented() {
        ApplicantView applicantView = new ApplicantView();
        IncomeCategoryIncomeType primaryJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView();

        assertAll(
                () -> assertEquals("Months Worked", primaryJob.incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.LABELS.get(3).getText(),
                        "[Bonus] Months Worked should be 4 on the list"),
                () -> assertTrue(primaryJob.incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY.LABELS.get(3).getAttribute("class")
                        .contains("indent"), "[Bonus] Months Worked should have class indent")
        );
    }
}
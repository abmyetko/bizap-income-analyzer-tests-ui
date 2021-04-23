package com.capsilon.incomeanalyzer.automation.ui.componentTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.SideNav;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.test.commons.selenide.ignoreon.IgnoreOn;
import com.capsilon.test.ignoreon.Browser;
import com.capsilon.test.ui.Retry;
import com.capsilon.test.ui.Wait;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.openqa.selenium.TimeoutException;

import java.time.Duration;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.HOURLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.MONTHLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$x;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("SideNav specification")
@Execution(CONCURRENT)
@ResourceLock(value = "SideNavTest")
class SideNavTest extends TestBaseUI {

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        IAFolderBuilder iafolderBuilder = createFolderBuilder("IASideNav");
        iafolderBuilder.setBorrowerFirstName("Erica")
                .setBorrowerLastName("AutomationOne")
                .setCoBorrowerFirstName("Mary")
                .setCoBorrowerLastName("AutomationTwo")
                .setBorrowerCurrentEmployment("CurrentJobNameOne")
                .setCoBorrowerCurrentEmployment("CurrentJobNameTwo")
                .setBorrowerYearsOnThisJob("10")
                .setBorrowerMonthsOnThisJob("10")
                .setCoBorrowerYearsOnThisJob("15")
                .setCoBorrowerMonthsOnThisJob("5")
                .setBorrowerPreviousEmployment("PreviousJobNameOne")
                .setCoBorrowerPreviousEmployment("PreviousJobNameTwo")
                .setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setCoBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("01/31/" + YEAR_TO_DATE)
                .setCoBorrowerPreviousJobEndDate("01/31/" + YEAR_TO_DATE)
                .setSignDate("02/28/" + YEAR_TO_DATE)
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2000", "2000"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1500", "1500")))
                .addDocument(dataUpload.createCustomW2(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "24000",
                        ONE_YEAR_PRIOR.toString()))
                .addDocument(dataUpload.createCustomVoePrevious(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerPreviousEmployment(),
                        MONTHLY,
                        "07/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "2500",
                        "0",
                        "0",
                        "0"))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        null,
                        "01/07/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "15.00", "40.00", "600.00", "600.00"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1500", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerPreviousEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2595", "173.33", "2595", "2595"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1500", "1500")))
                .addDocument((dataUpload.createCustomVoeCurrent(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        "Abacus",
                        HOURLY,
                        "11/01/2010",
                        "03/31/" + YEAR_TO_DATE,
                        "15.00",
                        "3000",
                        "03/31/" + YEAR_TO_DATE))
                        .setAvgHoursPerWeek("40.00"))
                .importDocumentList();

        refreshFolder();
    }

    @DisabledIfSystemProperty(named = "bizapps.LoanDocumentType", matches = "mismo")
    @Test
    void checkIfFirstApplicantHavePrimaryTag() {
        IncomeAnalyzerPage.sideNavView.sideNav.getApplicant(0).tag.shouldHave(exactText("primary"));
    }

    @Test
    void checkIfGenerateWorksheetButtonWorks() {
        IncomeAnalyzerPage.sideNavView.sideNav.GENERATE_INCOME_WORKSHEET_BUTTON_LABEL.shouldHave(Condition.exactText("Generate Now"), Duration.ofMillis(TIMEOUT_ONE_MINUTE));
        IncomeAnalyzerPage.sideNavView.sideNav.GENERATE_INCOME_WORKSHEET_BUTTON.click();
        IncomeAnalyzerPage.sideNavView.sideNav.GENERATE_INCOME_WORKSHEET_BUTTON_LABEL.shouldHave(Condition.exactText("Generating..."), Duration.ofMillis(TIMEOUT_ONE_MINUTE));
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            refreshFolder();
            IncomeAnalyzerPage.sideNavView.sideNav.GENERATE_INCOME_WORKSHEET_BUTTON_LABEL.shouldHave(Condition.exactText("Generate Now"), Duration.ofMillis(TIMEOUT_ONE_MINUTE));
        });
    }

    @IgnoreOn(Browser.EDGE)
    @Test
    void totalMonthlyIncomeShouldChangeOnApplicantSelectionAndUnselection() {
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;
        sideNav.checkAllApplicantsCheckboxes();
        double[] incomes = IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer.getApplicantIncomeLists();
        double totalIncome = sideNav.getDoubleValueOfTotalAdjustedMonthlyIncome();
        assertEquals(totalIncome, CommonMethods.getSumOfPositiveNumbers(incomes), 0.1D);
        for (int i = 0; i < sideNav.getApplicantNumber(); i++) {
            if (incomes[i] != 0) {
                Wait.waitForElementVisibilityWithoutThrowingException($x("//ia-income"));
                sideNav.getApplicant(i).unCheckApplicantCheckbox();
                incomes = IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer.getApplicantIncomeLists();
                sideNav.waitForTotalIncomeChange(totalIncome);
                totalIncome = sideNav.getDoubleValueOfTotalAdjustedMonthlyIncome();
                assertEquals(totalIncome, 0.0D, 0.1D);

                sideNav.getApplicant(i).checkApplicantCheckbox();
                sideNav.waitForTotalIncomeChange(totalIncome);
                try {
                    Retry.whileTrue(TIMEOUT_FIVE_SECONDS, () -> {
                        double finalTotalIncome = sideNav.getDoubleValueOfTotalAdjustedMonthlyIncome();
                        double[] finalIncomes = IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer.getApplicantIncomeLists();
                        return finalTotalIncome != CommonMethods.getSumOfPositiveNumbers(finalIncomes);
                    }, "Income not changed");
                    //@formatter:off
                } catch (TimeoutException ignore) {
                } //NOSONAR
                //@formatter:on
                incomes = IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer.getApplicantIncomeLists();
                totalIncome = sideNav.getDoubleValueOfTotalAdjustedMonthlyIncome();
            }
            final double[] finalTotalIncome = new double[1];
            final double[][] finalIncomes = new double[1][1];
            Retry.whileTrue(TIMEOUT_FIVE_SECONDS, () -> {
                        finalTotalIncome[0] = sideNav.getDoubleValueOfTotalAdjustedMonthlyIncome();
                        finalIncomes[0] = IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer.getApplicantIncomeLists();
                        return finalTotalIncome[0] != CommonMethods.getSumOfPositiveNumbers(finalIncomes[0]);
                    },
                    String.format("Incomes did not became equal in time, expected: %s actual %s", finalTotalIncome[0], CommonMethods.getSumOfPositiveNumbers(finalIncomes[0])));
        }
    }
}
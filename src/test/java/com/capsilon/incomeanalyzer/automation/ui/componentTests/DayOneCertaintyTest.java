package com.capsilon.incomeanalyzer.automation.ui.componentTests;

import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.SideNav;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAMismoBuilder;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_FIVE_SECONDS;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_TWENTY_SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

//TODO
@Disabled("TODO - to unlock after D1C release")
@Execution(CONCURRENT)
@ResourceLock(value = "DayOneCertaintyTestUI")
public class DayOneCertaintyTest extends TestBaseUI {

    private final IAMismoBuilder iafolderBuilder = new IAMismoBuilder("IAUID1C");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.generateLoanDocument().uiBuild());
        refreshFolder();
    }

    @Order(1)
    @Test
    @Description("IA-2960 IA-2962 Check if D1C tag is displayed only for Borrower on UI")
    void checkD1CIsDisplayedOnlyForBorrower() {
        iafolderBuilder.generateLoanDocument()
                .setBorrowerIncomeVerified(true)
                .setCoBorrowerIncomeVerified(false)
                .uploadNewLoanDocument();
        refreshFolder();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

            assertAll("D1C assertions",
                    () -> assertTrue(sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag should be displayed for Borrower"),
                    () -> assertEquals("D1C/AIM", sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).tag.getText(),
                            "text on D1C tag for Borrower should be equal D1C/AIM"),
                    () -> assertFalse(sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag shouldn't be displayed for Borrower"));

            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsUnselected();
            sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
        });
    }

    @Order(2)
    @Test
    @Description("IA-2960 IA-2962 Check if D1C tag isn't displayed for Borrowers on UI")
    void checkIfD1CIsntDisplayedForBorrowerAndCoBorrowerForFalseValue() {
        iafolderBuilder.generateLoanDocument()
                .setBorrowerIncomeVerified(false)
                .setCoBorrowerIncomeVerified(false)
                .uploadNewLoanDocument();
        refreshFolder();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

            assertAll("D1C assertions",
                    () -> assertFalse(sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag shouldn't be displayed for Borrower"),
                    () -> assertFalse(sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag shouldn't be displayed for CoBorrower"));

            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
            sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
        });
    }

    @Order(3)
    @Test
    @Description("IA-2960 IA-2962 Check if D1C tag isn't displayed for Borrowers on UI")
    void checkIfD1CIsntDisplayedForBorrowerAndCoBorrowerForNullValue() {
        iafolderBuilder.uploadNewLoanDocument();
        refreshFolder();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

            assertAll("D1C assertions",
                    () -> assertFalse(sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag shouldn't be displayed for Borrower"),
                    () -> assertFalse(sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag shouldn't be displayed for CoBorrower"));

            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
            sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsSelected();
        });
    }

    @Order(4)
    @Test
    @Description("IA-2960 IA-2962 Check if D1C tag is displayed for Borrowers on UI")
    void checkD1CIsDisplayedForBorrowerAndCoBorrower() {
        iafolderBuilder.generateLoanDocument()
                .setBorrowerIncomeVerified(true)
                .setCoBorrowerIncomeVerified(true)
                .uploadNewLoanDocument();
        refreshFolder();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

            assertAll("D1C assertions",
                    () -> assertTrue(sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag should be displayed for Borrower"),
                    () -> assertEquals("D1C/AIM", sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).tag.getText(),
                            "text on D1C tag for Borrower should be equal D1C/AIM"),
                    () -> assertTrue(sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).tag.isDisplayed(),
                            "D1C tag should be displayed for CoBorrower"),
                    () -> assertEquals("D1C/AIM", sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).tag.getText(),
                            "text on D1C tag for CoBorrower should be equal D1C/AIM"));

            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsUnselected();
            sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsUnselected();
            sideNav.getApplicant(iafolderBuilder.getBorrowerFullName()).checkBox.checkIfCheckboxIsDisabled();
            sideNav.getApplicant(iafolderBuilder.getCoBorrowerFullName()).checkBox.checkIfCheckboxIsDisabled();
            sideNav.checkIFAiwButtonisDisabled();
        });
    }
}
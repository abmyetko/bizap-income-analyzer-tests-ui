package com.capsilon.incomeanalyzer.automation.ui;

import com.capsilon.incomeanalyzer.automation.ui.view.*;
import com.capsilon.test.run.confiuration.BizappsConfig;
import com.capsilon.test.ui.Retry;
import com.capsilon.test.ui.Wait;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_TEN_SECONDS;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_TWO_SECONDS;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public final class IncomeAnalyzerPage {

    public static final SideNavView sideNavView = new SideNavView();
    public static final SummaryView summaryView = new SummaryView();
    public static final ApplicantView applicantView = new ApplicantView();
    public static final EmptyIAView emptyIAView = new EmptyIAView();
    public static final RestoreDefaultsView restoreDefaultsView = new RestoreDefaultsView();

    public static final SelenideElement generateWorksheetButton = $x("//button[@mattooltip='Generate Income Worksheet']");
    public static final SelenideElement generateWorksheetDownloadIcon = generateWorksheetButton.$x(".//svg-icon");
    public static final SelenideElement generateWorksheetSpinnerIcon = generateWorksheetButton.$x(".//mat-spinner");

    private IncomeAnalyzerPage() {
    }

    public static void shouldBeDisplayed() {
        sideNavView.sideNav.shouldBeDisplayed();
        summaryView.summary.shouldBeDisplayed();
    }

    public static void checkIfSummaryAccordionNamesAreEqualToSideNav() {
        for (int i = 0; i < sideNavView.sideNav.getApplicantNumber(); i++) {
            summaryView.summary.getApplicantAccordion(i).getPanel().NAME
                    .shouldHave(Condition.text(sideNavView.sideNav.getApplicant(i).checkBox.LABEL.text()));
        }
    }

    public static void checkIfSideNavTotalAdjustedMonthlyIncomeIsEqualToSummary() {
        sideNavView.sideNav.TOTAL_MONTHLY_INCOME_VALUE
                .shouldHave(Condition.text(summaryView.summary.totalMonthlyIncomeContainer.TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE.text()));
    }

    public static void waitForWorksheetGeneration() {
        Wait.waitForElementVisibilityWithoutThrowingException(IncomeAnalyzerPage.generateWorksheetSpinnerIcon);
        Wait.waitUntilElementNotVisibleWithoutThrowingException(BizappsConfig.getMaxBizappLoadTime(), IncomeAnalyzerPage.generateWorksheetSpinnerIcon);
        IncomeAnalyzerPage.generateWorksheetDownloadIcon.shouldBe(visible);
    }

    public static int getNumberOfApplicants() {
        return sideNavView.sideNav.getApplicantNumber();
    }

    public static ElementsCollection tabsWithoutSelected(String tabId) {
        return $$x("//mat-tab-body[contains(@aria-labelledby,'mat-tab-label-0-') and not(contains(@aria-labelledby,'" + tabId + "'))]/div/div");
    }

    public static String firstSubstringSeparatedByNewLineOrSubstringText(SelenideElement element, SelenideElement substringElement) {
        return firstSubstringSeparatedByNewLineOrSubstringText(element, substringElement.text());
    }

    public static String firstSubstringSeparatedByNewLineOrSubstringText(SelenideElement element, String substringText) {
        try {
            return element.text().substring(0, element.text().indexOf('\n')).trim();
        } catch (StringIndexOutOfBoundsException outOfBounds) { //NOSONAR
            return element.text().substring(0, element.text().indexOf(substringText)).trim();
        }
    }

    public static void closeOverlayBackdrop() {
        SelenideElement OVERLAY_BACKDROP = Selenide.$x("//div[@class='cdk-overlay-container']/div[contains(@class,'transparent-backdrop')]");
        Retry.tryRun(TIMEOUT_TWO_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
            if (OVERLAY_BACKDROP.exists())
                OVERLAY_BACKDROP.click();
        });
    }

    public static SelenideElement scrollIntoViewCenter(SelenideElement element) {
        element.scrollIntoView("{block: \"center\"}");
        return element;
    }
}

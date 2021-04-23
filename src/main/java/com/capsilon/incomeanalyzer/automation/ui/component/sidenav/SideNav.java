package com.capsilon.incomeanalyzer.automation.ui.component.sidenav;

import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.TimeoutException;

import java.util.Locale;

import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.replaceDollarsAndCommas;
import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

public class SideNav {

    public final SelenideElement COMPONENT_CONTAINER = $x("//mat-sidenav[not(contains(@class,'container'))]");
    public final SelenideElement TOTAL_MONTHLY_INCOME_HEADER = COMPONENT_CONTAINER.$("h4");
    public final SelenideElement TOTAL_MONTHLY_INCOME_VALUE = COMPONENT_CONTAINER.$x(".//div[contains(@class,'qualifying-income')]/span");
    public final SelenideElement INCOME_WORKSHEET_HEADER = COMPONENT_CONTAINER.$("ia-automated-income-worksheet > h4");
    public final SelenideElement GENERATE_INCOME_WORKSHEET_BUTTON = COMPONENT_CONTAINER.$("ia-automated-income-worksheet > div  .generate-btn");
    public final SelenideElement GENERATE_INCOME_WORKSHEET_BUTTON_LABEL = GENERATE_INCOME_WORKSHEET_BUTTON.$("span");

    public SideNav shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        TOTAL_MONTHLY_INCOME_HEADER.shouldBe(visible);
        TOTAL_MONTHLY_INCOME_VALUE.shouldBe(visible);
        return this;
    }

    public int getApplicantNumber() {
        return COMPONENT_CONTAINER.$$x(".//mat-checkbox[contains(@id,'mat-checkbox-')]/parent::li").size();
    }

    public SideNavApplicant getApplicant(int applicantIndex) {
        return new SideNavApplicant(COMPONENT_CONTAINER.$x(".//mat-checkbox[@id='mat-checkbox-" + (applicantIndex + 1) + "']/parent::li"));
    }

    public SideNavApplicant getApplicant(String name) {
        return new SideNavApplicant(COMPONENT_CONTAINER.$x(".//li[contains(@aria-label, '" + name.toLowerCase(Locale.US) + "')]"));
    }

    @SuppressWarnings("squid:S109")
    public void waitForTotalIncomeChange(double currentTotalIncome) {
        if (!Double.isInfinite(2 / currentTotalIncome)) {
            Retry.whileTrue(9000,
                    () -> Double.compare(Math.round(getDoubleValueOfTotalAdjustedMonthlyIncome() * 100), Math.round(currentTotalIncome * 100)) == 0,
                    "No change");
        }
    }

    @SuppressWarnings("squid:S109")
    public void waitForTotalIncomeChange() {
        try {
            final double currentTotalIncome = getDoubleValueOfTotalAdjustedMonthlyIncome();
            Retry.whileTrue(9000,
                    () -> Double.compare(Math.round(getDoubleValueOfTotalAdjustedMonthlyIncome() * 100), Math.round(currentTotalIncome * 100)) == 0,
                    "No change");
            //@formatter:off
        } catch (TimeoutException ignore) {} //NOSONAR
        //@formatter:on
    }

    public double getDoubleValueOfTotalAdjustedMonthlyIncome() {
        try {
            return Double.parseDouble(replaceDollarsAndCommas(TOTAL_MONTHLY_INCOME_VALUE.getText()));
            //@formatter:off
        } catch (NumberFormatException ignore) {} //NOSONAR
        //@formatter:on
        return 0.0D;
    }

    public SideNav checkAllApplicantsCheckboxes() {
        for (int i = 0; i < getApplicantNumber(); i++) {
            getApplicant(i).checkApplicantCheckbox();
        }
        return this;
    }

    public SideNav checkAllApplicantsCheckboxesDisabled() {
        for (int i = 0; i < getApplicantNumber(); i++) {
            getApplicant(i).shouldBeDisabled();
        }
        return this;
    }

    public SideNav checkIFAiwButtonisDisabled() {
        GENERATE_INCOME_WORKSHEET_BUTTON.shouldBe(disabled);
        return this;
    }
}

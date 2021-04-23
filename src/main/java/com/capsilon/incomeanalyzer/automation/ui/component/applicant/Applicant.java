package com.capsilon.incomeanalyzer.automation.ui.component.applicant;

import com.capsilon.incomeanalyzer.automation.ui.component.alert.ApplicantAlerts;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.codeborne.selenide.ClickOptions;
import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class Applicant {

    public final SelenideElement COMPONENT_CONTAINER = $x("//ia-applicant-page");
    public final SelenideElement TAB_BUTTON;
    public final SelenideElement TAB_BUTTON_ALERT_BADGE;

    public Applicant(int applicantIndex) {
        TAB_BUTTON = $$x("//nav/a[contains(@href,'applicant')]").get(applicantIndex);
        TAB_BUTTON_ALERT_BADGE = TAB_BUTTON.$x(".//span[contains(@id,'mat-badge-content')]");
    }

    public Applicant(String applicantName) {
        TAB_BUTTON = $x("//nav/a[contains(@href,'applicant') and translate(@aria-label, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='"
                + applicantName.toLowerCase(Locale.US) + "']");
        TAB_BUTTON_ALERT_BADGE = TAB_BUTTON.$x(".//span[contains(@id,'mat-badge-content')]");
    }

    public Applicant shouldBeDisplayed() {
        TAB_BUTTON.shouldBe(visible);
        return this;
    }

    public Applicant goToSummaryApplicantTab() {
        TAB_BUTTON.scrollIntoView(true).click(ClickOptions.usingJavaScript());
        TAB_BUTTON.shouldHave(attributeContains("class", "active"));
        return this;
    }

    public ApplicantIncomeCategory incomeCategory(IncomePartCategory category) {
        return new ApplicantIncomeCategory(COMPONENT_CONTAINER.$x(String.format(".//section[@data-testid='income-category-%s']", category.toString())));
    }

    public ApplicantAlerts getAlerts() {
        return new ApplicantAlerts(COMPONENT_CONTAINER.$x(".//div[contains(@class,'alerts')]"));
    }
}

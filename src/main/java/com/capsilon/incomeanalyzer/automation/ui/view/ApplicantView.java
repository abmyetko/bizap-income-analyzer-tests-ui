package com.capsilon.incomeanalyzer.automation.ui.view;

import com.capsilon.incomeanalyzer.automation.ui.component.applicant.Applicant;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Selenide.$$x;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

public class ApplicantView {

    public Applicant applicant(int index) {
        Applicant app = new Applicant(index);
        app.goToSummaryApplicantTab();
        return app;
    }

    public Applicant applicant(String name) {
        Applicant app = new Applicant(name);
        app.goToSummaryApplicantTab();
        return app;
    }

    public static void collapseOrExpandTabCollection(ElementsCollection collection, boolean expand) {
        collection.forEach(tab -> collapseOrExpandTab(tab, expand));
    }

    public static void collapseOrExpandTab(SelenideElement tabName, boolean expand) {
        if (tabName.getAttribute(CommonMethods.ARIA_EXPANDED).contains(toStringTrueFalse(!expand)) && tabName.exists()) {
            tabName.scrollIntoView(false).click();
            tabName.shouldNotHave(attributeContains("class", "ng-animating"))
                    .shouldHave(attribute(CommonMethods.ARIA_EXPANDED, toStringTrueFalse(expand)));
        }
    }

    public static Boolean isTabExpanded(WebElement tabName) {
        return Boolean.valueOf(tabName.getAttribute(CommonMethods.ARIA_EXPANDED));
    }

    public static void loseFocusFromAnyElement() {
        Selenide.actions().sendKeys(Keys.ESCAPE);
    }

    public int getNumberOfApplicants() {
        return $$x("//nav/a[contains(@href,'applicant')]").size();
    }
}

package com.capsilon.incomeanalyzer.automation.ui.component.summary.summary;

import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.getDoubleValueOfIncome;
import static com.codeborne.selenide.Condition.visible;

public class Panel {

    public final SelenideElement NAME;
    public final SelenideElement INCOME;
    private final SelenideElement COMPONENT_CONTAINER;

    public Panel(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        NAME = componentContainer.$x(".//*[contains(@class,'applicant') or contains(@class,'type')]");
        INCOME = componentContainer.$x(".//*[contains(@class,'income-amount')]");
    }

    public Panel shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        NAME.shouldBe(visible);
        INCOME.shouldBe(visible);
        return this;
    }

    public void expandAccordion() {
        ApplicantView.collapseOrExpandTab(COMPONENT_CONTAINER, true);
    }

    public void collapseAccordion() {
        ApplicantView.collapseOrExpandTab(COMPONENT_CONTAINER, false);
    }

    public double getNumericalValueOfIncome() {
        return getDoubleValueOfIncome(INCOME.text());
    }
}

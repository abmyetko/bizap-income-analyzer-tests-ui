package com.capsilon.incomeanalyzer.automation.ui.component.summary.summary;

import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.DOUBLE_DELTA_MEDIUM;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicantAccordion {

    private final SelenideElement COMPONENT_CONTAINER;
    private final Panel panel;

    public ApplicantAccordion(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        panel = new Panel(COMPONENT_CONTAINER.$x("./mat-expansion-panel-header"));
    }

    public ApplicantAccordion shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        getPanel().shouldBeDisplayed();
        return this;
    }

    public Income getIncomeTypes() {
        return new Income(COMPONENT_CONTAINER.$x("./div"));
    }

    public Panel getPanel() {
        return panel;
    }

    public void checkIfSumOfIncomeTypesEqualsToApplicantIncome() {
        assertEquals(getIncomeTypes().getApplicantIncomeTypesSum(), panel.getNumericalValueOfIncome(), DOUBLE_DELTA_MEDIUM);
    }
}

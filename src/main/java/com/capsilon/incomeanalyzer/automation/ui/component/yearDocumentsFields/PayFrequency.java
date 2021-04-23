package com.capsilon.incomeanalyzer.automation.ui.component.yearDocumentsFields;

import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.codeborne.selenide.*;

public class PayFrequency {

    private final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement PAY_FREQUENCY_BUTTON;
    public final ElementsCollection DROPDOWN_OPTIONS;
    public final SelenideElement LABEL;

    public PayFrequency(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        PAY_FREQUENCY_BUTTON = COMPONENT_CONTAINER.$x(".//mat-select//div[contains(@class,'mat-select-arrow-wrapper')]");
        LABEL = COMPONENT_CONTAINER.$x(".//div[contains(@class,'value')]");
        DROPDOWN_OPTIONS = Selenide.$$x("//div[contains(@class,'cdk-overlay-pane')]//mat-option[@role='option']");
    }

    public PayFrequency selectFrequency(IncomeFrequency incomeFrequency) {
        DROPDOWN_OPTIONS.find(Condition.exactText(incomeFrequency.text)).click();
        return this;
    }

    public PayFrequency openDropDown() {
        if (!DROPDOWN_OPTIONS.get(0).exists()) {
            PAY_FREQUENCY_BUTTON.click(ClickOptions.usingJavaScript());
        }
        return this;
    }

    public PayFrequency checkLabelValue(String expectedLabel) {
        LABEL.shouldBe(Condition.exactText(expectedLabel));
        return this;
    }
}

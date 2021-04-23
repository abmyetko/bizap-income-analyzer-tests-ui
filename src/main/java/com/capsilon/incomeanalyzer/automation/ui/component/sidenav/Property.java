package com.capsilon.incomeanalyzer.automation.ui.component.sidenav;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;

public class Property {

    public final SelenideElement PROPERTY_NAME;
    public final SelenideElement PROPERTY_VALUE;

    public Property(SelenideElement componentContainer) {
        PROPERTY_NAME = componentContainer;
        PROPERTY_VALUE = componentContainer.$x(".//span[contains(@class,'property__value')]");
    }

    public Property shouldBeDisplayed() {
        PROPERTY_NAME.shouldBe(visible);
        PROPERTY_VALUE.shouldBe(visible);
        return this;
    }

    public String firstSubstringOrWhole() {
        try {
            return PROPERTY_NAME.text().substring(0, PROPERTY_NAME.text().indexOf('\n')).trim();
        } catch (StringIndexOutOfBoundsException outOfBounds) { //NOSONAR
            return PROPERTY_NAME.text().substring(0, PROPERTY_NAME.text().indexOf(PROPERTY_VALUE.text())).trim();
        }
    }

    public String getPropertyValueText() {
        return PROPERTY_VALUE.getText();
    }
}

package com.capsilon.incomeanalyzer.automation.ui.component.emptyia;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$x;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EmptyIA {

    public final SelenideElement COMPONENT_CONTAINER = $x("//div[@class='cdk-overlay-container']");
    public final SelenideElement OPT_OUT_WARNING = COMPONENT_CONTAINER.$x(".//div[contains(@class,'cdk-overlay-pane')]");


    public Boolean isWarningVisible() {
        return OPT_OUT_WARNING.getAttribute("Class").contains("empty-app");
    }

    public void checkIfWarningIsNotVisible() {
        assertFalse(isWarningVisible());
    }

}

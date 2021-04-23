package com.capsilon.incomeanalyzer.automation.ui.component.summary.summary;

import com.capsilon.incomeanalyzer.automation.utilities.StringUtilities;
import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Type {

    private final SelenideElement COMPONENT_CONTAINER;
    private final Parts parts;
    private final Panel panel;

    public Type(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        panel = new Panel(COMPONENT_CONTAINER.$x("./mat-expansion-panel-header"));
        parts = new Parts(COMPONENT_CONTAINER.$x("./div"));
    }

    private boolean isPartTypeCorrect(String typeName) {
        switch (typeName.toLowerCase(Locale.US)) {
            case "base pay":
            case "overtime":
            case "commission":
            case "bonus":
            case "tips":
                return true;
            default:
                return false;
        }
    }

    public Type shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        getParts().shouldBeDisplayed();
        getPanel().shouldBeDisplayed();
        return this;
    }

    public Parts getParts() {
        return parts;
    }

    public Panel getPanel() {
        return panel;
    }

    public double getSumOfIncomeParts() {
        return parts.getIncomeParts().values().stream().mapToDouble(StringUtilities::getDoubleValueOfIncome).sum();
    }

    public void checkIncomeParts() {
        parts.getIncomeParts().keySet().forEach(key -> assertTrue(isPartTypeCorrect(key)));
    }
}

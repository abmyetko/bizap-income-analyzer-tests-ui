package com.capsilon.incomeanalyzer.automation.ui.component.sidenav;

import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView.loseFocusFromAnyElement;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.codeborne.selenide.Condition.*;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

public class CheckBox {

    public final SelenideElement BOX;
    public final SelenideElement LABEL;

    public CheckBox(SelenideElement componentContainer) {
        BOX = componentContainer.$("input");
        LABEL = componentContainer.$x(".//span");
    }

    public CheckBox(SelenideElement componentContainer, SelenideElement label) {
        BOX = componentContainer.$("input");
        LABEL = label;
    }

    public CheckBox shouldBeDisplayed() {
        BOX.shouldBe(visible);
        LABEL.shouldBe(visible);
        return this;
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(BOX.getAttribute(ARIA_CHECKED));
    }

    public void setCheckboxValue(boolean expectedState) {
        Retry.tryRun(TIMEOUT_TWO_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            BOX.shouldBe(enabled);
            loseFocusFromAnyElement();
            if (BOX.getAttribute(ARIA_CHECKED).contains(toStringTrueFalse(!expectedState))) {
                BOX.$x("./parent::span").click();
                BOX.shouldHave(attribute(ARIA_CHECKED, toStringTrueFalse(expectedState)));
            }
        });
    }

    public CheckBox checkIfCheckboxIsEnabled() {
        BOX.shouldBe(enabled);
        return this;
    }

    public CheckBox checkIfCheckboxIsDisabled() {
        BOX.shouldBe(disabled);
        return this;
    }

    public CheckBox checkIfCheckboxIsSelected() {
        BOX.shouldBe(selected);
        return this;
    }

    public CheckBox checkIfCheckboxIsUnselected() {
        BOX.shouldNotBe(selected);
        return this;
    }
}

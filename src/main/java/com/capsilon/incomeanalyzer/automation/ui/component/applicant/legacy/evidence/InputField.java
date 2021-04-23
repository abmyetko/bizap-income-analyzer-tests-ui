package com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.evidence;

import com.codeborne.selenide.SelenideElement;

public class InputField {

    public final SelenideElement INPUT;
    private final SelenideElement COMPONENT_CONTAINER; //NOSONAR

    public InputField(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer; //NOSONAR
        INPUT = componentContainer.$x(".//input");
    }
}

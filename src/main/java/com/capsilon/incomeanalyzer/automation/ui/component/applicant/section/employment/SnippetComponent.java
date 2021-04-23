package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment;

import com.codeborne.selenide.SelenideElement;

public class SnippetComponent {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement VALUE;
    public final SelenideElement SNIPPET_POPUP;

    public SnippetComponent(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        VALUE = COMPONENT_CONTAINER.$x("./div[@class='value']");
        SNIPPET_POPUP = COMPONENT_CONTAINER.$x("./popper-content");
    }
}

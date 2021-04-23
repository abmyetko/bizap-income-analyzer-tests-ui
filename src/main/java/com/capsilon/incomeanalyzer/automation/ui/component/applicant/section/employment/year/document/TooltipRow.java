package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document;

import com.codeborne.selenide.SelenideElement;

public class TooltipRow {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement DOCUMENT_NAME;
    public final SelenideElement EMPLOYER_NAME;
    public final SelenideElement START_DATE;
    public final SelenideElement END_DATE;

    public TooltipRow(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        DOCUMENT_NAME = COMPONENT_CONTAINER.$x(".//span[@class='name']");
        EMPLOYER_NAME = COMPONENT_CONTAINER.$x(".//span[contains(@class,'employer')]");
        START_DATE = COMPONENT_CONTAINER.$x(".//span[@class='start-date']");
        END_DATE = COMPONENT_CONTAINER.$x(".//span[@class='end-date']");
    }
}
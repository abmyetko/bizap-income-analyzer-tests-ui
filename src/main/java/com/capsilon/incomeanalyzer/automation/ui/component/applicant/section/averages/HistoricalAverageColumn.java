package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.averages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

@SuppressWarnings("squid:S1068")
public class HistoricalAverageColumn {

    public final SelenideElement COMPONENT_CONTAINER;
    public final ElementsCollection YEARS_PILLS;
    public final SelenideElement MONTHS_WORKED;
    public final SelenideElement AVG_MONTHLY_INCOME;
    public final SelenideElement RADIO_BUTTON_FIELD;

    public HistoricalAverageColumn(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        YEARS_PILLS = COMPONENT_CONTAINER.$$x(".//ia-dynamic-years/div");
        MONTHS_WORKED = COMPONENT_CONTAINER.$x(".//ia-column-cell[@data-testid='Months Worked']");
        AVG_MONTHLY_INCOME = COMPONENT_CONTAINER.$x(".//ia-column-cell[@data-testid='Avg Monthly Income']");
        RADIO_BUTTON_FIELD = COMPONENT_CONTAINER.$x(".//mat-radio-button/parent::ia-column-cell");
    }
}

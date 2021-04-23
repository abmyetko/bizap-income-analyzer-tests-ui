package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.summary;

import com.codeborne.selenide.SelenideElement;

public class AnnualSummaryColumn {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement YEAR_LABEL;
    public final SelenideElement TOTAL_MONTHS_WORKED;
    public final SelenideElement AVG_MONTHLY_INCOME;
    public final SelenideElement TRENDING;

    public AnnualSummaryColumn(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        YEAR_LABEL = COMPONENT_CONTAINER.$x("./div/div[@header]");
        TOTAL_MONTHS_WORKED = COMPONENT_CONTAINER.$x(".//ia-column-cell[@data-testid='Total Months Worked']");
        AVG_MONTHLY_INCOME = COMPONENT_CONTAINER.$x(".//ia-column-cell[@data-testid='Avg Monthly Income']");
        TRENDING = COMPONENT_CONTAINER.$x(".//ia-column-cell[@data-testid='Trending']/ia-trending");
    }
}

package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.averages;

import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$$x;

@SuppressWarnings("squid:S1068")
public class HistoricalAverages {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement LABEL;
    public final ElementsCollection ROWS_LABELS;
    public final ElementsCollection YEARS;

    public HistoricalAverages(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        LABEL = COMPONENT_CONTAINER.$x(".//div[@class='header']");
        ROWS_LABELS = $$x(".//div[@class='labels']");
        YEARS = COMPONENT_CONTAINER.$$x(".//ia-column[@data-testid]");
    }

    public HistoricalAverageColumn year(IncomeAvg year) {
        return new HistoricalAverageColumn(COMPONENT_CONTAINER.$x(String.format(".//ia-column[contains(@data-testid,'%s')]", year.value)));
    }
}

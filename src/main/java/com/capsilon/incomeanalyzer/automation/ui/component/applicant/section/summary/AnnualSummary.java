package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.summary;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$$x;

public class AnnualSummary {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement LABEL;
    public final ElementsCollection ROWS_LABELS;
    public final ElementsCollection YEARS;

    public AnnualSummary(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        LABEL = COMPONENT_CONTAINER.$x(".//div[@class='header']");
        ROWS_LABELS = $$x(".//div[@class='labels']");
        YEARS = COMPONENT_CONTAINER.$$x(".//ia-column[@data-testid]");
    }

    public AnnualSummaryColumn year(String year) {
        return new AnnualSummaryColumn(COMPONENT_CONTAINER.$x(String.format(".//ia-column[@data-testid='%s']", year)));
    }
}

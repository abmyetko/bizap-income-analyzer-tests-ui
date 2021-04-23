package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

public class DocumentsTooltip {

    public final SelenideElement COMPONENT_CONTAINER;
    public final List<TooltipRow> TOOLTIP_ROWS;

    public DocumentsTooltip(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        TOOLTIP_ROWS = new ArrayList<>();
        COMPONENT_CONTAINER.$$("div.document").forEach(it -> TOOLTIP_ROWS.add(new TooltipRow(it)));
    }

}

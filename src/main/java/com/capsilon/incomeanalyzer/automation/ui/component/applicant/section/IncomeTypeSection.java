package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section;

import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView.isTabExpanded;

public class IncomeTypeSection {

    public final SelenideElement COMPONENT_CONTAINER;
    public final IncomeTypeSectionHeader HEADER;
    public final IncomeTypeSectionBody BODY;

    public IncomeTypeSection(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        HEADER = new IncomeTypeSectionHeader(componentContainer.$x(".//mat-expansion-panel-header"));
        BODY = new IncomeTypeSectionBody(componentContainer.$x(".//mat-expansion-panel//div[contains(@class,'mat-expansion-panel-body')]"),
                IncomePartType.valueOf(COMPONENT_CONTAINER.getAttribute("data-testid")));
    }

    public IncomeTypeSection(SelenideElement componentContainer, IncomePartType incomePartType) {
        COMPONENT_CONTAINER = componentContainer;
        HEADER = new IncomeTypeSectionHeader(componentContainer.$x(".//mat-expansion-panel-header"));
        BODY = new IncomeTypeSectionBody(componentContainer.$x(".//mat-expansion-panel//div[contains(@class,'mat-expansion-panel-body')]"), incomePartType);
    }

    public Boolean isExpanded() {
        return isTabExpanded(HEADER.COMPONENT_CONTAINER);
    }
}

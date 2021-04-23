package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section;

import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.CheckBox;
import com.codeborne.selenide.SelenideElement;

public class IncomeTypeSectionHeader {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement NAME;
    public final SelenideElement TOTAL_INCOME;
    public final CheckBox CHECKBOX;
    public final SelenideElement TAG_TYPE;

    public IncomeTypeSectionHeader(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        NAME = componentContainer.$x(".//ia-includable/span[@class='type']");
        TOTAL_INCOME = componentContainer.$x(".//span[@class='amount']");
        CHECKBOX = new CheckBox(componentContainer.$x(".//ia-includable/mat-checkbox"));
        TAG_TYPE = componentContainer.$x(".//div[@class='tags']/div");
    }
}

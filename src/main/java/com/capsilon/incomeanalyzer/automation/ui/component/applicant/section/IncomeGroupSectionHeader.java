package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section;

import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.CheckBox;
import com.codeborne.selenide.SelenideElement;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.getBigDecimalValueOfIncome;

public class IncomeGroupSectionHeader {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement NAME;
    public final SelenideElement TOTAL_INCOME;
    public final CheckBox CHECKBOX;

    public IncomeGroupSectionHeader(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        NAME = componentContainer.$x("./span/ia-includable");
        TOTAL_INCOME = componentContainer.$x("./span/ia-includable/div[@class='total']");
        CHECKBOX = new CheckBox(componentContainer.$x("./span/ia-includable/mat-checkbox"));
    }

    public BigDecimal getIncomeValue() {
        return getBigDecimalValueOfIncome(TOTAL_INCOME.getText());
    }

    public IncomeGroupSectionHeader scrollIntoView(boolean alignToTop) {
        COMPONENT_CONTAINER.scrollIntoView(alignToTop);
        return this;
    }

    public IncomeGroupSectionHeader scrollIntoView(String scrollIntoViewOptions) {
        COMPONENT_CONTAINER.scrollIntoView(scrollIntoViewOptions);
        return this;
    }

    public IncomeGroupSectionHeader scrollIntoViewCenter() {
        COMPONENT_CONTAINER.scrollIntoView("{block: \"center\"}");
        return this;
    }
}

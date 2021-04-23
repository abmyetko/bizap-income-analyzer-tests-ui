package com.capsilon.incomeanalyzer.automation.ui.component.applicant;

import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.IncomeTypeSection;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView.collapseOrExpandTab;

public class IncomeCategoryIncomeType {

    public final SelenideElement COMPONENT_CONTAINER;
    public final ElementsCollection INCOME_TYPE_COLLECTION;

    private final String incomeTypeLocator = "./ia-current-employment-list/ia-current-employment/ia-income-type-foldable";

    public IncomeCategoryIncomeType(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        INCOME_TYPE_COLLECTION = COMPONENT_CONTAINER.$$x(incomeTypeLocator);
    }

    public IncomeTypeSection incomeType(IncomePartType incomePartType) {
        IncomeTypeSection section = incomeTypeWithoutExpanding(incomePartType);
        collapseOrExpandTab(section.HEADER.COMPONENT_CONTAINER, true);
        return section;
    }

    public IncomeTypeSection incomeTypeWithoutExpanding(IncomePartType incomePartType) {
        IncomeTypeSection section = new IncomeTypeSection(
                COMPONENT_CONTAINER.$x(String.format(incomeTypeLocator + "[@data-testid='%s']", incomePartType.toString())), incomePartType);
        section.COMPONENT_CONTAINER.scrollIntoView(true);
        return section;
    }

    public List<IncomeTypeSection> incomeTypeList() {
        List<IncomeTypeSection> incomeTypeSectionList = new ArrayList<>();
        COMPONENT_CONTAINER.$$x(incomeTypeLocator).forEach(incomeType -> {
            IncomeTypeSection section = new IncomeTypeSection(incomeType);
            incomeTypeSectionList.add(section);
            collapseOrExpandTab(section.HEADER.COMPONENT_CONTAINER, true);
        });
        return incomeTypeSectionList;
    }

    public IncomeCategoryIncomeType expandAllIncomeTypes() {
        for (SelenideElement incomeTypeTab : COMPONENT_CONTAINER.$$x(incomeTypeLocator)) {
            IncomeTypeSection section = new IncomeTypeSection(incomeTypeTab, null);
            section.COMPONENT_CONTAINER.scrollIntoView(true);
            collapseOrExpandTab(section.HEADER.COMPONENT_CONTAINER, true);
        }
        return this;
    }
}

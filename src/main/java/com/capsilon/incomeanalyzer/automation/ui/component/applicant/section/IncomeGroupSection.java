package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section;

import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView.collapseOrExpandTab;

public class IncomeGroupSection {

    public final SelenideElement COMPONENT_CONTAINER;
    public final IncomeGroupSectionHeader HEADER;
    public final ElementsCollection INCOME_TYPE_COLLECTION;

    private static final String CURRENT_EMPLOYMENT_LOCATOR = "./div/div/ia-current-employment";

    public IncomeGroupSection(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        HEADER = new IncomeGroupSectionHeader(COMPONENT_CONTAINER.$x("./mat-expansion-panel-header"));
        INCOME_TYPE_COLLECTION = COMPONENT_CONTAINER.$$x(CURRENT_EMPLOYMENT_LOCATOR + "/ia-income-type-foldable");
    }

    public IncomeTypeSection incomeType(IncomePartType incomePartType) {
        IncomeTypeSection section = incomeTypeWithoutExpanding(incomePartType);
        collapseOrExpandTab(section.HEADER.COMPONENT_CONTAINER, true);
        return section;
    }

    public IncomeTypeSection incomeTypeWithoutExpanding(IncomePartType incomePartType) {
        IncomeTypeSection section = new IncomeTypeSection(COMPONENT_CONTAINER.$x(
                String.format("%s/ia-income-type-foldable[@data-testid='%s']", CURRENT_EMPLOYMENT_LOCATOR, incomePartType.toString())), incomePartType);
        section.COMPONENT_CONTAINER.scrollIntoView(true);
        return section;
    }

    public List<IncomeTypeSection> incomeTypeList() {
        List<IncomeTypeSection> incomeTypeSectionList = new ArrayList<>();
        INCOME_TYPE_COLLECTION.forEach(incomeType -> {
            IncomeTypeSection section = new IncomeTypeSection(incomeType);
            incomeTypeSectionList.add(section);
            collapseOrExpandTab(section.HEADER.COMPONENT_CONTAINER, true);
        });
        return incomeTypeSectionList;
    }

    public IncomeGroupSection expandAllIncomeTypes() {
        for (SelenideElement incomeTypeTab : COMPONENT_CONTAINER.$$x(CURRENT_EMPLOYMENT_LOCATOR + "/ia-income-type-foldable")) {
            IncomeTypeSection section = new IncomeTypeSection(incomeTypeTab, null);
            section.COMPONENT_CONTAINER.scrollIntoView(true);
            collapseOrExpandTab(section.HEADER.COMPONENT_CONTAINER, true);
        }
        return this;
    }
}

package com.capsilon.incomeanalyzer.automation.ui.component.applicant;

import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.IncomeGroupSection;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.IncomeTypeSection;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.NoSuchElementException;

import static com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView.collapseOrExpandTab;

public class IncomeCategoryIncomeGroup {

    public static final String PRIMARY_GROUP_NAME = "Primary  employment";
    public static final String SEC_GROUP_NAME_NO_NUMBER = "Secondary  employment ";
    public static final String SEC_GROUP_NAME_1 = "Secondary  employment 1";
    public static final String SEC_GROUP_NAME_2 = "Secondary  employment 2";
    public final SelenideElement COMPONENT_CONTAINER;
    public final ElementsCollection INCOME_GROUP_COLLECTION;

    private final String incomeGroupLocator = "./ia-current-employment-list/mat-expansion-panel";

    public IncomeCategoryIncomeGroup(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        INCOME_GROUP_COLLECTION = COMPONENT_CONTAINER.$$x(incomeGroupLocator);
    }


    public IncomeCategoryIncomeType onlyPrimaryJobView() {
        return new IncomeCategoryIncomeType(COMPONENT_CONTAINER);
    }

    public IncomeGroupSection incomeGroup(String incomeGroupName) {
        IncomeGroupSection group = incomeGroupWithoutExpanding(incomeGroupName);
        collapseOrExpandTab(group.HEADER.COMPONENT_CONTAINER, true);
        return group;
    }

    public IncomeGroupSection incomeGroupByCurrentEmployerName(String employerName) {
        return incomeGroupByCurrentEmployerName(employerName, false);
    }

    public IncomeGroupSection incomeGroupByCurrentEmployerName(String employerName, Boolean expand) {
        for (SelenideElement incomeGroup : INCOME_GROUP_COLLECTION) {
            IncomeGroupSection employmentLocator = new IncomeGroupSection(incomeGroup);
            if (employerName.equalsIgnoreCase(employmentLocator.INCOME_TYPE_COLLECTION.get(0).$x(".//ia-employment").attr("data-testid"))) {
                if (expand) {
                    collapseOrExpandTab(employmentLocator.HEADER.COMPONENT_CONTAINER, true);
                }
                return employmentLocator;
            }
        }
        throw new NoSuchElementException(String.format("Income Group %s could not be found exception", employerName));
    }

    public IncomeGroupSection incomeGroupWithoutExpanding(String incomeGroupName) {
        return new IncomeGroupSection(COMPONENT_CONTAINER.$x(String.format(incomeGroupLocator + "[@data-testid='%s']", incomeGroupName)));
    }

    public IncomeTypeSection incomeTypePrimaryGroup(IncomePartType incomePartType) {
        return incomeGroup(PRIMARY_GROUP_NAME).incomeType(incomePartType);
    }

    public IncomeTypeSection incomeTypeSecondaryNoNumberGroup(IncomePartType incomePartType) {
        return incomeGroup(SEC_GROUP_NAME_NO_NUMBER).incomeType(incomePartType);
    }

    public IncomeTypeSection incomeTypePrimaryGroupWithoutExpanding(IncomePartType incomePartType) {
        return incomeGroup(PRIMARY_GROUP_NAME).incomeTypeWithoutExpanding(incomePartType);
    }

    public List<IncomeTypeSection> incomeTypePrimaryGroupList() {
        return incomeGroup(PRIMARY_GROUP_NAME).incomeTypeList();
    }

    public IncomeCategoryIncomeGroup expandAllIncomeTypesForPrimaryGroup() {
        incomeGroup(PRIMARY_GROUP_NAME).expandAllIncomeTypes();
        return this;
    }
}

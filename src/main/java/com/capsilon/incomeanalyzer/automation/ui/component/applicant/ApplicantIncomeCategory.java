package com.capsilon.incomeanalyzer.automation.ui.component.applicant;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public class ApplicantIncomeCategory {

    public final SelenideElement COMPONENT_CONTAINER;
    public final ApplicantIncomeCategoryHeader HEADER;
    public final ElementsCollection INCOME_GROUP_COLLECTION;

    public ApplicantIncomeCategory(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        HEADER = new ApplicantIncomeCategoryHeader(componentContainer.$x("./*[@class='section-header']"));
        INCOME_GROUP_COLLECTION = COMPONENT_CONTAINER.$$x("./ia-current-employment-list/mat-expansion-panel");
    }

    public IncomeCategoryIncomeType onlyPrimaryJobView() {
        return new IncomeCategoryIncomeType(COMPONENT_CONTAINER);
    }

    public IncomeCategoryIncomeGroup multiCurrentJobView() {
        return new IncomeCategoryIncomeGroup(COMPONENT_CONTAINER);
    }
}

package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment;

import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.EmploymentYear;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public class EmploymentBody {

    public final SelenideElement COMPONENT_CONTAINER;
    public final ElementsCollection LABELS;
    public final ElementsCollection FOOTNOTE;

    public EmploymentBody(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        LABELS = COMPONENT_CONTAINER.$$x(".//div[@class='labels']/div");
        FOOTNOTE = COMPONENT_CONTAINER.$$x(".//div[@class='footnote ng-star-inserted']");
    }

    public EmploymentYear employmentYear(String year) {
        EmploymentYear employmentYear = new EmploymentYear(COMPONENT_CONTAINER.$x(String.format(".//ia-column[@data-testid='%s']", year)));
        IncomeAnalyzerPage.scrollIntoViewCenter(employmentYear.HEADER).click();
        return new EmploymentYear(COMPONENT_CONTAINER.$x(String.format(".//ia-column[@data-testid='%s']", year)));
    }
}
package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section;

import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.averages.HistoricalAverages;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.Employment;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.summary.AnnualSummary;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

public class IncomeTypeSectionBody {

    public final SelenideElement COMPONENT_CONTAINER;
    public final HistoricalAverages HISTORICAL_AVERAGES;
    public final AnnualSummary ANNUAL_SUMMARY;
    private final IncomePartType INCOME_PART_TYPE;

    public IncomeTypeSectionBody(SelenideElement componentContainer, IncomePartType incomePartType) {
        COMPONENT_CONTAINER = componentContainer;
        HISTORICAL_AVERAGES = new HistoricalAverages(COMPONENT_CONTAINER.$x(".//ia-historical-averages"));
        ANNUAL_SUMMARY = new AnnualSummary(COMPONENT_CONTAINER.$x(".//ia-annual-summary"));
        INCOME_PART_TYPE = incomePartType;
    }

    public Employment employment(String jobName) {
        Employment employment = new Employment(COMPONENT_CONTAINER.$x(String.format(".//ia-employment[@data-testid='%s']", jobName.toLowerCase(Locale.US))), INCOME_PART_TYPE);
        employment.COMPONENT_CONTAINER.scrollIntoView(true);
        return employment;
    }

    public Employment employmentSameName(String jobName, Boolean isCurrent) {
        ElementsCollection employments = COMPONENT_CONTAINER.$$x(String.format(".//ia-employment[@data-testid='%s']", jobName.toLowerCase(Locale.US)));
        Employment employment = new Employment(isCurrent ? employments.get(0) : employments.get(1), INCOME_PART_TYPE);
        employment.COMPONENT_CONTAINER.scrollIntoView(true);
        return employment;
    }
}

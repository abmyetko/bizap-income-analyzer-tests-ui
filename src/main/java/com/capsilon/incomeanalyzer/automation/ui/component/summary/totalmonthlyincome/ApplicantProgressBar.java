package com.capsilon.incomeanalyzer.automation.ui.component.summary.totalmonthlyincome;

import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.getDoubleValueOfIncome;
import static com.codeborne.selenide.Condition.visible;

public class ApplicantProgressBar {

    public TuttiFrutti TUTTI_FRUTTI; //NOSONAR
    public final SelenideElement INCOME;
    public final SelenideElement TRENDING;
    public final SelenideElement NAME;
    public final SelenideElement DISTRIBUTION;
    private final SelenideElement COMPONENT_CONTAINER; //NOSONAR

    public ApplicantProgressBar(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer; //NOSONAR
        DISTRIBUTION = COMPONENT_CONTAINER.$x("./div");
        NAME = COMPONENT_CONTAINER.$x(".//div[@class='applicant']");
        INCOME = COMPONENT_CONTAINER.$x(".//div[@class='income']/span");
        TRENDING = COMPONENT_CONTAINER.$x(".//ia-trending-value");
        TUTTI_FRUTTI = new TuttiFrutti(COMPONENT_CONTAINER.$x(".//ia-tutti-frutti-chart"));
    }

    public ApplicantProgressBar shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        NAME.shouldBe(visible);
        INCOME.shouldBe(visible);
        return this;
    }

    public double getNumericalValueOfIncome() {
        return getDoubleValueOfIncome(INCOME.text());
    }

    public boolean compareProgressBarIncomeToTooltipSum() {
        if (!"$0.00".equals(INCOME.text())) {
            return getDoubleValueOfIncome((INCOME.text())) - TUTTI_FRUTTI.getTooltipIncomeSum() <= 0.001; //NOSONAR
        }
        return true;
    }
}

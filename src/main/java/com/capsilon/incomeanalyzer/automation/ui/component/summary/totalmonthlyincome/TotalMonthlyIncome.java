package com.capsilon.incomeanalyzer.automation.ui.component.summary.totalmonthlyincome;

import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.DOUBLE_DELTA_MEDIUM;
import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.getDoubleValueOfIncome;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TotalMonthlyIncome {

    public final SelenideElement TOTAL_ADJUSTED_MONTHLY_INCOME_HEADER;
    public final SelenideElement TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE;
    public final SelenideElement TOTAL_ADJUSTED_MONTHLY_INCOME_TRENDING;
    private final SelenideElement COMPONENT_CONTAINER;

    public TotalMonthlyIncome(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        TOTAL_ADJUSTED_MONTHLY_INCOME_HEADER = componentContainer.$x(".//h4");
        TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE = componentContainer.$x(".//div[contains(@class,'qualifying-income')]/span");
        TOTAL_ADJUSTED_MONTHLY_INCOME_TRENDING = componentContainer.$x(".//div[contains(@class,'qualifying-income')]/ia-trending-value");
    }

    public TotalMonthlyIncome shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        TOTAL_ADJUSTED_MONTHLY_INCOME_HEADER.shouldBe(visible);
        TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE.shouldBe(visible);
        return this;
    }

    public double getTotalMonthlyIncomeNumericalValue() {
        return getDoubleValueOfIncome(TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE.text());
    }

    public int getProgressBarNumber() {
        return COMPONENT_CONTAINER.$$x(".//ia-applicant-income-distribution").size();
    }

    public ApplicantProgressBar getApplicantProgressBar(int applicantIndex) {
        return new ApplicantProgressBar(COMPONENT_CONTAINER.$$x(".//ia-applicant-income-distribution").get(applicantIndex));
    }

    public double getApplicableApplicantsIncomeSum() {
        double sum = 0;
        for (int i = 0; i < getProgressBarNumber(); i++) {
            ApplicantProgressBar applicantProgressBar = getApplicantProgressBar(i);
            if (!applicantProgressBar.INCOME.getAttribute("class").contains("striked")) {
                sum += getDoubleValueOfIncome(applicantProgressBar.INCOME.text());
            }
        }
        return sum;
    }

    public double[] getApplicantIncomeLists() { //minus value is flag that income is not selected
        double[] sum = new double[getProgressBarNumber()];
        for (int i = 0; i < getProgressBarNumber(); i++) {
            ApplicantProgressBar applicantProgressBar = getApplicantProgressBar(i);
            if (!applicantProgressBar.DISTRIBUTION.getAttribute("class").contains("excluded")) {
                sum[i] = getDoubleValueOfIncome(applicantProgressBar.INCOME.text());
            } else {
                sum[i] = -getDoubleValueOfIncome(applicantProgressBar.INCOME.text());
            }
        }
        return sum;
    }

    public void checkIfTotalMonthlyIncomeIsEqualToSumOfApplicableIncomes() {
        assertEquals(getTotalMonthlyIncomeNumericalValue(), getApplicableApplicantsIncomeSum(), DOUBLE_DELTA_MEDIUM);
    }
}

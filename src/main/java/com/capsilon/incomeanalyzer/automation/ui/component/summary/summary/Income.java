package com.capsilon.incomeanalyzer.automation.ui.component.summary.summary;

import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

public class Income {

    public final SelenideElement COMPONENT_CONTAINER;
    private final int incomeTypeSize;

    public Income(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        incomeTypeSize = componentContainer.$$x(".//mat-expansion-panel").size();
    }

    public int getIncomeTypesNumber() {
        return incomeTypeSize;
    }

    public double getApplicantIncomeTypesSum() {
        double sum = 0;
        for (int i = 0; i < incomeTypeSize; i++) {
            sum += getIncomeById(i).getPanel().getNumericalValueOfIncome();
        }
        return sum;
    }

    public Type getIncomeById(int incomeIndex) {
        return new Type(COMPONENT_CONTAINER.$$x(".//mat-expansion-panel").get(incomeIndex));
    }

    public Type getIncomeByName(String incomeName) {
        return new Type(COMPONENT_CONTAINER.$x(".//ul[@data-income-type='" + incomeName.toUpperCase(Locale.US)
                + "']/parent::ia-income-type-summary//ancestor::mat-expansion-panel"));
    }
}
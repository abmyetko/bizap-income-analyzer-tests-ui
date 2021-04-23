package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum MortgageType {

    CONVENTIONAL("Conventional", "01"),
    FHA("FHA", "03");

    public final String textName;
    public final String numCode;

    MortgageType(String textName, String numCode) {
        this.textName = textName;
        this.numCode = numCode;
    }
}
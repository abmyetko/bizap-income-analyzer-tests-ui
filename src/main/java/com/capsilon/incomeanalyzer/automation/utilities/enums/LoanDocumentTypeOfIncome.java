package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum LoanDocumentTypeOfIncome {

    BASE("Base", "20"),
    OVERTIME("Overtime", "09"),
    BONUS("Bonus", "08"),
    COMMISSIONS("Commissions", "10"),
    OTHER("Other", "45");

    public final String textName;
    public final String numCode;

    LoanDocumentTypeOfIncome(String textName, String numCode) {
        this.textName = textName;
        this.numCode = numCode;

    }
}

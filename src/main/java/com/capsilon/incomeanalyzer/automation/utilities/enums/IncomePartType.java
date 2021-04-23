package com.capsilon.incomeanalyzer.automation.utilities.enums;


public enum IncomePartType {

    BASE_PAY("|BASE_PAY"),
    OVERTIME("|OVERTIME"),
    COMMISSIONS("|COMMISSIONS"),
    BONUS("|BONUS");
    public final String value;

    IncomePartType(String value) {
        this.value = value;
    }

    public static IncomePartType valueOfLabel(String value) {
        for (IncomePartType e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}

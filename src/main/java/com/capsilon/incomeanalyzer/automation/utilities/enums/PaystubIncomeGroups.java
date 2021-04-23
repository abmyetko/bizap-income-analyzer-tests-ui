package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum PaystubIncomeGroups {
    REGULAR("Regular"),
    HOLIDAY("Holiday"),
    PTO("Pto"),

    OVERTIME("Overtime"),
    OT("OT"),
    HOLIDAY_OT("Holiday OT"),

    COMMISSION("Commission"),
    COMMISSIONS("Commissions"),
    COMM("Comm"),

    BONUS("Bonus"),
    INCENTIVE("Incentive"),
    MONTHLY_BONUS("Monthly Bonus"),

    ;

    public final String value;

    PaystubIncomeGroups(String value) {
        this.value = value;
    }

    public static PaystubIncomeGroups valueOfLabel(String value) {
        for (PaystubIncomeGroups e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}

package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum IncomeFrequency {

    HOURLY(52, "Hourly"),
    WEEKLY(52, "Weekly"),
    BI_WEEKLY(26, "Bi-Weekly"),
    SEMI_MONTHLY(24, "Semi-Monthly"),
    MONTHLY(12, "Monthly"),
    QUARTERLY(4, "Quarterly"),
    SEMI_ANNUALLY(2, "Semi-Annually"),
    ANNUALLY(1, "Annually"),
    UNKNOWN(0, "Unknown");

    public final Integer value;
    public final String text;

    IncomeFrequency(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static IncomeFrequency valueOfLabel(String value) {
        for (IncomeFrequency e : values()) {
            if (e.text.equals(value)) {
                return e;
            }
        }
        return null;
    }

}

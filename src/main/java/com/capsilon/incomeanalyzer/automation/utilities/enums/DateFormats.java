package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum DateFormats {
    YYYY_MM_DD_DASH("yyyy-MM-dd"),
    MM_DD_YYYY_F_SLASH("MM/dd/yyyy"),
    MM_DD_YY_F_SLASH("MM/dd/yy"),
    M_D_YY_F_SLASH("M/d/yy");

    public final String dateFormat;

    DateFormats(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}

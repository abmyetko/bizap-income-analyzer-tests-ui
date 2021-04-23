package com.capsilon.incomeanalyzer.automation.utilities.enums;

import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType.*;

@SuppressWarnings("squid:S1192")
public enum IncomeAvg {

    BASEPAY_YTD_AVG(BASE_PAY.value + "|YTD_AVG"),
    BASEPAY_YTD_AVG_PLUS_PREV_YR(BASE_PAY.value + "|YTD_AVG_PLUS_PREV_YR"),
    BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS(BASE_PAY.value + "|YTD_AVG_PLUS_PREV_TWO_YRS"),
    COMMISSION_YTD_AVG(COMMISSIONS.value + "|YTD_AVG"),
    COMMISSION_YTD_AVG_PLUS_PREV_YR(COMMISSIONS.value + "|YTD_AVG_PLUS_PREV_YR"),
    COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS(COMMISSIONS.value + "|YTD_AVG_PLUS_PREV_TWO_YRS"),
    OVERTIME_YTD_AVG(OVERTIME.value + "|YTD_AVG"),
    OVERTIME_YTD_AVG_PLUS_PREV_YR(OVERTIME.value + "|YTD_AVG_PLUS_PREV_YR"),
    OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS(OVERTIME.value + "|YTD_AVG_PLUS_PREV_TWO_YRS"),
    BONUS_YTD_AVG(BONUS.value + "|YTD_AVG"),
    BONUS_YTD_AVG_PLUS_PREV_YR(BONUS.value + "|YTD_AVG_PLUS_PREV_YR"),
    BONUS_YTD_AVG_PLUS_PREV_TWO_YRS(BONUS.value + "|YTD_AVG_PLUS_PREV_TWO_YRS"),
    NONE_AVG("");

    public final String value;

    IncomeAvg(String value) {
        this.value = value;
    }

    public static IncomeAvg valueOfLabel(String value) {
        for (IncomeAvg e : values()) {
            if (e.value.equals(value.substring(value.indexOf('|', value.indexOf('|') + 1)))) {
                return e;
            }
        }
        return null;
    }

}

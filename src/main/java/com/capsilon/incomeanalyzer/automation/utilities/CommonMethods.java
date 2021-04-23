package com.capsilon.incomeanalyzer.automation.utilities;

import com.capsilon.test.run.confiuration.BizappsConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;

public final class CommonMethods {

    public static final double DOUBLE_DELTA_SMALL = 3D;
    public static final double DOUBLE_DELTA_MEDIUM = 10D;
    public static final double DOUBLE_DELTA_BIG = 500D;
    public static final Integer YEAR_TO_DATE = LocalDateTime.now().getYear();
    public static final Integer ONE_YEAR_PRIOR = LocalDateTime.now().getYear() - 1;
    public static final Integer TWO_YEARS_PRIOR = LocalDateTime.now().getYear() - 2;
    public static final Integer THREE_YEARS_PRIOR = LocalDateTime.now().getYear() - 3;
    public static final Integer FOUR_YEARS_PRIOR = LocalDateTime.now().getYear() - 4;
    public static final int TIMEOUT_TWO_SECONDS = shouldIncreaseTimeoutValues() ? 8000 : 2000;
    public static final int TIMEOUT_FIVE_SECONDS = shouldIncreaseTimeoutValues() ? 20000 : 5000;
    public static final int TIMEOUT_TEN_SECONDS = shouldIncreaseTimeoutValues() ? 40000 : 10000;
    public static final int TIMEOUT_FIFTEEN_SECONDS = shouldIncreaseTimeoutValues() ? 60000 : 15000;
    public static final int TIMEOUT_TWENTY_SECONDS = shouldIncreaseTimeoutValues() ? 80000 : 20000;
    public static final int TIMEOUT_FORTY_SECONDS = shouldIncreaseTimeoutValues() ? 160_000 : 40000;
    public static final int TIMEOUT_ONE_MINUTE = shouldIncreaseTimeoutValues() ? 240_0000 : 60000;
    public static final int TIMEOUT_TWO_MINUTES = shouldIncreaseTimeoutValues() ? 480_000 : 120_000;
    public static final int TIMEOUT_THREE_MINUTES = shouldIncreaseTimeoutValues() ? 720_000 : 180_000;
    public static final int TIMEOUT_FIVE_MINUTES = shouldIncreaseTimeoutValues() ? 1_200_000 : 300_000;
    public static final int TIMEOUT_SIX_MINUTES = shouldIncreaseTimeoutValues() ? 1_440_000 : 360_000;
    public static final int TIMEOUT_TEN_MINUTES = shouldIncreaseTimeoutValues() ? 2_400_000 : 600_000;
    public static final int HUNDRED_PERCENT = 100;
    public static final String ARIA_CHECKED = "aria-checked";
    public static final String ARIA_LABEL = "aria-label";
    public static final String ARIA_EXPANDED = "aria-expanded";
    public static final String JOHN_NAME = "John Homeowner";
    public static final String JOHN_SSN = "999-40-5000";
    public static final String MARY_NAME = "Mary Homeowner";
    public static final String HOMEOWNER_ADDRESS = "175 13th Street";
    public static final String MARY_SSN = "500-22-2000";
    public static final String BLUE_YONDER_NAME = "Blue Yonder Airlines";
    public static final String BLUE_YONDER_ADDRESS = "1 Clove Rd";
    public static final String AWESOME_COMPUTERS_NAME = "Awesome Computers";
    public static final String AWESOME_COMPUTERS_ADDRESS = "12367 E Street";
    public static final String BALDWIN_MUSEUM_NAME = "Baldwin Museum";
    public static final String BALDWIN_MUSEUM_ADDRESS = "11111 Fountain Street";
    public static final String WASHINGTON_ADDRESS = "Washington DC 20013";
    public static final String DATE_HALF_MARCH = "03/15/" + YEAR_TO_DATE;
    public static final String INCOME_64K = "64932.61";
    public static final String INCOME_47K = "47174.40";
    public static final String INCOME_30K = "30310.88";
    public static final String INCOME_1K = "1142.34";
    public static final String INCOME_900 = "925.82";
    public static final String INCOME_400 = "433.04";
    public static final String INCOME_340 = "342.82";
    public static final String INCOME_ZERO = "0.00";
    public static final Integer PERIOD_NUMBER_WEEKLY = 52;
    public static final Integer PERIOD_NUMBER_BI_WEEKLY = 26;
    public static final Integer PERIOD_NUMBER_SEMI_MONTHLY = 24;
    public static final Integer PERIOD_NUMBER_MONTHLY = 12;
    public static final Integer PERIOD_NUMBER_QUARTERLY = 4;
    public static final Integer PERIOD_NUMBER_SEMI_ANNUALLY = 2;
    public static final Integer PERIOD_NUMBER_ANNUALLY = 1;
    public static final int MAX_FNM_DATE_FORMAT_LENGTH = 8;
    public static final int YEAR_SUBSTRING_INDEX_START = 6;
    public static final int YEAR_SUBSTRING_INDEX_END = 10;
    public static final int MONTH_SUBSTRING_INDEX_START = 0;
    public static final int MONTH_SUBSTRING_INDEX_END = 2;
    public static final int DAY_SUBSTRING_INDEX_START = 3;
    public static final int DAY_SUBSTRING_INDEX_END = 5;

    private CommonMethods() {
    }

    public static String transformDateToFnmFormat(String signDate) {
        if (signDate == null || signDate.isEmpty())
            return "";
        if (signDate.length() == MAX_FNM_DATE_FORMAT_LENGTH) //NOSONAR
            return signDate;
        return signDate.substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END) +
                signDate.substring(MONTH_SUBSTRING_INDEX_START, MONTH_SUBSTRING_INDEX_END) +
                signDate.substring(DAY_SUBSTRING_INDEX_START, DAY_SUBSTRING_INDEX_END);
    }

    public static double getSumOfPositiveNumbers(double[] inputArray) {
        double selectedSum = 0;
        for (double income : inputArray) {
            if (income > 0.0D) {
                selectedSum += income;
            }
        }
        return selectedSum;
    }

    public static int getNextLeapYear() {
        int checkedYear = LocalDate.now().getYear();
        boolean isLeapYear = false;
        while (!isLeapYear) {
            if (checkedYear % 400 == 0) { //NOSONAR
                isLeapYear = true;
            } else if (checkedYear % 100 == 0) { //NOSONAR
                checkedYear++;
            } else if (checkedYear % 4 == 0) { //NOSONAR
                isLeapYear = true;
            } else {
                checkedYear++;
            }
        }
        return checkedYear;
    }

    public static LocalDate getThisWeekMondayDate(String fullDate) {
        String[] split = fullDate.split("/", THREE);
        int dayNumber = Integer.parseInt(split[1]);
        int monthNumber = Integer.parseInt(split[0]);
        int yearNumber = Integer.parseInt(split[TWO]);

        LocalDate ld = LocalDate.of(yearNumber, monthNumber, dayNumber);

        return ld.with(DayOfWeek.MONDAY);
    }

    public static LocalDate getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek day) {
        return LocalDate.now().with(TemporalAdjusters.lastInMonth(day));
    }

    public static BigDecimal bigD(Integer value) {
        return bigD(new BigDecimal(value), BIG_DECIMAL_PRECISION_TWO_POINTS);
    }

    public static BigDecimal bigD(Integer value, Integer scale) {
        return bigD(new BigDecimal(value), scale);
    }

    public static BigDecimal bigD(Double value) {
        return bigD(new BigDecimal(value), BIG_DECIMAL_PRECISION_TWO_POINTS);
    }

    public static BigDecimal bigD(String value) {
        if (value.isEmpty())
            return bigD(new BigDecimal(0), BIG_DECIMAL_PRECISION_TWO_POINTS);
        return bigD(new BigDecimal(value), BIG_DECIMAL_PRECISION_TWO_POINTS);
    }

    public static BigDecimal bigD(Double value, Integer scale) {
        return bigD(new BigDecimal(value), scale);
    }

    public static BigDecimal bigD(BigDecimal value) {
        return bigD(value, BIG_DECIMAL_PRECISION_TWO_POINTS);
    }

    public static BigDecimal bigD(BigDecimal value, Integer scale) {
        if (value != null)
            return value.setScale(scale, RoundingMode.HALF_UP);
        else
            return new BigDecimal(0).setScale(scale, RoundingMode.HALF_UP);
    }

    private static Boolean shouldIncreaseTimeoutValues() {
        if (!BizappsConfig.getString("bizapps.ia.increaseTimeouts", "").isEmpty())
            return BizappsConfig.getBoolean("bizapps.ia.increaseTimeouts", false);

        String envId = BizappsConfig.getEnvId();
        return !envId.equals("dev-ia") && !envId.equals("qa-ia");
    }
}

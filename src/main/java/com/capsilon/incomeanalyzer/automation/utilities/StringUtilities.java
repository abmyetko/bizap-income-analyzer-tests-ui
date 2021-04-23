package com.capsilon.incomeanalyzer.automation.utilities;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.bigD;

public final class StringUtilities {
    public static final Pattern TIME_ON_JOB_PATTERN = Pattern.compile("((?:\\d+(?:y))?(?: ?\\d+m|(?: ?\\d+\\.\\d{1,2}m))?)");
    public static final Pattern DIGITS_PATTERN = Pattern.compile("\\d*");
    public static final Pattern NEWLINE_PATTERN = Pattern.compile("\\n");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
    private static final Pattern DOLLAR_PATTERN = Pattern.compile("\\$");
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final Pattern DOT_PATTERN = Pattern.compile(".");
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D");
    private static final Pattern RESPONSE_ID_PATTERN = Pattern.compile("(?<=\"id\":)[^(,})]*");
    private static final Pattern RESPONSE_REFID_PATTERN = Pattern.compile("(?<=\"refId\":)[^(,})]*");
    private static final Pattern RESPONSE_DOCUMENT_ID_PATTERN = Pattern.compile("(?<=\"documentId\":)[^(,})]*");
    private static final Pattern RESPONSE_DOC_ID_PATTERN = Pattern.compile("(?<=\"docId\":)[^(,})]*");
    private static final Pattern RESPONSE_APPLICANT_ID_PATTERN = Pattern.compile("(?<=\"applicantId\":)[^(,})]*");
    private static final Pattern RESPONSE_CALCULATION_DATE_PATTERN = Pattern.compile("(?<=\"calculationDate\":)[^(,})]*");
    private static final Pattern RESPONSE_EXECUTION_DATE_PATTERN = Pattern.compile("(?<=\"executionDate\":)[^(,})]*");
    private static final Pattern RESPONSE_STATUS_MODIFICATION_PATTERN = Pattern.compile("(?<=\"lastStatusModification\":)[^(,})]*");
    private static final Pattern RESPONSE_DOCUMENT_INT_ID_PATTERN = Pattern.compile("(?<=\")\\d{1,20}(?=\")");
    private static final Pattern RESPONSE_DOCUMENT_DATA_PATTERN = Pattern.compile("(?<=\"docIntId\":)(.|\\n)*?}(?=},)");
    private static final Pattern RESPONSE_RULE_APPLICANT_ID_3_PATTERN = Pattern.compile("(?<=\"INC-\\d{3})#[^\"]*");
    private static final Pattern RESPONSE_RULE_APPLICANT_ID_4_PATTERN = Pattern.compile("(?<=\"INC-\\d{4})#[^\"]*");
    private static final Pattern RESPONSE_RULE_STATUS_3_PATTERN = Pattern.compile("(?<=\"ruleId\":\"\\S{7}\",\"status\":)[^(,})]*");
    private static final Pattern RESPONSE_RULE_STATUS_4_PATTERN = Pattern.compile("(?<=\"ruleId\":\"\\S{8}\",\"status\":)[^(,})]*");
    private static final Pattern RESPONSE_SITE_GUID_PATTERN = Pattern.compile("(?<=\"siteGUID\":\")[^\"]*");
    private static final Pattern RESPONSE_FAILED_RULE_COUNT_PATTERN = Pattern.compile("(?<=\"failedRuleCount\":)\\d{1,3}");
    private static final long MAX_STACK_SIZE = 100_000_000;

    private StringUtilities() {
    }

    public static double getDoubleValueOfIncome(String inputString) {
        try {
            return Double.parseDouble(replaceDollarsAndCommas(inputString));
            //@formatter:off
        } catch (NumberFormatException ignore) {
        } //NOSONAR
        //@formatter:on
        return 0.0D;
    }

    public static BigDecimal getBigDecimalValueOfIncome(String inputString) {
        try {
            return bigD(Double.parseDouble(replaceDollarsAndCommas(inputString)));
            //@formatter:off
        } catch (NumberFormatException ignore) {
        } //NOSONAR
        //@formatter:on
        return bigD(0);
    }

    public static String replaceDollarsAndCommas(String inputString) {
        if (inputString == null)
            return "0.0";
        String formattedString = inputString;
        formattedString = DOLLAR_PATTERN.matcher(formattedString).replaceAll("");
        formattedString = COMMA_PATTERN.matcher(formattedString).replaceAll("");
        return formattedString;
    }

    public static String createNameToken(String inputString) {
        String formattedString = inputString;
        formattedString = DOT_PATTERN.matcher(formattedString).replaceAll("");
        formattedString = COMMA_PATTERN.matcher(formattedString).replaceAll("");
        formattedString = SPACE_PATTERN.matcher(formattedString).replaceAll("");
        return formattedString;
    }

    public static String sterilizeIncomeResponse(String inputString) {
        String formattedString = inputString;
        formattedString = SPACE_PATTERN.matcher(formattedString).replaceAll("");
        formattedString = RESPONSE_ID_PATTERN.matcher(formattedString).replaceAll("1");
        formattedString = RESPONSE_REFID_PATTERN.matcher(formattedString).replaceAll("10");
        formattedString = RESPONSE_DOCUMENT_ID_PATTERN.matcher(formattedString).replaceAll("100");
        formattedString = RESPONSE_DOC_ID_PATTERN.matcher(formattedString).replaceAll("1000");
        formattedString = RESPONSE_APPLICANT_ID_PATTERN.matcher(formattedString).replaceAll("10000");
        formattedString = RESPONSE_CALCULATION_DATE_PATTERN.matcher(formattedString).replaceAll("100000");
        formattedString = RESPONSE_EXECUTION_DATE_PATTERN.matcher(formattedString).replaceAll("1000000");
        formattedString = RESPONSE_STATUS_MODIFICATION_PATTERN.matcher(formattedString).replaceAll("10000000");
        formattedString = RESPONSE_DOCUMENT_INT_ID_PATTERN.matcher(formattedString).replaceAll("docIntId");
        formattedString = RESPONSE_SITE_GUID_PATTERN.matcher(formattedString).replaceAll("GUID");
        final String[] tmp = {formattedString};
        Thread thread = new Thread(null, null, "TT", MAX_STACK_SIZE) {
            @Override
            public void run() throws StackOverflowError {
                tmp[0] = RESPONSE_DOCUMENT_DATA_PATTERN.matcher(tmp[0]).replaceFirst("\"noData\"");
            }

        };
        thread.start();
        try {
            thread.join();
            //@formatter:off
        } catch (InterruptedException ignore) {
        } //NOSONAR
        //@formatter:on
        formattedString = tmp[0];
        formattedString = RESPONSE_RULE_APPLICANT_ID_3_PATTERN.matcher(formattedString).replaceAll("");
        formattedString = RESPONSE_RULE_APPLICANT_ID_4_PATTERN.matcher(formattedString).replaceAll("");
        formattedString = RESPONSE_RULE_STATUS_3_PATTERN.matcher(formattedString).replaceAll("\"TUS\"");
        formattedString = RESPONSE_RULE_STATUS_4_PATTERN.matcher(formattedString).replaceAll("\"TUS\"");
        formattedString = RESPONSE_FAILED_RULE_COUNT_PATTERN.matcher(formattedString).replaceAll("0");
        return formattedString;
    }

    public static String filterOutNonDigits(String inputString) {
        String formattedString = inputString;
        formattedString = NON_DIGIT_PATTERN.matcher(formattedString).replaceAll("");
        return formattedString;
    }

    public static String trimStringAfterChar(String string, char character) {
        return (string.contains(String.valueOf(character)) ? string.substring(0, string.indexOf(character)) : string);
    }
}

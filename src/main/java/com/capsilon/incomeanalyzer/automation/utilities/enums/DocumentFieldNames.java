package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum DocumentFieldNames {

    //Universal
    FULL_NAME("FullName"),
    EMPLOYER_NAME("EmployerName"),

    //Paystub W-2
    TAXPAYER_IDENTIFIER_VALUE("TaxpayerIdentifierValue"),
    DOCUMENT_PERIOD_START_DATE("DocumentPeriodStartDate"),
    DOCUMENT_PERIOD_END_DATE("DocumentPeriodEndDate"),


    //W-2
    WAGES_AND_OTHER_COMPENSATION_AMOUNT("WagesAndOtherCompensationAmount"),
    EMPLOYMENT_INCOME_AMOUNT("EmploymentIncomeAmount"),
    ALLOCATED_TIPS("AllocatedTips"),
    TOTAL_TIPS_REPORTED_TO_EMPLOYER_AMOUNT("TotalTipsReportedToEmployerAmount"),
    MEDICARE_WAGES_AND_TIPS_AMOUNT("MedicareWagesAndTipsAmount"),

    //Voe Evoe
    EMPLOYEE_SSN("EmployeeSSN"),
    EMPLOYMENT_START_DATE("EmploymentStartDate"),
    YEAR_TO_DATE_INCOME_THROUGH_DATE("YearToDateIncomeThroughDate"),
    BASE_INCOME_AMOUNT("BaseIncomeAmount"),
    YEAR_TO_DATE_BASE_INCOME_AMOUNT("YearToDateBaseIncomeAmount"),
    YEAR_TO_DATE_COMMISSIONS_INCOME_AMOUNT("YearToDateCommissionsIncomeAmount"),
    YEAR_TO_DATE_OVERTIME_INCOME_AMOUNT("YearToDateOvertimeIncomeAmount"),
    YEAR_TO_DATE_BONUS_INCOME_AMOUNT("YearToDateBonusIncomeAmount"),
    YEAR_TO_DATE_TIPS_INCOME_AMOUNT("YearToDateTotalIncomeAmount"),
    YEAR_TO_DATE_TOTAL_INCOME_AMOUNT("YearToDateTotalIncomeAmount"),
    PRIOR_YEAR_BASE_INCOME_AMOUNT("PriorYearBaseIncomeAmount"),
    PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT("PriorYearCommissionsIncomeAmount"),
    PRIOR_YEAR_OVERTIME_INCOME_AMOUNT("PriorYearOvertimeIncomeAmount"),
    PRIOR_YEAR_BONUS_INCOME_AMOUNT("PriorYearBonusIncomeAmount"),
    PRIOR_YEAR_TIPS_INCOME_AMOUNT("PriorYearTotalIncomeAmount"),
    PRIOR_YEAR_TOTAL_INCOME_AMOUNT("PriorYearTotalIncomeAmount"),
    SECOND_PRIOR_YEAR_BASE_INCOME_AMOUNT("SecondPriorYearBaseIncomeAmount"),
    SECOND_PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT("SecondPriorYearCommissionsIncomeAmount"),
    SECOND_PRIOR_YEAR_OVERTIME_INCOME_AMOUNT("SecondPriorYearOvertimeIncomeAmount"),
    SECOND_PRIOR_YEAR_BONUS_INCOME_AMOUNT("SecondPriorYearBonusIncomeAmount"),
    SECOND_PRIOR_YEAR_TIPS_INCOME_AMOUNT("SecondPriorYearTotalIncomeAmount"),
    SECOND_PRIOR_YEAR_TOTAL_INCOME_AMOUNT("SecondPriorYearTotalIncomeAmount"),

    //VOE
    PAYMENT_PERIOD_TYPE("PaymentPeriodType"),
    EXECUTION_DATE("ExecutionDate"),
    AVG_HOURS_PER_WEEK("AvgHoursPerWeek"),

    //VOE previous
    EMPLOYMENT_PREVIOUS_START_DATE("EmploymentPreviousStartDate"),
    EMPLOYMENT_PREVIOUS_END_DATE("EmploymentPreviousEndDate"),
    EMPLOYMENT_PREVIOUS_BASE_INCOME_AMOUNT("EmploymentPreviousBaseIncomeAmount"),
    EMPLOYMENT_PREVIOUS_OVERTIME_INCOME_AMOUNT("EmploymentPreviousOvertimeIncomeAmount"),
    EMPLOYMENT_PREVIOUS_COMMISSIONS_INCOME_AMOUNT("EmploymentPreviousCommissionsIncomeAmount"),
    EMPLOYMENT_PREVIOUS_BONUS_INCOME_AMOUNT("EmploymentPreviousBonusIncomeAmount"),
    EMPLOYMENT_PREVIOUS_PAYMENT_PERIOD_TYPE("EmploymentPreviousPaymentPeriodType"),

    //EVOE
    EMPLOYMENT_END_DATE("EmploymentEndDate"),
    VERIFICATION_DATE("VerificationDate"),
    INCOME_PAY_RATE_FREQUENCY_TYPE("EmploymentIncomePayRateFrequencyType"),
    INCOME_FREQUENCY_TYPE("EmploymentIncomeFrequencyType"),
    INCOME_PAY_RATE_AMOUNT("EmploymentIncomePayRateAmount"),
    INCOME_AVG_HOURS_PER_PERIOD("EmploymentIncomeAverageHoursPerPeriodNumber"),

    //Paystub
    INCOME_ITEM_PERIOD_HOURS("IncomeItemPeriodHours"),
    INCOME_ITEM_PERIOD_RATE("IncomeItemPeriodRate"),
    INCOME_ITEM_OVERTIME_AMOUNT("IncomeItemOvertimeAmount"),
    INCOME_ITEM_OVERTIME_YEAR_TO_DATE_AMOUNT("IncomeItemOvertimeYearToDateAmount"),
    INCOME_ITEM_OVERTIME_PERIOD_RATE("IncomeItemOvertimePeriodRate"),
    INCOME_ITEM_COMMISSIONS_AMOUNT("IncomeItemCommissionsAmount"),
    INCOME_ITEM_COMMISSIONS_YEAR_TO_DATE_AMOUNT("IncomeItemCommissionsYearToDateAmount"),
    INCOME_ITEM_BONUS_AMOUNT("IncomeItemBonusAmount"),
    INCOME_ITEM_BONUS_YEAR_TO_DATE_AMOUNT("IncomeItemBonusYearToDateAmount"),
    INCOME_ITEM_BONUS_PERIOD_HOURS("IncomeItemBonusPeriodHours"),
    INCOME_ITEM_BONUS_PERIOD_RATE("IncomeItemBonusPeriodRate"),
    INCOME_ITEM_TIPS_AMOUNT("IncomeItemTipsAmount"),
    INCOME_ITEM_TIPS_YEAR_TO_DATE_AMOUNT("IncomeItemTipsYearToDateAmount"),
    INCOME_ITEM_AMOUNT("IncomeItemAmount"),
    INCOME_ITEM_YEAR_TO_DATE_AMOUNT("IncomeItemYearToDateAmount"),
    INCOME_ITEM_MANUAL_FREQUENCY("ManualFrequency"),
    INCOME_ITEM_EXPLICIT_FREQUENCY("ExplicitFrequency"),
    INCOME_ITEM_PAY_TYPE("PayType"),
    INCOME_ITEM_YTD_GROSS_INCOME_AMOUNT("YearToDateGrossIncomeAmount"),
    DOCUMENT_PAYCHECK_DATE("PaycheckDate")
    ;

    public final String value;

    DocumentFieldNames(String value) {
        this.value = value;
    }
}

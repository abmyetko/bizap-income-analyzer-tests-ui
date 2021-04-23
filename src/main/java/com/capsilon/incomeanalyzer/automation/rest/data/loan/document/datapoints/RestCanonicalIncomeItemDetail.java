package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalIncomeItemDetail {

    private String classifier;
    private String id;
    private String incomeType;
    private String incomeStatusType;
    private String incomeFrequencyType;
    private BigDecimal incomeTypeTotalAmount;
    private Boolean incomeTypeLikelyToContinueIndicator;

    private String IncomeTypeOtherDescription;
    private String incomeFrequencyTypeOtherDescription;
    private BigDecimal incomeTypeHourlyPayRatePercent;
    private BigDecimal incomeTypePeriodHoursNumber;
    private BigDecimal incomePaidAverageHoursPerWeekNumber;
    private BigDecimal incomeTypeYearToDateAmount;
    private String employmentYearToDateIncomeThroughDate;

    public RestCanonicalIncomeItemDetail setIncomeTypeTotalAmount(BigDecimal incomeTypeTotalAmount) {
        this.incomeTypeTotalAmount = setBigDecimalValue(incomeTypeTotalAmount);
        return this;
    }

    public RestCanonicalIncomeItemDetail setIncomeTypeYearToDateAmount(BigDecimal incomeTypeYearToDateAmount) {
        this.incomeTypeYearToDateAmount = setBigDecimalValue(incomeTypeYearToDateAmount);
        return this;
    }

    public RestCanonicalIncomeItemDetail setIncomePaidAverageHoursPerWeekNumber(BigDecimal incomePaidAverageHoursPerWeekNumber) {
        this.incomePaidAverageHoursPerWeekNumber = setBigDecimalValue(incomePaidAverageHoursPerWeekNumber);
        return this;
    }

    public RestCanonicalIncomeItemDetail setIncomeTypeHourlyPayRatePercent(BigDecimal incomeTypeHourlyPayRatePercent) {
        this.incomeTypeHourlyPayRatePercent = setBigDecimalValue(incomeTypeHourlyPayRatePercent);
        return this;
    }

    public RestCanonicalIncomeItemDetail setIncomeTypePeriodHoursNumber(BigDecimal incomeTypePeriodHoursNumber) {
        this.incomeTypePeriodHoursNumber = setBigDecimalValue(incomeTypePeriodHoursNumber);
        return this;
    }
}

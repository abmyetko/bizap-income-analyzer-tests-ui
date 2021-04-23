package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalEmployerEmployment {

    private String id;
    private BigDecimal employmentIncomeAmount;
    private BigDecimal employmentIncomeNetAmount;
    private String employmentIncomeExplicitFrequencyType;
    private String employmentIncomeFrequencyType;

    private String employmentStartDate;
    private String employmentEndDate;
    private String employmentRemarkDescription;
    private String employmentPositionDescription;
    private String employmentContinuesProbabilityDescription;
    private String employmentStatusType;

    private String employmentClassificationType;
    private String employmentApplicantSelfEmployedIndicator;
    private String employmentMonthsOnJobCount;
    private String employmentTimeInLineOfWorkYearsCount;
    private String employmentYearsOnJobCount;

    private String payType;

    public void setEmploymentIncomeAmount(BigDecimal employmentIncomeAmount) {
        this.employmentIncomeAmount = setBigDecimalValue(employmentIncomeAmount);
    }

    public void setEmploymentIncomeNetAmount(BigDecimal employmentIncomeNetAmount) {
        this.employmentIncomeNetAmount = setBigDecimalValue(employmentIncomeNetAmount);
    }
}

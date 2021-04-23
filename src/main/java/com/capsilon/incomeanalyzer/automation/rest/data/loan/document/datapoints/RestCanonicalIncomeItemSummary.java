package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalIncomeItemSummary {

    private String id;
    private BigDecimal applicantTotalYearToDateIncomeAmount;
    private BigDecimal applicantTotalYearToDateIncomeNetAmount;
    private BigDecimal applicantPreviousYearTotalIncomeAmount;

    public void setApplicantTotalYearToDateIncomeAmount(BigDecimal applicantTotalYearToDateIncomeAmount) {
        this.applicantTotalYearToDateIncomeAmount = setBigDecimalValue(applicantTotalYearToDateIncomeAmount);
    }

    public void setApplicantTotalYearToDateIncomeNetAmount(BigDecimal applicantTotalYearToDateIncomeNetAmount) {
        this.applicantTotalYearToDateIncomeNetAmount = setBigDecimalValue(applicantTotalYearToDateIncomeNetAmount);
    }

    public void setApplicantPreviousYearTotalIncomeAmount(BigDecimal applicantPreviousYearTotalIncomeAmount) {
        this.applicantPreviousYearTotalIncomeAmount = setBigDecimalValue(applicantPreviousYearTotalIncomeAmount);
    }
}

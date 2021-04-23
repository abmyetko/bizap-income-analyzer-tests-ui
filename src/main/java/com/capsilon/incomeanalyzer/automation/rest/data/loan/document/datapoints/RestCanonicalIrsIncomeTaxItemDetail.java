package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalIrsIncomeTaxItemDetail {

    private String id;
    private String incomeTaxType;
    private BigDecimal incomeTaxAmount;
    private BigDecimal incomeTaxYearToDateAmount;

    public void setIncomeTaxAmount(BigDecimal incomeTaxAmount) {
        this.incomeTaxAmount = setBigDecimalValue(incomeTaxAmount);
    }

    public void setIncomeTaxYearToDateAmount(BigDecimal incomeTaxYearToDateAmount) {
        this.incomeTaxYearToDateAmount = setBigDecimalValue(incomeTaxYearToDateAmount);
    }
}

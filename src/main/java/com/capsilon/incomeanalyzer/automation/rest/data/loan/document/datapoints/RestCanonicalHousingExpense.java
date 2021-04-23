package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalHousingExpense {

    private String id;
    private String housingExpenseTimingType;
    private String housingExpenseType;
    private BigDecimal housingExpensePaymentAmount;

    public void setHousingExpensePaymentAmount(BigDecimal housingExpensePaymentAmount) {
        this.housingExpensePaymentAmount = setBigDecimalValue(housingExpensePaymentAmount);
    }
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadExpense {

    private String id;
    private String expenseType;
    private BigDecimal expenseMonthlyPaymentAmount;
    private String alimonyOwedToName;
    private Integer expenseRemainingTermMonthsCount;

    public void setExpenseMonthlyPaymentAmount(BigDecimal expenseMonthlyPaymentAmount) {
        this.expenseMonthlyPaymentAmount = setBigDecimalValue(expenseMonthlyPaymentAmount);
    }
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadLoanDetail {

    private String id;
    private BigDecimal totalSubordinateFinancingAmount;

    public void setTotalSubordinateFinancingAmount(BigDecimal totalSubordinateFinancingAmount) {
        this.totalSubordinateFinancingAmount = setBigDecimalValue(totalSubordinateFinancingAmount);
    }
}

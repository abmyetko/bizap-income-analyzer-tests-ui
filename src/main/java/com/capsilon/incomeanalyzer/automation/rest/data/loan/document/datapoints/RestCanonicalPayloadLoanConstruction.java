package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadLoanConstruction {

    private String id;
    private BigDecimal landAppraisedValueAmount;

    public void setLandAppraisedValueAmount(BigDecimal landAppraisedValueAmount) {
        this.landAppraisedValueAmount = setBigDecimalValue(landAppraisedValueAmount);
    }
}

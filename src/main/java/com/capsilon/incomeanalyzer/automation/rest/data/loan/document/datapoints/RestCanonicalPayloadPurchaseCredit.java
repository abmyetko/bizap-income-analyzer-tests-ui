package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadPurchaseCredit {

    private String id;
    private String purchaseCreditType;
    private BigDecimal purchaseCreditAmount;

    public void setPurchaseCreditAmount(BigDecimal purchaseCreditAmount) {
        this.purchaseCreditAmount = setBigDecimalValue(purchaseCreditAmount);
    }
}

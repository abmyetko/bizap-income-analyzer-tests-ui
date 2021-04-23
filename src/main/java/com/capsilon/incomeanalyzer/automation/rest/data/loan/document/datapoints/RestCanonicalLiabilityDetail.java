package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalLiabilityDetail {

    private String id;
    private String liabilityType;
    private Integer liabilityRemainingTermMonthsCount;
    private BigDecimal liabilityUnpaidBalanceAmount;
    private BigDecimal liabilityMonthlyPaymentAmount;
    private Boolean subjectLoanResubordinationIndicator;
    private Boolean liabilityExclusionIndicator;
    private Boolean liabilityPayoffStatusIndicator;
    private Boolean liabilitySecuredBySubjectPropertyIndicator;
    private String liabilityAccountIdentifier;

    public void setLiabilityUnpaidBalanceAmount(BigDecimal liabilityUnpaidBalanceAmount) {
        this.liabilityUnpaidBalanceAmount = setBigDecimalValue(liabilityUnpaidBalanceAmount);
    }

    public void setLiabilityMonthlyPaymentAmount(BigDecimal liabilityMonthlyPaymentAmount) {
        this.liabilityMonthlyPaymentAmount = setBigDecimalValue(liabilityMonthlyPaymentAmount);
    }
}

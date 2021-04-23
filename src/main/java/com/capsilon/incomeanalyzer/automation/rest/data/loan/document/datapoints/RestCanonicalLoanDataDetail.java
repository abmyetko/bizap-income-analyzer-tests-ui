package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalLoanDataDetail {

    private String id;
    private BigDecimal prepaidItemsEstimatedAmount;
    private BigDecimal sellerPaidClosingCostsAmount;
    private BigDecimal mIAndFundingFeeTotalAmount;
    private BigDecimal applicantPaidDiscountPointsTotalAmount;
    private BigDecimal refinanceIncludingDebtsToBePaidOffAmount;
    private BigDecimal alterationsImprovementsAndRepairsAmount;
    private BigDecimal estimatedClosingCostsAmount;

    public void setPrepaidItemsEstimatedAmount(BigDecimal prepaidItemsEstimatedAmount) {
        this.prepaidItemsEstimatedAmount = setBigDecimalValue(prepaidItemsEstimatedAmount);
    }

    public void setSellerPaidClosingCostsAmount(BigDecimal sellerPaidClosingCostsAmount) {
        this.sellerPaidClosingCostsAmount = setBigDecimalValue(sellerPaidClosingCostsAmount);
    }

    public void setmIAndFundingFeeTotalAmount(BigDecimal mIAndFundingFeeTotalAmount) {
        this.mIAndFundingFeeTotalAmount = setBigDecimalValue(mIAndFundingFeeTotalAmount);
    }

    public void setApplicantPaidDiscountPointsTotalAmount(BigDecimal applicantPaidDiscountPointsTotalAmount) {
        this.applicantPaidDiscountPointsTotalAmount = setBigDecimalValue(applicantPaidDiscountPointsTotalAmount);
    }

    public void setRefinanceIncludingDebtsToBePaidOffAmount(BigDecimal refinanceIncludingDebtsToBePaidOffAmount) {
        this.refinanceIncludingDebtsToBePaidOffAmount = setBigDecimalValue(refinanceIncludingDebtsToBePaidOffAmount);
    }

    public void setAlterationsImprovementsAndRepairsAmount(BigDecimal alterationsImprovementsAndRepairsAmount) {
        this.alterationsImprovementsAndRepairsAmount = setBigDecimalValue(alterationsImprovementsAndRepairsAmount);
    }

    public void setEstimatedClosingCostsAmount(BigDecimal estimatedClosingCostsAmount) {
        this.estimatedClosingCostsAmount = setBigDecimalValue(estimatedClosingCostsAmount);
    }
}

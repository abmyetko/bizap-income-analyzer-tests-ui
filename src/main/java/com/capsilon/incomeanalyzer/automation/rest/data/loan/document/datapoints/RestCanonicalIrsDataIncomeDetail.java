package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalIrsDataIncomeDetail {

    private String id;
    private BigDecimal wagesSalariesTipsEtcAmount;
    private BigDecimal allocatedTipsAmount;
    private BigDecimal medicareWagesAndTipsAmount;
    private BigDecimal socialSecurityBenefitsAmount;
    private BigDecimal totalTipsReportedToEmployerAmount;

    public RestCanonicalIrsDataIncomeDetail setWagesSalariesTipsEtcAmount(BigDecimal wagesSalariesTipsEtcAmount) {
        this.wagesSalariesTipsEtcAmount = setBigDecimalValue(wagesSalariesTipsEtcAmount);
        return this;
    }

    public RestCanonicalIrsDataIncomeDetail setAllocatedTipsAmount(BigDecimal allocatedTipsAmount) {
        this.allocatedTipsAmount = setBigDecimalValue(allocatedTipsAmount);
        return this;
    }

    public RestCanonicalIrsDataIncomeDetail setMedicareWagesAndTipsAmount(BigDecimal medicareWagesAndTipsAmount) {
        this.medicareWagesAndTipsAmount = setBigDecimalValue(medicareWagesAndTipsAmount);
        return this;
    }

    public RestCanonicalIrsDataIncomeDetail setSocialSecurityBenefitsAmount(BigDecimal socialSecurityBenefitsAmount) {
        this.socialSecurityBenefitsAmount = setBigDecimalValue(socialSecurityBenefitsAmount);
        return this;
    }

    public RestCanonicalIrsDataIncomeDetail setTotalTipsReportedToEmployerAmount(BigDecimal totalTipsReportedToEmployerAmount) {
        this.totalTipsReportedToEmployerAmount = setBigDecimalValue(totalTipsReportedToEmployerAmount);
        return this;
    }
}

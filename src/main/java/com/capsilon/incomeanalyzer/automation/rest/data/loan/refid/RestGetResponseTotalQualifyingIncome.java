package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestGetResponseTotalQualifyingIncome {

    private Long currentYear;
    private BigDecimal qualifyingIncome;
    private String type;

    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public List<FailureComparison> compareTo(RestGetResponseTotalQualifyingIncome anotherTotalQualifyingIncome) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison("totalQualifyingIncome.type", this.getType(), anotherTotalQualifyingIncome.getType()));
        failList.add(new FailureComparison("totalQualifyingIncome.qualifyingIncome", this.getQualifyingIncome(), anotherTotalQualifyingIncome.getQualifyingIncome()));
        failList.add(new FailureComparison("totalQualifyingIncome.currentYear", this.getCurrentYear(), anotherTotalQualifyingIncome.getCurrentYear()));
        return failList;
    }
}

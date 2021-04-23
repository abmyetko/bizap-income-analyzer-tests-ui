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
public class RestAvgIncomes {

    private BigDecimal avgMonthlyIncome;
    private String avgType;
    private BigDecimal gross;
    private String id;
    private BigDecimal months;
    private Boolean selected;

    public void setGross(BigDecimal gross) {
        this.gross = setBigDecimalValue(gross);
    }

    public void setAvgMonthlyIncome(BigDecimal avgMonthlyIncome) {
        this.avgMonthlyIncome = setBigDecimalValue(avgMonthlyIncome);
    }

    public void setMonths(BigDecimal months) {
        this.months = setBigDecimalValue(months);
    }

    public Boolean getSelected() {
        return selected == Boolean.TRUE;
    }

    public List<FailureComparison> compareTo(RestAvgIncomes anotherAvgIncomes) {
        return compareTo(anotherAvgIncomes, "", "");
    }

    public List<FailureComparison> compareTo(RestAvgIncomes anotherAvgIncomes, String applicantName, String incomeType) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).avgIncomes.avgType", applicantName, incomeType),
                this.getAvgType(), anotherAvgIncomes.getAvgType()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).avgIncomes.gross", applicantName, incomeType),
                this.getGross(), anotherAvgIncomes.getGross()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).avgIncomes.months", applicantName, incomeType),
                this.getMonths(), anotherAvgIncomes.getMonths()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).avgIncomes.avgMonthlyIncome", applicantName, incomeType),
                this.getAvgMonthlyIncome(), anotherAvgIncomes.getAvgMonthlyIncome()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).avgIncomes.selected", applicantName, incomeType),
                this.getSelected(), anotherAvgIncomes.getSelected()));
        return failList;
    }
}

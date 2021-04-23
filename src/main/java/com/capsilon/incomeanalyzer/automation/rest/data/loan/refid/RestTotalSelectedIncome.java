package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.bigD;

@Data
@Accessors(chain = true)
public class RestTotalSelectedIncome {

    private BigDecimal avgMonthlyIncome;
    private BigDecimal gross;
    private BigDecimal months;
    private BigDecimal trending = new BigDecimal(0);
    private Integer year;

    public void setGross(BigDecimal gross) {
        this.gross = setBigDecimalValue(gross);
    }

    public void setAvgMonthlyIncome(BigDecimal avgMonthlyIncome) {
        this.avgMonthlyIncome = setBigDecimalValue(avgMonthlyIncome);
    }

    public void setMonths(BigDecimal months) {
        this.months = setBigDecimalValue(months);
    }

    public void setTrending(BigDecimal trending) {
        this.trending = setBigDecimalValue(trending);
    }

    public List<FailureComparison> compareTo(RestTotalSelectedIncome anotherTotalSelectedIncome) {
        return compareTo(anotherTotalSelectedIncome, "", "");
    }

    public List<FailureComparison> compareTo(RestTotalSelectedIncome anotherTotalSelectedIncome, String applicantName, String incomeType) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).totalSelectedIncome.gross", applicantName, incomeType),
                this.getGross(), anotherTotalSelectedIncome.getGross()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).totalSelectedIncome.months", applicantName, incomeType),
                this.getMonths(), anotherTotalSelectedIncome.getMonths()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).totalSelectedIncome.trending", applicantName, incomeType),
                bigD(this.getTrending()), bigD(anotherTotalSelectedIncome.getTrending())));
        failList.add(new FailureComparison(String.format("applicants(%s).incomeCategories.W2.incomeType(%s).totalSelectedIncome.avgMonthlyIncome", applicantName, incomeType),
                this.getAvgMonthlyIncome(), anotherTotalSelectedIncome.getAvgMonthlyIncome()));
        return failList;
    }
}

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
public class RestIncomeMonths {

    private BigDecimal value;
    private List<RestSnippet> snippets = new ArrayList<>();
    private String from;
    private String to;

    public void setValue(BigDecimal value) {
        this.value = setBigDecimalValue(value);
    }

    public List<FailureComparison> compareTo(RestIncomeMonths anotherAnnualSummaryIncomeMonths) {
        return compareTo(anotherAnnualSummaryIncomeMonths, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestIncomeMonths anotherAnnualSummaryIncomeMonths, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.months.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryIncomeMonths.getValue()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.months.from", applicantName, employerName, incomeType, year),
                this.getFrom(), anotherAnnualSummaryIncomeMonths.getFrom()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.months.to", applicantName, employerName, incomeType, year),
                this.getTo(), anotherAnnualSummaryIncomeMonths.getTo()));
        return failList;
    }
}

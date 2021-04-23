package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestSummaryMonthlyAmount {

    private String id;
    private Boolean selected;
    private List<RestSnippet> snippets = new ArrayList<>();
    private String type;
    private BigDecimal value;
    private String documentId;
    private List<String> footnotesIdx = new ArrayList<>();
    private Map<Integer, String> footnotes;

    public void setValue(BigDecimal value) {
        this.value = setBigDecimalValue(value);
    }

    public List<FailureComparison> compareTo(RestSummaryMonthlyAmount anotherAnnualSummaryMonthlyAmount) {
        return compareTo(anotherAnnualSummaryMonthlyAmount, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestSummaryMonthlyAmount anotherAnnualSummaryMonthlyAmount, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.monthlyAmount.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryMonthlyAmount.getValue()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.monthlyAmount.type", applicantName, employerName, incomeType, year),
                this.getType(), anotherAnnualSummaryMonthlyAmount.getType()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.monthlyAmount.selected", applicantName, employerName, incomeType, year),
                this.getSelected(), anotherAnnualSummaryMonthlyAmount.getSelected()));
        return failList;
    }
}

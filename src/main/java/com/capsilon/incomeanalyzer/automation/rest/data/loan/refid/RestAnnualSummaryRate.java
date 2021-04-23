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
public class RestAnnualSummaryRate {

    private List<RestSnippet> snippets = new ArrayList<>();
    private BigDecimal value;
    private String valueType;
    private String documentId;
    private List<String> footnotesIdx = new ArrayList<>();

    public void setValue(BigDecimal value) {
        this.value = setBigDecimalValue(value);
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryRate anotherAnnualSummaryRate) {
        return compareTo(anotherAnnualSummaryRate, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryRate anotherAnnualSummaryRate, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.rate.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryRate.getValue()));
        return failList;
    }
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestAnnualSummaryPayPeriodDates {

    private List<RestSnippet> snippets = new ArrayList<>();
    private String value;
    private ValueType valueType;
    private String documentId;
    private List<String> footnotesIdx = new ArrayList<>();

    public List<FailureComparison> compareTo(RestAnnualSummaryPayPeriodDates anotherAnnualSummaryPayPeriodDates) {
        return compareTo(anotherAnnualSummaryPayPeriodDates, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryPayPeriodDates anotherAnnualSummaryPayPeriodDates, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.payPeriodDates.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryPayPeriodDates.getValue()));
        return failList;
    }
}

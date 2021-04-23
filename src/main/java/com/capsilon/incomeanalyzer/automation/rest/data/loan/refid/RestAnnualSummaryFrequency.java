package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestAnnualSummaryFrequency {

    private List<RestSnippet> snippets = new ArrayList<>();
    private IncomeFrequency value;
    private ValueType valueType;
    private String documentId;
    private List<String> footnotesIdx = new ArrayList<>();

    public List<FailureComparison> compareTo(RestAnnualSummaryFrequency anotherAnnualSummaryFrequency) {
        return compareTo(anotherAnnualSummaryFrequency, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryFrequency anotherAnnualSummaryFrequency, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.frequency.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryFrequency.getValue()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.frequency.valueType", applicantName, employerName, incomeType, year),
                this.getValueType(), anotherAnnualSummaryFrequency.getValueType()));
        return failList;
    }
}

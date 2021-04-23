package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestAnnualSummaryType {

    private List<RestSnippet> snippets = new ArrayList<>();
    private IncomeType value;
    private String valueType;
    private String documentId;
    private List<String> footnotesIdx = new ArrayList<>();

    public List<FailureComparison> compareTo(RestAnnualSummaryType anotherAnnualSummaryType) {
        return compareTo(anotherAnnualSummaryType, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryType anotherAnnualSummaryType, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.type.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryType.getValue()));
        return failList;
    }
}

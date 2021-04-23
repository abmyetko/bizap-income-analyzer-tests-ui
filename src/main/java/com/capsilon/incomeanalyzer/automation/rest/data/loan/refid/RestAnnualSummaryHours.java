package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestAnnualSummaryHours {

    private Boolean cappedHours;
    private BigDecimal originalValue;
    private List<RestSnippet> snippets = new ArrayList<>();
    private BigDecimal value;
    private ValueType valueType;
    private String documentId;
    private List<String> footnotesIdx = new ArrayList<>();

    public void setOriginalValue(BigDecimal originalValue) {
        this.originalValue = setBigDecimalValue(originalValue);
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryHours anotherAnnualSummaryHours) {
        return compareTo(anotherAnnualSummaryHours, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryHours anotherAnnualSummaryHours, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.hours.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryHours.getValue()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.hours.cappedHours", applicantName, employerName, incomeType, year),
                this.getCappedHours(), anotherAnnualSummaryHours.getCappedHours()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.hours.originalValue", applicantName, employerName, incomeType, year),
                this.getOriginalValue(), anotherAnnualSummaryHours.getOriginalValue()));
        return failList;
    }
}

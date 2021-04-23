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
public class RestAnnualSummaryDisplayGrossPay {

    private List<RestSnippet> snippets = new ArrayList<>();
    private BigDecimal value;
    private ValueType valueType;
    private BigDecimal calculatedValue;
    private String documentId;
    private List<String> footnotesIdx = new ArrayList<>();

    public void setValue(BigDecimal value) {
        this.value = setBigDecimalValue(value);
    }

    public void setCalculatedValue(BigDecimal calculatedValue) {
        this.calculatedValue = setBigDecimalValue(calculatedValue);
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryDisplayGrossPay anotherAnnualSummaryDisplayGrossPay) {
        return compareTo(anotherAnnualSummaryDisplayGrossPay, "", "", "", "");
    }

    public List<FailureComparison> compareTo(RestAnnualSummaryDisplayGrossPay anotherAnnualSummaryDisplayGrossPay, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.displayGrossPay.value", applicantName, employerName, incomeType, year),
                this.getValue(), anotherAnnualSummaryDisplayGrossPay.getValue()));
        return failList;
    }
}

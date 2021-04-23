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
public class RestIncomePart {

    private String id;
    private List<RestPartIncome> incomes = new ArrayList<>();
    private Boolean qualified;
    private BigDecimal qualifyingIncome;
    private Boolean selected;
    private List<RestPartTooltip> tooltips = new ArrayList<>();
    private String type;
    private Map<Integer, String> footnotes;

    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public RestPartIncome getIncome(String year) {
        return getIncome("YEAR", Integer.valueOf(year));
    }

    public RestPartIncome getIncome(Integer year) {
        return incomes.stream().filter(income -> year.equals(income.getYear())).findFirst().orElse(null);
    }

    public RestPartIncome getIncome(String variant, Integer year) {
        for (RestPartIncome income : incomes) {
            if (variant.equalsIgnoreCase(income.getVariant())) {
                if (year == null)
                    return income;
                if (year.equals(income.getYear()))
                    return income;
            }
        }
        return null;
    }

    public List<FailureComparison> compareTo(RestIncomePart anotherIncomePart) {
        return compareTo(anotherIncomePart, "", "");
    }

    public List<FailureComparison> compareTo(RestIncomePart anotherIncomePart, String applicantName, String employerName) {
        List<FailureComparison> failList = new ArrayList<>();

        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).qualified", applicantName, employerName, this.getType()),
                this.getQualified(), anotherIncomePart.getQualified()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).selected", applicantName, employerName, this.getType()),
                this.getSelected(), anotherIncomePart.getSelected()));
        for (RestPartIncome thisPartIncome : this.getIncomes())
            failList.addAll(thisPartIncome.compareTo(anotherIncomePart.getIncome(thisPartIncome.getVariant(), thisPartIncome.getYear()), applicantName, employerName, this.getType()));

        return failList;
    }
}

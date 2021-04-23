package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestIncomeType {

    private List<RestAvgIncomes> avgIncomes = new ArrayList<>();
    private String id;
    private String incomeType;
    private String paymentType;
    private Boolean qualified;
    private BigDecimal qualifyingIncome;
    private Boolean selected;
    private List<RestTotalSelectedIncome> totalSelectedIncome = new ArrayList<>();

    public Boolean getSelected() {
        return selected == Boolean.TRUE;
    }

    public RestTotalSelectedIncome getTotalSelectedIncome(Integer year) {
        return totalSelectedIncome.stream().filter(income -> income.getYear().equals(year)).findFirst().orElse(null);
    }

    public RestAvgIncomes getAvgIncome(IncomeAvg id) {
        return avgIncomes.stream().filter(income -> id == IncomeAvg.valueOfLabel(income.getId())).findFirst().orElse(null);
    }

    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public List<FailureComparison> compareTo(RestIncomeType anotherIncomePartType) {
        return compareTo(anotherIncomePartType, "");
    }

    public List<FailureComparison> compareTo(RestIncomeType anotherIncomePartType, String applicantName) {
        List<FailureComparison> failList = new ArrayList<>();

        for (RestTotalSelectedIncome thisTotalSelectedIncome : this.getTotalSelectedIncome()) {
            RestTotalSelectedIncome anotherTotalSelectedIncome = anotherIncomePartType.getTotalSelectedIncome(thisTotalSelectedIncome.getYear());
            failList.addAll(thisTotalSelectedIncome.compareTo(anotherTotalSelectedIncome, applicantName, this.getIncomeType()));
        }
        for (RestAvgIncomes thisAvgIncomes : this.getAvgIncomes()) {
            RestAvgIncomes anotherAvgIncomes = anotherIncomePartType.getAvgIncome(IncomeAvg.valueOfLabel(thisAvgIncomes.getId()));
            failList.addAll(thisAvgIncomes.compareTo(anotherAvgIncomes, applicantName, this.getIncomeType()));
        }

        return failList;
    }
}

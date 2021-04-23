package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType.*;

@Data
@Accessors(chain = true)
public class RestIncomeGroup {

    private String classificationType;
    private String id;
    private Map<String, RestIncomeType> incomeTypes;
    private long[] incomeIds;
    private Boolean selected;
    private Boolean qualified;
    private BigDecimal qualifyingIncome;
    private Long ytd;


    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public RestIncomeType getIncomeType(IncomePartType incomePartType) {
        return incomeTypes.get(incomePartType.toString());
    }

    public RestIncomeType getIncomeTypeBasePay() {
        return incomeTypes.get(BASE_PAY.toString());
    }

    public RestIncomeType getIncomeTypeOvertime() {
        return incomeTypes.get(OVERTIME.toString());
    }

    public RestIncomeType getIncomeTypeCommissions() {
        return incomeTypes.get(COMMISSIONS.toString());
    }

    public RestIncomeType getIncomeTypeBonus() {
        return incomeTypes.get(BONUS.toString());
    }

    public List<FailureComparison> compareTo(RestIncomeGroup anotherIncomeGroup) {
        return compareTo(anotherIncomeGroup, "");
    }

    public List<FailureComparison> compareTo(RestIncomeGroup anotherIncomeGroup, String applicantName) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicant(%s).incomeCategories.W2.incomeGroup(%s).selected", applicantName, id),
                this.getSelected(), anotherIncomeGroup.getSelected()));
        failList.add(new FailureComparison(String.format("applicant(%s).incomeCategories.W2.incomeGroup(%s).qualified", applicantName, id),
                this.getQualified(), anotherIncomeGroup.getQualified()));
        failList.add(new FailureComparison(String.format("applicant(%s).incomeCategories.W2.incomeGroup(%s).qualifyingIncome", applicantName, id),
                this.getQualifyingIncome(), anotherIncomeGroup.getQualifyingIncome()));

        this.getIncomeTypes().forEach((name, thisIncomePartType) -> {
            RestIncomeType anotherIncomePartType = anotherIncomeGroup.getIncomeType(IncomePartType.valueOf(name));
            failList.addAll(thisIncomePartType.compareTo(anotherIncomePartType, applicantName));
        });
        return failList;
    }
}

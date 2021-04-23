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
public class RestIncomeEmployer {

    private String address;
    private String classification;
    private RestEmployerEmploymentPeriod employmentPeriod;
    private Long id;
    private BigDecimal incomeAmount;
    private BigDecimal monthsOnJob;
    private Boolean current;
    private String name;
    private List<String> nameAliases = new ArrayList<>();
    private String status;

    public void setIncomeAmount(BigDecimal incomeAmount) {
        this.incomeAmount = setBigDecimalValue(incomeAmount);
    }

    public void setMonthsOnJob(BigDecimal monthsOnJob) {
        this.monthsOnJob = setBigDecimalValue(monthsOnJob);
    }

    public List<FailureComparison> compareTo(RestIncomeEmployer anotherIncomeEmployer) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison("applicants.incomes.employer.status", this.getStatus(), anotherIncomeEmployer.getStatus()));
        failList.add(new FailureComparison("applicants.incomes.employer.classification", this.getClassification(), anotherIncomeEmployer.getClassification()));
        failList.add(new FailureComparison("applicants.incomes.employer.monthsOnJob", this.getMonthsOnJob(), anotherIncomeEmployer.getMonthsOnJob()));
        failList.addAll(this.getEmploymentPeriod().compareTo(anotherIncomeEmployer.getEmploymentPeriod()));
        return failList;
    }
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestGetResponseApplicant {

    private List<String> associatedRefIds = new ArrayList<>();
    private String filingType;
    private String firstName;
    private Long id;
    private Map<String, RestApplicantIncomeCategory> incomeCategories;
    private List<RestApplicantIncome> incomes = new ArrayList<>();
    private String lastName;
    private String middleName;
    private BigDecimal monthsOnCurrentJob;
    private Boolean primaryInPair;
    private Map<IncomePartType, BigDecimal> declaredIncome;
    private BigDecimal statedBaseIncome;
    private BigDecimal statedOvertimeIncome;
    private BigDecimal statedCommissionsIncome;
    private BigDecimal statedBonusIncome;
    private Boolean touchless;
    private Boolean qualified;
    private BigDecimal qualifyingIncome;
    private String refId;
    private Boolean selected;
    private String type;
    private Boolean incomeVerified;

    public Boolean isQualified() {
        return qualified == Boolean.TRUE;
    }

    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public void setMonthsOnCurrentJob(BigDecimal monthsOnCurrentJob) {
        this.monthsOnCurrentJob = setBigDecimalValue(monthsOnCurrentJob);
    }

    public void setStatedOvertimeIncome(BigDecimal statedOvertimeIncome) {
        this.statedOvertimeIncome = setBigDecimalValue(statedOvertimeIncome);
    }

    public void setStatedCommissionsIncome(BigDecimal statedCommissionsIncome) {
        this.statedCommissionsIncome = setBigDecimalValue(statedCommissionsIncome);
    }

    public void setStatedBaseIncome(BigDecimal statedBaseIncome) {
        this.statedBaseIncome = setBigDecimalValue(statedBaseIncome);
    }

    public RestApplicantIncomeCategory getIncomeCategoryW2() {
        return incomeCategories.get("W2");
    }

    public RestApplicantIncome getIncome(String employer) {
        return getIncome("W2", employer);
    }

    public RestApplicantIncome getIncome(String category, String employer) {
        for (RestApplicantIncome income : incomes) {
            if (category.equalsIgnoreCase(income.getCategory()) && employer.equalsIgnoreCase(income.getEmployer().getName()))
                return income;
        }
        return null;
    }

    public RestApplicantIncome getSameNameEmployer(String category, String employer, Boolean isCurrent) {
        for (RestApplicantIncome income : incomes) {
            if (category.equalsIgnoreCase(income.getCategory()) &&
                    employer.equalsIgnoreCase(income.getEmployer().getName()) &&
                    income.getEmployer().getStatus().equals(isCurrent ? "Current" : "Previous"))
                return income;
        }
        return null;
    }

    public RestApplicantIncome getPrimaryIncome() {
        Long primaryId = 0L;
        for (RestIncomeGroup incomeGroup : getIncomeCategoryW2().getIncomeGroups()) {
            if ("Primary".equals(incomeGroup.getClassificationType())) {
                primaryId = incomeGroup.getIncomeIds()[0];
                break;
            }
        }
        for (RestApplicantIncome income : incomes) {
            if (primaryId.equals(income.getId())) {
                return income;
            }
        }
        return null;
    }

    public List<RestApplicantIncome> getCurrentIncomes() {
        List<RestApplicantIncome> currentIncomes = new ArrayList<>();
        for (RestApplicantIncome income : incomes) {
            if (income.getCurrent())
                currentIncomes.add(income);
        }
        return currentIncomes;
    }

    public List<RestApplicantIncome> getCurrentIncomes(String category) {
        List<RestApplicantIncome> currentIncomes = new ArrayList<>();
        for (RestApplicantIncome income : incomes) {
            if (category.equalsIgnoreCase(income.getCategory()) && income.getCurrent())
                currentIncomes.add(income);
        }
        return currentIncomes;
    }

    public List<RestApplicantIncome> getPreviousIncomes() {
        List<RestApplicantIncome> previousIncomes = new ArrayList<>();
        for (RestApplicantIncome income : incomes) {
            if (!income.getCurrent())
                previousIncomes.add(income);
        }
        return previousIncomes;
    }

    public List<RestApplicantIncome> getPreviousIncomes(String category) {
        List<RestApplicantIncome> previousIncomes = new ArrayList<>();
        for (RestApplicantIncome income : incomes) {
            if (category.equalsIgnoreCase(income.getCategory()) && !income.getCurrent())
                previousIncomes.add(income);
        }
        return previousIncomes;
    }

    public List<RestPartIncome> selectAllIncomeParts() {
        List<RestPartIncome> allCurrentIncomes = new ArrayList<>();
        allCurrentIncomes.addAll(selectAllIncomeParts(IncomePartType.BASE_PAY));
        allCurrentIncomes.addAll(selectAllIncomeParts(IncomePartType.OVERTIME));
        allCurrentIncomes.addAll(selectAllIncomeParts(IncomePartType.COMMISSIONS));
        allCurrentIncomes.addAll(selectAllIncomeParts(IncomePartType.BONUS));
        return allCurrentIncomes;
    }

    public List<RestPartIncome> selectAllIncomeParts(IncomePartType type) {
        List<RestPartIncome> currentIncomes = new ArrayList<>();
        incomes.forEach(income ->
                currentIncomes.addAll(selectAllIncomeParts(type, income.getEmployer().getName())));
        return currentIncomes;
    }

    public List<RestPartIncome> selectAllIncomeParts(IncomePartType type, String employerName) {
        return getIncome(employerName)
                .getPart(type.toString())
                .getIncomes().stream()
                .filter(variant -> !variant.getAnnualSummary().isEmpty())
                .filter(variant -> !variant.getQualified()).collect(Collectors.toList());
    }

    public List<FailureComparison> compareTo(RestGetResponseApplicant anotherApplicant) {
        return compareTo(anotherApplicant, "");
    }

    public List<FailureComparison> compareTo(RestGetResponseApplicant anotherApplicant, String applicantName) {
        List<FailureComparison> failList = new ArrayList<>(this.getIncomeCategoryW2().compareTo(anotherApplicant.getIncomeCategoryW2(), applicantName));
        for (RestApplicantIncome thisApplicantIncome : this.getIncomes()) {
            if (anotherApplicant.getIncome(thisApplicantIncome.getEmployer().getName()) == null) {
                failList.add(new FailureComparison(String.format("%s.%s is null", applicantName, thisApplicantIncome.getEmployer().getName()),
                        thisApplicantIncome.getEmployer().getName(), "null"));
            } else {
                failList.addAll(thisApplicantIncome.compareTo(anotherApplicant.getIncome(thisApplicantIncome.getEmployer().getName()), applicantName));
            }
        }
        return failList;
    }
}

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
public class RestApplicantIncomeCategory {

    private String category;
    private List<RestIncomeGroup> incomeGroups;
    private Map<String, RestIncomeType> incomeTypes;
    private Boolean selected;
    private String id;
    private Boolean qualified;
    private BigDecimal qualifyingIncome;

    private List<RestIncomeCategoryTooltip> tooltips = new ArrayList<>();

    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public RestIncomeGroup getIncomeGroup(String groupId) {
        return this.incomeGroups.stream().filter(group -> groupId.equals(group.getId())).findFirst().orElseThrow(NullPointerException::new);
    }

    public RestIncomeGroup getPrimaryIncomeGroup() {
        return this.incomeGroups.stream().filter(group -> "Primary".equals(group.getClassificationType())).findFirst().orElseThrow(NullPointerException::new);
    }

    public RestIncomeGroup getSecondaryIncomeGroup() {
        return this.incomeGroups.stream().filter(group -> "Secondary".equals(group.getClassificationType())).findFirst().orElseThrow(NullPointerException::new);
    }

    public List<FailureComparison> compareTo(RestApplicantIncomeCategory anotherIncomeCategory) {
        return compareTo(anotherIncomeCategory, "");
    }

    public List<FailureComparison> compareTo(RestApplicantIncomeCategory anotherIncomeCategory, String applicantName) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicant(%s).incomeCategories.W2.selected", applicantName),
                this.getSelected(), anotherIncomeCategory.getSelected()));
        failList.add(new FailureComparison(String.format("applicant(%s).incomeCategories.W2.qualified", applicantName),
                this.getQualified(), anotherIncomeCategory.getQualified()));
        failList.add(new FailureComparison(String.format("applicant(%s).incomeCategories.W2.qualifyingIncome", applicantName),
                this.getQualifyingIncome(), anotherIncomeCategory.getQualifyingIncome()));

        for (int i = 0; i < this.getIncomeGroups().size(); i++) {
            RestIncomeGroup thisIncomeGroup = this.getIncomeGroups().get(i);
            RestIncomeGroup anotherIncomeGroup = anotherIncomeCategory.getIncomeGroups().get(i);
            failList.addAll(thisIncomeGroup.compareTo(anotherIncomeGroup, applicantName));

        }
        return failList;
    }
}

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

@Data
@Accessors(chain = true)
public class RestApplicantIncome {

    private String category;
    private Boolean current;
    private RestIncomeEmployer employer;
    private Long id;
    private List<RestIncomePart> parts = new ArrayList<>();
    private Boolean qualified;
    private BigDecimal qualifyingIncome;
    private Map<IncomePartType, BigDecimal> declaredIncome;
    private Boolean selected;
    private List<RestIncomeTooltip> tooltips = new ArrayList<>();
    private Long ytd;

    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public RestIncomePart getBasePay() {
        return getPart("Base Pay");
    }

    public RestIncomePart getOvertime() {
        return getPart("Overtime");
    }

    public RestIncomePart getCommissions() {
        return getPart("Commissions");
    }

    public RestIncomePart getBonus() {
        return getPart("Bonus");
    }

    public RestIncomePart getPart(String type) {
        for (RestIncomePart part : parts) {
            if (type.replace("_", " ").equalsIgnoreCase(part.getType().replace("_", " ")))
                return part;
        }
        return null;
    }

    public List<FailureComparison> compareTo(RestApplicantIncome anotherApplicantIncome) {
        return compareTo(anotherApplicantIncome, "");
    }

    public List<FailureComparison> compareTo(RestApplicantIncome anotherApplicantIncome, String applicantName) {
        List<FailureComparison> failList = new ArrayList<>();

        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).qualified", applicantName, this.getEmployer().getName()),
                this.getQualified(), anotherApplicantIncome.getQualified()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).selected", applicantName, this.getEmployer().getName()),
                this.getSelected(), anotherApplicantIncome.getSelected()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).current", applicantName, this.getEmployer().getName()),
                this.getCurrent(), anotherApplicantIncome.getCurrent()));
        failList.addAll(this.getEmployer().compareTo(anotherApplicantIncome.getEmployer()));
        for (RestIncomePart thisIncomePart : this.getParts())
            failList.addAll(thisIncomePart.compareTo(anotherApplicantIncome.getPart(thisIncomePart.getType()), applicantName, this.getEmployer().getName()));

        return failList;
    }
}

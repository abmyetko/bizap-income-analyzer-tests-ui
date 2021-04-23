package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@SuppressWarnings("squid:S1820")
@Data
@Accessors(chain = true)
public class RestPartIncomeAnnualSummary {

    private Map<String, List<String>> annotations;
    private BigDecimal difference;
    private RestAnnualSummaryDisplayGrossPay displayGrossPay;
    private String docType;
    private RestAnnualSummaryFrequency frequency;
    private RestAnnualSummaryGrossAmount grossAmount;
    private RestAnnualSummaryHours hours;
    private Long id;
    private RestSummaryMonthlyAmount monthlyAmountAnnualized;
    private RestSummaryMonthlyAmount monthlyAmountAvg;
    private RestSummaryMonthlyAmount monthlyAmountCalculated;
    private BigDecimal monthsPaid;
    private RestIncomeMonths months;
    private RestAnnualSummaryPayPeriodDates payPeriodEndDate;
    private RestAnnualSummaryPayPeriodDates payPeriodStartDate;
    private RestAnnualSummaryPeriodAmount periodAmount;
    private RestAnnualSummaryRate rate;
    private Boolean selected;
    private Boolean included;
    private RestAnnualSummaryType type;
    private BigDecimal variance;
    private List<Long> docIds = new ArrayList<>();

    public void setVariance(BigDecimal variance) {
        this.variance = setBigDecimalValue(variance);
    }

    public void setDifference(BigDecimal difference) {
        this.difference = setBigDecimalValue(difference);
    }

    public void setMonthsPaid(BigDecimal monthsPaid) {
        this.monthsPaid = setBigDecimalValue(monthsPaid);
    }

    public List<FailureComparison> compareTo(RestPartIncomeAnnualSummary anotherPartIncomeAnnualSummary) {
        return compareTo(anotherPartIncomeAnnualSummary, "", "", "", "");
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
    public List<FailureComparison> compareTo(RestPartIncomeAnnualSummary anotherPartIncomeAnnualSummary, String applicantName, String employerName, String incomeType, String year) {
        List<FailureComparison> failList = new ArrayList<>();

        if (anotherPartIncomeAnnualSummary != null) {
            failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.variance", applicantName, employerName, incomeType, year),
                    this.getVariance(), anotherPartIncomeAnnualSummary.getVariance()));
            failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.difference", applicantName, employerName, incomeType, year),
                    this.getDifference(), anotherPartIncomeAnnualSummary.getDifference()));
            failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.selected", applicantName, employerName, incomeType, year),
                    this.getSelected(), anotherPartIncomeAnnualSummary.getSelected()));
            failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.included", applicantName, employerName, incomeType, year),
                    this.getIncluded(), anotherPartIncomeAnnualSummary.getIncluded()));
            if (this.getType() != null)
                failList.addAll(this.getType().compareTo(anotherPartIncomeAnnualSummary.getType(), applicantName, employerName, incomeType, year));
            if (this.getRate() != null)
                failList.addAll(this.getRate().compareTo(anotherPartIncomeAnnualSummary.getRate(), applicantName, employerName, incomeType, year));
            if (this.getPeriodAmount() != null)
                failList.addAll(this.getPeriodAmount().compareTo(anotherPartIncomeAnnualSummary.getPeriodAmount(), applicantName, employerName, incomeType, year));
            if (this.getGrossAmount() != null)
                failList.addAll(this.getGrossAmount().compareTo(anotherPartIncomeAnnualSummary.getGrossAmount(), applicantName, employerName, incomeType, year));
            if (this.getPayPeriodStartDate() != null)
                failList.addAll(this.getPayPeriodStartDate().compareTo(anotherPartIncomeAnnualSummary.getPayPeriodStartDate(), applicantName, employerName, incomeType, year));
            if (this.getPayPeriodEndDate() != null)
                failList.addAll(this.getPayPeriodEndDate().compareTo(anotherPartIncomeAnnualSummary.getPayPeriodEndDate(), applicantName, employerName, incomeType, year));
            if (this.getDisplayGrossPay() != null)
                failList.addAll(this.getDisplayGrossPay().compareTo(anotherPartIncomeAnnualSummary.getDisplayGrossPay(), applicantName, employerName, incomeType, year));
            if (this.getFrequency() != null)
                failList.addAll(this.getFrequency().compareTo(anotherPartIncomeAnnualSummary.getFrequency(), applicantName, employerName, incomeType, year));
            if (this.getHours() != null)
                failList.addAll(this.getHours().compareTo(anotherPartIncomeAnnualSummary.getHours(), applicantName, employerName, incomeType, year));
            if (this.getMonths() != null)
                failList.addAll(this.getMonths().compareTo(anotherPartIncomeAnnualSummary.getMonths(), applicantName, employerName, incomeType, year));
            if (this.getMonthlyAmountCalculated() != null)
                failList.addAll(this.getMonthlyAmountCalculated().compareTo(anotherPartIncomeAnnualSummary.getMonthlyAmountCalculated(), applicantName, employerName, incomeType, year));
            if (this.getMonthlyAmountAvg() != null)
                failList.addAll(this.getMonthlyAmountAvg().compareTo(anotherPartIncomeAnnualSummary.getMonthlyAmountAvg(), applicantName, employerName, incomeType, year));
        } else {
            failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).annualSummary.NULL", applicantName, employerName, incomeType, year), this.id, null));
        }

        return failList;
    }
}

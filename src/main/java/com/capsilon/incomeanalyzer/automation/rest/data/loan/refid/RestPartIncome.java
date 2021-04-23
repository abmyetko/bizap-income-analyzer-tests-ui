package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestPartIncome {

    private List<RestPartIncomeAnnualSummary> allAnnualSummary = new ArrayList<>();
    private List<String> disabledIds = new ArrayList<>();
    private String id;
    private Boolean qualified;
    private BigDecimal qualifyingIncome;
    private Boolean selected;
    private String variant = "YEAR";
    private Integer year;
    private Boolean ytd;
    private BigDecimal monthsPaid;

    public void setQualifyingIncome(BigDecimal qualifyingIncome) {
        this.qualifyingIncome = setBigDecimalValue(qualifyingIncome);
    }

    public void setMonthsPaid(BigDecimal monthsPaid) {
        this.monthsPaid = setBigDecimalValue(monthsPaid);
    }

    public RestPartIncomeAnnualSummary getAllAnnualSummaryDocument(SummaryDocumentType documentType) {
        return allAnnualSummary.stream().filter(document -> documentType.toString().equals(document.getDocType())).findFirst().orElse(null);
    }

    public RestPartIncomeAnnualSummary getAllAnnualSummaryDocument(Long docId) {
        return allAnnualSummary.stream().filter(document -> docId.equals(document.getDocIds().get(0))).findFirst().orElse(null);
    }

    public List<RestPartIncomeAnnualSummary> getAnnualSummary() {
        if (allAnnualSummary == null)
            return new ArrayList<>();
        return allAnnualSummary.stream().filter(RestPartIncomeAnnualSummary::getIncluded).collect(Collectors.toList());
    }

    public RestPartIncomeAnnualSummary getAnnualSummaryDocument(SummaryDocumentType documentType) {
        return allAnnualSummary.stream().filter(RestPartIncomeAnnualSummary::getIncluded)
                .filter(document -> documentType.toString().equals(document.getDocType())).findFirst().orElse(null);
    }

    public RestPartIncomeAnnualSummary getAnnualSummaryDocument(Long docId) {
        return allAnnualSummary.stream().filter(RestPartIncomeAnnualSummary::getIncluded)
                .filter(document -> docId.equals(document.getDocIds().get(0))).findFirst().orElse(null);
    }

    public RestPartIncomeAnnualSummary getAnnualSummarySelectedDocument() {
        return allAnnualSummary.stream().filter(RestPartIncomeAnnualSummary::getIncluded)
                .filter(RestPartIncomeAnnualSummary::getSelected).findFirst().orElse(null);
    }

    public List<RestPartIncomeAnnualSummary> getAnnualSummaryNotSelectedDocuments() {
        return allAnnualSummary.stream().filter(RestPartIncomeAnnualSummary::getIncluded)
                .filter(summary -> !summary.getSelected()).collect(Collectors.toList());
    }

    public Boolean isIdInDisabledIdList(String searchedId) {
        return disabledIds.stream().anyMatch(value -> value.equalsIgnoreCase(searchedId));
    }

    public List<FailureComparison> compareTo(RestPartIncome anotherPartIncome) {
        return compareTo(anotherPartIncome, "", "", "");
    }

    public List<FailureComparison> compareTo(RestPartIncome anotherPartIncome, String applicantName, String employerName, String incomeType) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).ytd", applicantName, employerName, incomeType, this.getYear()),
                this.getYtd(), anotherPartIncome.getYtd()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).selected", applicantName, employerName, incomeType, this.getYear()),
                this.getSelected(), anotherPartIncome.getSelected()));
        failList.add(new FailureComparison(String.format("applicants(%s).incomes(%s).parts(%s).incomes(%s).qualified", applicantName, employerName, incomeType, this.getYear()),
                this.getQualified(), anotherPartIncome.getQualified()));
        for (RestPartIncomeAnnualSummary thisPartIncomeAnnualSummary : this.getAnnualSummary())
            failList.addAll(thisPartIncomeAnnualSummary.compareTo(anotherPartIncome.getAnnualSummaryDocument(SummaryDocumentType.valueOf(thisPartIncomeAnnualSummary.getDocType())),
                    applicantName, employerName, incomeType, this.getYear().toString()));

        return failList;
    }
}

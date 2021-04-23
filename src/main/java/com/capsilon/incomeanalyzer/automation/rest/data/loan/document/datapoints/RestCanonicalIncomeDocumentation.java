package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class RestCanonicalIncomeDocumentation {

    @NonNull
    private String id;
    @NonNull
    private String documentPeriodStartDate;
    private String documentPeriodEndDate;
    private String incomePayCheckDate;

    public RestCanonicalIncomeDocumentation(RestCanonicalIncomeDocumentation income) {
        this.id = income.id;
        this.documentPeriodStartDate = income.documentPeriodStartDate;
        this.documentPeriodEndDate = income.documentPeriodEndDate;
        this.incomePayCheckDate = income.incomePayCheckDate;
    }

    public void setDocumentPeriodStartDateUsingInteger(Integer documentPeriodStartDate) {
        this.documentPeriodStartDate = Integer.toString(documentPeriodStartDate);
    }
}

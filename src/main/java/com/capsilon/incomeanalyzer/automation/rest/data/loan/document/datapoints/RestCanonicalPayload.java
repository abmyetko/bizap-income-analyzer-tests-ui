package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RestCanonicalPayload {

    private String id;
    private List<RestCanonicalPayloadApplicant> applicant;
    private RestCanonicalPayloadLoanOriginator loanOriginator;
    private RestCanonicalPayloadSubjectProperty subjectProperty;
    private RestCanonicalPayloadLiabilities liabilities;
    private List<RestCanonicalPayloadExpense> expense;
    private RestCanonicalPayloadAssets assets;
    private RestCanonicalPayloadLoan loan;
    private RestCanonicalPayloadDocument document;

    public void parseAllIncomeItemObjects() {
        applicant.forEach(RestCanonicalPayloadApplicant::parseAllIncomeItemObjects);
    }

    public void parseBackAllIncomeItems() {
        applicant.forEach(RestCanonicalPayloadApplicant::parseBackAllIncomeItems);
    }
}

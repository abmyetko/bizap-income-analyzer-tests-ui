package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadLoan {

    private String id;
    private RestCanonicalPayloadLoanConstruction construction;
    private RestCanonicalPayloadLoanDetail loanDetail;
    private RestCanonicalPayloadLoanData loanData;
    private List<RestCanonicalPayloadPurchaseCredit> purchaseCredit;
    private RestCanonicalPayloadTermsOfLoan termsOfLoan;
}

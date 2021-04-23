package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RestCanonicalIrsData {

    private String id;
    private RestCanonicalIrsDataIncomeDetail irsTaxDocumentDataIncomeDetail;
    private List<RestCanonicalIrsIncomeTaxItem> incomeTaxItem;
}

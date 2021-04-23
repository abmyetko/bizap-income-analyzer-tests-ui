package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalIrsTaxDocument {

    private String id;
    private RestCanonicalIrsData irsTaxDocumentData;
    private RestCanonicalIrsDocumentationDetail irsTaxDocumentDocumentationDetail;
}

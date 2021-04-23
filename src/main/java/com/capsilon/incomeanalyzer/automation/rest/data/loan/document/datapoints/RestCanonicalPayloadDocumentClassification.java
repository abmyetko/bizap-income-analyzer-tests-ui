package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadDocumentClassification {

    private String id;
    private RestCanonicalDocumentClassificationDetail documentClassificationDetail;
}

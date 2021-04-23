package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadSubjectProperty {

    private String id;
    private RestCanonicalSubjectPropertyDetail propertyDetail;
    private RestCanonicalAddress address;
    private RestCanonicalSalesContract salesContract;
}

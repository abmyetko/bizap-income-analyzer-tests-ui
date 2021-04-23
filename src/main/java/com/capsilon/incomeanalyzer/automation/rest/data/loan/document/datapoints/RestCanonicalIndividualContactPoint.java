package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalIndividualContactPoint {

    private String id;
    private String contactPointType;
    private RestCanonicalContactPointDetail contactPointDetail;
    private String contactPointValue;
}

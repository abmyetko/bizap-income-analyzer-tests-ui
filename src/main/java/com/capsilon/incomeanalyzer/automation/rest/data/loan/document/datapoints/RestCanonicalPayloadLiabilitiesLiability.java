package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadLiabilitiesLiability {

    private String id;
    private RestCanonicalLiabilityHolder liabilityHolder;
    private RestCanonicalLiabilityDetail liabilityDetail;
    private RestCanonicalLiabilityAsset asset;
}

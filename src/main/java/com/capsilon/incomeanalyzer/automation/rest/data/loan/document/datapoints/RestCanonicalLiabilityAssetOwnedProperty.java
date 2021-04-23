package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalLiabilityAssetOwnedProperty {

    private String id;
    private Boolean investmentRentalIncomePresentIndicator;
    private String investorREOPropertyIdentifier;
    private RestCanonicalOwnedPropertyDetail propertyDetail;
}

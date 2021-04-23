package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalLiabilityAsset {

    private String id;
    private RestCanonicalLiabilityAssetOwnedProperty ownedProperty;
}

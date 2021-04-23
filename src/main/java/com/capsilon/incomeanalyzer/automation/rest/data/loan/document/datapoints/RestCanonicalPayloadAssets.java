package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadAssets {

    private String id;
    private List<RestCanonicalAssetsAsset> asset;
}

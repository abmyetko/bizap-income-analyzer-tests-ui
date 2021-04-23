package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalLiabilityHolder {

    private String id;
    private RestCanonicalAddress address;
    private RestCanonicalName name;
}

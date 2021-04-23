package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class RestCanonicalMetadata {

    private String dmsModified;
    private String dmsCreated;

    private RestCanonicalMetadataEmployers associatedEmployers;
    private Map<String, String> associatedApplicants;
    private String updateReason;
    private String metadataCreated;
    private String metadataModified;
}

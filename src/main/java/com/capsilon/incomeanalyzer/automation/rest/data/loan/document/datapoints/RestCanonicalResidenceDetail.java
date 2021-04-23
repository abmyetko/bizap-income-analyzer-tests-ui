package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalResidenceDetail {

    private String id;
    private String applicantResidencyBasisType;
    private Long applicantResidencyDurationYearsCount;
    private String applicantResidencyType;
}

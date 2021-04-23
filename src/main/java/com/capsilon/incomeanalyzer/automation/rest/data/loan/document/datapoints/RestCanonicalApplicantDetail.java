package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalApplicantDetail {

    private String id;
    private String applicantClassificationType;
    private String jointAssetLiabilityReportingType;
    private String maritalStatusType;
    private Integer applicantAgeAtApplicationYearsCount;
    private String jointAssetLiabilityReportingApplicantCrossReferenceIdentifier;
    private Integer schoolingYearsCount;
    private Integer dependentCount;
    private String applicantBirthDate;
}

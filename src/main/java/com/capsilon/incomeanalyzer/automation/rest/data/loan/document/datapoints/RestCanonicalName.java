package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalName {

    private String id;
    private String fullName;
    private String firstName;
    private String lastName;

    private String middleName;
    private String suffixName;

    private String functionalTitleDescription;
}

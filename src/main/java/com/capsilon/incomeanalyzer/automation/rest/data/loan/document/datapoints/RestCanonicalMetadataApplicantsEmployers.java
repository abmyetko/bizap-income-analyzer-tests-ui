package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.createNameToken;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestCanonicalMetadataApplicantsEmployers {

    private String employerAlias;
    private String employerAliasToken;
    private List<RestCanonicalMetadataApplicantsNames> employerNames;

    public RestCanonicalMetadataApplicantsEmployers(String employerAlias, String employerName) {
        this.employerAlias = employerAlias;
        this.employerAliasToken = createNameToken(employerAlias);
        this.employerNames = new ArrayList<>();
        this.employerNames.add(new RestCanonicalMetadataApplicantsNames(employerName));
    }
}

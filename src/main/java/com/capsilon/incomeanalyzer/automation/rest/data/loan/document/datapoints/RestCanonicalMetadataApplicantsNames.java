package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Locale;

import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.createNameToken;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestCanonicalMetadataApplicantsNames {

    private String employerName;
    private String employerNameToken;

    public RestCanonicalMetadataApplicantsNames(String employerName) {
        this.employerName = employerName;
        this.employerNameToken = createNameToken(employerName.toUpperCase(Locale.US));
    }
}

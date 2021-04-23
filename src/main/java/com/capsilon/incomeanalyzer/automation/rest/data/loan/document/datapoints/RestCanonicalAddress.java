package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RestCanonicalAddress {

    private String id;
    private String addressLineText;
    private String cityName;
    private String postalCode;
    private String stateCode;
    private String plusFourZipCode;

    public RestCanonicalAddress(RestCanonicalAddress address) {
        this.id = address.id;
        this.addressLineText = address.addressLineText;
        this.cityName = address.cityName;
        this.postalCode = address.postalCode;
        this.stateCode = address.stateCode;
        this.plusFourZipCode = address.plusFourZipCode;
    }
}

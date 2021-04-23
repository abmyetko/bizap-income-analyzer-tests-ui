package com.capsilon.incomeanalyzer.automation.data.upload.data;


import lombok.Data;
import lombok.experimental.Accessors;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;

@Data
@Accessors(chain = true)
public class W2Data extends DocumentData<W2Data> {

    private String borrowerAddress1 = HOMEOWNER_ADDRESS;
    private String borrowerAddress2 = WASHINGTON_ADDRESS;
    private String employerAddress1 = AWESOME_COMPUTERS_ADDRESS;
    private String employerAddress2 = WASHINGTON_ADDRESS;
    private String year;
    private String wagesTipsOtherCompensation;
    private String socialSecurityWages;
    private String medicareWages;
    private String socialSecurityTips;
    private String federalIncomeTax;
    private String socialSecurityTax;
    private String medicareTax;
    private String allocatedTips;
    private String stateWagesTipsEtc;
    private String stateIncomeTax;

    public W2Data(String documentName) {
        super(documentName);
    }
}

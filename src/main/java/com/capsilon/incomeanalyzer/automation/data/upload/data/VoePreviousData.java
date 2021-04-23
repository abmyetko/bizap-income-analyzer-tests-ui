package com.capsilon.incomeanalyzer.automation.data.upload.data;


import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;

@Data
@Accessors(chain = true)
public class VoePreviousData extends DocumentData<VoePreviousData> {

    private String borrowerAddress1 = HOMEOWNER_ADDRESS;
    private String borrowerAddress2 = WASHINGTON_ADDRESS;
    private String employerAddress1 = AWESOME_COMPUTERS_ADDRESS;
    private String employerAddress2 = WASHINGTON_ADDRESS;
    private String lenderName = "Other Guy";
    private String lenderAddress1 = AWESOME_COMPUTERS_ADDRESS;
    private String lenderAddress2 = WASHINGTON_ADDRESS;
    private String lenderTitle = "Nobody";
    private String lenderNumber = "123 666";
    private String lenderSignatureDate = DATE_HALF_MARCH;
    private String lenderSignature = "Other Guy";
    private String borrowerSignature = "SigGis";
    private String signatureTitle = "HR";
    private String signatureSignature = "SigGis";
    private String signatureDate = DATE_HALF_MARCH;
    private String signatureName = "Difer Guy";
    private String signaturePhoneNo = "12-235-695";

    private String hiredDate = "01/01/2000";
    private String terminatedDate = DATE_HALF_MARCH;
    private String baseWageAmount = INCOME_ZERO;
    private String overtimeWageAmount = INCOME_ZERO;
    private String commissionWageAmount = INCOME_ZERO;
    private String bonusWageAmount = INCOME_ZERO;
    private String reasonForLeaving = "Procrastination";
    private String positionHeld = "Court Fool";
    private IncomeFrequency periodType = IncomeFrequency.MONTHLY;


    public VoePreviousData(String documentName) {
        super(documentName);
    }

    public VoePreviousData setExecutionDate(String executionDate) {
        this.signatureSignature = executionDate;
        return this;
    }
}

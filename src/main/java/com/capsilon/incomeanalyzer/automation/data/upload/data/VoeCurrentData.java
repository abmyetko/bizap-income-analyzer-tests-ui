package com.capsilon.incomeanalyzer.automation.data.upload.data;


import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;

@Data
@Accessors(chain = true)
public class VoeCurrentData extends DocumentData<VoeCurrentData> {

    private String borrowerAddress1 = HOMEOWNER_ADDRESS;
    private String borrowerAddress2 = WASHINGTON_ADDRESS;
    private String employerAddress1 = AWESOME_COMPUTERS_ADDRESS;
    private String employerAddress2 = WASHINGTON_ADDRESS;
    private String lenderName = "Some Guy";
    private String lenderAddress1 = AWESOME_COMPUTERS_ADDRESS;
    private String lenderAddress2 = WASHINGTON_ADDRESS;
    private String lenderTitle = "Nobody";
    private String lenderNumber = "123 666";
    private String lenderSignatureDate = DATE_HALF_MARCH;
    private String lenderSignature = "Some Guy";
    private String borrowerSignature = "SigGis";
    private String signDate = DATE_HALF_MARCH;
    private String employmentStartDate;
    private String presentPosition = "Vice-Nobody";
    private String probabilityOfContinuousEmployment = "Yes";
    private String signatureTitle = "HR";
    private String signatureSignature = "SigGis";
    private String signatureDate = DATE_HALF_MARCH;
    private String signatureName = "Difer Guy";
    private String signaturePhoneNo = "12-235-695";
    private String avgHoursPerWeek;

    private IncomeFrequency frequency;
    private String currentGrossBasePayAmount = INCOME_ZERO;
    private String ytdIncomeThruDate = DATE_HALF_MARCH;
    private String ytdBasePay = INCOME_ZERO;
    private String ytdOvertime = INCOME_ZERO;
    private String ytdCommission = INCOME_ZERO;
    private String ytdBonus = INCOME_ZERO;
    private String ytdTotal = INCOME_ZERO;

    private String priorYearYear = ONE_YEAR_PRIOR.toString();
    private String priorYearBasePay = INCOME_ZERO;
    private String priorYearOvertime = INCOME_ZERO;
    private String priorYearCommission = INCOME_ZERO;
    private String priorYearBonus = INCOME_ZERO;
    private String priorYearTotal = INCOME_ZERO;

    private String twoYearPriorYear = TWO_YEARS_PRIOR.toString();
    private String twoYearPriorBasePay = INCOME_ZERO;
    private String twoYearPriorOvertime = INCOME_ZERO;
    private String twoYearPriorCommission = INCOME_ZERO;
    private String twoYearPriorBonus = INCOME_ZERO;
    private String twoYearPriorTotal = INCOME_ZERO;

    public VoeCurrentData(String documentName) {
        super(documentName);
    }

    public VoeCurrentData setExecutionDate(String executionDate) {
        this.signatureSignature = executionDate;
        return this;
    }
}

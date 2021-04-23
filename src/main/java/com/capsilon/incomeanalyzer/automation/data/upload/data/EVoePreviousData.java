package com.capsilon.incomeanalyzer.automation.data.upload.data;


import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EVoePreviousData extends DocumentData<EVoePreviousData> {

    private String employerAddress1;
    private String employerAddress2;
    private String signDate;
    private String verificationDate;
    private String originalHireDate;
    private String endDate;
    private String jobTitle;
    private String signatureDate;
    private String incomeThruDate;

    private IncomeFrequency payRateFrequency;
    private IncomeFrequency frequency;
    private String avgHoursPerPeriod;
    private String currentGrossBasePayAmount;

    private String ytdBasePay;
    private String ytdOvertime;
    private String ytdCommission;
    private String ytdBonus;
    private String ytdTotal;

    private String priorYearBasePay;
    private String priorYearOvertime;
    private String priorYearCommission;
    private String priorYearBonus;
    private String priorYearTotal;

    private String twoYearPriorBasePay;
    private String twoYearPriorOvertime;
    private String twoYearPriorCommission;
    private String twoYearPriorBonus;
    private String twoYearPriorTotal;

    private String verificationType;
    private String permissiblePurpose;
    private String referenceNumber;
    private String trackingNumber;
    private String headquartersAddress;
    private String city;
    private String state;
    private String zipCode;
    private String disclaimer;
    private String division;
    private String employmentStatus;
    private String mostRecentStartDate;
    private String timeOnJob;
    private String rate;
    private String hours;
    private IncomeFrequency payFrequency;
    private String lastAmountOfPayIncrease;
    private String nextAmountOfPayIncrease;
    private String ytdOther;
    private String priorYearOther;
    private String twoYearPriorOther;

    public EVoePreviousData(String documentName) {
        super(documentName);
    }
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestCanonicalDeclarationDetail {

    private String id;
    private String homeownerPastThreeYearsType;
    private String priorPropertyUsageType;
    private String citizenshipResidencyType;
    private String priorPropertyTitleType;
    private String intentToOccupyType;
    private Boolean outstandingJudgmentsIndicator;
    private Boolean borrowedDownPaymentIndicator;
    private Boolean propertyForeclosedPastSevenYearsIndicator;
    private Boolean presentlyDelinquentIndicator;
    private Boolean loanForeclosureOrJudgmentIndicator;
    private Boolean alimonyChildSupportObligationIndicator;
    private Boolean partyToLawsuitIndicator;
    private Boolean bankruptcyIndicator;
    private Boolean coMakerEndorserOfNoteIndicator;
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.setBigDecimalValue;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadTermsOfLoan {

    private String id;
    private BigDecimal noteAmount;

    public void setNoteAmount(BigDecimal noteAmount) {
        this.noteAmount = setBigDecimalValue(noteAmount);
    }
}

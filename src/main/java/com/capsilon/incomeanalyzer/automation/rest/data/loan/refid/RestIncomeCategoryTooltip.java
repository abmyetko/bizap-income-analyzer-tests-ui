package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestIncomeCategoryTooltip {

    private String type;
    private String code;
    private String message;
}

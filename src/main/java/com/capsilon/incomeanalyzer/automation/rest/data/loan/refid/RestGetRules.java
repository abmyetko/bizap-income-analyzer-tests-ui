package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestGetRules {

    private String applicantId;
    private String errorMessage;
    private Long folderId;
    private String fullName;
    private String ruleCode;
    private String ruleId;
    private String status;
}

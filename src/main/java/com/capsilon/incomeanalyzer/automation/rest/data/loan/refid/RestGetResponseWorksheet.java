package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestGetResponseWorksheet {

    private String documentId;
    private Boolean underGeneration;
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestGetResponseCalculatedBy {

    private String firstName;
    private String id;
    private String imageUrl;
    private String lastName;
    private String name;
    private String refId;
}

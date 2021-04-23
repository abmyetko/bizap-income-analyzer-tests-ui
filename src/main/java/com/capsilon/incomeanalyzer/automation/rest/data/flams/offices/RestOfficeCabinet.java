package com.capsilon.incomeanalyzer.automation.rest.data.flams.offices;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestOfficeCabinet {

    private String name;
    private String id;
}

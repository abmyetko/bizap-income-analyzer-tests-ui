package com.capsilon.incomeanalyzer.automation.rest.data.flams.offices;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestOffice {

    private String name;
    private String id;
    private List<RestOfficeCabinet> cabinets = new ArrayList<>();
}

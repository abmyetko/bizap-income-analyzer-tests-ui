package com.capsilon.incomeanalyzer.automation.rest.data.flams.offices;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestOfficeList {

    private List<RestOffice> officeList = new ArrayList<>();
}

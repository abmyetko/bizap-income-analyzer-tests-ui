package com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestPageInfoMeta {

    private List<RestMetaEntry> entry = new ArrayList<>();
}

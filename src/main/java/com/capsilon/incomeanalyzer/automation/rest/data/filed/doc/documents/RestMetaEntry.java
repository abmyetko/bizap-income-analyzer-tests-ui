package com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RestMetaEntry {

    private String key;
    private String value;
}

package com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestFiledDocDocumentCollection {

    private RestDocumentCollection documentCollection;
}

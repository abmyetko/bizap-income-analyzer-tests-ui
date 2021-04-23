package com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RestCollectionDocumentsPageInfo {

    private String id;
    private RestPageInfoMeta meta;
    private String pageId;
}

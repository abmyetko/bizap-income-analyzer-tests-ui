package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RestCanonicalDocument {

    private String dataSourceStatus;
    private List<String> documentTags;
    private String dvFolderId;
    private String created;
    private String siteGuid;
    private RestCanonicalPayload canonicalPayload;
    private String folderId;
    private SummaryDocumentType canonicalDocumentType;
    private SummaryDocumentType sourceCanonicalDocumentType;
    private String dataSourceId;
    private String dateReceived;
    private String modified;
    private String dateClientReceived;
    private String documentId;
    private String conversionDateTime;
    private RestCanonicalMetadata metadata;

    public void removeDocumentData() {
        dataSourceStatus = "REMOVED";
        canonicalPayload = null;
        created = null;
        modified = null;
        dateReceived = null;
        dateClientReceived = null;
    }
}

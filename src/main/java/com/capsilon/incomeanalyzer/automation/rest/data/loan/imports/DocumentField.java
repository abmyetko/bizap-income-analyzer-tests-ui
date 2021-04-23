package com.capsilon.incomeanalyzer.automation.rest.data.loan.imports;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class DocumentField {

    @NonNull
    private String refId;
    @NonNull
    private String name;
    @NonNull
    private String value;
    private String container;
    private String collaboratorId;

    public DocumentField(String refId, String name, String value, Boolean copyValueToCollaboratorId, String collaboratorId) {
        this.refId = refId;
        this.name = name;
        this.value = value;
        if (copyValueToCollaboratorId)
            this.collaboratorId = collaboratorId;
    }

    public DocumentField(String refId, String name, String value, String container) {
        this.refId = refId;
        this.name = name;
        this.value = value;
        this.container = container;
    }
}

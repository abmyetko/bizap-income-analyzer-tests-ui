package com.capsilon.incomeanalyzer.automation.rest.data.loan.imports;

import com.capsilon.incomeanalyzer.automation.utilities.enums.DocumentFieldNames;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.DEFAULT_REFID;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class JsonDocument {

    @NonNull
    private final SummaryDocumentType type;
    @NonNull
    private final String refId;
    private final List<DocumentField> fields = new ArrayList<>();

    public JsonDocument setSSNWithCollabId(String refid, DocumentFieldNames name, String value, String collaboratorId) {
        DocumentField documentField = fields.stream().filter(it -> it.getName().equals(name.value))
                .findFirst()
                .orElseGet(() -> new DocumentField(refId, name.value, value, true, collaboratorId))
                .setRefId(refid)
                .setName(name.value)
                .setValue(value)
                .setCollaboratorId(collaboratorId);
        if (fields.stream().noneMatch(it -> it.getName().equals(name.value))) {
            fields.add(documentField);
        }
        return this;
    }

    public JsonDocument setField(DocumentFieldNames name, String value) {
        setField(DEFAULT_REFID, name.value, value, null);
        return this;
    }

    public JsonDocument setField(DocumentFieldNames name, String value, PaystubIncomeGroups incomeGroup) {
        setField(refId, name.value, value, incomeGroup);
        return this;
    }

    public JsonDocument setField(String refId, String name, String value) {
        setField(refId, name, value, null);
        return this;
    }

    public JsonDocument setField(String refId, String name, String value, PaystubIncomeGroups container) {
        String groupName = null;
        if (container != null)
            groupName = container.value;
        String finalGroupName = groupName;
        DocumentField documentField = fields.stream().filter(it -> it.getName().equals(name))
                .findFirst()
                .orElseGet(() -> new DocumentField(refId, name, value, finalGroupName))
                .setRefId(refId)
                .setName(name)
                .setValue(value)
                .setContainer(finalGroupName);
        if (fields.stream().noneMatch(it -> it.getName().equals(name))) {
            fields.add(documentField);
        }

        return this;
    }

    public JsonDocument setDuplicatedField(String refId, String name, String value, PaystubIncomeGroups container) {
        fields.add(new DocumentField(refId, name, value, container.value));

        return this;
    }

    public DocumentField getField(String name) {
        return fields.stream().filter(it -> it.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public JsonDocument removeField(DocumentFieldNames name) {
        removeField(name.value);
        return this;
    }

    public JsonDocument removeField(String name) {
        fields.removeIf(it -> it.getName().equals(name));
        return this;
    }
}

package com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S1820")
@Data
@Accessors(chain = true)
public class RestCollectionDocuments {

    private String id;
    private String parentId;
    private String dateCreated;
    private String dateModified;
    private String friendlyId;
    private String typeName;
    private String typeDescription;
    private String documentDictionaryName;
    private String category;
    private String hint;
    private String signed;
    private String active;
    private String displayStatus;
    private String pageCount;
    private List<RestCollectionDocumentsPageInfo> pageInfoList = new ArrayList<>();
    private String dateClientReceived;
    private String sender;
    private String dateReceived;
    private String accepted;
    private String decisionDocument;
    private String documentTags;
    private String creator;
    private String noteCount;
    private String dataOnly;
    private String dataFileType;
    private String viewCount;
    private String processingStatus;
    private String revision;
    private String internalAnnotationsPresent;
    private String externalAnnotationsPresent;
    private String containsSignatureInfo;
    private String mailitemFriendlyId;
    private String activeVersion;
    private String autoVersioning;
    private String dataIngested;

    private String collaboratorId;
    private List<String> collaboratorIdList = new ArrayList<>();

    @JsonProperty("collaboratorIds")
    @SerializedName("collaboratorIds")
    private Object collaboratorIdObject;

    @JsonSetter("collaboratorIds")
    public RestCollectionDocuments setCollaboratorIds(JsonNode channelInternal) {
        if (channelInternal != null && channelInternal.size() > 1) {
            if (channelInternal.isArray()) {
                List<String> idList = new ArrayList<>();
                for (JsonNode node : channelInternal) {
                    idList.add(node.get(0).asText());
                }
                this.collaboratorIdList = new ArrayList<>(idList);
            } else {
                collaboratorId = channelInternal.get(0).asText();
            }
        }
        return this;
    }

    public RestCollectionDocuments parseIncomeDocumentationObject() {
        if (collaboratorIdObject != null) {
            if (collaboratorIdObject.getClass().isInstance(ArrayList.class)) {
                collaboratorIdList = (List<String>) collaboratorIdObject;
            } else {
                collaboratorId = (String) collaboratorIdObject;
            }
        }
        return this;
    }

    public RestCollectionDocuments parseBackToIncomeDocumentationObject() {
        if (collaboratorId != null)
            collaboratorIdObject = collaboratorId;
        if (collaboratorIdList != null)
            collaboratorIdObject = new ArrayList<>(collaboratorIdList);
        collaboratorId = null;
        collaboratorIdList = null;
        return this;
    }

    public RestCollectionDocuments moveIdToList() {
        collaboratorIdList.add(collaboratorId);
        collaboratorId = null;
        parseBackToIncomeDocumentationObject();
        return this;
    }

    public RestCollectionDocuments moveIdFromList() {
        collaboratorId = collaboratorIdList.get(0);
        collaboratorIdList = null;
        parseBackToIncomeDocumentationObject();
        return this;
    }
}

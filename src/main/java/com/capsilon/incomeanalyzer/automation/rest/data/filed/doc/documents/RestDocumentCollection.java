package com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Data
@Accessors(chain = true)
public class RestDocumentCollection {

    private String available;
    private String id;
    private RestCollectionDocuments document;
    private List<RestCollectionDocuments> documentsList = new ArrayList<>();

    @JsonSetter("documents")
    public void setDocuments(JsonNode channelInternal) {
        if (channelInternal != null && channelInternal.size() > 1) {
            if (channelInternal.isArray()) {
                List<RestCollectionDocuments> collectionDocumentsList = new ArrayList<>();
                for (JsonNode node : channelInternal) {
                    collectionDocumentsList.add(generateDocument(node));
                }
                this.documentsList = new ArrayList<>(collectionDocumentsList);
            } else {
                document = generateDocument(channelInternal);
            }
        }
    }

    private RestCollectionDocuments generateDocument(JsonNode node) {
        JsonNode listOfPages = node.get("pageInfoList");
        List<RestCollectionDocumentsPageInfo> pageInfoList = new ArrayList<>();
        if (listOfPages.isArray()) {
            pageInfoList = getDocumentPageInfo(listOfPages);
        }

        RestCollectionDocuments documents = new RestCollectionDocuments();
        for (Map.Entry<String, String> docEntry : getAllStringValues(node).entrySet()) {
            try {
                String methodName = "set" + docEntry.getKey().substring(0, 1).toUpperCase(Locale.US) + docEntry.getKey().substring(1);
                Method m = documents.getClass().getMethod(methodName, String.class);
                m.invoke(documents, docEntry.getValue());
                //@formatter:off
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {} //NOSONAR formatter:on
        }
        return documents.setPageInfoList(pageInfoList);
    }

    private HashMap<String, String> getAllStringValues(JsonNode node) {
        HashMap<String, String> map =  new HashMap<>();
        map.put("id", "");
        map.put("parentId", "");
        map.put("dateCreated", "");
        map.put("dateModified", "");
        map.put("friendlyId", "");
        map.put("typeName", "");
        map.put("typeDescription", "");
        map.put("documentDictionaryName", "");
        map.put("category", "");
        map.put("hint", "");
        map.put("signed", "");
        map.put("active", "");
        map.put("displayStatus", "");
        map.put("pageCount", "");
        map.put("dateClientReceived", "");
        map.put("sender", "");
        map.put("dateReceived", "");
        map.put("accepted", "");
        map.put("decisionDocument", "");
        map.put("documentTags", "");
        map.put("creator", "");
        map.put("noteCount", "");
        map.put("dataOnly", "");
        map.put("dataFileType", "");
        map.put("viewCount", "");
        map.put("processingStatus", "");
        map.put("revision", "");
        map.put("internalAnnotationsPresent", "");
        map.put("externalAnnotationsPresent", "");
        map.put("containsSignatureInfo", "");
        map.put("mailitemFriendlyId", "");
        map.put("activeVersion", "");
        map.put("autoVersioning", "");
        map.put("dataIngested", "");

        map.replaceAll((k, v) -> node.get(k) != null ? node.get(k).asText(null) : null);
        return map;
    }

    private List<RestCollectionDocumentsPageInfo> getDocumentPageInfo(JsonNode listOfPages) {
        List<RestCollectionDocumentsPageInfo> pageInfoList = new ArrayList<>();
        for (JsonNode nodeNode : listOfPages) {
            JsonNode metaNodeEntries = nodeNode.get("meta").get("entry");
            List<RestMetaEntry> entries = new ArrayList<>();
            for (JsonNode entry : metaNodeEntries){
                entries.add(new RestMetaEntry(entry.get("key")
                        .asText(null),
                        entry.get("value").asText(null)
                ));
            }
            pageInfoList.add(new RestCollectionDocumentsPageInfo(
                            nodeNode.get("id").asText(null),
                            new RestPageInfoMeta().setEntry(entries),
                            nodeNode.get("pageId").asText(null)
                    )
            );
        }
        return pageInfoList;
    }
}

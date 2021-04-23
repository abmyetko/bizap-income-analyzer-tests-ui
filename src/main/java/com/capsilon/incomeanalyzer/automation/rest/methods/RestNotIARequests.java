package com.capsilon.incomeanalyzer.automation.rest.methods;

import com.capsilon.common.endpoint.ApiGatewayEndpoint;
import com.capsilon.incomeanalyzer.automation.rest.RestCommons;
import com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents.RestCollectionDocuments;
import com.capsilon.incomeanalyzer.automation.rest.data.filed.doc.documents.RestFiledDocDocumentCollection;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.capsilon.incomeanalyzer.automation.utilities.enums.CduStatus;
import com.capsilon.test.applicantassociation.model.Collaborator;
import com.capsilon.test.run.confiuration.BizappsConfig;
import com.capsilon.test.ui.Retry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.capsilon.common.endpoint.ApiGatewayEndpoint.getApplicationTicket;
import static com.capsilon.common.endpoint.FlamsEndpoint.getDvFolderId;
import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static io.restassured.RestAssured.given;

@SuppressWarnings({"squid:S1200", "squid:S1448"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestNotIARequests {

    private static final Integer MAX_FILED_DOC_ITEM_COUNT = 20;
    private static final Integer INCOME_WORKSHEET_PAGE_WIDTH = 820;
    private static final Integer INCOME_WORKSHEET_HEADER_REGION_SIZE = 110;
    private static final Integer INCOME_WORKSHEET_MAIN_REGION_SIZE = 650;
    private static final Integer INCOME_WORKSHEET_FOOTER_REGION_SIZE = 200;

    //Folder collaborators/applicants id's
    public static Collaborator[] getCollaborators(String folderId) {
        final Collaborator[][] response = new Collaborator[1][1];
        Retry.whileTrue(TIMEOUT_TWENTY_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            Response rsp = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .get(REST_URL + "/flams/api/collaborators?folderId={folderId}");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp.then().statusCode(STATUS_OK).extract().as(Collaborator[].class);
                return response[0].length == 0;
            }
        }, "Getting collaborators error, timeout exception");  //NOSONAR
        return response[0];
    }

    public static Response setCduStatus(String folderId, CduStatus status, String reason) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_TEN_SECONDS, () ->
                given().spec(getRequestSpecBuilder())
                        .body(String.format(
                                "{\"cduStatus\": \"%s\"," +
                                        "\"reason\":{" +
                                        "\"custom\":\"%s\"" +
                                        "}}",
                                status.toString(),
                                reason))
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .patch(REST_URL + "/flams/api/pipelineFolderDetails/{folderId}")
                        .then().statusCode(STATUS_OK).extract().response());
    }

    public static void waiveAllFailedChecklistRules(String folderId) {
        Map<Integer, Integer> failedRules = getFailedChecklistRulesIds(folderId);
        waiveChecklistRules(folderId, failedRules, "Flex Tape");
    }

    public static Map<Integer, Integer> getFailedChecklistRulesIds(String folderId) {
        HashMap<Integer, Integer> ruleApplicantMap = new HashMap<>();
        List<Integer> applicantIds = getChecklistRules(folderId).then().extract().jsonPath().getList("applicants.applicantId");
        applicantIds.forEach(applicantId -> {
            List<Integer> applicantFailedRules = getChecklistRules(folderId).then()
                    .extract().jsonPath()
                    .getList("applicants.find { it.applicantId == " + applicantId + "}.rules.findAll { it.status == 'FAILED' }.id");
            applicantFailedRules.forEach(rule -> ruleApplicantMap.put(rule, applicantId));
        });
        return ruleApplicantMap;
    }

    public static Response getChecklistRules(String folderId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_TEN_SECONDS, () ->
            given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .get(REST_URL + "/bizapps/checklist/folders/{folderId}/checklist")
                    .then().statusCode(STATUS_OK).extract().response());
    }

    public static void waiveChecklistRules(String folderId, Map<Integer, Integer> ruleList, String reason) {
        ruleList.forEach((key, value) -> waiveChecklistRule(folderId, value, key, reason));
    }

    public static Response waiveChecklistRule(String folderId, Integer applicantId, Integer ruleId, String reason) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_TEN_SECONDS, () ->
            given().spec(getRequestSpecBuilder())
                    .body("{\"reason\":\"" + reason + "\"}")
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .pathParam(PARAMETER_NAME_APPLICANT_ID, applicantId)
                    .pathParam(PARAMETER_NAME_RULE_ID, ruleId)
                    .post(REST_URL + "/bizapps/checklist//folders/{folderId}/applicants/{applicantId}/rules/{ruleId}/waive")
                    .then().statusCode(STATUS_OK).extract().response(),
                String.format("Waiving rules failed for folderId: %s applicantId: %s ruleId: %s", folderId, applicantId, ruleId));
    }

    public static Response addEmployerAlias(String folderId, String applicantId, String employerName, String alias) {
        return addEmployerAlias(folderId, Integer.valueOf(applicantId), employerName, alias);
    }

    public static Response addEmployerAlias(String folderId, Integer applicantId, String employerName, String alias) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_TEN_SECONDS, () ->
            given().spec(getRequestSpecBuilder())
                    .body(String.format(
                            "[{\"employerAlias\": \"%s\"," +
                                    "\"employerName\":\"%s\"}]",
                            employerName,
                            alias))
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .pathParam(PARAMETER_NAME_APPLICANT_ID, applicantId)
                    .post(REST_URL + "/bizapps/checklist/folders/{folderId}/applicants/{applicantId}/employers/saveNames")
                    .then().statusCode(STATUS_OK).extract().response(),
                String.format("Adding employer alias failed for folderId: %s applicantId: %s employer: %s alias: %s", folderId, applicantId, employerName, alias));
    }

    //Not used Folder canonical document collection for selected document type
    public static List<RestCanonicalDocument> getDocumentCollections(String folderId, String documentType) {
        ObjectMapper objectMapper = new ObjectMapper();
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (type, s) -> {
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return objectMapper;
                }
        ));
        final List<RestCanonicalDocument>[] response = new List[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            Response rsp = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .pathParam(PARAMETER_NAME_DOCUMENT_TYPE, documentType)
                    .get(REST_URL + "/flams/api/v2/canonicalDocuments?folderId={folderId}&canonicalDocumentType={docType}");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp.then().extract().jsonPath().getList("", RestCanonicalDocument.class);
                return false;
            }
        }, "Get document collection error, timeout exception on folder: " + folderId);
        return response[0];
    }

    //Not used Document count of canonical collection for selected type
    public static Integer getDocumentCount(String folderId, String documentType) {
        final Response[] response = new Response[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            Response rsp = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .pathParam(PARAMETER_NAME_DOCUMENT_TYPE, documentType)
                    .get(REST_URL + "/flams/api/v2/canonicalDocuments?folderId={folderId}&canonicalDocumentType={docType}");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp;
                return false;
            }
        }, String.format("Get %s count error, timeout exception on folder: %s", documentType, folderId));
        if (response[0].jsonPath().getList("canonicalPayload") == null)
            return 0;
        else
            return response[0].jsonPath().getList("canonicalPayload").size();
    }

    public static PDFTextStripperByArea getIncomeWorksheetPageText(PDDocument worksheetDocument, Integer pageNumber) {
        if (worksheetDocument.getNumberOfPages() <= pageNumber)
            return null;
        try {
            PDFTextStripperByArea textStripperByArea = new PDFTextStripperByArea();
            textStripperByArea.addRegion("header", new Rectangle2D.Double(0, 0, INCOME_WORKSHEET_PAGE_WIDTH, INCOME_WORKSHEET_HEADER_REGION_SIZE));
            textStripperByArea.addRegion("mainPage", new Rectangle2D.Double(0, INCOME_WORKSHEET_HEADER_REGION_SIZE, INCOME_WORKSHEET_PAGE_WIDTH, INCOME_WORKSHEET_MAIN_REGION_SIZE));
            textStripperByArea.addRegion("footer", new Rectangle2D.Double(0, INCOME_WORKSHEET_MAIN_REGION_SIZE, INCOME_WORKSHEET_PAGE_WIDTH, INCOME_WORKSHEET_FOOTER_REGION_SIZE));
            textStripperByArea.extractRegions(worksheetDocument.getPage(pageNumber));
            return textStripperByArea;
            //@formatter:off
        } catch (IOException ignore) {
        } //NOSONAR
        //@formatter:on
        return null;
    }

    public static PDDocument getCurrentIncomeWorksheetPdf(String folderId) {
        return getSelectedPdfDocument(getCurrentAutomatedIncomeWorksheet(folderId),
                String.format("IncomeWorksheetDocument%s.pdf", System.currentTimeMillis()),
                String.format("target\\downloaded-income-worksheets\\%s", folderId));
    }

    public static PDDocument getSelectedPdfDocument(RestCollectionDocuments documentToDownload, String documentName, String filePath) {
        try {
            byte[] entityBytes = downloadSelectedDocument(documentToDownload).body().asByteArray();
            if (!new File(filePath).exists())
                new File(filePath).mkdirs();
            Files.write(Paths.get(filePath, documentName), entityBytes);
            return PDDocument.load(Paths.get(filePath, documentName).toFile());
            //@formatter:off
        } catch (IOException ignore) {
        } //NOSONAR
        //@formatter:on
        return null;
    }

    public static Response downloadSelectedDocument(RestCollectionDocuments document) {
        final Response[] response = new Response[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {

            Response rsp = given().spec(new RequestSpecBuilder()
                    .addCookies(getRestCookies())
                    .setContentType(CONTENT_TYPE_PDF)
                    .addHeader(REFERER, LOCALHOST)
                    .addHeader(HEADER_SEC_FETCH_MODE, "navigate")
                    .addHeader(HEADER_SEC_FETCH_SITE, "same-site")
                    .addHeader("Upgrade-Insecure-Requests", "1")
                    .build())
                    .pathParam(PARAMETER_NAME_SESSION_ID, getApplicationTicket())
                    .pathParam(PARAMETER_NAME_RESOURCE_ID, document.getId())
                    .pathParam(PARAMETER_NAME_PAYLOAD_ID, getPackageStatusResponse(document.getId()).jsonPath().getString("packageStatusResponse.payloadId"))
                    .pathParam(PARAMETER_NAME_PAYLOAD_NAME, document.getId())
                    .get(BizappsConfig.getGatewayBaseURL() +
                            "/download-gateway/DownloadPackage?" +
                            "sessionId={sessionId}&" +
                            "resourceId={resourceId}&" +
                            "payloadId={payloadId}&" +
                            "payloadName={payloadName}");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp;
                return false;
            }
        }, "Selected document could not be downloaded in time");
        return response[0];
    }

    public static Response getPackageStatusResponse(String documentId) {
        final Response[] response = new Response[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            Response rsp = given().spec(new RequestSpecBuilder()
                    .addCookies(getRestCookies())
                    .setContentType(CONTENT_TYPE_JSON)
                    .addHeader(REFERER, LOCALHOST)
                    .addHeader(HEADER_SEC_FETCH_MODE, "cors")
                    .addHeader(HEADER_APPLICATION_TICKET_ID, getApplicationTicket())
                    .setBody("{\"checkPackageStatusRequest\":" +
                            "{\"resourceId\":\"" + documentId + "\"," +
                            "\"printable\":\"false\"," +
                            "\"unRedaction\":\"false\"}}")
                    .build())
                    .post(BizappsConfig.getGatewayBaseURL() + "/RESTGateway/services/rest/DeliveryInterfaceV2/getPackageStatusResponse");
            if (rsp.getStatusCode() != STATUS_OK || "COMPLETED".compareTo(rsp.jsonPath().getString("packageStatusResponse.status")) != 0) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp;
                return false;
            }
        }, String.format("Timeout exception, document %s has not been processed in time", documentId));
        return response[0];
    }

    public static RestCollectionDocuments getCurrentAutomatedIncomeWorksheet(String folderId) {
        int documentIndex = 0;
        RestFiledDocDocumentCollection docDocumentCollection = getFiledDocsDocumentCollection(folderId, documentIndex, MAX_FILED_DOC_ITEM_COUNT);
        if (docDocumentCollection.getDocumentCollection().getDocumentsList().isEmpty())
            return null;
        RestCollectionDocuments currentIncomeWorksheet = new RestCollectionDocuments();
        while (currentIncomeWorksheet.getId() == null) {
            currentIncomeWorksheet = docDocumentCollection.getDocumentCollection().getDocumentsList().stream()
                    .filter(doc -> "AutomatedIncomeWorksheet".compareTo(doc.getTypeName()) == 0 && "YES".compareTo(doc.getActiveVersion()) == 0).findFirst().orElse(currentIncomeWorksheet);
            if (currentIncomeWorksheet.getId() == null) {
                if (docDocumentCollection.getDocumentCollection().getDocumentsList().size() == MAX_FILED_DOC_ITEM_COUNT) {
                    documentIndex += MAX_FILED_DOC_ITEM_COUNT;
                    docDocumentCollection = getFiledDocsDocumentCollection(folderId, documentIndex, MAX_FILED_DOC_ITEM_COUNT);
                } else {
                    break;
                }
            }
        }
        if (currentIncomeWorksheet.getId() == null)
            return null;
        else
            return currentIncomeWorksheet;
    }

    public static Response acceptSelectedDocument(String folderId, String documentId) {
        return changeSelectedDocumentStatus(folderId, documentId, true);
    }

    public static Response unAcceptSelectedDocument(String folderId, String documentId) {
        return changeSelectedDocumentStatus(folderId, documentId, false);
    }

    public static Response changeSelectedDocumentStatus(String folderId, String documentId, Boolean isAccepted) {
        final Response[] response = new Response[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            String gatewayAddress = BizappsConfig.getGatewayBaseURL();
            String getDvFolderId = getDvFolderId(folderId);
            response[0] = given().spec(new RequestSpecBuilder()
                    .addCookies(getRestCookies())
                    .setContentType(CONTENT_TYPE_WWW_FORM_URLENCODED_UTF)
                    .addHeader(REFERER, LOCALHOST)
                    .addHeader(HEADER_SEC_FETCH_MODE, "cors")
                    .addHeader(HEADER_APPLICATION_TICKET_ID, getApplicationTicket())
                    .build())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, getDvFolderId)
                    .pathParam(PARAMETER_NAME_DOCUMENTS_ID, documentId)
                    .pathParam("docChange", isAccepted ? "acceptDocuments" : "unmarkAcceptedDocuments")
                    .post(gatewayAddress + "/RESTGateway/services/rest/DocumentInterface/{docChange}?folderId={folderId}&documentIds={documentIds}");
            if (response[0].getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                return false;
            }
        }, String.format("Change document status error, timeout exception on changing status of document: %s to: %s on folder: %s", documentId, isAccepted, folderId));
        return response[0];
    }

    public static RestCollectionDocuments getDocumentById(String folderId, String documentId) {
        final Integer[] documentIndex = {0};
        final RestFiledDocDocumentCollection[] docDocumentCollection = {getFiledDocsDocumentCollection(folderId, documentIndex[0], MAX_FILED_DOC_ITEM_COUNT)};
        final RestCollectionDocuments[] selectedDocument = {new RestCollectionDocuments()};
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            if (docDocumentCollection[0].getDocumentCollection().getDocumentsList().isEmpty())
                selectedDocument[0] = docDocumentCollection[0].getDocumentCollection().getDocument();

            selectedDocument[0] = docDocumentCollection[0].getDocumentCollection().getDocumentsList().stream()
                    .filter(doc -> documentId.compareTo(doc.getId()) == 0).findFirst().orElse(selectedDocument[0]);

            if (selectedDocument[0].getId() == null && docDocumentCollection[0].getDocumentCollection().getDocumentsList().size() == MAX_FILED_DOC_ITEM_COUNT) {
                documentIndex[0] += MAX_FILED_DOC_ITEM_COUNT;
                docDocumentCollection[0] = getFiledDocsDocumentCollection(folderId, documentIndex[0], MAX_FILED_DOC_ITEM_COUNT);
            }
            return selectedDocument[0].getId() == null;
        }, String.format("Get document by id error, timeout exception on folder: %s for documentId: %s", folderId, documentId));
        return selectedDocument[0];
    }

    public static List<PDDocument> downloadAllDocuments(Collection<RestCollectionDocuments> documentsList, String targetFolder) {
        List<PDDocument> documentList = documentsList.stream().parallel()
                .sorted(Comparator.comparingInt(document -> LocalDateTime.parse(document.getDateReceived().replace("Z", "")).toLocalTime().toSecondOfDay()))
                .map(document -> getSelectedPdfDocument(document,
                        String.format("%s%s.pdf", document.getFriendlyId(), System.currentTimeMillis()),
                        String.format("target\\downloaded-income-worksheets\\%s", targetFolder)))
                .collect(Collectors.toList());
        Collections.reverse(documentList);
        return documentList;
    }

    public static List<RestCollectionDocuments> getAllIncomeWorksheets(String folderId) {
        return getAllFiledDocsDocumentCollection(folderId).stream().filter(document -> "AutomatedIncomeWorksheet".equals(document.getTypeName())).collect(Collectors.toList());
    }

    public static List<RestCollectionDocuments> getAllFiledDocsDocumentCollection(String folderId) {
        int documentIndex = 0;
        RestFiledDocDocumentCollection docDocumentCollection = getFiledDocsDocumentCollection(folderId, documentIndex, MAX_FILED_DOC_ITEM_COUNT);
        if (docDocumentCollection.getDocumentCollection().getDocumentsList().isEmpty()) {
            if (docDocumentCollection.getDocumentCollection().getDocument() != null) {
                List<RestCollectionDocuments> oneDocList = new ArrayList<>();
                oneDocList.add(docDocumentCollection.getDocumentCollection().getDocument());
                return oneDocList;
            } else {
                return new ArrayList<>();
            }
        }
        List<RestCollectionDocuments> allDocumentCount = new ArrayList<>();
        while (docDocumentCollection.getDocumentCollection().getDocumentsList().size() == MAX_FILED_DOC_ITEM_COUNT) {
            allDocumentCount.addAll(docDocumentCollection.getDocumentCollection().getDocumentsList());
            documentIndex += MAX_FILED_DOC_ITEM_COUNT;
            docDocumentCollection = getFiledDocsDocumentCollection(folderId, documentIndex, MAX_FILED_DOC_ITEM_COUNT);
        }

        if (docDocumentCollection.getDocumentCollection().getDocumentsList().size() <= MAX_FILED_DOC_ITEM_COUNT && !docDocumentCollection.getDocumentCollection().getDocumentsList().isEmpty()) {
            docDocumentCollection = getFiledDocsDocumentCollection(folderId, documentIndex, MAX_FILED_DOC_ITEM_COUNT);
            allDocumentCount.addAll(docDocumentCollection.getDocumentCollection().getDocumentsList());
        }
        return allDocumentCount;
    }

    public static RestFiledDocDocumentCollection getFiledDocsDocumentCollection(String folderId, Integer startIndex, Integer itemCount) {
        final RestFiledDocDocumentCollection[] response = new RestFiledDocDocumentCollection[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            String gatewayAddress = BizappsConfig.getGatewayBaseURL();
            String dvFolderId = null;
            String userTicketId = null;

            ExecutorService parallelExecutor = Executors.newCachedThreadPool();
            Callable<String> getDvFolderId = () -> getDvFolderId(folderId);
            Callable<String> getUserTicketId = ApiGatewayEndpoint::getApplicationTicket;
            Future<String> futureDvFolderId = parallelExecutor.submit(getDvFolderId);
            Future<String> futureUserTicketId = parallelExecutor.submit(getUserTicketId);
            try {
                dvFolderId = futureDvFolderId.get();
                userTicketId = futureUserTicketId.get();
                //@formatter:off
            } catch (InterruptedException | ExecutionException ignore) {
            } //NOSONAR
            //@formatter:on
            parallelExecutor.shutdown();

            Response rsp = given().spec(new RequestSpecBuilder()
                    .addCookies(getRestCookies())
                    .addHeader(REFERER, LOCALHOST)
                    .addHeader(HEADER_SEC_FETCH_MODE, "cors")
                    .addHeader(HEADER_APPLICATION_TICKET_ID, userTicketId)
                    .build())
                    .pathParam(PARAMETER_NAME_DV_FOLDER_ID, dvFolderId)
                    .pathParam(PARAMETER_NAME_TIMESTAMP, System.currentTimeMillis())
                    .pathParam(PARAMETER_NAME_START_INDEX, startIndex)
                    .pathParam("itemCount", itemCount > MAX_FILED_DOC_ITEM_COUNT ? MAX_FILED_DOC_ITEM_COUNT : itemCount)
                    .get(gatewayAddress +
                            "/RESTGateway/services/rest/DocumentInterface/getFiledDocumentCollection?folderId={dvFolderId}" +
                            "&startIndex={startIndex}&itemCount={itemCount}&includePages=true&timestamp={timestamp}");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp.then().extract().as(RestFiledDocDocumentCollection.class);
                return false;
            }
        }, "Get document collection error, timeout exception on folder: " + folderId);
        return response[0];
    }

    public static Map getFeatureTogglesMap() {
        final Map[] response = new Map[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            Response rsp = given()
                    .spec(getRequestSpecBuilder())
                    .get(REST_URL + "/RESTGateway/api/featureToggles");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp.then().extract().response().jsonPath().get();
                return false;
            }
        }, "Getting feature toggles error, timeout exception");
        return response[0];
    }

    //Not used Get DocReader document endpoint that is used when opening linked income document in new frame
    public static Response getDocReaderDocument(String folderId, String documentId) {
        final Response[] response = new Response[1];
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            Response rsp = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .pathParam(PARAMETER_NAME_DOC_ID, documentId)
                    .get(REST_URL + "/gateway/docReaderDirectLaunch?folderId={folderId}&documentId={docId}");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp;
                return false;
            }
        }, "Getting document error, timeout exception");
        return response[0];
    }

    public static Response getCduStatusUpdate(String folderId) {
        return Retry.tryGet(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
            given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .get(REST_URL + "/topic/mposs/folder/{folderId}/cduStatusUpdate")
                    .then().extract().response(),
                RestCommons.SELECTION_EXCEPTION_MESSAGE);
    }

    public static Response getCduStatus(String folderId) {
        return Retry.tryGet(TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
            given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .get(REST_URL + "/flams/api/pipelineFolderDetails/{folderId}")
                    .then().extract().response(),
                RestCommons.SELECTION_EXCEPTION_MESSAGE);
    }
}

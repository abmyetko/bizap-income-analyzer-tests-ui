package com.capsilon.incomeanalyzer.automation.rest;

import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.run.confiuration.BizappsConfig;
import com.capsilon.test.ui.Retry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.internal.util.IOUtils;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_ONE_MINUTE;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_THREE_MINUTES;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.valueOfLabel;
import static io.restassured.RestAssured.given;

public final class RestCommons {

    public static final Integer ZERO = 0;
    public static final Integer ONE = 1;
    public static final Integer TWO = 2;
    public static final Integer THREE = 3;
    public static final Integer FOUR = 4;
    public static final Integer FIVE = 5;
    public static final Integer SIX = 6;
    public static final Integer SEVEN = 7;
    public static final Integer EIGHT = 8;
    public static final Integer NINE = 9;
    public static final Integer TEN = 10;
    public static final Integer ELEVEN = 11;
    public static final Integer TWELVE = 12;
    public static final Integer THIRTEEN = 13;
    public static final Integer FOURTEEN = 14;
    public static final Integer FIFTEEN = 15;
    public static final Integer SIXTEEN = 16;
    public static final Integer SEVENTEEN = 17;
    public static final String REFERER = "Referer";
    public static final String LOCALHOST = "http://localhost";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String CONTENT_TYPE_JSON_UTF = "application/json;charset=utf-8";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_TEXT_PLAIN_UTF = "text/plain;charset=utf-8";
    public static final String CONTENT_TYPE_WWW_FORM_URLENCODED_UTF = "application/x-www-form-urlencoded; charset=UTF-8";
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int BIG_DECIMAL_PRECISION_EIGHT_POINTS = 8;
    public static final int BIG_DECIMAL_PRECISION_TWO_POINTS = 2;
    public static final String REST_URL = BizappsConfig.getBaseUrl();
    public static final String HEADER_SEC_FETCH_MODE = "Sec-Fetch-Mode";
    public static final String HEADER_SEC_FETCH_SITE = "Sec-Fetch-Site";
    public static final String HEADER_SITE_NAME = "Site-Name";
    public static final String HEADER_DV_APPLICATION_TICKET_ID = "Application-Ticket-Id";
    public static final String HEADER_DV_USER_TICKET_ID = "User-Ticket-Id";
    public static final String HEADER_APPLICATION_TICKET_ID = "applicationTicketId";
    public static final String PARAMETER_NAME_FOLDER_ID = "folderId";
    public static final String PARAMETER_NAME_TOPIC_ID = "topicId";
    public static final String PARAMETER_NAME_APPLICANT_ID = "applicantId";
    public static final String PARAMETER_NAME_RULE_ID = "ruleId";
    public static final String PARAMETER_NAME_DV_FOLDER_ID = "dvFolderId";
    public static final String PARAMETER_NAME_SNIPPET_ID = "snippetId";
    public static final String PARAMETER_NAME_DOC_ID = "docId";
    public static final String PARAMETER_NAME_DOCUMENTS_ID = "documentIds";
    public static final String PARAMETER_NAME_COLLABORATOR_ID = "collaboratorId";
    public static final String PARAMETER_NAME_SESSION_ID = "sessionId";
    public static final String PARAMETER_NAME_PAYLOAD_ID = "payloadId";
    public static final String PARAMETER_NAME_RESOURCE_ID = "resourceId";
    public static final String PARAMETER_NAME_PROPERTY_ID = "propertyId";
    public static final String PARAMETER_NAME_GROUP_ID = "groupId";
    public static final String PARAMETER_NAME_PAYLOAD_NAME = "payloadName";
    public static final String PARAMETER_NAME_DOCUMENT_TYPE = "docType";
    public static final String PARAMETER_NAME_TIMESTAMP = "timestamp";
    public static final String PARAMETER_NAME_START_INDEX = "startIndex";
    public static final String PARAMETER_NAME_EMPLOYER_NAME = "namePart";
    public static final String PARAMETER_NAME_SEARCHED = "searched";
    public static final String PARAMETER_NAME_ID = "id";
    public static final String PARAMETER_NAME_APPLICATION_NAME = "applicationName";
    public static final String PARAMETER_NAME_APPLICATION_SECRET = "applicationSecret";
    public static final String PARAMETER_NAME_EMPLOYER = "employer";
    public static final String PARAMETER_NAME_ALIAS = "alias";
    public static final String PARAMETER_NAME_SELECTED = "selected";
    public static final String PARAMETER_NAME_CATEGORY = "category";
    public static final String PARAMETER_NAME_CHECK = "check";
    public static final String PARAMETER_NAME_DATE = "date";
    public static final String SELECTION_EXCEPTION_MESSAGE = "Selection not changed in time";
    public static final String FIRST_JANUARY = "-01-01";
    public static final String LAST_MARCH = "-03-31";
    public static final String DEFAULT_REFID = "6666666";
    private static Map<String, String> sessionCookies;

    private RestCommons() {
    }

    public static Map<String, String> getRestCookies() {
        if (sessionCookies == null)
            loginToBizapps();
        return sessionCookies;
    }

    public static Map<String, String> getRestCookiesWithoutLogin() {
        return sessionCookies;
    }

    public static Map<String, String> refreshRestCookies() {
        loginToBizapps(true);
        return sessionCookies;
    }

    public static void loginToBizapps() {
        loginToBizapps(false);
    }

    public static void loginToBizapps(boolean force) {
        if (sessionCookies == null || force) {
            String siteGUID = getSiteGuid();

            sessionCookies = given()
                    .spec(new RequestSpecBuilder()
                            .setContentType(CONTENT_TYPE_JSON_UTF)
                            .addHeader(REFERER, REST_URL + "/login")
                            .addHeader("x-site-guid", siteGUID)
                            .build())
                    .body("{\"userName\": \"" + BizappsConfig.getUnderwriterUser() +
                            "\", \"userPassword\": \"" + BizappsConfig.getUnderwriterUserPassword() + "\" }")
                    .when().post(REST_URL + "/gateway/api/dmsSession")
                    .then().statusCode(STATUS_CREATED)
                    .extract().cookies();
        }
    }

    public static Gson getGsonConfig() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public static String getSiteGuid() {
        return given()
                .spec(new RequestSpecBuilder()
                        .setContentType(CONTENT_TYPE_JSON_UTF)
                        .addHeader(REFERER, REST_URL + "/")
                        .build())
                .when().get(REST_URL + "/gateway/api/siteConfiguration")
                .then().statusCode(STATUS_OK)
                .extract().path("siteGUID");
    }

    public static RequestSpecification getRequestSpecBuilder() {
        return getRequestSpecBuilder((String) null);
    }

    public static RequestSpecification getRequestSpecBuilder(String fileBody) {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder
                .addCookies(getRestCookies())
                .setContentType(CONTENT_TYPE_JSON_UTF)
                .addHeader(REFERER, LOCALHOST);
        if (fileBody != null)
            requestSpecBuilder.setBody(fileBody);
        return requestSpecBuilder.build();
    }

    public static RequestSpecification getRequestSpecBuilder(File fileBody) {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder
                .addCookies(getRestCookies())
                .setContentType(CONTENT_TYPE_JSON_UTF)
                .addHeader(REFERER, LOCALHOST);
        if (fileBody != null)
            requestSpecBuilder.setBody(fileBody);
        return requestSpecBuilder.build();
    }

    public static void loadDocumentData(String docName, byte[] docBytes, String documentType, String documentCategory, String folderId, int statusCode) {
        given()
                .spec(new RequestSpecBuilder()
                        .setContentType("multipart/form-data")
                        .addHeader(REFERER, REST_URL + "/")
                        .addCookies(getRestCookies())
                        .build())
                .multiPart("documentUploadMode", "FOLDER", CONTENT_TYPE_TEXT_PLAIN)
                .multiPart("documentType", documentType, CONTENT_TYPE_TEXT_PLAIN)
                .multiPart("documentCategory", documentCategory, CONTENT_TYPE_TEXT_PLAIN)
                .multiPart("document", "@" + docName, docBytes, CONTENT_TYPE_PDF)
                .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                .when()
                .post(REST_URL + "/flams/api/folders/{folderId}/documents")
                .then()
                .statusCode(statusCode);
    }

    public static void loadDocumentData(String docName, byte[] docBytes, String documentType, String documentCategory, String folderId) {
        loadDocumentData(docName, docBytes, documentType, documentCategory, folderId, STATUS_CREATED);
    }


    public static void waitForDocumentProcessing(String folderId, int voeCount, int w2Count, int paystubCount, int eVoeCount) {
        int timeout = (TIMEOUT_ONE_MINUTE) * (voeCount + w2Count + paystubCount + eVoeCount) + TIMEOUT_THREE_MINUTES;
        long timeStart = System.currentTimeMillis();

        waitForDocTypeProcessing(timeout, folderId, SummaryDocumentType.VOE, voeCount);
        timeout -= (System.currentTimeMillis() - timeStart);
        waitForDocTypeProcessing(timeout, folderId, SummaryDocumentType.W2, w2Count);
        timeout -= System.currentTimeMillis() - timeStart;
        waitForDocTypeProcessing(timeout, folderId, SummaryDocumentType.PAYSTUB, paystubCount);
        timeout -= System.currentTimeMillis() - timeStart;
        waitForDocTypeProcessing(timeout, folderId, SummaryDocumentType.EVOE, eVoeCount);
    }

    private static void waitForDocTypeProcessing(int timeout, String folderId, SummaryDocumentType documentName, int documentCount) {
        final int[] docCount = new int[1];
        Retry.whileTrue(TIMEOUT_ONE_MINUTE, timeout, () -> {
            docCount[0] = RestNotIARequests.getDocumentCount(folderId, documentName.toString());
            return docCount[0] < documentCount;
        }, String.format("%s not processed in time in folder %s, something is wrong.", documentName, folderId));
    }

    public static void uploadDocumentsAndWaitForProcessingToFinish(String folderId, Map<String, String> documentList, int statusCode) {
        int voeCount = 0;
        int eVoeCount = 0;
        int w2Count = 0;
        int paystubCount = 0;
        for (Map.Entry<String, String> entry : documentList.entrySet()) {
            byte[] bytes = new byte[0];
            try {
                bytes = IOUtils.toByteArray(new FileInputStream(new File(entry.getKey())));
                //@formatter:off
            } catch (IOException ignore) {} //NOSONAR
            //@formatter:on
            RestCommons.loadDocumentData("testDoc.pdf", bytes, "", "Income/Assets", folderId, statusCode);
            switch (valueOfLabel(entry.getValue())) {
                case VOE:
                    voeCount++;
                    break;
                case EVOE:
                    eVoeCount++;
                    break;
                case W2:
                    w2Count++;
                    break;
                case PAYSTUB:
                    paystubCount++;
                    break;
                default:
                    break;
            }
        }
        RestCommons.waitForDocumentProcessing(folderId, voeCount, w2Count, paystubCount, eVoeCount);
    }

    public static void uploadDocumentsAndWaitForProcessingToFinish(String folderId, Map<String, String> documentList) {
        uploadDocumentsAndWaitForProcessingToFinish(folderId, documentList, STATUS_CREATED);
    }

    public static BigDecimal setBigDecimalValue(BigDecimal input) {
        if (input == null)
            return null;
        else
            return input.setScale(BIG_DECIMAL_PRECISION_EIGHT_POINTS, RoundingMode.HALF_UP);
    }
}

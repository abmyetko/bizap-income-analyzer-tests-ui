package com.capsilon.incomeanalyzer.automation.rest.methods;

import com.capsilon.common.utils.fnmbuilder.FNMBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFNMBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.ui.Retry;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.internal.util.IOUtils;
import io.restassured.response.Response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static io.restassured.RestAssured.given;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestChangeRequests {

    private static final SecureRandom random = new SecureRandom();

    public static Response recalculateFolder(String folderId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_TWENTY_SECONDS, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .post(REST_URL + "/bizapps/incomeanalyzer/internal/calculate?refId={folderId}")
                        .then().statusCode(STATUS_OK).extract().response());
    }

    public static Response cleanJsonDocuments(String folderId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_TWENTY_SECONDS, () ->
                        given().spec(getRequestSpecBuilder())
                                .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                                .put(REST_URL + "/bizapps/incomeanalyzer/internal/application-doc-remover/{folderId}")
                                .then().statusCode(STATUS_OK).extract().response(),
                "Failed to clean JSON documents from folder: " + folderId);
    }

    public static Response selectJobAnnualSummary(String incomeId, Long docId, AnnualSummaryIncomeType incomeType) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                        given().spec(getRequestSpecBuilder())
                                .body("{\"docId\": \"" + docId +
                                        "\", \"id\": \"" + incomeType + "\" }")
                                .pathParam(PARAMETER_NAME_ID, incomeId)
                                .put(REST_URL + "/bizapps/incomeanalyzer/annual-summary/{id}")
                                .then().extract().response(),
                "Data not received in time");
    }

    public static Response selectIncomeCategory(long applicantId, IncomePartCategory category, boolean selected) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_ID, applicantId)
                        .pathParam(PARAMETER_NAME_SELECTED, selected)
                        .pathParam(PARAMETER_NAME_CATEGORY, category)
                        .put(REST_URL + "/bizapps/incomeanalyzer/applicant/{id}/income-category?selected={selected}&id={category}")
                        .then().extract().response());
    }

    public static Response selectIncomeType(long applicantId, String groupId, IncomePartType type, boolean selected) {
        return selectIncomeType(applicantId, groupId + type.value, selected);
    }

    public static Response selectIncomeType(long applicantId, String incomeTypeId, boolean selected) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_ID, applicantId)
                        .pathParam(PARAMETER_NAME_SELECTED, selected)
                        .pathParam(PARAMETER_NAME_CATEGORY, incomeTypeId)
                        .put(REST_URL + "/bizapps/incomeanalyzer/applicant/{id}/income-type?selected={selected}&id={category}")
                        .then().extract().response());
    }

    public static Response selectIncomeAvg(long applicantId, String groupId, IncomeAvg type, boolean selected) {
        return selectIncomeAvg(applicantId, groupId + type.value, selected);
    }

    public static Response selectIncomeAvg(long applicantId, String incomeAvgId, boolean selected) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_ID, applicantId)
                        .pathParam(PARAMETER_NAME_SELECTED, selected)
                        .pathParam(PARAMETER_NAME_CATEGORY, incomeAvgId)
                        .put(REST_URL + "/bizapps/incomeanalyzer/applicant/{id}/income-avg?selected={selected}&id={category}")
                        .then().extract().response());
    }

    public static Response selectIncomeGroup(long applicantId, String groupId, boolean selected) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_ID, applicantId)
                        .pathParam(PARAMETER_NAME_SELECTED, selected)
                        .pathParam(PARAMETER_NAME_GROUP_ID, groupId)
                        .put(REST_URL + "/bizapps/incomeanalyzer/applicant/{id}/income-group?selected={selected}&id={groupId}")
                        .then().extract().response());
    }

    public static Response selectApplicant(long applicantId, boolean selected) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_ID, applicantId)
                        .pathParam(PARAMETER_NAME_CHECK, selected)
                        .put(REST_URL + "/bizapps/incomeanalyzer/applicant/{id}?qualified={check}")
                        .then().extract().response());
    }

    public static Response restoreApplicantDefaults(long applicantId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_ID, applicantId)
                        .post(REST_URL + "/bizapps/incomeanalyzer/applicant/{id}/restore-defaults")
                        .then().extract().response());
    }

    public static Response selectIncomeV1(String partId, boolean selected) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_ID, partId)
                        .pathParam(PARAMETER_NAME_CHECK, selected)
                        .put(REST_URL + "/bizapps/incomeanalyzer/income/{id}?qualified={check}")
                        .then().extract().response());
    }

    public static Response selectIncome(String groupId, String partId, Boolean selected) {
        return selectIncomeAndSetMonthsPaid(groupId, partId, selected, null);
    }

    public static Response setMonthsPaid(String partId, BigDecimal monthsPaid) {
        return selectIncomeAndSetMonthsPaid(null, partId, null, monthsPaid);
    }

    public static Response selectIncomeAndSetMonthsPaid(String groupId, String partId, Boolean selected, BigDecimal monthsPaid) {
        String requestBody = "";
        if (selected != null) {
            requestBody += "\n\"incomeGroupId\": " + groupId + ",";
            requestBody += "\n\"qualified\": " + selected.toString();
        }
        if (monthsPaid != null)
            requestBody += (!"".equals(requestBody) ? "," : "") + "\n\"monthsPaid\": " + monthsPaid.setScale(TWO, RoundingMode.HALF_UP).toString();
        String finalRequestBody = "{" + requestBody + "\n}";
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_TWENTY_SECONDS, () ->
                given().spec(getRequestSpecBuilder())
                        .body(finalRequestBody)
                        .pathParam(PARAMETER_NAME_ID, partId)
                        .patch(REST_URL + "/bizapps/incomeanalyzer/income/{id}")
                        .then().extract().response());

    }

    public static Response uploadNewFnm(String folderId, FNMBuilder builder) {
        String defaultFilePath = new IAFNMBuilder().getDEFAULT_FILEPATH();
        return uploadNewFnm(folderId, builder, String.format("%s%s%s.fnm", defaultFilePath.substring(0, defaultFilePath.lastIndexOf('.')), random.nextInt(), folderId));
    }

    public static Response uploadNewFnm(String folderId, FNMBuilder builder, String filePath) {
        File fnmFile = new File(filePath);
        builder.build(fnmFile.getAbsolutePath());
        return uploadNewFnm(folderId, fnmFile);
    }

    public static Response uploadNewFnm(String folderId, File filePath) {
        Map<String, String> cookies = getRestCookies();
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(new RequestSpecBuilder()
                        .setContentType("multipart/form-data")
                        .addHeader(REFERER, REST_URL + "/")
                        .addHeader("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"))
                        .addCookies(cookies)
                        .build())
                        .multiPart("fnm", filePath, CONTENT_TYPE_OCTET_STREAM)
                        .multiPart("siteGuid", cookies.get("x-site-guid"), CONTENT_TYPE_TEXT_PLAIN)
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .post(REST_URL + "/bizappsdev/flams/dev/api/folders/{folderId}/fnms")
                        .then()
                        .statusCode(STATUS_CREATED)
                        .extract().response());
    }

    public static Response uploadNewMismo(String folderId, File filePath) {
        Map<String, String> cookies = getRestCookies();
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(new RequestSpecBuilder()
                        .setContentType("multipart/form-data")
                        .addHeader(REFERER, REST_URL + "/")
                        .addHeader("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"))
                        .addHeader("x-site-guid", cookies.get("x-site-guid"))
                        .addHeader("x-api-version", "4")
                        .addHeader("x-session-token", cookies.get("x-session-token"))
                        .addCookies(cookies).build()).multiPart("documentUploadMode", "FOLDER", "text/plain")
                        .multiPart("documentType", "MISMO_3_4", "text/plain")
                        .multiPart("document", "@mismoDoc.xml", IOUtils.toByteArray(new FileInputStream(filePath)))
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .post(REST_URL + "/flams/api/folders/{folderId}/electronicDataFiles")
                        .then()
                        .statusCode(STATUS_OK)
                        .extract().response());

    }

    public static Response lockIncome(long applicationId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, () ->
                given().spec(new RequestSpecBuilder()
                        .addCookies(getRestCookies())
                        .setContentType(CONTENT_TYPE_JSON_UTF)
                        .addHeader(REFERER, LOCALHOST)
                        .build())
                        .body("\"LOCK\"")
                        .pathParam("id", applicationId)
                        .patch(REST_URL + "/bizapps/incomeanalyzer/application/{id}")
                        .then().extract().response());

    }

    public static Response unlockIncome(long applicationId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, () ->
                given().spec(new RequestSpecBuilder()
                        .addCookies(getRestCookies())
                        .setContentType(CONTENT_TYPE_JSON_UTF)
                        .addHeader(REFERER, LOCALHOST)
                        .build())
                        .body("\"OPEN\"")
                        .pathParam("id", applicationId)
                        .patch(REST_URL + "/bizapps/incomeanalyzer/application/{id}")
                        .then().extract().response());
    }

    public static Response updatePayFrequency(String folderId, String documentId, IncomeFrequency payFrequency) {
        return updatePayFrequency(folderId, documentId, payFrequency, STATUS_OK);
    }

    public static Response updatePayFrequency(String folderId, String documentId, IncomeFrequency payFrequency, int statusCode) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                given().spec(new RequestSpecBuilder().addCookies(getRestCookies())
                        .setContentType(CONTENT_TYPE_JSON_UTF)
                        .addHeader(REFERER, LOCALHOST).build())
                        .body("{\"frequency\": \"" + payFrequency + "\"}")
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .pathParam(PARAMETER_NAME_DOC_ID, documentId)
                        .post(REST_URL + "/bizapps/incomeanalyzer/application/{folderId}/document/{docId}/updateFrequency")
                        .then()
                        .statusCode(statusCode)
                        .extract().response()
        );
    }

    //request does generate pdf but another is needed to download it and check correctness
    public static Response generateIncomeWorksheetPdf(String incomeId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                        given().spec(new RequestSpecBuilder()
                                .addCookies(getRestCookies())
                                .addHeader(REFERER, REST_URL)
                                .build())
                                .pathParam(PARAMETER_NAME_FOLDER_ID, incomeId)
                                .get(REST_URL + "/bizapps/incomeanalyzer/application/{folderId}/income-worksheet")
                                .then().extract().response(),
                "Income worksheet not generated in time");
    }
}

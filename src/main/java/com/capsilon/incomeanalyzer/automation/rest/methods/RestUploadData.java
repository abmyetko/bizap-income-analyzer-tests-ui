package com.capsilon.incomeanalyzer.automation.rest.methods;

import com.capsilon.incomeanalyzer.automation.rest.data.flams.offices.RestOffice;
import com.capsilon.incomeanalyzer.automation.rest.data.flams.offices.RestOfficeCabinet;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.capsilon.test.ui.Retry;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static io.restassured.RestAssured.given;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestUploadData {

    private static final String TIMEOUT_EXCEPTION_MESSAGE = "\nSending error, timeout exception";

    //Create folder with preprocess
    public static String createLoanWithPreprocess(File fnmFile) {
        preprocessLoan(fnmFile);
        return createLoanV2(fnmFile);
    }

    //Preprocess fnmFile that apparently helps with loan creation
    public static String preprocessLoan(File fnmFile) {
        final String[] folderId = {""};
        Retry.whileTrue(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            if (fnmFile.exists()) {
                String body;
                try {
                    body = new String(Base64.getEncoder().encode(new String(Files.readAllBytes(fnmFile.toPath())).getBytes(Charset.defaultCharset())), StandardCharsets.US_ASCII); //NOSONAR
                    if (body.isEmpty()) {
                        return true;
                    } else {
                        folderId[0] = given().spec(getRequestSpecBuilder
                                ("{ \"fileContent\": \"" + body +
                                        "\", \"fileType\": \"FNM\"}"))
                                .post(REST_URL + "/mposs/utility/preprocessLoanApplicationFromFileStream")
                                .then().statusCode(STATUS_OK).extract().response().asString();
                        return false;
                    }
                    //@formatter:off
                        } catch (IOException ignore) {} //NOSONAR
                //@formatter:on
            }
            return true;
        }, TIMEOUT_EXCEPTION_MESSAGE);
        return folderId[0];
    }

    public static String createLoanV2(File fnmFile) {
        String cabinetId = getSelectedCabinetId();

        Map loadFnmMap = given().spec(new RequestSpecBuilder()
                .setContentType("multipart/form-data")
                .addHeader(REFERER, REST_URL + "/")
                .addCookies(getRestCookies())
                .build())
                .multiPart("loanFile", fnmFile, CONTENT_TYPE_OCTET_STREAM)
                .post(REST_URL + "/flams/api/basicLoanDetails")
                .then()
                .statusCode(STATUS_OK)
                .extract().response().jsonPath().get();
        String payloadId = (String) loadFnmMap.get("payloadId");

        Map requestBody = new HashMap();
        requestBody.put("cabinetId", cabinetId);
        requestBody.put("payloadId", payloadId);
        JSONObject paramsJson = new JSONObject();
        paramsJson.putAll(requestBody);
        return given()
                .spec(getRequestSpecBuilder())
                .body(paramsJson)
                .post(REST_URL + "/flams/api/v3/pipelineFolders")
                .then()
                .statusCode(STATUS_CREATED)
                .extract().response().jsonPath().get("folderId");
    }

    public static String getSelectedCabinetId() {
        return getSelectedCabinetId("Wholesale_demo", "Loans_demo");
    }

    public static String getSelectedCabinetId(String officeName, String cabinetName) {
        final List<RestOffice>[] officeList = new List[]{null};
        Retry.whileTrue(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_FIVE_MINUTES, () -> {
            Response rsp = given()
                    .spec(getRequestSpecBuilder())
                    .get(REST_URL + "/flams/api/offices");
            if (rsp.getStatusCode() != STATUS_OK) {
                return true;
            } else {
                officeList[0] = rsp.then().extract().jsonPath().getList("", RestOffice.class);
                return false;
            }
        }, TIMEOUT_EXCEPTION_MESSAGE);

        List<RestOfficeCabinet> cabinetList = Objects.requireNonNull(officeList[0].stream().filter(office ->
                officeName.equals(office.getName())).findFirst().orElse(officeList[0].get(0))
                .getCabinets());
        return cabinetList.stream().filter(cabinet -> cabinetName.equals(cabinet.getName())).findFirst().orElse(cabinetList.get(0))
                .getId();
    }

    public static void importApplicationData(File jsonFile, String folderId) {
        Retry.whileTrue(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_FIVE_MINUTES, () -> {
            if (jsonFile.exists()) {
                return given().spec(getRequestSpecBuilder(jsonFile))
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .post(REST_URL + "/bizapps/incomeanalyzer/internal/application-import/{folderId}")
                        .getStatusCode() != STATUS_OK;
            }
            return true;
        }, TIMEOUT_EXCEPTION_MESSAGE);
    }

    public static void pushMessageToKafkaTopic(String topic, String jsonBody) {
        pushMessageToKafkaTopic(topic, jsonBody, null);
    }

    public static void pushMessageToKafkaTopic(String topic, String jsonBody, String key) {
        String requestAddress = key == null ? "internal/kafka/{topicId}" : "internal/kafka/{topicId}/" + key;
        final String[] lastResponseFail = new String[1];

        Retry.whileTrue(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_FIVE_MINUTES, () -> {
            Response rsp = given().spec(new RequestSpecBuilder()
                    .setContentType(CONTENT_TYPE_JSON)
                    .addCookies(getRestCookies())
                    .addHeader("X-XSRF-TOKEN", getRestCookies().get("XSRF-TOKEN"))
                    .build())
                    .body(jsonBody.replace("\n", "").replace("\t", ""))
                    .pathParam(PARAMETER_NAME_TOPIC_ID, topic)
                    .post(REST_URL + "bizapps/incomeanalyzer/" + requestAddress);
            if (rsp.getStatusCode() != STATUS_OK) {
                lastResponseFail[0] = rsp.toString();
                refreshRestCookies();
                return true;
            } else {
                return false;
            }
        }, String.format("%s%n message could not be pushed to kafka topic: %s%n Topic: %s%n Message: %n%s%n", TIMEOUT_EXCEPTION_MESSAGE, lastResponseFail[0], topic, jsonBody));
    }

    //Upload jsons with income values to selected folder
    public static void importApplicationData(String fileBody, String folderId,  int statusCode) {
        final String[] lastResponseFail = new String[1];
        Retry.whileTrue(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_FIVE_MINUTES, () -> {
            if (fileBody != null) {
                Response rsp = given().spec(getRequestSpecBuilder(fileBody))
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .post(REST_URL + "/bizapps/incomeanalyzer/internal/application-import/{folderId}");
                if (rsp.getStatusCode() != statusCode) {
                    lastResponseFail[0] = rsp.toString();
                    refreshRestCookies();
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        }, TIMEOUT_EXCEPTION_MESSAGE + "\non folder: " + folderId + "\nwith response:\n" + lastResponseFail[0]);
    }

    public static void importApplicationData(String fileBody, String folderId) {
        importApplicationData(fileBody, folderId, STATUS_OK);
    }
}

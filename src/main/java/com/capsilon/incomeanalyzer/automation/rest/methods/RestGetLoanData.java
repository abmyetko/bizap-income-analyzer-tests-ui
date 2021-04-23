package com.capsilon.incomeanalyzer.automation.rest.methods;

import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.capsilon.test.ui.Retry;
import io.restassured.response.Response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static io.restassured.RestAssured.given;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestGetLoanData {

    //Folder entire IA response
    public static RestGetResponse getApplicationData(String folderId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_TEN_SECONDS, CommonMethods.TIMEOUT_TWO_MINUTES, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                        .get(REST_URL + "/bizapps/incomeanalyzer/application-refid/{folderId}")
                        .then().statusCode(STATUS_OK).extract().as(RestGetResponse.class));
    }

    public static Boolean getActuatorFeatureToggleValue(String propertyName) {
        final Response response = Retry.tryGet(CommonMethods.TIMEOUT_TEN_SECONDS, CommonMethods.TIMEOUT_TWO_MINUTES, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_PROPERTY_ID, propertyName)
                        .get(REST_URL + "/bizapps/incomeanalyzer/actuator/env/{propertyId}")
                        .then().extract().response());
        if (response.getStatusCode() != STATUS_OK)
            return false;
        return returnBooleanValue(response.jsonPath().get("property.value"));
    }

    public static String getActuatorFieldValue(String propertyName) {
        return Retry.tryGet(CommonMethods.TIMEOUT_TEN_SECONDS, CommonMethods.TIMEOUT_TWO_MINUTES, () ->
                given().spec(getRequestSpecBuilder())
                        .pathParam(PARAMETER_NAME_PROPERTY_ID, propertyName)
                        .get(REST_URL + "/bizapps/incomeanalyzer/actuator/env/{propertyId}")
                        .then().extract().response()).jsonPath().get("property.value");
    }

    public static Response getActuatorData() {
        return Retry.tryGet(CommonMethods.TIMEOUT_TEN_SECONDS, CommonMethods.TIMEOUT_TWO_MINUTES, () ->
                given().spec(getRequestSpecBuilder())
                        .get(REST_URL + "/bizapps/incomeanalyzer/actuator/env")
                        .then().statusCode(STATUS_OK).extract().response());
    }

    public static BufferedImage getSnippet(String folderId, String documentId, String snippetId) {
        final byte[][] response = new byte[1][1];
        Retry.whileTrue(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            response[0] = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .pathParam(PARAMETER_NAME_SNIPPET_ID, snippetId)
                    .pathParam(PARAMETER_NAME_DOC_ID, documentId)
                    .get(REST_URL + "/bizapps/incomeanalyzer/application-refid/{folderId}/document/{docId}/snippet/{snippetId}")
                    .then().statusCode(STATUS_OK)
                    .and().extract().body().asByteArray();
            return response[0] == null;
        }, "Snippet not received in time");
        try {
            return ImageIO.read(new ByteArrayInputStream(response[0]));
            //@formatter:off
        } catch (IOException ignore) {
        } //NOSONAR
        //@formatter:on
        return null;
    }

    public static BufferedImage getContextSnippet(String folderId, String documentId, String snippetId) {
        final byte[][] response = new byte[1][1];
        Retry.whileTrue(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () -> {
            response[0] = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .pathParam(PARAMETER_NAME_SNIPPET_ID, snippetId)
                    .pathParam(PARAMETER_NAME_DOC_ID, documentId)
                    .get(REST_URL + "/bizapps/incomeanalyzer/application-refid/{folderId}/document/{docId}/snippet/{snippetId}?contextRight=100&contextLeft=100&contextTop=50&contextBottom=50")
                    .then().statusCode(STATUS_OK)
                    .and().extract().body().asByteArray();
            return response[0] == null;
        }, "Context snippet not received in time");
        try {
            return ImageIO.read(new ByteArrayInputStream(response[0]));
            //@formatter:off
        } catch (IOException ignore) {
        } //NOSONAR
        //@formatter:on
        return null;
    }

    public static Response getEmployerAlias(Long folderId, String collaboratorId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
                        given().spec(getRequestSpecBuilder())
                                .pathParam(PARAMETER_NAME_ID, folderId)
                                .pathParam(PARAMETER_NAME_COLLABORATOR_ID, collaboratorId)
                                .get(REST_URL + "/employermatcher-api/folders/{folderId}/applicants/{collaboratorId}/employers")
                                .then().extract().response(),
                String.format("Employer aliases not received in time on folder: %s", folderId));
    }

    public static Response getFolderEmployerAlias(Long folderId) {
        return Retry.tryGet(CommonMethods.TIMEOUT_FIVE_SECONDS, CommonMethods.TIMEOUT_ONE_MINUTE, () ->
            given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_ID, folderId)
                    .get(REST_URL + "/employermatcher-api/folders/{folderId}/employers")
                    .then().extract().response(),
                String.format("All employer aliases not received in time on folder: %s", folderId));
    }

    private static Boolean returnBooleanValue(Object value) {
        if (value.getClass().equals(String.class))
            return Boolean.valueOf((String) value);
        else
            return (Boolean) value;
    }
}

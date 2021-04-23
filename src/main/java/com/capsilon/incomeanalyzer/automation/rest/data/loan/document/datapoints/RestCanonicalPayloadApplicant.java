package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.getGsonConfig;

@Data
@Accessors(chain = true)
public class RestCanonicalPayloadApplicant {

    private String id;
    private String collaboratorId;
    private RestCanonicalName name;
    private RestCanonicalRoleDetail roleDetail;
    private List<RestCanonicalTaxpayerIdentifier> taxpayerIdentifier;
    private List<RestCanonicalEmployer> employer;
    private List<RestCanonicalApplicantResidence> residence;
    private List<RestCanonicalIrsTaxDocument> irsTaxDocument;

    private RestCanonicalApplicantDetail applicantDetail;
    private List<RestCanonicalHousingExpense> housingExpense;
    private RestCanonicalIndividualExecution execution;
    private RestCanonicalEmployerIncomeItem incomeItem;
    private RestCanonicalDeclarationDetail declarationDetail;
    private List<RestCanonicalAddress> addressList;
    private RestCanonicalAddress addressSingular;

    @JsonProperty("address")
    @SerializedName("address")
    private Object addressObject;

    @JsonSetter("address")
    public void setAddress(JsonNode channelInternal) {
        String zipCode = "plusFourZipCode";
        if (channelInternal != null && channelInternal.size() > 1) {
            if (channelInternal.isArray()) {
                List<RestCanonicalAddress> canonicalAddresses = new ArrayList<>();
                for (JsonNode node : channelInternal) {
                    canonicalAddresses.add(new RestCanonicalAddress(
                            node.get("id").asText(null),
                            node.get("addressLineText").asText(null),
                            node.get("cityName").asText(null),
                            node.get("postalCode").asText(null),
                            node.get("stateCode").asText(null),
                            node.get(zipCode) == null ? null :
                                    node.get(zipCode).asText(null)));
                }
                this.addressList = new ArrayList<>(canonicalAddresses);
            } else if(channelInternal.isObject()){ //NOSONAR
                addressSingular = new RestCanonicalAddress(
                        channelInternal.get("id").asText(null),
                        channelInternal.get("addressLineText").asText(null),
                        channelInternal.get("cityName").asText(null),
                        channelInternal.get("postalCode").asText(null),
                        channelInternal.get("stateCode").asText(null),
                        channelInternal.get(zipCode) == null ? null :
                                channelInternal.get(zipCode).asText());
            }
        }
    }

    public void parseAllIncomeItemObjects() {
        employer.forEach(RestCanonicalEmployer::parseIncomeItemObject);
        if (incomeItem != null) {
            incomeItem.parseIncomeDocumentationObject();
        }
        parseAddressObject();
    }

    public void parseBackAllIncomeItems() {
        employer.forEach(RestCanonicalEmployer::parseBackIncomeItemObject);
        if (incomeItem != null) {
            incomeItem.parseIncomeDocumentationObject();
        }
        parseBackToIAddressObject();
    }

    public void parseAddressObject() {
        Gson gson = getGsonConfig();
        if (addressObject != null) {
            switch (addressObject.getClass().getName()) {
                case "java.util.ArrayList":
                    addressList = (List<RestCanonicalAddress>) addressObject;
                    break;
                case "com.google.gson.internal.LinkedTreeMap":
                    gson.fromJson(gson.toJson(addressObject), RestCanonicalAddress.class);
                    break;
                default:
                    break;
            }
        }
    }

    public void parseBackToIAddressObject() {
        if (addressSingular != null)
            addressObject = new RestCanonicalAddress(addressSingular);
        if (addressList != null)
            addressObject = new ArrayList<>(addressList);
        addressSingular = null;
        addressList = null;
    }
}

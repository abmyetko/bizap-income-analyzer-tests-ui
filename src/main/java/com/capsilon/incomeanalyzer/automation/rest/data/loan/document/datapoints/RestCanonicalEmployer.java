package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.getGsonConfig;

@Data
@Accessors(chain = true)
public class RestCanonicalEmployer {

    private String id;
    private RestCanonicalAddress address;
    private RestCanonicalEmployerLegalEntity legalEntity;
    private RestCanonicalEmployerEmployment employment;
    private RestCanonicalEmployerIncomeItem incomeItemSingular;
    private List<RestCanonicalEmployerIncomeItem> incomeItemList;

    private RestCanonicalEmployerIndividual individual;

    @JsonProperty("incomeItem")
    @SerializedName("incomeItem")
    private Object incomeItemObject;

    public void parseIncomeItemObject() {
        Gson gson = getGsonConfig();
        if (incomeItemObject != null) {
            switch (incomeItemObject.getClass().getName()) {
                case "java.util.ArrayList":
                    incomeItemList = Arrays.asList(gson.fromJson(gson.toJson(incomeItemObject), RestCanonicalEmployerIncomeItem[].class));
                    incomeItemList.forEach(RestCanonicalEmployerIncomeItem::parseIncomeDocumentationObject);
                    break;
                case "com.google.gson.internal.LinkedTreeMap":
                    incomeItemSingular = gson.fromJson(gson.toJson(incomeItemObject), RestCanonicalEmployerIncomeItem.class);
                    incomeItemSingular.parseIncomeDocumentationObject();
                    break;
                default:
                    break;
            }
        }
    }

    public List<RestCanonicalEmployerIncomeItem> getIncomeItemList() {
        if (incomeItemList == null) {
            Gson gson = getGsonConfig();
            incomeItemList = Arrays.asList(gson.fromJson(gson.toJson(incomeItemObject), RestCanonicalEmployerIncomeItem[].class));
            incomeItemList.forEach(RestCanonicalEmployerIncomeItem::parseIncomeDocumentationObject);
        }
        return incomeItemList;
    }

    public void parseBackIncomeItemObject() {
        if (incomeItemSingular != null) {
            incomeItemSingular.parseBackToIncomeDocumentationObject();
            incomeItemObject = new RestCanonicalEmployerIncomeItem(incomeItemSingular);
        }
        if (incomeItemList != null) {
            incomeItemList.forEach(RestCanonicalEmployerIncomeItem::parseBackToIncomeDocumentationObject);
            incomeItemObject = new ArrayList<>(incomeItemList);
        }
        incomeItemSingular = null;
        incomeItemList = null;
    }
}

package com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.getGsonConfig;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RestCanonicalEmployerIncomeItem {

    private String id;
    private List<RestCanonicalIncomeItemDetail> incomeItemDetail;
    private RestCanonicalIncomeItemSummary incomeItemSummary;
    private List<RestCanonicalIncomeDocumentation> incomeDocumentationList;
    private RestCanonicalIncomeDocumentation incomeDocumentationSingular;

    public RestCanonicalEmployerIncomeItem(RestCanonicalEmployerIncomeItem income) {
        this.id = income.id;
        this.incomeItemDetail = income.incomeItemDetail;
        this.incomeItemSummary = income.incomeItemSummary;
        this.incomeDocumentationList = income.incomeDocumentationList;
        this.incomeDocumentationSingular = income.incomeDocumentationSingular;
    }

    @JsonProperty("incomeDocumentation")
    @SerializedName("incomeDocumentation")
    private Object incomeDocumentationObject;

    @JsonSetter("incomeDocumentation")
    public void setIncomeDocumentation(JsonNode channelInternal) {
        if (channelInternal != null && channelInternal.size() > 1) {
            if (channelInternal.isArray()) {
                List<RestCanonicalIncomeDocumentation> incomeDocList = new ArrayList<>();
                for (JsonNode node : channelInternal) {
                    incomeDocList.add(new RestCanonicalIncomeDocumentation(
                            node.get("id").asText(),
                            node.get("documentPeriodStartDate").asText(),
                            node.get("documentPeriodEndDate").asText(),
                            node.get("incomePayCheckDate").asText()));
                }
                incomeDocumentationList = new ArrayList<>(incomeDocList);
            } else if (channelInternal.isObject()) { //NOSONAR
                incomeDocumentationSingular = new RestCanonicalIncomeDocumentation(
                        channelInternal.get("id").asText(),
                        channelInternal.get("documentPeriodStartDate").asText(),
                        channelInternal.get("documentPeriodEndDate").asText(),
                        channelInternal.get("incomePayCheckDate").asText());
            }
        }
    }

    public void parseIncomeDocumentationObject() {
        Gson gson = getGsonConfig();
        if (incomeDocumentationObject != null) {
            switch (incomeDocumentationObject.getClass().getName()) {
                case "java.util.ArrayList":
                    incomeDocumentationList = Arrays.asList(gson.fromJson(gson.toJson(incomeDocumentationObject), RestCanonicalIncomeDocumentation[].class));
                    break;
                case "com.google.gson.internal.LinkedTreeMap":
                    incomeDocumentationSingular = gson.fromJson(gson.toJson(incomeDocumentationObject), RestCanonicalIncomeDocumentation.class);
                    break;
                default:
                    break;
            }
        }
    }

    public void setIncomeItemListThruDate(String incomeThruDate) {
        this.incomeItemDetail.get(0).setEmploymentYearToDateIncomeThroughDate(incomeThruDate);
    }

    public void setIncomeDocumentationListThruDate(String incomeThruDate) {
        this.incomeDocumentationList.get(0).setIncomePayCheckDate(incomeThruDate);
    }

    public void setIncomeDocumentationListStartDate(Integer startDate) {
        this.incomeDocumentationList.get(0).setDocumentPeriodStartDateUsingInteger(startDate);
    }

    public void setIncomeDocumentationListStartDate(String startDate) {
        this.incomeDocumentationList.get(0).setDocumentPeriodStartDate(startDate);
    }

    public void parseBackToIncomeDocumentationObject() {
        if (incomeDocumentationSingular != null)
            incomeDocumentationObject = new RestCanonicalIncomeDocumentation(incomeDocumentationSingular);
        if (incomeDocumentationList != null)
            incomeDocumentationObject = new ArrayList<>(incomeDocumentationList);
        incomeDocumentationSingular = null;
        incomeDocumentationList = null;
    }
}

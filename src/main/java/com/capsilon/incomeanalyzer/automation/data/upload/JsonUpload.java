package com.capsilon.incomeanalyzer.automation.data.upload;

import com.capsilon.automation.dv.helpers.AsyncDVClient;
import com.capsilon.incomeanalyzer.automation.data.upload.data.*;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.imports.JsonDocument;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestUploadData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.DocumentFieldNames;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.STATUS_OK;
import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.getGsonConfig;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DocumentFieldNames.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;

@SuppressWarnings({"squid:S00107", "rawtypes"})
public class JsonUpload extends DataUploadObject<JsonUpload> {

    public static final String DEFAULT_REFID = "6666666";

    public JsonUpload(IAFolderBuilder iafolderBuilder, AsyncDVClient dvFolderClient) {
        super(iafolderBuilder, dvFolderClient);
    }

    public JsonUpload importDocument(DocumentData document, String folderId, int expectedStatusCode) {
        List<JsonDocument> oneDocList = new ArrayList<>();
        oneDocList.add(createJsonDocument(document));
        RestUploadData.importApplicationData(getGsonConfig().toJson(oneDocList), folderId, expectedStatusCode);
        return this;
    }

    public JsonUpload importDocument(DocumentData document, String folderId) {
        importDocument(document, folderId, STATUS_OK);
        return this;
    }

    public JsonUpload importDocumentList(Gson gsonConfig) {
        importDocumentList(gsonConfig, folderId);
        return this;
    }

    public JsonUpload importDocumentList(Gson gsonConfig, String folderId) {
        List<JsonDocument> jsonDocumentList = new ArrayList<>();
        documentList.forEach(document -> jsonDocumentList.add(createJsonDocument(document)));
        RestUploadData.importApplicationData(gsonConfig.toJson(jsonDocumentList), folderId);
        return this;
    }

    public JsonUpload removeDocumentsFromFolder() {
        RestChangeRequests.cleanJsonDocuments(folderId);
        return this;
    }

    private JsonDocument createJsonDocument(DocumentData document) {
        JsonDocument jsonDocument;
        if (PaystubData.class.equals(document.getClass())) {
            jsonDocument = createJsonPaystub((PaystubData) document);
        } else if (W2Data.class.equals(document.getClass())) {
            jsonDocument = createJsonW2((W2Data) document);
        } else if (VoeCurrentData.class.equals(document.getClass())) {
            jsonDocument = createJsonVoeCurrent((VoeCurrentData) document);
        } else if (VoePreviousData.class.equals(document.getClass())) {
            jsonDocument = createJsonVoePrevious((VoePreviousData) document);
        } else if (VoeFullData.class.equals(document.getClass())) {
            jsonDocument = createJsonVoeFull((VoeFullData) document);
        } else if (EVoeCurrentData.class.equals(document.getClass())) {
            jsonDocument = createJsonEVoeCurrent((EVoeCurrentData) document);
        } else {
            jsonDocument = createJsonEVoePrevious((EVoePreviousData) document);
        }
        return jsonDocument;
    }

    @SuppressWarnings("squid:MethodCyclomaticComplexity")
    private JsonDocument createJsonPaystub(PaystubData paystubData) {
        JsonDocument jsonDocument = new JsonDocument(PAYSTUB, paystubData.getDocumentName());
        addFieldIfValueExist(jsonDocument, DEFAULT_REFID, FULL_NAME.value, paystubData.getBorrowerFullName())
                .addFieldWithCollaboratorId(jsonDocument, DEFAULT_REFID, TAXPAYER_IDENTIFIER_VALUE, paystubData.getBorrowerSSN(), paystubData.getCollaboratorId())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYER_NAME.value, paystubData.getEmployerName())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, DOCUMENT_PERIOD_START_DATE.value, paystubData.getStartDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, DOCUMENT_PERIOD_END_DATE.value, paystubData.getEndDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, DOCUMENT_PAYCHECK_DATE.value, paystubData.getPayDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_ITEM_MANUAL_FREQUENCY.value, paystubData.getManualFrequency())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_ITEM_EXPLICIT_FREQUENCY.value, paystubData.getExplicitFrequency())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_ITEM_YTD_GROSS_INCOME_AMOUNT.value, paystubData.getYtdGrossIncomeAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_ITEM_PAY_TYPE.value, paystubData.getPayType());

        for (PaystubData.PaystubIncomeRow incomeRow : paystubData.getIncomes()) {
            String periodName = "";
            String ytdName = "";

            switch (incomeRow.getIncomeGroup()) {
                case REGULAR:
                case PTO:
                case HOLIDAY:
                    periodName = INCOME_ITEM_AMOUNT.value;
                    ytdName = INCOME_ITEM_YEAR_TO_DATE_AMOUNT.value;
                    break;
                case OVERTIME:
                case OT:
                case HOLIDAY_OT:
                    periodName = INCOME_ITEM_OVERTIME_AMOUNT.value;
                    ytdName = INCOME_ITEM_OVERTIME_YEAR_TO_DATE_AMOUNT.value;
                    break;
                case COMMISSION:
                case COMMISSIONS:
                case COMM:
                    periodName = INCOME_ITEM_COMMISSIONS_AMOUNT.value;
                    ytdName = INCOME_ITEM_COMMISSIONS_YEAR_TO_DATE_AMOUNT.value;
                    break;
                case BONUS:
                case INCENTIVE:
                case MONTHLY_BONUS:
                    periodName = INCOME_ITEM_BONUS_AMOUNT.value;
                    ytdName = INCOME_ITEM_BONUS_YEAR_TO_DATE_AMOUNT.value;
                    break;
            }
            addDuplicatedFieldIfValueExist(jsonDocument, DEFAULT_REFID, periodName, incomeRow.getPeriodAmount(), incomeRow.getIncomeGroup())
                    .addDuplicatedFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_ITEM_PERIOD_HOURS.value, incomeRow.getHours(), incomeRow.getIncomeGroup())
                    .addDuplicatedFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_ITEM_PERIOD_RATE.value, incomeRow.getRate(), incomeRow.getIncomeGroup())
                    .addDuplicatedFieldIfValueExist(jsonDocument, DEFAULT_REFID, ytdName, incomeRow.getYearToDateAmount(), incomeRow.getIncomeGroup());
        }

        return jsonDocument;
    }

    public JsonDocument createJsonW2(W2Data w2Data) {
        JsonDocument jsonDocument = new JsonDocument(W2, w2Data.getDocumentName());
        addFieldIfValueExist(jsonDocument, DEFAULT_REFID, FULL_NAME.value, w2Data.getBorrowerFullName())
                .addFieldWithCollaboratorId(jsonDocument, DEFAULT_REFID, TAXPAYER_IDENTIFIER_VALUE, w2Data.getBorrowerSSN(), w2Data.getCollaboratorId())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYER_NAME.value, w2Data.getEmployerName())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, WAGES_AND_OTHER_COMPENSATION_AMOUNT.value, w2Data.getWagesTipsOtherCompensation())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, DOCUMENT_PERIOD_START_DATE.value, w2Data.getYear())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, ALLOCATED_TIPS.value, w2Data.getAllocatedTips())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, TOTAL_TIPS_REPORTED_TO_EMPLOYER_AMOUNT.value, w2Data.getSocialSecurityTips())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, MEDICARE_WAGES_AND_TIPS_AMOUNT.value, w2Data.getMedicareWages());

        return jsonDocument;
    }

    public JsonDocument createJsonVoeCurrent(VoeCurrentData voeCurrentData) {
        JsonDocument jsonDocument = new JsonDocument(VOE, voeCurrentData.getDocumentName());
        addFieldIfValueExist(jsonDocument, DEFAULT_REFID, FULL_NAME.value, voeCurrentData.getBorrowerFullName())
                .addFieldWithCollaboratorId(jsonDocument, DEFAULT_REFID, EMPLOYEE_SSN, voeCurrentData.getBorrowerSSN(), voeCurrentData.getCollaboratorId())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYER_NAME.value, voeCurrentData.getEmployerName())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PAYMENT_PERIOD_TYPE.value, voeCurrentData.getFrequency().toString())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_START_DATE.value, voeCurrentData.getEmploymentStartDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_INCOME_THROUGH_DATE.value, voeCurrentData.getYtdIncomeThruDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EXECUTION_DATE.value, voeCurrentData.getSignatureDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, AVG_HOURS_PER_WEEK.value, voeCurrentData.getAvgHoursPerWeek())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, BASE_INCOME_AMOUNT.value, voeCurrentData.getCurrentGrossBasePayAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BASE_INCOME_AMOUNT.value, voeCurrentData.getYtdBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_COMMISSIONS_INCOME_AMOUNT.value, voeCurrentData.getYtdCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_OVERTIME_INCOME_AMOUNT.value, voeCurrentData.getYtdOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BONUS_INCOME_AMOUNT.value, voeCurrentData.getYtdBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_TOTAL_INCOME_AMOUNT.value, voeCurrentData.getYtdTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BASE_INCOME_AMOUNT.value, voeCurrentData.getPriorYearBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, voeCurrentData.getPriorYearCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, voeCurrentData.getPriorYearOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, voeCurrentData.getPriorYearBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, voeCurrentData.getPriorYearTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BASE_INCOME_AMOUNT.value, voeCurrentData.getTwoYearPriorBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, voeCurrentData.getTwoYearPriorCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, voeCurrentData.getTwoYearPriorOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, voeCurrentData.getTwoYearPriorBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, voeCurrentData.getTwoYearPriorTotal());

        return jsonDocument;
    }

    public JsonDocument createJsonVoePrevious(VoePreviousData voePreviousData) {
        JsonDocument jsonDocument = new JsonDocument(VOE, voePreviousData.getDocumentName());
        addFieldIfValueExist(jsonDocument, DEFAULT_REFID, FULL_NAME.value, voePreviousData.getBorrowerFullName())
                .addFieldWithCollaboratorId(jsonDocument, DEFAULT_REFID, EMPLOYEE_SSN, voePreviousData.getBorrowerSSN(), voePreviousData.getCollaboratorId())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYER_NAME.value, voePreviousData.getEmployerName())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EXECUTION_DATE.value, voePreviousData.getSignatureDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_PAYMENT_PERIOD_TYPE.value, voePreviousData.getPeriodType().toString())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_START_DATE.value, voePreviousData.getHiredDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_END_DATE.value, voePreviousData.getTerminatedDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_BASE_INCOME_AMOUNT.value, voePreviousData.getBaseWageAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_OVERTIME_INCOME_AMOUNT.value, voePreviousData.getOvertimeWageAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_COMMISSIONS_INCOME_AMOUNT.value, voePreviousData.getCommissionWageAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_BONUS_INCOME_AMOUNT.value, voePreviousData.getBonusWageAmount());

        return jsonDocument;
    }

    public JsonDocument createJsonVoeFull(VoeFullData voeFullData) {
        JsonDocument jsonDocument = new JsonDocument(VOE, voeFullData.getDocumentName());
        addFieldIfValueExist(jsonDocument, DEFAULT_REFID, FULL_NAME.value, voeFullData.getBorrowerFullName())
                .addFieldWithCollaboratorId(jsonDocument, DEFAULT_REFID, EMPLOYEE_SSN, voeFullData.getBorrowerSSN(), voeFullData.getCollaboratorId())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYER_NAME.value, voeFullData.getEmployerName())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PAYMENT_PERIOD_TYPE.value, voeFullData.getFrequency().toString())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_START_DATE.value, voeFullData.getEmploymentStartDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_INCOME_THROUGH_DATE.value, voeFullData.getYtdIncomeThruDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EXECUTION_DATE.value, voeFullData.getSignatureDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, AVG_HOURS_PER_WEEK.value, voeFullData.getAvgHoursPerWeek())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, BASE_INCOME_AMOUNT.value, voeFullData.getCurrentGrossBasePayAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BASE_INCOME_AMOUNT.value, voeFullData.getYtdBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_COMMISSIONS_INCOME_AMOUNT.value, voeFullData.getYtdCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_OVERTIME_INCOME_AMOUNT.value, voeFullData.getYtdOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BONUS_INCOME_AMOUNT.value, voeFullData.getYtdBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_TOTAL_INCOME_AMOUNT.value, voeFullData.getYtdTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BASE_INCOME_AMOUNT.value, voeFullData.getPriorYearBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, voeFullData.getPriorYearCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, voeFullData.getPriorYearOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, voeFullData.getPriorYearBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, voeFullData.getPriorYearTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BASE_INCOME_AMOUNT.value, voeFullData.getTwoYearPriorBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, voeFullData.getTwoYearPriorCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, voeFullData.getTwoYearPriorOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, voeFullData.getTwoYearPriorBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, voeFullData.getTwoYearPriorTotal())

                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_PAYMENT_PERIOD_TYPE.value, voeFullData.getPeriodType().toString())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_START_DATE.value, voeFullData.getHiredDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_END_DATE.value, voeFullData.getTerminatedDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_BASE_INCOME_AMOUNT.value, voeFullData.getBaseWageAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_OVERTIME_INCOME_AMOUNT.value, voeFullData.getOvertimeWageAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_COMMISSIONS_INCOME_AMOUNT.value, voeFullData.getCommissionWageAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_PREVIOUS_BONUS_INCOME_AMOUNT.value, voeFullData.getBonusWageAmount());

        return jsonDocument;
    }

    public JsonDocument createJsonEVoeCurrent(EVoeCurrentData evoeCurrentData) {
        JsonDocument jsonDocument = new JsonDocument(EVOE, evoeCurrentData.getDocumentName());
        addFieldIfValueExist(jsonDocument, DEFAULT_REFID, FULL_NAME.value, evoeCurrentData.getBorrowerFullName())
                .addFieldWithCollaboratorId(jsonDocument, DEFAULT_REFID, EMPLOYEE_SSN, evoeCurrentData.getBorrowerSSN(), evoeCurrentData.getCollaboratorId())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYER_NAME.value, evoeCurrentData.getEmployerName())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_PAY_RATE_FREQUENCY_TYPE.value, evoeCurrentData.getPayRateFrequency().toString())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_PAY_RATE_AMOUNT.value, evoeCurrentData.getRate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_FREQUENCY_TYPE.value, evoeCurrentData.getFrequency().toString())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_AVG_HOURS_PER_PERIOD.value, evoeCurrentData.getAvgHoursPerPeriod())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_START_DATE.value, evoeCurrentData.getOriginalHireDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_INCOME_THROUGH_DATE.value, evoeCurrentData.getIncomeThruDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, VERIFICATION_DATE.value, evoeCurrentData.getSignatureDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, BASE_INCOME_AMOUNT.value, evoeCurrentData.getCurrentGrossBasePayAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BASE_INCOME_AMOUNT.value, evoeCurrentData.getYtdBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_COMMISSIONS_INCOME_AMOUNT.value, evoeCurrentData.getYtdCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_OVERTIME_INCOME_AMOUNT.value, evoeCurrentData.getYtdOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BONUS_INCOME_AMOUNT.value, evoeCurrentData.getYtdBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_TOTAL_INCOME_AMOUNT.value, evoeCurrentData.getYtdTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BASE_INCOME_AMOUNT.value, evoeCurrentData.getPriorYearBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, evoeCurrentData.getPriorYearCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, evoeCurrentData.getPriorYearOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, evoeCurrentData.getPriorYearBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, evoeCurrentData.getPriorYearTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BASE_INCOME_AMOUNT.value, evoeCurrentData.getTwoYearPriorBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, evoeCurrentData.getTwoYearPriorCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, evoeCurrentData.getTwoYearPriorOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, evoeCurrentData.getTwoYearPriorBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, evoeCurrentData.getTwoYearPriorTotal());

        return jsonDocument;
    }

    public JsonDocument createJsonEVoePrevious(EVoePreviousData eVoePreviousData) {
        JsonDocument jsonDocument = new JsonDocument(EVOE, eVoePreviousData.getDocumentName());
        addFieldIfValueExist(jsonDocument, DEFAULT_REFID, FULL_NAME.value, eVoePreviousData.getBorrowerFullName())
                .addFieldWithCollaboratorId(jsonDocument, DEFAULT_REFID, EMPLOYEE_SSN, eVoePreviousData.getBorrowerSSN(), eVoePreviousData.getCollaboratorId())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYER_NAME.value, eVoePreviousData.getEmployerName())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_PAY_RATE_AMOUNT.value, eVoePreviousData.getRate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_AVG_HOURS_PER_PERIOD.value, eVoePreviousData.getAvgHoursPerPeriod())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_START_DATE.value, eVoePreviousData.getOriginalHireDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, EMPLOYMENT_END_DATE.value, eVoePreviousData.getEndDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_INCOME_THROUGH_DATE.value, eVoePreviousData.getIncomeThruDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, VERIFICATION_DATE.value, eVoePreviousData.getSignatureDate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, BASE_INCOME_AMOUNT.value, eVoePreviousData.getCurrentGrossBasePayAmount())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BASE_INCOME_AMOUNT.value, eVoePreviousData.getYtdBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_COMMISSIONS_INCOME_AMOUNT.value, eVoePreviousData.getYtdCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_OVERTIME_INCOME_AMOUNT.value, eVoePreviousData.getYtdOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_BONUS_INCOME_AMOUNT.value, eVoePreviousData.getYtdBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, YEAR_TO_DATE_TOTAL_INCOME_AMOUNT.value, eVoePreviousData.getYtdTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BASE_INCOME_AMOUNT.value, eVoePreviousData.getPriorYearBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, eVoePreviousData.getPriorYearCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, eVoePreviousData.getPriorYearOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, eVoePreviousData.getPriorYearBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, eVoePreviousData.getPriorYearTotal())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BASE_INCOME_AMOUNT.value, eVoePreviousData.getTwoYearPriorBasePay())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_COMMISSIONS_INCOME_AMOUNT.value, eVoePreviousData.getTwoYearPriorCommission())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_OVERTIME_INCOME_AMOUNT.value, eVoePreviousData.getTwoYearPriorOvertime())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_BONUS_INCOME_AMOUNT.value, eVoePreviousData.getTwoYearPriorBonus())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, SECOND_PRIOR_YEAR_TOTAL_INCOME_AMOUNT.value, eVoePreviousData.getTwoYearPriorTotal())

                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_PAY_RATE_AMOUNT.value, eVoePreviousData.getRate())
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_FREQUENCY_TYPE.value, eVoePreviousData.getFrequency() != null ? eVoePreviousData.getFrequency().toString() : null)
                .addFieldIfValueExist(jsonDocument, DEFAULT_REFID, INCOME_PAY_RATE_FREQUENCY_TYPE.value, eVoePreviousData.getPayRateFrequency() != null ? eVoePreviousData.getPayRateFrequency().toString() : null);

        return jsonDocument;
    }

    private JsonUpload addDuplicatedFieldIfValueExist(JsonDocument document, String refId, String name, String value, PaystubIncomeGroups container) {
        if (refId != null && name != null && value != null && !"".equals(value)) {
            document.setDuplicatedField(refId, name, value, container);
        }
        return this;
    }

    private JsonUpload addFieldIfValueExist(JsonDocument document, String refId, String name, String value) {
        addFieldIfValueExist(document, refId, name, value, null);
        return this;
    }

    private JsonUpload addFieldIfValueExist(JsonDocument document, String refId, String name, String value, PaystubIncomeGroups container) {
        if (refId != null && name != null && value != null) {
            document.setField(refId, name, value, container);
        }
        return this;
    }

    private JsonUpload addFieldWithCollaboratorId(JsonDocument document, String refId, DocumentFieldNames name, String value, String collaboratorId) {
        if (refId != null && name != null && value != null) {
            document.setSSNWithCollabId(refId, name, value, collaboratorId);
        }
        return this;
    }
}

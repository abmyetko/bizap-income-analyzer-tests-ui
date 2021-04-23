package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Strings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.getGsonConfig;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("squid:S1448")
@Data
@Accessors(chain = true)
public class RestGetResponse {

    private List<RestGetResponseApplicant> applicants = new ArrayList<>();
    private RestGetResponseCalculatedBy calculatedBy;
    private String calculationDate;
    private Map<String, RestGetResponseDocument> documents;
    private Map<String, Boolean> featureToggles;
    private String executionDate;
    private RestGetResponseWorksheet worksheet;
    private Long id;
    private String lastStatusModification;
    private String mortgageType;
    private String refId;
    private Map<String, RestGetRules> rules;
    private String siteGUID;
    private String status;
    private Integer failedRuleCount;
    private RestGetResponseTotalQualifyingIncome totalQualifyingIncome;
    private Boolean touchless;
    private String urlaVersion;

    public RestGetResponseApplicant getApplicant(String firstName) {
        for (RestGetResponseApplicant applicant : applicants) {
            if (firstName.equalsIgnoreCase(applicant.getFirstName()))
                return applicant;
        }
        return null;
    }

    public RestGetResponseDocument getDocumentById(Long id) {
        return getDocumentById(id.toString());
    }

    public RestGetResponseDocument getDocumentById(String id) {
        return getDocuments().get(id);
    }

    public RestGetResponseDocument getDocumentByDataSource(String dataSourceId) {
        return documents.entrySet().stream().filter(doc ->
                dataSourceId.equalsIgnoreCase(doc.getValue().getDataSourceId())).findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    public Long getNotFNMDocumentCount() {
        return getDocuments().values().stream().filter(document -> !"LOAN_APP_FNM_CANONICAL".equals(document.getType()))
                .count();
    }

    public Long getDocumentTypeCount(SummaryDocumentType docType) {
        return getDocuments().values().stream().filter(document -> docType.toString().equals(document.getType()))
                .count();
    }

    public List<RestGetRules> getFailedRules() {
        List<RestGetRules> failedRulesList = new ArrayList<>();
        if (rules != null) {
            for (Map.Entry<String, RestGetRules> rule : rules.entrySet()) {
                if ("failed".equalsIgnoreCase(rule.getValue().getStatus()))
                    failedRulesList.add(rule.getValue());
            }
        }
        return failedRulesList;
    }

    public List<String> getApplicantNames() {
        List<String> applicantNames = new ArrayList<>();
        applicants.forEach(applicant -> applicantNames.add(applicant.getFirstName()));
        return applicantNames;
    }

    public List<String> getCollaboratorIds() {
        List<String> collaboratorIds = new ArrayList<>();
        applicants.forEach(applicant -> collaboratorIds.add(applicant.getRefId()));
        return collaboratorIds;
    }

    public Map<IncomePartType, BigDecimal> getPrimaryDeclaredIncomeForLoanDocumentType(String firstName) {
        if ("URLA_2009".equals(urlaVersion)) {
            return getApplicant(firstName).getDeclaredIncome();
        } else {
            return getApplicant(firstName).getPrimaryIncome().getDeclaredIncome();
        }
    }

    public BigDecimal getSecondaryJobMonthlyIncomeValue(String firstName, String employerName) {
        if ("URLA_2009".equals(urlaVersion)) {
            return getApplicant(firstName).getIncome(employerName).getEmployer().getIncomeAmount();
        } else {
            return getApplicant(firstName).getIncome(employerName).getDeclaredIncome().get(IncomePartType.BASE_PAY);
        }
    }

    public List<FailureComparison> compareTo(RestGetResponse actual) {
        List<FailureComparison> failList = new ArrayList<>();
        try {
            failList.add(new FailureComparison("status", this.getStatus(), actual.getStatus()));
            failList.add(new FailureComparison("touchless", this.getTouchless(), actual.getTouchless()));
            failList.addAll(this.getTotalQualifyingIncome().compareTo(actual.getTotalQualifyingIncome()));
            for (RestGetResponseApplicant thisApplicant : this.getApplicants()) {
                if (actual.getApplicant(thisApplicant.getFirstName()) != null)
                    failList.addAll(thisApplicant.compareTo(actual.getApplicant(thisApplicant.getFirstName()), thisApplicant.getFirstName()));
                else
                    failList.add(new FailureComparison(String.format("applicants(%s).firstName", thisApplicant.getFirstName()),
                            thisApplicant.getFirstName(),
                            StringUtils.join(actual.getApplicantNames(), "|")));
            }
        } catch (NullPointerException e) { //NOSONAR
            e.printStackTrace(); //NOSONAR
            fail(String.format("Null pointer exception. response is not equal to template. \nFound issues:\n %s \nExpected:\n %s \nActual:\n %s", //NOSONAR
                    failList.toString(),
                    getGsonConfig().toJson(this),
                    getGsonConfig().toJson(actual)));
        }
        failList.removeIf(failure -> Strings.isNullOrEmpty(failure.getFieldName()));
        return failList;

    }
}

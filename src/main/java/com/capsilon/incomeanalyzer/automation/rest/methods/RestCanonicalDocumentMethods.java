package com.capsilon.incomeanalyzer.automation.rest.methods;

import com.capsilon.incomeanalyzer.automation.rest.RestCommons;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalEmployerIncomeItem;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.ui.Retry;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.capsilon.common.endpoint.FlamsEndpoint.getDvFolderId;
import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.IATestResources.toAbsolutePath;
import static io.restassured.RestAssured.given;

@NoArgsConstructor
public final class RestCanonicalDocumentMethods {

    private int documentIdCounter;
    private int dataSourceIdCounter;

    //split into smaller chunks ie. getJob getBorrower get dateForDocAndYear AND make distinction between current and previous voe
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S109"})
    public Response uploadDefaultCanonicalDocument(IAFolderBuilder builder, Boolean primaryBorrower, Boolean currentJob, SummaryDocumentType documentType, Integer mostRecentYear) {
        final String LAST_MARCH = "-03-31";
        String borrower = primaryBorrower ? builder.getBorrowerFullName() : builder.getCoBorrowerFullName();
        String job = primaryBorrower ?
                currentJob ? builder.getBorrowerCurrentEmployment() : builder.getBorrowerPreviousEmployment() :
                currentJob ? builder.getCoBorrowerCurrentEmployment() : builder.getCoBorrowerPreviousEmployment();
        String start = "";
        String end = "";
        String exec = "";
        switch (documentType) {
            case PAYSTUB:
                start = mostRecentYear + (builder.getSignDate().contains(mostRecentYear.toString()) ? "-03-01" : "-12-01");
                end = mostRecentYear + (builder.getSignDate().contains(mostRecentYear.toString()) ? LAST_MARCH : "-12-31");
                exec = mostRecentYear + (builder.getSignDate().contains(mostRecentYear.toString()) ? LAST_MARCH : "-12-31");
                break;
            case VOE:
            case VOE_PREVIOUS:
                start = (mostRecentYear - 3) + "-01-01";
                end = mostRecentYear + LAST_MARCH;
                exec = mostRecentYear + LAST_MARCH;
                break;
            case W2:
                start = mostRecentYear.toString();
                break;
            default:
                break;
        }
        return uploadDefaultCanonicalDocument(builder.getFolderId(),
                borrower,
                job,
                documentType,
                start,
                end,
                exec);
    }

    public Response uploadDefaultCanonicalDocument(String folderId, String borrowerName, String jobName, SummaryDocumentType documentType,
                                                   String startDate, String endDate, String executionDate) {
        RestCanonicalDocument canonicalDocument = generateDefaultCanonicalDocumentFromTemplate(folderId, documentType);
        setBorrowerAndJob(canonicalDocument, folderId, borrowerName, jobName);
        setDatesForSelectedDocument(canonicalDocument, startDate, endDate, executionDate, documentType);
        return uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());
    }

    public void uploadCanonicalDocumentList(Iterable<RestCanonicalDocument> document, String siteGuid) {
        for (RestCanonicalDocument doc : document)
            uploadCanonicalDocument(doc, siteGuid);
    }

    public Response uploadCanonicalDocument(RestCanonicalDocument document, String siteGuid) {
        if (document.getCanonicalPayload() != null)
            document.getCanonicalPayload().parseBackAllIncomeItems();
        return uploadCanonicalDocument(getGsonConfig().toJson(document), siteGuid);
    }

    public Response uploadCanonicalDocument(String document, String siteGuid) {
        final Response[] response = new Response[1];
        Retry.whileTrue(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            Response rsp = given().spec(new RequestSpecBuilder()
                    .setContentType(CONTENT_TYPE_JSON)
                    .addHeader("x-site-guid", siteGuid)
                    .addCookies(getRestCookies())
                    .build())
                    .body(document)
                    .post(REST_URL + "/mdm_converter-api/trigger/canonicalDocumentsStorage");
            if (rsp.getStatusCode() != STATUS_OK) {
                refreshRestCookies();
                return true;
            } else {
                response[0] = rsp;
                return false;
            }
        }, String.format("Canonical document upload has failed on folder: %s", siteGuid));
        return response[0];
    }

    public RestCanonicalDocument setBorrowerAndJob(RestCanonicalDocument document, String folderId, String borrowerName, String jobName) {
        RestGetResponse response = getFolderResponse(folderId);
        document.getCanonicalPayload().getApplicant().get(0).setCollaboratorId(response.getApplicant(borrowerName).getRefId());
        document.getCanonicalPayload().getApplicant().get(0).getName().setFirstName(borrowerName.substring(0, borrowerName.indexOf(" ")));
        document.getCanonicalPayload().getApplicant().get(0).getName().setLastName(borrowerName.substring(borrowerName.indexOf(" ") + 1));
        document.getCanonicalPayload().getApplicant().get(0).getName().setFullName(borrowerName);
        document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getLegalEntity().getLegalEntityDetail().setFullName(jobName);
        return document;
    }

    public RestCanonicalDocument setOrphanDoc(RestCanonicalDocument document) {
        document.getCanonicalPayload().getApplicant().get(0).setCollaboratorId("");
        document.getCanonicalPayload().getApplicant().get(0).getName().setFirstName("");
        document.getCanonicalPayload().getApplicant().get(0).getName().setLastName("");
        document.getCanonicalPayload().getApplicant().get(0).getName().setFullName("");
        document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getLegalEntity().getLegalEntityDetail().setFullName("NoName Employer");
        return document;
    }

    public RestCanonicalDocumentMethods setDatesForSelectedDocument(RestCanonicalDocument document, String startDate, String endDate, String executionDate, SummaryDocumentType documentType) {
        switch (documentType) {
            case PAYSTUB:
                setPaystubDates(document, startDate, endDate, executionDate);
                break;
            case W2:
                setW2Dates(document, startDate);
                break;
            case VOE:
                setCurrentVoeDates(document, startDate, executionDate);
                break;
            case VOE_PREVIOUS:
                setPreviousVoeDates(document, startDate, endDate, executionDate);
                break;
            default:
                break;
        }
        return this;
    }

    public RestCanonicalDocumentMethods setPaystubDates(RestCanonicalDocument document, String startDate, String endDate, String executionDate) {
        RestCanonicalEmployerIncomeItem income = document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList().get(0);
        if (income.getIncomeDocumentationSingular() != null) {
            if (startDate != null)
                income.getIncomeDocumentationSingular().setDocumentPeriodStartDate(startDate);
            if (endDate != null)
                income.getIncomeDocumentationSingular().setDocumentPeriodEndDate(endDate);
            if (executionDate != null)
                income.getIncomeDocumentationSingular().setIncomePayCheckDate(executionDate);
        }
        return this;
    }

    public RestCanonicalDocumentMethods setW2Dates(RestCanonicalDocument document, String date) {
        if (date != null)
            document.getCanonicalPayload().getApplicant().get(0).getIrsTaxDocument().get(0)
                    .getIrsTaxDocumentDocumentationDetail().setDocumentPeriodStartDate(date.split("-")[0]);
        return this;
    }

    public RestCanonicalDocumentMethods setPreviousVoeDates(RestCanonicalDocument document, String startDate, String endDate, String executionDate) {
        if (executionDate != null) {
            document.getCanonicalPayload().getLoanOriginator().getExecution().getExecutionDetail().setExecutionDate(executionDate);
            document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIndividual().getExecution().getExecutionDetail().setExecutionDate(executionDate);
        }
        if (startDate != null)
            document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(ONE).getEmployment().setEmploymentStartDate(startDate);
        if (endDate != null)
            document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(ONE).getEmployment().setEmploymentEndDate(endDate);
        return this;
    }

    public RestCanonicalDocumentMethods setCurrentVoeDates(RestCanonicalDocument document, String startDate, String executionDate) {
        if (executionDate != null) {
            document.getCanonicalPayload().getLoanOriginator().getExecution().getExecutionDetail().setExecutionDate(executionDate);
            document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIndividual().getExecution().getExecutionDetail().setExecutionDate(executionDate);

            List<RestCanonicalEmployerIncomeItem> incomeItems = document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList();
            Integer priorYear = Integer.parseInt(executionDate.split("-")[0]) - ONE;
            Integer twoYearPrior = Integer.parseInt(executionDate.split("-")[0]) - TWO;

            incomeItems.get(ONE).setIncomeItemListThruDate(executionDate);
            incomeItems.get(TWO).setIncomeItemListThruDate(executionDate);
            incomeItems.get(THREE).setIncomeItemListThruDate(executionDate);
            incomeItems.get(FOUR).setIncomeItemListThruDate(executionDate);
            incomeItems.get(FIVE).setIncomeItemListThruDate(executionDate);

            incomeItems.get(SIX).setIncomeDocumentationListStartDate(priorYear);
            incomeItems.get(SEVEN).setIncomeDocumentationListStartDate(priorYear);
            incomeItems.get(EIGHT).setIncomeDocumentationListStartDate(priorYear);
            incomeItems.get(NINE).setIncomeDocumentationListStartDate(priorYear);
            incomeItems.get(TEN).setIncomeDocumentationListStartDate(priorYear);

            incomeItems.get(ELEVEN).setIncomeDocumentationListStartDate(twoYearPrior);
            incomeItems.get(TWELVE).setIncomeDocumentationListStartDate(twoYearPrior);
            incomeItems.get(THIRTEEN).setIncomeDocumentationListStartDate(twoYearPrior);
            incomeItems.get(FOURTEEN).setIncomeDocumentationListStartDate(twoYearPrior);
            incomeItems.get(FIFTEEN).setIncomeDocumentationListStartDate(twoYearPrior);
        }
        if (startDate != null)
            document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getEmployment().setEmploymentStartDate(startDate);
        return this;
    }

    public RestCanonicalDocumentMethods clearDefaultIncomeItems(RestCanonicalDocument document) {
        List<RestCanonicalEmployerIncomeItem> incomeItems;
        switch (document.getCanonicalDocumentType()) {
            case PAYSTUB:
                setPaystubBasePayValues(document, bigD(0), bigD(0), bigD(0), bigD(0));
                break;
            case W2:
                setW2BasePayValues(document, bigD(0), bigD(0), bigD(0), bigD(0), bigD(0));
                break;
            case VOE: //NOSONAR
                incomeItems = document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList();

                incomeItems.get(ZERO).getIncomeItemDetail().get(0).setIncomeFrequencyType("").setIncomeTypeTotalAmount(bigD(0));//voeCurr currGrossPay

                incomeItems.get(ONE).getIncomeItemSummary().setApplicantTotalYearToDateIncomeAmount(bigD(0));//voeCurr ytd total
                incomeItems.get(SIX).getIncomeItemSummary().setApplicantTotalYearToDateIncomeAmount(bigD(0));//voeCurr prev yr total
                incomeItems.get(ELEVEN).getIncomeItemSummary().setApplicantTotalYearToDateIncomeAmount(bigD(0));//voeCurr two yr prior total

                incomeItems.get(TWO).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr base ytd
                incomeItems.get(THREE).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr ot ytd
                incomeItems.get(FOUR).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr com ytd
                incomeItems.get(FIVE).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr bns ytd
                incomeItems.get(SEVEN).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr base prev yr
                incomeItems.get(EIGHT).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr ot prev yr
                incomeItems.get(NINE).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr com prev yr
                incomeItems.get(TEN).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr bns prev yr
                incomeItems.get(TWELVE).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr base two yr prior
                incomeItems.get(THIRTEEN).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr ot two yr prior
                incomeItems.get(FOURTEEN).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr com two yr prior
                incomeItems.get(FIFTEEN).getIncomeItemDetail().get(0).setIncomeTypeYearToDateAmount(bigD(0));//voeCurr bns two yr prior
                break;
            case VOE_PREVIOUS: //NOSONAR
                incomeItems = document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList();

                incomeItems.get(ZERO).getIncomeItemDetail().get(0).setIncomeFrequencyType("").setIncomeTypeTotalAmount(bigD(0));//voePrev base
                incomeItems.get(ONE).getIncomeItemDetail().get(0).setIncomeFrequencyType("").setIncomeTypeTotalAmount(bigD(0));//voePrev ot
                incomeItems.get(TWO).getIncomeItemDetail().get(0).setIncomeFrequencyType("").setIncomeTypeTotalAmount(bigD(0));//voePrev com
                incomeItems.get(THREE).getIncomeItemDetail().get(0).setIncomeFrequencyType("").setIncomeTypeTotalAmount(bigD(0));//voePrev bns
                break;
            default:
                break;
        }
        return this;
    }

    public RestCanonicalDocumentMethods setPaystubBasePayValues(RestCanonicalDocument document, BigDecimal rate, BigDecimal periodHours, BigDecimal totalAmount, BigDecimal ytdTotalAmount) {
        document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList().get(1).getIncomeItemDetail().get(0)
                .setIncomeTypeHourlyPayRatePercent(rate)
                .setIncomeTypePeriodHoursNumber(periodHours)
                .setIncomeTypeTotalAmount(totalAmount)
                .setIncomeTypeYearToDateAmount(ytdTotalAmount);
        return this;
    }

    public RestCanonicalDocumentMethods setW2BasePayValues(RestCanonicalDocument document, BigDecimal wagesAmount, BigDecimal allocatedTipsAmount,
                                                           BigDecimal medicareAmount, BigDecimal socialBenefitsAmount, BigDecimal totalTips) {
        document.getCanonicalPayload().getApplicant().get(0).getIrsTaxDocument().get(0).getIrsTaxDocumentData().getIrsTaxDocumentDataIncomeDetail()
                .setWagesSalariesTipsEtcAmount(wagesAmount)
                .setAllocatedTipsAmount(allocatedTipsAmount)
                .setMedicareWagesAndTipsAmount(medicareAmount)
                .setSocialSecurityBenefitsAmount(socialBenefitsAmount)
                .setTotalTipsReportedToEmployerAmount(totalTips);
        return this;
    }

    public RestCanonicalDocument generateDefaultCanonicalDocumentFromTemplate(String folderId, String relativePath) {
        RestCanonicalDocument[] canonicalDocument = new RestCanonicalDocument[0];
        Gson gson = RestCommons.getGsonConfig();
        try {
            canonicalDocument = gson.fromJson(new JsonReader(new FileReader(new File(toAbsolutePath(relativePath)))), RestCanonicalDocument[].class); //NOSONAR
            //@formatter:off
        } catch (IOException ignore) {}//NOSONAR
        //@formatter:on

        setCanonicalDocumentSiteAndFolderIdFields(canonicalDocument[0], folderId);

        dataSourceIdCounter++;
        documentIdCounter++;

        canonicalDocument[0].getCanonicalPayload().parseAllIncomeItemObjects();

        return canonicalDocument[0];
    }

    public RestCanonicalDocument generateDefaultCanonicalDocumentFromTemplate(String folderId, SummaryDocumentType documentType) {
        RestCanonicalDocument[] canonicalDocument = new RestCanonicalDocument[0];
        Gson gson = RestCommons.getGsonConfig();
        try {
            switch (documentType) {
                case PAYSTUB:
                    canonicalDocument = gson.fromJson(new JsonReader(new FileReader(new File(toAbsolutePath("/sampleCanonicalDocuments/ExtractedPaystub.json")))), RestCanonicalDocument[].class); //NOSONAR
                    break;
                case VOE:
                    canonicalDocument = gson.fromJson(new JsonReader(new FileReader(new File(toAbsolutePath("/sampleCanonicalDocuments/ExtractedVoeCurrent.json")))), RestCanonicalDocument[].class); //NOSONAR
                    break;
                case VOE_PREVIOUS:
                    canonicalDocument = gson.fromJson(new JsonReader(new FileReader(new File(toAbsolutePath("/sampleCanonicalDocuments/ExtractedVoePrevious.json")))), RestCanonicalDocument[].class); //NOSONAR
                    break;
                case W2:
                    canonicalDocument = gson.fromJson(new JsonReader(new FileReader(new File(toAbsolutePath("/sampleCanonicalDocuments/ExtractedW2.json")))), RestCanonicalDocument[].class); //NOSONAR
                    break;
                default:
                    break;
            }
            //@formatter:off
        } catch (IOException ignore) {}//NOSONAR
        //@formatter:on

        setCanonicalDocumentSiteAndFolderIdFields(canonicalDocument[0], folderId);
        canonicalDocument[0].getCanonicalPayload().parseAllIncomeItemObjects();

        dataSourceIdCounter++;
        documentIdCounter++;

        return canonicalDocument[0];
    }

    public RestCanonicalDocument generateDefaultCanonicalDocumentFromFile(String folderId, String filePath) {
        RestCanonicalDocument[] canonicalDocument = new RestCanonicalDocument[0];
        try {
            canonicalDocument = RestCommons.getGsonConfig().fromJson(new JsonReader(new FileReader(toAbsolutePath(filePath))), RestCanonicalDocument[].class);

            //@formatter:off
        } catch (IOException ignore) {}//NOSONAR
        //@formatter:on

        setCanonicalDocumentSiteAndFolderIdFields(canonicalDocument[0], folderId);
        canonicalDocument[0].getCanonicalPayload().parseAllIncomeItemObjects();

        dataSourceIdCounter++;
        documentIdCounter++;

        return canonicalDocument[0];
    }

    public RestCanonicalDocument generateDefaultCanonicalDocumentFromJson(String folderId, String docInput) {
        Gson gson = RestCommons.getGsonConfig();
        RestCanonicalDocument[] canonicalDocument = gson.fromJson(docInput, RestCanonicalDocument[].class);

        setCanonicalDocumentSiteAndFolderIdFields(canonicalDocument[0], folderId);
        canonicalDocument[0].getCanonicalPayload().parseAllIncomeItemObjects();

        dataSourceIdCounter++;
        documentIdCounter++;

        return canonicalDocument[0];
    }

    public void setCanonicalDocumentSiteAndFolderIdFields(RestCanonicalDocument document, String folderId) {
        document.setSiteGuid(getSiteGuid());
        document.setFolderId(folderId);
        document.setDvFolderId(getDvFolderId(folderId));
        document.setDataSourceId(String.format("%s-source-%s-%s", document.getCanonicalDocumentType().toString(), folderId, dataSourceIdCounter));
        document.getCanonicalPayload().setId(String.format("%s-source-%s-%s", document.getCanonicalDocumentType().toString(), folderId, dataSourceIdCounter));
        document.setDocumentId(String.format("%s-document-%s-%s", document.getCanonicalDocumentType().toString(), folderId, documentIdCounter));
    }

    private RestGetResponse getFolderResponse(String folderId) {
        return RestGetLoanData.getApplicationData(folderId);
    }
}

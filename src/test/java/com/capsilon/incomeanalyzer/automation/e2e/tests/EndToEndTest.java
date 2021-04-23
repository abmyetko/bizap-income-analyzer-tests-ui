package com.capsilon.incomeanalyzer.automation.e2e.tests;

import com.capsilon.incomeanalyzer.automation.data.upload.PdfUpload;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.e2e.base.RestTestBaseE2E;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalEmployer;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalPayload;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalPayloadApplicant;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import io.restassured.response.Response;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests.getDocumentCollections;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.PDF_GENERATION_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@SuppressWarnings({"rawtypes", "unchecked"})
@Execution(CONCURRENT)
@ResourceLock(value = "EndToEndTest")
class EndToEndTest extends RestTestBaseE2E {
    private final IAFolderBuilder folderBuilderE2ETest = createFolderBuilder("IAE2E_Tst");
    private RestGetResponse getResponse;
    private PdfUpload dataUpload;

    @BeforeAll
    void importDocument() {
        folderBuilderE2ETest.generateLoanDocument().restBuild();

        dataUpload = new PdfUpload(folderBuilderE2ETest, dvFolderClient);
        dataUpload
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderE2ETest.getBorrowerFullName(),
                        folderBuilderE2ETest.getBorrowerSSN(),
                        folderBuilderE2ETest.getBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.WEEKLY,
                        "09/01/" + FOUR_YEARS_PRIOR,
                        "03/15/" + YEAR_TO_DATE,
                        "600.56",
                        "",
                        folderBuilderE2ETest.getSignDate())
                        .setPriorYearYear(ONE_YEAR_PRIOR.toString())
                        .setYtdBasePay("12808.05")
                        .setPriorYearBasePay("20492.88")
                        .setYtdOvertime(INCOME_ZERO)
                        .setPriorYearOvertime(INCOME_ZERO)
                        .setYtdCommission("5000.00")
                        .setPriorYearCommission("8000.00")
                        .setYtdBonus(INCOME_ZERO)
                        .setPriorYearBonus(INCOME_ZERO)
                        .setYtdTotal("17808.05")
                        .setPriorYearTotal("28492.88"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderE2ETest.getBorrowerFullName(),
                        folderBuilderE2ETest.getBorrowerSSN(),
                        folderBuilderE2ETest.getBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.WEEKLY,
                        "09/01/" + FOUR_YEARS_PRIOR,
                        "03/15/" + YEAR_TO_DATE,
                        "600.56",
                        "",
                        "03/31/" + YEAR_TO_DATE)
                        .setPriorYearYear(ONE_YEAR_PRIOR.toString())
                        .setYtdBasePay("12808.05")
                        .setPriorYearBasePay("20492.88")
                        .setYtdOvertime(INCOME_ZERO)
                        .setPriorYearOvertime(INCOME_ZERO)
                        .setYtdCommission("5000.00")
                        .setPriorYearCommission("8000.00")
                        .setYtdBonus(INCOME_ZERO)
                        .setPriorYearBonus(INCOME_ZERO)
                        .setYtdTotal("17808.05")
                        .setPriorYearTotal("28492.88"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderE2ETest.getBorrowerFullName(),
                        folderBuilderE2ETest.getBorrowerSSN(),
                        folderBuilderE2ETest.getBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.WEEKLY,
                        "09/01/" + FOUR_YEARS_PRIOR,
                        "03/15/" + YEAR_TO_DATE,
                        "600.56",
                        "",
                        "02/28/" + YEAR_TO_DATE)
                        .setPriorYearYear(ONE_YEAR_PRIOR.toString())
                        .setYtdBasePay("12808.05")
                        .setPriorYearBasePay("20492.88")
                        .setYtdOvertime(INCOME_ZERO)
                        .setPriorYearOvertime(INCOME_ZERO)
                        .setYtdCommission("5000.00")
                        .setPriorYearCommission("8000.00")
                        .setYtdBonus(INCOME_ZERO)
                        .setPriorYearBonus(INCOME_ZERO)
                        .setYtdTotal("17808.05")
                        .setPriorYearTotal("28492.88"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderE2ETest.getBorrowerFullName(),
                        folderBuilderE2ETest.getBorrowerSSN(),
                        folderBuilderE2ETest.getBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getBorrowerPreviousEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2561.61", "12808.05"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1600.00", "8000.00"))
                        .setPayDate("03/15/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderE2ETest.getBorrowerFullName(),
                        folderBuilderE2ETest.getBorrowerSSN(),
                        folderBuilderE2ETest.getBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getBorrowerCurrentEmployment(),
                        "12000.00",
                        ONE_YEAR_PRIOR.toString())
                        .setWagesTipsOtherCompensation(INCOME_30K)
                        .setSocialSecurityWages(INCOME_30K)
                        .setMedicareWages(INCOME_30K)
                        .setSocialSecurityTips(INCOME_ZERO)
                        .setFederalIncomeTax(INCOME_1K)
                        .setSocialSecurityTax(INCOME_900)
                        .setMedicareTax(INCOME_400)
                        .setAllocatedTips(INCOME_ZERO)
                        .setStateWagesTipsEtc(INCOME_ZERO)
                        .setStateIncomeTax(INCOME_ZERO))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderE2ETest.getBorrowerFullName(),
                        folderBuilderE2ETest.getBorrowerSSN(),
                        folderBuilderE2ETest.getBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getBorrowerCurrentEmployment(),
                        "12000.00",
                        TWO_YEARS_PRIOR.toString())
                        .setWagesTipsOtherCompensation(INCOME_30K)
                        .setSocialSecurityWages(INCOME_30K)
                        .setMedicareWages(INCOME_30K)
                        .setSocialSecurityTips(INCOME_ZERO)
                        .setFederalIncomeTax(INCOME_1K)
                        .setSocialSecurityTax(INCOME_900)
                        .setMedicareTax(INCOME_400)
                        .setAllocatedTips(INCOME_ZERO)
                        .setStateWagesTipsEtc(INCOME_ZERO)
                        .setStateIncomeTax(INCOME_ZERO))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderE2ETest.getBorrowerFullName(),
                        folderBuilderE2ETest.getBorrowerSSN(),
                        folderBuilderE2ETest.getBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getBorrowerCurrentEmployment(),
                        "12000.00",
                        THREE_YEARS_PRIOR.toString())
                        .setWagesTipsOtherCompensation(INCOME_30K)
                        .setSocialSecurityWages(INCOME_30K)
                        .setMedicareWages(INCOME_30K)
                        .setSocialSecurityTips(INCOME_ZERO)
                        .setFederalIncomeTax(INCOME_1K)
                        .setSocialSecurityTax(INCOME_900)
                        .setMedicareTax(INCOME_400)
                        .setAllocatedTips(INCOME_ZERO)
                        .setStateWagesTipsEtc(INCOME_ZERO)
                        .setStateIncomeTax(INCOME_ZERO))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderE2ETest.getCoBorrowerFullName(),
                        folderBuilderE2ETest.getCoBorrowerSSN(),
                        folderBuilderE2ETest.getCoBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "09/01/" + FOUR_YEARS_PRIOR,
                        "10/31/" + YEAR_TO_DATE,
                        "1900.01",
                        "",
                        "10/31/" + YEAR_TO_DATE)
                        .setPriorYearYear(ONE_YEAR_PRIOR.toString())
                        .setYtdBasePay("12808.05")
                        .setPriorYearBasePay("20492.88")
                        .setYtdOvertime(INCOME_ZERO)
                        .setPriorYearOvertime(INCOME_ZERO)
                        .setYtdCommission("5000.00")
                        .setPriorYearCommission("8000.00")
                        .setYtdBonus(INCOME_ZERO)
                        .setPriorYearBonus(INCOME_ZERO)
                        .setYtdTotal("17808.05")
                        .setPriorYearTotal("28492.88"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderE2ETest.getCoBorrowerFullName(),
                        folderBuilderE2ETest.getCoBorrowerSSN(),
                        folderBuilderE2ETest.getCoBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2561.61", "12808.05"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1600.00", "8000.00"))
                        .setPayDate("03/15/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderE2ETest.getCoBorrowerFullName(),
                        folderBuilderE2ETest.getCoBorrowerSSN(),
                        folderBuilderE2ETest.getCoBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2561.61", "12808.05"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1600.00", "8000.00"))
                        .setPayDate("03/15/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderE2ETest.getCoBorrowerFullName(),
                        folderBuilderE2ETest.getCoBorrowerSSN(),
                        folderBuilderE2ETest.getCoBorrowerCollaboratorId(),
                        folderBuilderE2ETest.getCoBorrowerCurrentEmployment(),
                        "12000.00",
                        ONE_YEAR_PRIOR.toString())
                        .setWagesTipsOtherCompensation("11000.00")
                        .setSocialSecurityWages(INCOME_30K)
                        .setMedicareWages(INCOME_30K)
                        .setSocialSecurityTips(INCOME_ZERO)
                        .setFederalIncomeTax(INCOME_1K)
                        .setSocialSecurityTax(INCOME_900)
                        .setMedicareTax(INCOME_400)
                        .setAllocatedTips(INCOME_ZERO)
                        .setStateWagesTipsEtc(INCOME_ZERO)
                        .setStateIncomeTax(INCOME_ZERO))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilderE2ETest.getFolderId());
    }

    private Stream previousVoeFrequencyTestCases() {
        return Stream.of(
                of(new BigDecimal(60), HOURLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR),
                of(new BigDecimal(600), WEEKLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR),
                of(new BigDecimal(1200), BI_WEEKLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR),
//                of(new BigDecimal(1300), SEMI_MONTHLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR),
                of(new BigDecimal(2400), MONTHLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR),
                of(new BigDecimal(6400), QUARTERLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR),
//                of(new BigDecimal(9000), SEMI_ANNUALLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR),
                of(new BigDecimal(9999), ANNUALLY, "01/01/" + TWO_YEARS_PRIOR, "12/31/" + ONE_YEAR_PRIOR)
        );
    }

    @Test
    @EnableIfToggled(propertyName = PDF_GENERATION_TOGGLE)
    @Description("IA-1740 Check If Income Worksheet is Generated With Correct Months Worked Value")
    void checkIfIncomeWorksheetHasCorrectDateForEndOfOctober() {
        final List<PDDocument>[] docs = new List[1];
        final PDDocument[] latestDocForEnv = new PDDocument[1];
        Retry.whileTrue(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            Retry.whileTrue(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
                latestDocForEnv[0] = null;
                docs[0] = RestNotIARequests.downloadAllDocuments(RestNotIARequests.getAllIncomeWorksheets(folderBuilderE2ETest.getFolderId()), folderBuilderE2ETest.getFolderId());
                latestDocForEnv[0] = docs[0].stream().filter(document -> Objects.requireNonNull(RestNotIARequests.getIncomeWorksheetPageText(document, 0))
                        .getTextForRegion("mainPage").contains(getResponse.getSiteGUID())).findFirst().orElse(null);
                return latestDocForEnv[0] == null;
            }, "The Automated Income Worksheet for selected env has not been generated correctly");
            for (int pageNumber = 0; pageNumber < latestDocForEnv[0].getPages().getCount(); pageNumber++) {
                if (Objects.requireNonNull(RestNotIARequests.getIncomeWorksheetPageText(latestDocForEnv[0], pageNumber)).getTextForRegion("header")
                        .contains(folderBuilderE2ETest.getCoBorrowerFullName() + " > W2 > Base Pay")) {
                    return !Objects.requireNonNull(RestNotIARequests.getIncomeWorksheetPageText(latestDocForEnv[0], pageNumber))
                            .getTextForRegion("mainPage").contains("Months Worked 10 months");
                }
            }
            return true;
        }, "The latest Automated Income Worksheet for selected env had incorrect or null months worked.");
    }

    @Test
    void checkIfDocReaderDirectLaunchEndpointDoesNotResultInError() {
        getResponse.getDocuments().forEach((key, value) ->
                assertAll("Testing document: " + value.getDocumentId(), () -> {
                            if (value.getDocumentId() != null) {
                                Response rsp = RestNotIARequests.getDocReaderDocument(folderBuilderE2ETest.getFolderId(), value.getDocumentId());
                                assertEquals("0", rsp.jsonPath().getString("errorCode"), "Error code does exist: " + rsp.jsonPath().getString("errorCode"));
                                assertNull(rsp.jsonPath().getString("errorMessage"), "Error message does exist: " + rsp.jsonPath().getString("errorMessage"));
                                assertNotEquals("Failure", rsp.jsonPath().getString("status"), "Status: Failure");
                            }
                        }
                ));
    }

    @Test
    void checkIfDocumentWithLowestIncomeIsSelectedByDefault() {
        assertAll("document with lowest income is selected by default",
                () -> {
                    List<RestPartIncomeAnnualSummary> summaryList = getResponse.getApplicant(folderBuilderE2ETest.getCoBorrowerFullName()).getIncome(folderBuilderE2ETest.getCoBorrowerCurrentEmployment())
                            .getPart("Base Pay")
                            .getIncome(YEAR_TO_DATE).getAnnualSummary();
                    assertTrue(summaryList.stream().sorted(Comparator.comparing(doc -> doc.getMonthlyAmountCalculated().getValue(), Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()).get(0).getSelected());
                },
                () -> {
                    List<RestPartIncomeAnnualSummary> summaryList = getResponse.getApplicant(folderBuilderE2ETest.getCoBorrowerFullName()).getIncome(folderBuilderE2ETest.getCoBorrowerCurrentEmployment())
                            .getPart("Base Pay")
                            .getIncome(ONE_YEAR_PRIOR).getAnnualSummary();
                    assertTrue(summaryList.stream().sorted(Comparator.comparing(doc -> doc.getMonthlyAmountAvg().getValue(), Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()).get(0).getSelected());
                }
        );
    }

    @Test
    void checkIfDataFromDocumentWithNewestDateIsUsedInsteadOfOtherOnes() {
        String[] documentValues = getSelectedDocumentId(folderBuilderE2ETest.getBorrowerFullName(), folderBuilderE2ETest.getBorrowerCurrentEmployment(), YEAR_TO_DATE);
        List<RestCanonicalDocument> docList = getDocumentCollections(folderBuilderE2ETest.getFolderId(), documentValues[1]);
        RestCanonicalPayload doc = docList.stream().filter(document ->
                documentValues[0].equals(document.getDataSourceId())).findFirst().orElseThrow(NullPointerException::new).getCanonicalPayload();
        assertEquals(YEAR_TO_DATE + "-03-31", doc.getApplicant().get(0).getEmployer().get(0).getIndividual().getExecution().getExecutionDetail().getExecutionDate());
    }

    //    @Disabled("Not yet ready")
    @Test
    void gettingSnippetsShouldReturnCorrectImages() {
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilderE2ETest.getFolderId());
            RestPartIncomeAnnualSummary incomeDocument = getResponse.getApplicant(folderBuilderE2ETest.getBorrowerFullName())
                    .getIncome(folderBuilderE2ETest.getBorrowerCurrentEmployment())
                    .getBasePay()
                    .getIncome(YEAR_TO_DATE)
                    .getAllAnnualSummary().get(0);
            BufferedImage getBigSnip = RestGetLoanData.getSnippet(getResponse.getRefId(),
                    incomeDocument.getDocIds().get(0).toString(),
                    incomeDocument.getFrequency().getSnippets().get(0).getId());
            if (getBigSnip == null)
                fail("Snippet has not been returned correctly");
        });
    }

    //    @Disabled("Not yet ready")
    @Test
    void gettingContextSnippetsShouldReturnCorrectImages() {
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilderE2ETest.getFolderId());
            RestPartIncomeAnnualSummary incomeDocument = getResponse.getApplicant(folderBuilderE2ETest.getBorrowerFullName())
                    .getIncome(folderBuilderE2ETest.getBorrowerCurrentEmployment())
                    .getBasePay()
                    .getIncome(YEAR_TO_DATE)
                    .getAllAnnualSummary().get(0);
            BufferedImage getBigSnip = RestGetLoanData.getContextSnippet(getResponse.getRefId(),
                    incomeDocument.getDocIds().get(0).toString(),
                    incomeDocument.getFrequency().getSnippets().get(0).getId());
            if (getBigSnip == null)
                fail("Context snippet has not been returned correctly");
        });
    }

    private String[] getFirstDocumentId(String applicantName, String jobName, Integer year) {
        return getDocumentId(applicantName, jobName, year, 0);
    }

    private String[] getSelectedDocumentId(String applicantName, String jobName, Integer year) {
        Long docId = getResponse.getApplicant(applicantName)
                .getIncome("W2", jobName)
                .getPart("Base Pay")
                .getIncome(year)
                .getAnnualSummarySelectedDocument()
                .getDocIds().get(0);
        return new String[]{getResponse.getDocumentById(docId).getDataSourceId(), getResponse.getDocumentById(docId).getType()};
    }

    private String[] getDocumentId(String applicantName, String jobName, Integer year, Integer documentNumber) {
        Long docId = getResponse.getApplicant(applicantName)
                .getIncome("W2", jobName)
                .getPart("Base Pay")
                .getIncome(year)
                .getAnnualSummary().get(documentNumber)
                .getDocIds().get(0);
        return new String[]{getResponse.getDocumentById(docId).getDataSourceId(), getResponse.getDocumentById(docId).getType()};
    }

    private RestCanonicalPayloadApplicant getSelectedDocumentByName(String documentType, String applicantName, String documentEmployerName) {
        List<RestCanonicalDocument> docCollection = getDocumentCollections(folderBuilderE2ETest.getFolderId(), documentType);
        for (RestCanonicalDocument document : docCollection) {
            for (RestCanonicalPayloadApplicant applicant : document.getCanonicalPayload().getApplicant()) {
                if (applicantName.equalsIgnoreCase(applicant.getName().getFullName())) {
                    for (RestCanonicalEmployer employer : applicant.getEmployer()) {
                        if (documentEmployerName.equalsIgnoreCase(employer.getLegalEntity().getLegalEntityDetail().getFullName())) {
                            return applicant;
                        }
                    }
                }
            }
        }
        return null;
    }

    private RestCanonicalPayload getSelectedDocumentById(String[] documentId) {
        List<RestCanonicalDocument> docCollection = getDocumentCollections(folderBuilderE2ETest.getFolderId(), documentId[1]);
        for (RestCanonicalDocument document : docCollection) {
            if (documentId[0].equals(document.getDataSourceId())) {
                return document.getCanonicalPayload();
            }
        }
        return null;
    }
}

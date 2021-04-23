package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponseDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncome;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.UNKNOWN;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType.CALCULATED;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType.MANUAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumingThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("iaCanonical")
@Execution(CONCURRENT)
@ResourceLock(value = "CanonicalTest")
public class CanonicalTest extends TestBaseRest {

    private IAFolderBuilder folderBuilderPerfCanonicalTest = createFolderBuilder("IARestCanon");

    @BeforeAll
    void setupTestFolder() {
        folderBuilderPerfCanonicalTest.setSignDate("01/01/" + YEAR_TO_DATE)
                .setCoBorrowerCurrentEmployment(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment());
        folderBuilderPerfCanonicalTest.generateLoanDocument().restBuild();
    }

    @Test
    @Description("IA-2342 IA-2373 Changing Years On Job Should Change Start Date For Employer")
    void changingYearsOnJobShouldChangeStartDateForEmployer() {
        IAFolderBuilder folderBuilder = createFolderBuilder("IACanEmplDt");
        folderBuilder.generateLoanDocument().restBuild();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Default dates",
                    () -> assertEquals((YEAR_TO_DATE - 11) + "-04-28", response.getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                            .getEmployer().getEmploymentPeriod()
                            .getDateFrom(), "Updated employment start date for borrower was not correctly extracted after loan update"),
                    () -> assertEquals((YEAR_TO_DATE - 16) + "-09-28", response.getApplicant(folderBuilder.getCoBorrowerFullName())
                            .getIncome(folderBuilder.getCoBorrowerCurrentEmployment())
                            .getEmployer().getEmploymentPeriod()
                            .getDateFrom(), "Updated employment start date for co-borrower  was not correctly extracted after loan update")
            );
        });

        folderBuilder.setBorrowerYearsOnThisJob("6").setBorrowerMonthsOnThisJob("6").setCoBorrowerYearsOnThisJob("6").setCoBorrowerMonthsOnThisJob("6")
                .generateLoanDocument();

        folderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Updated dates",
                    () -> assertEquals((YEAR_TO_DATE - 7) + "-08-28", response.getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                            .getEmployer().getEmploymentPeriod()
                            .getDateFrom(), "Updated employment start date for borrower was not correctly extracted after loan update"),
                    () -> assertEquals((YEAR_TO_DATE - 7) + "-08-28", RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getCoBorrowerFullName())
                            .getIncome(folderBuilder.getCoBorrowerCurrentEmployment())
                            .getEmployer().getEmploymentPeriod()
                            .getDateFrom(), "Updated employment start date for co-borrower  was not correctly extracted after loan update")
            );
        });
    }

    @Test
    @Tag("integration")
    @Tag("health")
    @Description("IA-2342 Changing Document Association Manually Should Work Correctly")
    void changingDocumentAssociationManuallyShouldWorkCorrectly() {
        String originalDataSourceId = "Data-source-to-change-applicant-test";
        String newDataSourceId = "Source-to-data-change-applicant-test";
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        RestCanonicalDocument canonicalDocument = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderPerfCanonicalTest.getFolderId(), PAYSTUB);
        canonicalDocumentMethods.setBorrowerAndJob(canonicalDocument, folderBuilderPerfCanonicalTest.getFolderId(),
                folderBuilderPerfCanonicalTest.getBorrowerFullName(),
                folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(canonicalDocument,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                PAYSTUB);
        canonicalDocument.setDataSourceId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(originalDataSourceId);
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            Long documentId = response.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE)
                    .getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0);
            assertEquals(originalDataSourceId, response.getDocumentById(documentId).getDataSourceId(),
                    String.format("Document used for primary applicant current job does not have correct data source id expected: %s actual: %s",
                            originalDataSourceId,
                            response.getDocumentById(documentId).getDataSourceId()));
        });

        canonicalDocument.setDataSourceId(newDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(newDataSourceId);
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).setCollaboratorId(folderBuilderPerfCanonicalTest.getCoBorrowerCollaboratorId());
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getName().setFirstName(folderBuilderPerfCanonicalTest.getCoBorrowerFirstName());
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getName().setLastName(folderBuilderPerfCanonicalTest.getCoBorrowerLastName());
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getName().setFullName(folderBuilderPerfCanonicalTest.getCoBorrowerFullName());
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        final Long[] finalDocumentId = new Long[1];
        final RestGetResponse[] finalResponse = new RestGetResponse[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            Long documentId = response.getApplicant(folderBuilderPerfCanonicalTest.getCoBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getCoBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE)
                    .getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0);
            finalDocumentId[0] = documentId;
            finalResponse[0] = response;
        });

        assertAll("Document applicant change assertions",
                () -> {
                    assumingThat(finalResponse[0].getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(PAYSTUB) == null,
                            () -> assertNull(finalResponse[0].getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                            .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                            .getBasePay().getIncome(YEAR_TO_DATE)
                                            .getAnnualSummaryDocument(PAYSTUB),
                                    "Document has not been removed from primary applicant current job"));

                    assumingThat(finalResponse[0].getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(PAYSTUB) != null,
                            () -> assertNotEquals(canonicalDocument.getDocumentId(), finalResponse[0].getDocumentById(finalResponse[0].getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                            .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                            .getBasePay().getIncome(YEAR_TO_DATE)
                                            .getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0)).getDocumentId(),
                                    "Document has not been removed from primary applicant current job"));
                },
                () -> assertEquals(newDataSourceId, finalResponse[0].getDocumentById(finalDocumentId[0]).getDataSourceId(),
                        String.format("Document used for secondary applicant current job does not have correct data source id expected: %s actual: %s",
                                originalDataSourceId,
                                finalResponse[0].getDocumentById(finalDocumentId[0]).getDataSourceId()))
        );
    }

    @Test
    @Tag("integration")
    @Description("IA-2342 Associating Orphan Document Should Work Correctly")
    void associatingOrphanDocumentShouldWorkCorrectly() {
        String originalDataSourceId = "Data-source-to-change-orphan-test";
        String newDataSourceId = "Source-to-data-change-orphan-test";
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        RestCanonicalDocument canonicalDocument = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderPerfCanonicalTest.getFolderId(), PAYSTUB);
        canonicalDocumentMethods.setOrphanDoc(canonicalDocument);
        canonicalDocumentMethods.setDatesForSelectedDocument(canonicalDocument,
                YEAR_TO_DATE + "-12-01",
                YEAR_TO_DATE + "-12-31",
                YEAR_TO_DATE + "-12-31",
                PAYSTUB);
        canonicalDocument.setDataSourceId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(originalDataSourceId);
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            RestGetResponseDocument document = response.getDocumentByDataSource(originalDataSourceId);
            assertNotNull(document, "Orphan document is present in response doc list");
        });

        canonicalDocument.setDataSourceId(newDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(newDataSourceId);
        canonicalDocumentMethods.setBorrowerAndJob(canonicalDocument, folderBuilderPerfCanonicalTest.getFolderId(),
                folderBuilderPerfCanonicalTest.getBorrowerFullName(),
                folderBuilderPerfCanonicalTest.getBorrowerPreviousEmployment());
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        final RestGetResponse[] finalResponse = new RestGetResponse[1];
        final Long[] documentId = new Long[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            documentId[0] = response.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE)
                    .getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0);
            finalResponse[0] = response;
        });

        assertEquals(newDataSourceId, finalResponse[0].getDocumentById(documentId[0]).getDataSourceId(), "Orphan document is has not been associated to selected job correctly");
    }

    @Test
    @Description("IA-2342 Changing Pay Frequency Manually Should Work Correctly")
    void changingPayFrequencyManuallyShouldWorkCorrectly() {
        String originalDataSourceId = "Data-source-to-change-freq-test";
        String newDataSourceId = "Source-to-data-change-freq-test";
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        RestCanonicalDocument canonicalDocument = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderPerfCanonicalTest.getFolderId(), PAYSTUB);
        canonicalDocumentMethods.setBorrowerAndJob(canonicalDocument, folderBuilderPerfCanonicalTest.getFolderId(),
                folderBuilderPerfCanonicalTest.getBorrowerFullName(),
                folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(canonicalDocument,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                PAYSTUB);
        canonicalDocument.setDataSourceId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(originalDataSourceId);
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertEquals(CALCULATED, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                    String.format("Document used for primary applicant current job does not have frequency type expected: %s actual: %s",
                            CALCULATED,
                            income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()));
            assertEquals(CALCULATED, income.getAllAnnualSummaryDocument(income.getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0)).getFrequency().getValueType(),
                    String.format("Document used for primary applicant current job does not have frequency type expected: %s actual: %s",
                            CALCULATED,
                            income.getAllAnnualSummaryDocument(income.getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0)).getFrequency().getValueType()));
        });
        canonicalDocument.setDataSourceId(newDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(newDataSourceId);
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getEmployment().setEmploymentIncomeFrequencyType("Weekly");
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertEquals(MANUAL, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                    String.format("Document used for primary applicant current job does not have frequency type expected: %s actual: %s",
                            MANUAL,
                            income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()));
            assertEquals(MANUAL, income.getAllAnnualSummaryDocument(income.getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0)).getFrequency().getValueType(),
                    String.format("Document used for primary applicant current job does not have frequency type expected: %s actual: %s",
                            MANUAL,
                            income.getAllAnnualSummaryDocument(income.getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0)).getFrequency().getValueType()));
        });
    }

    @Test
    @Description("IA-2342 IA-2227 Pay Frequency Should Be Taken From Employment Income Frequency Type If Not Empty For Previous Voe")
    void payFrequencyShouldBeTakenFromEmploymentIncomeFrequencyTypeIfNotEmptyForPreviousVoe() {
        String newDataSourceId = "employment-income-frequency-type-test2";
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        RestCanonicalDocument voePrev = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderPerfCanonicalTest.getFolderId(), VOE_PREVIOUS);
        canonicalDocumentMethods.setBorrowerAndJob(voePrev, folderBuilderPerfCanonicalTest.getFolderId(),
                folderBuilderPerfCanonicalTest.getBorrowerFullName(),
                folderBuilderPerfCanonicalTest.getBorrowerPreviousEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(voePrev,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                VOE_PREVIOUS);
        voePrev.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getEmployment().setEmploymentIncomeFrequencyType(IncomeFrequency.WEEKLY.text);
        canonicalDocumentMethods.uploadCanonicalDocument(voePrev, voePrev.getSiteGuid());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertEquals(IncomeFrequency.WEEKLY, income.getAllAnnualSummaryDocument(VOE).getFrequency().getValue(),
                    String.format("Pay Frequency should be taken from employmentIncomeFrequencyType and should be: %s but is: %s",
                            IncomeFrequency.WEEKLY,
                            income.getAllAnnualSummaryDocument(VOE).getFrequency().getValue()));
        });
        voePrev.setDataSourceId(newDataSourceId);
        voePrev.getCanonicalPayload().setId(newDataSourceId);
        voePrev.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getEmployment().setEmploymentIncomeFrequencyType(null);
        voePrev.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getIncomeItemList().get(0).getIncomeItemDetail().get(0).setIncomeFrequencyType(IncomeFrequency.MONTHLY.text);
        canonicalDocumentMethods.uploadCanonicalDocument(voePrev, voePrev.getSiteGuid());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertEquals(IncomeFrequency.MONTHLY, income.getAllAnnualSummaryDocument(VOE).getFrequency().getValue(),
                    String.format("Pay Frequency should be taken from Base IncomeItem if employmentIncomeFrequencyType is empty and should be: %s but is: %s",
                            IncomeFrequency.MONTHLY,
                            income.getAllAnnualSummaryDocument(VOE).getFrequency().getValue()));
        });
    }

    @Test
    @ResourceLock(value = "SampleTestChecklistLock")
    @Description("IA-2087 Check If Paystub Data Is Extracted From Invalid Document From Chk Tests")
    void testCaseChkNpePaystub() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_CHK_PS");
        folderBuilderSampleTests.generateLoanDocument().restBuild();

        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        RestCanonicalDocument canonicalDocument = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderSampleTests.getFolderId(),
                "/sampleCanonicalDocuments/ChkNpePaystub.json");
        canonicalDocumentMethods.setBorrowerAndJob(canonicalDocument, folderBuilderSampleTests.getFolderId(),
                folderBuilderSampleTests.getBorrowerFullName(),
                folderBuilderSampleTests.getBorrowerCurrentEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(canonicalDocument,
                ONE_YEAR_PRIOR + "-03-19",
                ONE_YEAR_PRIOR + "-10-24",
                YEAR_TO_DATE + "-02-21",
                PAYSTUB);
        canonicalDocument.setDataSourceId("chk-npe-paystub");
        canonicalDocument.getCanonicalPayload().setId("chk-npe-paystub");
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertEquals(UNKNOWN, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                    "Invalid paystub could not be found in IA response");
        });
    }

    @Test
    @Description("IA-2726 Check If PayStub Data Is Extracted From Document after uploading Invalid W2")
    void checkIfPaystubIsUploadedAfterSendingIncorrectW2() {
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        RestCanonicalDocument w2 = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderPerfCanonicalTest.getFolderId(), W2);
        canonicalDocumentMethods.setBorrowerAndJob(w2, folderBuilderPerfCanonicalTest.getFolderId(),
                folderBuilderPerfCanonicalTest.getBorrowerFullName(),
                " ");
        canonicalDocumentMethods.setDatesForSelectedDocument(w2,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                W2);
        canonicalDocumentMethods.uploadCanonicalDocument(w2, w2.getSiteGuid());
        RestCanonicalDocument paystub = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderPerfCanonicalTest.getFolderId(), PAYSTUB);
        canonicalDocumentMethods.setBorrowerAndJob(paystub, folderBuilderPerfCanonicalTest.getFolderId(),
                folderBuilderPerfCanonicalTest.getBorrowerFullName(),
                folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(paystub,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                PAYSTUB);
        canonicalDocumentMethods.uploadCanonicalDocument(paystub, paystub.getSiteGuid());
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestPartIncome income = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId()).getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertEquals("PAYSTUB", income.getAnnualSummaryDocument(PAYSTUB).getDocType(),
                    "PayStub couldn't be found in IA Response");
        });
    }
}

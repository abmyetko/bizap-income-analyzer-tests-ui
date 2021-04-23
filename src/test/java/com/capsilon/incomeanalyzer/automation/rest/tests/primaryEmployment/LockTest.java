package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.CduStatus;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.RoundingMode;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.AUTOMATIC_LOCK;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("iaCanonical")
@Tag("integration")
@Execution(CONCURRENT)
@ResourceLock(value = "LockTest")
@EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
public class LockTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderPerfCanonicalTest = createFolderBuilder("IARestLock");

    @BeforeAll
    void setupTestFolder() {
        folderBuilderPerfCanonicalTest.setSignDate("01/01/" + YEAR_TO_DATE);
        folderBuilderPerfCanonicalTest.generateLoanDocument().restBuild();


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
        String originalDataSourceId = "Data-source-to-change-applicant-test";
        canonicalDocument.setDocumentId("Data-source-to-change-applicant-test");
        canonicalDocument.setDataSourceId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList().get(1).getIncomeItemDetail().get(0)
                .setIncomeTypeTotalAmount(bigD(660));
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList().get(1).getIncomeItemDetail().get(0)
                .setIncomeTypeYearToDateAmount(bigD(12000));
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());
    }

    @Test
    @Tag("health")
    @Order(1)
    void checkIfCalculationIsPreservedAfterLock() {
        final RestGetResponse[] originalResponse = {RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())};
        Retry.tryRun(TIMEOUT_FORTY_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            RestNotIARequests.waiveChecklistRules(folderBuilderPerfCanonicalTest.getFolderId(),
                    RestNotIARequests.getFailedChecklistRulesIds(folderBuilderPerfCanonicalTest.getFolderId()), "IALockTest");
            originalResponse[0] = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            RestNotIARequests.setCduStatus(folderBuilderPerfCanonicalTest.getFolderId(), CduStatus.LOCKED, "LockingToLock");
        });

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse lockedResponse = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            //Gross
            assertEquals(originalResponse[0].getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2().
                                    getPrimaryIncomeGroup()
                            .getIncomeTypeBasePay()
                            .getTotalSelectedIncome(YEAR_TO_DATE)
                            .getGross(),
                    lockedResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2().
                            getPrimaryIncomeGroup().getIncomeTypeBasePay()
                            .getTotalSelectedIncome(YEAR_TO_DATE)
                            .getGross());
            //Monthly AvgIncome
            assertEquals(originalResponse[0].getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2().
                                    getPrimaryIncomeGroup().getIncomeTypeBasePay()
                            .getTotalSelectedIncome(YEAR_TO_DATE)
                            .getAvgMonthlyIncome(),
                    lockedResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2().
                            getPrimaryIncomeGroup().getIncomeTypeBasePay()
                            .getTotalSelectedIncome(YEAR_TO_DATE)
                            .getAvgMonthlyIncome());
        });
    }

    @Test
    @Order(2)
    void checkIfLockedFolderIsNotCalculating() {
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            if (!RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId()).getStatus().equals("LOCK")) {
                RestNotIARequests.waiveChecklistRules(folderBuilderPerfCanonicalTest.getFolderId(),
                        RestNotIARequests.getFailedChecklistRulesIds(folderBuilderPerfCanonicalTest.getFolderId()), "IALockTest");
                RestNotIARequests.setCduStatus(folderBuilderPerfCanonicalTest.getFolderId(), CduStatus.LOCKED, "IALockTest");
            }
        });

        RestGetResponse originalResponse = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());

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
        String originalDataSourceId = "Second-data-source-to-change-applicant-test";
        canonicalDocument.setDocumentId("Second-data-source-to-change-applicant-test");
        canonicalDocument.setDataSourceId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList().get(1).getIncomeItemDetail().get(0)
                .setIncomeTypeTotalAmount(bigD(880));
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemList().get(1).getIncomeItemDetail().get(0)
                .setIncomeTypeYearToDateAmount(bigD(15000));
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse lockedResponse = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            assertEquals(originalResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                            .getBasePay()
                            .getIncome(YEAR_TO_DATE)
                            .getAnnualSummarySelectedDocument()
                            .getMonthlyAmountCalculated()
                            .getValue().setScale(2, RoundingMode.HALF_UP),
                    lockedResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                            .getBasePay()
                            .getIncome(YEAR_TO_DATE)
                            .getAnnualSummarySelectedDocument()
                            .getMonthlyAmountCalculated()
                            .getValue().setScale(2, RoundingMode.HALF_UP));
        });
    }

    @Test
    @Order(3)
    void checkIfUnlockCalculatesUploadedDocuments() {
        Retry.tryRun(TIMEOUT_FORTY_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            if (RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId()).getStatus().equals("LOCK")) {
                RestNotIARequests.setCduStatus(folderBuilderPerfCanonicalTest.getFolderId(), CduStatus.IN_PROGRESS, "LockingForTest");
            }
        });

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse finalResponse = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            assertEquals(bigD(880), finalResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                    .getBasePay()
                    .getIncome(YEAR_TO_DATE)
                    .getAnnualSummarySelectedDocument()
                    .getMonthlyAmountCalculated()
                    .getValue().setScale(2, RoundingMode.HALF_UP));
            assertEquals(bigD(5000), finalResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                    .getBasePay()
                    .getIncome(YEAR_TO_DATE)
                    .getAnnualSummarySelectedDocument()
                    .getMonthlyAmountAvg()
                    .getValue().setScale(2, RoundingMode.HALF_UP));
        });
    }
}
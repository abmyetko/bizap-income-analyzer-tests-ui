package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.automation.dv.helpers.folder.FolderAttributes;
import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestApplicantIncome;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFNMBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.CduStatus;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.katalyst.business.objects.EFolder;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.capsilon.common.endpoint.FlamsEndpoint.getDvFolderFriendlyId;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests.getDocumentCollections;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.IATestResources.toAbsolutePath;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.WEEKLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.AUTOMATIC_LOCK;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("IA clean test case scenarios ")
@Tag("iaCanonical")
@Execution(CONCURRENT)
public class SampleTest extends TestBaseRest {

    private static EFolder folderDv;
    private final Boolean replaceTemplate = false;

    @Test
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation For One Applicant With Documents")
    void testCaseOne() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_1");
        folderBuilderSampleTests.generateLoanDocumentWithOneApplicant().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_1.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @Tag("health")
    @Description("IA-2248 IA-2214 Check If There Was No Regression When Borrower Name And Sign Date Have Been Updated")
    void testCaseTwo() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_2");
        folderBuilderSampleTests.setSignDate("02/16/" + YEAR_TO_DATE).generateLoanDocumentWithOneApplicant().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_2_01.json", canonicalDocumentMethods, documentList);

        folderBuilderSampleTests.setSignDate("02/20/" + YEAR_TO_DATE).setBorrowerFirstName("Tom").setBorrowerLastName("Bombadil");
        folderBuilderSampleTests.generateLoanDocumentWithOneApplicant().uploadNewLoanDocument();

        compareIncome(folderBuilderSampleTests.getFolderId(), "TC_2_02.json");

        folderBuilderSampleTests.setSignDate("02/24/" + YEAR_TO_DATE).setBorrowerFirstName("Tommy").setBorrowerLastName("Vercetti");
        folderBuilderSampleTests.generateLoanDocumentWithOneApplicant().uploadNewLoanDocument();

        compareIncome(folderBuilderSampleTests.getFolderId(), "TC_2_03.json");
    }

    @Test
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation For Two Borrowers With Documents For CoBorrower Only")
    void testCaseThree() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_3");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);
        documentList.addAll(generateCoBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_3.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @Tag("integration")
    @Description("IA-2248 IA-2214 Check If There Was No Regression For Two Borrowers When Primary Borrower Name And Sign Date Have Been Updated")
    void testCaseFour() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_4");
        folderBuilderSampleTests.setSignDate("02/16/" + YEAR_TO_DATE).generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, true);
        documentList.addAll(generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, false));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_4_01.json", canonicalDocumentMethods, documentList);

        folderBuilderSampleTests.setSignDate("02/22/" + YEAR_TO_DATE).setBorrowerFirstName("Tommy").setBorrowerLastName("Angelo");
        folderBuilderSampleTests.generateLoanDocument().uploadNewLoanDocument();

        compareIncome(folderBuilderSampleTests.getFolderId(), "TC_4_02.json");

        folderBuilderSampleTests.setSignDate("02/25/" + YEAR_TO_DATE).setBorrowerFirstName("Tom").setBorrowerLastName("Sawyer");
        folderBuilderSampleTests.generateLoanDocument().uploadNewLoanDocument();

        compareIncome(folderBuilderSampleTests.getFolderId(), "TC_4_03.json");
    }

    @Test
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation For One Borrower When Sign Date Has Been Updated")
    void testCaseFive() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_5");
        folderBuilderSampleTests.setSignDate("02/16/" + YEAR_TO_DATE).generateLoanDocumentWithOneApplicant().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, true);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_5_01.json", canonicalDocumentMethods, documentList);

        folderBuilderSampleTests.setSignDate("02/27/" + YEAR_TO_DATE);
        folderBuilderSampleTests.generateLoanDocument().uploadNewLoanDocument();
        try {
            Thread.sleep(TIMEOUT_TWENTY_SECONDS); //NOSONAR
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, false);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_5_02.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @Tag("integration")
    @Description("IA-2248 IA-2214 Check If There Was No Regression When Borrower Name And Ssn Have Been Updated And He Had Documents Before Update")
    void testCaseSix() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_6");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, true);
        documentList.addAll(generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, false));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_6_01.json", canonicalDocumentMethods, documentList);

        folderBuilderSampleTests.setBorrowerSSN("111-22-3333").setBorrowerFirstName("Mark").setBorrowerLastName("Mawords");
        folderBuilderSampleTests.generateLoanDocument().uploadNewLoanDocument();
        try {
            Thread.sleep(TIMEOUT_TWENTY_SECONDS); //NOSONAR
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, true);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_6_02.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Locked")
    void testCaseSeven() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_7");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_7.json", canonicalDocumentMethods, documentList, CduStatus.LOCKED, true);
    }

    @Test
    @EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Locked Documents Withdrawn")
    void testCaseEight() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_8");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_8.json", canonicalDocumentMethods, documentList, CduStatus.LOCKED, true);

        for (RestCanonicalDocument canonicalDocument : documentList)
            canonicalDocument.setDataSourceStatus("REMOVED");

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_8.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
    @Tag("integration")
    @Description("IA-2248 IA-2214 IA-2348 Check If There Was No Regression In Calculation When Folder Has Been Locked Documents Removed And Unlocked")
    void testCaseEightWithUnlock() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_8_UN");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_8_01.json", canonicalDocumentMethods, documentList, CduStatus.LOCKED, true);

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.removeDocumentData();
        }

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_8_01.json", canonicalDocumentMethods, documentList);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_8_02.json", CduStatus.IN_PROGRESS);
    }

    @Test
    @EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
    @Tag("integration")
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Locked Sign Date And Borrower Name Updated And Unlocked")
    void testCaseNine() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_9");
        folderBuilderSampleTests.setSignDate("02/16/" + YEAR_TO_DATE).generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_9_01.json", canonicalDocumentMethods, documentList, CduStatus.LOCKED, true);

        folderBuilderSampleTests.setSignDate("02/28/" + YEAR_TO_DATE).setBorrowerFirstName("Norman").setBorrowerLastName("Bates");
        folderBuilderSampleTests.generateLoanDocument().uploadNewLoanDocument();

        compareIncome(folderBuilderSampleTests.getFolderId(), "TC_9_01.json");

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_9_02.json", CduStatus.IN_PROGRESS);
    }

    @Test
    @EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Locked Documents Updated And Unlocked")
    void testCaseTen() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_10");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);
        documentList.addAll(generateCoBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_10_01.json", canonicalDocumentMethods, documentList, CduStatus.LOCKED, true);

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceId(canonicalDocument.getDataSourceId() + "Changed");
            canonicalDocument.getCanonicalPayload().setId(canonicalDocument.getDataSourceId() + "Changed");

            switch (canonicalDocument.getCanonicalDocumentType()) {
                case PAYSTUB:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(0)
                            .getEmployment()
                            .setEmploymentIncomeFrequencyType("Weekly");
                    break;
                case VOE:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(1)
                            .getIncomeItemList().get(0)
                            .getIncomeItemDetail().get(0)
                            .setIncomeFrequencyType("Weekly");
                    break;
                case W2:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getIrsTaxDocument().get(0)
                            .getIrsTaxDocumentData()
                            .getIrsTaxDocumentDataIncomeDetail()
                            .setWagesSalariesTipsEtcAmount(bigD(30000))
                            .setMedicareWagesAndTipsAmount(bigD(30000))
                            .setSocialSecurityBenefitsAmount(bigD(30000));
                    break;
                default:
                    break;
            }
        }

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_10_01.json", canonicalDocumentMethods, documentList);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_10_02.json", CduStatus.IN_PROGRESS);
    }

    @Test
    @EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Locked Documents Updated")
    void testCaseEleven() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_11");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);
        documentList.addAll(generateCoBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_11.json", canonicalDocumentMethods, documentList, CduStatus.LOCKED, true);

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceId(canonicalDocument.getDataSourceId() + "Changed");
            canonicalDocument.getCanonicalPayload().setId(canonicalDocument.getDataSourceId() + "Changed");

            switch (canonicalDocument.getCanonicalDocumentType()) {
                case PAYSTUB:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(0)
                            .getEmployment()
                            .setEmploymentIncomeFrequencyType("Weekly");
                    break;
                case VOE:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(1)
                            .getIncomeItemList().get(1)
                            .getIncomeItemDetail().get(0)
                            .setIncomeFrequencyType("Weekly");
                    break;
                case W2:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getIrsTaxDocument().get(0)
                            .getIrsTaxDocumentData()
                            .getIrsTaxDocumentDataIncomeDetail()
                            .setWagesSalariesTipsEtcAmount(bigD(30000))
                            .setMedicareWagesAndTipsAmount(bigD(30000))
                            .setSocialSecurityBenefitsAmount(bigD(30000));
                    break;
                default:
                    break;
            }
        }

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_11.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Disabled")
    void testCaseTwelve() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_12");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_12.json", canonicalDocumentMethods, documentList, CduStatus.REMOVED, false);
    }

    @Test
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Disabled Documents Withdrawn And Unlocked")
    void testCaseThirteen() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_13");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_13_01.json", canonicalDocumentMethods, documentList, CduStatus.REMOVED, false);

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceStatus("REMOVED");
        }

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_13_01.json", canonicalDocumentMethods, documentList);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_13_02.json", CduStatus.IN_PROGRESS);
    }

    @Test
    @Tag("integration")
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Disabled Documents Updated And Unlocked")
    void testCaseFourteen() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_14");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);
        documentList.addAll(generateCoBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_14_01.json", canonicalDocumentMethods, documentList, CduStatus.REMOVED, true);

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceId(canonicalDocument.getDataSourceId() + "Changed");
            canonicalDocument.getCanonicalPayload().setId(canonicalDocument.getDataSourceId() + "Changed");

            switch (canonicalDocument.getCanonicalDocumentType()) {
                case PAYSTUB:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(0)
                            .getEmployment()
                            .setEmploymentIncomeFrequencyType("Weekly");
                    break;
                case VOE:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(1)
                            .getIncomeItemList().get(0)
                            .getIncomeItemDetail().get(0)
                            .setIncomeFrequencyType("Weekly");
                    break;
                case W2:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getIrsTaxDocument().get(0)
                            .getIrsTaxDocumentData()
                            .getIrsTaxDocumentDataIncomeDetail()
                            .setWagesSalariesTipsEtcAmount(bigD(30000))
                            .setMedicareWagesAndTipsAmount(bigD(30000))
                            .setSocialSecurityBenefitsAmount(bigD(30000));
                    break;
                default:
                    break;
            }
        }

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_14_01.json", canonicalDocumentMethods, documentList);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_14_02.json", CduStatus.IN_PROGRESS);
    }

    @Test
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Disabled Documents Updated But Chk Rules Were Not Waived")
    void testCaseFifteen() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_15");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);
        documentList.addAll(generateCoBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_15_01.json", canonicalDocumentMethods, documentList, CduStatus.REMOVED, false);

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceId(canonicalDocument.getDataSourceId() + "Changed");
            canonicalDocument.getCanonicalPayload().setId(canonicalDocument.getDataSourceId() + "Changed");

            switch (canonicalDocument.getCanonicalDocumentType()) {
                case PAYSTUB:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(0)
                            .getEmployment()
                            .setEmploymentIncomeFrequencyType("Weekly");
                    break;
                case VOE:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(1)
                            .getIncomeItemList().get(0)
                            .getIncomeItemDetail().get(0)
                            .setIncomeFrequencyType("Weekly");
                    break;
                case W2:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getIrsTaxDocument().get(0)
                            .getIrsTaxDocumentData()
                            .getIrsTaxDocumentDataIncomeDetail()
                            .setWagesSalariesTipsEtcAmount(bigD(30000))
                            .setMedicareWagesAndTipsAmount(bigD(30000))
                            .setSocialSecurityBenefitsAmount(bigD(30000));
                    break;
                default:
                    break;
            }
        }

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_15_01.json", canonicalDocumentMethods, documentList);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_15_02.json", CduStatus.IN_PROGRESS);
    }

    @Test
    @Description("IA-2248 IA-2214 Check If There Was No Regression In Calculation When Folder Has Been Disabled Documents Updated")
    void testCaseSixteen() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_TC_16");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);
        documentList.addAll(generateCoBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods));

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_16.json", canonicalDocumentMethods, documentList, CduStatus.REMOVED, false);

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceId(canonicalDocument.getDataSourceId() + "Changed");
            canonicalDocument.getCanonicalPayload().setId(canonicalDocument.getDataSourceId() + "Changed");

            switch (canonicalDocument.getCanonicalDocumentType()) {
                case PAYSTUB:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(0)
                            .getEmployment()
                            .setEmploymentIncomeFrequencyType("Weekly");
                    break;
                case VOE:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(1)
                            .getIncomeItemList().get(1)
                            .getIncomeItemDetail().get(0)
                            .setIncomeFrequencyType("Weekly");
                    break;
                case W2:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getIrsTaxDocument().get(0)
                            .getIrsTaxDocumentData()
                            .getIrsTaxDocumentDataIncomeDetail()
                            .setWagesSalariesTipsEtcAmount(bigD(30000))
                            .setMedicareWagesAndTipsAmount(bigD(30000))
                            .setSocialSecurityBenefitsAmount(bigD(30000));
                    break;
                default:
                    break;
            }
        }
        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_16.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @Description("IA-2576 IA-2785 Check if changing paystub pay type from hourly to salaried works correctly and fields are calculated")
    void checkIfChangingPaystubPayTypeFromHourlyToSalariedWorksCorrectlyAndFieldsAreCalculated() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_PType_01");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        RestCanonicalDocument paystubYtdPrimaryBorrower = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderSampleTests.getFolderId(), PAYSTUB);
        canonicalDocumentMethods.setBorrowerAndJob(paystubYtdPrimaryBorrower, folderBuilderSampleTests.getFolderId(),
                folderBuilderSampleTests.getBorrowerFullName(),
                folderBuilderSampleTests.getBorrowerCurrentEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(paystubYtdPrimaryBorrower,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                PAYSTUB);
        canonicalDocumentMethods.setPaystubBasePayValues(paystubYtdPrimaryBorrower, bigD(10), bigD(400), bigD(3000), null);
        paystubYtdPrimaryBorrower.setDataSourceId("Paystub-default-hourly");
        paystubYtdPrimaryBorrower.getCanonicalPayload().setId("Paystub-default-hourly-hourly");

        canonicalDocumentMethods.uploadCanonicalDocument(paystubYtdPrimaryBorrower, paystubYtdPrimaryBorrower.getSiteGuid());

        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId()).getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment()).getBasePay();
        Map footnote = incomePartBasePay.getFootnotes();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary paystub = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId())
                    .getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(PAYSTUB);
            assertAll("Hourly before update assertions",
                    () -> assertTrue(footnote.containsValue("The maximum hours allowed for this pay frequency (173.34 hours) was used instead of the 400 hours reported on the paystub."),
                            "Annotations are missing monthlyAmountAvg footnote message"),
                    () -> assertTrue(footnote.containsValue("Actual YTD Avg Income was not calculated as there are no YTD base amounts available on the paystub."),
                            "Annotations are missing monthlyAmountAvg footnote message"),
                    () -> assertEquals(1,
                            incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getFootnotesIdx().size(),
                            "Monthly Amount Avg footnote index is present"),
                    () -> assertEquals(1,
                            incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountCalculated().getFootnotesIdx().size(),
                            "Monthly Amount Calculated footnote index is present"),
                    () -> assertEquals(bigD(1733.4),
                            bigD(paystub.getDisplayGrossPay().getValue()),
                            "Incorrect displayGrossPay before update"),
                    () -> assertEquals(bigD(0),
                            bigD(paystub.getMonthlyAmountAvg().getValue()),
                            "monthlyAmountAvg was not equal to zero before update"),
                    () -> assertEquals(bigD(1733.4),
                            bigD(paystub.getMonthlyAmountCalculated().getValue()),
                            "Incorrect monthlyAmountCalculated value before update"),
                    () -> assertEquals(IncomeType.HOURLY,
                            paystub.getType().getValue(),
                            "Income type was not hourly before update")
            );
        });

        paystubYtdPrimaryBorrower.getCanonicalPayload()
                .getApplicant().get(0)
                .getEmployer().get(0)
                .getEmployment()
                .setPayType("Salary");
        paystubYtdPrimaryBorrower.getCanonicalPayload()
                .setId("Paystub-default-hourly-salary");
        paystubYtdPrimaryBorrower.getCanonicalPayload()
                .getApplicant().get(0)
                .getEmployer().get(0)
                .getEmployment()
                .setEmploymentIncomeFrequencyType("Monthly");
        paystubYtdPrimaryBorrower.setConversionDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        canonicalDocumentMethods.uploadCanonicalDocument(paystubYtdPrimaryBorrower, paystubYtdPrimaryBorrower.getSiteGuid());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary paystub = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId())
                    .getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(PAYSTUB);
            assertAll("Salaried after update assertions",
                    () -> assertEquals(bigD(3000),
                            bigD(paystub.getDisplayGrossPay().getValue()),
                            "Incorrect displayGrossPay after update"),
                    () -> assertEquals(bigD(4333.06),
                            bigD(paystub.getMonthlyAmountAvg().getValue()),
                            "Incorrect monthlyAmountAvg value after update"),
                    () -> assertEquals(bigD(3000),
                            bigD(paystub.getMonthlyAmountCalculated().getValue()),
                            "Incorrect monthlyAmountCalculated value after update"),
                    () -> assertEquals(IncomeType.SALARIED,
                            paystub.getType().getValue(),
                            "Income type was not salaried after update")
            );
        });
    }

    @Test
    @Description("IA-2576 IA-2785 Check if changing paystub pay type from hourly to salaried works correctly and fields are calculated")
    void checkIfChangingPaystubPayTypeFromSalariedToHourlyWorksCorrectlyAndFieldsAreCalculated() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_PType_02");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

        RestCanonicalDocument paystubYtdPrimaryBorrower = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderSampleTests.getFolderId(), PAYSTUB);
        canonicalDocumentMethods.setBorrowerAndJob(paystubYtdPrimaryBorrower, folderBuilderSampleTests.getFolderId(),
                folderBuilderSampleTests.getBorrowerFullName(),
                folderBuilderSampleTests.getBorrowerCurrentEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(paystubYtdPrimaryBorrower,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                PAYSTUB);
        canonicalDocumentMethods.setPaystubBasePayValues(paystubYtdPrimaryBorrower, null, null, bigD(3000), null);
        paystubYtdPrimaryBorrower.setDataSourceId("Paystub-default-salary");
        paystubYtdPrimaryBorrower.getCanonicalPayload().setId("Paystub-default-salary-salary");

        canonicalDocumentMethods.uploadCanonicalDocument(paystubYtdPrimaryBorrower, paystubYtdPrimaryBorrower.getSiteGuid());

        RestIncomePart incomePartBasePay = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId()).getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment()).getBasePay();
        Map footnote = incomePartBasePay.getFootnotes();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary paystub = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId())
                    .getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(PAYSTUB);
            assertAll("Salaried before update assertions",
                    () -> assertEquals("The YTD gross pay, which may contain more than just base income, was used in the calculation since there are no YTD base pay amounts available on the paystub.",
                            footnote.get(1), "Annotations are missing monthlyAmountAvg footnote message"),
                    () -> assertEquals("1",
                            incomePartBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getFootnotesIdx().get(0),
                            "Footnote index is present"),
                    () -> assertEquals(bigD(3000),
                            bigD(paystub.getDisplayGrossPay().getValue()),
                            "Incorrect displayGrossPay before update"),
                    () -> assertEquals(bigD(4333.06),
                            bigD(paystub.getMonthlyAmountAvg().getValue()),
                            "monthlyAmountAvg was not equal to zero before update"),
                    () -> assertEquals(bigD(3000),
                            bigD(paystub.getMonthlyAmountCalculated().getValue()),
                            "Incorrect monthlyAmountCalculated value before update"),
                    () -> assertEquals(IncomeType.SALARIED,
                            paystub.getType().getValue(),
                            "Income type was not hourly before update")
            );
        });

        paystubYtdPrimaryBorrower.getCanonicalPayload()
                .getApplicant().get(0)
                .getEmployer().get(0)
                .getEmployment()
                .setPayType("Hourly");
        paystubYtdPrimaryBorrower.getCanonicalPayload()
                .setId("Paystub-default-salary-hourly");
        paystubYtdPrimaryBorrower.setConversionDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        canonicalDocumentMethods.uploadCanonicalDocument(paystubYtdPrimaryBorrower, paystubYtdPrimaryBorrower.getSiteGuid());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary paystub = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId())
                    .getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(PAYSTUB);
            assertAll("Hourly after update assertions",
                    () -> assertEquals(bigD(3000),
                            bigD(paystub.getDisplayGrossPay().getValue()),
                            "Incorrect displayGrossPay after update"),
                    () -> assertEquals(bigD(0),
                            bigD(paystub.getMonthlyAmountAvg().getValue()),
                            "Incorrect monthlyAmountAvg value after update"),
                    () -> assertEquals(bigD(3000),
                            bigD(paystub.getMonthlyAmountCalculated().getValue()),
                            "Incorrect monthlyAmountCalculated value after update"),
                    () -> assertEquals(IncomeType.HOURLY,
                            paystub.getType().getValue(),
                            "Income type was not salaried after update")
            );
        });
    }

    @Test
    @Tag("health")
    @Description("IA-2248 IA-2214 IA-2300 IA-2507 Check If There Was No Regression For Getting Income Data From Documents Linked To Employer Using Alias In LoanDoc")
    void testCaseEmployerAlias() {
        String aliasName = "NO JOB IN URLA";
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_EMP_AL");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods, true);

        documentList.removeIf(document -> SummaryDocumentType.VOE.equals(document.getCanonicalDocumentType()));
        documentList.forEach(document ->
                document.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getLegalEntity().getLegalEntityDetail().setFullName(aliasName));

        canonicalDocumentMethods.uploadCanonicalDocumentList(documentList, documentList.get(0).getSiteGuid());

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId());
        response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment())
                .getBasePay()
                .getIncomes().forEach(income -> assertTrue(income.getAllAnnualSummary().isEmpty()));

        Integer applicantId = (Integer) ((HashMap<String, Object>) RestNotIARequests.getChecklistRules(folderBuilderSampleTests.getFolderId()).then().extract().jsonPath().getList("applicants")
                .stream().filter(app -> ((HashMap<String, Object>) app).get("fullName").equals(folderBuilderSampleTests.getBorrowerFullName())).findFirst().orElseThrow(NullPointerException::new)).get("applicantId");

        RestNotIARequests.addEmployerAlias(folderBuilderSampleTests.getFolderId(), applicantId, folderBuilderSampleTests.getBorrowerCurrentEmployment(), aliasName);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestApplicantIncome employer = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId())
                    .getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment());
            RestIncomePart basePay = employer.getBasePay();

            assertAll("Employer merge assertions",
                    () -> assertFalse(employer.getEmployer().getNameAliases().isEmpty()),
                    () -> assertEquals(aliasName, employer.getEmployer().getNameAliases().stream().filter(aliasName::equals).findFirst().orElse(null)),
                    () -> basePay.getIncomes().forEach(income -> assertFalse(income.getAllAnnualSummary().isEmpty())),
                    () -> assertNotNull(basePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB)),
                    () -> assertNotNull(basePay.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(W2)),
                    () -> assertNotNull(basePay.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(W2))
            );
        });
    }

    @Test
    @Description("IA-2087 Check If Income Is Recalculated Correctly When Document Is Updated Bug Still Has Same Datasource Id")
    void testCaseDatasourceUpdate() {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_DT_UP");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        canonicalDocumentMethods.uploadCanonicalDocumentList(documentList, documentList.get(0).getSiteGuid());

        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceStatus("UPDATED");
            canonicalDocument.setConversionDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));


            switch (canonicalDocument.getCanonicalDocumentType()) {
                case PAYSTUB:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(0)
                            .getEmployment()
                            .setEmploymentIncomeFrequencyType("Weekly");
                    break;
                case VOE:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getEmployer().get(1)
                            .getIncomeItemList().get(0)
                            .getIncomeItemDetail().get(0)
                            .setIncomeFrequencyType("Weekly");
                    break;
                case W2:
                    canonicalDocument.getCanonicalPayload().getApplicant().get(0)
                            .getIrsTaxDocument().get(0)
                            .getIrsTaxDocumentData()
                            .getIrsTaxDocumentDataIncomeDetail()
                            .setWagesSalariesTipsEtcAmount(bigD(20000))
                            .setMedicareWagesAndTipsAmount(bigD(20000))
                            .setSocialSecurityBenefitsAmount(bigD(20000));
                    break;
                default:
                    break;
            }
        }

        canonicalDocumentMethods.uploadCanonicalDocumentList(documentList, documentList.get(0).getSiteGuid());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderSampleTests.getFolderId());
            assertAll("Document Datasource update assertions",
                    () -> assertEquals(WEEKLY, response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getFrequency().getValue(),
                            "Paystub frequency was not changed correctly"),
                    () -> assertEquals(bigD(10833.33), bigD(response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getValue()),
                            "Paystub monthly amount was not changed correctly"),

                    () -> assertEquals(WEEKLY, response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                                    .getIncome(folderBuilderSampleTests.getBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(VOE).getFrequency().getValue(),
                            "Previous Voe frequency was not changed correctly"),
                    () -> assertEquals(bigD(17798.39), bigD(response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                                    .getIncome(folderBuilderSampleTests.getBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getValue()),
                            "Previous Voe monthly amount was not changed correctly"),

                    () -> assertEquals(bigD(17931.84), bigD(response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR)
                                    .getAnnualSummaryDocument(W2).getGrossAmount().getValue()),
                            "One year prior W-2 has incorrect Gross Amount income"),
                    () -> assertEquals(bigD(17931.84), bigD(response.getApplicant(folderBuilderSampleTests.getBorrowerFullName())
                                    .getIncome(folderBuilderSampleTests.getBorrowerCurrentEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR)
                                    .getAnnualSummaryDocument(W2).getGrossAmount().getValue()),
                            "Two years prior W-2 has incorrect Gross Amount income")
            );
        });
    }

    @Test
    @EnableIfToggled(propertyName = AUTOMATIC_LOCK, negateState = true, isSiteGuidNeeded = true)
    @Description("IA-2649 Application maintains LOCK & DISABLED states when eligibility flag changes")
    void cduFlagChangeShouldNotTriggerIncomeCalculationWhenLocked() throws InterruptedException {
        IAFolderBuilder folderBuilderSampleTests = createFolderBuilder("IA_2649");
        folderBuilderSampleTests.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        List<RestCanonicalDocument> documentList = generateBorrowerDefaultDocs(folderBuilderSampleTests, canonicalDocumentMethods);

        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_17.json", canonicalDocumentMethods, documentList, CduStatus.LOCKED, true);

        for (RestCanonicalDocument canonicalDocument : documentList)
            canonicalDocument.setDataSourceStatus("REMOVED");

        folderDv = dvFolderClient.findByKeyless(getDvFolderFriendlyId(folderBuilderSampleTests.getFolderId()));
        folderDv.getAttributes().replace(FolderAttributes.CDU_ELIGIBLE.attributeName, "No");
        folderDv = dvFolderClient.updateFolder(folderDv);
        Thread.sleep(TIMEOUT_FORTY_SECONDS);
        folderDv.getAttributes().replace(FolderAttributes.CDU_ELIGIBLE.attributeName, "Yes");
        folderDv = dvFolderClient.updateFolder(folderDv);
        Thread.sleep(TIMEOUT_FORTY_SECONDS);
        assertEquals("Locked", dvFolderClient.findByKeyless(getDvFolderFriendlyId(folderBuilderSampleTests.getFolderId())).getAttributes().get("iu_income_status"));
        uploadAndCompareIncome(folderBuilderSampleTests.getFolderId(), "TC_17.json", canonicalDocumentMethods, documentList);
    }

    @Test
    @Disabled
    @Description("IA-2806 Check If CollaboratorId Is Extracted From LoanDoc Document Instead Of Collaborator Service")
    void checkIfCollaboratorIdAreExtractedFromLoanDocDocument() {
        IAFNMBuilder fnmBuilderCollUpdate = new IAFNMBuilder("IA_CollChn");
        fnmBuilderCollUpdate.generateLoanDocument().restBuild();
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        final String[] updatedCollId = new String[1];
        final String[] updatedCoCollId = new String[1];
        final RestGetResponse[] response = new RestGetResponse[1];

        RestCanonicalDocument oldCanonUrla = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromJson(fnmBuilderCollUpdate.getFolderId(),
                gson.toJson(getDocumentCollections(fnmBuilderCollUpdate.getFolderId(), "URLA")));

        String collId = oldCanonUrla.getCanonicalPayload().getApplicant().get(0).getCollaboratorId();
        String coCollId = oldCanonUrla.getCanonicalPayload().getApplicant().get(1).getCollaboratorId();

        updatedCollId[0] = collId.replace(collId.substring(27), "D66DDEAE6666");
        updatedCoCollId[0] = coCollId.replace(coCollId.substring(27), "D99DDEAE9999");
        oldCanonUrla.getCanonicalPayload().getApplicant().stream()
                .filter(applicant -> "Primary".equals(applicant.getApplicantDetail().getApplicantClassificationType()))
                .findFirst().orElseThrow(NullPointerException::new).setCollaboratorId(updatedCollId[0]);
        oldCanonUrla.getCanonicalPayload().getApplicant().stream()
                .filter(applicant -> "Secondary".equals(applicant.getApplicantDetail().getApplicantClassificationType()))
                .findFirst().orElseThrow(NullPointerException::new).setCollaboratorId(updatedCoCollId[0]);

        canonicalDocumentMethods.uploadCanonicalDocument(oldCanonUrla, oldCanonUrla.getSiteGuid());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            response[0] = RestGetLoanData.getApplicationData(fnmBuilderCollUpdate.getFolderId());
            assertEquals(updatedCollId[0], response[0].getApplicant(fnmBuilderCollUpdate.getBorrowerFullName()).getRefId(), "CollaboratorId for Borrower was not updated from old urla");
            assertEquals(updatedCoCollId[0], response[0].getApplicant(fnmBuilderCollUpdate.getCoBorrowerFullName()).getRefId(), "CollaboratorId for CoBorrower was not updated from old urla");
        });

        fnmBuilderCollUpdate.setDefaultsToNewUrla();

        updatedCollId[0] = collId.replace(collId.substring(27), "A66AAHUE6666");
        updatedCoCollId[0] = coCollId.replace(coCollId.substring(27), "A99AAHUE9999");
        fnmBuilderCollUpdate.canonicalUrlaDocument.getCanonicalPayload().getApplicant().stream()
                .filter(applicant -> "Primary".equals(applicant.getApplicantDetail().getApplicantClassificationType()))
                .findFirst().orElseThrow(NullPointerException::new).setCollaboratorId(updatedCollId[0]);
        fnmBuilderCollUpdate.canonicalUrlaDocument.getCanonicalPayload().getApplicant().stream()
                .filter(applicant -> "Secondary".equals(applicant.getApplicantDetail().getApplicantClassificationType()))
                .findFirst().orElseThrow(NullPointerException::new).setCollaboratorId(updatedCoCollId[0]);

        fnmBuilderCollUpdate.uploadNewUrlaToFolder();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            response[0] = RestGetLoanData.getApplicationData(fnmBuilderCollUpdate.getFolderId());
            assertEquals(updatedCollId[0], response[0].getApplicant(fnmBuilderCollUpdate.getBorrowerFullName()).getRefId(), "CollaboratorId for Borrower was not updated from new urla");
            assertEquals(updatedCoCollId[0], response[0].getApplicant(fnmBuilderCollUpdate.getCoBorrowerFullName()).getRefId(), "CollaboratorId for CoBorrower was not updated from new urla");
        });
    }


    void uploadAndCompareIncome(String folderId, String templateName, RestCanonicalDocumentMethods canonicalDocumentMethods, List<RestCanonicalDocument> canonicalDocuments) {
        uploadAndCompareIncome(folderId, templateName, canonicalDocumentMethods, canonicalDocuments, null, false);
    }

    void uploadAndCompareIncome(String folderId, String templateName, CduStatus cduStatus) {
        uploadAndCompareIncome(folderId, templateName, null, null, cduStatus, false);
    }

    void uploadAndCompareIncome(String folderId, String templateName,
                                RestCanonicalDocumentMethods canonicalDocumentMethods, List<RestCanonicalDocument> canonicalDocuments, CduStatus cduStatus, boolean shouldWaiveRules) {
        Retry.tryRun(TIMEOUT_ONE_MINUTE, TIMEOUT_THREE_MINUTES, () -> {
            if (canonicalDocuments != null && cduStatus != null && RestNotIARequests.getCduStatus(folderId).jsonPath().get("locked") == "true") {
                RestNotIARequests.setCduStatus(folderId, CduStatus.IN_PROGRESS, "Insane");
            }
            if (canonicalDocuments != null)
                canonicalDocumentMethods.uploadCanonicalDocumentList(canonicalDocuments, canonicalDocuments.get(0).getSiteGuid());

            Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
                if (shouldWaiveRules)
                    RestNotIARequests.waiveAllFailedChecklistRules(folderId);
                boolean isLockedFolder = Boolean.parseBoolean(RestNotIARequests.getCduStatus(folderId).jsonPath().get("locked").toString());
                if (cduStatus != null && (!cduStatus.equals(CduStatus.LOCKED) || !isLockedFolder))
                    RestNotIARequests.setCduStatus(folderId, cduStatus, "Insane");

                compareIncome(folderId, templateName);
            });
        });
    }

    RestGetResponse compareIncome(String folderId, String templateName) {
        final RestGetResponse[] finalResponse = new RestGetResponse[1];
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            finalResponse[0] = RestGetLoanData.getApplicationData(folderId);
            List<FailureComparison> failList = compareTwoIncomeResponses(templateName, finalResponse[0], replaceTemplate);
            assertEquals(0, failList.size(), "Received response is different from template:\n" + failList.toString());
        });

        return finalResponse[0];
    }

    List<RestCanonicalDocument> generateBorrowerDefaultDocs(IAFolderBuilder folderBuilder, RestCanonicalDocumentMethods canonicalDocumentMethods) {
        return generateBorrowerDefaultDocs(folderBuilder, canonicalDocumentMethods, true);
    }

    List<RestCanonicalDocument> generateCoBorrowerDefaultDocs(IAFolderBuilder folderBuilder, RestCanonicalDocumentMethods canonicalDocumentMethods) {
        return generateBorrowerDefaultDocs(folderBuilder, canonicalDocumentMethods, false);
    }

    List<RestCanonicalDocument> generateBorrowerDefaultDocs(IAFolderBuilder folderBuilder, RestCanonicalDocumentMethods canonicalDocumentMethods, Boolean isPrimaryBorrower) {
        String borrowerName = isPrimaryBorrower ? folderBuilder.getBorrowerFullName() : folderBuilder.getCoBorrowerFullName();
        String borrowerCurrentJob = isPrimaryBorrower ? folderBuilder.getBorrowerCurrentEmployment() : folderBuilder.getCoBorrowerCurrentEmployment();
        String borrowerPreviousJob = isPrimaryBorrower ? folderBuilder.getBorrowerPreviousEmployment() : folderBuilder.getCoBorrowerPreviousEmployment();
        String idSuffix = String.format("-%s-%s", folderBuilder.getRandomNumber(8), borrowerName.replace(" ", "-"));

        List<RestCanonicalDocument> canonicalDocsList = new ArrayList<>();
        RestCanonicalDocument paystubYtdPrimaryBorrower = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilder.getFolderId(), PAYSTUB);
        canonicalDocumentMethods.setBorrowerAndJob(paystubYtdPrimaryBorrower, folderBuilder.getFolderId(),
                borrowerName,
                borrowerCurrentJob);
        canonicalDocumentMethods.setDatesForSelectedDocument(paystubYtdPrimaryBorrower,
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                PAYSTUB);
        paystubYtdPrimaryBorrower.setDataSourceId("Paystub-ytd" + idSuffix);
        paystubYtdPrimaryBorrower.getCanonicalPayload().setId("Paystub-ytd" + idSuffix);
        canonicalDocsList.add(paystubYtdPrimaryBorrower);

        RestCanonicalDocument w2PreviousYearPrimaryBorrower = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilder.getFolderId(), W2);
        canonicalDocumentMethods.setBorrowerAndJob(w2PreviousYearPrimaryBorrower, folderBuilder.getFolderId(),
                borrowerName,
                borrowerCurrentJob);
        canonicalDocumentMethods.setDatesForSelectedDocument(w2PreviousYearPrimaryBorrower,
                ONE_YEAR_PRIOR.toString(),
                null,
                null,
                W2);
        w2PreviousYearPrimaryBorrower.setDataSourceId("W2-previous" + idSuffix);
        w2PreviousYearPrimaryBorrower.getCanonicalPayload().setId("W2-previous" + idSuffix);
        canonicalDocsList.add(w2PreviousYearPrimaryBorrower);

        RestCanonicalDocument w2TwoYearPriorPrimaryBorrower = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilder.getFolderId(), W2);
        canonicalDocumentMethods.setBorrowerAndJob(w2TwoYearPriorPrimaryBorrower, folderBuilder.getFolderId(),
                borrowerName,
                borrowerCurrentJob);
        canonicalDocumentMethods.setDatesForSelectedDocument(w2TwoYearPriorPrimaryBorrower,
                TWO_YEARS_PRIOR.toString(),
                null,
                null,
                W2);
        w2TwoYearPriorPrimaryBorrower.setDataSourceId("W2-two-yr-prior" + idSuffix);
        w2TwoYearPriorPrimaryBorrower.getCanonicalPayload().setId("W2-two-yr-prior" + idSuffix);
        canonicalDocsList.add(w2TwoYearPriorPrimaryBorrower);

        RestCanonicalDocument voePreviousJobPrimaryBorrower = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilder.getFolderId(), VOE_PREVIOUS);
        canonicalDocumentMethods.setBorrowerAndJob(voePreviousJobPrimaryBorrower, folderBuilder.getFolderId(),
                borrowerName,
                borrowerPreviousJob);
        canonicalDocumentMethods.setDatesForSelectedDocument(voePreviousJobPrimaryBorrower,
                (YEAR_TO_DATE - 4) + "-01-01",
                YEAR_TO_DATE + "-12-31",
                YEAR_TO_DATE + "-12-31",
                VOE_PREVIOUS);
        voePreviousJobPrimaryBorrower.setDataSourceId("Voe-previous-job" + idSuffix);
        voePreviousJobPrimaryBorrower.getCanonicalPayload().setId("Voe-previous-job" + idSuffix);
        canonicalDocsList.add(voePreviousJobPrimaryBorrower);

        return canonicalDocsList;
    }

    private RestGetResponse getTemplateToCompare(String templateName) {
        JsonReader contentBuilder = null;
        try {
            contentBuilder = new JsonReader(new FileReader(new File(toAbsolutePath("/testCaseImports/" + templateName))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(Objects.requireNonNull(contentBuilder), RestGetResponse.class);
    }

    private List<FailureComparison> compareTwoIncomeResponses(String templateName, RestGetResponse actual, Boolean replaceTemplate) {
        if (replaceTemplate) {
            try {
                FileWriter fw = new FileWriter(toAbsolutePath("/testCaseImports/" + templateName), false);
                fw.append(gson.toJson(actual));
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }
        RestGetResponse expected = getTemplateToCompare(templateName);
        return expected.compareTo(actual);
    }
}

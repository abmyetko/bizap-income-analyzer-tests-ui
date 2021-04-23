package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.loanDocTests;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.LoanDocumentTypeOfIncome;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "LoanDocWithBasePayTest")
public class LoanDocWithBasePayTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARLoDWBS");

    @BeforeAll
    public void createFolder() {
        iafolderBuilder.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(iafolderBuilder);
    }

    @BeforeEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    @Test
    @Description("IA-2490 IA-2639 Upload loan document with base pay for all borrowers and check if values are imported")
    void uploadLoanDocAndCheckIfBasePaysAreImported() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.01");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Base Pay selection assertion",
                    () -> assertEquals(bigD(1.10),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getBorrowerFullName()).get(IncomePartType.BASE_PAY)), "StatedBaseIncome value for Borrower was not imported"),
                    () -> assertEquals(bigD(1.01),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getCoBorrowerFullName()).get(IncomePartType.BASE_PAY)), "StatedBaseIncome value for CoBorrower was not imported"));
        });
    }

    @Test
    @Description("IA-2490 IA-2639 Check if Base Pay is selected by default Ii FNM has Base Pay with income greater than one")
    void uploadLoanDocAndCheckIfBasePayAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Base Pay selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay wasn't selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay wasn't selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2490 IA-2639 Check if Base Pay are selected by default if FNM has Base Pay with income greater than one and documents are present")
    void uploadLoanDocAndIncomeDocumentsAndCheckIfBasePayAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10");
        iafolderBuilder.uploadNewLoanDocument();

        DataUploadObject dataUpload = createUploadObject(iafolderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Base Pay selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().
                            getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay wasn't selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().
                            getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay wasn't selected for co-borrower"),
                    () -> assertEquals(bigD(1.10),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getBorrowerFullName()).get(IncomePartType.BASE_PAY)), "StatedBasePayIncome value for Borrower was not imported"),
                    () -> assertEquals(bigD(1.10),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getCoBorrowerFullName()).get(IncomePartType.BASE_PAY)), "StatedBasePayIncome value for CoBorrower was not imported"));
        });
    }

    @Test
    @Description("IA-2490 IA-2639 Check if Base Pay is selected by default only for applicants with Base Pay income greater than one")
    void uploadLoanDocWithBasePayOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Base Pay selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2490 IA-2639 Check if Base Pay is selected by default only for applicants with base pay income greater than one regardless of documents income")
    void uploadLoanDocWithBasePayAndIncomeDocumentsOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        DataUploadObject dataUpload = createUploadObject(iafolderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Base Pay selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2639 Check If Base Pay and primary job Is selected by default only for applicants with Base Pay income greater than one")
    void uploadLoanDocWithBasePayAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Primary job and base pay selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job was selected for co-borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2843 Check if W2 is selected if Base Pay is selected")
    void uploadLoanDocWithBasePayAndCheckIfW2IsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getSelected(), "W2 should be selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job should be selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay should be selected for borrower"));
        });
    }

    @Test
    @Description("IA-2843 Check if W2 is unselected if all income type are unselected")
    void uploadLoanDocWithoutAnyIncomeTypesAndCheckIfW2IsUnselected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getSelected(), "W2 shouldn't be selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job shouldn't be selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Base Pay shouldn't be selected for borrower"));
        });
    }

    @Test
    @Description("IA-2672 Check if applicant is selected if Base Pay is selected")
    void uploadLoanDocWithBasePayAndCheckIfApplicantIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getSelected(), "W2 should be selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getSelected(), "Applicant should be selected for borrower"));
        });
    }

    @Test
    @Description("IA-2672 Check if applicant is unselected if all income type are unselected")
    void uploadLoanDocWithoutAnyIncomeTypesAndCheckIfApplicantIsUnselected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getSelected(), "W2 shouldn't be selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getSelected(), "Applicant shouldn't be selected for borrower"));
        });
    }
}
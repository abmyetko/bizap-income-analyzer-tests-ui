package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.commissions.loanDocTests;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.COMMISSIONS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings("rawtypes")
@EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock("LoanDocWithCommissionsTest")
public class LoanDocWithCommissionsTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARLoDWCom");

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
    @Description("IA-2156 IA-2639 Check If Commissions Are Selected By Default If LoanDoc Has Commission With Income Greater Than One")
    void uploadLoanDocAndCheckIfCommissionsAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "1.10");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions wasn't selected for borrower"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions wasn't selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-2156 IA-2639 Check If Commissions Are Selected By Default If LoanDoc Has Commission With Income Greater Than One And Documents Are Present")
    void uploadLoanDocAndIncomeDocumentsAndCheckIfCommissionsAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "1.10");
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
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "2500")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions wasn't selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions wasn't selected for co-borrower"),
                    () -> assertEquals(bigD(1.10),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getBorrowerFullName()).get(IncomePartType.COMMISSIONS)), "DeclaredIncome for Commission value for Borrower was not imported"),
                    () -> assertEquals(bigD(1.10),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getCoBorrowerFullName()).get(IncomePartType.COMMISSIONS)), "DeclaredIncome for Commission value for CoBorrower was not imported"));
        });
    }

    @Test
    @Description("IA-2156 IA-2639 Check If Commissions Is Selected By Default Only For Applicants With Ot Income Greater Than One")
    void uploadLoanDocWithCommissionsOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "0.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions wasn't selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-2156 IA-2639 Check If Commissions Is Selected By Default Only For Applicants With Ot Income Greater Than One Regardless Of Documents Income")
    void uploadLoanDocWithCommissionsAndIncomeDocumentsOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "0.00");
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
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "2500")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions wasn't selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-2639 Check If Commissions and primary job Is Selected By Default Only For Applicants With Base Pay Income Greater Than One")
    void uploadLoanDocWithCommisssionsAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Primary job and Bonus selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job was selected for co-borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commisssions was selected for co-borrower"));
        });
    }
}
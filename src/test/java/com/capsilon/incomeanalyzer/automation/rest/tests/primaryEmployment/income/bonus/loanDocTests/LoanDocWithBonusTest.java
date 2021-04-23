package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.bonus.loanDocTests;

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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.BONUS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings("rawtypes")
@Execution(CONCURRENT)
@EnableIfToggled(propertyName = BONUS_TOGGLE)
@ResourceLock("LoanDocWithBonusTest")
public class LoanDocWithBonusTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARLoDWBo");

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
    @Description("IA-2489 IA-2639 Upload FNM with Bonus for all borrowers and check if values are imported")
    void uploadLoanDocAndCheckIfBonusesAreImported() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "500.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus imported values assertion",
                    () -> assertEquals(bigD(300.00),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getBorrowerFullName()).get(IncomePartType.BONUS)), "DeclaredIncome for Bonus value for Borrower was not imported"),
                    () -> assertEquals(bigD(500.00),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getCoBorrowerFullName()).get(IncomePartType.BONUS)), "DeclaredIncome for Bonus value for CoBorrower was not imported"));
        });
    }

    @Test
    @Description("IA-2489 IA-2639 Check if Bonus is selected by default if loan document has Bonus with income greater than one")
    void uploadLoanDocAndCheckIfBonusesAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "500.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus wasn't selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus wasn't selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2489 IA-2639 Check if Bonuses are selected by default if FNM has Bonus Wwith income greater than one and documents are present")
    void uploadLoanDocAndIncomeDocumentsAndCheckIfBonusesAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "500.00");
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
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypeBonus().getSelected(), "Bonus wasn't selected for borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypeBonus().getSelected(), "Bonus wasn't selected for co-borrower"),
                    () -> assertEquals(bigD(300.00),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getBorrowerFullName()).get(IncomePartType.BONUS)), "DeclaredIncome for Bonus value for Borrower was not imported"),
                    () -> assertEquals(bigD(500.00),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getCoBorrowerFullName()).get(IncomePartType.BONUS)), "DeclaredIncome for Bonus value for CoBorrower was not imported"));
        });
    }

    @Test
    @Description("IA-2489 IA-2639 Check if Bonus is selected by default only for applicants with ot income greater than one")
    void uploadLoanDocWithBonusPayOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "0.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2489 IA-2639 Check if Bonus is selected by default only for applicants with OT income greater than one regardless of documents income")
    void uploadLoanDocWithBonusPayAndIncomeDocumentsOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "0.00");
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
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2639 Check if Bonus and primary job is selected by default only for applicants with Base Pay income greater than one")
    void uploadLoanDocWithBasePayAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Primary job and Bonus selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job was selected for co-borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for co-borrower"));
        });
    }
}
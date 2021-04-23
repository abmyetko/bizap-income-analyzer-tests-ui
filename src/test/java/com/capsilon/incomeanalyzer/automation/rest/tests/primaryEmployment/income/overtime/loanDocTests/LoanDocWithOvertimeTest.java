package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.overtime.loanDocTests;

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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.OVERTIME_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings("rawtypes")
@EnableIfToggled(propertyName = OVERTIME_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock("LoanDocWithOvertimeTest")
public class LoanDocWithOvertimeTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARLoDWOv");

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
    @Description("IA-1285 IA-2639 Check If Overtime Is Selected If LoanDoc Has Ot Income Greater Than One")
    void uploadLoanDocAndCheckIfOvertimeAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "500.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "1.10");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Overtime selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime wasn't selected for borrower"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime wasn't selected for co-borrower"),
                    () -> assertEquals(bigD(500.00),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getBorrowerFullName()).get(IncomePartType.OVERTIME)), "DeclaredIncome for Overtime value for Borrower was not imported"),
                    () -> assertEquals(bigD(1.10),
                            bigD(getResponse.getPrimaryDeclaredIncomeForLoanDocumentType(iafolderBuilder.getCoBorrowerFullName()).get(IncomePartType.OVERTIME)), "DeclaredIncome for Overtime value for CoBorrower was not imported"));
        });
    }

    @Test
    @Description("IA-1285 IA-2639 Check If Overtime Is Selected If LoanDoc Has Ot Income Greater Than One And Documents Were Uploaded")
    void uploadLoanDocAndIncomeDocumentsAndCheckIfOvertimeAreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "1.10");
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
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "2500")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Overtime selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime wasn't selected for borrower"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime wasn't selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-1285 IA-2639 Check If Overtime Is Selected For One Borrower If He Has Ot Income Greater Than One On LoanDoc")
    void uploadLoanDocWithOvertimeOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "0.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("overtime selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime wasn't selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-1285 IA-2639 Check If Overtime Is Selected For One Borrower If He Has Ot Income Greater Than One On LoanDoc And Documents For Both Were Uploaded")
    void uploadLoanDocWithOvertimeAndIncomeDocumentsOnlyForBorrowerAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "0.00");
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
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "2500")))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("overtime selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime wasn't selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-2639 Check If Overtime and primary job Is Selected By Default Only For Applicants With Base Pay Income Greater Than One")
    void uploadLoanDocWithOvertimeAndCheckIfSelectedForBorrowerAndUnselectedForCoBorrower() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Primary job and Overtime selection assertion",
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job was selected for co-borrower"),
                    () -> assertTrue(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime wasn't selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for co-borrower"));
        });
    }
}
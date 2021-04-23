package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.overtime.loanDocTests;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings("rawtypes")
@EnableIfToggled(propertyName = OVERTIME_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock("LoanDocWithoutOvertimeTest")
public class LoanDocWithoutOvertimeTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARLoDNoOv");

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
    @Description("IA-1334 IA-2639 Check If Overtime Is Not Selected If LoanDoc Was Uploaded With One Ot Income")
    void checkIfOvertimeAreNotSelectedAfterUploadLoanDocWith0OvertimeValues() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "0.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "0.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("overtime selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-1334 IA-2639 Check If Overtime Is Not Selected If LoanDoc Was Uploaded With One Ot Income And Documents Were Uploaded")
    void uploadLoanDocWith0OvertimeValuesAndDocumentsAndCheckIfOvertimeAreNotSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "0.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
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
                        "03/01/" + ONE_YEAR_PRIOR,
                        "03/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("overtime selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-1334 IA-2639 Check If Overtime Is Not Selected If LoanDoc Was Uploaded With Ot Without Income Value")
    void checkIfOvertimeAreNotSelectedAfterUploadLoanDocWithEmptyOvertimeValues() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("overtime selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-1334 IA-2639 Check If Overtime Is Not Selected If LoanDoc Was Uploaded With Ot Without Income Value And Documents Were Uploaded")
    void uploadLoanDocWithEmptyOvertimeValuesAndDocumentsAndCheckIfOvertimeAreNotSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iafolderBuilder.getBorrowerSSN(), "")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "");
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
                        "03/01/" + ONE_YEAR_PRIOR,
                        "03/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("overtime selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected(), "Overtime was selected for co-borrower")
            );
        });
    }
}

package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.commissions.loanDocTests;

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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.COMMISSIONS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings("rawtypes")
@EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock("LoanDocWithoutCommissionsTest")
public class LoanDocWithoutCommissionsTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARLoDNoCo");

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
    @Description("IA-2155 IA-2639 Check If Commissions Are Not Selected If LoanDoc Has Commission With Income Equal To One")
    void checkIfCommissionsAreNotSelectedAfterUploadLoanDocWith0CommissionsValues() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "0.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "0.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-2155 IA-2639 Check If Commissions Are Not Selected If LoanDoc Has Commission With Income Equal To One Regardless Of Document Values")
    void uploadLoanDocWith0CommissionsValuesAndDocumentsAndCheckIfCommissionsAreNotSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "0.00")
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
                        "03/01/" + ONE_YEAR_PRIOR,
                        "03/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-2155 IA-2639 Check If Commissions Are Not Selected If LoanDoc Has Commission With Blank Income")
    void checkIfCommissionsAreNotSelectedAfterUploadLoanDocWithEmptyCommissionsValues() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for co-borrower")
            );
        });
    }

    @Test
    @Description("IA-2155 IA-2639 Check If Commissions Are Not Selected If LoanDoc Has Commission With Blank Income Regardless Of Document Income")
    void uploadLoanDocWithEmptyCommissionsValuesAndDocumentsAndCheckIfCommissionsAreNotSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iafolderBuilder.getBorrowerSSN(), "")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "");
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
                        "03/01/" + ONE_YEAR_PRIOR,
                        "03/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "2500")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Commissions selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for borrower"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected(), "Commissions was selected for co-borrower")
            );
        });
    }
}

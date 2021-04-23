package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.bonus.loanDocTests;

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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.BONUS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings("rawtypes")
@Execution(CONCURRENT)
@EnableIfToggled(propertyName = BONUS_TOGGLE)
@ResourceLock("LoanDocWithoutBonusTest")
public class LoanDocWithoutBonusTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARLoDNoBo");

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
    @Description("IA-2489 IA-2639 Check If Bonuses Are Not Selected If LoanDoc Has Bonus With Income Equal To One")
    void checkIfBonusesAreNotSelectedAfterUploadLoanDocWith0BonusesValues() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "0.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "0.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2489 IA-2639 Check If Bonuses Are Not Selected If LoanDoc Has Bonus With Income Equal To One Regardless Of Document Values")
    void uploadLoanDocWith0BonusesValuesAndDocumentsAndCheckIfBonusAreNotSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "0.00")
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
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2489 IA-2639 Check If Bonuses Are Not Selected If LoanDoc Has Bonus With Blank Income")
    void checkIfBonusesAreNotSelectedAfterUploadLoanDocWithEmptyBonusValues() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for co-borrower"));
        });
    }

    @Test
    @Description("IA-2489 IA-2639 Check If Bonuses Are Not Selected If LoanDoc Has Bonus With Blank Income Regardless Of Document Income")
    void uploadLoanDocWithEmptyBonusesValuesAndDocumentsAndCheckIfBonusesAreNotSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBonus(iafolderBuilder.getBorrowerSSN(), "")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "");
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
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Bonus selection assertion",
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for borrower"),
                    () -> assertFalse(getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2()
                            .getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected(), "Bonus was selected for co-borrower"));
        });
    }
}
package com.capsilon.incomeanalyzer.automation.rest.tests.secondaryEmployment.loanDocTests;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.LoanDocumentTypeOfIncome;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryJobSelectionsFromLoanDocTest")
public class SecondaryJobSelectionsFromLoanDocTest extends TestBaseRest {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IARSecSel");

    @BeforeAll
    public void generateDocs() {
        iafolderBuilder.generateLoanDocument().restBuild();
    }

    @Test
    @Description("IA-2555 Check if income fields for secondary job were imported ")
    void checkIfIncomeFieldsForSecondaryEmploymentsWereImported() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "10.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "12.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Secondary job income field assertions selection assertion",
                    () -> assertEquals(bigD(10.00),
                            bigD(getResponse.getSecondaryJobMonthlyIncomeValue(iafolderBuilder.getBorrowerFullName(), iafolderBuilder.getBorrowerSecondEmployment())), "IncomeItem value for secondary job  for Borrower was not imported"),
                    () -> assertEquals(bigD(12.00),
                            bigD(getResponse.getSecondaryJobMonthlyIncomeValue(iafolderBuilder.getCoBorrowerFullName(), iafolderBuilder.getCoBorrowerSecondEmployment())), "IncomeItem value for secondary job  for CoBorrower was not imported")
            );
        });
    }
    @Order(3)
    @Test
    @Description("IA-2557 Autoselect secondary job if IncomeItem is greater than one ")
    void checkIfSecondaryJobIsSelectedByDefaultWithIncomeItemIsGreaterThanOne() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "10.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Secondary job selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getSelected(), "Secondary job for Borrower should be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getSelected(), "Secondary job for CoBorrower shouldn't be selected")
            );
        });
    }
    @Order(4)
    @Test
    @Description("IA-2577 Autoselect secondary base income if secondary job is selected ")
    void checkIfSecondaryJobBasePayIsSelectedIfSecondaryJobIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "300.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "10.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("Secondary job selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getSelected(), "Secondary job for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Secondary job base pay for Borrower shouldn't be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getSelected(), "Secondary job for CoBorrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Secondary job base pay for CoBorrower should be selected")
            );
        });
    }
    @Order(1)
    @Test
    @Description("IA-2843 Autoselect W2 if primary job is Selected ")
    void checkIfW2IsSelectedIfPrimaryJobIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "300")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job for Borrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Primary job base pay for Borrower should be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getSelected(), "Secondary job for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Secondary job base pay for Borrower shouldn't be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Borrower should be selected")
            );
        });
    }

    @Test
    @Description("IA-2843 Autoselect W2 if secondary job is selected ")
    void checkIfW2IsSelectedIfSecondaryJobIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.10")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Primary job base pay for Borrower shouldn't be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getSelected(), "Secondary job for Borrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Secondary job base pay for Borrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Borrower should be selected")
            );
        });
    }
    @Order(0)
    @Test
    @Description("IA-2843 Autodeselect W2 if primary and secondary is deselected ")
    void checkIfWa2IsSelectedIfPrimaryJobIsSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "500.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getSelected(), "Primary job for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Primary job base pay for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getSelected(), "Secondary job for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getSelected(), "Secondary job base pay for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Borrower shouldn't be selected")
            );
        });
    }

    @Test
    @Description("IA-2672 Autoselect Applicant if primary job and W2 is selected")
    void checkIfApplicantIsSelectedIfPrimaryJobAndW2AreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.10")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.0")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Borrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Coborrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getSelected(), "Borrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getSelected(), "Coborrower should be selected"));
        });
    }

    @Test
    @Description("IA-2672 Autoselect Applicant if secondary job and W2 is selected")
    void checkIfApplicantIsSelectedIfSecondaryJobAndW2AreSelected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.10")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.10");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Borrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Coborrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getSelected(), "Borrower should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getSelected(), "Coborrower should be selected"));
        });
    }

    @Test
    @Description("IA-2672 Autodeselect Applicant if secondary job and W2 is unselected")
    void checkIfApplicantIsUnselectedIfSecondaryJobAndW2AreUnselected() {
        iafolderBuilder.generateLoanDocumentWithNoIncome().setPrimaryJobBasePay(iafolderBuilder.getBorrowerSSN(), "1.00")
                .addLoanDocumentTypeOfIncome(iafolderBuilder.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.00")
                .addNewEmployment(iafolderBuilder.getBorrowerSecondEmployment(),
                        iafolderBuilder.getBorrowerSecondJobStartDate(),
                        iafolderBuilder.getBorrowerSecondJobEndDate(),
                        true,
                        true,
                        "1.00")
                .addNewEmployment(iafolderBuilder.getCoBorrowerSecondEmployment(),
                        iafolderBuilder.getCoBorrowerPreviousJobStartDate(),
                        iafolderBuilder.getCoBorrowerSecondJobEndDate(),
                        false,
                        true,
                        "1.00");
        iafolderBuilder.uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(iafolderBuilder.getFolderId());

            assertAll("W2 selection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getIncomeCategoryW2().getSelected(), "W2 for COBorrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getBorrowerFullName()).getSelected(), "Borrower shouldn't be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(iafolderBuilder.getCoBorrowerFullName()).getSelected(), "Coborrower shouldn't be selected"));
        });
    }
}
package com.capsilon.incomeanalyzer.automation.rest.tests;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAMismoBuilder;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.core.annotation.Order;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_FIVE_SECONDS;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_TWENTY_SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

//TODO
@Disabled("TODO - to unlock after D1C release")
@Execution(CONCURRENT)
@ResourceLock(value = "DayOneCertaintyRestTest")
public class DayOneCertaintyRestTest extends TestBaseRest {

    private IAMismoBuilder builder = new IAMismoBuilder("IARestD1C");

    @BeforeAll
    void setupTestFolder() {
        builder.generateLoanDocument().restBuild();
    }

    @Order(1)
    @Test
    @Description("IA-2961 IA-2962 D1C shows up only for Borrower")
    void checkIfOnlyBorrowerHasIncomeVerifiedOnTrue() {
        builder.generateLoanDocument()
                .setBorrowerIncomeVerified(true)
                .setCoBorrowerIncomeVerified(false)
                .uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(builder.getFolderId());

            assertAll("D1C assertions",
                    () -> assertTrue(response.getApplicant(builder.getBorrowerFullName()).getIncomeVerified(), "[Borrower] IncomeVerified should be true"),
                    () -> assertFalse(response.getApplicant(builder.getBorrowerFullName()).getSelected(), "[Borrower] shouldn't be selected"),
                    () -> assertFalse(response.getApplicant(builder.getCoBorrowerFullName()).getIncomeVerified(), "[CoBorrower] IncomeVerified should be false"),
                    () -> assertTrue(response.getApplicant(builder.getCoBorrowerFullName()).getSelected(), "[CoBorrower] should be selected"));
        });
    }

    @Order(2)
    @Test
    @Description("IA-2961 IA-2962 D1C don't show up for both Borrowers")
    void checkIfBorrowersHaveIncomeVerifiedOnFalse() {
        builder.generateLoanDocument()
                .setBorrowerIncomeVerified(false)
                .setCoBorrowerIncomeVerified(false)
                .uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(builder.getFolderId());

            assertAll("D1C assertions",
                    () -> assertFalse(response.getApplicant(builder.getBorrowerFullName()).getIncomeVerified(), "[Borrower] IncomeVerified should be false"),
                    () -> assertTrue(response.getApplicant(builder.getBorrowerFullName()).getSelected(), "[Borrower] should be selected"),
                    () -> assertFalse(response.getApplicant(builder.getCoBorrowerFullName()).getIncomeVerified(), "[Borrower] IncomeVerified should be false"),
                    () -> assertTrue(response.getApplicant(builder.getBorrowerFullName()).getSelected(), "[Borrower] should be selected"));
        });
    }

    @Order(3)
    @Test
    @Description("IA-2961 IA-2962 D1C don't show up for both Borrowers-null values")
    void checkIfBorrowersHaveIncomeVerifiedOnFalseForNulls() {
        builder.generateLoanDocument()
                .uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(builder.getFolderId());

            assertAll("D1C assertions",
                    () -> assertNull(response.getApplicant(builder.getBorrowerFullName()).getIncomeVerified(), "[Borrower] IncomeVerified should be null"),
                    () -> assertTrue(response.getApplicant(builder.getBorrowerFullName()).getSelected(), "[Borrower] should be selected"),
                    () -> assertNull(response.getApplicant(builder.getCoBorrowerFullName()).getIncomeVerified(), "[Borrower] IncomeVerified should be null"),
                    () -> assertTrue(response.getApplicant(builder.getCoBorrowerFullName()).getSelected(), "[Borrower] should be selected"));
        });
    }

    @Order(4)
    @Test
    @Description("IA-2961 IA-2962 D1C shows up for both Borrowers")
    void checkIfBorrowersHaveIncomeVerifiedOnTrue() {
        builder.generateLoanDocument()
                .setBorrowerIncomeVerified(true)
                .setCoBorrowerIncomeVerified(true)
                .uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(builder.getFolderId());

            assertAll("D1C assertions",
                    () -> assertTrue(response.getApplicant(builder.getBorrowerFullName()).getIncomeVerified(), "[Borrower] IncomeVerified should be true"),
                    () -> assertFalse(response.getApplicant(builder.getBorrowerFullName()).getSelected(), "[Borrower] shouldn't be selected"),
                    () -> assertTrue(response.getApplicant(builder.getCoBorrowerFullName()).getIncomeVerified(), "[CoBorrower] IncomeVerified should be true"),
                    () -> assertFalse(response.getApplicant(builder.getBorrowerFullName()).getSelected(), "[Borrower] shouldn't be selected"));
        });
    }
}

package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.BIG_DECIMAL_PRECISION_EIGHT_POINTS;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "EndYearScenarioTest")
public class EndYearScenarioTest extends TestBaseRest {


    private final IAFolderBuilder folderBuilderEndptTest = createFolderBuilder("IARestEndYr");

    @BeforeAll
    void setupTestFolder() {
        folderBuilderEndptTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderEndptTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "01/07/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "01/01/" + (YEAR_TO_DATE + 1),
                        "01/31/" + (YEAR_TO_DATE + 1),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getCoBorrowerFullName(),
                        folderBuilderEndptTest.getCoBorrowerSSN(),
                        folderBuilderEndptTest.getCoBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getCoBorrowerCurrentEmployment(),
                        "12/02/" + YEAR_TO_DATE,
                        "01/01/" + (YEAR_TO_DATE + 1),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("12/29/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getCoBorrowerFullName(),
                        folderBuilderEndptTest.getCoBorrowerSSN(),
                        folderBuilderEndptTest.getCoBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getCoBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("01/07/" + (YEAR_TO_DATE + 1)))
                .importDocumentList();

        RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId());
    }

    @Test
    void checkIfPaystubWithNextYearPayDateIsShownInNextYearWithCorrectDate() {
        RestIncomePart incomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getCoBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getCoBorrowerCurrentEmployment()).getBasePay();

        assertEquals("01/07/" + (YEAR_TO_DATE + 1),
                incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(),
                String.format("Incorrect next year paystub end date: %s Check if document exist and is correct",
                        incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue()));
    }

    @Test
    void checkIfPaystubWithNextYearPeriodEndDateIsShownInYtdWithCorrectDate() {
        RestIncomePart incomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getCoBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getCoBorrowerCurrentEmployment()).getBasePay();

        assertEquals("12/31/" + YEAR_TO_DATE,
                incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(),
                String.format("Incorrect this year paystub end date: %s Check if document exist and is correct",
                        incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue()));
    }

    @Test
    void checkIfYtdPaystubHadCorrectDateUsedToCalculateMonths() {
        RestIncomePart incomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getCoBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getCoBorrowerCurrentEmployment()).getBasePay();

        assertEquals(YEAR_TO_DATE + "-12-31",
                incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getTo(),
                String.format("Incorrect this year paystub months worked to date: %s Check if document exist and is correct",
                        incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getTo()));
    }

    @Test
    void checkIfNextYearPaystubHadCorrectDateUsedToCalculateMonths() {
        RestIncomePart incomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getCoBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getCoBorrowerCurrentEmployment()).getBasePay();

        assertEquals((YEAR_TO_DATE + 1) + "-01-07",
                incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getTo(),
                String.format("Incorrect next year paystub months worked to date: %s Check if document exist and is correct",
                        incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getTo()));
    }

    @Test
    void checkIfYtdPaystubHaveMonthsWorkedCorrectlyCalculated() {
        RestIncomePart incomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getCoBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getCoBorrowerCurrentEmployment()).getBasePay();

        assertEquals(bigD(12, BIG_DECIMAL_PRECISION_EIGHT_POINTS),
                incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getValue(),
                String.format("Incorrect this year paystub months worked value: %s Check if document exist and is correct",
                        incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getValue()));
    }

    @Test
    void checkIfNextYearPaystubHaveMonthsWorkedCorrectlyCalculated() {
        RestIncomePart incomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getCoBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getCoBorrowerCurrentEmployment()).getBasePay();

        assertEquals(bigD(0.23, BIG_DECIMAL_PRECISION_EIGHT_POINTS),
                incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getValue(),
                String.format("Incorrect next year paystub months worked value: %s Check if document exist and is correct",
                        incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getValue()));
    }

    @Test
    @Disabled("Disabled until IA-2606 is resolved")
    void checkIfNextYearDocumentUpdatesYtdDate() {
        RestIncomePart incomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment()).getBasePay();

        assertAll("Borrower ytd + 1 year incomes",
                () -> assertEquals("01/31/" + (YEAR_TO_DATE + 1),
                        incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(),
                        String.format("Incorrect next year paystub date: %s Check if document exist and is correct",
                                incomePart.getIncome(YEAR_TO_DATE + 1).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue())),
                () -> assertEquals("12/31/" + YEAR_TO_DATE,
                        incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(),
                        String.format("Incorrect this year paystub date: %s Check if document exist and is correct",
                                incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue())),
                () -> assertEquals("12/31/" + YEAR_TO_DATE,
                        incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE).getPayPeriodEndDate().getValue(),
                        String.format("Incorrect this year voe date: %s Check if document exist and is correct",
                                incomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE).getPayPeriodEndDate().getValue())),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR,
                        incomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(),
                        String.format("Incorrect prior year paystub date: %s Check if document exist and is correct",
                                incomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue()))
        );
    }
}


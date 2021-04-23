package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.bigD;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "PaystubMissingDatesTest")
class PaystubMissingDatesTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderPaystubMissingDatesTest = createFolderBuilder("IARMissDate");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilderPaystubMissingDatesTest
                .setBorrowerYearsOnThisJob("4")
                .generateLoanDocument().restBuild();

        dataUpload = createUploadObject(folderBuilderPaystubMissingDatesTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment(),
                        null,
                        null,
                        new PaystubData.PaystubIncomeRow(REGULAR, "100.50", "180.00", "296.00", "1,308.20"))
                        .setPayDate("03/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        null,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "296.00", "1,308.20"))
                        .setPayDate("03/31/" + YEAR_TO_DATE))
                .importDocumentList();

        RestGetLoanData.getApplicationData(folderBuilderPaystubMissingDatesTest.getFolderId());
    }

    @Test
    @Order(1)
    @Description("IA-2365 IA-1773 Check If Paystubs With Missing Dates Are Showing With Correct Dates")
    void checkIfPaystubsWithMissingDatesAreShowingWithCorrectDates() {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilderPaystubMissingDatesTest.getFolderId());

        RestPartIncomeAnnualSummary paystubYTDBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Borrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("UNKNOWN", paystubYTDBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(null, paystubYTDBorrower.getMonthlyAmountCalculated().getValue()),
                () -> assertEquals(bigD(436.07), bigD(paystubYTDBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(null, paystubYTDBorrower.getDifference()),
                () -> assertEquals(null, paystubYTDBorrower.getVariance()),
                () -> assertEquals("03/31/" + YEAR_TO_DATE, paystubYTDBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(3), bigD(paystubYTDBorrower.getMonths().getValue())));

        RestPartIncomeAnnualSummary paystubYTDCoBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("CoBorrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("UNKNOWN", paystubYTDCoBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDCoBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(null, paystubYTDCoBorrower.getMonthlyAmountCalculated().getValue()),
                () -> assertEquals(bigD(436.07), bigD(paystubYTDCoBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(null, paystubYTDCoBorrower.getDifference()),
                () -> assertEquals(null, paystubYTDCoBorrower.getVariance()),
                () -> assertEquals("03/31/" + YEAR_TO_DATE, paystubYTDCoBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(3), bigD(paystubYTDCoBorrower.getMonths().getValue())));
    }

    @Test
    @Order(2)
    @Description("IA-2365 IA-1773 Check If Paystubs With Missing Dates Are Showing With Correct Dates")
    void uploadPaystubWithMissingDatesMostRecent() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment(),
                        "04/01/" + YEAR_TO_DATE,
                        null,
                        new PaystubData.PaystubIncomeRow(REGULAR, "100.50", "180.00", "296.00", "1,308.20"))
                        .setPayDate("04/30/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment(),
                        null,
                        null,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "296.00", "1,308.20"))
                        .setPayDate("04/30/" + YEAR_TO_DATE))
                .importDocumentList();

        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilderPaystubMissingDatesTest.getFolderId());

        RestPartIncomeAnnualSummary paystubYTDBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Borrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("UNKNOWN", paystubYTDBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(null, paystubYTDBorrower.getMonthlyAmountCalculated().getValue()),
                () -> assertEquals(bigD(327.05), bigD(paystubYTDBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(null, paystubYTDBorrower.getDifference()),
                () -> assertEquals(null, paystubYTDBorrower.getVariance()),
                () -> assertEquals("04/30/" + YEAR_TO_DATE, paystubYTDBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(4), bigD(paystubYTDBorrower.getMonths().getValue())));

        RestPartIncomeAnnualSummary paystubYTDCoBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("CoBorrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("UNKNOWN", paystubYTDCoBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDCoBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(null, paystubYTDCoBorrower.getMonthlyAmountCalculated().getValue()),
                () -> assertEquals(bigD(327.05), bigD(paystubYTDCoBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(null, paystubYTDCoBorrower.getDifference()),
                () -> assertEquals(null, paystubYTDCoBorrower.getVariance()),
                () -> assertEquals("04/30/" + YEAR_TO_DATE, paystubYTDCoBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(4), bigD(paystubYTDCoBorrower.getMonths().getValue())));
    }

    @Test
    @Order(3)
    @Tag("integration")
    @Description("IA-2365 IA-1773 Check If Paystubs With All Dates Are Showing With Correct Dates And Calculations")
    void uploadPaystubWithAllDatesAndCheckCalculations() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "100.50", "180.00", "296.00", "1,308.20"))
                        .setPayDate("05/30/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "296.00", "1,308.20"))
                        .setPayDate("05/30/" + YEAR_TO_DATE))
                .importDocumentList();

        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilderPaystubMissingDatesTest.getFolderId());

        RestPartIncomeAnnualSummary paystubYTDBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Borrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("MONTHLY", paystubYTDBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(17420.67), bigD(paystubYTDBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(17420.67), bigD(paystubYTDBorrower.getMonthlyAmountCalculated().getValue())),
                () -> assertEquals(bigD(261.64), bigD(paystubYTDBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(bigD(-17159.03), bigD(paystubYTDBorrower.getDifference())),
                () -> assertEquals(bigD(-98.5), bigD(paystubYTDBorrower.getVariance())),
                () -> assertEquals("05/31/" + YEAR_TO_DATE, paystubYTDBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(5), bigD(paystubYTDBorrower.getMonths().getValue())));

        RestPartIncomeAnnualSummary paystubYTDCoBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("CoBorrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("MONTHLY", paystubYTDCoBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDCoBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDCoBorrower.getMonthlyAmountCalculated().getValue())),
                () -> assertEquals(bigD(261.64), bigD(paystubYTDCoBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(bigD(-34.36), bigD(paystubYTDCoBorrower.getDifference())),
                () -> assertEquals(bigD(-11.61), bigD(paystubYTDCoBorrower.getVariance())),
                () -> assertEquals("05/31/" + YEAR_TO_DATE, paystubYTDCoBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(5), bigD(paystubYTDCoBorrower.getMonths().getValue())));
    }

    @Test
    @Order(4)
    @Tag("integration")
    @Description("IA-2365 IA-1773 Upload Most Current Paystubs With Missing Dates And Check Calculations")
    void uploadPaystubsWithMissingDatesMostCurrentAndCheckCalculations() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment(),
                        null,
                        null,
                        new PaystubData.PaystubIncomeRow(REGULAR, "100.50", "180.00", "296.00", "1,308.20"))
                        .setPayDate("06/30/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerSSN(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCollaboratorId(),
                        folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment(),
                        "06/00/" + YEAR_TO_DATE,
                        null,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "296.00", "1,308.20"))
                        .setPayDate("06/30/" + YEAR_TO_DATE))
                .importDocumentList();

        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilderPaystubMissingDatesTest.getFolderId());

        RestPartIncomeAnnualSummary paystubYTDBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("Borrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("UNKNOWN", paystubYTDBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(null, paystubYTDBorrower.getMonthlyAmountCalculated().getValue()),
                () -> assertEquals(bigD(218.03), bigD(paystubYTDBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(null, paystubYTDBorrower.getDifference()),
                () -> assertEquals(null, paystubYTDBorrower.getVariance()),
                () -> assertEquals("06/30/" + YEAR_TO_DATE, paystubYTDBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(6), bigD(paystubYTDBorrower.getMonths().getValue())));

        RestPartIncomeAnnualSummary paystubYTDCoBorrower = getResponse.getApplicant(folderBuilderPaystubMissingDatesTest.getCoBorrowerFullName()).getIncome(folderBuilderPaystubMissingDatesTest.getCoBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll("CoBorrower - Base Pay YTD Paystub correct values assertion",
                () -> assertEquals("UNKNOWN", paystubYTDCoBorrower.getFrequency().getValue().toString()),
                () -> assertEquals(bigD(296.00), bigD(paystubYTDCoBorrower.getDisplayGrossPay().getValue())),
                () -> assertEquals(null, paystubYTDCoBorrower.getMonthlyAmountCalculated().getValue()),
                () -> assertEquals(bigD(218.03), bigD(paystubYTDCoBorrower.getMonthlyAmountAvg().getValue())),
                () -> assertEquals(null, paystubYTDCoBorrower.getDifference()),
                () -> assertEquals(null, paystubYTDCoBorrower.getVariance()),
                () -> assertEquals("06/30/" + YEAR_TO_DATE, paystubYTDCoBorrower.getPayPeriodEndDate().getValue()),
                () -> assertEquals(bigD(6), bigD(paystubYTDCoBorrower.getMonths().getValue())));
    }
}

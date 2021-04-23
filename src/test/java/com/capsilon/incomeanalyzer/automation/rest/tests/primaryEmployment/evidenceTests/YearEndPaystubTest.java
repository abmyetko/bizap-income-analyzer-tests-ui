package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "YearEndPaystubTest")
class YearEndPaystubTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderYearEndPaystubTest = createFolderBuilder("IARPsYeaEnd");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilderYearEndPaystubTest
                .setBorrowerYearsOnThisJob("1")
                .setBorrowerMonthsOnThisJob("10")
                .setCoBorrowerYearsOnThisJob("1")
                .setCoBorrowerMonthsOnThisJob("15")
                .generateLoanDocument().restBuild();

        dataUpload = createUploadObject(folderBuilderYearEndPaystubTest, dvFolderClient);
    }

    @AfterEach
    void cleanDocuments() {
        RestChangeRequests.cleanJsonDocuments(folderBuilderYearEndPaystubTest.getFolderId());
    }

    @Test
    @Description("IA-2475 IA-2474 Check if Months Worked and Period End Date for year end Paystubs are calculated properly")
    void checkIfMonthsWorkedAndPeriodEndDateForYearEndPaystubAreCalculatedProperly() {
        dataUpload.importDocumentList()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderYearEndPaystubTest.getBorrowerFullName(),
                        folderBuilderYearEndPaystubTest.getBorrowerSSN(),
                        folderBuilderYearEndPaystubTest.getBorrowerCollaboratorId(),
                        folderBuilderYearEndPaystubTest.getBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("05/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderYearEndPaystubTest.getBorrowerFullName(),
                        folderBuilderYearEndPaystubTest.getBorrowerSSN(),
                        folderBuilderYearEndPaystubTest.getBorrowerCollaboratorId(),
                        folderBuilderYearEndPaystubTest.getBorrowerCurrentEmployment(),
                        "11/20/" + ONE_YEAR_PRIOR,
                        "12/01/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("12/01/" + ONE_YEAR_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderYearEndPaystubTest.getBorrowerFullName(),
                        folderBuilderYearEndPaystubTest.getBorrowerSSN(),
                        folderBuilderYearEndPaystubTest.getBorrowerCollaboratorId(),
                        folderBuilderYearEndPaystubTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/07/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("12/07/" + TWO_YEARS_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderYearEndPaystubTest.getCoBorrowerFullName(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerSSN(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerCollaboratorId(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerCurrentEmployment(),
                        "06/01/" + YEAR_TO_DATE,
                        "06/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("06/30/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderYearEndPaystubTest.getCoBorrowerFullName(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerSSN(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerCollaboratorId(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/15/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("12/15/" + ONE_YEAR_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderYearEndPaystubTest.getCoBorrowerFullName(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerSSN(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerCollaboratorId(),
                        folderBuilderYearEndPaystubTest.getCoBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/16/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"))
                        .setPayDate("12/16/" + TWO_YEARS_PRIOR))
                .importDocumentList();

        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilderYearEndPaystubTest.getFolderId());

        //Borrower
        RestPartIncomeAnnualSummary dataFromPaystubYtdBorrower = getResponse.getApplicant(folderBuilderYearEndPaystubTest.getBorrowerFullName())
                .getIncome(folderBuilderYearEndPaystubTest.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);
        RestPartIncomeAnnualSummary dataFromPaystubOneYearPriorBorrower = getResponse.getApplicant(folderBuilderYearEndPaystubTest.getBorrowerFullName())
                .getIncome(folderBuilderYearEndPaystubTest.getBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB);
        RestPartIncomeAnnualSummary dataFromPaystubTwoYearsPriorBorrower = getResponse.getApplicant(folderBuilderYearEndPaystubTest.getBorrowerFullName())
                .getIncome(folderBuilderYearEndPaystubTest.getBorrowerCurrentEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB);
        //Coborrower
        RestPartIncomeAnnualSummary dataFromPaystubYtdCoBorrower = getResponse.getApplicant(folderBuilderYearEndPaystubTest.getCoBorrowerFullName())
                .getIncome(folderBuilderYearEndPaystubTest.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);
        RestPartIncomeAnnualSummary dataFromPaystubOneYearPriorCoBorrower = getResponse.getApplicant(folderBuilderYearEndPaystubTest.getCoBorrowerFullName())
                .getIncome(folderBuilderYearEndPaystubTest.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB);
        RestPartIncomeAnnualSummary dataFromPaystubTwoYearsPriorCoBorrower = getResponse.getApplicant(folderBuilderYearEndPaystubTest.getCoBorrowerFullName())
                .getIncome(folderBuilderYearEndPaystubTest.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB);


        assertAll("Months Worked assertion",
                //Borrower
                () -> assertEquals(bigD(5), bigD(dataFromPaystubYtdBorrower.getMonths().getValue()), "Months Worked in " + YEAR_TO_DATE + " for Borrower should equal 5"),
                () -> assertEquals(bigD(12), bigD(dataFromPaystubOneYearPriorBorrower.getMonths().getValue()), "Months Worked in " + ONE_YEAR_PRIOR + " for Borrower should equal 12"),
                () -> assertEquals(bigD(8.10), bigD(dataFromPaystubTwoYearsPriorBorrower.getMonths().getValue()), "Months Worked in " + TWO_YEARS_PRIOR + " for Borrower should equal 8,10"),
                //CoBorrower
                () -> assertEquals(bigD(6), bigD(dataFromPaystubYtdCoBorrower.getMonths().getValue()), "Months Worked in " + YEAR_TO_DATE + " for CoBorrower should equal 6"),
                () -> assertEquals(bigD(12), bigD(dataFromPaystubOneYearPriorCoBorrower.getMonths().getValue()), "Months Worked in " + ONE_YEAR_PRIOR + " for CoBorrower should equal 12"),
                () -> assertEquals(bigD(12.00),
                        bigD(dataFromPaystubTwoYearsPriorCoBorrower.getMonths().getValue()), "Months Worked in " + TWO_YEARS_PRIOR + " for CoBorrower should equal 12,00")
        );

        assertAll("Period End Date Assertion",
                //Borrower
                () -> assertEquals("05/31/" + YEAR_TO_DATE,
                        dataFromPaystubYtdBorrower.getPayPeriodEndDate().getValue(), "Period End Date in" + YEAR_TO_DATE + "for Borrower should equal 05/31/" + YEAR_TO_DATE),
                () -> assertEquals("12/01/" + ONE_YEAR_PRIOR,
                        dataFromPaystubOneYearPriorBorrower.getPayPeriodEndDate().getValue(), "Period End Date in" + ONE_YEAR_PRIOR + "for Borrower should equal 12/01/" + ONE_YEAR_PRIOR),
                () -> assertEquals("12/07/" + TWO_YEARS_PRIOR,
                        dataFromPaystubTwoYearsPriorBorrower.getPayPeriodEndDate().getValue(), "Period End Date in" + TWO_YEARS_PRIOR + "for Borrower should equal 12/07/" + TWO_YEARS_PRIOR),
                //CoBorrower
                () -> assertEquals("06/30/" + YEAR_TO_DATE,
                        dataFromPaystubYtdCoBorrower.getPayPeriodEndDate().getValue(), "Period End Date in" + YEAR_TO_DATE + "for CoBorrower should equal 06/30/" + YEAR_TO_DATE),
                () -> assertEquals("12/15/" + ONE_YEAR_PRIOR,
                        dataFromPaystubOneYearPriorCoBorrower.getPayPeriodEndDate().getValue(), "Period End Date in" + YEAR_TO_DATE + "for CoBorrower should equal 12/15/" + ONE_YEAR_PRIOR),
                () -> assertEquals("12/16/" + TWO_YEARS_PRIOR,
                        dataFromPaystubTwoYearsPriorCoBorrower.getPayPeriodEndDate().getValue(), "Period End Date in" + YEAR_TO_DATE + "for CoBorrower should equal 12/16/" + TWO_YEARS_PRIOR)
        );
    }
}

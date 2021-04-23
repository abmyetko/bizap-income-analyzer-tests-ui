package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.WEEKLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "SameEmploymentNameTest")
public class SameEmploymentNameTest extends TestBaseRest {


    private final IAFolderBuilder folderBuilderEndptTest = createFolderBuilder("IARSameEmp");
    String currentJobStartDate = "10/01/" + ONE_YEAR_PRIOR;
    String signDate = "01/11/" + YEAR_TO_DATE;

    String previousJobStartDate = "10/01/" + THREE_YEARS_PRIOR;
    String previousJobEndDate = "08/31/" + ONE_YEAR_PRIOR;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderEndptTest.setSignDate(signDate);
        folderBuilderEndptTest.setBorrowerPreviousEmployment(folderBuilderEndptTest.getBorrowerCurrentEmployment());
        folderBuilderEndptTest.setBorrowerYearsOnThisJob("1").setBorrowerMonthsOnThisJob("1")
                .setBorrowerPreviousJobStartDate(previousJobStartDate)
                .setBorrowerPreviousJobEndDate(previousJobEndDate);

        folderBuilderEndptTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderEndptTest);

        RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId());
    }

    @BeforeEach
    void clearIncomeData() {
        dataUpload.removeDocumentsFromFolder();
    }

    @Test
    @Order(1)
    @Description("IA-2660 Check if Paystub with end date before new employment is associated in previous employment correctly")
    void checkIfPriorYearPaystubIsShownInPreviousEmployment() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderEndptTest.getBorrowerFullName(),
                folderBuilderEndptTest.getBorrowerSSN(),
                folderBuilderEndptTest.getBorrowerCollaboratorId(),
                folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                previousJobEndDate.replace("31", "01"),
                previousJobEndDate,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")));

        RestIncomePart currentIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), true).getBasePay();
        RestIncomePart previousIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), false).getBasePay();


        assertAll("Previous job paystub -> current job ytd change assertions",
//                () -> assertNotNull(currentIncomePart.getIncome(YEAR_TO_DATE),
//                        String.format("Current job is missing ytd %s year", YEAR_TO_DATE)),
                () -> assertNotNull(previousIncomePart.getIncome(ONE_YEAR_PRIOR)
                                .getAnnualSummaryDocument(PAYSTUB).getPayPeriodEndDate().getValue(),
                        String.format("Paystub was not added to previous job with same name as current job or %s date was not correct",
                                previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getPayPeriodEndDate().getValue()))
        );
    }

    @Test
    @Order(1)
    @Description("IA-2662 IA-2670 IA-2785 Check If Overlapping Paystub Has Its Income Proportionally Divided Between Both Jobs")
    void checkIfOverlappingPaystubHasItsIncomeProportionallyDivided() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "12/16/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .importDocumentList();

        RestIncomePart currentIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), true).getBasePay();
        RestIncomePart previousIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), false).getBasePay();

        assertAll("Overlapping paystub avg income assertions",
                () -> assertEquals(bigD(1000.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getValue()),
                        "Incorrect current job avg income value"),
                () -> assertEquals(bigD(1500.00),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getValue()),
                        "Current and previous jobs have different avg income value"),
                () -> assertEquals(bigD(12.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonths().getValue()),
                        "Incorrect current job months value"),
                () -> assertEquals(bigD(8.00),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonths().getValue()),
                        "Incorrect current job months value"),
                () -> assertEquals(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0),
                        previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0),
                        "Current and previous job do not have same documentId for paystub doc")
//                () -> assertEquals("Income has been calculated based on time worked for the year",
//                        currentIncomePart.getFootnotes().get(1),
//                        "Footnote for income divided is not visible [Borrower, current job, ONE_YEAR_PRIOR, PAYSTUB, monthlyAmountAvg"),
//                () -> assertEquals("Income has been calculated based on time worked for the year",
//                        previousIncomePart.getFootnotes().get(1),
//                        "Footnote for income divided is not visible [Borrower, previous job, ONE_YEAR_PRIOR, PAYSTUB, monthlyAmountAvg"),
//                Temporary disabled
//                () -> assertEquals("1",
//                        currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getFootnotesIdx().get(0),
//                        "Footnote index income divided is invalid [Borrower, current job, ONE_YEAR_PRIOR, PAYSTUB, monthlyAmountAvg"),
//                () -> assertEquals("1",
//                        previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getFootnotesIdx().get(0),
//                        "Footnote index for income divided is invalid [Borrower, previous job, ONE_YEAR_PRIOR, PAYSTUB, monthlyAmountAvg")
        );
    }

    @Test
    @Order(1)
    @Description("IA-2662 Check If Non Overlapping Paystub For Only Previous Job Is Not Divided Between Both Jobs")
    void checkIfOnlyPreviousJobPaystubIsNotDividedBetweenBothEmployments() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "12/16/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .importDocumentList();

        RestIncomePart previousIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), false).getBasePay();

        assertEquals(bigD(1000.00),
                bigD(previousIncomePart.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getValue()),
                "Paystub has its income divided");
    }

    @Test
    @Order(1)
    @Description("IA-2663 Check If Voe With Both Sections Has Them Correctly Extracted And Associated With Both Employments")
    void checkIfFullVoeHasBothIncomesCorrectlyExtracted() {
        dataUpload.importDocument(dataUpload.createCustomVoeFull(
                folderBuilderEndptTest.getBorrowerFullName(),
                folderBuilderEndptTest.getBorrowerSSN(),
                folderBuilderEndptTest.getBorrowerCollaboratorId(),
                folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                IncomeFrequency.MONTHLY,
                currentJobStartDate,
                signDate,
                "1000.00",
                "12000.00",
                signDate,
                WEEKLY,
                previousJobStartDate,
                previousJobEndDate,
                "1000.00",
                "0.00",
                "0.00",
                "0.00")
                .setPriorYearYear(ONE_YEAR_PRIOR.toString())
                .setPriorYearBasePay("3000.00")
                .setYtdTotal("8000.00")
                .setPriorYearTotal("6600.00"));

        RestIncomePart currentIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), true).getBasePay();
        RestIncomePart previousIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), false).getBasePay();

        assertAll("Overlapping Voe avg income assertions",
                () -> assertEquals(bigD(1000.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountAvg().getValue()),
                        "Incorrect current job avg income value"),
                () -> assertEquals(bigD(3000.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getDisplayGrossPay().getValue()),
                        "Incorrect current job avg gross pay value"),
                () -> assertEquals(bigD(3.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonths().getValue()),
                        "Incorrect current job months value"),

                () -> assertEquals(bigD(4333.33),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountCalculated().getValue()),
                        "Incorrect previous job monthly amount value"),
                () -> assertEquals(bigD(1000.00),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getDisplayGrossPay().getValue()),
                        "Incorrect previous job gross pay value"),
                () -> assertEquals(WEEKLY,
                        previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getFrequency().getValue(),
                        "Incorrect previous job frequency"),
                () -> assertEquals(bigD(8.00),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonths().getValue()),
                        "Incorrect previous job months value"),
                () -> assertEquals(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getDocIds().get(0),
                        previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getDocIds().get(0),
                        "Current and previous job do not have same documentId for Voe doc")
        );
    }

    @Test
    @Order(1)
    @Description("IA-2659 IA-2750 IA-2670 IA-2785 Check If Overlapping W2 Document Has Its Income Proportionally Divided Between Both Jobs")
    void checkIfOverlappingW2DocumentHasItsIncomeProportionallyDivided() {
        dataUpload.importDocument(dataUpload.createCustomW2(
                folderBuilderEndptTest.getBorrowerFullName(),
                folderBuilderEndptTest.getBorrowerSSN(),
                folderBuilderEndptTest.getBorrowerCollaboratorId(),
                folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                "12000.00",
                ONE_YEAR_PRIOR.toString()));

        RestIncomePart currentIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), true).getBasePay();
        RestIncomePart previousIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), false).getBasePay();

        assertAll("Overlapping W-2 avg income assertions",
                () -> assertEquals(bigD(12000.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getGrossAmount().getValue()),
                        "Incorrect current job gross amount value"),
                () -> assertEquals(bigD(12000.00),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getGrossAmount().getValue()),
                        "Incorrect current job gross amount value"),
                () -> assertEquals(bigD(1000.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect current job monthly amount value"),
                () -> assertEquals(bigD(1500.00),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getMonthlyAmountAvg().getValue()),
                        "Current and previous jobs have different monthly amount value"),
                () -> assertEquals(bigD(12.00),
                        bigD(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getMonths().getValue()),
                        "Current job has incorrect number of months"),
                () -> assertEquals(bigD(8.00),
                        bigD(previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getMonths().getValue()),
                        "Previous job has incorrect number of months"),
                () -> assertEquals(currentIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getDocIds().get(0),
                        previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getDocIds().get(0),
                        "Current and previous job do not have same documentId for W-2 doc")
//                () -> assertEquals("Income has been calculated based on time worked for the year",
//                        previousIncomePart.getFootnotes().get(1),
//                        "Footnote for income divided is not visible [Borrower, previous job,  ONE_YEAR_PRIOR, W2, monthlyAmountAvg"),
//                () -> assertEquals("Income has been calculated based on time worked for the year",
//                        previousIncomePart.getFootnotes().get(1),
//                        "Footnote for income divided is not visible [Borrower, previous job, ONE_YEAR_PRIOR, W2, displayGrossPay"),
//                Temporarily Disabled
//                () -> assertEquals("1",
//                        previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getFootnotesIdx().get(0),
//                        "Footnote index for income divided is not valid [Borrower, previous job,  ONE_YEAR_PRIOR, W2, monthlyAmountAvg"),
//                () -> assertEquals("1",
//                        previousIncomePart.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getFootnotesIdx().get(0),
//                        "Footnote index for income divided is not valid [Borrower, previous job, ONE_YEAR_PRIOR, W2, displayGrossPay")
        );
    }

    @Test
    @Order(1)
    @Description("IA-2659 Check If W-2 document only in previous job year does not have its income divided")
    void checkIfPreviousW2DocumentIsNotDividedBetweenBothEmployments() {
        dataUpload.importDocument(dataUpload.createCustomW2(
                folderBuilderEndptTest.getBorrowerFullName(),
                folderBuilderEndptTest.getBorrowerSSN(),
                folderBuilderEndptTest.getBorrowerCollaboratorId(),
                folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                "12000.00",
                TWO_YEARS_PRIOR.toString()));

        RestIncomePart previousIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), false).getBasePay();

        assertEquals(bigD(12000.00),
                bigD(previousIncomePart.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2).getGrossAmount().getValue()),
                "Only previous job W-2 had its income divided between jobs");
    }

    @Test
    @SuppressWarnings("rawtypes")
    @Description("IA-2755 Check If Overlapping Ytd Paystub Has Its Income Proportionally Divided")
    void checkIfOverlappingYtdPaystubHasItsIncomeProportionallyDivided() {
        folderBuilderEndptTest.setSignDate("11/01/" + YEAR_TO_DATE);
        folderBuilderEndptTest.setBorrowerPreviousEmployment(folderBuilderEndptTest.getBorrowerCurrentEmployment());
        folderBuilderEndptTest.setBorrowerYearsOnThisJob("0").setBorrowerMonthsOnThisJob("1")
                .setBorrowerPreviousJobStartDate("10/01/" + THREE_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("08/31/" + YEAR_TO_DATE);

        folderBuilderEndptTest.generateLoanDocument().uploadNewLoanDocument();
        DataUploadObject dataUpload = createUploadObject(folderBuilderEndptTest);

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderEndptTest.getBorrowerFullName(),
                folderBuilderEndptTest.getBorrowerSSN(),
                folderBuilderEndptTest.getBorrowerCollaboratorId(),
                folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                "11/01/" + YEAR_TO_DATE,
                "11/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")));

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {

            RestIncomePart currentIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                    .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerCurrentEmployment(), true).getBasePay();
            RestIncomePart previousIncomePart = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                    .getSameNameEmployer("W2", folderBuilderEndptTest.getBorrowerPreviousEmployment(), false).getBasePay();

            assertAll("Overlapping W-2 avg income assertions",
                    () -> assertEquals(bigD(1440.00),
                            bigD(currentIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getGrossAmount().getValue()),
                            "Incorrect current job gross amount value"),
                    () -> assertEquals(bigD(600.00),
                            bigD(currentIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getDisplayGrossPay().getValue()),
                            "Incorrect current job display gross pay value"),
                    () -> assertEquals(bigD(600.00),
                            bigD(previousIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getDisplayGrossPay().getValue()),
                            "Incorrect previous job display gross pay value"),
                    () -> assertEquals(bigD(720.00),
                            bigD(currentIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect current job monthly amount value"),
                    () -> assertEquals(bigD(currentIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getValue()),
                            bigD(previousIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Current and previous jobs have different monthly amount value"),
                    () -> assertEquals(bigD(2.00),
                            bigD(currentIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonths().getValue()),
                            "Current job has incorrect number of months"),
                    () -> assertEquals(bigD(8.00),
                            bigD(previousIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonths().getValue()),
                            "Previous job has incorrect number of months"),
                    () -> assertEquals(currentIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0),
                            previousIncomePart.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0),
                            "Current and previous job do not have same documentId for W-2 doc")
            );
        });
    }
}


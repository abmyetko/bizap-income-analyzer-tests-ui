package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectIncome;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.setMonthsPaid;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.TEACHERS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = TEACHERS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "TeachersTest")
public class TeachersTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilder = createFolderBuilder("IARTchrs");
    private RestGetResponse getResponse;

    @BeforeAll
    public void generateDocs() {
        folderBuilder.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        "1200",
                        "65",
                        "0",
                        "0"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
    }

    @Test
    @Tag("integration")
    @Description("IA-2277 check if all documents are defaulted to 12.00 Months Paid value")
    @Order(0)
    void checkIfMonthsPaidFieldShouldSyncForAllAvailableDocumentsInBasePay() {
        assertAll("Base Pay default months paid value check",
                () -> assertEquals(
                        bigD(12.00), bigD(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                                .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthsPaid()), "EVOE had incorrect months paid value"),
                () -> assertEquals(
                        bigD(12.00), bigD(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                                .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthsPaid()), "Paystub had incorrect months paid value")
        );
    }


    @Test
    @Description("IA-2279 No change to touchless indicator from Months Paid selected ")
    @Order(1)
    void stayTouchlessAfterChangeMonthsPaid() {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).
                getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();
        setMonthsPaid(borrowerBP.getIncome(ONE_YEAR_PRIOR).getId(), bigD(11));
        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestGetResponse finalGetResponse = getResponse;
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertAll("Asserting Touchless state",
                        () -> assertTrue(
                                finalGetResponse.getTouchless(), "Touchless state for folder should be true"),
                        () -> assertTrue(
                                finalGetResponse.getApplicant(folderBuilder.getBorrowerFullName()).getTouchless(), "Touchless state for first applicant should be true"),
                        () -> assertTrue(
                                finalGetResponse.getApplicant(folderBuilder.getCoBorrowerFullName()).getTouchless(), "Touchless state for second applicant should be true")
                ));

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR).getId(), true);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        assertFalse(getResponse.getTouchless(), "Touchless state for folder should be false");

        setMonthsPaid(borrowerBP.getIncome(ONE_YEAR_PRIOR).getId(), bigD(10));

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        assertFalse(getResponse.getTouchless(), "Touchless state for folder should stay false");
    }

    @Test
    @Order(2)
    @Description("IA-2318 Months Paid field should sync for all available documents")
    void checkIfMonthsPaidSyncForAllDocuments() {
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            for (BigDecimal monthsPaidValue = bigD(0.75); monthsPaidValue.compareTo(BigDecimal.valueOf(12)) <= 0; monthsPaidValue = monthsPaidValue.add(bigD(0.75))) {

                RestGetResponse responseBeforeChange = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
                RestIncomePart borrowerBP = responseBeforeChange.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

                setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(monthsPaidValue));

                getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

                BigDecimal firstValue = bigD(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                        .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthsPaid());
                BigDecimal secondValue = bigD(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                        .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthsPaid());

                assertEquals(firstValue, secondValue, "Months Paid changes did not sync between documents");
            }
        });
    }

    @Test
    @Order(3)
    @Description("IA-2278 Months Paid Calculation Paystub Monthly")
    void monthsPaidPaystubMonthly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                "12/01/" + YEAR_TO_DATE,
                "12/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(300),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Monthly paystub");
        });
    }

    @Test
    @Order(4)
    @Description("IA-2278 Months Paid Calculation Paystub Semi-Monthly")
    void monthsPaidPaystubSemiMonthly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                "12/16/" + YEAR_TO_DATE,
                "12/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(600),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Semi-Monthly paystub");

        });
    }

    @Test
    @Order(4)
    @Description("IA-2278 Months Paid Calculation Paystub Bi-Weekly")
    void monthsPaidPaystubBiWeekly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                "12/01/" + YEAR_TO_DATE,
                "12/14/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(650),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Bi-Weekly paystub");
        });
    }

    @Test
    @Order(5)
    @Description("IA-2278 Months Paid Calculation Paystub Weekly")
    void monthsPaidPaystubWeekly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                "12/25/" + YEAR_TO_DATE,
                "12/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(1300),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Weekly paystub");
        });
    }

    @Test
    @Order(6)
    @Description("IA-2278 Months Paid Calculation Paystub Hourly")
    void monthsPaidPaystubHourly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                "12/01/" + YEAR_TO_DATE,
                "12/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "10.24", "40", "600", "7200")));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(300),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Hourly paystub");
        });
    }

    @Test
    @Order(7)
    @Description("IA-2278 Months Paid Calculation Evoe Monthly ")
    void monthsPaidEvoeMonthly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.MONTHLY,
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                "1200",
                "1200",
                "01/31/" + YEAR_TO_DATE)
                .setRate("1200"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(600),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Monthly EVOE");
        });
    }

    @Test
    @Order(8)
    @Description("IA-2278 Months Paid Calculation Evoe Semi-Monthly")
    void monthsPaidEvoeSemiMonthly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.SEMI_MONTHLY,
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                "1200",
                "1200",
                "01/31/" + YEAR_TO_DATE)
                .setRate("1200"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(1200),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Semi-Monthly EVOE");
        });
    }

    @Test
    @Order(9)
    @Description("IA-2278 Months Paid Calculation Evoe Bi-Weekly")
    void monthsPaidEvoeBiWeekly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.BI_WEEKLY,
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                "1200",
                "1200",
                "01/31/" + YEAR_TO_DATE)
                .setRate("1200"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(1300),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Bi-Weekly EVOE");
        });
    }

    @Test
    @Order(10)
    @Description("IA-2278 Months Paid Calculation Evoe Weekly")
    void monthsPaidEvoeWeekly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.WEEKLY,
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                "1200",
                "1200",
                "01/31/" + YEAR_TO_DATE)
                .setRate("1200"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(2600),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Weekly EVOE");
        });
    }

    @Test
    @Order(11)
    @Description("IA-2278 Months Paid Calculation Evoe Hourly")
    void monthsPaidEvoeHourly() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.WEEKLY,
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                "1200",
                "1200",
                "01/31/" + YEAR_TO_DATE)
                .setAvgHoursPerPeriod("40")
                .setRate("1200"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomePart borrowerBP = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay();

            setMonthsPaid(borrowerBP.getIncome(YEAR_TO_DATE).getId(), bigD(6));

            assertEquals(bigD(2600),
                    bigD(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountCalculated().getValue()), "Monthly Amount was not calculated correctly when months paid was changed for Hourly EVOE");

        });
    }

    @Test
    @Order(12)
    @Description("IA-2296 Check if Months Paid are not calculated W2 YTD")
    void casesWhenMonthPaidAreNotCalculatedW2Ytd() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomW2(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                "12000.00",
                YEAR_TO_DATE.toString()));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertNull(
                    getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.W2).getMonthsPaid(), "Check if Months Paid are not calculated W2 YTD");
        });
    }

    @Test
    @Order(13)
    @Description("IA-2296 Check if Months Paid are not calculated EVOE Annualy YTD")
    void casesWhenMonthPaidAreNotCalculatedEvoeAnnualYtd() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.ANNUALLY,
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                "1200",
                "1200",
                "01/31/" + YEAR_TO_DATE));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertNull(
                    getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthsPaid(), "Check if Months Paid are not calculated EVOE Annualy YTD");
        });
    }

    @Test
    @Order(14)
    @Description("IA-2296 Check if Months Paid are not calculated VOE Ytd")
    void casesWhenMonthPaidAreNotCalculateVoeAnnualYtd() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomVoeCurrent(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.ANNUALLY,
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                "1200",
                "1200",
                "01/31/" + YEAR_TO_DATE));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertNull(
                    getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthsPaid(), "Check if Months Paid are not calculated VOE Ytd");
        });
    }

    @Test
    @Order(15)
    @Description("IA-2296 Check if Months Paid are not calculated Paystub Zero Income")
    void casesWhenMonthPaidAreNotCalculatedPaystubWithZeroIncome() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerCurrentEmployment(),
                "12/01/" + YEAR_TO_DATE,
                "12/31/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "0", "0")));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertNull(
                    getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay()
                            .getIncome(YEAR_TO_DATE).getAllAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthsPaid(), "Check if Months Paid are not calculated Paystub Zero Income");
        });
    }
}

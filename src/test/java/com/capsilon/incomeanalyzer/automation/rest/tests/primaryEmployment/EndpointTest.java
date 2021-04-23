package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.*;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.STATUS_BAD_REQUEST;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.OVERTIME_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "EndpointTest")
class EndpointTest extends TestBaseRest {


    private final IAFolderBuilder folderBuilderEndptTest = createFolderBuilder("IARestEndpt");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderEndptTest.generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(folderBuilderEndptTest.getBorrowerSSN(), "1.10")
                .setPrimaryJobBasePay(folderBuilderEndptTest.getCoBorrowerSSN(), "1.10")
                .setPrimaryJobOvertime(folderBuilderEndptTest.getCoBorrowerSSN(), "1.10")
                .restBuild();

        dataUpload = createUploadObject(folderBuilderEndptTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/07/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
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
                        folderBuilderEndptTest.getCoBorrowerFullName(),
                        folderBuilderEndptTest.getCoBorrowerSSN(),
                        folderBuilderEndptTest.getCoBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/07/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "15.00", "40.00", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderEndptTest.getCoBorrowerFullName(),
                        folderBuilderEndptTest.getCoBorrowerSSN(),
                        folderBuilderEndptTest.getCoBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2595", "173.33", "2595", "2595")))
                .addDocument((dataUpload.createCustomVoeCurrent(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        "Abacus",
                        IncomeFrequency.HOURLY,
                        "11/01/2010",
                        "03/31/" + YEAR_TO_DATE,
                        "15.00",
                        "3000",
                        "03/31/" + YEAR_TO_DATE))
                        .setAvgHoursPerWeek("40.00"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderEndptTest.getBorrowerFullName(),
                        folderBuilderEndptTest.getBorrowerSSN(),
                        folderBuilderEndptTest.getBorrowerCollaboratorId(),
                        folderBuilderEndptTest.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "07/01/" + THREE_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "2500",
                        "0",
                        "0",
                        "0"))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId());
    }

    @AfterEach
    void enableFolderIfDisabled() {
        if ("DISABLED".equals(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getStatus()))
            RestNotIARequests.setCduStatus(folderBuilderEndptTest.getFolderId(), CduStatus.IN_PROGRESS, "Reason");
    }

    @Test
    @Tag("integration")
    @Tag("health")
    void incomeCategoryShouldBePossibleToSelectDeselect() {
        final RestGetResponseApplicant[] firstApplicant = {getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeCategory(firstApplicant[0].getId(), IncomePartCategory.W2, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                            .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getQualified(),
                    "W2 income category was not selected by default or could not be selected on folder: " + folderBuilderEndptTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeCategory(firstApplicant[0].getId(), IncomePartCategory.W2, false);
            assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                            .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getQualified(),
                    "W2 income category could not be deselected on folder: " + folderBuilderEndptTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeCategory(firstApplicant[0].getId(), IncomePartCategory.W2, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                            .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getQualified(),
                    "W2 income category could not be selected on folder: " + folderBuilderEndptTest.getFolderId());
        });
    }

    @Test
    @Tag("integration")
    @Tag("health")
    @Description("IA-2568 Income Type Should Be Possible To Select And Deselect")
    void incomeTypeShouldBePossibleToSelectDeselect() {
        final RestGetResponseApplicant[] firstApplicant = {getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(firstApplicant[0].getId(),
                    getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                    IncomePartType.BASE_PAY, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                            .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getSelected(),
                    "Income type was not selected by default or could not be selected on folder: " + folderBuilderEndptTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(firstApplicant[0].getId(),
                    getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                    IncomePartType.BASE_PAY, false);
            assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                            .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getSelected(),
                    "Income type could not be deselected on folder: " + folderBuilderEndptTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(firstApplicant[0].getId(),
                    getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                    IncomePartType.BASE_PAY, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                            .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getSelected(),
                    "Income type could not be selected on folder: " + folderBuilderEndptTest.getFolderId());
        });
    }

    @Test
    @Description("IA-2340 Changing Months Paid Should Work Only For Correct Values")
    void changingMonthsPaidShouldWorkOnlyForCorrectValues() {
        RestIncomePart firstApplicantBasePayIncome = getResponse
                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                .getBasePay();

        Retry.tryRun(TIMEOUT_ONE_MINUTE, TIMEOUT_TWO_MINUTES, () -> {
            for (BigDecimal expectedValue = bigD(0.25); expectedValue.compareTo(BigDecimal.valueOf(12)) < 0; expectedValue = expectedValue.add(bigD(0.25))) {

                setMonthsPaid(firstApplicantBasePayIncome.getIncome(YEAR_TO_DATE).getId(), expectedValue);

                BigDecimal actualValue = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                        .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                        .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                        .getBasePay().getIncome(YEAR_TO_DATE).getMonthsPaid();

                assertEquals(expectedValue, bigD(actualValue), String.format("monthsPaid value is not correct, expected: %s actual: %s", expectedValue, actualValue));
            }
        });
        assertEquals(STATUS_BAD_REQUEST, setMonthsPaid(firstApplicantBasePayIncome.getIncome(YEAR_TO_DATE).getId(), bigD(0)).getStatusCode(), "Months paid was set to zero");
    }

    @Test
    @Description("IA-1328 Deselecting Previous Year Should Deselect Two Years Prior")
    void deselectingPreviousYearShouldDeselectTwoYearsPrior() {
        RestIncomePart firstApplicantBasePayIncome = getResponse
                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                .getBasePay();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment()).getId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
            assertAll("Asserting selection of incomes for previous years on folder: " + folderBuilderEndptTest.getFolderId(),
                    () -> assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getSelected(),
                            "Prior year could not be selected"),
                    () -> assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(TWO_YEARS_PRIOR).getSelected(),
                            "Two years prior could not be selected")
            );
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
            assertAll("Asserting income selection on deselecting previous year on folder: " + folderBuilderEndptTest.getFolderId(),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getSelected(),
                            "Prior year has not been deselected"),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(TWO_YEARS_PRIOR).getSelected(),
                            "Two years prior was not deselected automatically")
            );
        });
    }

    @Test
    @Description("IA-1279 Deselecting Previous Year Should Default Historical Average To Ytd")
    void deselectingPreviousYearShouldDefaultHistoricalAverageToYtd() {
        RestIncomePart firstApplicantBasePayIncome = getResponse
                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                .getBasePay();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment()).getId());
        Long firstApplicantId = getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getId();
        String firstApplicantGroupId = getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
            selectIncomeAvg(firstApplicantId, firstApplicantGroupId, IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                            .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(),
                    "Historical Average for two years prior could not be selected on folder: " + folderBuilderEndptTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
            assertAll("Assertion of defaults on deselecting previous year income on folder: " + folderBuilderEndptTest.getFolderId(),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getSelected(),
                            "Prior year is still selected"),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(),
                            "Historical Average for two years prior is still selected"),
                    () -> assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected(),
                            "Historical Average did not default to YTD")
            );
        });
    }

    @Test
    @Tag("health")
    @Description("IA-1451 Selecting Year Before Prior Should Show Values In Historical Averages And Default To Ytd")
    void selectingYearBeforePriorShouldShowValuesInHaAndDefaultToYtd() {
        RestIncomePart firstApplicantBasePayIncome = getResponse
                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                .getBasePay();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                .getIncomeCategoryW2()
                                .getPrimaryIncomeGroup()
                                .getIncomeTypes().get("BASE_PAY")
                                .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(),
                        "Historical Average income for two years prior is selected on folder: " + folderBuilderEndptTest.getFolderId()));

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
            assertAll("Asserting Historical Average income defaults on selecting two years prior on folder: " + folderBuilderEndptTest.getFolderId(),
                    () -> assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected(),
                            "Historical Average for YTD was not selected by default"),
                    () -> assertNotNull(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getAvgMonthlyIncome(),
                            "Monthly income for two years prior are null"),
                    () -> assertNotNull(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getGross(),
                            "Gross amount for two years prior are null"),
                    () -> assertNotNull(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                    .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getMonths(),
                            "Months worked for two years prior are null")

            );
        });
    }

    @Test
    @Tag("integration")
    @Description("IA-2597 Income Historical Average Values Should Be Possible To Select")
    void incomeHistoricalAveragesShouldBePossibleToSelect() {
        final RestGetResponseApplicant[] firstApplicant = new RestGetResponseApplicant[1];
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName());

        Executable checkYtdAvgFalse = () -> assertFalse(firstApplicant[0]
                        .getIncomeCategoryW2()
                        .getPrimaryIncomeGroup()
                        .getIncomeTypes().get("BASE_PAY")
                        .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG)
                        .getSelected(),
                "YTD Avg is selected");
        Executable checkYtdAvgPlusPrevYrFalse = () -> assertFalse(firstApplicant[0]
                        .getIncomeCategoryW2()
                        .getPrimaryIncomeGroup()
                        .getIncomeTypes().get("BASE_PAY")
                        .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR)
                        .getSelected(),
                "YTD + 2018 Avg is selected");
        Executable checkYtdAvgPlusPrevTwoYrsFalse = () -> assertFalse(firstApplicant[0]
                        .getIncomeCategoryW2()
                        .getPrimaryIncomeGroup()
                        .getIncomeTypes().get("BASE_PAY")
                        .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS)
                        .getSelected(),
                "YTD + 2018/17 Avg is selected");

        selectIncomeAvg(firstApplicant[0].getId(),
                getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR, true);
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName());
        assertAll("ytd avg + prev year selection on folder: " + folderBuilderEndptTest.getFolderId(),
                () -> assertTrue(firstApplicant[0]
                                .getIncomeCategoryW2()
                                .getPrimaryIncomeGroup()
                                .getIncomeTypes().get("BASE_PAY")
                                .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR)
                                .getSelected(),
                        "YTD + 2018 Avg is not selected"),
                checkYtdAvgPlusPrevTwoYrsFalse,
                checkYtdAvgFalse
        );

        selectIncomeAvg(firstApplicant[0].getId(),
                getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS, true);
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName());
        assertAll("ytd avg + two years selection on folder: " + folderBuilderEndptTest.getFolderId(),
                () -> assertTrue(firstApplicant[0]
                                .getIncomeCategoryW2()
                                .getPrimaryIncomeGroup()
                                .getIncomeTypes().get("BASE_PAY")
                                .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS)
                                .getSelected(),
                        "YTD + 2018/17 Avg is not selected"),
                checkYtdAvgPlusPrevYrFalse,
                checkYtdAvgFalse
        );

        selectIncomeAvg(firstApplicant[0].getId(),
                getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomeAvg.BASEPAY_YTD_AVG, true);
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName());
        assertAll("ytd avg selection on folder: " + folderBuilderEndptTest.getFolderId(),
                () -> assertTrue(firstApplicant[0]
                                .getIncomeCategoryW2()
                                .getPrimaryIncomeGroup()
                                .getIncomeTypes().get("BASE_PAY")
                                .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG)
                                .getSelected(),
                        "YTD Avg is not selected"),
                checkYtdAvgPlusPrevYrFalse,
                checkYtdAvgPlusPrevTwoYrsFalse
        );
    }

    @Test
    @Tag("integration")
    @Description("IA-2598 Income Group Should Be Possible To Select")
    void incomeGroupsShouldBePossibleToSelect() {
        RestGetResponseApplicant firstApplicant =
                RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                .getIncomeCategoryW2()
                                .getPrimaryIncomeGroup()
                                .getSelected(),
                        "Primary income Group was not selected on folder: " + folderBuilderEndptTest.getFolderId()));

        selectIncomeGroup(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), false);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                .getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                                .getIncomeCategoryW2()
                                .getPrimaryIncomeGroup()
                                .getSelected(),
                        "Primary income Group could not be deselected on folder: " + folderBuilderEndptTest.getFolderId()));
    }

    @Test
    @Tag("integration")
    void onlyOneMonthlyIncomeShouldBeSelectedForOneIncomePart() {
        RestPartIncome income = getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                .getBasePay()
                .getIncome(YEAR_TO_DATE);

        int selectedCount = 0;
        for (RestPartIncomeAnnualSummary document : income.getAnnualSummary()) {
            if (document.getMonthlyAmountCalculated().getSelected())
                selectedCount++;
            if (document.getMonthlyAmountAvg().getSelected())
                selectedCount++;
        }
        assertEquals(1, selectedCount, "No income or more than one is selected by default on folder: " + folderBuilderEndptTest.getFolderId());
        RestPartIncomeAnnualSummary incomeToSelect = income.getAnnualSummaryNotSelectedDocuments().get(0);
        RestChangeRequests.selectJobAnnualSummary(income.getId(), incomeToSelect.getDocIds().get(0), AnnualSummaryIncomeType.CALCULATED);

        getResponse = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId());

        income = getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment())
                .getBasePay()
                .getIncome(YEAR_TO_DATE);

        selectedCount = 0;
        for (RestPartIncomeAnnualSummary document : income.getAnnualSummary()) {
            if (document.getMonthlyAmountCalculated().getSelected())
                selectedCount++;
            if (document.getMonthlyAmountAvg().getSelected())
                selectedCount++;
        }
        assertEquals(1, selectedCount, "No income or more than one is selected" + folderBuilderEndptTest.getFolderId());
    }

    @Test
    void applicantCheckShouldSelectOrDeselectHim() {
        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(0).getId(), false);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertFalse(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                .getApplicants().get(0)
                                .isQualified(),
                        "Applicant should become unqualified on folder: " + folderBuilderEndptTest.getFolderId())
        );

        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(0).getId(), true);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertTrue(RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId())
                                .getApplicants().get(0)
                                .isQualified(),
                        "Applicant should become qualified on folder: " + folderBuilderEndptTest.getFolderId())
        );
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE, negateState = true)
    @Description("IA-2253 Overtime Section Should Be Deselected If Feature Is Disabled")
    void overtimeSectionShouldBeDeselectedIfFeatureIsDisabled() {
        assertAll("Check overtime feature toggle state",
                () -> assertFalse(getResponse.getFeatureToggles().get(OVERTIME_TOGGLE.value)),
                () -> assertFalse(getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                        .getIncomeCategoryW2()
                        .getPrimaryIncomeGroup()
                        .getIncomeTypeOvertime().getSelected())
        );
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Description("IA-2253 Overtime Section Should Be Selected If Feature Is Enabled")
    void overtimeSectionShouldBeSelectedIfFeatureIsEnabled() {
        assertAll("Check overtime feature toggle state",
                () -> assertTrue(getResponse.getFeatureToggles().get(OVERTIME_TOGGLE.value)),
                () -> assertTrue(getResponse.getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                        .getIncomeCategoryW2()
                        .getPrimaryIncomeGroup()
                        .getIncomeTypeOvertime().getSelected())
        );
    }

    @Disabled("Automatic generation is sometimes not running on some sites")
    @Test
    void lockingAndDisablingFolderShouldGenerateAutomatedIncomeWorksheet() {
        Integer[] initialWorksheetCount = {RestNotIARequests.getAllIncomeWorksheets(folderBuilderEndptTest.getFolderId()).size()};
        RestNotIARequests.setCduStatus(folderBuilderEndptTest.getFolderId(), CduStatus.REMOVED, "Reason");
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () ->
                assertTrue(RestNotIARequests.getAllIncomeWorksheets(folderBuilderEndptTest.getFolderId()).size() >= initialWorksheetCount[0] + 1,
                        "At least 1 worksheet has not been generated in time on disabling folder")
        );
    }

    @Test
    @Description("IA-2970 check if endpoint works for changing pay frequency")
    void checkIfEndpointWorksForChangingPayFrequency(){
        RestGetResponse localGetResponse = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId());
        String documentId = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                .getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0).toString();
        updatePayFrequency(localGetResponse.getId().toString(), documentId,IncomeFrequency.MONTHLY);
        IncomeFrequency incomeFrequency = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getApplicant(folderBuilderEndptTest.getBorrowerFullName())
                .getIncome(folderBuilderEndptTest.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE)
                .getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue();
        assertEquals(IncomeFrequency.MONTHLY,incomeFrequency);
    }

    @Test
    @Tag("health")
    @Description("IA-2409 Worksheet Id Should Be Added To IA Response Correctly")
    void worksheetIdShouldBeAddedToIAResponseCorrectly() {
        RestGetResponseWorksheet initialWorksheet = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getWorksheet();
        RestChangeRequests.generateIncomeWorksheetPdf(getResponse.getId().toString());
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            RestGetResponseWorksheet afterGenerationWorksheet = RestGetLoanData.getApplicationData(folderBuilderEndptTest.getFolderId()).getWorksheet();
            assertFalse(afterGenerationWorksheet.getUnderGeneration());
            assertNotNull(afterGenerationWorksheet.getDocumentId());
            assertNotEquals(initialWorksheet.getDocumentId(), afterGenerationWorksheet.getDocumentId());
        });
    }
}

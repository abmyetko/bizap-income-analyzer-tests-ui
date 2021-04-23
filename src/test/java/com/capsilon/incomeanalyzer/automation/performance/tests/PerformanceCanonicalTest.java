package com.capsilon.incomeanalyzer.automation.performance.tests;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.*;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.CommonMethods;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfSystemProperty(named = "bizapps.ia.performance", matches = "true")
@Execution(CONCURRENT)
@ResourceLock(value = "PerformanceCanonicalTest")
public class PerformanceCanonicalTest extends TestBaseRest {

    private IAFolderBuilder folderBuilderPerfCanonicalTest = createFolderBuilder("IAPerfTest");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderPerfCanonicalTest.setSignDate("01/01/" + YEAR_TO_DATE);
        folderBuilderPerfCanonicalTest.generateLoanDocument().restBuild();

        RestCanonicalDocumentMethods method = new RestCanonicalDocumentMethods();

        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, true, SummaryDocumentType.PAYSTUB, YEAR_TO_DATE);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, false, SummaryDocumentType.PAYSTUB, YEAR_TO_DATE);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, true, SummaryDocumentType.PAYSTUB, YEAR_TO_DATE);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, false, SummaryDocumentType.PAYSTUB, YEAR_TO_DATE);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, true, SummaryDocumentType.VOE, YEAR_TO_DATE);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, false, SummaryDocumentType.VOE_PREVIOUS, YEAR_TO_DATE);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, true, SummaryDocumentType.VOE, YEAR_TO_DATE);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, false, SummaryDocumentType.VOE_PREVIOUS, YEAR_TO_DATE);

        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, true, SummaryDocumentType.PAYSTUB, ONE_YEAR_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, false, SummaryDocumentType.PAYSTUB, ONE_YEAR_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, true, SummaryDocumentType.PAYSTUB, ONE_YEAR_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, false, SummaryDocumentType.PAYSTUB, ONE_YEAR_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, true, SummaryDocumentType.W2, ONE_YEAR_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, false, SummaryDocumentType.W2, ONE_YEAR_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, true, SummaryDocumentType.W2, ONE_YEAR_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, false, SummaryDocumentType.W2, ONE_YEAR_PRIOR);

        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, true, SummaryDocumentType.PAYSTUB, TWO_YEARS_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, false, SummaryDocumentType.PAYSTUB, TWO_YEARS_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, true, SummaryDocumentType.PAYSTUB, TWO_YEARS_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, false, SummaryDocumentType.PAYSTUB, TWO_YEARS_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, true, SummaryDocumentType.W2, TWO_YEARS_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, true, false, SummaryDocumentType.W2, TWO_YEARS_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, true, SummaryDocumentType.W2, TWO_YEARS_PRIOR);
        method.uploadDefaultCanonicalDocument(folderBuilderPerfCanonicalTest, false, false, SummaryDocumentType.W2, TWO_YEARS_PRIOR);


        Retry.whileTrue(CommonMethods.TIMEOUT_FORTY_SECONDS, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());
            return getResponse.getNotFNMDocumentCount() < 22;
        }, "Canonical documents have not been loaded correctly");
    }

    @Test
    @Description("IA-2687 Income Category Should Be Possible To Select Deselect")
    void incomeCategoryShouldBePossibleToSelectDeselect() {
        final RestGetResponseApplicant[] firstApplicant = {getResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeCategory(firstApplicant[0].getId(), IncomePartCategory.W2, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getQualified(),
                    "W2 income category was not selected by default or could not be selected on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeCategory(firstApplicant[0].getId(), IncomePartCategory.W2, false);
            assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getQualified(),
                    "W2 income category could not be deselected on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
            assertEquals(bigD(0), bigD(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getQualifyingIncome()),
                    "W2 qualifying income value was not zero after deselection on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeCategory(firstApplicant[0].getId(), IncomePartCategory.W2, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getQualified(),
                    "W2 income category could not be selected on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        });
    }

    @Test
    void incomeTypeShouldBePossibleToSelectDeselect() {
        final RestGetResponseApplicant[] firstApplicant = {getResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())};
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomePartType.BASE_PAY, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getQualified(),
                    "Income type was not selected by default or could not be selected on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomePartType.BASE_PAY, false);
            assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getQualified(),
                    "Income type could not be deselected on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncomeType(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomePartType.BASE_PAY, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getQualified(),
                    "Income type could not be selected on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        });
    }

    @Test
    @Description("IA-1328 Check If Deselecting Previous Year Deselect Two Year Prior")
    void deselectingPreviousYearShouldDeselectTwoYearPrior() {
        RestIncomePart firstApplicantBasePayIncome = getResponse
                .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                .getBasePay();

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment()).getId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
            assertAll("Asserting selection of incomes for previous years on folder: " + folderBuilderPerfCanonicalTest.getFolderId(),
                    () -> assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getSelected(),
                            "Prior year could not be selected"),
                    () -> assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(TWO_YEARS_PRIOR).getSelected(),
                            "Two years prior could not be selected")
            );
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
            assertAll("Asserting income selection on deselecting previous year on folder: " + folderBuilderPerfCanonicalTest.getFolderId(),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getSelected(),
                            "Prior year has not been deselected"),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(TWO_YEARS_PRIOR).getSelected(),
                            "Two years prior was not deselected automatically")
            );
        });
    }

    @Test
    @Description("IA-1279 Check If Deselecting Previous Year Does Not Defaults Historical Average To Ytd For Hourly Income")
    void deselectingPreviousYearShouldNotDefaultHistoricalAverageToYtdForHourlyIncome() {
        RestIncomePart firstApplicantBasePayIncome = getResponse
                .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                .getBasePay();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment()).getId());
        Long firstApplicantId = getResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName()).getId();
        String firstApplicantGroupId = getResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
            selectIncomeAvg(firstApplicantId, firstApplicantGroupId, IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS, true);
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                            .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypes().get("BASE_PAY")
                            .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(),
                    "Historical Average for two years prior could not be selected on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        });

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
            assertAll("Assertion of defaults on deselecting previous year income on folder: " + folderBuilderPerfCanonicalTest.getFolderId(),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getSelected(),
                            "Prior year is still selected"),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(),
                            "Historical Average for two years prior is still selected"),
                    () -> assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected(),
                            "Historical Average did not default to YTD")
            );
        });
    }

    @Test
    @Description("IA-1451 Check If Selecting Year Before Prior Shows Values In Historical Average And Defaults To Lowest Income")
    void selectingYearBeforePriorShouldShowValuesInHaAndDefaultToLowestIncome() {
        RestIncomePart firstApplicantBasePayIncome = getResponse
                .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                .getBasePay();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                .getIncomeCategoryW2()
                                .getPrimaryIncomeGroup()
                                .getIncomeTypes().get("BASE_PAY")
                                .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(),
                        "Historical Average income for two years prior did not get unselected on deselecting previous year on folder: " + folderBuilderPerfCanonicalTest.getFolderId()));

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryGroupId, firstApplicantBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
            assertAll("Asserting Historical Average income defaults on selecting two years prior on folder: " + folderBuilderPerfCanonicalTest.getFolderId(),
                    () -> assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(),
                            "Historical Average for YTD + previous was not selected"),
                    () -> assertNotNull(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getAvgMonthlyIncome(),
                            "Monthly income for previous year is null"),
                    () -> assertNotNull(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getGross(),
                            "Gross amount for previous year is null"),
                    () -> assertNotNull(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                    .getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypes().get("BASE_PAY")
                                    .getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getMonths(),
                            "Months worked for previous year is null")

            );
        });
    }

    @Test
    void incomeHistoricalAveragesShouldBePossibleToSelect() {
        final RestGetResponseApplicant[] firstApplicant = new RestGetResponseApplicant[1];
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId()).getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName());

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

        selectIncomeAvg(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR, true);
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId()).getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName());
        assertAll("ytd avg + prev year selection on folder: " + folderBuilderPerfCanonicalTest.getFolderId(),
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

        selectIncomeAvg(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS, true);
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId()).getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName());
        assertAll("ytd avg + two years selection on folder: " + folderBuilderPerfCanonicalTest.getFolderId(),
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

        selectIncomeAvg(firstApplicant[0].getId(), firstApplicant[0].getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), IncomeAvg.BASEPAY_YTD_AVG, true);
        firstApplicant[0] = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId()).getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName());
        assertAll("ytd avg selection on folder: " + folderBuilderPerfCanonicalTest.getFolderId(),
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
    void onlyOneMonthlyIncomeShouldBeSelectedForOneIncomePart() {
        RestPartIncome income = getResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                .getBasePay()
                .getIncome(YEAR_TO_DATE);

        int selectedCount = 0;
        for (RestPartIncomeAnnualSummary document : income.getAnnualSummary()) {
            if (document.getMonthlyAmountCalculated().getSelected())
                selectedCount++;
            if (document.getMonthlyAmountAvg().getSelected())
                selectedCount++;
        }
        assertEquals(1, selectedCount, "No income or more than one is selected by default on folder: " + folderBuilderPerfCanonicalTest.getFolderId());
        RestPartIncomeAnnualSummary incomeToSelect = income.getAnnualSummaryNotSelectedDocuments().get(0);
        RestChangeRequests.selectJobAnnualSummary(income.getId(), incomeToSelect.getDocIds().get(0), AnnualSummaryIncomeType.CALCULATED);

        getResponse = RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId());

        income = getResponse.getApplicant(folderBuilderPerfCanonicalTest.getBorrowerFullName())
                .getIncome(folderBuilderPerfCanonicalTest.getBorrowerCurrentEmployment())
                .getBasePay()
                .getIncome(YEAR_TO_DATE);

        selectedCount = 0;
        for (RestPartIncomeAnnualSummary document : income.getAnnualSummary()) {
            if (document.getMonthlyAmountCalculated().getSelected())
                selectedCount++;
            if (document.getMonthlyAmountAvg().getSelected())
                selectedCount++;
        }
        assertEquals(1, selectedCount, "No income or more than one is selected" + folderBuilderPerfCanonicalTest.getFolderId());
    }

    @Test
    void applicantCheckShouldSelectOrDeselectHim() {
        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(0).getId(), false);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertFalse(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                .getApplicants().get(0)
                                .isQualified(),
                        "Applicant should become unqualified on folder: " + folderBuilderPerfCanonicalTest.getFolderId())
        );

        RestChangeRequests.selectApplicant(getResponse.getApplicants().get(0).getId(), true);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertTrue(RestGetLoanData.getApplicationData(folderBuilderPerfCanonicalTest.getFolderId())
                                .getApplicants().get(0)
                                .isQualified(),
                        "Applicant should become qualified on folder: " + folderBuilderPerfCanonicalTest.getFolderId())
        );
    }

    @Test
    void checkIfAllCanonicalDocumentsHaveBeenImportedCorrectlyToAnnualSummaries() {
        getResponse.getApplicants().forEach(applicant ->
                applicant.getIncomes().forEach(job ->
                        job.getBasePay().getIncomes().forEach(year -> {
                            if (YEAR_TO_DATE.equals(year.getYear())) {
                                assertEquals(2, year.getAnnualSummary().size(), String.format("YearToDate annualSummary expected 2 documents, found: %s", year.getAnnualSummary().size()));
                                assertEquals(2, year.getAllAnnualSummary().size(), String.format("YearToDate allAnnualSummary expected 2 documents, found: %s", year.getAllAnnualSummary().size()));
                            } else {
                                assertEquals(3, year.getAnnualSummary().size(), String.format("PreviousYear annualSummary expected 2 documents, found: %s", year.getAnnualSummary().size()));
                                assertEquals(3, year.getAllAnnualSummary().size(), String.format("PreviousYear allAnnualSummary expected 2 documents, found: %s", year.getAllAnnualSummary().size()));
                            }
                        })));
    }
}

package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncome;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.BIG_DECIMAL_PRECISION_TWO_POINTS;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.AVG;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.CALCULATED;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.PTO;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@SuppressWarnings("rawtypes")
@Execution(CONCURRENT)
@ResourceLock(value = "StoryTest")
class StoryTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderStoryTest = createFolderBuilder("IARestStory");
    private final int BIG_DECIMAL_COMPARE_EQUAL = 0;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderStoryTest
                .setBorrowerYearsOnThisJob("1")
                .setBorrowerMonthsOnThisJob("0")
                .setBorrowerPreviousJobStartDate("05/01/" + ONE_YEAR_PRIOR)
                .setBorrowerPreviousJobEndDate("12/31/" + ONE_YEAR_PRIOR)
                .setCoBorrowerPreviousJobEndDate("05/31/" + YEAR_TO_DATE)
                .generateLoanDocument().restBuild();

        dataUpload = createUploadObject(folderBuilderStoryTest);
        //Line used to break test execution in case of changed API response
        RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId());
    }

    Stream paystubTestCases() {
        return Stream.of(
                of("03/01/" + YEAR_TO_DATE, "03/07/" + YEAR_TO_DATE, WEEKLY, bigD(41.00), bigD(20.00), bigD(7.00), bigD(20.00)),
                of("03/01/" + YEAR_TO_DATE, "03/07/" + YEAR_TO_DATE, WEEKLY, bigD(41.00), bigD(80.00), bigD(7.00), bigD(20.00)),
                of("03/01/" + YEAR_TO_DATE, "03/14/" + YEAR_TO_DATE, BI_WEEKLY, bigD(100.00), bigD(20.00), bigD(10.00), bigD(20.00)),
                of("03/01/" + YEAR_TO_DATE, "03/14/" + YEAR_TO_DATE, BI_WEEKLY, bigD(100.00), bigD(80.00), bigD(10.00), bigD(20.00)),
                of("03/01/" + YEAR_TO_DATE, "03/15/" + YEAR_TO_DATE, SEMI_MONTHLY, bigD(170.00), bigD(20.00), bigD(16.67), bigD(20.00)),
                of("03/01/" + YEAR_TO_DATE, "03/15/" + YEAR_TO_DATE, SEMI_MONTHLY, bigD(170.00), bigD(80.00), bigD(16.67), bigD(20.00)),
                of("03/01/" + YEAR_TO_DATE, "03/31/" + YEAR_TO_DATE, MONTHLY, bigD(270.00), bigD(20.00), bigD(90.00), bigD(20.00)),
                of("03/01/" + YEAR_TO_DATE, "03/31/" + YEAR_TO_DATE, MONTHLY, bigD(270.00), bigD(80.00), bigD(90.00), bigD(20.00))
        );
    }

    Stream frequencyPrecedencePaystub() {
        return Stream.of( // startDate endDate payDate, explicitFreq, manualFreq, expected projectedIncome
                of(null, null, "01/31/" + YEAR_TO_DATE, SEMI_MONTHLY, SEMI_MONTHLY, bigD(41.00)),
                of(null, null, "01/31/" + YEAR_TO_DATE, MONTHLY, null, bigD(41.00)),
                of("01/01/" + YEAR_TO_DATE, null, "01/31/" + YEAR_TO_DATE, SEMI_MONTHLY, SEMI_MONTHLY, bigD(41.00)),
                of("01/01/" + YEAR_TO_DATE, null, "01/31/" + YEAR_TO_DATE, MONTHLY, null, bigD(41.00)),
                of("01/01/" + YEAR_TO_DATE, "01/31/" + YEAR_TO_DATE, "01/31/" + YEAR_TO_DATE, WEEKLY, WEEKLY, bigD(41.00)),
                of("01/01/" + YEAR_TO_DATE, "01/31/" + YEAR_TO_DATE, "01/31/" + YEAR_TO_DATE, BI_WEEKLY, null, bigD(41.00))
        );
    }

    Stream missingDateOneFrequencyPaystub() {
        return Stream.of( // startDate endDate payDate, explicitFreq, manualFreq, expected projectedIncome
                of(null, null, "01/31/" + YEAR_TO_DATE, null, WEEKLY, bigD(41.00)),
                of(null, null, "01/31/" + YEAR_TO_DATE, BI_WEEKLY, null, bigD(41.00)),
                of("01/01/" + YEAR_TO_DATE, null, "01/31/" + YEAR_TO_DATE, null, SEMI_MONTHLY, bigD(41.00)),
                of("01/01/" + YEAR_TO_DATE, null, "01/31/" + YEAR_TO_DATE, MONTHLY, null, bigD(41.00))
        );
    }

    Stream missingDateBothFrequenciesPaystub() {
        return Stream.of( // startDate endDate payDate, explicitFreq, manualFreq, expected projectedIncome
                of(null, null, "01/31/" + YEAR_TO_DATE, MONTHLY, WEEKLY, bigD(1234.00)),
                of("01/01/" + YEAR_TO_DATE, null, "01/31/" + YEAR_TO_DATE, WEEKLY, MONTHLY, bigD(5678.00))
        );
    }

    Stream missingDateYtdPaystub() {
        return Stream.of( // startDate endDate payDate, explicitFreq, manualFreq, expected projectedIncome
                of("01/01/" + YEAR_TO_DATE, "01/07/" + YEAR_TO_DATE, "01/14/" + YEAR_TO_DATE, WEEKLY, MONTHLY, bigD(5678.00)),
                of("01/01/" + YEAR_TO_DATE, null, "01/16/" + YEAR_TO_DATE, MONTHLY, WEEKLY, bigD(1234.00)),
                of("01/01/" + YEAR_TO_DATE, "01/18/" + YEAR_TO_DATE, null, MONTHLY, WEEKLY, bigD(1234.00)),
                of(null, "01/20/" + YEAR_TO_DATE, null, MONTHLY, WEEKLY, bigD(1234.00)),
                of("12/01/" + ONE_YEAR_PRIOR, "12/18/" + ONE_YEAR_PRIOR, "12/21/" + ONE_YEAR_PRIOR, WEEKLY, MONTHLY, bigD(5678.00)),
                of("12/01/" + ONE_YEAR_PRIOR, null, "12/22/" + ONE_YEAR_PRIOR, MONTHLY, WEEKLY, bigD(1234.00)),
//                of("12/01/" + ONE_YEAR_PRIOR, "12/23/" + ONE_YEAR_PRIOR, null, IncomeFrequency.MONTHLY, IncomeFrequency.WEEKLY, bigD(1234.00)),
//                of(null, "12/24/" + ONE_YEAR_PRIOR, null, IncomeFrequency.MONTHLY, IncomeFrequency.WEEKLY, bigD(1234.00)),
                of("12/01/" + TWO_YEARS_PRIOR, "12/18/" + TWO_YEARS_PRIOR, "12/21/" + TWO_YEARS_PRIOR, WEEKLY, MONTHLY, bigD(5678.00)),
                of("12/01/" + TWO_YEARS_PRIOR, null, "12/22/" + TWO_YEARS_PRIOR, MONTHLY, WEEKLY, bigD(1234.00))
//                of("12/01/" + TWO_YEARS_PRIOR, "12/23/" + TWO_YEARS_PRIOR, null, IncomeFrequency.MONTHLY, IncomeFrequency.WEEKLY, bigD(1234.00)),
//                of(null, "12/24/" + TWO_YEARS_PRIOR, null, IncomeFrequency.MONTHLY, IncomeFrequency.WEEKLY, bigD(1234.00))
        );
    }

    @ParameterizedTest(name = "{index} Explicit frequency paystub start date: {0} end date: {1} pay date: {2} explicit Frequency: {3} manual Frequency: {4} expected projected Income: {5}")
    @MethodSource("frequencyPrecedencePaystub")
    @Description("IA-2395 Check If Explicit Frequency Is Taken Only When There Is No Calculated And No Manual Frequency")
    void checkIfExplicitFrequencyIsTakenOnlyWhenThereIsNoCalculatedAndNoManualFrequency(String startDate, String endDate, String payDate,
                                                                                        IncomeFrequency explicitFrequency, IncomeFrequency manualFrequency, BigDecimal expectedProjectedIncome) {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderStoryTest.getCoBorrowerFullName(),
                folderBuilderStoryTest.getCoBorrowerSSN(),
                folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                folderBuilderStoryTest.getCoBorrowerCurrentEmployment(),
                startDate,
                endDate,
                new PaystubData.PaystubIncomeRow(REGULAR, null, null,
                        expectedProjectedIncome.toString(), expectedProjectedIncome.toString()))
                .setPayDate(payDate)
                .setExplicitFrequency(explicitFrequency)
                .setManualFrequency(manualFrequency));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary currentPaystub = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName()).getIncome(folderBuilderStoryTest.getCoBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);

            if (endDate == null) {
                if (manualFrequency != null) {
                    assertAll("Manual frequency when dates are missing",
                            () -> assertEquals(manualFrequency, currentPaystub.getFrequency().getValue()),
                            () -> assertEquals(ValueType.MANUAL, currentPaystub.getFrequency().getValueType())
                    );
                } else {
                    assertAll("Explicit frequency when dates are missing",
                            () -> assertEquals(explicitFrequency, currentPaystub.getFrequency().getValue()),
                            () -> assertEquals(ValueType.EXPLICIT, currentPaystub.getFrequency().getValueType())
                    );
                }
            } else {
                if (manualFrequency != null) {
                    assertAll("Manual frequency when dates are missing",
                            () -> assertEquals(manualFrequency, currentPaystub.getFrequency().getValue()),
                            () -> assertEquals(ValueType.MANUAL, currentPaystub.getFrequency().getValueType())
                    );
                } else {
                    assertAll("Explicit frequency when dates are missing",
                            () -> assertEquals(MONTHLY, currentPaystub.getFrequency().getValue()),
                            () -> assertEquals(ValueType.CALCULATED, currentPaystub.getFrequency().getValueType())
                    );
                }
            }
        });
    }

    @ParameterizedTest(name = "{index} Missing date paystub start date: {0} end date: {1} pay date: {2} explicit Frequency: {3} manual Frequency: {4} expected projected Income: {5}")
    @MethodSource({"missingDateOneFrequencyPaystub", "missingDateBothFrequenciesPaystub"})
    @Tag("integration")
    @Description("IA-1724 Check If Manual And Explicit Frequencies Are Used When Dates Are Missing")
    void checkIfManualAndExplicitFrequenciesAreUsedWhenDatesAreMissing(String startDate, String endDate, String payDate,
                                                                       IncomeFrequency explicitFrequency, IncomeFrequency manualFrequency, BigDecimal expectedProjectedIncome) {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderStoryTest.getCoBorrowerFullName(),
                folderBuilderStoryTest.getCoBorrowerSSN(),
                folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                folderBuilderStoryTest.getCoBorrowerCurrentEmployment(),
                startDate,
                endDate,
                new PaystubData.PaystubIncomeRow(REGULAR, null, null,
                        expectedProjectedIncome.toString(), expectedProjectedIncome.toString()))
                .setPayDate(payDate)
                .setExplicitFrequency(explicitFrequency)
                .setManualFrequency(manualFrequency));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary currentPaystub = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName()).getIncome(folderBuilderStoryTest.getCoBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);

            if (manualFrequency != null) {
                assertAll("Manual frequency when dates are missing",
                        () -> assertEquals(manualFrequency, currentPaystub.getFrequency().getValue()),
                        () -> assertEquals(ValueType.MANUAL, currentPaystub.getFrequency().getValueType())
                );
            } else {
                assertAll("Explicit frequency when dates are missing",
                        () -> assertEquals(explicitFrequency, currentPaystub.getFrequency().getValue()),
                        () -> assertEquals(ValueType.EXPLICIT, currentPaystub.getFrequency().getValueType())
                );
            }
        });
    }

    @ParameterizedTest(name = "{index} Paystub start date: {0} end date: {1} pay date: {2} explicit Frequency: {3} manual Frequency: {4} expected projected Income: {5}")
    @MethodSource({"missingDateYtdPaystub", "missingDateBothFrequenciesPaystub"})
    @Description("IA-2361 Check If Ytd Year Is Determined Correctly If Dates Are Missing")
    void checkIfYtdYearIsDeterminedCorrectlyIfDatesAreMissing(String startDate, String endDate, String payDate,
                                                              IncomeFrequency explicitFrequency, IncomeFrequency manualFrequency, BigDecimal expectedProjectedIncome) {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        folderBuilderStoryTest.getCoBorrowerFullName(),
                        folderBuilderStoryTest.getCoBorrowerSSN(),
                        folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getCoBorrowerPreviousEmployment(),
                        startDate,
                        endDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, null, null,
                                expectedProjectedIncome.toString(), expectedProjectedIncome.toString()))
                        .setPayDate(payDate)
                        .setExplicitFrequency(explicitFrequency)
                        .setManualFrequency(manualFrequency));
        Integer year = 0;
        if (endDate != null) {
            year = Integer.parseInt(endDate.split("/")[2]);
        } else if (payDate != null) {
            year = Integer.parseInt(payDate.split("/")[2]);
        } else {
            year = Integer.parseInt(startDate.split("/")[2]);
        }

        Integer finalYear = year;
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
            RestPartIncomeAnnualSummary currentPaystub = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName())
                    .getIncome(folderBuilderStoryTest.getCoBorrowerPreviousEmployment())
                    .getBasePay()
                    .getIncome(finalYear)
                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);

            assertAll("Docs",
                    () -> assertEquals(startDate, currentPaystub.getPayPeriodStartDate().getValue()),
                    () -> assertEquals(bigD(expectedProjectedIncome), bigD(currentPaystub.getGrossAmount().getValue())),
                    () -> {
                        if (endDate != null)
                            assertEquals(endDate, currentPaystub.getPayPeriodEndDate().getValue());
                        else
                            assertEquals(payDate, currentPaystub.getPayPeriodEndDate().getValue());
                    }
            );
        });
    }

    @Test
    @Description("IA-1140 Check If Ytd Avg Income And Months Worked Are Calculated For Unknown Frequency Paystub")
    void checkIfYtdAvgIncomeAndMoWorkedAreCalculatedForUnknownFrequencyPaystub() {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderStoryTest.getBorrowerFullName(),
                folderBuilderStoryTest.getBorrowerSSN(),
                folderBuilderStoryTest.getBorrowerCollaboratorId(),
                folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                "01/27/" + YEAR_TO_DATE,
                "01/29/" + YEAR_TO_DATE,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "4,000.00")));
        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId());

        assertAll("Check YTD Avg default values for unknown frequency paystub on folder: " + folderBuilderStoryTest.getFolderId(),
                () -> assertNotNull(response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                .getBasePay()
                                .getIncome(YEAR_TO_DATE)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                                .getMonthlyAmountAvg().getValue(),
                        "Ytd Avg income value is null"),
                () -> assertNull(response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                .getBasePay()
                                .getIncome(YEAR_TO_DATE)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                                .getMonthlyAmountCalculated().getValue(),
                        "Calculated monthly income is not null"),
                () -> assertFalse(response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                .getBasePay()
                                .getIncome(YEAR_TO_DATE)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                                .getMonthlyAmountAvg().getSelected(),
                        "Ytd Avg Income is selected"),
                () -> assertNotNull(response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                .getBasePay()
                                .getIncome(YEAR_TO_DATE)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                                .getMonths().getValue(),
                        "Months worked is null")
        );
    }

    @Test
    @Description("IA-1189 Check If Months Worked Is Calculated Correctly As Per Doc Precedence")
    void checkIfMonthsWorkedIsCalculatedCorrectlyAsPerDocPrecedence() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "4,000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerPreviousEmployment(),
                        "05/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "4,000.00")))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                        "47,321.00",
                        ONE_YEAR_PRIOR.toString()))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerPreviousEmployment(),
                        "47,321.00",
                        ONE_YEAR_PRIOR.toString()))
                .importDocumentList();

        checkMoWorkedFor2018PrimaryBorrower(new BigDecimal(10.07), new BigDecimal(8));

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                        MONTHLY,
                        "01/01/" + ONE_YEAR_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "2000.00",
                        "6000.00",
                        "01/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerPreviousEmployment(),
                        MONTHLY,
                        "01/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        "2000.00",
                        "0",
                        "0",
                        "0"))
                .importDocumentList();

        checkMoWorkedFor2018PrimaryBorrower(new BigDecimal(12), new BigDecimal(12));
    }

    @ParameterizedTest(name = "{index} Paystub hours sum for {0}-{1} hours: {2} {4} and rates {3} {5} expected frequency: {2}")
    @MethodSource("paystubTestCases")
    @Description("IA-1374 IA-1632 Check If Paystub With Two Same Type Incomes Have Hours Summed Up To Selected Frequency")
    void checkIfPaystubWithTwoSameTypeIncomesHaveHoursSummedUpToSelectedFrequency(String startDate, String endDate, IncomeFrequency frequency,
                                                                                  BigDecimal firstIncomeHours, BigDecimal firstIncomeRate,
                                                                                  BigDecimal secondIncomeHours, BigDecimal secondIncomeRate) {
        BigDecimal calculatedTotalPeriodIncome = firstIncomeHours.multiply(firstIncomeRate).add(secondIncomeHours.multiply(secondIncomeRate)).setScale(2, RoundingMode.HALF_UP);
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                        startDate,
                        endDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, firstIncomeRate.toString(), firstIncomeHours.toString(),
                                calculatedTotalPeriodIncome.toString(), calculatedTotalPeriodIncome.toString()),
                        new PaystubData.PaystubIncomeRow(PTO, secondIncomeRate.toString(), secondIncomeHours.toString(),
                                calculatedTotalPeriodIncome.toString(), calculatedTotalPeriodIncome.toString())));

        if (frequency.equals(UNKNOWN)) {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId());
            assertNull(getResponse.getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                    .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment()).getBasePay()
                    .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB));
        } else {
            BigDecimal incomeMaxPeriodHours = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
            switch (frequency) {
                case WEEKLY:
                    incomeMaxPeriodHours = bigD(40);
                    break;
                case BI_WEEKLY:
                    incomeMaxPeriodHours = bigD(80);
                    break;
                case SEMI_MONTHLY:
                    incomeMaxPeriodHours = bigD(86.67);
                    break;
                case MONTHLY:
                    incomeMaxPeriodHours = bigD(173.34);
                    break;
                default:
                    break;
            }

            BigDecimal finalHours;
            BigDecimal finalOriginalHours = firstIncomeHours.add(secondIncomeHours);
            if (finalOriginalHours.compareTo(incomeMaxPeriodHours) > 0)
                finalHours = incomeMaxPeriodHours;
            else
                finalHours = firstIncomeHours.add(secondIncomeHours);


            Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
                BigDecimal projectedMonthlyIncome;

                if (firstIncomeRate.compareTo(secondIncomeRate) < 0) {
                    projectedMonthlyIncome = firstIncomeRate.multiply(finalHours).setScale(2, RoundingMode.HALF_UP);
                } else {
                    projectedMonthlyIncome = secondIncomeRate.multiply(finalHours).setScale(2, RoundingMode.HALF_UP);
                }

                switch (frequency) {
                    case WEEKLY:
                        projectedMonthlyIncome = projectedMonthlyIncome.multiply(bigD(52)).divide(bigD(12), 2, RoundingMode.HALF_UP);
                        break;
                    case BI_WEEKLY:
                        projectedMonthlyIncome = projectedMonthlyIncome.multiply(bigD(26)).divide(bigD(12), 2, RoundingMode.HALF_UP);
                        break;
                    case SEMI_MONTHLY:
                        projectedMonthlyIncome = projectedMonthlyIncome.multiply(bigD(24)).divide(bigD(12), 2, RoundingMode.HALF_UP);
                        break;
                    default:
                        break;
                }
                RestPartIncomeAnnualSummary getResponse = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId()).getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                        .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment()).getBasePay()
                        .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB);


                BigDecimal finalProjectedMonthlyIncome = projectedMonthlyIncome;
                assertAll("calculation assertion",
                        () -> assertEquals(getResponse.getMonthlyAmountCalculated().getValue().setScale(2, RoundingMode.HALF_UP),
                                finalProjectedMonthlyIncome,
                                String.format("Projected monthly income was not calculated correctly found: %s", finalProjectedMonthlyIncome)),
                        () -> assertEquals(finalHours, bigD(getResponse.getHours().getValue())),
                        () -> assertEquals(finalOriginalHours, bigD(getResponse.getHours().getOriginalValue()))
                );
            });
        }
    }

    @Test
    @Description("IA-1379 Check If Months Worked And Ytd Avg Are Calculated Properly For Voe With Unknown Hours")
    void checkIfMonthsWorkedAndYtdAvgAreCalculatedProperlyForVoeWithUnknownHours() {
        checkHourlyVoeDisplayForUnknownHoursOrRate(true);
    }

    @Test
    @Description("IA-1379 Check If Months Worked And Ytd Avg Are Calculated Properly For Voe With Unknown Rate")
    void checkIfMonthsWorkedAndYtdAvgAreCalculatedProperlyForVoeWithUnknownRate() {
        checkHourlyVoeDisplayForUnknownHoursOrRate(false);
    }

    @Test
    @Tag("health")
    @Description("IA-1485 Check If Min Income Is Selected For Hourly Trending Down By Default")
    void checkIfMinIncomeIsSelectedForHourlyTrendingDownByDefault() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderStoryTest.getCoBorrowerFullName(),
                        folderBuilderStoryTest.getCoBorrowerSSN(),
                        folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "15.00", "40.00", "500.00", "1200.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderStoryTest.getCoBorrowerFullName(),
                        folderBuilderStoryTest.getCoBorrowerSSN(),
                        folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "15.00", "40.00", "12000.00", "11000.00")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderStoryTest.getCoBorrowerFullName(),
                        folderBuilderStoryTest.getCoBorrowerSSN(),
                        folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getCoBorrowerCurrentEmployment(),
                        HOURLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "24.00",
                        "900.00",
                        "01/31/" + YEAR_TO_DATE)
                        .setAvgHoursPerWeek("40"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncome ytdDocuments = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName()).getIncome(folderBuilderStoryTest.getCoBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);

            assertAll(String.format("Asserting that lowest income is selected if trending for lowest ytd avg income is negative on folder: %s", folderBuilderStoryTest.getFolderId()),
                    () -> assertTrue(ytdDocuments.getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getSelected(), "Lowest Income was not selected"),
                    () -> assertFalse(ytdDocuments.getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountAvg().getSelected(), "Lowest avg income was selected"),
                    () -> assertFalse(ytdDocuments.getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountCalculated().getSelected(), "VoE monthly Income was selected"),
                    () -> assertFalse(ytdDocuments.getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getSelected(), "Paystub avg income was selected")
            );
        });
    }

    @Test
    @Description("IA-2065 Check If W-2 Tips Are Subtracted From Total Income")
    void checkIfW2TipsAreSubtractedFromTotalIncome() {
        dataUpload.importDocument(dataUpload.createCustomW2(
                folderBuilderStoryTest.getCoBorrowerFullName(),
                folderBuilderStoryTest.getCoBorrowerSSN(),
                folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                folderBuilderStoryTest.getCoBorrowerPreviousEmployment(),
                "3000.00",
                ONE_YEAR_PRIOR.toString())
                .setAllocatedTips("500.00")
                .setSocialSecurityTips("1000.00"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary w2IncomeDocument = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName())
                    .getIncome(folderBuilderStoryTest.getCoBorrowerPreviousEmployment())
                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2);

            assertEquals(bigD(1500, 8), w2IncomeDocument.getGrossAmount().getValue(),
                    "Tips were not subtracted from W-2 income, income was equal to: " + w2IncomeDocument.getGrossAmount().getValue());
        });
    }

    @Test
    @Description("IA-2065 Check If W-2 Tips With One Being Zero Are Subtracted From Total Income")
    void checkIfW2WithZeroTipsAreSubtractedFromTotalIncome() {
        dataUpload.importDocument(dataUpload.createCustomW2(
                folderBuilderStoryTest.getCoBorrowerFullName(),
                folderBuilderStoryTest.getCoBorrowerSSN(),
                folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                folderBuilderStoryTest.getCoBorrowerPreviousEmployment(),
                "3000.00",
                ONE_YEAR_PRIOR.toString())
                .setAllocatedTips("500.00")
                .setSocialSecurityTips("0.00"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary w2IncomeDocument = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName())
                    .getIncome(folderBuilderStoryTest.getCoBorrowerPreviousEmployment())
                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2);

            assertEquals(bigD(2500, 8), w2IncomeDocument.getGrossAmount().getValue(),
                    "Tips were not subtracted from W-2 income, income was equal to: " + w2IncomeDocument.getGrossAmount().getValue());
        });
    }

    @Test
    @Description("IA-2065 IA-2714 Check If W-2 Only Social Security Tips Are Subtracted From Total Income")
    void checkIfW2OnlySocialSecurityTipsAreSubtractedFromTotalIncome() {
        dataUpload.importDocument(dataUpload.createCustomW2(
                folderBuilderStoryTest.getCoBorrowerFullName(),
                folderBuilderStoryTest.getCoBorrowerSSN(),
                folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                folderBuilderStoryTest.getCoBorrowerPreviousEmployment(),
                "3000.00",
                ONE_YEAR_PRIOR.toString())
                .setSocialSecurityTips("1000.00"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary w2IncomeDocument = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName())
                    .getIncome(folderBuilderStoryTest.getCoBorrowerPreviousEmployment())
                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2);

            assertEquals(bigD(2000, 8), w2IncomeDocument.getGrossAmount().getValue(),
                    "Tips were not subtracted from W-2 income, income was equal to: " + w2IncomeDocument.getGrossAmount().getValue());
        });
    }

    @Test
    @Description("IA-2065 IA-2714 Check If W-2 Only Allocated Tips Are Subtracted From Total Income")
    void checkIfW2OnlyAllocatedTipsAreSubtractedFromTotalIncome() {
        dataUpload.importDocument(dataUpload.createCustomW2(
                folderBuilderStoryTest.getCoBorrowerFullName(),
                folderBuilderStoryTest.getCoBorrowerSSN(),
                folderBuilderStoryTest.getCoBorrowerCollaboratorId(),
                folderBuilderStoryTest.getCoBorrowerPreviousEmployment(),
                "3000.00",
                ONE_YEAR_PRIOR.toString())
                .setAllocatedTips("500.00"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestPartIncomeAnnualSummary w2IncomeDocument = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                    .getApplicant(folderBuilderStoryTest.getCoBorrowerFullName())
                    .getIncome(folderBuilderStoryTest.getCoBorrowerPreviousEmployment())
                    .getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.W2);

            assertEquals(bigD(2500, 8), w2IncomeDocument.getGrossAmount().getValue(),
                    "Tips were not subtracted from W-2 income, income was equal to: " + w2IncomeDocument.getGrossAmount().getValue());
        });
    }

    @Test
    @Tag("integration")
    void checkIfIncomeDoesNotThrowNullPointerWhenThereIsOnlyOneAvgIncomeInYtdForHourlyDocs() {
        IAFolderBuilder hourlyNpeLoanDoc = createFolderBuilder("IARestNPE");
        hourlyNpeLoanDoc.generateLoanDocument().restBuild();
        try {
            DataUploadObject dataUpload = createUploadObject(hourlyNpeLoanDoc);
            dataUpload.clearDocuments()
                    .addDocument(dataUpload.createCustomPaystub(
                            hourlyNpeLoanDoc.getBorrowerFullName(),
                            hourlyNpeLoanDoc.getBorrowerSSN(),
                            hourlyNpeLoanDoc.getBorrowerCollaboratorId(),
                            hourlyNpeLoanDoc.getBorrowerCurrentEmployment(),
                            "01/01/" + YEAR_TO_DATE,
                            "01/14/" + YEAR_TO_DATE,
                            new PaystubData.PaystubIncomeRow(REGULAR, "25.00", "80.00", "1000.00", "")))
                    .addDocument(dataUpload.createCustomVoeCurrent(
                            hourlyNpeLoanDoc.getBorrowerFullName(),
                            hourlyNpeLoanDoc.getBorrowerSSN(),
                            hourlyNpeLoanDoc.getBorrowerCollaboratorId(),
                            hourlyNpeLoanDoc.getBorrowerCurrentEmployment(),
                            HOURLY,
                            "01/01/" + TWO_YEARS_PRIOR,
                            "01/31/" + YEAR_TO_DATE,
                            "24.00",
                            "1500.00",
                            "01/31/" + YEAR_TO_DATE)
                            .setAvgHoursPerWeek("40")
                            .setPriorYearBasePay("12,800.00")
                            .setTwoYearPriorBasePay("28,800.00")
                            .setPriorYearTotal("28,800.00")
                            .setTwoYearPriorTotal("17,000.00"))
                    .importDocumentList();

            RestGetLoanData.getApplicationData(hourlyNpeLoanDoc.getFolderId());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception on importing data with only one ytd hourly income"); //NOSONAR
        }
    }

    @Test
    @Description("IA-2070 Check If Deselecting One Year In Overtime Disabled Entire Income Type")
    void checkIfDeselectingOneYearInOvertimeDisabledEntireIncomeType() {
        IAFolderBuilder previousVoeOtLoanDoc = createFolderBuilder("IARestPreOT");
        previousVoeOtLoanDoc.generateLoanDocument().restBuild();
        DataUploadObject dataUpload = createUploadObject(previousVoeOtLoanDoc);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        previousVoeOtLoanDoc.getBorrowerFullName(),
                        previousVoeOtLoanDoc.getBorrowerSSN(),
                        previousVoeOtLoanDoc.getBorrowerCollaboratorId(),
                        previousVoeOtLoanDoc.getBorrowerCurrentEmployment(),
                        MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1500.00",
                        "1500.00",
                        "01/31/" + YEAR_TO_DATE)
                        .setYtdOvertime("120.00")
                        .setPriorYearOvertime("12,000.00")
                        .setTwoYearPriorOvertime("11,800.00")
                        .setPriorYearTotal("28,800.00")
                        .setTwoYearPriorTotal("17,000.00"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        previousVoeOtLoanDoc.getBorrowerFullName(),
                        previousVoeOtLoanDoc.getBorrowerSSN(),
                        previousVoeOtLoanDoc.getBorrowerCollaboratorId(),
                        previousVoeOtLoanDoc.getBorrowerPreviousEmployment(),
                        MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + TWO_YEARS_PRIOR,
                        "1000.00",
                        "1000.00",
                        "0.00",
                        "0.00"))
                .importDocumentList();


        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(previousVoeOtLoanDoc.getFolderId());

            selectIncomeType(getResponse.getApplicant(previousVoeOtLoanDoc.getBorrowerFullName()).getId(),
                    getResponse.getApplicant(previousVoeOtLoanDoc.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(), OVERTIME, true);

            String primaryGroupId = Long.toString(getResponse
                    .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                    .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment()).getId());

            RestIncomePart currentJobOvertime = getResponse.getApplicant(previousVoeOtLoanDoc.getBorrowerFullName()).getIncome(previousVoeOtLoanDoc.getBorrowerCurrentEmployment()).getOvertime();
            RestIncomePart previousJobOvertime = getResponse.getApplicant(previousVoeOtLoanDoc.getBorrowerFullName()).getIncome(previousVoeOtLoanDoc.getBorrowerPreviousEmployment()).getOvertime();

            selectIncome(primaryGroupId, previousJobOvertime.getIncome(TWO_YEARS_PRIOR).getId(), true);
            assertTrue(
                    getResponse.getApplicant(previousVoeOtLoanDoc.getBorrowerFullName()).getIncome(previousVoeOtLoanDoc.getBorrowerCurrentEmployment()).getOvertime().getSelected(), "Overtime was not selected before deselecting year");

            selectIncome(primaryGroupId, previousJobOvertime.getIncome(TWO_YEARS_PRIOR).getId(), false);
            assertTrue(
                    getResponse.getApplicant(previousVoeOtLoanDoc.getBorrowerFullName()).getIncome(previousVoeOtLoanDoc.getBorrowerCurrentEmployment()).getOvertime().getSelected(), "Overtime was deselected after deselecting previous job two year prior");

            selectIncome(primaryGroupId, currentJobOvertime.getIncome(ONE_YEAR_PRIOR).getId(), false);
            assertTrue(
                    getResponse.getApplicant(previousVoeOtLoanDoc.getBorrowerFullName()).getIncome(previousVoeOtLoanDoc.getBorrowerCurrentEmployment()).getOvertime().getSelected(), "Overtime was deselected after deselecting current job one year prior");
        });
    }

    @Disabled("Needs another current job to check all cases, can be modified for previous job or year but not all incomes can be tested that way PROBABLY second option")
    @Test
    @Description("IA-1880 Check If Changing Document And Income Type Changes Selection In All Annual Summary")
    void checkIfChangingDocumentAndIncomeTypeChangesSelectionInAllAnnualSummary() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                        "12/31/" + TWO_YEARS_PRIOR,
                        "12/01/" + (THREE_YEARS_PRIOR - 2),
                        new PaystubData.PaystubIncomeRow(REGULAR, "15.00", "40.00", "500.00", "1200.00")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                        HOURLY,
                        "01/01/" + (THREE_YEARS_PRIOR - 2),
                        "01/31/" + TWO_YEARS_PRIOR,
                        "24.00",
                        "1000.00",
                        "01/31/" + TWO_YEARS_PRIOR)
                        .setAvgHoursPerWeek("40"))
                .importDocumentList();

        RestPartIncome yearToDateIncome = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId())
                .getApplicant(folderBuilderStoryTest.getBorrowerFullName()).getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE);

        compareSelectionsForAnnualSummaryAndAllAnnualSummary(yearToDateIncome);

        selectJobAnnualSummary(yearToDateIncome.getId(), yearToDateIncome.getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getDocIds().get(0), AnnualSummaryIncomeType.CALCULATED);
        compareSelectionsForAnnualSummaryAndAllAnnualSummary(yearToDateIncome);

        selectJobAnnualSummary(yearToDateIncome.getId(), yearToDateIncome.getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getDocIds().get(0), AnnualSummaryIncomeType.AVG);
        compareSelectionsForAnnualSummaryAndAllAnnualSummary(yearToDateIncome);

        selectJobAnnualSummary(yearToDateIncome.getId(), yearToDateIncome.getAnnualSummaryDocument(SummaryDocumentType.VOE).getDocIds().get(0), AnnualSummaryIncomeType.CALCULATED);
        compareSelectionsForAnnualSummaryAndAllAnnualSummary(yearToDateIncome);

        selectJobAnnualSummary(yearToDateIncome.getId(), yearToDateIncome.getAnnualSummaryDocument(SummaryDocumentType.VOE).getDocIds().get(0), AnnualSummaryIncomeType.AVG);
        compareSelectionsForAnnualSummaryAndAllAnnualSummary(yearToDateIncome);
    }

    @Test
    @EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
    @Description("IA-2554 IA-2643 YTD base defaults with past jobs to include only Actual Avg")
    void checkIfBaseDefaultsWithPastJobsToIncludeOnlyActualAvg() {
        IAFolderBuilder baseDefaultsLoanDoc = createFolderBuilder("IARestDflts");
        baseDefaultsLoanDoc.generateSecondaryJobsLoanDocument()
                .restBuild();

        DataUploadObject dataUpload = createUploadObject(baseDefaultsLoanDoc, dvFolderClient);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        baseDefaultsLoanDoc.getBorrowerFullName(),
                        baseDefaultsLoanDoc.getBorrowerSSN(),
                        baseDefaultsLoanDoc.getBorrowerCollaboratorId(),
                        baseDefaultsLoanDoc.getBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        baseDefaultsLoanDoc.getBorrowerFullName(),
                        baseDefaultsLoanDoc.getBorrowerSSN(),
                        baseDefaultsLoanDoc.getBorrowerCollaboratorId(),
                        baseDefaultsLoanDoc.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        baseDefaultsLoanDoc.getBorrowerFullName(),
                        baseDefaultsLoanDoc.getBorrowerSSN(),
                        baseDefaultsLoanDoc.getBorrowerCollaboratorId(),
                        baseDefaultsLoanDoc.getBorrowerSecondEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", "1000")))
                .importDocumentList();
        final RestGetResponse[] response = {RestGetLoanData.getApplicationData(baseDefaultsLoanDoc.getFolderId())};

        String primaryJobId = Long.toString(response[0]
                .getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                .getIncome(baseDefaultsLoanDoc.getBorrowerCurrentEmployment()).getId());

        String primaryGroupId = response[0]
                .getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                .getIncomeCategoryW2().getPrimaryIncomeGroup().getId();

        RestIncomePart currentBasePay = response[0].getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                .getIncome(baseDefaultsLoanDoc.getBorrowerCurrentEmployment())
                .getBasePay();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectIncome(primaryJobId, response[0]
                    .getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                    .getIncome(baseDefaultsLoanDoc.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getId(), true);

            selectJobAnnualSummary(currentBasePay.getIncome(YEAR_TO_DATE).getId(), currentBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0), AVG);

            selectIncome(primaryJobId, response[0]
                    .getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                    .getIncome(baseDefaultsLoanDoc.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE).getId(), true);

            response[0] = RestGetLoanData.getApplicationData(baseDefaultsLoanDoc.getFolderId());

            assertAll("Check if ytd BasePay values are selected correctly",
                    () -> assertTrue(response[0].getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                            .getIncome(baseDefaultsLoanDoc.getBorrowerPreviousEmployment()).getBasePay().getSelected(), "Previous employment could not be selected"),
                    () -> assertFalse(RestGetLoanData.getApplicationData(baseDefaultsLoanDoc.getFolderId())
                            .getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                            .getIncome(baseDefaultsLoanDoc.getBorrowerPreviousEmployment())
                            .getBasePay().getIncome(YEAR_TO_DATE)
                            .isIdInDisabledIdList(primaryGroupId), "Previous employment checkbox can be selected")
            );
        });

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            selectJobAnnualSummary(currentBasePay.getIncome(YEAR_TO_DATE).getId(), currentBasePay.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0), CALCULATED);

            assertTrue(RestGetLoanData.getApplicationData(baseDefaultsLoanDoc.getFolderId())
                    .getApplicant(baseDefaultsLoanDoc.getBorrowerFullName())
                    .getIncome(baseDefaultsLoanDoc.getBorrowerPreviousEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE)
                    .isIdInDisabledIdList(primaryGroupId), "Previous employment cannot be selected");
        });
    }

    void compareSelectionsForAnnualSummaryAndAllAnnualSummary(RestPartIncome yearlyIncome) {
        List<RestPartIncomeAnnualSummary> annualSummary = yearlyIncome.getAnnualSummary();
        annualSummary.forEach(document -> {

            Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
                RestPartIncomeAnnualSummary ytdDoc = yearlyIncome.getAllAnnualSummaryDocument(document.getDocIds().get(0));
                assertAll(String.format("Asserting that selection for documents in annualSummary and allAnnualSummary is the same for folder: %s", folderBuilderStoryTest.getFolderId()),
                        () -> assertEquals(document.getSelected(), ytdDoc.getSelected(),
                                String.format("AllAnnualSummary document %s does not have same selection: %s instead of %s",
                                        document.getDocIds().get(0), ytdDoc.getSelected(), document.getSelected())),
                        () -> assertEquals(document.getMonthlyAmountCalculated().getSelected(), ytdDoc.getMonthlyAmountCalculated().getSelected(),
                                String.format("AllAnnualSummary calculated income for document %s does not have same selection: %s instead of %s",
                                        document.getDocIds().get(0), ytdDoc.getMonthlyAmountCalculated().getSelected(), document.getMonthlyAmountCalculated().getSelected())),
                        () -> assertEquals(document.getMonthlyAmountAvg().getSelected(), ytdDoc.getMonthlyAmountAvg().getSelected(),
                                String.format("AllAnnualSummary avg income for document %s does not have same selection: %s instead of %s",
                                        document.getDocIds().get(0), ytdDoc.getMonthlyAmountAvg().getSelected(), document.getMonthlyAmountAvg().getSelected()))
                );
            });
        });
    }

    void checkHourlyVoeDisplayForUnknownHoursOrRate(Boolean hoursIsUnknown) {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderStoryTest.getBorrowerFullName(),
                        folderBuilderStoryTest.getBorrowerSSN(),
                        folderBuilderStoryTest.getBorrowerCollaboratorId(),
                        folderBuilderStoryTest.getBorrowerCurrentEmployment(),
                        HOURLY,
                        "01/01/" + ONE_YEAR_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        hoursIsUnknown ? "6.25" : "",
                        "1000.00",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("2000.00")
                        .setAvgHoursPerWeek(hoursIsUnknown ? "" : "160"));


        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId());

            assertAll(String.format("Check YTD Avg default values for hourly Voe with unknown %s on folder: %s", hoursIsUnknown ? "hours" : "rate", folderBuilderStoryTest.getFolderId()),
                    () -> assertNotNull(response
                                    .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                    .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                    .getBasePay()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.VOE)
                                    .getMonthlyAmountAvg().getValue(),
                            "Ytd Avg income value is null"),
                    () -> assertNull(response
                                    .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                    .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                    .getBasePay()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.VOE)
                                    .getMonthlyAmountCalculated().getValue(),
                            "Calculated monthly income is not null"),
                    () -> assertNotNull(response
                                    .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                    .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                    .getBasePay()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.VOE)
                                    .getMonths().getValue(),
                            "Months worked is null")
            );
        });
    }

    void checkMoWorkedFor2018PrimaryBorrower(BigDecimal currentJobMonths, BigDecimal previousJobMonths) {

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderStoryTest.getFolderId());
            BigDecimal currentJob = currentJobMonths.setScale(BIG_DECIMAL_PRECISION_TWO_POINTS, RoundingMode.HALF_UP);
            BigDecimal previousJob = previousJobMonths.setScale(BIG_DECIMAL_PRECISION_TWO_POINTS, RoundingMode.HALF_UP);
            assertAll("Check months worked values for unknown frequency paystub on folder: " + folderBuilderStoryTest.getFolderId(),
                    () -> {
                        BigDecimal actual = response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                .getBasePay()
                                .getIncome(ONE_YEAR_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                                .getMonths().getValue();
                        assertEquals(BIG_DECIMAL_COMPARE_EQUAL, currentJob.setScale(BIG_DECIMAL_PRECISION_TWO_POINTS, RoundingMode.HALF_UP).compareTo(actual),
                                String.format("Paystub current job months worked not equal expected: %s actual: %s", currentJob, actual));
                    },
                    () -> {
                        BigDecimal actual = response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerCurrentEmployment())
                                .getBasePay()
                                .getIncome(ONE_YEAR_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.W2)
                                .getMonths().getValue();
                        assertEquals(BIG_DECIMAL_COMPARE_EQUAL, currentJob.compareTo(actual),
                                String.format("W2 current job months worked not equal expected: %s actual: %s", currentJob, actual));
                    },
                    () -> {
                        BigDecimal actual = response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerPreviousEmployment())
                                .getBasePay()
                                .getIncome(ONE_YEAR_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                                .getMonths().getValue();
                        assertEquals(BIG_DECIMAL_COMPARE_EQUAL, previousJob.compareTo(actual),
                                String.format("Paystub previous job months worked not equal expected: %s actual: %s", previousJob, actual));
                    },
                    () -> {
                        BigDecimal actual = response
                                .getApplicant(folderBuilderStoryTest.getBorrowerFullName())
                                .getIncome(folderBuilderStoryTest.getBorrowerPreviousEmployment())
                                .getBasePay()
                                .getIncome(ONE_YEAR_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.W2)
                                .getMonths().getValue();
                        assertEquals(BIG_DECIMAL_COMPARE_EQUAL, previousJob.compareTo(actual),
                                String.format("W2 previous job months not equal expected: %s actual: %s", previousJob, actual));
                    }
            );
        });
    }
}

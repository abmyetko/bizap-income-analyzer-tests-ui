package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.data.upload.data.VoeCurrentData;
import com.capsilon.incomeanalyzer.automation.data.upload.data.VoePreviousData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectIncome;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.MONTHLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType.BASE_PAY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static java.math.RoundingMode.HALF_UP;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "TrendingTest")
class TrendingTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderTrendTest = createFolderBuilder("IARestTrend");

    @BeforeAll
    void setupTestFolder() {
        folderBuilderTrendTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderTrendTest);
        //Line used to break test execution in case of changed API response
        RestGetLoanData.getApplicationData(folderBuilderTrendTest.getFolderId());
    }

    private Stream singleJobTestCases() {
        return Stream.of(
                of(BASE_PAY, MONTHLY, new BigDecimal[]{
                                new BigDecimal(6000),
                                new BigDecimal(15000),
                                new BigDecimal(13000)},
                        "03/01/" + THREE_YEARS_PRIOR, "06/30/" + YEAR_TO_DATE, true, new BigDecimal(-20), new BigDecimal(15.38))/*,
                of(COMMISSIONS, MONTHLY, new BigDecimal[]{
                                new BigDecimal(1500),
                                new BigDecimal(2400),
                                new BigDecimal(2000)},
                        "01/01/" + FOUR_YEARS_PRIOR, "06/30/" + YEAR_TO_DATE, true, new BigDecimal(25), new BigDecimal(20)),
                of(OVERTIME, MONTHLY, new BigDecimal[]{
                                new BigDecimal(1500),
                                new BigDecimal(2400),
                                new BigDecimal(2000)},
                        "01/01/" + FOUR_YEARS_PRIOR, "06/30/" + YEAR_TO_DATE, true, new BigDecimal(25), new BigDecimal(20))*/
        );
    }

    private Stream doubleJobTestCases() {
        return Stream.of(
                of(BASE_PAY,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(6000),
                                new BigDecimal(0),
                                new BigDecimal(0)},
                        "03/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        true,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(4000.84)},
                        "07/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        false,
                        new BigDecimal(-50.01), new BigDecimal(0)),
                of(BASE_PAY,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(0),
                                new BigDecimal(0),
                                new BigDecimal(0)},
                        "03/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        true,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(4000.84)},
                        "07/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        false,
                        new BigDecimal(0), new BigDecimal(0))/*,
                of(COMMISSIONS,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(300),
                                new BigDecimal(0),
                                new BigDecimal(0)},
                        "03/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        true,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(600),
                                new BigDecimal(500)},
                        "01/01/" + TWO_YEAR_PRIOR,
                        "12/31/" + PREVIOUS_YEAR,
                        false,
                        new BigDecimal(-83.33), new BigDecimal(20)),
                of(OVERTIME,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(300),
                                new BigDecimal(0),
                                new BigDecimal(0)},
                        "03/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        true,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(600),
                                new BigDecimal(500)},
                        "01/01/" + TWO_YEAR_PRIOR,
                        "12/31/" + PREVIOUS_YEAR,
                        false,
                        new BigDecimal(-83.33), new BigDecimal(20)),
                of(COMMISSIONS,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(0),
                                new BigDecimal(0),
                                new BigDecimal(0)},
                        "03/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        true,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(600),
                                new BigDecimal(500)},
                        "01/01/" + TWO_YEAR_PRIOR,
                        "12/31/" + PREVIOUS_YEAR,
                        false,
                        new BigDecimal(0), new BigDecimal(20)),
                of(OVERTIME,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(0),
                                new BigDecimal(0),
                                new BigDecimal(0)},
                        "03/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        true,
                        MONTHLY,
                        new BigDecimal[]{
                                new BigDecimal(600),
                                new BigDecimal(500)},
                        "01/01/" + TWO_YEAR_PRIOR,
                        "12/31/" + PREVIOUS_YEAR,
                        false,
                        new BigDecimal(0), new BigDecimal(20))*/
        );
    }

    @BeforeEach
    void unselectPreviousJobs() {
        dataUpload.removeDocumentsFromFolder();
        RestGetLoanData.getApplicationData(folderBuilderTrendTest.getFolderId()).getApplicants()
                .forEach(applicant -> {
                    String primaryGroupId = Long.toString(applicant.getPrimaryIncome().getId());
                    applicant.getPreviousIncomes()
                            .forEach(prevIncome -> prevIncome.getParts()
                                    .forEach(part -> part.getIncomes()
                                            .forEach(year -> {
                                                if (year.getSelected() != null && year.getSelected()) {
                                                    selectIncome(primaryGroupId, year.getId(), false);
                                                }
                                            })));
                });
    }

    @ParameterizedTest(name = "{index} Trending calculations for one job {0} frequency {1} incomes {2} expected trending: {6}% {7}%")
    @MethodSource("singleJobTestCases")
    void checkOneJobTrendingCalculations(IncomePartType incomePartType, IncomeFrequency frequency, BigDecimal[] primaryJobIncomes, String startDate, String endDate, Boolean isJobCurrent,
                                         BigDecimal predictedYTDTrending, BigDecimal predictedPrevYrTrending) {

        DocumentProperties voeDocumentProperties = new DocumentProperties(incomePartType, frequency, primaryJobIncomes, startDate, endDate, isJobCurrent, true);
        generateAndUploadDocuments(voeDocumentProperties);

        selectIncomes(incomePartType,
                new String[]{folderBuilderTrendTest.getBorrowerFullName(), folderBuilderTrendTest.getBorrowerCurrentEmployment()}
        );

        checkTrending(incomePartType, predictedYTDTrending, predictedPrevYrTrending, true);
    }

    @ParameterizedTest(name = "{index} Trending calculations for two jobs {0} frequency {1} incomes {2} expected trending: {11}% {12}%")
    @MethodSource("doubleJobTestCases")
    void checkTwoJobsTrendingCalculations(IncomePartType incomePartType,
                                          IncomeFrequency firstJobFrequency, BigDecimal[] firstJobIncomes, String firstStartDate, String firstEndDate, Boolean firstJobIsCurrent,
                                          IncomeFrequency secondJobFrequency, BigDecimal[] secondJobIncomes, String secondStartDate, String secondEndDate, Boolean secondJobIsCurrent,
                                          BigDecimal predictedYTDTrending, BigDecimal predictedPrevYrTrending) {

        DocumentProperties firstJobProperties = new DocumentProperties(incomePartType, firstJobFrequency, firstJobIncomes, firstStartDate, firstEndDate, firstJobIsCurrent, true);
        DocumentProperties secondJobProperties = new DocumentProperties(incomePartType, secondJobFrequency, secondJobIncomes, secondStartDate, secondEndDate, secondJobIsCurrent, true);
        generateAndUploadDocuments(firstJobProperties, secondJobProperties);

        String primaryGroupId = Long.toString(RestGetLoanData.getApplicationData(folderBuilderTrendTest.getFolderId())
                .getApplicant(folderBuilderTrendTest.getBorrowerFullName())
                .getIncome(folderBuilderTrendTest.getBorrowerCurrentEmployment()).getId());

        selectIncomes(incomePartType,
                new String[]{folderBuilderTrendTest.getBorrowerFullName(), folderBuilderTrendTest.getBorrowerCurrentEmployment(), primaryGroupId},
                new String[]{folderBuilderTrendTest.getBorrowerFullName(), folderBuilderTrendTest.getBorrowerPreviousEmployment(), primaryGroupId}
        );

        checkTrending(incomePartType, predictedYTDTrending, predictedPrevYrTrending, true);
    }

    void selectIncomes(IncomePartType incomePartType, String[]... employments) {
        for (String[] employment :
                employments) {
            RestGetLoanData.getApplicationData(folderBuilderTrendTest.getFolderId())
                    .getApplicant(employment[0])
                    .selectAllIncomeParts(incomePartType, employment[1])
                    .forEach(year -> selectIncome(employment[2], year.getId(), true));
        }
    }

    List<BigDecimal> getNewTimeOnJob(String periodEndDate, String jobStartDate) {
        String[] endDateParts = periodEndDate.split("/");
        String[] startDateParts = jobStartDate.split("/");
        return getNewTimeOnJob(Integer.parseInt(endDateParts[0]),
                Integer.parseInt(endDateParts[1]),
                Integer.parseInt(endDateParts[2]),
                Integer.parseInt(startDateParts[0]),
                Integer.parseInt(startDateParts[1]),
                Integer.parseInt(startDateParts[2]));
    }

    List<BigDecimal> getNewTimeOnJob(int periodEndYear, int periodEndMonth, int periodEndDay, int startDateYear, int startDateMonth, int startDateDay) {
        YearMonth periodEndDayCount = YearMonth.of(periodEndYear, periodEndMonth);
        if (startDateDay == 0)
            startDateDay = 1;

        int calculatedTimeOnJobYear;
        int calculatedTimeOnJobMonth;
        if (periodEndYear == startDateYear) {
            calculatedTimeOnJobYear = 0;
            calculatedTimeOnJobMonth = periodEndMonth - startDateMonth;
        } else {
            calculatedTimeOnJobYear = periodEndYear - startDateYear - 1;
            calculatedTimeOnJobMonth = Math.abs(startDateMonth - 12) + periodEndMonth;
            if (calculatedTimeOnJobMonth >= 12) {
                calculatedTimeOnJobYear++;
                calculatedTimeOnJobMonth -= 12;
            }
        }

        BigDecimal decimalPeriodEndDay = new BigDecimal(periodEndDay);
        BigDecimal decimalStartDateDay = new BigDecimal(startDateDay);
        BigDecimal daysInMonth = new BigDecimal(periodEndDayCount.lengthOfMonth());
        BigDecimal percentageOfMonth = (decimalPeriodEndDay
                .subtract(decimalStartDateDay).abs()
                .add(new BigDecimal(1))
                .divide(daysInMonth, 2, RoundingMode.HALF_UP))
                .remainder(new BigDecimal(1));
        if (percentageOfMonth.movePointRight(2).setScale(0, RoundingMode.HALF_UP).remainder(new BigDecimal(10)).intValue() == 0) {
            percentageOfMonth = percentageOfMonth.setScale(1, RoundingMode.HALF_UP);
        }

        return new ArrayList<>(Arrays.asList(new BigDecimal(calculatedTimeOnJobYear).setScale(0, RoundingMode.HALF_UP),
                new BigDecimal(calculatedTimeOnJobMonth).setScale(0, RoundingMode.HALF_UP),
                percentageOfMonth));
    }

    List<Integer> getStartDate(int signDateYear, int signDateMonth, int timeOnJobYearCount, int timeOnJobMonthCount) {
        if (signDateYear == 0)
            signDateYear = YEAR_TO_DATE;
        if (signDateMonth == 0)
            signDateMonth = 4;

        int calculatedJobStartYear = signDateYear - timeOnJobYearCount;
        int calculatedJobStartMonth = Math.abs(signDateMonth - timeOnJobMonthCount);
        if (signDateMonth - timeOnJobMonthCount <= 0) {
            calculatedJobStartYear--;
            calculatedJobStartMonth = 12 - calculatedJobStartMonth;
        }

        return new ArrayList<>(Arrays.asList(calculatedJobStartYear, calculatedJobStartMonth));
    }

    void checkTrending(IncomePartType incomePartType, BigDecimal predictedYTDTrending, BigDecimal predictedPrevYrTrending, Boolean primaryApplicant) {
        BigDecimal expectedCurrYrTrending = predictedYTDTrending.setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedPrevYrTrending = predictedPrevYrTrending.setScale(2, RoundingMode.HALF_UP);

        RestIncomeType summaryIncomeType = RestGetLoanData.getApplicationData(folderBuilderTrendTest.getFolderId())
                .getApplicant(primaryApplicant ? folderBuilderTrendTest.getBorrowerFullName() : folderBuilderTrendTest.getCoBorrowerFullName())
                .getIncomeCategoryW2()
                .getPrimaryIncomeGroup()
                .getIncomeTypes().get(incomePartType.toString());


        assertAll("Trending calculations",
                () -> Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
                    BigDecimal actualTrending = summaryIncomeType.getTotalSelectedIncome(YEAR_TO_DATE).getTrending();
                    if (expectedCurrYrTrending.compareTo(new BigDecimal(0)) != 0) {
                        assertEquals(expectedCurrYrTrending, actualTrending.setScale(2, RoundingMode.HALF_UP),
                                String.format("Expected %s Found %s", expectedCurrYrTrending, actualTrending));
                    } else {
                        if (actualTrending != null) {
                            assertEquals(0, actualTrending.compareTo(new BigDecimal(0)),
                                    String.format("Predicted trending 0.00, but found %s", actualTrending));
                        }
                    }
                }),
                () -> Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
                    BigDecimal actualTrending = summaryIncomeType.getTotalSelectedIncome(ONE_YEAR_PRIOR).getTrending();
                    if (expectedPrevYrTrending.compareTo(new BigDecimal(0)) != 0) {
                        assertEquals(expectedPrevYrTrending, actualTrending.setScale(2, RoundingMode.HALF_UP),
                                String.format("Expected %s Found %s", expectedPrevYrTrending, actualTrending));
                    } else {
                        if (actualTrending != null) {
                            assertEquals(0, actualTrending.compareTo(new BigDecimal(0)),
                                    String.format("Predicted trending 0.00, but found %s", actualTrending));
                        }
                    }
                })
        );
    }

    void generateAndUploadDocuments(DocumentProperties... jobIncomes) {
        for (DocumentProperties property : jobIncomes) {
            property.uploadDocsToFolder();
        }
    }

    @SuppressWarnings("unchecked")
    public final class DocumentProperties {
        private final String startDate;
        private final String endDate;
        private final IncomePartType incomePartType;
        private final Boolean isCurrent;
        private final IncomeFrequency incomeFrequency;
        private final BigDecimal[] incomeValues;
        private final Boolean primaryBorrower;

        DocumentProperties(IncomePartType incomePartType, IncomeFrequency incomeFrequency, BigDecimal[] incomeValues, String startDate, String endDate, Boolean isCurrent, Boolean primaryBorrower) {
            this.incomeValues = incomeValues;
            this.startDate = startDate;
            this.endDate = endDate;
            this.incomePartType = incomePartType;
            this.isCurrent = isCurrent;
            this.incomeFrequency = incomeFrequency;
            this.primaryBorrower = primaryBorrower;
            Arrays.stream(this.incomeValues).forEach(income -> income = income.setScale(2, RoundingMode.HALF_UP));
        }

        public void uploadDocsToFolder() {
            String name;
            String ssn;
            String collaboratorId;
            String employment;

            if (primaryBorrower) {
                name = folderBuilderTrendTest.getBorrowerFullName();
                ssn = folderBuilderTrendTest.getBorrowerSSN();
                collaboratorId = folderBuilderTrendTest.getBorrowerCollaboratorId();
                if (isCurrent)
                    employment = folderBuilderTrendTest.getBorrowerCurrentEmployment();
                else
                    employment = folderBuilderTrendTest.getBorrowerPreviousEmployment();
            } else {
                name = folderBuilderTrendTest.getCoBorrowerFullName();
                ssn = folderBuilderTrendTest.getCoBorrowerSSN();
                collaboratorId = folderBuilderTrendTest.getCoBorrowerCollaboratorId();
                if (isCurrent)
                    employment = folderBuilderTrendTest.getCoBorrowerCurrentEmployment();
                else
                    employment = folderBuilderTrendTest.getCoBorrowerPreviousEmployment();
            }

            dataUpload.clearDocuments();

            if (isCurrent) {
                dataUpload.addDocument((dataUpload.createCustomVoeCurrent(
                        name,
                        ssn,
                        collaboratorId,
                        employment,
                        incomeFrequency,
                        startDate,
                        endDate,
                        "00.00",
                        "00.00",
                        endDate))
                        .setYtdBasePay("")
                        .setCurrentGrossBasePayAmount("")
                        .setExecutionDate("03/31/2020"));
                switch (incomePartType) {
                    case BASE_PAY:
                        ((List<VoeCurrentData>) dataUpload.getDocumentList()).get(0)
                                .setYtdBasePay(incomeValues[0].toString())
                                .setCurrentGrossBasePayAmount(incomeValues[0].divide(
                                        getTimeOnJob(
                                                Integer.parseInt(endDate.substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END)),
                                                Integer.parseInt(endDate.substring(MONTH_SUBSTRING_INDEX_START, MONTH_SUBSTRING_INDEX_END)),
                                                Integer.parseInt(endDate.substring(DAY_SUBSTRING_INDEX_START, DAY_SUBSTRING_INDEX_END))
                                        ), HALF_UP).toString())
                                .setPriorYearBasePay(incomeValues[1].toString())
                                .setTwoYearPriorBasePay(incomeValues[2].toString());
                        break;
                    case BONUS:
                        ((List<VoeCurrentData>) dataUpload.getDocumentList()).get(0)
                                .setYtdBonus(incomeValues[0].toString())
                                .setPriorYearBonus(incomeValues[1].toString())
                                .setTwoYearPriorBonus(incomeValues[2].toString());
                        break;
                    case OVERTIME:
                        ((List<VoeCurrentData>) dataUpload.getDocumentList()).get(0)
                                .setYtdOvertime(incomeValues[0].toString())
                                .setPriorYearOvertime(incomeValues[1].toString())
                                .setTwoYearPriorOvertime(incomeValues[2].toString());
                        break;
                    case COMMISSIONS:
                        ((List<VoeCurrentData>) dataUpload.getDocumentList()).get(0)
                                .setYtdCommission(incomeValues[0].toString())
                                .setPriorYearCommission(incomeValues[1].toString())
                                .setTwoYearPriorCommission(incomeValues[2].toString());
                        break;
                    default:
                        break;
                }
            } else {
                if (incomeValues.length == 1) {
                    dataUpload.addDocument((dataUpload.createCustomVoePrevious(
                            name,
                            ssn,
                            collaboratorId,
                            employment,
                            incomeFrequency,
                            startDate,
                            endDate,
                            "00.00",
                            "00.00",
                            "00.00",
                            "00.00"))
                            .setBaseWageAmount("")
                            .setOvertimeWageAmount("")
                            .setCommissionWageAmount("")
                            .setBonusWageAmount(""));

                    switch (incomePartType) {
                        case BASE_PAY:
                            ((List<VoePreviousData>) dataUpload.getDocumentList())
                                    .get(0).setBaseWageAmount(incomeValues[0].setScale(2, HALF_UP).toString());
                            break;
                        case OVERTIME:
                            ((List<VoePreviousData>) dataUpload.getDocumentList())
                                    .get(0).setOvertimeWageAmount(incomeValues[0].setScale(2, HALF_UP).toString());
                            break;
                        case COMMISSIONS:
                            ((List<VoePreviousData>) dataUpload.getDocumentList())
                                    .get(0).setCommissionWageAmount(incomeValues[0].setScale(2, HALF_UP).toString());
                            break;
                        case BONUS:
                            ((List<VoePreviousData>) dataUpload.getDocumentList())
                                    .get(0).setBonusWageAmount(incomeValues[0].setScale(2, HALF_UP).toString());
                            break;
                        default:
                            break;
                    }
                } else {
                    int endYear = Integer.parseInt(endDate.substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END));
                    for (BigDecimal income : incomeValues) {
                        PaystubData paystubToModify = dataUpload.createCustomPaystub(
                                name,
                                ssn,
                                collaboratorId,
                                employment,
                                "",
                                "12/31/" + endYear,
                                (PaystubData.PaystubIncomeRow) null);

                        switch (incomePartType) {
                            case BASE_PAY:
                                paystubToModify.addIncome(
                                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", income.toString(),
                                                income.multiply(
                                                        getTimeOnJob(
                                                                Integer.parseInt(endDate.substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(MONTH_SUBSTRING_INDEX_START, MONTH_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(DAY_SUBSTRING_INDEX_START, DAY_SUBSTRING_INDEX_END))
                                                        )).toString()));
                                break;
                            case BONUS:
                                paystubToModify.addIncome(
                                        new PaystubData.PaystubIncomeRow(BONUS, "", "", income.toString(),
                                                income.multiply(
                                                        getTimeOnJob(
                                                                Integer.parseInt(endDate.substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(MONTH_SUBSTRING_INDEX_START, MONTH_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(DAY_SUBSTRING_INDEX_START, DAY_SUBSTRING_INDEX_END))
                                                        )).toString()));
                                break;
                            case OVERTIME:
                                paystubToModify.addIncome(
                                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", income.toString(),
                                                income.multiply(
                                                        getTimeOnJob(
                                                                Integer.parseInt(endDate.substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(MONTH_SUBSTRING_INDEX_START, MONTH_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(DAY_SUBSTRING_INDEX_START, DAY_SUBSTRING_INDEX_END))
                                                        )).toString()));
                                break;
                            case COMMISSIONS:
                                paystubToModify.addIncome(
                                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", income.toString(),
                                                income.multiply(
                                                        getTimeOnJob(
                                                                Integer.parseInt(endDate.substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(MONTH_SUBSTRING_INDEX_START, MONTH_SUBSTRING_INDEX_END)),
                                                                Integer.parseInt(endDate.substring(DAY_SUBSTRING_INDEX_START, DAY_SUBSTRING_INDEX_END))
                                                        )).toString()));
                                break;
                            default:
                                break;
                        }
                        dataUpload.addDocument(paystubToModify);
                        endYear--;
                    }
                }
            }
            dataUpload.importDocumentList();
        }

        BigDecimal getTimeOnJob(int signDateYear, int signDateMonth, int signDateDay) {
            return getTimeOnJob(signDateYear, signDateMonth, signDateDay, THREE_YEARS_PRIOR, 1, 1);
        }

        BigDecimal getTimeOnJob(int signDateYear, int signDateMonth, int signDateDay, int startDateYear, int startDateMonth, int startDateDay) {

            YearMonth yearMonth = YearMonth.of(signDateYear, signDateMonth);
            BigDecimal decimalSignDay = new BigDecimal(signDateDay);
            BigDecimal daysInMonth = new BigDecimal(yearMonth.lengthOfMonth());
            BigDecimal percentageOfMonth = new BigDecimal(0);

            if (signDateYear != startDateYear) {
                percentageOfMonth = decimalSignDay.divide(daysInMonth, 2, RoundingMode.HALF_UP);
                signDateMonth--;
            } else if (startDateDay == 1) {
                percentageOfMonth = decimalSignDay.divide(daysInMonth, 2, RoundingMode.HALF_UP);
                signDateMonth = -(startDateMonth - 1) - 1;
            }

            return new BigDecimal(signDateMonth).add(percentageOfMonth).setScale(2, RoundingMode.HALF_UP);
        }
    }
}

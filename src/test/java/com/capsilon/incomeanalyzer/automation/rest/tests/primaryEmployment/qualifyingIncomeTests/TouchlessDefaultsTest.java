package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.data.upload.data.VoeCurrentData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "TouchlessDefaultsTest")
class TouchlessDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderDefTest = createFolderBuilder("IARestDef");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderDefTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderDefTest);
        //Line used to break test execution in case of changed API response
        getResponse = RestGetLoanData.getApplicationData(folderBuilderDefTest.getFolderId());
    }

    private Stream voeTestCases() {
        return Stream.of(
                of(IncomePartType.BASE_PAY, new BigDecimal(2000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.BASEPAY_YTD_AVG),
                of(IncomePartType.BASE_PAY, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG),
                of(IncomePartType.BASE_PAY, new BigDecimal(500), new BigDecimal(1100), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG)/*,
                of(IncomeType.OVERTIME, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.OVERTIME, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG),
                of(IncomeType.OVERTIME, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.OVERTIME, new BigDecimal(1000), new BigDecimal(6000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_YR),
                of(IncomeType.BONUS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.BONUS, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG),
                of(IncomeType.BONUS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.BONUS, new BigDecimal(1000), new BigDecimal(6000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_YR),
                of(IncomeType.COMMISSIONS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.COMMISSIONS, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG),
                of(IncomeType.COMMISSIONS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.COMMISSIONS, new BigDecimal(1000), new BigDecimal(6000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_YR)*/
        );
    }

    private Stream paystubTestCases() {
        return Stream.of(
                of(IncomePartType.BASE_PAY, new BigDecimal(2000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.BASEPAY_YTD_AVG),
                of(IncomePartType.BASE_PAY, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG),
                of(IncomePartType.BASE_PAY, new BigDecimal(500), new BigDecimal(1100), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG)/*,
                of(IncomeType.OVERTIME, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.OVERTIME, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG),
                of(IncomeType.OVERTIME, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.OVERTIME, new BigDecimal(1000), new BigDecimal(6000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_YR),
                of(IncomeType.BONUS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.BONUS, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG),
                of(IncomeType.BONUS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.BONUS, new BigDecimal(1000), new BigDecimal(6000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_YR),
                of(IncomeType.COMMISSIONS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.COMMISSIONS, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.YTD_AVG),
                of(IncomeType.COMMISSIONS, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.YTD_AVG_PLUS_PREV_TWO_YRS),
                of(IncomeType.COMMISSIONS, new BigDecimal(1000), new BigDecimal(6000), new BigDecimal(12000), IncomeAvg.YTD_AVG_PLUS_PREV_YR)*/
        );
    }

    private Stream hourlyTestCases() {
        return Stream.of(
                of(VOE, IncomePartType.BASE_PAY, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG),
                of(VOE, IncomePartType.BASE_PAY, new BigDecimal(500), new BigDecimal(24000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG),
                of(VOE, IncomePartType.BASE_PAY, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(VOE, IncomePartType.BASE_PAY, new BigDecimal(2000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(PAYSTUB, IncomePartType.BASE_PAY, new BigDecimal(500), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG),
                of(PAYSTUB, IncomePartType.BASE_PAY, new BigDecimal(500), new BigDecimal(24000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG),
                of(PAYSTUB, IncomePartType.BASE_PAY, new BigDecimal(1000), new BigDecimal(12000), new BigDecimal(6000), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(PAYSTUB, IncomePartType.BASE_PAY, new BigDecimal(2000), new BigDecimal(12000), new BigDecimal(12000), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR)
        );
    }

    @ParameterizedTest(name = "{index} {0} hourly calc for {1} with YTD: {2} Previous year: {3} Two years prior: {4} expected {5}")
    @MethodSource("hourlyTestCases")
    void checkHourlyCalculations(SummaryDocumentType docType, IncomePartType incomePartType, BigDecimal incomeYTD, BigDecimal incomePrevYr, BigDecimal incomeTwoYrPrev, IncomeAvg predictedSel) {
        if (docType == VOE)
            importVoeAndCheckIfSelectionIsCorrect(incomePartType, incomeYTD, incomePrevYr, incomeTwoYrPrev, predictedSel, true);
        else if (docType == PAYSTUB)
            importPaystubAndCheckIfSelectionIsCorrect(incomePartType, incomeYTD, incomePrevYr, incomeTwoYrPrev, predictedSel, true);
    }

    @ParameterizedTest(name = "{index} Paystub calc for {0} with YTD: {1} Previous year: {2} Two years prior: {3} expected {4}")
    @MethodSource("paystubTestCases")
    void checkPaystubCalculations(IncomePartType incomePartType, BigDecimal incomeYTD, BigDecimal incomePrevYr, BigDecimal incomeTwoYrPrev, IncomeAvg predictedSel) {
        importPaystubAndCheckIfSelectionIsCorrect(incomePartType, incomeYTD, incomePrevYr, incomeTwoYrPrev, predictedSel, false);
    }

    @ParameterizedTest(name = "{index} Voe salaried calc for {0} with YTD: {1} Previous year: {2} Two years prior: {3} expected {4}")
    @MethodSource("voeTestCases")
    void checkVoeCalculations(IncomePartType incomePartType, BigDecimal incomeYTD, BigDecimal incomePrevYr, BigDecimal incomeTwoYrPrev, IncomeAvg predictedSel) {
        importVoeAndCheckIfSelectionIsCorrect(incomePartType, incomeYTD, incomePrevYr, incomeTwoYrPrev, predictedSel, false);
    }

    @Test
    @Description("IA-1366 Check If No Income Is Selected For Document If One Is Equal To Zero")
    void checkIfNoIncomeIsSelectedForDocumentIfOneIsEqualToZero() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        folderBuilderDefTest.getCoBorrowerFullName(),
                        folderBuilderDefTest.getCoBorrowerSSN(),
                        folderBuilderDefTest.getCoBorrowerCollaboratorId(),
                        folderBuilderDefTest.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "00.00")));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderDefTest.getFolderId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
                    getResponse = RestGetLoanData.getApplicationData(folderBuilderDefTest.getFolderId());
                    RestPartIncomeAnnualSummary paystubIncome = getResponse.getApplicant(folderBuilderDefTest.getCoBorrowerFullName())
                            .getIncome(folderBuilderDefTest.getCoBorrowerCurrentEmployment())
                            .getBasePay()
                            .getIncome(YEAR_TO_DATE)
                            .getAnnualSummaryDocument(PAYSTUB);
                    assertAll("Assert that no income is selected by default",
                            () -> assertFalse(paystubIncome.getMonthlyAmountAvg().getSelected()),
                            () -> assertFalse(paystubIncome.getMonthlyAmountCalculated().getSelected())
                    );
                }
        );
    }

    @Test
    @Description("IA-1326 Check If No Historical Average Is Selected If Ytd Has Zero Income")
    void checkIfNoHistoricalAverageIsSelectedIfYtdHasZeroIncome() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderDefTest.getCoBorrowerFullName(),
                        folderBuilderDefTest.getCoBorrowerSSN(),
                        folderBuilderDefTest.getCoBorrowerCollaboratorId(),
                        folderBuilderDefTest.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "00.00", "00.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderDefTest.getCoBorrowerFullName(),
                        folderBuilderDefTest.getCoBorrowerSSN(),
                        folderBuilderDefTest.getCoBorrowerCollaboratorId(),
                        folderBuilderDefTest.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "600.00")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderDefTest.getCoBorrowerFullName(),
                        folderBuilderDefTest.getCoBorrowerSSN(),
                        folderBuilderDefTest.getCoBorrowerCollaboratorId(),
                        folderBuilderDefTest.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "0.00",
                        "0.00",
                        "01/31/" + YEAR_TO_DATE))
                .importDocumentList();
        getResponse = RestGetLoanData.getApplicationData(folderBuilderDefTest.getFolderId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
                    getResponse = RestGetLoanData.getApplicationData(folderBuilderDefTest.getFolderId());
                    RestIncomeType incomeType = getResponse.getApplicant(folderBuilderDefTest.getCoBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypeBasePay();

                    assertAll("Assert that no historical average is selected by default",
                            () -> assertFalse(incomeType.getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected()),
                            () -> assertFalse(incomeType.getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected()),
                            () -> assertFalse(incomeType.getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
                    );
                }
        );
    }

    void importPaystubAndCheckIfSelectionIsCorrect(IncomePartType incomePartType, BigDecimal incomeYTD, BigDecimal incomePrevYr, BigDecimal incomeTwoYrPrev, IncomeAvg incomeAvg, Boolean isHourly) {
        PaystubData paystubYTD = dataUpload.createCustomPaystub(
                folderBuilderDefTest.getCoBorrowerFullName(),
                folderBuilderDefTest.getCoBorrowerSSN(),
                folderBuilderDefTest.getCoBorrowerCollaboratorId(),
                folderBuilderDefTest.getCoBorrowerCurrentEmployment(),
                "01/01/" + YEAR_TO_DATE,
                "01/31/" + YEAR_TO_DATE,
                (PaystubData.PaystubIncomeRow) null);
        PaystubData paystubPrior = dataUpload.createCustomPaystub(
                folderBuilderDefTest.getCoBorrowerFullName(),
                folderBuilderDefTest.getCoBorrowerSSN(),
                folderBuilderDefTest.getCoBorrowerCollaboratorId(),
                folderBuilderDefTest.getCoBorrowerCurrentEmployment(),
                "12/01/" + ONE_YEAR_PRIOR,
                "12/31/" + ONE_YEAR_PRIOR,
                (PaystubData.PaystubIncomeRow) null);
        PaystubData paystubTwoYrPrior = dataUpload.createCustomPaystub(
                folderBuilderDefTest.getCoBorrowerFullName(),
                folderBuilderDefTest.getCoBorrowerSSN(),
                folderBuilderDefTest.getCoBorrowerCollaboratorId(),
                folderBuilderDefTest.getCoBorrowerCurrentEmployment(),
                "12/01/" + TWO_YEARS_PRIOR,
                "12/31/" + TWO_YEARS_PRIOR,
                (PaystubData.PaystubIncomeRow) null);

        PaystubIncomeGroups incomeGroup;

        switch (incomePartType) {
            case BASE_PAY:
                incomeGroup = REGULAR;
                break;
            case BONUS:
                incomeGroup = BONUS;
                break;
            case OVERTIME:
                incomeGroup = OVERTIME;
                break;
            case COMMISSIONS:
                incomeGroup = COMMISSIONS;
                break;
            default:
                incomeGroup = null;
                break;
        }
        String periodAmount = "151900.00";

        paystubYTD.addIncome(new PaystubData.PaystubIncomeRow(incomeGroup, "", "", periodAmount, incomeYTD.toString()));
        paystubPrior.addIncome(new PaystubData.PaystubIncomeRow(incomeGroup, "", "", periodAmount, incomePrevYr.toString()));
        paystubTwoYrPrior.addIncome(new PaystubData.PaystubIncomeRow(incomeGroup, "", "", periodAmount, incomeTwoYrPrev.toString()));


        if (isHourly) {
            paystubYTD.getIncomes().get(0)
                    .setHours("40.00")
                    .setRate("15.00");
            paystubPrior.getIncomes().get(0)
                    .setHours("40.00")
                    .setRate("15.00");
            paystubTwoYrPrior.getIncomes().get(0)
                    .setHours("40.00")
                    .setRate("15.00");
        }

        BigDecimal incomeVal = incomeYTD.add(incomePrevYr).setScale(8, RoundingMode.HALF_UP);

        switch (incomeAvg) {
            case BASEPAY_YTD_AVG:
                incomeVal = incomeYTD;
                break;
            case BASEPAY_YTD_AVG_PLUS_PREV_YR:
                incomeVal = incomeVal.divide(new BigDecimal(13), RoundingMode.HALF_UP);
                break;
            case BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS:
                incomeVal = incomeVal.add(incomeTwoYrPrev)
                        .divide(new BigDecimal(25), RoundingMode.HALF_UP);
                break;
            default:
                break;
        }

        dataUpload.clearDocuments()
                .addDocument(paystubYTD)
                .addDocument(paystubPrior)
                .addDocument(paystubTwoYrPrior)
                .importDocumentList();
        checkCalculations(incomeAvg, incomePartType, incomeVal, false);
    }

    void importVoeAndCheckIfSelectionIsCorrect(IncomePartType incomePartType, BigDecimal incomeYTD, BigDecimal incomePrevYr, BigDecimal incomeTwoYrPrev, IncomeAvg incomeAvg, Boolean isBPHourly) {
        VoeCurrentData voe = dataUpload.createCustomVoeCurrent(
                folderBuilderDefTest.getBorrowerFullName(),
                folderBuilderDefTest.getBorrowerSSN(),
                folderBuilderDefTest.getBorrowerCollaboratorId(),
                folderBuilderDefTest.getBorrowerCurrentEmployment(),
                isBPHourly ? IncomeFrequency.HOURLY : IncomeFrequency.MONTHLY,
                "01/01/" + TWO_YEARS_PRIOR,
                "01/31/" + YEAR_TO_DATE,
                incomeYTD.toString(),
                incomeYTD.toString(),
                "01/31/" + YEAR_TO_DATE);

        if (isBPHourly)
            voe.setAvgHoursPerWeek("40");

        switch (incomePartType) {
            case BASE_PAY:
                voe.setCurrentGrossBasePayAmount(incomeYTD.toString())
                        .setYtdBasePay(incomeYTD.toString())
                        .setPriorYearBasePay(incomePrevYr.toString())
                        .setTwoYearPriorBasePay(incomeTwoYrPrev.toString());
                break;
            case OVERTIME:
                voe.setCurrentGrossBasePayAmount(incomeYTD.toString())
                        .setYtdOvertime(incomeYTD.toString())
                        .setPriorYearOvertime(incomePrevYr.toString())
                        .setTwoYearPriorOvertime(incomeTwoYrPrev.toString());
                break;
            case COMMISSIONS:
                voe.setCurrentGrossBasePayAmount(incomeYTD.toString())
                        .setYtdCommission(incomeYTD.toString())
                        .setPriorYearCommission(incomePrevYr.toString())
                        .setTwoYearPriorCommission(incomeTwoYrPrev.toString());
                break;
            case BONUS:
                voe.setCurrentGrossBasePayAmount(incomeYTD.toString())
                        .setYtdBonus(incomeYTD.toString())
                        .setPriorYearBonus(incomePrevYr.toString())
                        .setTwoYearPriorBonus(incomeTwoYrPrev.toString());
                break;
            default:
                break;
        }
        BigDecimal incomeVal = incomeYTD.add(incomePrevYr).setScale(8, RoundingMode.HALF_UP);

        switch (incomeAvg) {
            case BASEPAY_YTD_AVG:
                incomeVal = incomeYTD;
                break;
            case BASEPAY_YTD_AVG_PLUS_PREV_YR:
                incomeVal = incomeVal.divide(new BigDecimal(13), RoundingMode.HALF_UP);
                break;
            case BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS:
                incomeVal = incomeVal.add(incomeTwoYrPrev)
                        .divide(new BigDecimal(25), RoundingMode.HALF_UP);
                break;
            default:
                break;
        }

        dataUpload.importDocument(voe);
        checkCalculations(incomeAvg, incomePartType, incomeVal, true);
    }

    void checkCalculations(IncomeAvg incomeAvg, IncomePartType incomePartType, BigDecimal incomeVal, boolean checkPrimaryBorrower) {
        final RestIncomeType[] income = new RestIncomeType[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilderDefTest.getFolderId());
            income[0] = getResponse
                    .getApplicant(checkPrimaryBorrower ?
                            folderBuilderDefTest.getBorrowerFullName() :
                            folderBuilderDefTest.getCoBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypes().get(incomePartType.toString());
                }
        );

        if (incomePartType != IncomePartType.BONUS) {
            assertAll("calculation assertion",
                    () -> assertTrue(income[0].getAvgIncome(incomeAvg).getSelected()),
                    () -> assertEquals(incomeVal.setScale(2, RoundingMode.HALF_UP), income[0].getAvgIncome(incomeAvg).getAvgMonthlyIncome().setScale(2, RoundingMode.HALF_UP))
            );
        } else {
            assertAll("Bonus not selected assertion",
                    () -> assertFalse(income[0].getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG).getSelected()),
                    () -> assertFalse(income[0].getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(income[0].getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        }
    }
}

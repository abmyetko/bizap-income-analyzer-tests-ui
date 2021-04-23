package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.overtime;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.AVG;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.OVERTIME_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = OVERTIME_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "OvertimeDefaultsTest")
public class OvertimeDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderOvertimeDefaultsTest = createFolderBuilder("IAR-OT-Def");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderOvertimeDefaultsTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderOvertimeDefaultsTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "900")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "01/07/" + YEAR_TO_DATE,
                        "1500",
                        "80",
                        "0",
                        "00"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "12000")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        "1500",
                        "1000",
                        "0",
                        "00"))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId());
    }

    @Test
    @Order(0)
    @Description("IA-2127 Check If Current Job All Years In Overtime Are Selected By Default")
    void checkIfCurrentJobOtAllYearsAreSelectedByDefault() {
        RestIncomePart currentOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment())
                .getOvertime();
        currentOt.getIncomes().forEach(year -> assertTrue(year.getSelected(), String.format("Year: %s was not selected by default", year.getYear())));
    }

    @Test
    @Order(0)
    @Description("IA-2127 Check If Previous Job All Years In Overtime Are Deselected By Default")
    void checkIfPreviousJobOtAllYearsAreDeselectedByDefault() {
        RestIncomePart previousOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment())
                .getOvertime();
        previousOt.getIncomes().forEach(year -> assertFalse(year.getSelected(), String.format("Year: %s was selected by default", year.getYear())));
    }

    @Test
    @Order(0)
    @Description("IA-2131 Check If Default Selection For Year Is Min Of Values")
    void checkIfDefaultSelectionForYearIsMinOfValues() {
        RestIncomePart currentOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment())
                .getOvertime();
        assertAll("Asserting all yearly incomes have min value selected by default or doc precedence in case of equals",
                () -> assertTrue(currentOt.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(currentOt.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(currentOt.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertTrue(currentOt.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(currentOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertTrue(currentOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected())
        );
        RestIncomePart previousOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment())
                .getOvertime();
        assertAll("Asserting all yearly incomes have min value selected by default or doc precedence in case of equals",
                () -> assertTrue(previousOt.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected()),
                () -> assertFalse(previousOt.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected())
        );
    }

    @Test
    @Order(0)
    @Description("IA-2195 Check If Doc Precedence Works For Previous Job")
    void checkIfDocPrecedenceWorksForPreviousJob() {
        RestIncomePart previousOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getCoBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getCoBorrowerPreviousEmployment())
                .getOvertime();
        assertAll("Asserting that voe overtime income takes precedence for previous job overtime",
                () -> assertTrue(previousOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "VOE income was not selected"),
                () -> assertFalse(previousOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "Paystub income was selected")
        );
    }

    @Test
    @Order(0)
    @Description("IA-2129 Check If Default Historical Average Defaults To Min Correctly")
    void checkIfDefaultHistoricalAverageDefaultsToMinCorrectly() {
        selectIncomeType(getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName()).getId(),
                getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomePartType.OVERTIME, true);

        final RestIncomeType[] historicalAverages = new RestIncomeType[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeType(IncomePartType.OVERTIME);
            assertAll("Asserting that longest income is selected if all have same value",
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertTrue(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });

        RestIncomePart currentOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment())
                .getOvertime();
        selectJobAnnualSummary(currentOt.getIncome(TWO_YEARS_PRIOR).getId(), currentOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0), AVG);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeType(IncomePartType.OVERTIME);
            assertAll("Asserting lowest income is selected",
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG).getSelected()),
                    () -> assertTrue(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });

        RestIncomePart previousOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment())
                .getOvertime();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, previousOt.getIncome(ONE_YEAR_PRIOR).getId(), true);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeType(IncomePartType.OVERTIME);
            assertAll("Asserting lowest income is selected",
                    () -> assertTrue(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });
    }

    @Test
    @Order(1)
    @Description("IA-2989 Data from EVOE Previous Job Overtime is shown in every column")
    void checkIfEvoePreviousJobOvertimeIsShownInEveryColumn() {

        dataUpload.removeDocumentsFromFolder().clearDocuments()
                .importDocument(dataUpload.createCustomEvoePrevious(
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getCoBorrowerPreviousEmployment(),
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "1500",
                        "12000",
                        "02/28/" + YEAR_TO_DATE)
                        .setYtdOvertime("1111")
                        .setPriorYearOvertime("1112")
                        .setTwoYearPriorOvertime("1113"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestIncomePart getResponse = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId()).getApplicant(folderBuilderOvertimeDefaultsTest.getCoBorrowerFullName())
                    .getIncome(folderBuilderOvertimeDefaultsTest.getCoBorrowerPreviousEmployment()).getOvertime();

            RestPartIncomeAnnualSummary evoeYtd = getResponse.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(EVOE);
            RestPartIncomeAnnualSummary evoeOneYearPrior = getResponse.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(EVOE);
            RestPartIncomeAnnualSummary evoeTwoYearsPrior = getResponse.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(EVOE);

            assertAll("Overtime EVOE Previous Job correct values assertion",
                    () -> assertNull(evoeYtd.getFrequency().getValue()),
                    () -> assertEquals(bigD(1111.00), bigD(evoeYtd.getDisplayGrossPay().getValue())),
                    () -> assertEquals(bigD(92.58), bigD(evoeYtd.getMonthlyAmountAvg().getValue())),
                    () -> assertEquals(bigD(12.00), bigD(evoeYtd.getMonths().getValue())),
                    () -> assertEquals("12/31/" + YEAR_TO_DATE, evoeYtd.getPayPeriodEndDate().getValue()),
                    () -> assertNull(evoeOneYearPrior.getFrequency().getValue()),
                    () -> assertEquals(bigD(1112.00), bigD(evoeOneYearPrior.getDisplayGrossPay().getValue())),
                    () -> assertEquals(bigD(92.67), bigD(evoeOneYearPrior.getMonthlyAmountAvg().getValue())),
                    () -> assertEquals(bigD(12), bigD(evoeOneYearPrior.getMonths().getValue())),
                    () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, evoeOneYearPrior.getPayPeriodEndDate().getValue()),
                    () -> assertNull(evoeTwoYearsPrior.getFrequency().getValue()),
                    () -> assertEquals(bigD(1113.00), bigD(evoeTwoYearsPrior.getDisplayGrossPay().getValue())),
                    () -> assertEquals(bigD(92.75), bigD(evoeTwoYearsPrior.getMonthlyAmountAvg().getValue())),
                    () -> assertEquals(bigD(12), bigD(evoeTwoYearsPrior.getMonths().getValue())),
                    () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, evoeTwoYearsPrior.getPayPeriodEndDate().getValue()));
        });
    }
}

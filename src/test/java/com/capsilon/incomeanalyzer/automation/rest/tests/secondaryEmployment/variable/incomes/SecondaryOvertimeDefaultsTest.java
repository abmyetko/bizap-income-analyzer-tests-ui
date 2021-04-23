package com.capsilon.incomeanalyzer.automation.rest.tests.secondaryEmployment.variable.incomes;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = OVERTIME_TOGGLE)
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryOvertimeDefaultsTest")
public class SecondaryOvertimeDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderOvertimeDefaultsTest = createFolderBuilder("IARSecOtDf");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderOvertimeDefaultsTest.generateSecondaryJobsLoanDocument()
                .restBuild();
        dataUpload = createUploadObject(folderBuilderOvertimeDefaultsTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderOvertimeDefaultsTest.getBorrowerFullName(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSSN(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment(),
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
    @Description("IA-2591 Check If Secondary Job All Years In Overtime Are Selected By Default")
    void checkIfSecondaryJobOtAllYearsAreSelectedByDefault() {
        RestIncomePart secondaryOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment())
                .getOvertime();
        secondaryOt.getIncomes().forEach(year -> assertTrue(year.getSelected(), String.format("Year: %s was not selected by default", year.getYear())));
    }

    @Test
    @Order(0)
    @Description("IA-2591 Check If Previous Job All Years In Overtime Are Deselected By Default")
    void checkIfPreviousJobOtAllYearsAreDeselectedByDefault() {
        RestIncomePart previousOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerPreviousEmployment())
                .getOvertime();
        previousOt.getIncomes().forEach(year -> assertFalse(year.getSelected(), String.format("Year: %s was selected by default", year.getYear())));
    }

    @Test
    @Order(0)
    @Description("IA-2591 Check If Default Selection For Year Is Min Of Values")
    void checkIfDefaultSelectionForYearIsMinOfValues() {
        RestIncomePart secondaryOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment())
                .getOvertime();
        assertAll("Asserting all yearly incomes have min value selected by default or doc precedence in case of equals",
                () -> assertTrue(secondaryOt.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryOt.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryOt.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertTrue(secondaryOt.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertTrue(secondaryOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected())
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
    @Description("IA-2591 Check If Doc Precedence Works For Previous Job")
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
    @Description("IA-2591 Check If Default Historical Average Defaults To Min Correctly")
    void checkIfDefaultHistoricalAverageDefaultsToMinCorrectly() {
        selectIncomeType(getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName()).getId(),
                getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getId(),
                IncomePartType.OVERTIME, true);

        final RestIncomeType[] historicalAverages = new RestIncomeType[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.OVERTIME);
            assertAll("Asserting that longest income is selected if all have same value",
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertTrue(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });

        RestIncomePart secondaryOt = getResponse.getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment())
                .getOvertime();
        selectJobAnnualSummary(secondaryOt.getIncome(TWO_YEARS_PRIOR).getId(), secondaryOt.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0), AVG);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
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
                .getIncome(folderBuilderOvertimeDefaultsTest.getBorrowerSecondEmployment()).getId());

        selectIncome(primaryGroupId, previousOt.getIncome(ONE_YEAR_PRIOR).getId(), true);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderOvertimeDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderOvertimeDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.OVERTIME);
            assertAll("Asserting lowest income is selected",
                    () -> assertTrue(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });
    }
}

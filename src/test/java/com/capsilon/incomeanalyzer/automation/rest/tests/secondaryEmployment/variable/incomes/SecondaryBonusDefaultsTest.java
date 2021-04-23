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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.BONUS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = BONUS_TOGGLE)
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryBonusDefaultsTest")
public class SecondaryBonusDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderBonusDefaultsTest = createFolderBuilder("IARSecBnsDf");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderBonusDefaultsTest.generateSecondaryJobsLoanDocument()
                .restBuild();
        dataUpload = createUploadObject(folderBuilderBonusDefaultsTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdBonus("1200")
                        .setPriorYearBonus("1000")
                        .setTwoYearPriorBonus("1000"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "900")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderBonusDefaultsTest.getBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "01/07/" + YEAR_TO_DATE,
                        "1500",
                        "80",
                        "00",
                        "80"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderBonusDefaultsTest.getCoBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getCoBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getCoBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getCoBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "12000")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderBonusDefaultsTest.getCoBorrowerFullName(),
                        folderBuilderBonusDefaultsTest.getCoBorrowerSSN(),
                        folderBuilderBonusDefaultsTest.getCoBorrowerCollaboratorId(),
                        folderBuilderBonusDefaultsTest.getCoBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        "1500",
                        "1000",
                        "0",
                        "1000"))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilderBonusDefaultsTest.getFolderId());
    }

    @Test
    @Order(0)
    @Description("IA-2591 Check If Secondary Job Bonus All Years Are Selected By Default")
    void checkIfSecondaryJobBonusAllYearsAreSelectedByDefault() {
        RestIncomePart secondaryBonus = getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment())
                .getBonus();
        secondaryBonus.getIncomes().forEach(year -> assertTrue(year.getSelected(), String.format("Year: %s was not selected by default", year.getYear())));
    }


    @Test
    @Order(0)
    @Description("IA-2591 Check If Previous Job Bonus All Years Are Deselected By Default")
    void checkIfPreviousJobBonusAllYearsAreDeselectedByDefault() {
        RestIncomePart previousBonus = getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getBorrowerPreviousEmployment())
                .getBonus();
        previousBonus.getIncomes().forEach(year -> assertFalse(year.getSelected(), String.format("Year: %s was selected by default", year.getYear())));
    }

    @Test
    @Order(0)
    @Tag("integration")
    @Description("IA-2591 Check If Default Selection For Year Is Min Of Values For Secondary Job")
    void checkIfDefaultSelectionForYearIsMinOfValuesForSecondaryJob() {
        RestIncomePart secondaryBonus = getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment())
                .getBonus();
        assertAll("Asserting all yearly incomes have min value selected by default or doc precedence in case of equals",
                () -> assertFalse(secondaryBonus.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryBonus.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertTrue(secondaryBonus.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAnnualized().getSelected()),
                () -> assertFalse(secondaryBonus.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountAnnualized().getSelected()),

                () -> assertFalse(secondaryBonus.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryBonus.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryBonus.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAnnualized().getSelected()),
                () -> assertTrue(secondaryBonus.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAnnualized().getSelected()),

                () -> assertFalse(secondaryBonus.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryBonus.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryBonus.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAnnualized().getSelected()),
                () -> assertTrue(secondaryBonus.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAnnualized().getSelected())
        );
    }

    @Test
    @Order(0)
    @Tag("integration")
    @Description("IA-2591 Check If Default Selection For Year Is Min Of Values For Previous Job")
    void checkIfDefaultSelectionForYearIsMinOfValuesForPreviousJob() {
        RestIncomePart previousBonus = getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getBorrowerPreviousEmployment())
                .getBonus();

        assertAll("Asserting all yearly incomes have min value selected by default",
                () -> assertTrue(previousBonus.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected()),
                () -> assertFalse(previousBonus.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(previousBonus.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAnnualized().getSelected())
        );
    }

    @Test
    @Order(0)
    @Description("IA-2591 Check If Doc Precedence Works For Previous Job")
    void checkIfDocPrecedenceWorksForPreviousJob() {
        RestIncomePart previousBonus = getResponse.getApplicant(folderBuilderBonusDefaultsTest.getCoBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getCoBorrowerPreviousEmployment())
                .getBonus();

        assertAll("Asserting that voe bonus income takes precedence for previous job bonus",
                () -> assertTrue(previousBonus.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "VOE income was not selected"),
                () -> assertFalse(previousBonus.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "Paystub income was selected")
        );
    }

    @Test
    @Tag("integration")
    @Description("IA-2591 Check If Default Historical Average Defaults To Min Income Correctly")
    void checkIfDefaultHistoricalAverageDefaultsToMinCorrectly() {
        selectIncomeType(getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName()).getId(),
                getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getId(),
                IncomePartType.BONUS, true);

        final RestIncomeType[] historicalAverages = new RestIncomeType[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderBonusDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.BONUS);
            assertAll("Asserting that longest income is selected if all have same value",
                    () -> assertFalse(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertTrue(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });

        RestIncomePart secondaryBonus = getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment())
                .getBonus();
        selectJobAnnualSummary(secondaryBonus.getIncome(TWO_YEARS_PRIOR).getId(), secondaryBonus.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0), AVG);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderBonusDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.BONUS);

            assertAll("Asserting lowest income is selected",
                    () -> assertFalse(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG).getSelected()),
                    () -> assertTrue(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });

        RestIncomePart previousBonus = getResponse.getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getBorrowerPreviousEmployment())
                .getBonus();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderBonusDefaultsTest.getBorrowerSecondEmployment()).getId());

        selectIncome(primaryGroupId, previousBonus.getIncome(ONE_YEAR_PRIOR).getId(), true);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderBonusDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderBonusDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.BONUS);

            assertAll("Asserting lowest income is selected",
                    () -> assertTrue(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });
    }
}

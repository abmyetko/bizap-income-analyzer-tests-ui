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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.COMMISSIONS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryCommissionsDefaultsTest")
public class SecondaryCommissionsDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderCommissionsDefaultsTest = createFolderBuilder("IARSecComDf");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderCommissionsDefaultsTest.generateSecondaryJobsLoanDocument()
                .restBuild();
        dataUpload = createUploadObject(folderBuilderCommissionsDefaultsTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdCommission("1200")
                        .setPriorYearCommission("1000")
                        .setTwoYearPriorCommission("1000"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "900")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderCommissionsDefaultsTest.getBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "01/07/" + YEAR_TO_DATE,
                        "1500",
                        "80",
                        "80",
                        "00"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "12000")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerFullName(),
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerSSN(),
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerCollaboratorId(),
                        folderBuilderCommissionsDefaultsTest.getCoBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        "1500",
                        "1000",
                        "1000",
                        "00"))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilderCommissionsDefaultsTest.getFolderId());
    }

    @Test
    @Order(0)
    @Description("IA-2591 Check If Secondary Job Commissions All Years Are Selected By Default")
    void checkIfSecondaryJobCommissionsAllYearsAreSelectedByDefault() {
        RestIncomePart secondaryComm = getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment())
                .getCommissions();
        secondaryComm.getIncomes().forEach(year -> assertTrue(year.getSelected(), String.format("Year: %s was not selected by default", year.getYear())));
    }

    @Test
    @Order(0)
    @Description("IA-2591 Check If Previous Job Commissions All Years Are Deselected By Default")
    void checkIfPreviousJobCommissionsAllYearsAreDeselectedByDefault() {
        RestIncomePart previousComm = getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getBorrowerPreviousEmployment())
                .getCommissions();
        previousComm.getIncomes().forEach(year -> assertFalse(year.getSelected(), String.format("Year: %s was selected by default", year.getYear())));
    }

    @Test
    @Order(0)
    @Tag("integration")
    @Description("IA-2591 Check If Default Selection For Year Is Min Of Values")
    void checkIfDefaultSelectionForYearIsMinOfValues() {
        RestIncomePart secondaryComm = getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment())
                .getCommissions();
        assertAll("Asserting all yearly incomes have min value selected by default or doc precedence in case of equals",
                () -> assertTrue(secondaryComm.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryComm.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryComm.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertTrue(secondaryComm.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected()),
                () -> assertFalse(secondaryComm.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected()),
                () -> assertTrue(secondaryComm.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountAvg().getSelected())
        );

        RestIncomePart previousComm = getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getBorrowerPreviousEmployment())
                .getCommissions();

        assertAll("Asserting all yearly incomes have min value selected by default or doc precedence in case of equals",
                () -> assertTrue(previousComm.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected()),
                () -> assertFalse(previousComm.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected())
        );
    }

    @Test
    @Order(0)
    @Description("IA-2591 Check If Doc Precedence Works For Previous Job")
    void checkIfDocPrecedenceWorksForPreviousJob() {
        RestIncomePart previousComm = getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getCoBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getCoBorrowerPreviousEmployment())
                .getCommissions();

        assertAll("Asserting that voe commissions income takes precedence for previous job commissions",
                () -> assertTrue(previousComm.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE).getMonthlyAmountCalculated().getSelected(), "VOE income was not selected"),
                () -> assertFalse(previousComm.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getMonthlyAmountAvg().getSelected(), "Paystub income was selected")
        );
    }

    @Test
    @Tag("integration")
    @Description("IA-2591 Check If Default Historical Average Defaults To Min Income Correctly")
    void checkIfDefaultHistoricalAverageDefaultsToMinCorrectly() {
        selectIncomeType(getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName()).getId(),
                getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getId(),
                IncomePartType.COMMISSIONS, true);

        final RestIncomeType[] historicalAverages = new RestIncomeType[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderCommissionsDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.COMMISSIONS);
            assertAll("Asserting that longest income is selected if all have same value",
                    () -> assertFalse(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertTrue(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });

        RestIncomePart secondaryComm = getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment())
                .getCommissions();
        selectJobAnnualSummary(secondaryComm.getIncome(TWO_YEARS_PRIOR).getId(), secondaryComm.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(PAYSTUB).getDocIds().get(0), AVG);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderCommissionsDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.COMMISSIONS);

            assertAll("Asserting lowest income is selected",
                    () -> assertFalse(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG).getSelected()),
                    () -> assertTrue(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });

        RestIncomePart previousComm = getResponse.getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getBorrowerPreviousEmployment())
                .getCommissions();
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                .getIncome(folderBuilderCommissionsDefaultsTest.getBorrowerSecondEmployment()).getId());

        selectIncome(primaryGroupId, previousComm.getIncome(ONE_YEAR_PRIOR).getId(), true);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            historicalAverages[0] = RestGetLoanData.getApplicationData(folderBuilderCommissionsDefaultsTest.getFolderId())
                    .getApplicant(folderBuilderCommissionsDefaultsTest.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getSecondaryIncomeGroup()
                    .getIncomeType(IncomePartType.COMMISSIONS);

            assertAll("Asserting lowest income is selected",
                    () -> assertTrue(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR).getSelected()),
                    () -> assertFalse(historicalAverages[0].getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected())
            );
        });
    }
}

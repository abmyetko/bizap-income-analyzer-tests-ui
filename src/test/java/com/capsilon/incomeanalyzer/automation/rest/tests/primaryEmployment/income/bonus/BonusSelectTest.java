package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.bonus;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectIncome;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectJobAnnualSummary;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.ANNUALIZED;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.AVG;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.BONUS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = BONUS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "BonusSelectTest")
public class BonusSelectTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilder = createFolderBuilder("IARBnsTest");
    private RestGetResponse getResponse;

    @BeforeAll
    public void generateDocs() {
        folderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(folderBuilder.getCoBorrowerSSN(), "0.10")
                .setPrimaryJobBonus(folderBuilder.getCoBorrowerSSN(), "1.10")
                .restBuild();

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
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "100")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1100")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "1200",
                        "3600",
                        "03/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdBonus("1500")
                        .setPriorYearBonus("2800")
                        .setTwoYearPriorBonus("2500"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + TWO_YEARS_PRIOR,
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
                        .setYtdBonus("200")
                        .setPriorYearBonus("1800")
                        .setTwoYearPriorBonus("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "1000")))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(TWO_YEARS_PRIOR).getId(), true);

    }

    @Order(0)
    @Test
    @Tag("integration")
    @Description("IA-2205 checking if bonus values are selected correctly")
    void checkIfBonusValuesAreSelectedCorrectly() {

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), false);
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Check if bonus values are selected correctly",
                    () -> assertFalse(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(ONE_YEAR_PRIOR).getSelected(), "One Year Prior income should be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(TWO_YEARS_PRIOR).getSelected(), "Two years prior year should be selected"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR).getAvgMonthlyIncome(), "Year to date average income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR).getMonths(), "One year prior average income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS).getAvgMonthlyIncome(), "Two years prior average income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS).getMonths(), "Two years prior monthly income income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getTotalSelectedIncome(ONE_YEAR_PRIOR).getMonths(), "One year prior monthly income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getTotalSelectedIncome(ONE_YEAR_PRIOR).getGross(), "One year prior gross amount should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getTotalSelectedIncome(ONE_YEAR_PRIOR).getTrending(), "One year prior trending value should be 0"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getTotalSelectedIncome(TWO_YEARS_PRIOR).getMonths(), "Two years prior monthly income should be null "),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getTotalSelectedIncome(TWO_YEARS_PRIOR).getGross(), "Two years prior gross income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus().getTotalSelectedIncome(TWO_YEARS_PRIOR).getTrending(), "Two year prior trending value should be 0"));
        });
    }


    @Order(1)
    @Test
    @Description("IA-2205 check if YTD is defaulted to after deselecting prior and two Plus Prior Year")
    void checkIfYTDIsDefaultedToAfterDeselectingPriorAndTwoPlusPriorYear() {


        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertTrue(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeBonus()
                    .getAvgIncome(BONUS_YTD_AVG)
                    .getSelected());
        });
    }

    @Order(2)
    @Test
    @Description("IA-2205 check calculated HA for YTD Year Before Prior Year should be shown")
    void checkCalculatedHAForYTDYearBeforePriorYearShouldBeShown() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), true);
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertTrue(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeBonus()
                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR)
                    .getSelected());
        });
    }

    @Order(3)
    @Test
    @Description("IA-2205 Check if year before prior is selected (applicant could  only 6 months and UW would want to consider only full employment years")
    void CheckIfTwoYearsPriorCheckBoxIsSelected() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), false);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(TWO_YEARS_PRIOR).getId(), true);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Check if bonus values are selected correctly",
                    () -> assertFalse(getResponse
                            .getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                            .getBonus().getIncome(ONE_YEAR_PRIOR)
                            .getSelected(), "On year prior income should be selected"),
                    () -> assertTrue(getResponse
                            .getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                            .getBonus().getIncome(TWO_YEARS_PRIOR)
                            .getSelected(), "Two year prior income should be selected"),
                    () -> assertNull(
                            (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR)
                                    .getAvgMonthlyIncome()), "One year prior average monthly income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR)
                                    .getMonths(), "One year prior monthly income income should be null"),
                    () -> assertNotNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS)
                                    .getAvgMonthlyIncome(), "Two years prior average monthly income should be null"),
                    () -> assertNotNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS)
                                    .getMonths(), "Two years prior monthly income income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getTotalSelectedIncome(ONE_YEAR_PRIOR)
                                    .getMonths(), "One year prior monthly income should be null"),
                    () -> assertNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getTotalSelectedIncome(ONE_YEAR_PRIOR)
                                    .getGross(), "One year prior gross amount should be null"),
                    () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                            .getIncomeCategoryW2()
                            .getPrimaryIncomeGroup()
                            .getIncomeTypeBonus()
                            .getTotalSelectedIncome(ONE_YEAR_PRIOR)
                            .getTrending(), "One year prior total selected income should be 0"),
                    () -> assertNotNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getTotalSelectedIncome(TWO_YEARS_PRIOR)
                                    .getMonths(), "Two years prior total selected income should not be null"),
                    () -> assertNotNull(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getTotalSelectedIncome(TWO_YEARS_PRIOR)
                                    .getGross(), "Two years prior total selected income should not be null"));
        });
    }

    @Order(4)
    @Test
    @Description("IA-2205 check HA for YTD and Year Prior are visible and calculated")
    void checkHAForYTDAndYearPriorAreVisibleAndCalculated() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), true);
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertNotEquals(null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeBonus()
                    .getAvgIncome(BONUS_YTD_AVG)
                    .getAvgMonthlyIncome()));
            assertNotEquals(null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeBonus()
                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR)
                    .getAvgMonthlyIncome()));
        });
    }

    @Order(5)
    @Test
    @Description("IA-2205 check if after deselecting YTD other years stays selected")
    void checkIfAfterDeselectingYTDOtherYearsStaysSelected() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(TWO_YEARS_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(YEAR_TO_DATE).getId(), false);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Check if bonus values are selected correctly",
                    () -> assertTrue(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(ONE_YEAR_PRIOR)
                                    .getSelected(), "One year prior income should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(TWO_YEARS_PRIOR)
                                    .getSelected(), "Two year prior income should be selected"),
                    () -> assertNotEquals(
                            null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_YR)
                                    .getAvgMonthlyIncome()), "One year prior average income should not be null"),
                    () -> assertNotEquals(
                            null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup()
                                    .getIncomeTypeBonus()
                                    .getAvgIncome(BONUS_YTD_AVG_PLUS_PREV_TWO_YRS)
                                    .getAvgMonthlyIncome()), "Two years prior average income should not be null"));
        });
    }

    @Order(6)
    @Test
    @Description("IA-1227 Check If All Values On Document Level Are Selectable For Current Job")
    void checkIfAllValuesOnDocumentLevelAreSelectableForCurrentJob() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(TWO_YEARS_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(YEAR_TO_DATE).getId(), true);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {

            RestIncomePart bonusPart = RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                    .getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                    .getBonus();
            checkDocumentIncomeSelection(bonusPart, YEAR_TO_DATE, SummaryDocumentType.VOE, AVG);
            checkDocumentIncomeSelection(bonusPart, YEAR_TO_DATE, SummaryDocumentType.PAYSTUB, AVG);
            checkDocumentIncomeSelection(bonusPart, YEAR_TO_DATE, SummaryDocumentType.VOE, ANNUALIZED);
            checkDocumentIncomeSelection(bonusPart, YEAR_TO_DATE, SummaryDocumentType.PAYSTUB, ANNUALIZED);

            bonusPart = RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                    .getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                    .getBonus();
            checkDocumentIncomeSelection(bonusPart, ONE_YEAR_PRIOR, SummaryDocumentType.VOE, AVG);
            checkDocumentIncomeSelection(bonusPart, ONE_YEAR_PRIOR, SummaryDocumentType.PAYSTUB, AVG);
            checkDocumentIncomeSelection(bonusPart, ONE_YEAR_PRIOR, SummaryDocumentType.VOE, ANNUALIZED);
            checkDocumentIncomeSelection(bonusPart, ONE_YEAR_PRIOR, SummaryDocumentType.PAYSTUB, ANNUALIZED);

            bonusPart = RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                    .getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                    .getBonus();
            checkDocumentIncomeSelection(bonusPart, TWO_YEARS_PRIOR, SummaryDocumentType.VOE, AVG);
            checkDocumentIncomeSelection(bonusPart, TWO_YEARS_PRIOR, SummaryDocumentType.PAYSTUB, AVG);
            checkDocumentIncomeSelection(bonusPart, TWO_YEARS_PRIOR, SummaryDocumentType.VOE, ANNUALIZED);
            checkDocumentIncomeSelection(bonusPart, TWO_YEARS_PRIOR, SummaryDocumentType.PAYSTUB, ANNUALIZED);
        });
    }

    @Order(7)
    @Test
    @Description("IA-1227 Check If All Years Are Selectable For Current Job")
    void checkIfAllYearsAreSelectableForCurrentJob() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(TWO_YEARS_PRIOR).getId(), true);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Bonus year level selection assertion",
                    () -> assertTrue(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(YEAR_TO_DATE)
                                    .getSelected(), "Year to date income should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(ONE_YEAR_PRIOR)
                                    .getSelected(), "One year prior income should be selected"),
                    () -> assertTrue(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(TWO_YEARS_PRIOR)
                                    .getSelected(), "Two year prior income should be selected"));
        });

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(YEAR_TO_DATE).getId(), false);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(ONE_YEAR_PRIOR).getId(), false);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus().getIncome(TWO_YEARS_PRIOR).getId(), false);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Bonus year level deselection assertion",
                    () -> assertFalse(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(YEAR_TO_DATE)
                                    .getSelected(), "Year to date income should be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(ONE_YEAR_PRIOR)
                                    .getSelected(), "One year prior income should be selected"),
                    () -> assertFalse(
                            getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                                    .getBonus().getIncome(TWO_YEARS_PRIOR)
                                    .getSelected(), "Two year prior income should be selected"));
        });
    }

    void checkDocumentIncomeSelection(RestIncomePart bonusPart, int year, SummaryDocumentType docType, AnnualSummaryIncomeType incomeType) {
        selectJobAnnualSummary(bonusPart.getIncome(year).getId(), bonusPart.getIncome(year).getAnnualSummaryDocument(docType).getDocIds().get(0), incomeType);
        bonusPart = RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBonus();
        Boolean isSelected = false;
        switch (incomeType) {
            case AVG:
                isSelected = bonusPart.getIncome(year).getAllAnnualSummaryDocument(docType).getMonthlyAmountAvg().getSelected();
                break;
            case CALCULATED:
                isSelected = bonusPart.getIncome(year).getAllAnnualSummaryDocument(docType).getMonthlyAmountCalculated().getSelected();
                break;
            case ANNUALIZED:
                isSelected = bonusPart.getIncome(year).getAllAnnualSummaryDocument(docType).getMonthlyAmountAnnualized().getSelected();
                break;
            case NONE:
                break;
        }
        assertTrue(isSelected, String.format("%s %s income for %s should be selected", docType.toString(), incomeType.toString(), year));
    }
}

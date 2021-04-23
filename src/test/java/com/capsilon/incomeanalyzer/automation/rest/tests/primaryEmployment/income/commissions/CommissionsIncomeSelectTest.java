package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.commissions;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectIncome;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.COMMISSIONS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Disabled
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "CommissionsIncomeSelectTest")
public class CommissionsIncomeSelectTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilder = createFolderBuilder("IACMMSSTest");
    private RestGetResponse getResponse;

    @BeforeAll
    public void generateDocs() {
        folderBuilder.generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(folderBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobCommissions(folderBuilder.getBorrowerSSN(), "0.10")
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
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1000")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "100")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1100")))
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
                        .setYtdCommission("1500")
                        .setPriorYearCommission("2800")
                        .setTwoYearPriorCommission("2500"))
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
                        .setYtdCommission("200")
                        .setPriorYearCommission("1800")
                        .setTwoYearPriorCommission("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "250", "1000")))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(TWO_YEARS_PRIOR).getId(), true);

    }

    @Order(0)
    @Test
    @Tag("integration")
    @Description("IA-2138 checking if commissions values are selected correctly")
    void checkingIfCommissionsValuesAreSelectedCorrectly() {

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(ONE_YEAR_PRIOR).getId(), false);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {

            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

            assertFalse(getResponse
                    .getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                    .getCommissions().getIncome(ONE_YEAR_PRIOR)
                    .getSelected());
            assertFalse(getResponse
                    .getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                    .getCommissions().getIncome(TWO_YEARS_PRIOR)
                    .getSelected());
            assertNull((getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR)
                    .getAvgMonthlyIncome()));
            assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR)
                    .getMonths());
            assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS)
                    .getAvgMonthlyIncome());
            assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS)
                    .getMonths());
            assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getTotalSelectedIncome(ONE_YEAR_PRIOR)
                    .getMonths());
            assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getTotalSelectedIncome(ONE_YEAR_PRIOR)
                    .getGross());
            assertEquals(new BigDecimal(0), (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getTotalSelectedIncome(ONE_YEAR_PRIOR)
                    .getTrending()));
            assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getTotalSelectedIncome(TWO_YEARS_PRIOR)
                    .getMonths());
            assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getTotalSelectedIncome(TWO_YEARS_PRIOR)
                    .getGross());
            assertEquals(new BigDecimal(0), (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getTotalSelectedIncome(TWO_YEARS_PRIOR)
                    .getTrending()));
        });
    }


    @Order(1)
    @Test
    @Description("IA-2138 check if YTD is defaulted to after deselecting prior and two Plus Prior Year")
    void checkIfYTDIsDefaultedToAfterDeselectingPriorAndTwoPlusPriorYear() {

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TEN_SECONDS, () -> assertTrue(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                .getIncomeCategoryW2()
                .getPrimaryIncomeGroup()
                .getIncomeTypeCommissions()
                .getAvgIncome(COMMISSION_YTD_AVG)
                .getSelected()));
    }

    @Order(2)
    @Test
    @Description("IA-2138 check calculated HA for YTD Year Before Prior Year should be shown")
    void checkCalculatedHAForYTDYearBeforePriorYearShouldBeShown() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(ONE_YEAR_PRIOR).getId(), true);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertTrue(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                .getIncomeCategoryW2()
                .getPrimaryIncomeGroup()
                .getIncomeTypeCommissions()
                .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR)
                .getSelected());
    }

    @Order(3)
    @Test
    @Description("IA-2138 check HA for YTD and Year Prior are visible and calculated")
    void checkHAForYTDAndYearPriorAreVisibleAndCalculated() {

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
            assertNotEquals(null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG)
                    .getAvgMonthlyIncome()));
            assertNotEquals(null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR)
                    .getAvgMonthlyIncome()));
        });
    }

    @Order(4)
    @Test
    @Description("IA-2138 check if after deselecting YTD other years stays selected")
    void checkIfAfterDeselectingYTDOtherYearsStaysSelected() {
        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(TWO_YEARS_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions().getIncome(YEAR_TO_DATE).getId(), false);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TEN_SECONDS, () -> {

            assertTrue(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                    .getCommissions().getIncome(ONE_YEAR_PRIOR)
                    .getSelected());
            assertTrue(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                    .getCommissions().getIncome(TWO_YEARS_PRIOR)
                    .getSelected());
            assertNotEquals(null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_YR)
                    .getAvgMonthlyIncome()));
            assertNotEquals(null, (getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeCommissions()
                    .getAvgIncome(COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS)
                    .getAvgMonthlyIncome()));
        });
    }
}

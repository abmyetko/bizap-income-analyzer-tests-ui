package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.hourlyDefaults;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestAvgIncomes;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectIncomeAvg;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectJobAnnualSummary;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.AVG;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.CALCULATED;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "ConventionalLowTouchTest")
public class ConventionalLowTouchTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderHourlyDefaults = createFolderBuilder("IARCHrHALT");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilderHourlyDefaults.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderHourlyDefaults);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderHourlyDefaults.getBorrowerFullName(),
                        folderBuilderHourlyDefaults.getBorrowerSSN(),
                        folderBuilderHourlyDefaults.getBorrowerCollaboratorId(),
                        folderBuilderHourlyDefaults.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "10.24", "40", "1000.00", "2000.00")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderHourlyDefaults.getBorrowerFullName(),
                        folderBuilderHourlyDefaults.getBorrowerSSN(),
                        folderBuilderHourlyDefaults.getBorrowerCollaboratorId(),
                        folderBuilderHourlyDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.HOURLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "10.00",
                        "600.00",
                        "01/31/" + YEAR_TO_DATE)
                        .setAvgHoursPerWeek("40")
                        .setPriorYearBasePay("6000.00")
                        .setTwoYearPriorBasePay("12000.00"))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderHourlyDefaults.getBorrowerFullName(),
                        folderBuilderHourlyDefaults.getBorrowerSSN(),
                        folderBuilderHourlyDefaults.getBorrowerCollaboratorId(),
                        folderBuilderHourlyDefaults.getBorrowerCurrentEmployment(),
                        "12000.00",
                        ONE_YEAR_PRIOR.toString()))
                .importDocumentList();

        RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId());
    }

    private Stream oneDocCappedHoursTestCases() { // ytdDocument, ytdIncomeSelection, prevYrDocument, predictedHa
        return Stream.of(
                of(VOE, AVG, VOE, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(VOE, AVG, W2, BASEPAY_YTD_AVG),
                of(VOE, CALCULATED, VOE, BASEPAY_YTD_AVG),
                of(PAYSTUB, CALCULATED, W2, BASEPAY_YTD_AVG)
        );
    }

    @ParameterizedTest
    @MethodSource("oneDocCappedHoursTestCases")
    @Order(1)
    @Description("IA-2415 Check If Historical Average Is Defaulted Correctly For Different Income Selections For Conventional Loan")
    void checkIfHaIsDefaultedCorrectlyForDifferentIncomeSelections(SummaryDocumentType ytdDocument, AnnualSummaryIncomeType ytdIncomeSelection,
                                                                   SummaryDocumentType prevYtDocument, IncomeAvg predictedHa) {
        Retry.tryRun(TIMEOUT_TWENTY_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestIncomePart borrowerBp = RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId())
                    .getApplicant(folderBuilderHourlyDefaults.getBorrowerFullName())
                    .getIncome(folderBuilderHourlyDefaults.getBorrowerCurrentEmployment())
                    .getBasePay();

            selectJobAnnualSummary(borrowerBp.getIncome(YEAR_TO_DATE).getId(), borrowerBp.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(ytdDocument).getDocIds().get(0), ytdIncomeSelection);
            selectJobAnnualSummary(borrowerBp.getIncome(ONE_YEAR_PRIOR).getId(), borrowerBp.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(prevYtDocument).getDocIds().get(0), AVG);

            Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
                RestAvgIncomes borrowerPredictedAvgIncome = RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId())
                        .getApplicant(folderBuilderHourlyDefaults.getBorrowerFullName())
                        .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(predictedHa);
                assertTrue(borrowerPredictedAvgIncome.getSelected(), String.format("Predicted historical average %s was not selected.", borrowerPredictedAvgIncome.getAvgType()));
            });
        });
    }

    @Test
    @Order(2)
    @Description("IA-2417 Check If Ytd Historical Average Ytd Can Be Selected Manually For Conventional Loan")
    void checkIfYtdHaCanBeSelectedManually() {
        setHistoricalAverageForPrimaryBorrower(BASEPAY_YTD_AVG);
        checkIfHistoricalAverageForPrimaryBorrowerIsSelected(BASEPAY_YTD_AVG);
    }

    @Test
    @Order(2)
    @Description("IA-2417 Check If Ytd Historical Average Ytd Plus Prior Year Can Be Selected Manually For Conventional Loan")
    void checkIfYtdPlusPreviousYearHaCanBeSelectedManually() {
        setHistoricalAverageForPrimaryBorrower(BASEPAY_YTD_AVG_PLUS_PREV_YR);
        checkIfHistoricalAverageForPrimaryBorrowerIsSelected(BASEPAY_YTD_AVG_PLUS_PREV_YR);
    }

    @Test
    @Order(2)
    @Description("IA-2417 Check If Ytd Historical Average Ytd Plus Two Years Prior Can Be Selected Manually For Conventional Loan")
    void checkIfYtdPlusTwoYearsPriorHaCanBeSelectedManually() {
        setHistoricalAverageForPrimaryBorrower(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS);
        checkIfHistoricalAverageForPrimaryBorrowerIsSelected(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS);
    }

    void setHistoricalAverageForPrimaryBorrower(IncomeAvg incomeAvg) {
        Long borrowerId = RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId())
                .getApplicant(folderBuilderHourlyDefaults.getBorrowerFullName()).getId();
        RestAvgIncomes borrowerAvgIncome = RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId())
                .getApplicant(folderBuilderHourlyDefaults.getBorrowerFullName())
                .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(incomeAvg);
        String groupId = RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId())
                .getApplicant(folderBuilderHourlyDefaults.getBorrowerFullName())
                .getIncomeCategoryW2().getPrimaryIncomeGroup().getId();
        selectIncomeAvg(borrowerId, groupId, IncomeAvg.valueOfLabel(borrowerAvgIncome.getId()), true);
    }

    void checkIfHistoricalAverageForPrimaryBorrowerIsSelected(IncomeAvg incomeAvg) {
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            assertTrue(RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId())
                            .getApplicant(folderBuilderHourlyDefaults.getBorrowerFullName())
                            .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getAvgIncome(incomeAvg).getSelected(),
                    "Ytd historical average was could not be selected");
        });
    }
}

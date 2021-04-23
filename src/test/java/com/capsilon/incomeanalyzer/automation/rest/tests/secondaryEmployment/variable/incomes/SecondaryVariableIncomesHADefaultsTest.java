package com.capsilon.incomeanalyzer.automation.rest.tests.secondaryEmployment.variable.incomes;

import com.capsilon.incomeanalyzer.automation.data.upload.data.VoeCurrentData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponseApplicant;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryVariableIncomesHADefaultsTest")
public class SecondaryVariableIncomesHADefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderFhaDefaultTest = createFolderBuilder("IARSecDef");

    @BeforeAll
    void createFolder() {
        folderBuilderFhaDefaultTest.setMortgageAppliedFor(MortgageType.FHA)
                .generateSecondaryJobsLoanDocument()
                .restBuild();
        dataUpload = createUploadObject(folderBuilderFhaDefaultTest);

        RestGetLoanData.getApplicationData(folderBuilderFhaDefaultTest.getFolderId());
    }

    private Stream historicalAverageSelectionCases() { // ytd prior twoPrior PartType Expected
        return Stream.of(
                of(bigD(1000), bigD(12000), bigD(12000), IncomePartType.OVERTIME, OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(900), bigD(12000), bigD(12000), IncomePartType.OVERTIME, OVERTIME_YTD_AVG),
                of(bigD(1000), bigD(11000), bigD(12000), IncomePartType.OVERTIME, OVERTIME_YTD_AVG_PLUS_PREV_YR),
                of(bigD(1000), bigD(12000), bigD(11000), IncomePartType.OVERTIME, OVERTIME_YTD_AVG_PLUS_PREV_TWO_YRS),

                of(bigD(1000), bigD(12000), bigD(12000), IncomePartType.COMMISSIONS, COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(900), bigD(12000), bigD(12000), IncomePartType.COMMISSIONS, COMMISSION_YTD_AVG),
                of(bigD(1000), bigD(11000), bigD(12000), IncomePartType.COMMISSIONS, COMMISSION_YTD_AVG_PLUS_PREV_YR),
                of(bigD(1000), bigD(12000), bigD(11000), IncomePartType.COMMISSIONS, COMMISSION_YTD_AVG_PLUS_PREV_TWO_YRS),

                of(bigD(12000), bigD(12000), bigD(12000), IncomePartType.BONUS, BONUS_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(11000), bigD(12000), bigD(12000), IncomePartType.BONUS, BONUS_YTD_AVG),
                of(bigD(12000), bigD(11000), bigD(12000), IncomePartType.BONUS, BONUS_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(12000), bigD(11000), IncomePartType.BONUS, BONUS_YTD_AVG_PLUS_PREV_TWO_YRS)
        );
    }

    @ParameterizedTest
    @MethodSource("historicalAverageSelectionCases")
    @Description("IA-2579 Check If Historical Average Values For Secondary Job Variable Incomes Are Defaulted To Min")
    void checkIfHistoricalAverageValuesForSecondaryJobVariableIncomesAreDefaultedToMin(BigDecimal ytdIncome, BigDecimal yearPriorIncome, BigDecimal twoYrPriorIncome,
                                                                                       IncomePartType incomePartType, IncomeAvg expectedSelection) {
        generateAndUploadDocument(ytdIncome, yearPriorIncome, twoYrPriorIncome, incomePartType);
        checkExpectedSelection(incomePartType, expectedSelection);
    }

    void generateAndUploadDocument(BigDecimal ytdIncome, BigDecimal yearPriorIncome, BigDecimal twoYrPriorIncome, IncomePartType incomePartType) {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "10.00",
                        "10.00",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearYear(ONE_YEAR_PRIOR.toString())
                        .setTwoYearPriorYear(TWO_YEARS_PRIOR.toString()));

        switch (incomePartType) {
            case OVERTIME:
                ((VoeCurrentData) dataUpload.getDocumentList().get(0))
                        .setYtdOvertime(ytdIncome.toString())
                        .setPriorYearOvertime(yearPriorIncome.toString())
                        .setTwoYearPriorOvertime(twoYrPriorIncome.toString());
                break;
            case COMMISSIONS:
                ((VoeCurrentData) dataUpload.getDocumentList().get(0))
                        .setYtdCommission(ytdIncome.toString())
                        .setPriorYearCommission(yearPriorIncome.toString())
                        .setTwoYearPriorCommission(twoYrPriorIncome.toString());
                break;
            case BONUS:
                ((VoeCurrentData) dataUpload.getDocumentList().get(0))
                        .setYtdBonus(ytdIncome.toString())
                        .setPriorYearBonus(yearPriorIncome.toString())
                        .setTwoYearPriorBonus(twoYrPriorIncome.toString());
                break;
            default:
                break;
        }

        dataUpload.importDocumentList();
    }

    void checkExpectedSelection(IncomePartType incomePartType, IncomeAvg expectedSelection) {
        RestGetResponseApplicant applicant = RestGetLoanData.getApplicationData(folderBuilderFhaDefaultTest.getFolderId()).getApplicant(folderBuilderFhaDefaultTest.getBorrowerFullName());

        assertTrue(
                applicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeType(incomePartType).getAvgIncome(expectedSelection).getSelected(), String.format("Historical average %s value for income type %s was not selected", expectedSelection, incomePartType));
    }
}
package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestAvgIncomes;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "FhaDefaultsTest")
public class FhaDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderFhaDefaultTest = createFolderBuilder("IARestFhaDf");
    private RestGetResponse getResponse;

    //Test case 3 & 4 are affected by conventional hourly defaults story should be changed to appropriate values when fha story is done
    private static Stream defaultVoeTestCases() {
        return Stream.of(
                of(new BigDecimal(1000), new BigDecimal(2000), new BigDecimal(3000), IncomeAvg.BASEPAY_YTD_AVG),
                of(new BigDecimal(2000), new BigDecimal(1000), new BigDecimal(3000), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(new BigDecimal(3000), new BigDecimal(2000), new BigDecimal(1000), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(new BigDecimal(1000), new BigDecimal(1000), new BigDecimal(1000), IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS)
        );
    }

    //Test case 2 & 3 & 4 are affected by conventional hourly defaults story should be changed to appropriate values when fha story is done
    private static Stream defaultPaystubTestCases() {
        return Stream.of(
                of(new BigDecimal(1000), new BigDecimal(2000), new BigDecimal(3000), IncomeAvg.BASEPAY_YTD_AVG),
                of(new BigDecimal(2000), new BigDecimal(1000), new BigDecimal(3000), IncomeAvg.BASEPAY_YTD_AVG),
                of(new BigDecimal(3000), new BigDecimal(2000), new BigDecimal(1000), IncomeAvg.BASEPAY_YTD_AVG),
                of(new BigDecimal(1000), new BigDecimal(1000), new BigDecimal(1000), IncomeAvg.BASEPAY_YTD_AVG)
        );
    }

    @BeforeAll
    void importDocument() {
        folderBuilderFhaDefaultTest.setMortgageAppliedFor(MortgageType.FHA);
        folderBuilderFhaDefaultTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderFhaDefaultTest);
        //Line used to break test execution in case of changed API response
        RestGetLoanData.getApplicationData(folderBuilderFhaDefaultTest.getFolderId());
    }

    @ParameterizedTest(name = "{index} Fha loan default for voe with monthly income ytd: {0} previous year: {1} two years prior: {2}")
    @MethodSource("defaultVoeTestCases")
    void testDefaultsForHourlyVoe(BigDecimal ytdMonthlyIncome, BigDecimal priorMonthlyIncome, BigDecimal twoYrPriorMonthlyIncome, IncomeAvg predictedSelection) {
        resetAllDocuments();
        generateAndUploadDefaultHourlyVoe(ytdMonthlyIncome, priorMonthlyIncome, twoYrPriorMonthlyIncome);

        getResponse = RestGetLoanData.getApplicationData(folderBuilderFhaDefaultTest.getFolderId());

        RestAvgIncomes smallestAvg = getResponse.getApplicant(folderBuilderFhaDefaultTest.getBorrowerFullName())
                .getIncomeCategoryW2()
                .getPrimaryIncomeGroup()
                .getIncomeType(IncomePartType.BASE_PAY).getAvgIncome(predictedSelection);

        assertTrue(smallestAvg.getSelected(), String.format("Predicted Historical Average was not selected. Expected %s", predictedSelection));
    }

    @ParameterizedTest(name = "{index} Fha loan default for paystubs with monthly income ytd: {0} previous year: {1} two years prior: {2}")
    @MethodSource("defaultPaystubTestCases")
    void testDefaultsForHourlyPaystubs(BigDecimal ytdMonthlyIncome, BigDecimal priorMonthlyIncome, BigDecimal twoYrPriorMonthlyIncome, IncomeAvg predictedSelection) {
        resetAllDocuments();
        generateAndUploadDefaultHourlyPaystubs(ytdMonthlyIncome, priorMonthlyIncome, twoYrPriorMonthlyIncome);

        getResponse = RestGetLoanData.getApplicationData(folderBuilderFhaDefaultTest.getFolderId());

        RestAvgIncomes smallestAvg = getResponse.getApplicant(folderBuilderFhaDefaultTest.getBorrowerFullName())
                .getIncomeCategoryW2()
                .getPrimaryIncomeGroup()
                .getIncomeType(IncomePartType.BASE_PAY).getAvgIncome(predictedSelection);

        assertTrue(smallestAvg.getSelected(), String.format("Predicted Historical Average was not selected. Expected %s", predictedSelection));
    }

    void resetAllDocuments() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.HOURLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "",
                        "",
                        "01/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", "")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", "")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", "")))
                .importDocumentList();
    }

    void generateAndUploadDefaultHourlyVoe(BigDecimal ytdMonthlyIncome, BigDecimal priorMonthlyIncome, BigDecimal twoYrPriorMonthlyIncome) {
        dataUpload.importDocument((dataUpload.createCustomVoeCurrent(
                folderBuilderFhaDefaultTest.getBorrowerFullName(),
                folderBuilderFhaDefaultTest.getBorrowerSSN(),
                folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                IncomeFrequency.HOURLY,
                "01/01/" + TWO_YEARS_PRIOR,
                "01/31/" + YEAR_TO_DATE,
                "150.00",
                ytdMonthlyIncome.setScale(2, RoundingMode.HALF_UP).toString(),
                "01/31/" + YEAR_TO_DATE))
                .setAvgHoursPerWeek("40.00")
                .setPriorYearBasePay(priorMonthlyIncome.multiply(new BigDecimal(12)).setScale(2, RoundingMode.HALF_UP).toString())
                .setPriorYearTotal("28,800.00")
                .setTwoYearPriorBasePay(twoYrPriorMonthlyIncome.multiply(new BigDecimal(12)).setScale(2, RoundingMode.HALF_UP).toString())
                .setTwoYearPriorTotal("17,000.00"));
    }

    void generateAndUploadDefaultHourlyPaystubs(BigDecimal ytdMonthlyIncome, BigDecimal priorMonthlyIncome, BigDecimal twoYrPriorMonthlyIncome) {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "150.00", "40", "600.00",
                                ytdMonthlyIncome.setScale(2, RoundingMode.HALF_UP).toString())))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "150.00", "40", "600.00",
                                priorMonthlyIncome.multiply(new BigDecimal(12)).setScale(2, RoundingMode.HALF_UP).toString())))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderFhaDefaultTest.getBorrowerFullName(),
                        folderBuilderFhaDefaultTest.getBorrowerSSN(),
                        folderBuilderFhaDefaultTest.getBorrowerCollaboratorId(),
                        folderBuilderFhaDefaultTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "150.00", "40", "600.00",
                                twoYrPriorMonthlyIncome.multiply(new BigDecimal(12)).setScale(2, RoundingMode.HALF_UP).toString())))
                .importDocumentList();
    }
}

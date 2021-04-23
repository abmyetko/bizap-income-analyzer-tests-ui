package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "CalculationTest")
public class CalculationTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderCalcTest = createFolderBuilder("IARestCalc");
    private static RestGetResponse getResponse;

    private static Stream voeTestCases() {
        return Stream.of(
                of(WEEKLY, "600.56", "12808.05", "01/07/" + YEAR_TO_DATE),
                of(BI_WEEKLY, "600.56", "12808.05", "01/14/" + YEAR_TO_DATE),
                of(SEMI_MONTHLY, "600.56", "12808.05", "01/15/" + YEAR_TO_DATE),
                of(MONTHLY, "5164.54", "12808.05", "01/31/" + YEAR_TO_DATE),
                of(QUARTERLY, "15000.56", "12808.05", "04/01/" + YEAR_TO_DATE),
                of(SEMI_ANNUALLY, "30000.54", "12808.05", "06/30/" + YEAR_TO_DATE),
                of(ANNUALLY, "60000.54", "12808.05", "12/31/" + YEAR_TO_DATE)
        );
    }

    private static Stream paystubTestCases() {
        return Stream.of(
                of("01/01/" + YEAR_TO_DATE, "01/07/" + YEAR_TO_DATE, "600.56", "12808.05"),
                of("01/01/" + YEAR_TO_DATE, "01/14/" + YEAR_TO_DATE, "600.56", "12808.05"),
                of("01/01/" + YEAR_TO_DATE, "01/15/" + YEAR_TO_DATE, "600.56", "12808.05"),
                of("01/01/" + YEAR_TO_DATE, "01/31/" + YEAR_TO_DATE, "5164.54", "12808.05"),
                of("01/01/" + YEAR_TO_DATE, "04/01/" + YEAR_TO_DATE, "15000.56", "12808.05"),
                of("01/01/" + YEAR_TO_DATE, "06/30/" + YEAR_TO_DATE, "30000.54", "12808.05"),
                of("01/01/" + YEAR_TO_DATE, "12/31/" + YEAR_TO_DATE, "60000.54", "12808.05")
        );
    }

    @BeforeAll
    void setupTestFolder() {
        folderBuilderCalcTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderCalcTest);
        //Line used to break test execution in case of changed API response
        getResponse = RestGetLoanData.getApplicationData(folderBuilderCalcTest.getFolderId());
    }

    @BeforeEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    @ParameterizedTest(name = "{index} Paystub calculations for {0}-{1} {2} period amount {3} gross amount")
    @MethodSource("paystubTestCases")
    @Description("IA-1791 IA-912 Check If Paystub Calculations Are Correct For Set Dates")
    void checkPaystubCalculations(String startDate, String endDate, String periodAmount, String grossAmount) {
        checkPaystubSalary(startDate, endDate, periodAmount, grossAmount);
    }

    @ParameterizedTest(name = "{index} VoE calculations for {0} frequency {1} period amount {2} gross amount {3} through date")
    @MethodSource("voeTestCases")
    @Description("IA-1791 IA-912 Check If Voe Calculations Are Correct For Set Dates")
    void checkVoeCalculations(IncomeFrequency paymentType, String periodAmount, String grossAmount, String incomeThroughDate) {
        checkVoeSalary(paymentType, periodAmount, grossAmount, incomeThroughDate);
    }

    void checkPaystubSalary(String startDate, String endDate, String periodAmount, String grossAmount) {
        dataUpload.importDocument(dataUpload.createCustomPaystub(
                folderBuilderCalcTest.getBorrowerFullName(),
                folderBuilderCalcTest.getBorrowerSSN(),
                folderBuilderCalcTest.getBorrowerCollaboratorId(),
                folderBuilderCalcTest.getBorrowerCurrentEmployment(),
                startDate,
                endDate,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", periodAmount, grossAmount)));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderCalcTest.getFolderId());

        checkCalculations(YEAR_TO_DATE, SummaryDocumentType.PAYSTUB);
    }

    void checkVoeSalary(IncomeFrequency paymentType, String periodAmount, String grossAmount, String incomeThroughDate) {
        dataUpload.importDocument(dataUpload.createCustomVoeCurrent(
                folderBuilderCalcTest.getBorrowerFullName(),
                folderBuilderCalcTest.getBorrowerSSN(),
                folderBuilderCalcTest.getBorrowerCollaboratorId(),
                folderBuilderCalcTest.getBorrowerCurrentEmployment(),
                paymentType,
                "06/01/" + TWO_YEARS_PRIOR,
                incomeThroughDate,
                periodAmount,
                grossAmount,
                incomeThroughDate));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderCalcTest.getFolderId());

        checkCalculations(YEAR_TO_DATE, SummaryDocumentType.VOE);
    }

    void checkCalculations(Integer year, SummaryDocumentType documentType) {
        final RestPartIncomeAnnualSummary[] income = new RestPartIncomeAnnualSummary[1];
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                income[0] = getResponse.getApplicant(folderBuilderCalcTest.getBorrowerFullName())
                        .getIncome("W2", folderBuilderCalcTest.getBorrowerCurrentEmployment())
                        .getPart("Base Pay")
                        .getIncome(year).getAnnualSummaryDocument(documentType)
        );

        BigDecimal ytdAvgIncome = income[0].getGrossAmount().getValue().divide(income[0].getMonths().getValue(), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyIncome = income[0].getPeriodAmount().getValue()
                .multiply(new BigDecimal(income[0].getFrequency().getValue().value))
                .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
        BigDecimal difference = ytdAvgIncome.subtract(monthlyIncome);

        assertAll("calculation assertion",
                () -> assertEquals(ytdAvgIncome.setScale(2, RoundingMode.HALF_UP), income[0].getMonthlyAmountAvg().getValue().setScale(2, RoundingMode.HALF_UP)),
                () -> assertEquals(monthlyIncome.setScale(2, RoundingMode.HALF_UP), income[0].getMonthlyAmountCalculated().getValue().setScale(2, RoundingMode.HALF_UP)),
                () -> assertEquals(difference.setScale(2, RoundingMode.HALF_UP), income[0].getDifference().setScale(2, RoundingMode.HALF_UP)),
                () -> assertEquals(difference.setScale(8, RoundingMode.HALF_UP)
                                .divide(monthlyIncome, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal(100))
                                .setScale(2, RoundingMode.HALF_UP),
                        income[0].getVariance().setScale(2, RoundingMode.HALF_UP))
        );
    }
}

package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "MonthsTest")
class MonthsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderMonthTest = createFolderBuilder("IARestMonth");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderMonthTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderMonthTest);
        //Line used to break test execution in case of changed API response
        getResponse = RestGetLoanData.getApplicationData(folderBuilderMonthTest.getFolderId());
    }

    private Stream paystubSalariedTestCases() {
        return Stream.of(
                of("01/07/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, "0.23000000", YEAR_TO_DATE),
                of("01/14/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, "0.45000000", YEAR_TO_DATE),
                of("01/15/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, "0.48000000", YEAR_TO_DATE),
                of("01/31/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, "1.00000000", YEAR_TO_DATE),
                of("03/31/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, "3.00000000", YEAR_TO_DATE),
                of("06/30/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, "6.00000000", YEAR_TO_DATE),
                of("12/31/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, "12.00000000", YEAR_TO_DATE),
                of("12/31/" + ONE_YEAR_PRIOR, "12/01/" + ONE_YEAR_PRIOR, "12.00000000", ONE_YEAR_PRIOR)
        );
    }

    private Stream paystubHourlyTestCases() {
        return Stream.of(
                of("40.00", "15.00", "0.23000000", "01/01/" + YEAR_TO_DATE, "01/07/" + YEAR_TO_DATE, YEAR_TO_DATE)
        );
    }

    private Stream voeSalariedTestCases() {
        return Stream.of(
                of("01/31/" + YEAR_TO_DATE, "06/01/" + TWO_YEARS_PRIOR, YEAR_TO_DATE, "1.00000000", IncomeFrequency.WEEKLY),
                of("01/31/" + YEAR_TO_DATE, "06/01/" + TWO_YEARS_PRIOR, ONE_YEAR_PRIOR, "12.00000000", IncomeFrequency.WEEKLY),
                of("01/31/" + YEAR_TO_DATE, "06/01/" + TWO_YEARS_PRIOR, TWO_YEARS_PRIOR, "7.00000000", IncomeFrequency.WEEKLY),
                of("01/31/" + YEAR_TO_DATE, "06/01/" + TWO_YEARS_PRIOR, YEAR_TO_DATE, "1.00000000", IncomeFrequency.MONTHLY),
                of("01/31/" + YEAR_TO_DATE, "06/01/" + TWO_YEARS_PRIOR, ONE_YEAR_PRIOR, "12.00000000", IncomeFrequency.MONTHLY),
                of("01/31/" + YEAR_TO_DATE, "06/01/" + TWO_YEARS_PRIOR, TWO_YEARS_PRIOR, "7.00000000", IncomeFrequency.MONTHLY)
        );
    }

    private Stream w2TestCases() {
        return Stream.of(
                of(ONE_YEAR_PRIOR, "12.00000000")
        );
    }

    @ParameterizedTest(name = "{index} Paystub salaried calculations for {1}-{0} with expected months: {2}")
    @MethodSource("paystubSalariedTestCases")
    void checkPaystubSalariedCalculations(String endDate, String startDate, String expectedMonths, Integer year) {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        folderBuilderMonthTest.getBorrowerFullName(),
                        folderBuilderMonthTest.getBorrowerSSN(),
                        folderBuilderMonthTest.getBorrowerCollaboratorId(),
                        folderBuilderMonthTest.getBorrowerCurrentEmployment(),
                        startDate,
                        endDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "731.20", "3,600.09")));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderMonthTest.getFolderId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertEquals(expectedMonths, getResponse.getApplicant(folderBuilderMonthTest.getBorrowerFullName())
                        .getIncome("W2", folderBuilderMonthTest.getBorrowerCurrentEmployment())
                        .getPart("Base Pay")
                        .getIncome(year).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getValue().toString()));
    }

    @ParameterizedTest(name = "{index} Paystub hourly calculations for {3}-{4} hours: {0} rate: {1} with expected months: {2}")
    @MethodSource("paystubHourlyTestCases")
    void checkPaystubHourlyCalculations(String periodHours, String periodRate, String expectedMonths, String startDate, String endDate, Integer year) {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        folderBuilderMonthTest.getCoBorrowerFullName(),
                        folderBuilderMonthTest.getCoBorrowerSSN(),
                        folderBuilderMonthTest.getCoBorrowerCollaboratorId(),
                        folderBuilderMonthTest.getCoBorrowerCurrentEmployment(),
                        startDate,
                        endDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, periodRate, periodHours, "600.00", "600.00")));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderMonthTest.getFolderId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertEquals(expectedMonths, getResponse.getApplicant(folderBuilderMonthTest.getCoBorrowerFullName())
                        .getIncome("W2", folderBuilderMonthTest.getCoBorrowerCurrentEmployment())
                        .getPart("Base Pay")
                        .getIncome(year).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonths().getValue().toString()));
    }

    @ParameterizedTest(name = "{index} VoE salaried calculations for {1}-{0} with expected months: {2}")
    @MethodSource("voeSalariedTestCases")
    void checkVoeSalariedCalculations(String endDate, String startDate, Integer year, String expectedMonths, IncomeFrequency paymentType) {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderMonthTest.getBorrowerFullName(),
                        folderBuilderMonthTest.getBorrowerSSN(),
                        folderBuilderMonthTest.getBorrowerCollaboratorId(),
                        folderBuilderMonthTest.getBorrowerCurrentEmployment(),
                        paymentType,
                        startDate,
                        endDate,
                        "2450.00",
                        "7,200.00",
                        endDate)
                        .setPriorYearBasePay("28,800.00")
                        .setTwoYearPriorBasePay("17,000.00")
                        .setPriorYearTotal("31,059.68")
                        .setTwoYearPriorTotal("17,000.00"));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderMonthTest.getFolderId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertEquals(expectedMonths, getResponse.getApplicant(folderBuilderMonthTest.getBorrowerFullName())
                        .getIncome("W2", folderBuilderMonthTest.getBorrowerCurrentEmployment())
                        .getPart("Base Pay")
                        .getIncome(year).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonths().getValue().toString()));
    }

    @Disabled("Disabled because bug will not be fixed in this release")
    @ParameterizedTest(name = "{index} W2 calculations for {0} year with expected months: {1}")
    @MethodSource("w2TestCases")
    void checkW2Calculations(Integer year, String expectedMonths) {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomW2(
                        folderBuilderMonthTest.getCoBorrowerFullName(),
                        folderBuilderMonthTest.getCoBorrowerSSN(),
                        folderBuilderMonthTest.getCoBorrowerCollaboratorId(),
                        folderBuilderMonthTest.getCoBorrowerCurrentEmployment(),
                        "47,321.00",
                        year.toString()));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderMonthTest.getFolderId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () ->
                assertEquals(expectedMonths, getResponse.getApplicant(folderBuilderMonthTest.getBorrowerFullName())
                        .getIncome("W2", folderBuilderMonthTest.getBorrowerCurrentEmployment())
                        .getPart("Base Pay")
                        .getIncome(year).getAnnualSummaryDocument(SummaryDocumentType.W2).getMonths().getValue().toString()));
    }
}

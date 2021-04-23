package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "FrequencyTest")
class FrequencyTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderFreqTest = createFolderBuilder("IARestFreq");
    private RestGetResponse getResponse;

    @BeforeAll
    void setupTestFolder() {
        folderBuilderFreqTest.setSignDate("02/01/" + YEAR_TO_DATE);
        folderBuilderFreqTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderFreqTest);
        //Line used to break test execution in case of changed API response
        getResponse = RestGetLoanData.getApplicationData(folderBuilderFreqTest.getFolderId());
    }

    @BeforeEach
    void clearIncomeData() {
        dataUpload.removeDocumentsFromFolder();
    }

    public Stream paystubTestCases() {
        return Stream.of(
                of("01/06/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/07/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.WEEKLY, YEAR_TO_DATE),
                of("01/08/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/14/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.BI_WEEKLY, YEAR_TO_DATE),
                of("01/15/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("01/16/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("01/17/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/18/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/19/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/20/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/21/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/22/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/23/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/24/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/25/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/26/" + YEAR_TO_DATE, "12/27/" + (YEAR_TO_DATE - 1), IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("01/26/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/27/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("01/28/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("01/29/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("01/30/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("01/31/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("01/31/" + YEAR_TO_DATE, "01/16/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("01/31/" + YEAR_TO_DATE, "01/17/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("02/14/" + YEAR_TO_DATE, "02/01/" + YEAR_TO_DATE, IncomeFrequency.BI_WEEKLY, YEAR_TO_DATE),
                of("02/15/" + YEAR_TO_DATE, "02/01/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("02/16/" + YEAR_TO_DATE, "02/01/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
//                of("02/16/" + CommonMethods.getNextLeapYear(), "02/03/" + YEAR_TO_DATE, IncomeFrequency.BI_WEEKLY, CommonMethods.getNextLeapYear()), //leap year needed
                of("02/16/" + YEAR_TO_DATE, "02/04/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
//                of("02/29/" + CommonMethods.getNextLeapYear(), "02/16/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, CommonMethods.getNextLeapYear()), //leap year needed
//                of("02/29/" + CommonMethods.getNextLeapYear(), "02/17/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, CommonMethods.getNextLeapYear()), //leap year needed
                of("03/16/" + YEAR_TO_DATE, "03/01/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
//                of("03/29/" + CommonMethods.getNextLeapYear(), "03/16/" + YEAR_TO_DATE, IncomeFrequency.BI_WEEKLY, CommonMethods.getNextLeapYear()), //leap year needed
//                remove comments when Leap Year will be available
                of("03/30/" + YEAR_TO_DATE, "03/15/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("03/30/" + YEAR_TO_DATE, "03/16/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("03/31/" + YEAR_TO_DATE, "03/04/" + YEAR_TO_DATE, IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("03/31/" + YEAR_TO_DATE, "03/16/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("04/01/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.QUARTERLY, YEAR_TO_DATE),
                of("04/01/" + YEAR_TO_DATE, "03/04/" + YEAR_TO_DATE, IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("04/02/" + YEAR_TO_DATE, "03/04/" + YEAR_TO_DATE, IncomeFrequency.MONTHLY, YEAR_TO_DATE),
                of("04/28/" + YEAR_TO_DATE, "04/16/" + YEAR_TO_DATE, IncomeFrequency.UNKNOWN, YEAR_TO_DATE),
                of("04/30/" + YEAR_TO_DATE, "04/16/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("05/16/" + YEAR_TO_DATE, "05/01/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("05/16/" + YEAR_TO_DATE, "05/02/" + YEAR_TO_DATE, IncomeFrequency.SEMI_MONTHLY, YEAR_TO_DATE),
                of("06/30/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.SEMI_ANNUALLY, YEAR_TO_DATE),
                of("12/31/" + YEAR_TO_DATE, "01/01/" + YEAR_TO_DATE, IncomeFrequency.ANNUALLY, YEAR_TO_DATE),
                of("02/14/" + (YEAR_TO_DATE + 1), "02/01/" + (YEAR_TO_DATE + 1), IncomeFrequency.BI_WEEKLY, (YEAR_TO_DATE + 1)),
                of("02/15/" + (YEAR_TO_DATE + 1), "02/01/" + (YEAR_TO_DATE + 1), IncomeFrequency.SEMI_MONTHLY, (YEAR_TO_DATE + 1)),
                of("02/16/" + (YEAR_TO_DATE + 1), "02/01/" + (YEAR_TO_DATE + 1), IncomeFrequency.SEMI_MONTHLY, (YEAR_TO_DATE + 1)),
                of("02/28/" + (YEAR_TO_DATE + 1), "02/16/" + (YEAR_TO_DATE + 1), IncomeFrequency.SEMI_MONTHLY, (YEAR_TO_DATE + 1)),
                of(getThisWeekMondayDate("03/01/" + (YEAR_TO_DATE + 1)).plusDays(18).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getThisWeekMondayDate("03/01/" + (YEAR_TO_DATE + 1)).plusDays(7).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.BI_WEEKLY, (YEAR_TO_DATE + 1)),
                of(getThisWeekMondayDate("03/01/" + (YEAR_TO_DATE + 1)).plusDays(19).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getThisWeekMondayDate("03/01/" + (YEAR_TO_DATE + 1)).plusDays(8).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE + 1)),
                of(getThisWeekMondayDate("03/01/" + (YEAR_TO_DATE + 1)).plusDays(19).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getThisWeekMondayDate("03/01/" + (YEAR_TO_DATE + 1)).plusDays(7).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE + 1)),
                of(getThisWeekMondayDate("04/01/" + (YEAR_TO_DATE + 1)).plusDays(4).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getThisWeekMondayDate("04/01/" + (YEAR_TO_DATE + 1)).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.WEEKLY, (YEAR_TO_DATE + 1)),
                of(getThisWeekMondayDate("04/01/" + (YEAR_TO_DATE + 1)).plusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getThisWeekMondayDate("04/01/" + (YEAR_TO_DATE + 1)).plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE + 1)),
                of(getThisWeekMondayDate("04/01/" + (YEAR_TO_DATE + 1)).plusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getThisWeekMondayDate("04/01/" + (YEAR_TO_DATE + 1)).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE + 1)),
                of(getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.FRIDAY).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.FRIDAY).minusDays(12).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.BI_WEEKLY, (YEAR_TO_DATE)),
                of(getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.THURSDAY).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.THURSDAY).minusDays(12).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE)),
                of(getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.WEDNESDAY).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.WEDNESDAY).minusDays(12).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE)),
                of(getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.TUESDAY).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.TUESDAY).minusDays(12).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE)),
                of(getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.MONDAY).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        getLastSpecificDayOfTheWeekOfTheMonth(DayOfWeek.MONDAY).minusDays(12).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        IncomeFrequency.UNKNOWN, (YEAR_TO_DATE)));
    }

    @ParameterizedTest(name = "{index} Paystub frequency for {1}-{0} with expected frequency: {2}")
    @MethodSource("paystubTestCases")
    @Description("IA-2368 IA-2369 IA-2370 IA-2952 Check Paystub Frequency Calculations For Set Date")
    void checkPaystubFrequencyCalculations(String endDate, String startDate, IncomeFrequency expectedFrequency, Integer year) {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        folderBuilderFreqTest.getBorrowerFullName(),
                        folderBuilderFreqTest.getBorrowerSSN(),
                        folderBuilderFreqTest.getBorrowerCollaboratorId(),
                        folderBuilderFreqTest.getBorrowerCurrentEmployment(),
                        startDate,
                        endDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "731.20", "3,600.09")));
        getResponse = RestGetLoanData.getApplicationData(folderBuilderFreqTest.getFolderId());

        assertEquals(expectedFrequency, getResponse.getApplicant(folderBuilderFreqTest.getBorrowerFullName())
                .getIncome("W2", folderBuilderFreqTest.getBorrowerCurrentEmployment())
                .getPart("Base Pay")
                .getIncome(year).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getFrequency().getValue());
    }
}
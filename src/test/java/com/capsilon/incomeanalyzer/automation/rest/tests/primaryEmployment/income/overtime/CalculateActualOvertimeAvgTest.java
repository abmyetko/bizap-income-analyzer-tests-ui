package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.overtime;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.BIG_DECIMAL_PRECISION_EIGHT_POINTS;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.OVERTIME_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;


@SuppressWarnings({"unchecked", "rawtypes"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = OVERTIME_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "CalculateActualOvertimeAvgTest")
public class CalculateActualOvertimeAvgTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderOvertimeConventionalTest = createFolderBuilder("IAROvAVGcon");
    private final IAFolderBuilder folderBuilderOvertimeFHATest = createFolderBuilder("IAROvAVGfha");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderOvertimeConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderOvertimeFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]));
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder iaBuilder, MortgageType loanType) {
        iaBuilder.setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iaBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobBasePay(iaBuilder.getCoBorrowerSSN(), "0.10")
                .setPrimaryJobOvertime(iaBuilder.getCoBorrowerSSN(), "0.10")
                .restBuild();

        DataUploadObject dataUpload = createUploadObject(iaBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1200", "3600"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "3000")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "1200",
                        "3600",
                        "03/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1500")
                        .setPriorYearOvertime("2800")
                        .setTwoYearPriorOvertime("2500"))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "150", "1800")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "150", "1800")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "4000", "4000"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "125", "125")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1200", "3600"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "100", "1200")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "150", "1800")))
                .addDocument(dataUpload.createCustomEvoePrevious(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "1500",
                        "12000",
                        "02/28/" + YEAR_TO_DATE)
                        .setYtdOvertime("1600")
                        .setPriorYearOvertime("1700")
                        .setTwoYearPriorOvertime("1800"))

                .importDocumentList();

        RestGetLoanData.getApplicationData(iaBuilder.getFolderId());
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2090 Check Actual Overtime Avg For Current Job")
    void checkActualOvertimeAvgForCurrentJob(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("correct values od YTD Avg Overtime",
                () -> assertEquals(bigD(500, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for YEAR_TO_DATE for Borrower )"),
                () -> assertEquals(bigD(250, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for YEAR_TO_DATE for Borrower"),
                () -> assertEquals(bigD(233.33, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for ONE_YEAR_PRIOR for Borrower"),
                () -> assertEquals(bigD(250, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for ONE_YEAR_PRIOR for Borrower"),
                () -> assertEquals(bigD(208.33, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for TWO_YEARS_PRIOR for Borrower"),

                () -> assertEquals(bigD(200, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for YEAR_TO_DATE for CoBorrower )"),
                () -> assertEquals(bigD(125, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for YEAR_TO_DATE for CoBorrower"),
                () -> assertEquals(bigD(150, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for ONE_YEAR_PRIOR for CoBorrower"),
                () -> assertEquals(bigD(100, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for ONE_YEAR_PRIOR for CoBorrower"),
                () -> assertEquals(bigD(125, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getOvertime().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE)
                        .getMonthlyAmountAvg().getValue(), "Incorrect calculation for Overtime Actual YTD Avg (for TWO_YEARS_PRIOR for CoBorrower")
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2091 IA-2987 Check Actual Overtime Avg For PreviousJob. For Paystub and Evoe")
    void checkActualOvertimeAvgForPreviousJob(IAFolderBuilder folderBuilder) {
        RestIncomePart getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime();
        assertAll("correct values od YTD Avg Overtime",
                () -> assertEquals(bigD(250), bigD(getResponse.getIncome(YEAR_TO_DATE)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                        "Incorrect calculation for Overtime Actual YTD Avg (for YEAR_TO_DATE for Borrower )"),
                () -> assertEquals(bigD(150), bigD(getResponse.getIncome(ONE_YEAR_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                        "Incorrect calculation for Overtime Actual YTD Avg (for ONE_YEAR_PRIOR for Borrower"),
                () -> assertEquals(bigD(150), bigD(getResponse.getIncome(TWO_YEARS_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                        "Incorrect calculation for Overtime Actual YTD Avg (for TWO_YEARS_PRIOR for Borrower"),
                () -> assertEquals(bigD(133.33), bigD(getResponse.getIncome(YEAR_TO_DATE)
                                .getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                        "Incorrect calculation for Overtime Actual YTD Avg (for Year to date for Borrower"),
                () -> assertEquals(bigD(141.67), bigD(getResponse.getIncome(ONE_YEAR_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                        "Incorrect calculation for Overtime Actual YTD Avg (for Year to date for Borrower"),
                () -> assertEquals(bigD(150), bigD(getResponse.getIncome(TWO_YEARS_PRIOR)
                                .getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                        "Incorrect calculation for Overtime Actual YTD Avg (for Year to date for Borrower")
        );
    }
}

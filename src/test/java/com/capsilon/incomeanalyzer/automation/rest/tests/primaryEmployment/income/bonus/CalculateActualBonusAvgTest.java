package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.bonus;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;


@SuppressWarnings({"unchecked", "rawtypes"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = BONUS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "CalculateActualBonusAvgTest")
public class CalculateActualBonusAvgTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderBonusConventionalTest = createFolderBuilder("IARBoAVGcon");
    private final IAFolderBuilder folderBuilderBonusFHATest = createFolderBuilder("IARBoAVGfha");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderBonusConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderBonusFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]));
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder iaBuilder, MortgageType loanType) {
        iaBuilder.setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(iaBuilder.getCoBorrowerSSN(), "0.10")
                .setPrimaryJobBonus(iaBuilder.getCoBorrowerSSN(), "0.10")
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
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1200", "3600"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "3000")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750")))
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
                        .setYtdBonus("1500")
                        .setPriorYearBonus("2800")
                        .setTwoYearPriorBonus("2500"))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750"),
                        new PaystubData.PaystubIncomeRow(MONTHLY_BONUS, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1200", "3600"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "3000")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "150", "1800")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "4000", "4000"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "125", "125")))
                .addDocument(dataUpload.createCustomEVoeCurrent(
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
                        .setYtdBonus("200")
                        .setPriorYearBonus("1800")
                        .setTwoYearPriorBonus("1500"))
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
                        .setYtdBonus("1600")
                        .setPriorYearBonus("1700")
                        .setTwoYearPriorBonus("1800"))
                .importDocumentList();
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-1224 Check calculations of Actual Bonus Avg for current job (Gross Bonus / Months Worked)")
    void checkActualBonusAvgForCurrentJob(IAFolderBuilder folderBuilder) {
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestIncomePart getBonusBorrower = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBonus();

            RestIncomePart getBonusCoBorrower = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getCoBorrowerFullName())
                    .getIncome(folderBuilder.getCoBorrowerCurrentEmployment()).getBonus();

            assertAll("correct values od YTD Avg Bonus",
                    () -> assertEquals(bigD(250),
                            bigD(getBonusBorrower.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB YEAR_TO_DATE for Borrower )"),
                    () -> assertEquals(bigD(250),
                            bigD(getBonusBorrower.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB ONE_YEAR_PRIOR for Borrower )"),
                    () -> assertEquals(bigD(62.5),
                            bigD(getBonusBorrower.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB TWO_YEARS_PRIOR for Borrower )"),
                    () -> assertEquals(bigD(500),
                            bigD(getBonusBorrower.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for VOE YEAR_TO_DATE for Borrower )"),
                    () -> assertEquals(bigD(233.33),
                            bigD(getBonusBorrower.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for VOE ONE_YEAR_PRIOR for Borrower )"),
                    () -> assertEquals(bigD(208.33),
                            bigD(getBonusBorrower.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for VOE TWO_YEARS_PRIOR for Borrower )"),

                    () -> assertEquals(bigD(250),
                            bigD(getBonusCoBorrower.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB YEAR_TO_DATE for CoBorrower )"),
                    () -> assertEquals(bigD(150),
                            bigD(getBonusCoBorrower.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB ONE_YEAR_PRIOR for CoBorrower )"),
                    () -> assertEquals(bigD(10.42),
                            bigD(getBonusCoBorrower.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB TWO_YEARS_PRIOR for CoBorrower )"),
                    () -> assertEquals(bigD(200),
                            bigD(getBonusCoBorrower.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for EVOE YEAR_TO_DATE for CoBorrower )"),
                    () -> assertEquals(bigD(150),
                            bigD(getBonusCoBorrower.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for EVOE ONE_YEAR_PRIOR for CoBorrower )"),
                    () -> assertEquals(bigD(125),
                            bigD(getBonusCoBorrower.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for EVOE TWO_YEARS_PRIOR for CoBorrower )"));
        });
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-1231 IA-2975 Check calculations of Actual Bonus Avg for previous job. Paystub and Evoe")
    void checkActualBonusAvgForPreviousJob(IAFolderBuilder folderBuilder) {
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestIncomePart getBonusBorrower = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBonus();

            assertAll("correct values od YTD Avg Bonus",
                    () -> assertEquals(bigD(500), bigD(getBonusBorrower.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB)
                                    .getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB YEAR_TO_DATE for Borrower )"),
                    () -> assertEquals(bigD(250),
                            bigD(getBonusBorrower.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB ONE_YEAR_PRIOR for Borrower )"),
                    () -> assertEquals(bigD(62.5),
                            bigD(getBonusBorrower.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for PAYSTUB TWO_YEARS_PRIOR for Borrower )"),
                    () -> assertEquals(bigD(133.33),
                            bigD(getBonusBorrower.getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for EVOE year to date for Borrower )"),
                    () -> assertEquals(bigD(141.67),
                            bigD(getBonusBorrower.getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for EVOE one year prior for Borrower )"),
                    () -> assertEquals(bigD(150),
                            bigD(getBonusBorrower.getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.EVOE).getMonthlyAmountAvg().getValue()),
                            "Incorrect calculation for Bonus Actual YTD Avg (for EVOE two year prior for Borrower )"));
        });
    }
}

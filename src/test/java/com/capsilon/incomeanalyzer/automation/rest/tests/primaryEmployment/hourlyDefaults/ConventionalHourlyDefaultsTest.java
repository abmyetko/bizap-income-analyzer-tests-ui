package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.hourlyDefaults;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.data.upload.data.VoeCurrentData;
import com.capsilon.incomeanalyzer.automation.data.upload.data.W2Data;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponseApplicant;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncome;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.AnnualSummaryIncomeType.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@SuppressWarnings("rawtypes")
@Execution(CONCURRENT)
@ResourceLock(value = "ConventionalHourlyDefaultsTest")
class ConventionalHourlyDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderHourlyDefaults = createFolderBuilder("IARCovHrDef");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilderHourlyDefaults.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderHourlyDefaults);

        RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId());
    }

    @AfterEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    private Stream allValuesHourlyTestCases() { // w2YtdAmount pstbRate pstbHours pstbYtdAmount, voeRate, voeHours, voeYtdAmount, ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection
        return Stream.of(
                of(bigD(12000), bigD(250), bigD(4), bigD(1000), bigD(250), bigD(1), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(260), bigD(4), bigD(1000), bigD(250), bigD(1), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(260), bigD(4), bigD(1000), bigD(250), bigD(1), bigD(1100), PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(250), bigD(4), bigD(1100), bigD(250), bigD(1), bigD(1100), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(260), bigD(4), bigD(1100), bigD(250), bigD(1), bigD(1100), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),

                of(bigD(12000), bigD(260), bigD(4), bigD(3000), bigD(250), bigD(1), bigD(3000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(250), bigD(4), bigD(1000), bigD(250), bigD(1), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(240), bigD(4), bigD(960), bigD(250), bigD(1), bigD(1000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(250), bigD(4), bigD(960), bigD(250), bigD(1), bigD(960), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(300), bigD(4), bigD(5000), bigD(500), bigD(1), bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(260), bigD(4), bigD(1000), bigD(260), bigD(1), bigD(1200), PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR)
        );
    }

    private Stream oneMissingValueHourlyTestCases() { // w2YtdAmount pstbRate pstbHours pstbYtdAmount, voeRate, voeHours, voeYtdAmount, ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection
        return Stream.of(
                of(bigD(12000), bigD(260), bigD(4), bigD(3000), bigD(250), null, bigD(3000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(260), bigD(4), bigD(3000), bigD(250), bigD(1), null, PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(260), bigD(4), null, bigD(250), bigD(1), bigD(3000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),

                of(bigD(12000), null, bigD(4), bigD(1000), bigD(250), bigD(1), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(250), bigD(4), null, bigD(250), bigD(1), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(250), bigD(4), bigD(1000), bigD(250), null, bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),

                of(bigD(12000), bigD(240), bigD(4), null, bigD(250), bigD(1), bigD(1000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(240), bigD(4), bigD(960), bigD(250), null, bigD(1000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(240), bigD(4), bigD(960), bigD(250), bigD(1), null, PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),

                of(bigD(12000), null, bigD(4), bigD(960), bigD(250), bigD(1), bigD(960), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(250), bigD(4), null, bigD(250), bigD(1), bigD(960), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(250), bigD(4), bigD(960), bigD(250), null, bigD(960), VOE, AVG, BASEPAY_YTD_AVG),

                of(bigD(12000), null, bigD(4), bigD(5000), bigD(500), bigD(1), bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(300), bigD(4), null, bigD(500), bigD(1), bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(300), bigD(4), bigD(5000), bigD(500), null, bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),

                of(bigD(12000), null, bigD(4), bigD(1000), bigD(260), bigD(1), bigD(1200), PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(260), bigD(4), bigD(1000), bigD(260), null, bigD(1200), PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(260), bigD(4), bigD(1000), bigD(260), bigD(1), null, PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR)
        );
    }

    private Stream twoMissingValuesHourlyTestCases() { // w2YtdAmount pstbRate pstbHours pstbYtdAmount, voeRate, voeHours, voeYtdAmount, ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection
        return Stream.of(
                of(bigD(12000), null, bigD(4), bigD(3000), bigD(250), null, bigD(3000), NO_DOC, NONE, NONE_AVG),
                of(bigD(12000), bigD(260), bigD(4), null, bigD(250), bigD(1), null, NO_DOC, NONE, NONE_AVG),
                of(bigD(12000), bigD(260), bigD(4), null, bigD(250), null, bigD(3000), NO_DOC, NONE, NONE_AVG),
                of(bigD(12000), null, bigD(4), bigD(1000), bigD(250), bigD(1), null, NO_DOC, NONE, NONE_AVG)
        );
    }

    @ParameterizedTest
    @MethodSource("allValuesHourlyTestCases")
    @Description("IA-2400 Check If Hourly Income Defaults Are Selected Correctly For Documents With All Values")
    void checkIfHourlyIncomeDefaultsAreSelectedCorrectlyForDocumentsWithAllValues(BigDecimal w2YtdAmount,
                                                                                  BigDecimal pstbRate, BigDecimal pstbHours, BigDecimal pstbYtdAmount,
                                                                                  BigDecimal voeRate, BigDecimal voeHours, BigDecimal voeYtdAmount,
                                                                                  SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        setDocumentValuesAndUploadThemToFolder(w2YtdAmount, pstbRate, pstbHours, pstbYtdAmount, voeRate, voeHours, voeYtdAmount);
        checkExpectedCalculations(ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection);
    }

    @ParameterizedTest
    @MethodSource("oneMissingValueHourlyTestCases")
    @Description("IA-2402 Check If Hourly Income Defaults Are Selected Correctly For Documents With One Value Missing")
    void checkIfHourlyIncomeDefaultsAreSelectedCorrectlyForDocumentsWithOneValueMissing(BigDecimal w2YtdAmount,
                                                                                        BigDecimal pstbRate, BigDecimal pstbHours, BigDecimal pstbYtdAmount,
                                                                                        BigDecimal voeRate, BigDecimal voeHours, BigDecimal voeYtdAmount,
                                                                                        SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        setDocumentValuesAndUploadThemToFolder(w2YtdAmount, pstbRate, pstbHours, pstbYtdAmount, voeRate, voeHours, voeYtdAmount);
        checkExpectedCalculations(ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection);
    }

    @ParameterizedTest
    @MethodSource("twoMissingValuesHourlyTestCases")
    @Description("IA-2419 Check If Hourly Income Defaults Are Selected Correctly For Documents With Two Values Missing")
    void checkIfHourlyIncomeDefaultsAreSelectedCorrectlyForDocumentsWithTwoValuesMissing(BigDecimal w2YtdAmount,
                                                                                         BigDecimal pstbRate, BigDecimal pstbHours, BigDecimal pstbYtdAmount,
                                                                                         BigDecimal voeRate, BigDecimal voeHours, BigDecimal voeYtdAmount,
                                                                                         SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        setDocumentValuesAndUploadThemToFolder(w2YtdAmount, pstbRate, pstbHours, pstbYtdAmount, voeRate, voeHours, voeYtdAmount);
        checkExpectedCalculations(ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection);
    }

    @Test
    @Description("IA-2521 Check If Hourly Income Defaults Are Working If Ytd Has Only One Document Ia Present In Ytd")
    void checkIfHourlyIncomeDefaultsWorkIfOnlyOneDocumentIsPresentInYtd() {
        generateZeroIncomeDocs();
        ((VoeCurrentData) dataUpload.getDocumentList().get(1))
                .setAvgHoursPerWeek("1")
                .setCurrentGrossBasePayAmount("130")
                .setYtdBasePay("600")
                .setPriorYearBasePay("6000")
                .setTwoYearPriorBasePay("12000");
        dataUpload.importDocumentList();

        checkExpectedCalculations(VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR);
    }

    void checkExpectedCalculations(SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        RestGetResponseApplicant applicant = RestGetLoanData.getApplicationData(folderBuilderHourlyDefaults.getFolderId()).getApplicant(folderBuilderHourlyDefaults.getBorrowerFullName());
        RestPartIncome applicantCurrentJobYtdIncomes = applicant.getIncome(folderBuilderHourlyDefaults.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE);
        RestIncomeType applicantIncomeType = applicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeType(IncomePartType.BASE_PAY);

        checkDocumentExpectedSelections(ytdDefaultDoc, applicantCurrentJobYtdIncomes, ytdAnnualSummaryIncomeType);
        if (NONE_AVG.equals(haSelection)) {
            assertAll("Historical average selection for with none expected",
                    () -> assertFalse(applicantIncomeType.getAvgIncome(BASEPAY_YTD_AVG).getSelected(), "Ytd Historical Average was selected"),
                    () -> assertFalse(applicantIncomeType.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_YR).getSelected(), "Ytd + Prev Historical Average was selected"),
                    () -> assertFalse(applicantIncomeType.getAvgIncome(BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS).getSelected(), "Ytd + Two Years Prior Historical Average was selected")
            );
        } else {
            assertTrue(applicantIncomeType.getAvgIncome(haSelection).getSelected(), "Historical Average was not selected by default");
        }
    }

    void setDocumentValuesAndUploadThemToFolder(BigDecimal w2YtdAmount,
                                                BigDecimal pstbRate, BigDecimal pstbHours, BigDecimal pstbYtdAmount,
                                                BigDecimal voeRate, BigDecimal voeHours, BigDecimal voeYtdAmount) {
        generateZeroIncomeDocs();
        setIncomeForSelectedDoc(PAYSTUB, pstbRate, pstbHours, pstbYtdAmount);
        setIncomeForSelectedDoc(VOE, voeRate, voeHours, voeYtdAmount);
        setIncomeForSelectedDoc(W2, null, null, w2YtdAmount);
        dataUpload.importDocumentList();
    }

    void generateZeroIncomeDocs() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderHourlyDefaults.getBorrowerFullName(),
                        folderBuilderHourlyDefaults.getBorrowerSSN(),
                        folderBuilderHourlyDefaults.getBorrowerCollaboratorId(),
                        folderBuilderHourlyDefaults.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        (PaystubData.PaystubIncomeRow) null))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderHourlyDefaults.getBorrowerFullName(),
                        folderBuilderHourlyDefaults.getBorrowerSSN(),
                        folderBuilderHourlyDefaults.getBorrowerCollaboratorId(),
                        folderBuilderHourlyDefaults.getBorrowerCurrentEmployment(),
                        IncomeFrequency.HOURLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "0.00",
                        "0.00",
                        "01/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderHourlyDefaults.getBorrowerFullName(),
                        folderBuilderHourlyDefaults.getBorrowerSSN(),
                        folderBuilderHourlyDefaults.getBorrowerCollaboratorId(),
                        folderBuilderHourlyDefaults.getBorrowerCurrentEmployment(),
                        "00.00",
                        ONE_YEAR_PRIOR.toString()));
    }

    void setIncomeForSelectedDoc(SummaryDocumentType documentType, BigDecimal rate, BigDecimal hours, BigDecimal ytdAmount) {
        String rateString = rate == null ? "" : rate.toString();
        String hoursString = hours == null ? "" : hours.toString();
        String ytdAmountString = ytdAmount == null ? "" : ytdAmount.toString();

        String paystubPeriodIncome = "";
        if (rate != null && hours != null) {
            paystubPeriodIncome = rate.multiply(hours).toString();
        }

        switch (documentType) {
            case PAYSTUB:
                ((PaystubData) dataUpload.getDocumentList().get(0))
                        .addIncome(new PaystubData.PaystubIncomeRow(REGULAR, "", "40", paystubPeriodIncome, ytdAmountString));
                break;
            case VOE:
                ((VoeCurrentData) dataUpload.getDocumentList().get(1))
                        .setAvgHoursPerWeek(hoursString)
                        .setCurrentGrossBasePayAmount(rateString)
                        .setYtdBasePay(ytdAmountString);
                break;
            case W2:
                ((W2Data) dataUpload.getDocumentList().get(2))
                        .setWagesTipsOtherCompensation(ytdAmountString);
                break;
            default:
                break;
        }
    }

    void checkDocumentExpectedSelections(SummaryDocumentType documentType, RestPartIncome incomes, AnnualSummaryIncomeType expectedDef) {
        List<RestPartIncomeAnnualSummary> documentList = new ArrayList<>();
        switch (documentType) {
            case VOE:
                documentList.add(incomes.getAnnualSummaryDocument(VOE));
                break;
            case PAYSTUB:
                documentList.add(incomes.getAnnualSummaryDocument(PAYSTUB));
                break;
            case NO_DOC:
                documentList.add(incomes.getAnnualSummaryDocument(VOE));
                documentList.add(incomes.getAnnualSummaryDocument(PAYSTUB));
                break;
            default:
                fail("Invalid doc type was used, please check if additional case must be added");
                break;
        }

        switch (expectedDef) {
            case CALCULATED:
                assertTrue(documentList.get(0).getMonthlyAmountCalculated().getSelected(), "Expected projected monthly income selection");
                assertFalse(documentList.get(0).getMonthlyAmountAvg().getSelected(), "Not expected ytd income selection");
                assertTrue(documentList.get(0).getSelected(), "Document was not selected for calculation");
                break;
            case AVG:
                assertFalse(documentList.get(0).getMonthlyAmountCalculated().getSelected(), "Not expected projected monthly income selection");
                assertTrue(documentList.get(0).getMonthlyAmountAvg().getSelected(), "Expected ytd income selection");
                assertTrue(documentList.get(0).getSelected(), "Document was not selected for calculation");
                break;
            case NONE:
                for (RestPartIncomeAnnualSummary document : documentList) {
                    assertFalse(document.getMonthlyAmountCalculated().getSelected(), "Not expected projected monthly income selection for document: " + document.getDocType());
                    assertFalse(document.getMonthlyAmountAvg().getSelected(), "Not expected ytd income selection for document: " + document.getDocType());
                    assertFalse(document.getSelected(), "Document was selected for calculation for document: " + document.getDocType());
                }
                break;
            default:
                fail("Expected incorrect default selection");
                break;
        }
    }
}

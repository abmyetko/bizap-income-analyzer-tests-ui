package com.capsilon.incomeanalyzer.automation.rest.tests.secondaryEmployment.folder.defaults;

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
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@SuppressWarnings("rawtypes")
@DisplayName("Secondary job defaults test for Conventional Salaried income")
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondarySalariedConventionalDefaultsTest")
class SecondarySalariedConventionalDefaultsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderSecSalCovDef = createFolderBuilder("IARSecCSDef");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilderSecSalCovDef.generateSecondaryJobsLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderSecSalCovDef);

        RestGetLoanData.getApplicationData(folderBuilderSecSalCovDef.getFolderId());
    }

    @BeforeEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    private Stream allValuesSalariedTestCases() { // prevW2YtdAmount twoPrevW2YtdAmount pstbMonthyAmount pstbYtdAmount, voeBasePayAmount, voeYtdAmount, ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection
        return Stream.of(
                of(bigD(12000), bigD(12000), bigD(1040), bigD(3000), bigD(1100), bigD(3000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(12000), bigD(1000), bigD(1000), bigD(1100), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(12111), bigD(12000), bigD(1040), bigD(1040), bigD(1040), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12111), bigD(12222), bigD(1040), bigD(1040), bigD(1040), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(12000), bigD(1200), bigD(5000), bigD(2100), bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(12000), bigD(12111), bigD(1040), bigD(1010), bigD(1040), bigD(1200), PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR)
        );
    }

    private Stream oneMissingValueSalariedTestCases() { // prevW2YtdAmount twoPrevW2YtdAmount pstbMonthyAmount pstbYtdAmount, voeBasePayAmount, voeYtdAmount, ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection
        return Stream.of(
                of(bigD(12000), bigD(12000), bigD(1040), null, bigD(1100), bigD(3000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(12000), bigD(1040), bigD(3000), null, bigD(3000), PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),
                of(bigD(12000), bigD(12000), bigD(1040), bigD(3000), bigD(1100), null, PAYSTUB, CALCULATED, BASEPAY_YTD_AVG),

                of(bigD(12000), bigD(12000), null, bigD(1000), bigD(1100), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(12000), bigD(12000), bigD(1000), null, bigD(1100), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(12000), bigD(12000), bigD(1000), bigD(1000), null, bigD(1000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),

                of(bigD(12111), bigD(12000), bigD(1040), null, bigD(1040), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12111), bigD(12000), bigD(1040), bigD(1040), null, bigD(1000), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12111), bigD(12000), bigD(1040), bigD(1040), bigD(1040), null, PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),

                of(bigD(12111), bigD(12222), null, bigD(1040), bigD(1040), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12111), bigD(12222), bigD(1040), null, bigD(1040), bigD(1000), VOE, AVG, BASEPAY_YTD_AVG),
                of(bigD(12111), bigD(12222), bigD(1040), bigD(1040), null, bigD(1000), VOE, AVG, BASEPAY_YTD_AVG),

                of(bigD(12000), bigD(12000), null, bigD(5000), bigD(2100), bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(12000), bigD(12000), bigD(1200), null, bigD(2100), bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),
                of(bigD(12000), bigD(12000), bigD(1200), bigD(5000), null, bigD(2000), VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_TWO_YRS),

                of(bigD(12000), bigD(12111), null, bigD(1010), bigD(1040), bigD(1200), PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(12111), bigD(1040), bigD(1010), null, bigD(1200), PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR),
                of(bigD(12000), bigD(12111), bigD(1040), bigD(1010), bigD(1040), null, PAYSTUB, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR)
        );
    }

    private Stream twoMissingValuesSalariedTestCases() {
        return Stream.of(
                of(bigD(12000), bigD(12000), null, bigD(3000), null, bigD(3000), NO_DOC, NONE, NONE_AVG),
                of(bigD(12000), bigD(12000), bigD(1040), null, bigD(1000), null, NO_DOC, NONE, NONE_AVG),
                of(bigD(12000), bigD(12000), bigD(1040), null, null, bigD(3000), NO_DOC, NONE, NONE_AVG),
                of(bigD(12000), bigD(12000), null, bigD(1000), bigD(1000), null, NO_DOC, NONE, NONE_AVG)
        );
    }

    @ParameterizedTest
    @MethodSource("allValuesSalariedTestCases")
    @Description("IA-2578 Check If Secondary Job Salaried Income Defaults Are Selected Correctly For Documents With All Values")
    void checkIfSecondarySalariedIncomeDefaultsAreSelectedCorrectlyForDocumentsWithAllValues(BigDecimal prevW2YtdAmount, BigDecimal twoPrevW2YtdAmount,
                                                                                             BigDecimal pstbPeriodAmount, BigDecimal pstbYtdAmount,
                                                                                             BigDecimal voePeriodAmount, BigDecimal voeYtdAmount,
                                                                                             SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        setDocumentvaluesAndUploadThemToFolder(prevW2YtdAmount, twoPrevW2YtdAmount, pstbPeriodAmount, pstbYtdAmount, voePeriodAmount, voeYtdAmount);
        checkExpectedCalculations(ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection);
    }

    @ParameterizedTest
    @MethodSource("oneMissingValueSalariedTestCases")
    @Description("IA-2578 Check If Secondary Job Salaried Income Defaults Are Selected Correctly For Documents With One Value Missing")
    void checkIfSecondarySalariedIncomeDefaultsAreSelectedCorrectlyForDocumentsWithOneValueMissing(BigDecimal prevW2YtdAmount, BigDecimal twoPrevW2YtdAmount,
                                                                                                   BigDecimal pstbPeriodAmount, BigDecimal pstbYtdAmount,
                                                                                                   BigDecimal voePeriodAmount, BigDecimal voeYtdAmount,
                                                                                                   SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        setDocumentvaluesAndUploadThemToFolder(prevW2YtdAmount, twoPrevW2YtdAmount, pstbPeriodAmount, pstbYtdAmount, voePeriodAmount, voeYtdAmount);
        checkExpectedCalculations(ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection);
    }

    @ParameterizedTest
    @MethodSource("twoMissingValuesSalariedTestCases")
    @Description("IA-2578 Check If Secondary Job Salaried Income Defaults Are Selected Correctly For Documents With Two Values Missing")
    void checkIfSecondarySalariedIncomeDefaultsAreSelectedCorrectlyForDocumentsWithTwoValuesMissing(BigDecimal prevW2YtdAmount, BigDecimal twoPrevW2YtdAmount,
                                                                                                    BigDecimal pstbPeriodAmount, BigDecimal pstbYtdAmount,
                                                                                                    BigDecimal voePeriodAmount, BigDecimal voeYtdAmount,
                                                                                                    SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        setDocumentvaluesAndUploadThemToFolder(prevW2YtdAmount, twoPrevW2YtdAmount, pstbPeriodAmount, pstbYtdAmount, voePeriodAmount, voeYtdAmount);
        checkExpectedCalculations(ytdDefaultDoc, ytdAnnualSummaryIncomeType, haSelection);
    }

    @Test
    @Description("IA-2578 Check If Secondary Job Salaried Income Defaults Are Working If Ytd Has Only One Document Ia Present In Ytd")
    void checkIfSecondarySalariedIncomeDefaultsWorkIfOnlyOneDocumentIsPresentInYtd() {
        generateZeroIncomeDocs();

        ((VoeCurrentData) dataUpload.getDocumentList().get(1))
                .setCurrentGrossBasePayAmount("590")
                .setYtdBasePay("600")
                .setPriorYearBasePay("6000")
                .setTwoYearPriorBasePay("12000");
        dataUpload.importDocumentList();

        checkExpectedCalculations(VOE, AVG, BASEPAY_YTD_AVG_PLUS_PREV_YR);
    }

    void checkExpectedCalculations(SummaryDocumentType ytdDefaultDoc, AnnualSummaryIncomeType ytdAnnualSummaryIncomeType, IncomeAvg haSelection) {
        RestGetResponseApplicant applicant = RestGetLoanData.getApplicationData(folderBuilderSecSalCovDef.getFolderId()).getApplicant(folderBuilderSecSalCovDef.getBorrowerFullName());
        RestPartIncome applicantCurrentJobYtdIncomes = applicant.getIncome(folderBuilderSecSalCovDef.getBorrowerSecondEmployment()).getBasePay().getIncome(YEAR_TO_DATE);
        RestIncomeType applicantIncomeType = applicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeType(IncomePartType.BASE_PAY);

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

    void setDocumentvaluesAndUploadThemToFolder(BigDecimal prevW2YtdAmount, BigDecimal twoPrevW2YtdAmount,
                                                BigDecimal pstbPeriodAmount, BigDecimal pstbYtdAmount,
                                                BigDecimal voePeriodAmount, BigDecimal voeYtdAmount) {
        generateZeroIncomeDocs();
        setIncomeForSelectedDoc(PAYSTUB, pstbPeriodAmount, pstbYtdAmount);
        setIncomeForSelectedDoc(VOE, voePeriodAmount, voeYtdAmount);
        setIncomeForSelectedDoc(W2, null, prevW2YtdAmount, ONE_YEAR_PRIOR);
        setIncomeForSelectedDoc(W2, null, twoPrevW2YtdAmount, TWO_YEARS_PRIOR);
        dataUpload.importDocumentList();
    }

    void generateZeroIncomeDocs() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecSalCovDef.getBorrowerFullName(),
                        folderBuilderSecSalCovDef.getBorrowerSSN(),
                        folderBuilderSecSalCovDef.getBorrowerCollaboratorId(),
                        folderBuilderSecSalCovDef.getBorrowerSecondEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        (PaystubData.PaystubIncomeRow) null))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderSecSalCovDef.getBorrowerFullName(),
                        folderBuilderSecSalCovDef.getBorrowerSSN(),
                        folderBuilderSecSalCovDef.getBorrowerCollaboratorId(),
                        folderBuilderSecSalCovDef.getBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "0.00",
                        "0.00",
                        "01/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderSecSalCovDef.getBorrowerFullName(),
                        folderBuilderSecSalCovDef.getBorrowerSSN(),
                        folderBuilderSecSalCovDef.getBorrowerCollaboratorId(),
                        folderBuilderSecSalCovDef.getBorrowerSecondEmployment(),
                        "00.00",
                        ONE_YEAR_PRIOR.toString()))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderSecSalCovDef.getBorrowerFullName(),
                        folderBuilderSecSalCovDef.getBorrowerSSN(),
                        folderBuilderSecSalCovDef.getBorrowerCollaboratorId(),
                        folderBuilderSecSalCovDef.getBorrowerSecondEmployment(),
                        "00.00",
                        TWO_YEARS_PRIOR.toString()));
    }

    void setIncomeForSelectedDoc(SummaryDocumentType documentType, BigDecimal periodAmount, BigDecimal ytdAmount) {
        setIncomeForSelectedDoc(documentType, periodAmount, ytdAmount, null);
    }

    void setIncomeForSelectedDoc(SummaryDocumentType documentType, BigDecimal periodAmount, BigDecimal ytdAmount, Integer w2Year) {
        String ytdAmountString = ytdAmount == null ? "" : ytdAmount.toString();
        String periodAmountString = periodAmount == null ? "" : periodAmount.toString();

        switch (documentType) {
            case PAYSTUB:
                ((PaystubData) dataUpload.getDocumentList().get(0))
                        .addIncome(new PaystubData.PaystubIncomeRow(REGULAR, "", "", periodAmountString, ytdAmountString));
                break;
            case VOE:
                ((VoeCurrentData) dataUpload.getDocumentList().get(1))
                        .setCurrentGrossBasePayAmount(periodAmountString)
                        .setYtdBasePay(ytdAmountString);
                break;
            case W2:
                if (!TWO_YEARS_PRIOR.equals(w2Year)) {
                    ((W2Data) dataUpload.getDocumentList().get(2))
                            .setWagesTipsOtherCompensation(ytdAmountString);
                } else {
                    ((W2Data) dataUpload.getDocumentList().get(3))
                            .setWagesTipsOtherCompensation(ytdAmountString);
                }
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

package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.qualifyingIncomeTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.data.upload.data.VoeCurrentData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncome;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "CappedHoursTest")
public class CappedHoursTest extends TestBaseRest {

    private static final String INCOME_PROJECTED = "projected";
    private static final String INCOME_ACTUAL = "actual";
    private static final String INCOME_NONE = "none";
    private static final BigDecimal CAPPED_HOURS = new BigDecimal(69).setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal NOT_CAPPED_HOURS = new BigDecimal(40).setScale(2, RoundingMode.HALF_UP);
    private final IAFolderBuilder folderBuilderCappedHoursTest = createFolderBuilder("IARestCap");

    @BeforeAll
    void setupTestFolder() {
        folderBuilderCappedHoursTest.generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderCappedHoursTest);

        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomW2(
                        folderBuilderCappedHoursTest.getBorrowerFullName(),
                        folderBuilderCappedHoursTest.getBorrowerSSN(),
                        folderBuilderCappedHoursTest.getBorrowerCollaboratorId(),
                        folderBuilderCappedHoursTest.getBorrowerCurrentEmployment(),
                        "24000.00",
                        ONE_YEAR_PRIOR.toString()));
        //Line used to break test execution in case of changed API response
        RestGetLoanData.getApplicationData(folderBuilderCappedHoursTest.getFolderId());
    }

    @BeforeEach
    void removeJsonDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    private Stream oneDocCappedHoursTestCases() { // DocType rate ytd Hours ExpectedDef
        return Stream.of(
                of(VOE, new BigDecimal("11.53"), new BigDecimal(2000), CAPPED_HOURS, INCOME_ACTUAL),
                of(VOE, new BigDecimal("11.53"), new BigDecimal(10000), CAPPED_HOURS, INCOME_PROJECTED),
                of(VOE, new BigDecimal("11.53"), new BigDecimal(0), CAPPED_HOURS, INCOME_NONE),
                of(PAYSTUB, new BigDecimal("11.53"), new BigDecimal(2000), CAPPED_HOURS, INCOME_ACTUAL),
                of(PAYSTUB, new BigDecimal("11.53"), new BigDecimal(10000), CAPPED_HOURS, INCOME_PROJECTED),
                of(PAYSTUB, new BigDecimal("11.53"), new BigDecimal(0), CAPPED_HOURS, INCOME_NONE)
        );
    }

    private Stream twoDocsCappedHoursTestCases() { // PaystubRate PaystubYtd VoeRate VoeYtd PaystubHours VoeHours ExpectedDoc ExpectedDef
        return Stream.of(
                of(new BigDecimal("10.53"), new BigDecimal(0), new BigDecimal("11.53"), new BigDecimal(10000), CAPPED_HOURS, CAPPED_HOURS, PAYSTUB, INCOME_PROJECTED),
                of(new BigDecimal("10.53"), new BigDecimal(0), new BigDecimal("11.53"), new BigDecimal(1000), CAPPED_HOURS, CAPPED_HOURS, VOE, INCOME_ACTUAL),
                of(new BigDecimal("12.53"), new BigDecimal(0), new BigDecimal("11.53"), new BigDecimal(10000), CAPPED_HOURS, CAPPED_HOURS, VOE, INCOME_PROJECTED),
                of(new BigDecimal("10.53"), new BigDecimal(8000), new BigDecimal("11.53"), new BigDecimal(10000), CAPPED_HOURS, CAPPED_HOURS, PAYSTUB, INCOME_PROJECTED),
                of(new BigDecimal("10.53"), new BigDecimal(1000), new BigDecimal("11.53"), new BigDecimal(10000), CAPPED_HOURS, CAPPED_HOURS, PAYSTUB, INCOME_ACTUAL),
                of(new BigDecimal("10.53"), new BigDecimal(2000), new BigDecimal("11.53"), new BigDecimal(1000), CAPPED_HOURS, CAPPED_HOURS, VOE, INCOME_ACTUAL),
                of(new BigDecimal("12.53"), new BigDecimal(8000), new BigDecimal("11.53"), new BigDecimal(10000), CAPPED_HOURS, CAPPED_HOURS, VOE, INCOME_PROJECTED)
        );
    }

    @Test
    void checkIfManualFrequencyWillCapHours() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomPaystub(
                        folderBuilderCappedHoursTest.getCoBorrowerFullName(),
                        folderBuilderCappedHoursTest.getCoBorrowerSSN(),
                        folderBuilderCappedHoursTest.getCoBorrowerCollaboratorId(),
                        folderBuilderCappedHoursTest.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "100.50", "160.00", "296.00", "1,308.20"))
                        .setPayDate("03/31/" + YEAR_TO_DATE)
                        .setManualFrequency(IncomeFrequency.WEEKLY));

        RestPartIncomeAnnualSummary paystub = RestGetLoanData.getApplicationData(folderBuilderCappedHoursTest.getFolderId())
                .getApplicant(folderBuilderCappedHoursTest.getCoBorrowerFullName())
                .getIncome(folderBuilderCappedHoursTest.getCoBorrowerCurrentEmployment())
                .getBasePay()
                .getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertAll(String.format("Asserting capped hours for manual frequency"),
                () -> assertEquals(IncomeFrequency.WEEKLY, paystub.getFrequency().getValue()),
                () -> assertEquals(ValueType.MANUAL, paystub.getFrequency().getValueType()),
                () -> assertTrue(paystub.getHours().getCappedHours()),
                () -> assertEquals(bigD(160), bigD(paystub.getHours().getOriginalValue())),
                () -> assertEquals(bigD(40), bigD(paystub.getHours().getValue()))
        );
    }


    @ParameterizedTest(name = "{index} {0} capped defaults for Rate: {1} Hours: {3} Year to date income: {2} Expected default: {4}")
    @MethodSource("oneDocCappedHoursTestCases")
    @Description("IA-1729 IA-1806 Check Defaults For One Ytd Document With Capped Hours")
    void checkDefaultsForOneYtdDocumentWithCappedHours(SummaryDocumentType documentType, BigDecimal rate, BigDecimal ytdAmount, BigDecimal hours, String expectedDef) {
        generateZeroIncomeDocs();
        setIncomeForSelectedDoc(documentType, rate, hours, ytdAmount);

        dataUpload.importDocumentList();
        RestPartIncome incomes = RestGetLoanData.getApplicationData(folderBuilderCappedHoursTest.getFolderId())
                .getApplicant(folderBuilderCappedHoursTest.getBorrowerFullName())
                .getIncome(folderBuilderCappedHoursTest.getBorrowerCurrentEmployment())
                .getBasePay()
                .getIncome(YEAR_TO_DATE);

        checkDocumentExpectedSelections(documentType, incomes, hours, expectedDef);
    }

    @ParameterizedTest(name = "{index} Two documents capped defaults. Paystub rate: {0} hours: {4} ytd: {1} Voe rate: {2} hours: {5} ytd: {3}. Expected document {6} with {7} income selected")
    @MethodSource("twoDocsCappedHoursTestCases")
    @Description("IA-1805 Check Defaults For Two Ytd Document With Capped Hours")
    void checkDefaultsForTwoYtdDocumentWithCappedHours(BigDecimal paystubRate, BigDecimal paystubYtd, BigDecimal voeRate, BigDecimal voeYtd,
                                                       BigDecimal paystubHours, BigDecimal voeHours, SummaryDocumentType expectedDocumentSelection, String expectedDefaultSelection) {
        generateZeroIncomeDocs();
        setIncomeForSelectedDoc(PAYSTUB, paystubRate, paystubHours, paystubYtd);
        setIncomeForSelectedDoc(VOE, voeRate, voeHours, voeYtd);

        dataUpload.importDocumentList();
        RestPartIncome incomes = RestGetLoanData.getApplicationData(folderBuilderCappedHoursTest.getFolderId())
                .getApplicant(folderBuilderCappedHoursTest.getBorrowerFullName())
                .getIncome(folderBuilderCappedHoursTest.getBorrowerCurrentEmployment())
                .getBasePay()
                .getIncome(YEAR_TO_DATE);

        checkDocumentExpectedSelections(expectedDocumentSelection, incomes, expectedDocumentSelection == PAYSTUB ? paystubHours : voeHours, expectedDefaultSelection);
    }


    void generateZeroIncomeDocs() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderCappedHoursTest.getBorrowerFullName(),
                        folderBuilderCappedHoursTest.getBorrowerSSN(),
                        folderBuilderCappedHoursTest.getBorrowerCollaboratorId(),
                        folderBuilderCappedHoursTest.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/07/" + YEAR_TO_DATE,
                        (PaystubData.PaystubIncomeRow) null))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderCappedHoursTest.getBorrowerFullName(),
                        folderBuilderCappedHoursTest.getBorrowerSSN(),
                        folderBuilderCappedHoursTest.getBorrowerCollaboratorId(),
                        folderBuilderCappedHoursTest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.HOURLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "0.00",
                        "0.00",
                        "03/31/" + YEAR_TO_DATE));
    }

    private void setIncomeForSelectedDoc(SummaryDocumentType documentType, BigDecimal rate, BigDecimal hours, BigDecimal ytdAmount) {
        switch (documentType) {
            case PAYSTUB:
                ((PaystubData) dataUpload.getDocumentList().get(0))
                        .addIncome(new PaystubData.PaystubIncomeRow(REGULAR, rate.toString(), hours.toString(), ytdAmount.toString(), ytdAmount.toString()));
                break;
            case VOE:
                ((VoeCurrentData) dataUpload.getDocumentList().get(1))
                        .setAvgHoursPerWeek(hours.toString())
                        .setCurrentGrossBasePayAmount(rate.toString())
                        .setYtdBasePay(ytdAmount.toString());
                break;
            default:
                break;
        }
    }

    void checkDocumentExpectedSelections(SummaryDocumentType documentType, RestPartIncome incomes, BigDecimal hours, String expectedDef) {
        RestPartIncomeAnnualSummary document = documentType == VOE ? incomes.getAnnualSummaryDocument(VOE) : incomes.getAnnualSummaryDocument(PAYSTUB);

        if (hours.compareTo(new BigDecimal(40)) > 0) {
            assertAll(String.format("Asserting capped hours for: %s", documentType),
                    () -> assertEquals(IncomeType.HOURLY, document.getType().getValue()),
                    () -> assertTrue(document.getHours().getCappedHours()),
                    () -> assertEquals(bigD(hours), bigD(document.getHours().getOriginalValue())),
                    () -> assertEquals(bigD(40), bigD(document.getHours().getValue()))
            );
        } else {
            assertAll(String.format("Asserting not capped hours for: %s", documentType),
                    () -> assertEquals(IncomeType.HOURLY, document.getType().getValue()),
                    () -> assertFalse(document.getHours().getCappedHours()),
                    () -> assertNull(document.getHours().getOriginalValue()),
                    () -> assertEquals(hours, bigD(document.getHours().getValue()))
            );
        }

        switch (expectedDef) {
            case INCOME_PROJECTED:
                assertTrue(document.getMonthlyAmountCalculated().getSelected(), "Expected projected monthly income selection");
                assertFalse(document.getMonthlyAmountAvg().getSelected(), "Not expected ytd income selection");
                assertTrue(document.getSelected(), "Document was not selected for calculation");
                break;
            case INCOME_ACTUAL:
                assertFalse(document.getMonthlyAmountCalculated().getSelected(), "Not expected projected monthly income selection");
                assertTrue(document.getMonthlyAmountAvg().getSelected(), "Expected ytd income selection");
                assertTrue(document.getSelected(), "Document was not selected for calculation");
                break;
            case INCOME_NONE:
                assertFalse(document.getMonthlyAmountCalculated().getSelected(), "Not expected projected monthly income selection");
                assertFalse(document.getMonthlyAmountAvg().getSelected(), "Not expected ytd income selection");
                assertFalse(document.getSelected(), "Document was selected for calculation");
                break;
            default:
                fail("Expected incorrect default selection");
                break;
        }
    }
}

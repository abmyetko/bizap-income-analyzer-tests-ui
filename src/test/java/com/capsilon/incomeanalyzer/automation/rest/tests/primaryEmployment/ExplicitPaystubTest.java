package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncome;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.PAYSTUB;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("iaCanonical")
@Execution(CONCURRENT)
@ResourceLock(value = "ExplicitPaystubTest")
public class ExplicitPaystubTest extends TestBaseRest {

    private final String defaultExplicitFrequency = "Biweekly";
    Integer counter = 0;
    private IAFolderBuilder folderBuilderExplFreqTest = createFolderBuilder("IARestExp");

    @BeforeAll
    void setupTestFolder() {
        folderBuilderExplFreqTest.setSignDate("01/01/" + YEAR_TO_DATE)
                .setCoBorrowerCurrentEmployment(folderBuilderExplFreqTest.getBorrowerCurrentEmployment());
        folderBuilderExplFreqTest.generateLoanDocument().restBuild();

        dataUpload = createUploadObject(folderBuilderExplFreqTest);
    }

    @Test
    @Description("IA-1669 IA-1691 IA-2342 IA-2315 Check If Paystub Explicit Frequency Overrides Other Document Frequencies")
    void checkIfPaystubExplicitFrequencyOverridesOtherDocumentFrequencies() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderExplFreqTest.getCoBorrowerFullName(),
                        folderBuilderExplFreqTest.getCoBorrowerSSN(),
                        folderBuilderExplFreqTest.getCoBorrowerCollaboratorId(),
                        folderBuilderExplFreqTest.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "12.00", "260", "10.00", "1000.00")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderExplFreqTest.getCoBorrowerFullName(),
                        folderBuilderExplFreqTest.getCoBorrowerSSN(),
                        folderBuilderExplFreqTest.getCoBorrowerCollaboratorId(),
                        folderBuilderExplFreqTest.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "03/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "2600",
                        "8000",
                        "03/31/" + YEAR_TO_DATE))
                .importDocumentList();

        RestIncomeType incomeTypeBasePay = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId())
                .getApplicant(folderBuilderExplFreqTest.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay();

        assertEquals(IncomeType.SALARIED, IncomeType.valueOf(incomeTypeBasePay.getPaymentType()));

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderExplFreqTest.getCoBorrowerFullName(),
                        folderBuilderExplFreqTest.getCoBorrowerSSN(),
                        folderBuilderExplFreqTest.getCoBorrowerCollaboratorId(),
                        folderBuilderExplFreqTest.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "333.33", "1000.00"))
                        .setPayType(IncomeType.HOURLY))
                .importDocumentList();

        incomeTypeBasePay = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId())
                .getApplicant(folderBuilderExplFreqTest.getCoBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay();
        RestPartIncomeAnnualSummary paystubDocument = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId())
                .getApplicant(folderBuilderExplFreqTest.getCoBorrowerFullName()).getIncome(folderBuilderExplFreqTest.getCoBorrowerCurrentEmployment())
                .getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(PAYSTUB);

        assertEquals(IncomeType.HOURLY, IncomeType.valueOf(incomeTypeBasePay.getPaymentType()));
        assertEquals(IncomeType.HOURLY, paystubDocument.getType().getValue());
        assertEquals(ValueType.EXPLICIT, ValueType.valueOf(paystubDocument.getType().getValueType()));
    }

    @Test
    @Description("IA-1669 IA-1691 IA-2342 Check If Frequency Is Unknown For Wrong Dates No Manual And No Explicit Frequency")
    void checkIfFrequencyIsUnknownForWrongDatesNoManualAndNoExplicitFrequency() {
        uploadExplicitPaystub(
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-22",
                YEAR_TO_DATE + "-03-22",
                null, null);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(UNKNOWN, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    UNKNOWN,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(CALCULATED, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    CALCULATED,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Description("IA-1669 IA-1691 IA-2342 Check If Frequency Is Explicit For Wrong Dates And Only Explicit Frequency")
    void checkIfFrequencyIsExplicitForWrongDatesAndOnlyExplicitFrequency() {
        uploadExplicitPaystub(
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-22",
                YEAR_TO_DATE + "-03-22",
                null, defaultExplicitFrequency);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(BI_WEEKLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    BI_WEEKLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(ValueType.EXPLICIT, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    ValueType.EXPLICIT,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Description("IA-1669 IA-1691 IA-2342 Check If Frequency Is Explicit For Explicit Frequency")
    void checkIfFrequencyIsExplicitForExplicitFrequency() {
        uploadExplicitPaystub(
                null,
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                null, defaultExplicitFrequency);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(BI_WEEKLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    BI_WEEKLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(ValueType.EXPLICIT, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    ValueType.EXPLICIT,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Tag("integration")
    @Description("IA-1669 IA-1691 IA-2342 Check If Frequency Is Manual For Wrong Dates Manual And Explicit Frequency")
    void checkIfFrequencyIsManualForWrongDatesManualAndExplicitFrequency() {
        uploadExplicitPaystub(
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-22",
                YEAR_TO_DATE + "-03-22",
                WEEKLY, defaultExplicitFrequency);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(WEEKLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    WEEKLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(ValueType.MANUAL, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    ValueType.MANUAL,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Description("IA-1669 IA-1691 IA-2342 Check If Frequency Is Manual For Manual And Explicit Frequency")
    void checkIfFrequencyIsManualForManualAndExplicitFrequency() {
        uploadExplicitPaystub(
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                WEEKLY, defaultExplicitFrequency);

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(WEEKLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    WEEKLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(MANUAL, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    MANUAL,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Description("IA-2955 Check if monthly explicit frequency is chosen when manual is semi-monthly")
    void checkIfMonthlyExplicitFrequencyIsChosenWhenManualIsSemiMonthly() {
        uploadExplicitPaystub(
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-15",
                YEAR_TO_DATE + "-03-15",
                null, "Monthly");

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(MONTHLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    MONTHLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(EXPLICIT, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    EXPLICIT,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Description("IA-2955 Check if monthly explicit frequency is chosen when manual is monthly")
    void checkIfMonthlyExplicitFrequencyIsChosenWhenManualIsMonthly() {
        uploadExplicitPaystub(
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                null, "Monthly");

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(MONTHLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    MONTHLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(EXPLICIT, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    EXPLICIT,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Description("IA-2955 Check if Monthly calculated frequency is chosen when explicit is weekly")
    void checkIfMonthlyCalculatedFrequencyIsChosenWhenExplicitIsWeekly() {
        uploadExplicitPaystub(
                YEAR_TO_DATE + "-03-01",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                null, "Weekly");

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(MONTHLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    MONTHLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(CALCULATED, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    CALCULATED,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    @Test
    @Description("IA-2955 Check if Hourly explicit frequency is chosen when calculated is Unknown")
    void checkIfHourlyExplicitFrequencyIsChosenWhenCalculatedIsUnknown() {
        uploadExplicitPaystub(
                "",
                YEAR_TO_DATE + "-03-31",
                YEAR_TO_DATE + "-03-31",
                null, "Hourly");

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilderExplFreqTest.getFolderId());
            RestPartIncome income = response.getApplicant(folderBuilderExplFreqTest.getBorrowerFullName())
                    .getIncome(folderBuilderExplFreqTest.getBorrowerCurrentEmployment())
                    .getBasePay().getIncome(YEAR_TO_DATE);
            assertAll("Asserting correct frequency for explicit paystub: " + folderBuilderExplFreqTest.getFolderId(),
                    () -> assertEquals(HOURLY, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue(),
                            String.format("Paystub has incorrect frequency value expected: %s actual: %s",
                                    HOURLY,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValue())),
                    () -> assertEquals(EXPLICIT, income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType(),
                            String.format("Paystub has incorrect frequency type expected: %s actual: %s",
                                    EXPLICIT,
                                    income.getAnnualSummaryDocument(PAYSTUB).getFrequency().getValueType()))
            );
        });
    }

    void uploadExplicitPaystub(String startDate, String endDate, String payDate, IncomeFrequency manualFrequency, String explicitFrequency) {
        String originalDataSourceId = "Data-source-to-change-freq-test-" + counter++;
        RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
        RestCanonicalDocument canonicalDocument = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderExplFreqTest.getFolderId(),
                "/sampleCanonicalDocuments/ExtractedExplicitPaystub.json");

        canonicalDocumentMethods.setBorrowerAndJob(canonicalDocument, folderBuilderExplFreqTest.getFolderId(),
                folderBuilderExplFreqTest.getBorrowerFullName(),
                folderBuilderExplFreqTest.getBorrowerCurrentEmployment());
        canonicalDocumentMethods.setDatesForSelectedDocument(canonicalDocument,
                startDate,
                endDate,
                payDate,
                PAYSTUB);
        canonicalDocument.setDataSourceId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().setId(originalDataSourceId);
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getEmployment().setEmploymentIncomeFrequencyType(manualFrequency != null ? manualFrequency.text : null);
        canonicalDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getEmployment().setEmploymentIncomeExplicitFrequencyType(explicitFrequency);
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());
    }
}

package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeType;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentBody;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory.W2;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.BONUS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Period end date format test")
@Execution(CONCURRENT)
@ResourceLock(value = "PeriodEndDateUiFormatTest")
class PeriodEndDateUiFormatTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIPEndDat");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iafolderBuilder
                .setBorrowerYearsOnThisJob("3")
                .setBorrowerPreviousEmployment("Previous Computer Inc")
                .setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("06/30/" + YEAR_TO_DATE)

                .setCoBorrowerYearsOnThisJob("3")
                .setCoBorrowerPreviousEmployment("Previous Blue Inc")
                .setCoBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setCoBorrowerPreviousJobEndDate("06/30/" + YEAR_TO_DATE)
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);
        refreshFolder();
    }

    @BeforeEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    @Test
    @Order(1)
    void checkIfPeriodEndDateForPaystubsAreInCorrectFormatForCurrentJob() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("12/31/" + TWO_YEARS_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("12/31/" + ONE_YEAR_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("01/15/" + YEAR_TO_DATE))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromCurrentJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

            assertAll("Current job Paystub Period End Date format",
                    () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromCurrentJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromCurrentJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, allDataFromCurrentJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB)
                            .getDocumentValuesMap().get("Period End Date")));
        });
    }

    @Test
    @Order(2)
    void checkIfPeriodEndDateForPaystubsAreInCorrectFormatForPreviousJob() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("12/31/" + TWO_YEARS_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("12/31/" + ONE_YEAR_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerPreviousEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500.06", "", "2500.05", "30000.06"))
                        .setPayDate("01/15/" + YEAR_TO_DATE))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertAll("Previous job Paystub Period End Date format",
                    () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromPreviousJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromPreviousJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, allDataFromPreviousJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB)
                            .getDocumentValuesMap().get("Period End Date")));
        });
    }

    @Test
    @Order(3)
    @Description("IA-2216 IA-2206 Check If Voe Period End Date Has Correct Format For All Years In Current Job")
    void checkIfPeriodEndDateForVoeAreInCorrectFormatForCurrentJob() {
        dataUpload.importDocument((dataUpload.createCustomVoeCurrent(
                iafolderBuilder.getBorrowerFullName(),
                iafolderBuilder.getBorrowerSSN(),
                iafolderBuilder.getBorrowerCollaboratorId(),
                iafolderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.MONTHLY,
                "10/01/" + TWO_YEARS_PRIOR,
                "03/31/" + YEAR_TO_DATE,
                "2800",
                "24000",
                "03/31/" + YEAR_TO_DATE))
                .setPriorYearBasePay("26000")
                .setTwoYearPriorBasePay("20000"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromCurrentJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

            assertAll("Current job VOE Period End Date format",
                    () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromCurrentJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromCurrentJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromCurrentJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")));
        });
    }

    @Test
    @Order(4)
    @Description("IA-2216 Check If Voe Period End Date Has Correct Format For All Years In Previous Job")
    void checkIfPeriodEndDateForVoeAreInCorrectFormatForPreviousJob() {
        //VOE in ONE_YEAR_PRIOR
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                iafolderBuilder.getBorrowerFullName(),
                iafolderBuilder.getBorrowerSSN(),
                iafolderBuilder.getBorrowerCollaboratorId(),
                iafolderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.WEEKLY,
                "01/01/" + THREE_YEARS_PRIOR,
                "12/31/" + ONE_YEAR_PRIOR,
                "1200",
                "0",
                "0",
                "0"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromPreviousJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap()
                    .get("Period End Date"));
        });

        //VOE in YTD
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                iafolderBuilder.getBorrowerFullName(),
                iafolderBuilder.getBorrowerSSN(),
                iafolderBuilder.getBorrowerCollaboratorId(),
                iafolderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.WEEKLY,
                "01/01/" + THREE_YEARS_PRIOR,
                "03/31/" + YEAR_TO_DATE,
                "1200",
                "0",
                "0",
                "0"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertEquals("3/31/" + YEAR_TO_DATE.toString(), allDataFromPreviousJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap()
                    .get("Period End Date"));
        });
    }

    @Test
    @Order(5)
    @Description("IA-2204 Check If Evoe Period End Date Has Correct Format For All Years In Current Job")
    void checkIfPeriodEndDateForEvoeAreInCorrectFormatForCurrentJob() {
        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                iafolderBuilder.getCoBorrowerFullName(),
                iafolderBuilder.getCoBorrowerSSN(),
                iafolderBuilder.getCoBorrowerCollaboratorId(),
                iafolderBuilder.getCoBorrowerCurrentEmployment(),
                IncomeFrequency.MONTHLY,
                "10/01/" + TWO_YEARS_PRIOR,
                "03/31/" + YEAR_TO_DATE,
                "2800",
                "24000",
                "03/31/" + YEAR_TO_DATE)
                .setPriorYearBasePay("26000")
                .setTwoYearPriorBasePay("20000"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromCurrentJob = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY;

            assertAll("Previous job VOE Period End Date format",
                    () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromCurrentJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromCurrentJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromCurrentJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")));
        });
    }

    @Test
    @Order(6)
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @Description("IA-1442 Check If Voe Period End Date Has Correct Format For All Years In Bonus Current Job")
    void checkIfPeriodEndDateForVoeAreInCorrectFormatForBonusCurrentJob() {
        //VOE in ONE_YEAR_PRIOR
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "04/01/" + YEAR_TO_DATE,
                        "04/30/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500", "", "2500", "30000"),
                        new PaystubData.PaystubIncomeRow(BONUS, "2500", "", "500", "1500"))
                        .setPayDate("04/30/" + YEAR_TO_DATE))
                .addDocument((dataUpload.createCustomVoeCurrent(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        "2800",
                        "24000",
                        "12/31/" + ONE_YEAR_PRIOR))
                        .setSignDate("12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setYtdBonus("5000")
                        .setPriorYearBonus("12000"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromCurrentJob =
                    applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BONUS)
                            .BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

            assertAll("Current job VOE Period End Date format",
                    () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromCurrentJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromCurrentJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")));
        });

        //VOE in YEAR_TO_DATE
        dataUpload.importDocument((dataUpload.createCustomVoeCurrent(
                iafolderBuilder.getBorrowerFullName(),
                iafolderBuilder.getBorrowerSSN(),
                iafolderBuilder.getBorrowerCollaboratorId(),
                iafolderBuilder.getBorrowerCurrentEmployment(),
                IncomeFrequency.MONTHLY,
                "10/01/" + TWO_YEARS_PRIOR,
                "03/31/" + YEAR_TO_DATE,
                "2800",
                "24000",
                "03/31/" + YEAR_TO_DATE))
                .setPriorYearBasePay("26000")
                .setTwoYearPriorBasePay("20000")
                .setYtdBonus("5000")
                .setPriorYearBonus("13000")
                .setTwoYearPriorBonus("2000"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromCurrentJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

            assertAll("Current job VOE Period End Date format",
                    () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromCurrentJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromCurrentJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromCurrentJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE)
                            .getDocumentValuesMap().get("Period End Date")));
        });
    }

    @Test
    @Order(7)
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @Description("IA-1442 Check If Voe Period End Date Has Correct Format For All Years In Bonus Previous Job")
    void checkIfPeriodEndDateForVoeAreInCorrectFormatForBonusPreviousJob() {
        //VOE in ONE_YEAR_PRIOR
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                iafolderBuilder.getBorrowerFullName(),
                iafolderBuilder.getBorrowerSSN(),
                iafolderBuilder.getBorrowerCollaboratorId(),
                iafolderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.MONTHLY,
                "01/01/" + THREE_YEARS_PRIOR,
                "12/31/" + ONE_YEAR_PRIOR,
                "1200",
                "0",
                "0",
                "5000"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromPreviousJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap()
                    .get("Period End Date"));
        });

        //VOE in YTD
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                iafolderBuilder.getBorrowerFullName(),
                iafolderBuilder.getBorrowerSSN(),
                iafolderBuilder.getBorrowerCollaboratorId(),
                iafolderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.WEEKLY,
                "01/01/" + THREE_YEARS_PRIOR,
                "03/31/" + YEAR_TO_DATE,
                "1200",
                "0",
                "0",
                "5000"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertEquals("3/31/" + YEAR_TO_DATE.toString(), allDataFromPreviousJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap()
                    .get("Period End Date"));
        });
    }

    @Test
    @Order(8)
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @Description("IA-1442 Check If Evoe Period End Date Has Correct Format For Bonus All Years In Current Job")
    void checkIfPeriodEndDateForEvoeAreInCorrectFormatForBonusCurrentJob() {
        //EVOE in One Year Prior
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2500", "", "2500", "30000"),
                        new PaystubData.PaystubIncomeRow(BONUS, "2500", "", "500", "1500"))
                        .setPayDate("03/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        "2800",
                        "24000",
                        "12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setYtdBonus("5000")
                        .setPriorYearBonus("12000"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromCurrentJob = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY;

            assertAll("Previous job VOE Period End Date format",
                    () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, allDataFromCurrentJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromCurrentJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")));
        });

        //EVOE in YTD
        dataUpload.importDocument(dataUpload.createCustomEVoeCurrent(
                iafolderBuilder.getCoBorrowerFullName(),
                iafolderBuilder.getCoBorrowerSSN(),
                iafolderBuilder.getCoBorrowerCollaboratorId(),
                iafolderBuilder.getCoBorrowerCurrentEmployment(),
                IncomeFrequency.MONTHLY,
                "10/01/" + TWO_YEARS_PRIOR,
                "03/31/" + YEAR_TO_DATE,
                "2800",
                "24000",
                "03/31/" + YEAR_TO_DATE)
                .setPriorYearBasePay("26000")
                .setTwoYearPriorBasePay("20000")
                .setYtdBonus("8000")
                .setPriorYearBonus("13000")
                .setTwoYearPriorBonus("6000"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromCurrentJob = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                    .onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY;

            assertAll("Previous job VOE Period End Date format",
                    () -> assertEquals("3/31/" + YEAR_TO_DATE, allDataFromCurrentJob.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromCurrentJob.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromCurrentJob.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE)
                            .getDocumentValuesMap().get("Period End Date")));
        });
    }

    @Test
    @Order(9)
    @Description("IA-2272 checks if Semi monthly text is shown correctly in Pay Frequency field for EVOE document")
    void checkIfSemiMonthlyLabelIsVisibleInPayFrequencyForSalariedEvoe() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.SEMI_MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "2800",
                        "24000",
                        "03/31/" + YEAR_TO_DATE))
                .importDocumentList();
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView()
                .incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY
                .employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE)
                .getDocumentLocatorsMap().get("Pay Frequency").shouldHave(Condition.text("Semi-monthly"));
    }

    @Test
    @Order(10)
    @Description("IA-2272 checks if Semi monthly text is shown correctly in Pay Frequency field for VOE document")
    void checkIfSemiMonthlyLabelIsVisibleInPayFrequencyForVoe() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.SEMI_MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "2800",
                        "24000",
                        "03/31/" + YEAR_TO_DATE))
                .importDocumentList();
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView()
                .incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY
                .employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE)
                .getDocumentLocatorsMap().get("Pay Frequency").shouldHave(Condition.text("Semi-monthly"));
    }

    @Test
    @Order(11)
    @Description("IA-2272 checks if Semi monthly text is shown correctly in Pay Frequency field for PayStub document")
    void checkIfSemiMonthlyLabelIsVisibleInPayFrequencyForPayStub() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "12/16/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2500", "30000"))
                        .setPayDate("03/31/" + YEAR_TO_DATE))
                .importDocumentList();
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView()
                .incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY
                .employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB)
                .getDocumentLocatorsMap().get("Pay Frequency").shouldHave(Condition.text("Semi-monthly"));
    }

    @Test
    @Order(12)
    @Description("IA-2272 checks if Semi monthly text is shown correctly in Pay Frequency field for EVOE document")
    void checkIfSemiMonthlyAndHourlyLabelIsVisibleInPayFrequencyForHourlyEvoe() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.SEMI_MONTHLY,
                        "10/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "2800",
                        "24000",
                        "03/31/" + YEAR_TO_DATE)
                        .setPayRateFrequency(IncomeFrequency.HOURLY)
                        .setAvgHoursPerPeriod("80").setRate("12"))
                .importDocumentList();
        ApplicantView applicantView = new ApplicantView();
        applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView()
                .incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY
                .employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE)
                .getDocumentLocatorsMap().get("Pay Frequency").shouldHave(Condition.text("Semi-monthly"));
    }

    @Test
    @Order(13)
    @Description("IA-2981 check if date format for previous evoe is correct")
    void checkIfDataFormatForPreviousEVOEIsCorrect() {
        BigDecimal evoeMonthlyAmount = bigD(1500);

        dataUpload.removeDocumentsFromFolder().clearDocuments()
                .addDocument(dataUpload.createCustomEvoePrevious(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerPreviousEmployment(),
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "12000",
                        "02/28/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdBonus(evoeMonthlyAmount.toString())
                        .setPriorYearBonus(evoeMonthlyAmount.toString())
                        .setTwoYearPriorBonus(evoeMonthlyAmount.toString())
                        .setYtdCommission(evoeMonthlyAmount.toString())
                        .setPriorYearCommission(evoeMonthlyAmount.toString())
                        .setTwoYearPriorCommission(evoeMonthlyAmount.toString())
                        .setYtdOvertime(evoeMonthlyAmount.toString())
                        .setPriorYearOvertime(evoeMonthlyAmount.toString())
                        .setTwoYearPriorOvertime(evoeMonthlyAmount.toString()))
                .importDocumentList();

        ApplicantView view = new ApplicantView();

        IncomeCategoryIncomeType incomeType = view.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView();
        EmploymentBody basepay = incomeType.incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getCoBorrowerPreviousEmployment()).BODY;
        EmploymentBody overtime = incomeType.incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getCoBorrowerPreviousEmployment()).BODY;
        EmploymentBody commissions = incomeType.incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getCoBorrowerPreviousEmployment()).BODY;
        EmploymentBody bonus = incomeType.incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getCoBorrowerPreviousEmployment()).BODY;

        Map<String, String> bonusYtd = bonus.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> bonusOneYearPrior = bonus.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> bonusTwoYearsPrior = bonus.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        Map<String, String> basepayYtd = basepay.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> basepayOneYearPrior = basepay.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> baespayTwoYearsPrior = basepay.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        Map<String, String> overtimeYtd = overtime.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> overtimeOneYearPrior = overtime.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> overtimeTwoYearsPrior = overtime.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        Map<String, String> commissionsYtd = commissions.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> commissionsOneYearPrior = commissions.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> commissionsTwoYearsPrior = commissions.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        assertAll("Check past evoe period end date for bonus income",
                () -> assertEquals("12/31/" + YEAR_TO_DATE.toString(), bonusYtd.get("Period End Date"),
                        "Previous job bonus ytd evoe has wrong period end date"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), bonusOneYearPrior.get("Period End Date"),
                        "Previous job bonus prior year evoe has wrong period end date"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), bonusTwoYearsPrior.get("Period End Date"),
                        "Previous job bonus two years prior evoe has wrong period end date"),
                () -> assertEquals("12/31/" + YEAR_TO_DATE.toString(), commissionsYtd.get("Period End Date"),
                        "Previous job commissions ytd evoe has wrong period end date"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), commissionsOneYearPrior.get("Period End Date"),
                        "Previous job commissions prior year evoe has wrong period end date"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), commissionsTwoYearsPrior.get("Period End Date"),
                        "Previous job commissions two years prior evoe has wrong period end date"),
                () -> assertEquals("12/31/" + YEAR_TO_DATE.toString(), basepayYtd.get("Period End Date"),
                        "Previous job base pay ytd evoe has wrong period end date"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), basepayOneYearPrior.get("Period End Date"),
                        "Previous job base pay prior year evoe has wrong period end date"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), baespayTwoYearsPrior.get("Period End Date"),
                        "Previous job base pay two years prior evoe has wrong period end date"),
                () -> assertEquals("12/31/" + YEAR_TO_DATE.toString(), overtimeYtd.get("Period End Date"),
                        "Previous job overtime ytd evoe has wrong period end date"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), overtimeOneYearPrior.get("Period End Date"),
                        "Previous job overtime prior year evoe has wrong period end date"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), overtimeTwoYearsPrior.get("Period End Date"),
                        "Previous job overtime two years prior evoe has wrong period end date"));
    }

    @Test
    @Order(14)
    @Description("IA-2981 Check if past job Evoe period end date has correct format for all years in previous job")
    void checkIfPeriodEndDateForPastEVOEAreInCorrectFormatForPreviousJob() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomEvoePrevious(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerPreviousEmployment(),
                        "09/01/" + THREE_YEARS_PRIOR,
                        "03/15/" + YEAR_TO_DATE,
                        "1203",
                        "12345",
                        "08/01/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("60891.07")
                        .setTwoYearPriorBasePay("62748.58")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromBasePayPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName())
                    .incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY)
                    .BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertAll("Previous job Evoe period end date format",
                    () -> assertEquals("3/15/" + YEAR_TO_DATE, allDataFromBasePayPreviousJob.employmentYear(YEAR_TO_DATE.toString())
                                    .document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Period End Date"),
                            "Borrower previous job YEAR_TO_DATE Evoe period end date should equal 3/15/" + YEAR_TO_DATE),
                    () -> assertEquals(ONE_YEAR_PRIOR.toString(), allDataFromBasePayPreviousJob.employmentYear(ONE_YEAR_PRIOR.toString())
                                    .document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Period End Date"),
                            "Borrower previous job ONE_YEAR_PRIOR Evoe period end date should equal " + ONE_YEAR_PRIOR),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromBasePayPreviousJob.employmentYear(TWO_YEARS_PRIOR.toString())
                                    .document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Period End Date"),
                            "Borrower previous job TWO_YEARS_PRIOR Evoe period end date should equal " + TWO_YEARS_PRIOR));
        });
    }

    @Test
    @Order(15)
    @Description("IA-2981 Check if past job Evoe period end date has correct format for two previous years")
    void checkIfPeriodEndDateForPastEVOEAreInCorrectFormatForTwoPreviousYears() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomEvoePrevious(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerPreviousEmployment(),
                        "09/01/" + THREE_YEARS_PRIOR,
                        "03/15/" + ONE_YEAR_PRIOR,
                        "1203",
                        "12345",
                        "08/01/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("60891.07")
                        .setTwoYearPriorBasePay("62748.58")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromBasePayPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName())
                    .incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY)
                    .BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertAll("Previous job EVOE period end date format",
                    () -> assertEquals("3/15/" + ONE_YEAR_PRIOR, allDataFromBasePayPreviousJob.employmentYear(ONE_YEAR_PRIOR.toString())
                                    .document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Period End Date"),
                            "Borrower previous job ONE_YEAR_PRIOR Evoe period end date should equal 3/15/" + ONE_YEAR_PRIOR),
                    () -> assertEquals(TWO_YEARS_PRIOR.toString(), allDataFromBasePayPreviousJob.employmentYear(TWO_YEARS_PRIOR.toString())
                                    .document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Period End Date"),
                            "Borrower previous job TWO_YEARS_PRIOR Evoe period end date should equal " + TWO_YEARS_PRIOR));
        });
    }

    @Test
    @Order(16)
    @Description("IA-2981 Check if past job Evoe period end date has correct format for last year")
    void checkIfPeriodEndDateForPastEVOEAreInCorrectFormatForLastYear() {
        dataUpload.clearDocuments()
                .importDocument(dataUpload.createCustomEvoePrevious(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerPreviousEmployment(),
                        "09/01/" + THREE_YEARS_PRIOR,
                        "03/15/" + TWO_YEARS_PRIOR,
                        "1203",
                        "12345",
                        "08/01/" + TWO_YEARS_PRIOR)
                        .setPriorYearBasePay("60891.07")
                        .setTwoYearPriorBasePay("62748.58")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            ApplicantView applicantView = new ApplicantView();
            EmploymentBody allDataFromBasePayPreviousJob = applicantView.applicant(iafolderBuilder.getBorrowerFullName())
                    .incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY)
                    .BODY.employment(iafolderBuilder.getBorrowerPreviousEmployment()).BODY;

            assertEquals("3/15/" + TWO_YEARS_PRIOR, allDataFromBasePayPreviousJob.employmentYear(TWO_YEARS_PRIOR.toString())
                            .document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Period End Date"),
                    "Borrower previous job TWO_YEARS_PRIOR Evoe period end date should equal 3/15/" + TWO_YEARS_PRIOR);
        });
    }
}
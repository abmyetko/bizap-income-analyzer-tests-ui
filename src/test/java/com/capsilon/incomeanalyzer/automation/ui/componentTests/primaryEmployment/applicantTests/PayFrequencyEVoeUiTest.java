package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentBody;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.BI_WEEKLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.MONTHLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Pay frequency Evoe Previous years test")
@Execution(CONCURRENT)
@ResourceLock(value = "PayFrequencyEVoeUiTest")
class PayFrequencyEVoeUiTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUIFreqEVO");

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iafolderBuilder
                .setBorrowerYearsOnThisJob("3")
                .setCoBorrowerYearsOnThisJob("3")
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        MONTHLY,
                        "05/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        "2800",
                        "24000",
                        "12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        BI_WEEKLY,
                        "07/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        "1200",
                        "1200",
                        "12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getCoBorrowerFullName(),
                        iafolderBuilder.getCoBorrowerSSN(),
                        iafolderBuilder.getCoBorrowerCollaboratorId(),
                        iafolderBuilder.getCoBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();

        refreshFolder();
    }

    @Test
    @Description("IA-1991 Checking pay frequency for EVOE in previous years")
    void checkFrequencyForEvoeForPreviousYears() {
        ApplicantView applicantView = new ApplicantView();
        EmploymentBody dataFromEvoe = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getBorrowerCurrentEmployment()).BODY;

        assertAll("Pay frequency for EVOE assertions",
                () -> assertEquals("—", dataFromEvoe.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Pay Frequency")),
                () -> assertEquals("—", dataFromEvoe.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap().get("Pay Frequency"))
        );
    }

    @Test
    @Description("IA-1991 Checking pay frequency for VOE in previous years")
    void checkFrequencyForVoeAndEvoeInTwoYearsPrior() {
        ApplicantView applicantView = new ApplicantView();
        EmploymentBody dataFromVoe = applicantView.applicant(iafolderBuilder.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(iafolderBuilder.getCoBorrowerCurrentEmployment()).BODY;

        assertAll("Pay frequency for EVOE assertions",
                () -> assertEquals("—", dataFromVoe.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap().get("Pay Frequency")),
                () -> assertEquals("—", dataFromVoe.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap().get("Pay Frequency"))
        );
    }
}

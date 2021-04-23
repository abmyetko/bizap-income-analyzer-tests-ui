package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.ApplicantIncomeCategory;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.IncomeTypeSection;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.IncomeTypeSectionHeader;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentBody;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.EmploymentYear;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.CheckBox;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.StringUtilities;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.interactions.Actions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.restoreApplicantDefaults;
import static com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage.getNumberOfApplicants;
import static com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentHeader.aliasTooltipComponent;
import static com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView.collapseOrExpandTab;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory.W2;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Applicant specification")
@Execution(CONCURRENT)
@ResourceLock(value = "ApplicantsTest")
class ApplicantsTest extends TestBaseUI {

    private final BigDecimal paystubYTDOvertimeAmount = bigD(12000);
    private final BigDecimal voeMonthlyOvertimeAmount = bigD(1000);
    private final BigDecimal evoeMonthlyAmount = bigD(1500);
    IAFolderBuilder folderBuilderApplicantsTest = createFolderBuilder("IAApplicant");
    private Boolean documentsShouldBeUploaded = true;
    RestGetResponse data;

    private static Stream sectionCases() {
        return Stream.of(//IncomePart, featureToggle, borrowerExpectedSelection, coBorrowerExpectedSelection
                of(IncomePartType.BASE_PAY, null, false, true),
                of(IncomePartType.OVERTIME, OVERTIME_TOGGLE, false, true),
                of(IncomePartType.COMMISSIONS, COMMISSIONS_TOGGLE, true, true),
                of(IncomePartType.BONUS, BONUS_TOGGLE, true, false)
        );
    }

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        folderBuilderApplicantsTest.generateLoanDocumentWithNoIncome();
        folderBuilderApplicantsTest.addLoanDocumentTypeOfIncome(folderBuilderApplicantsTest.getBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "1.10");
        if (RestGetLoanData.getActuatorFeatureToggleValue(BONUS_TOGGLE.value)) {
            folderBuilderApplicantsTest.addLoanDocumentTypeOfIncome(folderBuilderApplicantsTest.getBorrowerSSN(), LoanDocumentTypeOfIncome.BONUS, "1.10");
        }
        folderBuilderApplicantsTest.addLoanDocumentTypeOfIncome(folderBuilderApplicantsTest.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.BASE, "1.10");
        folderBuilderApplicantsTest.addLoanDocumentTypeOfIncome(folderBuilderApplicantsTest.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.OVERTIME, "1.10");
        folderBuilderApplicantsTest.addLoanDocumentTypeOfIncome(folderBuilderApplicantsTest.getCoBorrowerSSN(), LoanDocumentTypeOfIncome.COMMISSIONS, "1.10");
        loginCreateLoanAndGoToIncomeAnalyzerBizapp(folderBuilderApplicantsTest.uiBuild());

        dataUpload = createUploadObject(folderBuilderApplicantsTest);
        data = RestGetLoanData.getApplicationData(folderBuilderApplicantsTest.getFolderId());
        refreshFolder();
    }

    @BeforeEach
    void restoreDefaults() {
        IncomeAnalyzerPage.closeOverlayBackdrop();
        restoreApplicantDefaults(data.getApplicant(folderBuilderApplicantsTest.getBorrowerFullName()).getId());
        restoreApplicantDefaults(data.getApplicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).getId());
    }

    @BeforeEach
    void uploadDataAfterLoanDocExpandTabTest(TestInfo testInfo) {
        if (documentsShouldBeUploaded &&
                !"checkIfLoanDocIncomeTypeWithValueExpandsTabCorrectlyBeforeAnyChanges".equals(testInfo.getTestMethod().orElse(testInfo.getClass().getDeclaredMethods()[0]).getName()) &&
                !"checkIfEmploymentYearCheckboxesChangeStateOnDocumentAddOrRemoval".equals(testInfo.getTestMethod().orElse(testInfo.getClass().getDeclaredMethods()[0]).getName())) {
            dataUpload.clearDocuments()
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerCurrentEmployment(),
                            "12/01/" + YEAR_TO_DATE,
                            "12/31/" + YEAR_TO_DATE,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                            new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerCurrentEmployment(),
                            "12/01/" + ONE_YEAR_PRIOR,
                            "12/31/" + ONE_YEAR_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                            new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerCurrentEmployment(),
                            "12/01/" + TWO_YEARS_PRIOR,
                            "12/31/" + TWO_YEARS_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                            new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                    .addDocument(dataUpload.createCustomVoeCurrent(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerCurrentEmployment(),
                            IncomeFrequency.MONTHLY,
                            "01/01/" + THREE_YEARS_PRIOR,
                            "12/31/" + YEAR_TO_DATE,
                            "2600",
                            "590",
                            "01/07/" + YEAR_TO_DATE)
                            .setPriorYearBasePay("14000")
                            .setTwoYearPriorBasePay("13000")
                            .setYtdOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setPriorYearOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setTwoYearPriorOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setYtdCommission("1500")
                            .setPriorYearCommission("18000")
                            .setTwoYearPriorCommission("18000"))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerPreviousEmployment(),
                            "12/01/" + YEAR_TO_DATE,
                            "12/31/" + YEAR_TO_DATE,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                            new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerPreviousEmployment(),
                            "12/01/" + ONE_YEAR_PRIOR,
                            "12/31/" + ONE_YEAR_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                            new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerPreviousEmployment(),
                            "12/01/" + TWO_YEARS_PRIOR,
                            "12/31/" + TWO_YEARS_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                            new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                    .addDocument(dataUpload.createCustomVoePrevious(
                            folderBuilderApplicantsTest.getBorrowerFullName(),
                            folderBuilderApplicantsTest.getBorrowerSSN(),
                            folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getBorrowerPreviousEmployment(),
                            IncomeFrequency.MONTHLY,
                            "01/01/" + THREE_YEARS_PRIOR,
                            "12/31/" + YEAR_TO_DATE,
                            "1500",
                            voeMonthlyOvertimeAmount.toString(),
                            "12000",
                            "00"))
                    .addDocument(dataUpload.createCustomEVoeCurrent(
                            folderBuilderApplicantsTest.getCoBorrowerFullName(),
                            folderBuilderApplicantsTest.getCoBorrowerSSN(),
                            folderBuilderApplicantsTest.getCoBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment(),
                            IncomeFrequency.MONTHLY,
                            "01/01/" + THREE_YEARS_PRIOR,
                            "12/31/" + YEAR_TO_DATE,
                            "2600",
                            "590",
                            "01/07/" + YEAR_TO_DATE)
                            .setRate("2600")
                            .setPriorYearBasePay("14000")
                            .setTwoYearPriorBasePay("13000")
                            .setYtdOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setPriorYearOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setTwoYearPriorOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setYtdCommission("1500")
                            .setPriorYearCommission("18000")
                            .setTwoYearPriorCommission("18000"))
                    .addDocument(dataUpload.createCustomEvoePrevious(
                            folderBuilderApplicantsTest.getCoBorrowerFullName(),
                            folderBuilderApplicantsTest.getCoBorrowerSSN(),
                            folderBuilderApplicantsTest.getCoBorrowerCollaboratorId(),
                            folderBuilderApplicantsTest.getCoBorrowerPreviousEmployment(),
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
            refreshFolder();
            documentsShouldBeUploaded = false;
        }
    }

    @ParameterizedTest
    @MethodSource("sectionCases")
    @Order(0)
    @Description("IA-2396 IA-2509 Check If Tabs Are Expanded Correctly When LoanDoc Has That Income Type With Values Greater Than Zero")
    void checkIfLoanDocIncomeTypeWithValueExpandsTabCorrectlyBeforeAnyChanges(IncomePartType partType, PropertyToggles featureToggle, Boolean borrowerSelection, Boolean coBorrowerSelection) {
        Assumptions.assumeTrue(featureToggle != null ? RestGetLoanData.getActuatorFeatureToggleValue(featureToggle.value) : true);
        ApplicantView view = new ApplicantView();

        ApplicantIncomeCategory borrowerIncomeCategory = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2);
        if (borrowerSelection)
            assertTrue(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should be expanded", partType));
        else
            assertFalse(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should not be expanded", partType));


        ApplicantIncomeCategory coBorrowerIncomeCategory = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2);
        if (coBorrowerSelection)
            assertTrue(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should be expanded", partType));
        else
            assertFalse(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should be expanded", partType));


        view.applicant(folderBuilderApplicantsTest.getBorrowerFullName());
        if (borrowerSelection)
            assertTrue(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should be expanded", partType));
        else
            assertFalse(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should not be expanded", partType));
    }

    @Test
    @Order(1)
    @Description("IA-2763 Check If EmploymentYear Checkboxes Change State On Document Add Or Removal")
    void checkIfEmploymentYearCheckboxesChangeStateOnDocumentAddOrRemoval() {
        EmploymentBody currentJobBody = new ApplicantView()
                .applicant(folderBuilderApplicantsTest.getBorrowerFullName())
                .incomeCategory(W2)
                .onlyPrimaryJobView()
                .incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY;
        IncomeTypeSectionHeader incomeTypeHeader = new ApplicantView()
                .applicant(folderBuilderApplicantsTest.getBorrowerFullName())
                .incomeCategory(W2)
                .onlyPrimaryJobView()
                .incomeType(BASE_PAY).HEADER;

        incomeTypeHeader.CHECKBOX.setCheckboxValue(true);
        currentJobBody.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsDisabled();
        currentJobBody.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsDisabled();
        currentJobBody.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsDisabled();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderApplicantsTest.getBorrowerFullName(),
                        folderBuilderApplicantsTest.getBorrowerSSN(),
                        folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                        folderBuilderApplicantsTest.getBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderApplicantsTest.getBorrowerFullName(),
                        folderBuilderApplicantsTest.getBorrowerSSN(),
                        folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                        folderBuilderApplicantsTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderApplicantsTest.getBorrowerFullName(),
                        folderBuilderApplicantsTest.getBorrowerSSN(),
                        folderBuilderApplicantsTest.getBorrowerCollaboratorId(),
                        folderBuilderApplicantsTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200")))
                .importDocumentList();

        incomeTypeHeader.CHECKBOX.setCheckboxValue(true);
        currentJobBody.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsEnabled();
        currentJobBody.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsEnabled();
        currentJobBody.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsEnabled();

        dataUpload.removeDocumentsFromFolder();

        incomeTypeHeader.CHECKBOX.setCheckboxValue(true);
        currentJobBody.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsDisabled();
        currentJobBody.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsDisabled();
        currentJobBody.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsDisabled();
    }

    @ParameterizedTest
    @MethodSource("sectionCases")
    @Order(2)
    @Description("IA-2396 IA-2509 Check If Tabs Are Expanded Correctly When Tabs are collapsed/expanded and then folder is refreshed")
    void checkIfLoanDocIncomeTypeWithValueExpandsTabCorrectlyAfterChangesAndRefresh(IncomePartType partType, PropertyToggles featureToggle, Boolean borrowerSelection) {
        Assumptions.assumeTrue(featureToggle != null ? RestGetLoanData.getActuatorFeatureToggleValue(featureToggle.value) : true);
        ApplicantView view = new ApplicantView();

        ApplicantIncomeCategory borrowerIncomeCategory = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2);
        if (borrowerSelection) {
            collapseOrExpandTab(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).HEADER.COMPONENT_CONTAINER, false);
            assertFalse(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should not be expanded after manual change", partType));
        } else {
            collapseOrExpandTab(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).HEADER.COMPONENT_CONTAINER, true);
            assertTrue(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should be expanded after manual change", partType));
        }

        refreshFolder();

        view.applicant(folderBuilderApplicantsTest.getBorrowerFullName());
        if (borrowerSelection) {
            assertTrue(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should be expanded after refresh", partType));
        } else {
            assertFalse(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should not be expanded after refresh", partType));
        }
    }

    @ParameterizedTest
    @MethodSource("sectionCases")
    @Order(3)
    @Description("IA-2396 IA-2509 Check If Tabs Are Expanded Correctly When Tabs are collapsed/expanded and then second borrower tab is selected")
    void checkIfLoanDocIncomeTypeWithValueExpandsTabCorrectlyAfterChangesAndSwitchingApplicantTab(IncomePartType partType, PropertyToggles featureToggle,
                                                                                                  Boolean borrowerSelection, Boolean coBorrowerSelection) {
        Assumptions.assumeTrue(featureToggle != null ? RestGetLoanData.getActuatorFeatureToggleValue(featureToggle.value) : true);
        ApplicantView view = new ApplicantView();

        ApplicantIncomeCategory borrowerIncomeCategory = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2);
        if (borrowerSelection) {
            collapseOrExpandTab(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).HEADER.COMPONENT_CONTAINER, false);
            assertFalse(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should not be expanded after manual change", partType));
        } else {
            collapseOrExpandTab(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).HEADER.COMPONENT_CONTAINER, true);
            assertTrue(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should be expanded after manual change", partType));
        }

        ApplicantIncomeCategory coBorrowerIncomeCategory = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2);
        if (coBorrowerSelection) {
            assertTrue(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should be expanded after changing tab", partType));
        } else {
            assertFalse(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should not be expanded after changing tab", partType));
        }

        if (coBorrowerSelection) {
            collapseOrExpandTab(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).HEADER.COMPONENT_CONTAINER, false);
            assertFalse(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should not be expanded after manual change", partType));
        } else {
            collapseOrExpandTab(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).HEADER.COMPONENT_CONTAINER, true);
            assertTrue(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should be expanded after manual change", partType));
        }

        view.applicant(folderBuilderApplicantsTest.getBorrowerFullName());
        if (borrowerSelection) {
            assertTrue(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should be expanded after changing tab", partType));
        } else {
            assertFalse(borrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("Borrower %s section should not be expanded after changing tab", partType));
        }

        view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName());
        if (coBorrowerSelection) {
            assertTrue(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should be expanded after changing tab", partType));
        } else {
            assertFalse(coBorrowerIncomeCategory.onlyPrimaryJobView().incomeTypeWithoutExpanding(partType).isExpanded(), String.format("CoBorrower %s section should not be expanded after changing tab", partType));
        }
    }

    @Test
    @Order(4)
    @Description("IA-2575 Check if income for the section is not zeroed when section is deselected")
    void checkIfUncheckingApplicantBasePayShowsIncome() {
        ApplicantView view = new ApplicantView();
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).HEADER.scrollIntoViewCenter().CATEGORY_NAME.shouldHave(Condition.exactText(W2.toString()));
            IncomeTypeSectionHeader header = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).HEADER;
            IncomeAnalyzerPage.scrollIntoViewCenter(header.COMPONENT_CONTAINER);
            header.CHECKBOX.shouldBeDisplayed();
            header.CHECKBOX.setCheckboxValue(false);
            header.CHECKBOX.shouldBeDisplayed();
            header.TOTAL_INCOME.shouldNotHave(Condition.text("$0.00"));
            header.CHECKBOX.setCheckboxValue(false);
        });
    }

    @Test
    void checkIfApplicantTabsHaveW2IncomeCategoryVisible() {
        ApplicantView view = new ApplicantView();
        for (int i = 0; i < getNumberOfApplicants(); i++) {
            view.applicant(i).incomeCategory(W2).HEADER.scrollIntoViewCenter().CATEGORY_NAME.shouldHave(Condition.exactText(W2.toString()));
        }
    }

    @Test
    @Description("IA-2309 Check If Aliases And More Text Is Shown For Employer And Tooltip With Additional Names Is Displayed Correctly")
    void checkIfAliasesAndMoreIsShownForEmployer() {
        ApplicantView view = new ApplicantView();
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            Integer applicantId = (Integer) ((HashMap<String, Object>) RestNotIARequests.getChecklistRules(folderBuilderApplicantsTest.getFolderId()).then().extract().jsonPath().getList("applicants")
                    .stream().filter(app -> ((HashMap<String, Object>) app).get("fullName").equals(folderBuilderApplicantsTest.getBorrowerFullName())).findFirst().orElseThrow(NullPointerException::new)).get("applicantId");
            RestNotIARequests.addEmployerAlias(folderBuilderApplicantsTest.getFolderId(), applicantId, folderBuilderApplicantsTest.getBorrowerCurrentEmployment(), "First Alias");
            RestNotIARequests.addEmployerAlias(folderBuilderApplicantsTest.getFolderId(), applicantId, folderBuilderApplicantsTest.getBorrowerCurrentEmployment(), "Second Alias");
        });

        SelenideElement employerName = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView()
                .incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment())
                .HEADER.NAME;
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            IncomeAnalyzerPage.scrollIntoViewCenter(employerName);
            employerName.shouldHave(Condition.exactText("Dept of Interior Inc [First Alias] and 1 more"));
            new Actions(employerName.getWrappedDriver()).moveToElement(employerName.getWrappedElement()).perform();
            aliasTooltipComponent.shouldHave(Condition.exactText("Second Alias"));
        });
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Tag("integration")
    @Tag("health")
    @Description("IA-2106 Check If Paystub Overtime Values Are Shown In Correct Fields")
    void checkIfPaystubOvertimeValuesAreShownInCorrectFields() {
        ApplicantView view = new ApplicantView();
        EmploymentBody currentEmployment = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(OVERTIME).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY;
        EmploymentBody previousEmployment = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(OVERTIME).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerPreviousEmployment()).BODY;

        Map<String, String> currentYtdPaystub = currentEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();
        Map<String, String> currentYearPriorPaystub = currentEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();
        Map<String, String> currentTwoYearsPriorPaystub = currentEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();
        Map<String, String> previousYtdPaystub = previousEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();
        Map<String, String> previousYearPriorPaystub = previousEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();
        Map<String, String> previousTwoYearsPriorPaystub = previousEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.PAYSTUB).getDocumentValuesMap();

        assertEquals(StringUtilities.getBigDecimalValueOfIncome(currentYtdPaystub.get("Actual YTD Avg Income")),
                paystubYTDOvertimeAmount.divide(new BigDecimal(12), RoundingMode.HALF_UP), "Current job ytd paystub has different ytd avg income than should have: " + currentYtdPaystub.get("Actual YTD Avg Income"));
        assertEquals(StringUtilities.getBigDecimalValueOfIncome(currentYtdPaystub.get("Gross Pay")),
                paystubYTDOvertimeAmount, "Current job ytd paystub has different gross pay than should have: " + currentYtdPaystub.get("Gross Pay"));
        currentYtdPaystub.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("Period End Date")) {
                assertAll("Check all paystubs have same values in all fields",
                        () -> assertEquals(value, currentYearPriorPaystub.get(key)),
                        () -> assertEquals(value, currentTwoYearsPriorPaystub.get(key)),
                        () -> assertEquals(value, previousYtdPaystub.get(key)),
                        () -> assertEquals(value, previousYearPriorPaystub.get(key)),
                        () -> assertEquals(value, previousTwoYearsPriorPaystub.get(key))
                );
            }
        });
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Tag("integration")
    @Description("IA-2107 Check If Voe Overtime Values Are Shown In Correct Fields")
    void checkIfVoeOvertimeValuesAreShownInCorrectFields() {
        ApplicantView view = new ApplicantView();
        EmploymentBody currentEmployment = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(OVERTIME).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY;
        EmploymentBody previousEmployment = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(OVERTIME).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerPreviousEmployment()).BODY;

        Map<String, String> ytdVoe = currentEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();
        Map<String, String> priorYearVoe = currentEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();
        Map<String, String> twoYearsPriorVoe = currentEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();
        Map<String, String> previousJobYtdVoe = previousEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.VOE).getDocumentValuesMap();

        assertEquals(StringUtilities.getBigDecimalValueOfIncome(ytdVoe.get("Actual YTD Avg Income")),
                voeMonthlyOvertimeAmount, "Current job ytd voe has different income than should have: " + ytdVoe.get("Actual YTD Avg Income"));
        ytdVoe.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("Period End Date")) {
                assertAll("Check primary job voe have same values in all years",
                        () -> assertEquals(value, priorYearVoe.get(key)),
                        () -> assertEquals(value, twoYearsPriorVoe.get(key))
                );
            }
        });
        assertAll("Check previous job voe have same correct values in income fields",
                () -> assertEquals(StringUtilities.getBigDecimalValueOfIncome(previousJobYtdVoe.get("Projected Monthly Income")),
                        voeMonthlyOvertimeAmount, "Previous job ytd voe has incorrect monthly income value: " + previousJobYtdVoe.get("Projected Monthly Income")),
                () -> assertEquals(StringUtilities.getBigDecimalValueOfIncome(previousJobYtdVoe.get("Gross Pay")),
                        voeMonthlyOvertimeAmount, "Previous job ytd voe has incorrect gross pay value: " + previousJobYtdVoe.get("Gross Pay"))
        );
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Description("IA-2108 Check If EVOE Overtime Values Are Shown In Correct Fields")
    void checkIfEVOEOvertimeValuesAreShownInCorrectFields() {
        ApplicantView view = new ApplicantView();
        EmploymentBody currentEmployment = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(OVERTIME).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY;

        Map<String, String> ytdEVoe = currentEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> priorYearEVoe = currentEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> twoYearsPriorEVoe = currentEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        assertAll("Check current job evoe have correct values in income fields",
                () -> assertEquals(StringUtilities.getBigDecimalValueOfIncome(ytdEVoe.get("Actual YTD Avg Income")),
                        voeMonthlyOvertimeAmount, "Previous job ytd voe has incorrect avg income value: " + ytdEVoe.get("Actual YTD Avg Income")),
                () -> assertEquals(StringUtilities.getBigDecimalValueOfIncome(ytdEVoe.get("Gross Pay")),
                        voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)), "Previous job ytd voe has incorrect gross pay value: " + ytdEVoe.get("Gross Pay"))
        );
        ytdEVoe.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("Period End Date")) {
                assertAll("Check primary job evoe have same values in all years",
                        () -> assertEquals(value, priorYearEVoe.get(key)),
                        () -> assertEquals(value, twoYearsPriorEVoe.get(key))
                );
            }
        });

        assertAll("Check past evoe period end date",
                () -> assertEquals("12/31/" + YEAR_TO_DATE.toString(), ytdEVoe.get("Period End Date"),
                        "Previous job ytd evoe has period end date"),
                () -> assertEquals(ONE_YEAR_PRIOR.toString(), priorYearEVoe.get("Period End Date"),
                        "Previous job prior year evoe has period end date"),
                () -> assertEquals(TWO_YEARS_PRIOR.toString(), twoYearsPriorEVoe.get("Period End Date"),
                        "Previous job two years prior evoe has period end date"));
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Description("IA-2989 Check If Previous EVOE Overtime Values Are Shown In Correct Fields")
    void checkIfEVOEOvertimePreviousJobValuesAreShownInCorrectFields() {
        ApplicantView view = new ApplicantView();
        EmploymentBody previousEmployment = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(OVERTIME).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerPreviousEmployment()).BODY;

        Map<String, String> ytdEVoe = previousEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> priorYearEVoe = previousEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> twoYearsPriorEVoe = previousEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        ytdEVoe.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("Period End Date")) {
                assertAll("Check previous job evoe have same values in all years",
                        () -> assertEquals(value, priorYearEVoe.get(key)),
                        () -> assertEquals(value, twoYearsPriorEVoe.get(key))
                );
            }
        });
    }


    @Test
    @EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
    @Description("IA-2978 Check If Previous EVOE Comissions Values Are Shown In Correct Fields")
    void checkIfEVOECommissionsPreviousJobValuesAreShownInCorrectFields() {
        ApplicantView view = new ApplicantView();
        EmploymentBody previousEmployment = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerPreviousEmployment()).BODY;

        Map<String, String> ytdEVoe = previousEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> priorYearEVoe = previousEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> twoYearsPriorEVoe = previousEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        ytdEVoe.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("Period End Date")) {
                assertAll("Check previous job evoe have same values in all years",
                        () -> assertEquals(value, priorYearEVoe.get(key)),
                        () -> assertEquals(value, twoYearsPriorEVoe.get(key))
                );
            }
        });
    }

    @Test
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @Description("IA-2980 Check If Previous EVOE Bonus Values Are Shown In Correct Fields")
    void checkIfEVOEBonusPreviousJobValuesAreShownInCorrectFields() {
        ApplicantView view = new ApplicantView();
        EmploymentBody previousEmployment = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BONUS).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerPreviousEmployment()).BODY;

        Map<String, String> ytdEVoe = previousEmployment.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> priorYearEVoe = previousEmployment.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();
        Map<String, String> twoYearsPriorEVoe = previousEmployment.employmentYear(TWO_YEARS_PRIOR.toString()).document(SummaryDocumentType.EVOE).getDocumentValuesMap();

        ytdEVoe.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("Period End Date")) {
                assertAll("Check previous job evoe have same values in all years",
                        () -> assertEquals(value, priorYearEVoe.get(key)),
                        () -> assertEquals(value, twoYearsPriorEVoe.get(key))
                );
            }
        });
    }

    @Test
    @Description("IA-2092 Check If Income Section Title Is Visible At All Times")
    void checkIfIncomeSectionTitleIsVisibleAtAllTimes() {
        ApplicantView view = new ApplicantView();
        ApplicantIncomeCategory w2IncomeCategory = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2);
        w2IncomeCategory.onlyPrimaryJobView().expandAllIncomeTypes().COMPONENT_CONTAINER.scrollIntoView(false);
        view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeTypeList().forEach(incomeSection -> {
            incomeSection.COMPONENT_CONTAINER.scrollIntoView(false);
            Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
                assertTrue(incomeSection.HEADER.COMPONENT_CONTAINER.getLocation().getY() < 120, "Automation test has not scrolled down to section");
                assertTrue(incomeSection.HEADER.COMPONENT_CONTAINER.getLocation().getY() > 80, "Section header is not visible when scrolled down");
            });
        });
    }

    @Test
    @Description("IA-2535 Check If Income Category Title Is Visible At All Times")
    void checkIfIncomeCategoryTitleIsVisibleAtAllTimes() {
        ApplicantView view = new ApplicantView();
        ApplicantIncomeCategory w2IncomeCategory = view.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2);
        w2IncomeCategory.onlyPrimaryJobView().expandAllIncomeTypes().COMPONENT_CONTAINER.scrollIntoView(false);
        List<IncomeTypeSection> primarySections = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeTypeList();
        primarySections.get(primarySections.size() - 1).HEADER.COMPONENT_CONTAINER.scrollIntoView(false);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
            assertTrue(w2IncomeCategory.HEADER.COMPONENT_CONTAINER.getLocation().getY() < 70, "Automation test has not scrolled down to section");
            assertTrue(w2IncomeCategory.HEADER.COMPONENT_CONTAINER.getLocation().getY() > 30, "Section header is not visible when scrolled down");
        });
    }

    @Test
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @Description("IA-2128 Check If Ytd Overtime Income For Current Job Can Be Deselected")
    void checkIfOvertimeYtdForCurrentJobCanBeDeselected() {
        ApplicantView view = new ApplicantView();
        IncomeTypeSection overtimeSection = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(OVERTIME);
        overtimeSection.HEADER.CHECKBOX.setCheckboxValue(true);
        overtimeSection.HEADER.CHECKBOX.BOX.shouldBe(Condition.checked);
        EmploymentYear overtimeYtdYear = overtimeSection.BODY.employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString());
        overtimeYtdYear.CHECKBOX.shouldBeDisplayed().setCheckboxValue(true);
        overtimeYtdYear.CHECKBOX.BOX.shouldBe(Condition.checked);
        overtimeYtdYear.CHECKBOX.shouldBeDisplayed().setCheckboxValue(false);
        overtimeYtdYear.CHECKBOX.BOX.shouldNotBe(Condition.checked);
    }

    @Test
    @EnableIfToggled(propertyName = TEACHERS_TOGGLE)
    @Description("IA-2128 IA-2306 Check If Ytd Base Pay Income For Current Job Can Be Deselected For Teachers Income")
    void checkIfCurrentJobYtdHasWorkingCheckbox() {
        ApplicantView view = new ApplicantView();
        view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(true);
        CheckBox checkBox = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY
                .employmentYear(YEAR_TO_DATE.toString()).CHECKBOX;
        checkBox.shouldBeDisplayed().setCheckboxValue(false);
        checkBox.BOX.shouldNotBe(Condition.checked);
        checkBox.shouldBeDisplayed().setCheckboxValue(true);
        checkBox.BOX.shouldBe(Condition.checked);
    }

    @Test
    @EnableIfToggled(propertyName = TEACHERS_TOGGLE, negateState = true)
    @Description("IA-2128 IA-2306 Check If Ytd Base Pay Income For Current Job Cannot Be Deselected When Teachers Feature Is Disabled ")
    void checkIfCurrentJobYtdDoesNotHaveCheckboxIfFeatureIsDisabled() {
        ApplicantView view = new ApplicantView();
        CheckBox checkBox = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY
                .employmentYear(YEAR_TO_DATE.toString()).CHECKBOX;
        assertFalse(checkBox.BOX.exists(), "Checkbox for ytd income exists when teachers are disabled");
    }

    @Test
    @EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
    @Description("IA-2167 Check If Ytd Commissions Income For Current Job Can Be Deselected")
    void checkIfCommissionYtdForCurrentJobCanBeDeselected() {
        ApplicantView view = new ApplicantView();
        IncomeTypeSection commissionsSection = view.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS);
        commissionsSection.HEADER.CHECKBOX.setCheckboxValue(true);
        commissionsSection.HEADER.CHECKBOX.BOX.shouldBe(Condition.checked);
        EmploymentYear commissionsYtdYear = commissionsSection.BODY.employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString());
        commissionsYtdYear.CHECKBOX.shouldBeDisplayed().setCheckboxValue(true);
        commissionsYtdYear.CHECKBOX.BOX.shouldBe(Condition.checked);
        commissionsYtdYear.CHECKBOX.shouldBeDisplayed().setCheckboxValue(false);
        commissionsYtdYear.CHECKBOX.BOX.shouldNotBe(Condition.checked);
    }

    @Test
    @Description("IA-2867 check if W2 stays selected after income type deselection")
    void checkIfW2StaysSelectedAfterIncomeTypeDeselection() {
        ApplicantView applicantView = new ApplicantView();
        ApplicantIncomeCategory applicantIncomeCategory = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2);
        applicantIncomeCategory.onlyPrimaryJobView().incomeType(BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(false);
        applicantIncomeCategory.onlyPrimaryJobView().incomeType(OVERTIME).HEADER.CHECKBOX.setCheckboxValue(false);
        applicantIncomeCategory.onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS).HEADER.CHECKBOX.setCheckboxValue(false);
        applicantIncomeCategory.onlyPrimaryJobView().incomeType(IncomePartType.BONUS).HEADER.CHECKBOX.setCheckboxValue(false);
        applicantIncomeCategory.HEADER.CHECKBOX.checkIfCheckboxIsSelected();
    }

}

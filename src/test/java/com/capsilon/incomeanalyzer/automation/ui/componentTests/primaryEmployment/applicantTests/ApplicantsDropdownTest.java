package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.ApplicantIncomeCategory;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeType;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.averages.HistoricalAverages;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.Employment;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document.YearDocument;
import com.capsilon.incomeanalyzer.automation.ui.component.yearDocumentsFields.MonthsPaidPerYear;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.*;
import com.capsilon.test.reportportal.Description;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.restoreApplicantDefaults;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory.W2;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType.BASE_PAY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Applicants Dropdown Menu Test")
@Execution(CONCURRENT)
@ResourceLock(value = "ApplicantsDropdowmTest")
class ApplicantsDropdownTest extends TestBaseUI {

    private final BigDecimal paystubYTDOvertimeAmount = bigD(12000);
    private final BigDecimal voeMonthlyOvertimeAmount = bigD(1000);
    IAFolderBuilder folderBuilderApplicantsTest = createFolderBuilder("IAApplDrpdn");
    private Boolean documentsShouldBeUploaded = true;
    RestGetResponse data;

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
                            .setPriorYearBasePay("14000")
                            .setTwoYearPriorBasePay("13000")
                            .setYtdOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setPriorYearOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setTwoYearPriorOvertime(voeMonthlyOvertimeAmount.multiply(new BigDecimal(12)).toString())
                            .setYtdCommission("1500")
                            .setPriorYearCommission("18000")
                            .setTwoYearPriorCommission("18000"))
                    .importDocumentList();
            refreshFolder();
            documentsShouldBeUploaded = false;
        }
    }

    @Test
    @Order(1)
    @Description("IA-2874 check if Months Paid Per Year has editable state change")
    void checkIfMonthsPaidPerYearHasEditableStateChange() {
        ApplicantView applicantView = new ApplicantView();
        MonthsPaidPerYear monthsPaidPerYear = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY)
                .BODY.employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).monthsPaidPerYear();
        assertAll(
                () -> assertEquals("pointer", monthsPaidPerYear.getCursorStateOnButton(), "mouse hover should be stated as pointer"),
                () -> assertTrue(monthsPaidPerYear.changeDropDownVisibility().DROPDOWN_OPTIONS.get(0).exists(), "dropdown didnt expanded as expected"),
                () -> assertEquals(monthsPaidPerYear.SELECTED_LABEL.getCssValue("font-size"),
                        monthsPaidPerYear.getItemFromListByValue("12").getCssValue("font-size")),
                () -> assertEquals("rgba(2, 21, 44, 0.88)", monthsPaidPerYear.getArrowIcon().getCssValue("color")));
        monthsPaidPerYear.selectFromDropdown("12");
    }

    @Test
    @Order(2)
    @Description("IA-2770 IA-2765 check if Months Paid Per Year is editable by dropdown")
    void checkIfMonthsPaidPerYearIsEditable() {
        ApplicantView applicantView = new ApplicantView();
        YearDocument employmentYear = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE);
        employmentYear.monthsPaidPerYear().changeDropDownVisibility().selectFromDropdown("8");
        employmentYear.monthsPaidPerYear().selectedValueShouldBe("8");
    }

    @Test
    @Order(3)
    @Description("IA-2768 IA-2769 check If Months Worked are Touchless and can be restored by defaults")
    void checkIfMonthsPaidPerYearAreConsideredAsTouchlessAndCanBeRestoredByDefaultButton() {
        ApplicantView applicantView = new ApplicantView();
        ApplicantIncomeCategory applicantIncomeCategory = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2);
        YearDocument employmentYear = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE);
        employmentYear.monthsWorked().openDropdown().setCustomValue("8").selectedValueShouldBe("8");
        applicantIncomeCategory.HEADER.RESTORE_DEFAULTS_BUTTON.shouldNotBe(Condition.exist);
        applicantIncomeCategory.onlyPrimaryJobView().incomeType(OVERTIME).HEADER.CHECKBOX.setCheckboxValue(false);
        applicantIncomeCategory.HEADER.restoreDefaults().BUTTON_RESTORE.click();
        employmentYear.monthsWorked().selectedValueShouldBe("12");
    }

    @Test
    @Order(4)
    @Description("IA-2967 check if all years document does not vanish after choosing Months Worked")
    void checkIfAllYearsDocumentsExistAfterMonthsWorkedSelection() {
        ApplicantView applicantView = new ApplicantView();
        Employment incomeTypeSection = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).
                BODY.employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment());
        incomeTypeSection.BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE).monthsWorked().setCustomValue("5");
        incomeTypeSection.BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE).monthsWorked().LABEL.shouldBe(Condition.visible);
    }

    @Test
    @Order(5)
    @Description("IA-2765 IA-2966 Check if Months Worked has default value of 12 and no invalid values can be inserted")
    void checkIfMonthsWorkedAreEditableAndIncorrectValuesCannotBeInserted() {
        ApplicantView applicantView = new ApplicantView();
        YearDocument employmentYear = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView().incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.EVOE);
        employmentYear.monthsWorked().openDropdown()
                .DEFAULT_MONTH_WORKED_VALUE.shouldHave(Condition.text("12"));
        employmentYear.monthsWorked().SEARCH_BOX.sendKeys("13");
        employmentYear.monthsWorked().ERROR_LABEL.shouldHave(Condition.text("Invalid number (0.01-12.00)"));
        employmentYear.monthsWorked().SEARCH_BOX.clear();
        employmentYear.monthsWorked().SEARCH_BOX.sendKeys("2.");
        employmentYear.monthsWorked().ERROR_LABEL.shouldHave(Condition.text("Wrong format (eg. 5.14)"));
        employmentYear.monthsWorked().DEFAULT_MONTH_WORKED_VALUE.click();
        employmentYear.monthsWorked().selectedValueShouldBe("12");
    }

    @Test
    @Order(6)
    @Description("IA-2767 IA-3021 check if Actual Avg Income recalculate due to Months Worked changes and values flows to all incomes")
    void checkIfActualAvgRecalculateDueToMonthsWorkedChanges() {
        ApplicantView applicantView = new ApplicantView();
        IncomeCategoryIncomeType primaryIncomeGroup = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(W2).onlyPrimaryJobView();
        YearDocument employmentYearBasePay = primaryIncomeGroup.incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE);
        YearDocument employmentYearOvertimeIncome = primaryIncomeGroup.incomeType(OVERTIME).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE);
        YearDocument employmentYearOvertimeCommissions = primaryIncomeGroup.incomeType(IncomePartType.COMMISSIONS).BODY
                .employment(folderBuilderApplicantsTest.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(SummaryDocumentType.EVOE);
        HistoricalAverages historicalAverageValue = applicantView.applicant(folderBuilderApplicantsTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.HISTORICAL_AVERAGES;

        primaryIncomeGroup.incomeType(IncomePartType.BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(true);
        primaryIncomeGroup.incomeType(IncomePartType.OVERTIME).HEADER.CHECKBOX.setCheckboxValue(true);
        primaryIncomeGroup.incomeType(IncomePartType.COMMISSIONS).HEADER.CHECKBOX.setCheckboxValue(true);
        employmentYearBasePay.monthsWorked().setCustomValue("6");
        historicalAverageValue.year(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR).AVG_MONTHLY_INCOME.shouldHave(Condition.text("$2,333.33"));
        employmentYearBasePay.getDocumentLocatorsMap().get("Actual YTD Avg Income").shouldHave(Condition.text("2,333.33"));
        employmentYearBasePay.monthsWorked().selectedValueShouldBe("6");
        employmentYearOvertimeIncome.monthsWorked().selectedValueShouldBe("6");
        employmentYearOvertimeCommissions.monthsWorked().selectedValueShouldBe("6");
    }

    @Test
    @Order(7)
    @Description("IA-2948 IA-3048 IA-3045 check if Pay Frequency for paystub can be editable by dropdown and values are consistent ")
    void checkIfPayFrequencyForPaystubCanBeChangedByDropdown() {
        ApplicantView applicantView = new ApplicantView();
        ApplicantIncomeCategory applicantIncomeCategory = applicantView.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2);
        applicantIncomeCategory.onlyPrimaryJobView().incomeType(BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(true);
        YearDocument employmentYearBasePay = applicantIncomeCategory.onlyPrimaryJobView().incomeType(BASE_PAY).BODY
                .employment(folderBuilderApplicantsTest.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB);
        SelenideElement footnote = applicantView.applicant(folderBuilderApplicantsTest.getBorrowerFullName()).incomeCategory(W2)
                .onlyPrimaryJobView().incomeType(BASE_PAY).BODY
                .employmentSameName(folderBuilderApplicantsTest.getBorrowerCurrentEmployment(), true).BODY.FOOTNOTE.get(0);

        employmentYearBasePay.payFrequency().openDropDown().selectFrequency(IncomeFrequency.SEMI_MONTHLY).checkLabelValue(IncomeFrequency.SEMI_MONTHLY.text);
        footnote.shouldHave(Condition.exactText("[1] The pay frequency (semi-monthly) was applied by a business validator instead of being calculated from paystub dates"))
                .scrollIntoView("{block: \"center\"}");
        employmentYearBasePay.payFrequency().openDropDown().selectFrequency(IncomeFrequency.BI_WEEKLY).checkLabelValue(IncomeFrequency.BI_WEEKLY.text);
        footnote.shouldHave(Condition.exactText("[1] The pay frequency (bi-weekly) was applied by a business validator instead of being calculated from paystub dates"))
                .scrollIntoView("{block: \"center\"}");
        employmentYearBasePay.payFrequency().openDropDown().selectFrequency(IncomeFrequency.WEEKLY).checkLabelValue(IncomeFrequency.WEEKLY.text);
        footnote.shouldHave(Condition.exactText("[1] The pay frequency (weekly) was applied by a business validator instead of being calculated from paystub dates"))
                .scrollIntoView("{block: \"center\"}");
        employmentYearBasePay.payFrequency().openDropDown().selectFrequency(IncomeFrequency.UNKNOWN).checkLabelValue(IncomeFrequency.UNKNOWN.text);
        footnote.shouldHave(Condition.exactText("[1] The pay frequency (-) was applied by a business validator instead of being calculated from paystub dates"))
                .scrollIntoView("{block: \"center\"}");
        employmentYearBasePay.payFrequency().openDropDown().selectFrequency(IncomeFrequency.MONTHLY).checkLabelValue(IncomeFrequency.MONTHLY.text);
        footnote.shouldHave(Condition.exactText("[1] The pay frequency (monthly) was applied by a business validator instead of being calculated from paystub dates"))
                .scrollIntoView("{block: \"center\"}");
    }
}
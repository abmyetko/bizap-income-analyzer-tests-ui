package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.common.utils.mismo.structure.EmploymentStatusBase;
import com.capsilon.common.utils.mismo.structure.IncomeBase;
import com.capsilon.common.utils.mismo.wrapper.MismoWrapper;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.SideNav;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.Summary;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.summary.ApplicantAccordion;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.totalmonthlyincome.TotalMonthlyIncome;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAMismoBuilder;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;
import java.util.Objects;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Disabled("awaiting multi-borrower release")
@DisplayName("Multi applicants base test")
@Execution(CONCURRENT)
@ResourceLock(value = "MultiApplicantsBaseTest")
public class MultiApplicantsBaseTest extends TestBaseUI {
    private final IAMismoBuilder mismoBuilder = new IAMismoBuilder("MultiBorrCheck");
    private final int oneAppId = 0;
    private final int twoAppId = 1;
    private final int threeAppId = 2;
    private final String threeFirstName = "Penny";
    private final String threeLastName = "Slow";
    private final String threeCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String threePreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String threePreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;
    private final int fourAppId = 3;
    private final String fourFirstName = "Harry";
    private final String fourLastName = "Swift";
    private final String fourCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String fourPreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String fourPreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;
    private final int fiveAppId = 4;
    private final String fiveFirstName = "Why";
    private final String fiveLastName = "Ung";
    private final String fiveCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String fivePreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String fivePreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;
    private final int sixAppId = 5;
    private final String sixFirstName = "Dothi";
    private final String sixLastName = "Swift";
    private final String sixCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String sixPreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private final String sixPreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;
    private final String sixTotalMonthlyIncome = "$3,266.67";

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        String threeSSN = "333-22-0990";
        String threePriEmpName = "Hueh Co";
        String threePreEmpName = "Hue Inc";
        String fourSSN = "111-55-1234";
        String fourPriEmpName = "Oh Co";
        String fourPreEmpName = "Ohey Inc";
        String fiveSSN = "111-55-1235";
        String fivePriEmpName = "Od Co";
        String fivePreEmpName = "Oc Inc";
        String sixSSN = "111-55-1236";
        String sixPriEmpName = "Huey Co";
        String sixPreEmpName = "Whey Inc";

        mismoBuilder.generateLoanDocument();
        mismoBuilder.getMismoWrapper().getDealSets()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower();

        mismoBuilder.setBuilderFirstName(threeAppId, threeFirstName);
        mismoBuilder.setBuilderLastName(threeAppId, threeLastName);
        mismoBuilder.setBuilderFullName(threeAppId, threeFirstName + " " + threeLastName);
        mismoBuilder.setBuilderSSN(threeAppId, threeSSN.replace("-", ""));
        mismoBuilder.setBuilderEmployerName(threeAppId, 0, threePriEmpName);
        mismoBuilder.setBuilderEmployerName(threeAppId, 1, threePreEmpName);
        mismoBuilder.setEmployerStartDate(threeAppId, 0, threeCurrentStartDate);
        mismoBuilder.setEmployerStartDate(threeAppId, 1, threePreviousJobStartDate);
        mismoBuilder.setEmployerEndDate(threeAppId, 1, threePreviousJobEndDate);
        mismoBuilder.setPreviousEmployerStatusType(threeAppId, 1);

        mismoBuilder.setBuilderFirstName(fourAppId, fourFirstName);
        mismoBuilder.setBuilderLastName(fourAppId, fourLastName);
        mismoBuilder.setBuilderFullName(fourAppId, fourFirstName + " " + fourLastName);
        mismoBuilder.setBuilderSSN(fourAppId, fourSSN.replace("-", ""));
        mismoBuilder.setBuilderEmployerName(fourAppId, 0, fourPriEmpName);
        mismoBuilder.setBuilderEmployerName(fourAppId, 1, fourPreEmpName);
        mismoBuilder.setEmployerStartDate(fourAppId, 0, fourCurrentStartDate);
        mismoBuilder.setEmployerStartDate(fourAppId, 1, fourPreviousJobStartDate);
        mismoBuilder.setEmployerEndDate(fourAppId, 1, fourPreviousJobEndDate);
        mismoBuilder.setPreviousEmployerStatusType(fourAppId, 1);

        mismoBuilder.setBuilderFirstName(fiveAppId, fiveFirstName);
        mismoBuilder.setBuilderLastName(fiveAppId, fiveLastName);
        mismoBuilder.setBuilderFullName(fiveAppId, fiveFirstName + " " + fiveLastName);
        mismoBuilder.setBuilderSSN(fiveAppId, fiveSSN.replace("-", ""));
        mismoBuilder.setBuilderEmployerName(fiveAppId, 0, fivePriEmpName);
        mismoBuilder.setBuilderEmployerName(fiveAppId, 1, fivePreEmpName);
        mismoBuilder.setEmployerStartDate(fiveAppId, 0, fiveCurrentStartDate);
        mismoBuilder.setEmployerStartDate(fiveAppId, 1, fivePreviousJobStartDate);
        mismoBuilder.setEmployerEndDate(fiveAppId, 1, fivePreviousJobEndDate);
        mismoBuilder.setPreviousEmployerStatusType(fiveAppId, 1);
        mismoBuilder.setCurrentIncomeMonthlyTotalAmount(fiveAppId, IncomeBase.BASE, EmploymentStatusBase.CURRENT, BigDecimal.ZERO);

        mismoBuilder.setBuilderFirstName(sixAppId, sixFirstName);
        mismoBuilder.setBuilderLastName(sixAppId, sixLastName);
        mismoBuilder.setBuilderFullName(sixAppId, sixFirstName + " " + sixLastName);
        mismoBuilder.setBuilderSSN(sixAppId, sixSSN.replace("-", ""));
        mismoBuilder.setBuilderEmployerName(sixAppId, 0, sixPriEmpName);
        mismoBuilder.setBuilderEmployerName(sixAppId, 1, sixPreEmpName);
        mismoBuilder.setEmployerStartDate(sixAppId, 0, sixCurrentStartDate);
        mismoBuilder.setEmployerStartDate(sixAppId, 1, sixPreviousJobStartDate);
        mismoBuilder.setEmployerEndDate(sixAppId, 1, sixPreviousJobEndDate);
        mismoBuilder.setPreviousEmployerStatusType(sixAppId, 1);
        mismoBuilder.setPrimaryJobBasePay(sixSSN, "2,900.00");
        mismoBuilder.setPrimaryJobOvertime(sixSSN, "100.00");
        mismoBuilder.setPrimaryJobCommissions(sixSSN, "200");
        mismoBuilder.setPrimaryJobBonus(sixSSN, "100");

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(mismoBuilder.uiBuild());
        assertEquals(6, RestGetLoanData.getApplicationData(mismoBuilder.getFolderId()).getApplicants().size());
        RestGetResponse response = RestGetLoanData.getApplicationData(mismoBuilder.getFolderId());

        dataUpload = createUploadObject(mismoBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        mismoBuilder.getBorrowerFullName(),
                        mismoBuilder.getBorrowerSSN(),
                        mismoBuilder.getBorrowerCollaboratorId(),
                        mismoBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,912.43", "32,632.21")))
                .addDocument(dataUpload.createCustomPaystub(
                        mismoBuilder.getCoBorrowerFullName(),
                        mismoBuilder.getCoBorrowerSSN(),
                        mismoBuilder.getCoBorrowerCollaboratorId(),
                        mismoBuilder.getCoBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,000.00", "12,000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        threeFirstName + " " + threeLastName,
                        threeSSN.replace("-", ""),
                        response.getApplicant(threeFirstName + " " + threeLastName).getRefId(),
                        threePriEmpName,
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "800.00", "12,000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        fourFirstName + " " + fourLastName,
                        fourSSN.replace("-", ""),
                        response.getApplicant(fourFirstName + " " + fourLastName).getRefId(),
                        fourPriEmpName,
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "420.10", "12,000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        fiveFirstName + " " + fiveLastName,
                        fiveSSN.replace("-", ""),
                        response.getApplicant(fiveFirstName + " " + fiveLastName).getRefId(),
                        fivePriEmpName,
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,510.00", "12,000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        sixFirstName + " " + sixLastName,
                        sixSSN.replace("-", ""),
                        response.getApplicant(sixFirstName + " " + sixLastName).getRefId(),
                        sixPriEmpName,
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "2,900.00", "12,000.00"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "400", "800"),
                        new PaystubData.PaystubIncomeRow(COMMISSION, "", "", "100", "200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "200", "400")))
                .importDocumentList();
        refreshFolder();
    }

    @BeforeEach
    void goToSummaryTab() {
        IncomeAnalyzerPage.summaryView.summary.goToSummaryTab();
    }

    @Order(1)
    @Test
    @Description("IA-3049 Check if side nav correctly display up to six borrowers")
    void checkIfSideNavCorrectlyDisplayUpToSixBorrowers() {
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;

        for (int i = 0; i < sideNav.getApplicantNumber(); i++) {
            sideNav.getApplicant(i).checkBox.checkIfCheckboxIsEnabled();
        }
    }

    @Order(1)
    @Test
    @Description("IA-3049 Check if IA unselects Borrower when he has no income")
    void checkIfIAUnselectsBorrowerWhenHeHasNoIncome() {
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;
        sideNav.getApplicant(oneAppId).checkBox.checkIfCheckboxIsSelected();
        sideNav.getApplicant(twoAppId).checkBox.checkIfCheckboxIsSelected();
        sideNav.getApplicant(threeAppId).checkBox.checkIfCheckboxIsSelected();
        sideNav.getApplicant(fourAppId).checkBox.checkIfCheckboxIsSelected();
        sideNav.getApplicant(fiveAppId).checkBox.checkIfCheckboxIsUnselected();
        sideNav.getApplicant(sixAppId).checkBox.checkIfCheckboxIsSelected();
    }

    @Order(1)
    @Test
    @Description("IA-3050 Check if Summary tab correctly display up to 6 borrowers")
    void checkIfSummaryTabCorrectlyDisplayUpTo6Borrowers() {
        Summary summary = IncomeAnalyzerPage.summaryView.summary;

        for (int i = 0; i < summary.getApplicantAccordionTabCount(); i++) {
            summary.getApplicantAccordion(i).shouldBeDisplayed();
        }
    }

    @Order(1)
    @Test
    @Description("IA-3050 Check if Summary tab correctly display names borrowers")
    void checkIfSummaryTabCorrectlyDisplayNamesBorrowers() {
        TotalMonthlyIncome totalMonthlyIncome = IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer;
        Assertions.assertAll("Borrowers Names",
                () -> assertEquals(getApplicantShortFullName(mismoBuilder.getBorrowerFirstName(), mismoBuilder.getBorrowerLastName()),
                        totalMonthlyIncome.getApplicantProgressBar(oneAppId).NAME.getText()),
                () -> assertEquals(getApplicantShortFullName(mismoBuilder.getCoBorrowerFirstName(), mismoBuilder.getCoBorrowerLastName()),
                        totalMonthlyIncome.getApplicantProgressBar(twoAppId).NAME.getText()),
                () -> assertEquals(getApplicantShortFullName(threeFirstName, threeLastName),
                        totalMonthlyIncome.getApplicantProgressBar(threeAppId).NAME.getText()),
                () -> assertEquals(getApplicantShortFullName(fourFirstName, fourLastName),
                        totalMonthlyIncome.getApplicantProgressBar(fourAppId).NAME.getText()),
                () -> assertEquals(getApplicantShortFullName(fiveFirstName, fiveLastName),
                        totalMonthlyIncome.getApplicantProgressBar(fiveAppId).NAME.getText()),
                () -> assertEquals(getApplicantShortFullName(sixFirstName, sixLastName),
                        totalMonthlyIncome.getApplicantProgressBar(sixAppId).NAME.getText()));
    }

    @Order(1)
    @Test
    @Description("IA-3051 Check if Summary tab display borrowers incomes correctly")
    void checkIfSummaryTabDisplayBorrowersIncomesCorrectly() {
        TotalMonthlyIncome totalMonthlyIncome = IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer;
        Assertions.assertAll("Borrowers Incomes",
                () -> assertEquals("$1,912.43", totalMonthlyIncome.getApplicantProgressBar(oneAppId).INCOME.getText()),
                () -> assertEquals("$1,000.00", totalMonthlyIncome.getApplicantProgressBar(twoAppId).INCOME.getText()),
                () -> assertEquals("$800.00", totalMonthlyIncome.getApplicantProgressBar(threeAppId).INCOME.getText()),
                () -> assertEquals("$420.10", totalMonthlyIncome.getApplicantProgressBar(fourAppId).INCOME.getText()),
                () -> assertEquals("$0.00", totalMonthlyIncome.getApplicantProgressBar(fiveAppId).INCOME.getText()),
                () -> assertEquals(sixTotalMonthlyIncome, totalMonthlyIncome.getApplicantProgressBar(sixAppId).INCOME.getText()));
    }

    @Order(1)
    @Test
    @Description("IA-3051 Check if progress bar display correct width for Borrower")
    void checkIfProgressBarDisplayCorrectWidthForBorrower() {
        Assertions.assertTrue(Objects.requireNonNull(IncomeAnalyzerPage.summaryView.summary.totalMonthlyIncomeContainer
                .getApplicantProgressBar(oneAppId).TUTTI_FRUTTI.BAR.getAttribute("style")).startsWith("width: 58.5"));
    }

    @Order(1)
    @Test
    @Description("IA-3051 Check if Summary tab display Borrower incomes types correctly")
    void checkIfSummaryTabDisplayBorrowerIncomesTypesCorrectly() {
        ApplicantAccordion applicantAccordion = IncomeAnalyzerPage.summaryView.summary.getApplicantAccordion(sixAppId);
        applicantAccordion.checkIfSumOfIncomeTypesEqualsToApplicantIncome();

        Assertions.assertAll("Borrowers Incomes Types",
                () -> Assertions.assertEquals((sixFirstName + " " + sixLastName + " (1)").toUpperCase(),
                        applicantAccordion.getPanel().NAME.getText()),
                () -> Assertions.assertEquals(sixTotalMonthlyIncome, applicantAccordion.getPanel().INCOME.getText()),
                () -> Assertions.assertEquals("$2,900.00", applicantAccordion.getIncomeTypes()
                        .getIncomeById(0).getParts().getIncomeParts().get("Base Pay")),
                () -> Assertions.assertEquals("$266.67", applicantAccordion.getIncomeTypes()
                        .getIncomeById(0).getParts().getIncomeParts().get("Overtime")),
                () -> Assertions.assertEquals("$66.67", applicantAccordion.getIncomeTypes()
                        .getIncomeById(0).getParts().getIncomeParts().get("Commission")),
                () -> Assertions.assertEquals("$33.33", applicantAccordion.getIncomeTypes()
                        .getIncomeById(0).getParts().getIncomeParts().get("Bonus")));
    }

    @Order(1)
    @Test
    @Description("IA-3052 Check if Applicant tabs display up to six borrowers tab correctly")
    void checkIfApplicantTabsDisplayUpToSixBorrowersTabCorrectly() {
        ApplicantView applicantView = IncomeAnalyzerPage.applicantView;
        Assertions.assertEquals(6, applicantView.getNumberOfApplicants());

        for (int i = 0; i < applicantView.getNumberOfApplicants(); i++) {
            applicantView.applicant(i).shouldBeDisplayed();
        }
        Assertions.assertEquals(getApplicantShortFullName(threeFirstName, threeLastName).toUpperCase(),
                applicantView.applicant(threeAppId).TAB_BUTTON.getText());
    }

    @Order(Integer.MAX_VALUE - 1)
    @Test
    @Description("IA-3055 Check if deselect borrowers triggers recalculation")
    void checkIfDeselectBorrowersTriggersRecalculation() {
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;
        sideNav.getApplicant(sixAppId).checkBox.setCheckboxValue(false);
        sideNav.getApplicant(fourAppId).checkBox.setCheckboxValue(false);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            Assertions.assertEquals("$3,712.43", IncomeAnalyzerPage
                    .summaryView.summary.totalMonthlyIncomeContainer.TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE.getText());
            Assertions.assertEquals(3712.43, sideNav.getDoubleValueOfTotalAdjustedMonthlyIncome());
        });
    }

    @Order(Integer.MAX_VALUE)
    @Test
    @Description("IA-3055 Check if select borrowers triggers recalculation")
    void checkIfSelectBorrowersTriggersRecalculation() {
        SideNav sideNav = IncomeAnalyzerPage.sideNavView.sideNav;
        sideNav.getApplicant(sixAppId).checkBox.setCheckboxValue(true);
        sideNav.getApplicant(fourAppId).checkBox.setCheckboxValue(true);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            Assertions.assertEquals("$7,399.20", IncomeAnalyzerPage
                    .summaryView.summary.totalMonthlyIncomeContainer.TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE.getText());
            Assertions.assertEquals(7399.20, sideNav.getDoubleValueOfTotalAdjustedMonthlyIncome());
        });
    }

    public String getApplicantShortFullName(String firstName, String lastName) {
        return firstName + " " + lastName.charAt(0) + ".";
    }
}
package com.capsilon.incomeanalyzer.automation.ui.componentTests.secondaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.ApplicantIncomeCategory;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeGroup;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.IncomeTypeSection;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentBody;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.EmploymentHeader;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.SideNavApplicant;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.ui.view.SideNavView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.restoreApplicantDefaults;
import static com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView.collapseOrExpandTab;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory.W2;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Secondary Employment specification")
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryEmploymentTest")
class SecondaryEmploymentTest extends TestBaseUI {

    private final BigDecimal paystubYTDOvertimeAmount = bigD(12000);
    private final BigDecimal voeMonthlyOvertimeAmount = bigD(1000);
    private final String thirdEmploymentName = "SecEmp2";
    private final String testAttr = "data-testid";
    IAFolderBuilder folderBuilderSecEmpTest = createFolderBuilder("IAUISecEmp");
    private long borrowerId;
    private long coBorrowerId;

    @BeforeAll
    void generateFolderAndUploadDocuments() {
        folderBuilderSecEmpTest.generateSecondaryJobsLoanDocument()
                .addNewEmployment(thirdEmploymentName,
                        folderBuilderSecEmpTest.getCoBorrowerSecondJobStartDate(),
                        "", false, true, "0.00");

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(folderBuilderSecEmpTest.uiBuild());

        dataUpload = createUploadObject(folderBuilderSecEmpTest);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "04/31/" + YEAR_TO_DATE,
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
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
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
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerPreviousEmployment(),
                        "08/01/" + YEAR_TO_DATE,
                        "08/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "08/07/" + YEAR_TO_DATE,
                        "1500",
                        voeMonthlyOvertimeAmount.toString(),
                        "12000",
                        "00"))

                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getCoBorrowerFullName(),
                        folderBuilderSecEmpTest.getCoBorrowerSSN(),
                        folderBuilderSecEmpTest.getCoBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getCoBorrowerCurrentEmployment(),
                        "05/01/" + YEAR_TO_DATE,
                        "05/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getCoBorrowerFullName(),
                        folderBuilderSecEmpTest.getCoBorrowerSSN(),
                        folderBuilderSecEmpTest.getCoBorrowerCollaboratorId(),
                        thirdEmploymentName,
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderSecEmpTest.getCoBorrowerFullName(),
                        folderBuilderSecEmpTest.getCoBorrowerSSN(),
                        folderBuilderSecEmpTest.getCoBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
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
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        folderBuilderSecEmpTest.getCoBorrowerFullName(),
                        folderBuilderSecEmpTest.getCoBorrowerSSN(),
                        folderBuilderSecEmpTest.getCoBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getCoBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "01/31/" + (YEAR_TO_DATE + 1),
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setRate("1234.56")
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

        RestGetResponse rsp = RestGetLoanData.getApplicationData(folderBuilderSecEmpTest.getFolderId());
        borrowerId = rsp.getApplicant(folderBuilderSecEmpTest.getBorrowerFullName()).getId();
        coBorrowerId = rsp.getApplicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).getId();
    }

    @Test
    @Order(1)
    @Description("IA-1294 IA-2842 IA-2840 Check If Primary Job Group Section Is Always Expanded")
    void checkIfPrimaryJobGroupSectionIsAlwaysExpanded() {
        restoreApplicantDefaults(borrowerId);
        restoreApplicantDefaults(coBorrowerId);

        ApplicantView view = new ApplicantView();
        ElementsCollection borrowerIncomeGroupCollection = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).INCOME_GROUP_COLLECTION;
        borrowerIncomeGroupCollection.get(0).shouldHave(Condition.cssClass("mat-expanded"));
        collapseOrExpandTab(view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.COMPONENT_CONTAINER, false);
        borrowerIncomeGroupCollection.get(0).shouldNotHave(Condition.cssClass("mat-expanded"));

        ElementsCollection coBorrowerIncomeGroupCollection = view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).INCOME_GROUP_COLLECTION;
        coBorrowerIncomeGroupCollection.get(0).shouldHave(Condition.cssClass("mat-expanded"));
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName());
        borrowerIncomeGroupCollection.get(0).shouldHave(Condition.cssClass("mat-expanded"));
    }

    @Test
    @Order(1)
    @Description("IA-1294 Check If Secondary Job Group Sections Can Be Expanded")
    void checkIfSecondaryJobGroupSectionsCanBeExpanded() {
        restoreApplicantDefaults(borrowerId);
        restoreApplicantDefaults(coBorrowerId);

        ApplicantView view = new ApplicantView();
        ElementsCollection borrowerIncomeGroupCollection = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).INCOME_GROUP_COLLECTION;
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.COMPONENT_CONTAINER.click();
        borrowerIncomeGroupCollection.get(1).shouldNotHave(Condition.cssClass("mat-expanded"));
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.COMPONENT_CONTAINER.click();
        borrowerIncomeGroupCollection.get(1).shouldHave(Condition.cssClass("mat-expanded"));
        ElementsCollection coBorrowerIncomeGroupCollection = view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).INCOME_GROUP_COLLECTION;
        coBorrowerIncomeGroupCollection.get(1).shouldNotHave(Condition.cssClass("mat-expanded"));
        coBorrowerIncomeGroupCollection.get(2).shouldHave(Condition.cssClass("mat-expanded"));
        collapseOrExpandTab(view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_1).HEADER.COMPONENT_CONTAINER, true);
        collapseOrExpandTab(view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_2).HEADER.COMPONENT_CONTAINER, true);
        coBorrowerIncomeGroupCollection.get(1).shouldHave(Condition.cssClass("mat-expanded"));
        coBorrowerIncomeGroupCollection.get(2).shouldHave(Condition.cssClass("mat-expanded"));
    }

    @Test
    @Order(1)
    @Description("IA-1294 Check If Primary Job Group Section Checkboxes Are Selected By Default And Secondary If Income Is Greater Than Zero")
    void checkIfJobGroupSectionCheckboxesAreSelectedByDefaultCorrectly() {
        restoreApplicantDefaults(borrowerId);
        restoreApplicantDefaults(coBorrowerId);

        ApplicantView view = new ApplicantView();
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.CHECKBOX
                .shouldBeDisplayed().checkIfCheckboxIsEnabled().checkIfCheckboxIsSelected();
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX
                .shouldBeDisplayed().checkIfCheckboxIsEnabled().checkIfCheckboxIsSelected();

        view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.CHECKBOX
                .shouldBeDisplayed().checkIfCheckboxIsEnabled().checkIfCheckboxIsSelected();
        view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroupByCurrentEmployerName(folderBuilderSecEmpTest.getCoBorrowerSecondEmployment()).HEADER.CHECKBOX
                .shouldBeDisplayed().checkIfCheckboxIsEnabled().checkIfCheckboxIsSelected();
        view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroupByCurrentEmployerName(thirdEmploymentName).HEADER.CHECKBOX
                .shouldBeDisplayed().checkIfCheckboxIsEnabled().checkIfCheckboxIsUnselected();
    }

    @Test
    @Order(1)
    @Description("IA-1294 IA-2641 Check If Job Group Section Checkboxes Are Working Correctly But Do Not Change Income Values")
    void checkIfJobGroupSectionCheckboxesAreWorkingCorrectly() {
        restoreApplicantDefaults(borrowerId);
        restoreApplicantDefaults(coBorrowerId);

        ApplicantView view = new ApplicantView();
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () ->
                assertAll("Default income values assertions",
                        () -> assertTrue(
                                bigD(0)
                                        .compareTo(view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.getIncomeValue()) < 0, "Primary employment income was not greater than zero"),
                        () -> assertTrue(
                                bigD(0)
                                        .compareTo(view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.getIncomeValue()) < 0, "Secondary employment income was not greater than zero")
                )
        );

        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.CHECKBOX.setCheckboxValue(false);
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.setCheckboxValue(false);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () ->
                assertAll("Default income values assertions",
                        () -> assertTrue(
                                bigD(0)
                                        .compareTo(view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.getIncomeValue()) == 0, "Primary employment income was changed to zero"),
                        () -> assertTrue(
                                bigD(0)
                                        .compareTo(view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.getIncomeValue()) == 0, "Secondary employment income was changed to zero")
                )
        );
        SideNavView sideNavView = new SideNavView();
        sideNavView.sideNav.getApplicant(folderBuilderSecEmpTest.getBorrowerFullName()).checkBox.setCheckboxValue(true);
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).HEADER.CHECKBOX.setCheckboxValue(true);
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.CHECKBOX.setCheckboxValue(true);
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.CHECKBOX.setCheckboxValue(true);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () ->
                assertAll("Default income values assertions",
                        () -> assertTrue(
                                bigD(0)
                                        .compareTo(view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.getIncomeValue()) < 0, "Primary employment income was not greater than zero"),
                        () -> assertTrue(
                                bigD(0)
                                        .compareTo(view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.getIncomeValue()) < 0, "Secondary employment income was not greater than zero")
                )
        );
    }

    @Test
    @Order(1)
    @Description("IA-1294 Check If Primary Job Group Is Shown First")
    void checkIfPrimaryJobGroupIsShownFirst() {
        ApplicantView view = new ApplicantView();
        ElementsCollection incomeGroupCollection = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).INCOME_GROUP_COLLECTION;
        incomeGroupCollection.get(0).shouldHave(Condition.attribute(testAttr, IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME));
    }

    @Test
    @Order(1)
    @Description("IA-1294 Check If Secondary Job Group Name Has No Number If There Is Only One")
    void checkIfSecondaryJobGroupNameHasNoNumberIfThereIsOnlyOne() {
        ApplicantView view = new ApplicantView();
        ElementsCollection incomeGroupCollection = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).INCOME_GROUP_COLLECTION;
        incomeGroupCollection.get(1).shouldHave(Condition.attribute(testAttr, IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER));
    }

    @Test
    @Order(1)
    @Description("IA-1294 Check If Secondary Job Group Names Have Number If There Is More Than One")
    void checkIfSecondaryJobGroupNamesHaveNumberIfThereIsMoreThanOne() {
        ApplicantView view = new ApplicantView();
        ElementsCollection incomeGroupCollection = view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2).INCOME_GROUP_COLLECTION;
        incomeGroupCollection.get(1).shouldHave(Condition.attribute(testAttr, IncomeCategoryIncomeGroup.SEC_GROUP_NAME_1));
        incomeGroupCollection.get(2).shouldHave(Condition.attribute(testAttr, IncomeCategoryIncomeGroup.SEC_GROUP_NAME_2));
    }

    @Test
    @Order(2)
    @Description("IA-1194 Check If Past Job is shown in each current job")
    void checkIfPasJobIsShownInEachCurrentJob() {
        ApplicantView view = new ApplicantView();

        EmploymentHeader dataFromHeaderPreviousJobPrimary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypePrimaryGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).HEADER;

        assertEquals("Awesome Computers", dataFromHeaderPreviousJobPrimary.NAME.getText());
        assertEquals("Previous", dataFromHeaderPreviousJobPrimary.TAGS.get(0).getText());

        EmploymentHeader dataFromHeaderPreviousJobSecondary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypeSecondaryNoNumberGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).HEADER;

        assertEquals("Awesome Computers", dataFromHeaderPreviousJobSecondary.NAME.getText());
        assertEquals("Previous", dataFromHeaderPreviousJobPrimary.TAGS.get(0).getText());
    }

    @Test
    @Order(3)
    @Description("IA-1194 Check If Past Job is shown in each current job")
    void checkIfPastJobCheckboxesArehownInEachCurrentJob() {
        ApplicantView view = new ApplicantView();

        EmploymentBody checkboxesPreviousJobPrimary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypePrimaryGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        checkboxesPreviousJobPrimary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobPrimary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsEnabled();
        checkboxesPreviousJobPrimary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobPrimary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsEnabled();
        checkboxesPreviousJobPrimary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobPrimary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsEnabled();

        EmploymentBody checkboxesPreviousJobSecondary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypeSecondaryNoNumberGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        checkboxesPreviousJobSecondary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobSecondary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsEnabled();
        checkboxesPreviousJobSecondary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobSecondary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsEnabled();
        checkboxesPreviousJobSecondary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobSecondary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsEnabled();
    }

    @Test
    @Order(4)
    @Description("IA-1194 Check If past job can be selected only in one primary current job")
    void checkIfPastJobCanBeSelectedOnlyInOnePrimaryJob() {
        ApplicantView view = new ApplicantView();
        EmploymentBody checkboxesPreviousJobPrimary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypePrimaryGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        //select past job in primary job
        checkboxesPreviousJobPrimary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.setCheckboxValue(true);
        checkboxesPreviousJobPrimary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.setCheckboxValue(true);
        checkboxesPreviousJobPrimary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.setCheckboxValue(true);

        //check if past job is selected in primary job
        checkboxesPreviousJobPrimary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        checkboxesPreviousJobPrimary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        checkboxesPreviousJobPrimary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();

        EmploymentBody checkboxesPreviousJobSecondary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypeSecondaryNoNumberGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        //check if past job is unselected in secondary job
        checkboxesPreviousJobSecondary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobSecondary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobSecondary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
    }

    @Test
    @Order(5)
    @Description("IA-1194 Check If past job can be selected only in one secondary current job")
    void checkIfPastJobCanBeSelectedOnlyInOneSecondaryJob() {
        ApplicantView view = new ApplicantView();
        EmploymentBody checkboxesPreviousJobSecondary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypeSecondaryNoNumberGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        //select past job in secondary job
        checkboxesPreviousJobSecondary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.setCheckboxValue(true);
        checkboxesPreviousJobSecondary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.setCheckboxValue(true);
        checkboxesPreviousJobSecondary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.setCheckboxValue(true);

        //check if past job is selected in secondary job
        checkboxesPreviousJobSecondary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        checkboxesPreviousJobSecondary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        checkboxesPreviousJobSecondary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();

        EmploymentBody checkboxesPreviousJobPrimary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypePrimaryGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        //check if past job is unselected in primary job
        checkboxesPreviousJobPrimary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobPrimary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobPrimary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
    }

    @Test
    @Order(6)
    @Description("IA-1194 Check If past job can be selected only one year in primary job")
    void checkIfPastJobCanBeSelectedOnlyInOneYearInPrimaryJob() {
        ApplicantView view = new ApplicantView();
        EmploymentBody checkboxesPreviousJobPrimary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypePrimaryGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        //select one year of past job in primary job
        checkboxesPreviousJobPrimary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.setCheckboxValue(true);

        //check if one year prior of past job is selected in primary job
        checkboxesPreviousJobPrimary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobPrimary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsSelected();
        checkboxesPreviousJobPrimary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();

        EmploymentBody checkboxesPreviousJobSecondary = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeTypeSecondaryNoNumberGroup(IncomePartType.BASE_PAY).BODY.employment(folderBuilderSecEmpTest.getBorrowerPreviousEmployment()).BODY;

        //check if past job is unselected in secondary job
        checkboxesPreviousJobSecondary.employmentYear(YEAR_TO_DATE.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobSecondary.employmentYear(ONE_YEAR_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
        checkboxesPreviousJobSecondary.employmentYear(TWO_YEARS_PRIOR.toString()).CHECKBOX.checkIfCheckboxIsUnselected();
    }

    @Test
    @Order(7)
    @Description("IA-2718 Show YTD (YYYY) when YTD is different across current jobs ")
    void YTDIsShownWhenYTDIsDifferentAcrossCurrentJobs() {
        dataUpload.removeDocumentsFromFolder();
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600", "7200")))
                .importDocumentList();
        refreshFolder();

        ApplicantView view = new ApplicantView();
        SelenideElement currentEmploymentDate = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).incomeType(IncomePartType.BASE_PAY)
                .BODY.employment(folderBuilderSecEmpTest.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).HEADER;
        SelenideElement secondaryEmploymentDate = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).incomeType(IncomePartType.BASE_PAY)
                .BODY.employment(folderBuilderSecEmpTest.getBorrowerSecondEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString()).HEADER;

        assertAll("Check correct date format YTD + YYYY",
                () -> assertEquals("YTD (" + ONE_YEAR_PRIOR + ")", currentEmploymentDate.getText()),
                () -> assertEquals("YTD (" + YEAR_TO_DATE + ")", secondaryEmploymentDate.getText())
        );
    }

    @Test
    @Description("IA-2092 Check If Income Section Title Is Visible At All Times")
    void checkIfIncomeSectionTitleIsVisibleAtAllTimes() {
        ApplicantView view = new ApplicantView();
        ApplicantIncomeCategory w2IncomeCategory = view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2);
        w2IncomeCategory.multiCurrentJobView().expandAllIncomeTypesForPrimaryGroup().COMPONENT_CONTAINER.scrollIntoView(false);
        view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeTypePrimaryGroupList().forEach(incomeSection -> {
            incomeSection.COMPONENT_CONTAINER.scrollIntoView(false);
            Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
                assertTrue(incomeSection.HEADER.COMPONENT_CONTAINER.getLocation().getY() < 170, "Automation test has not scrolled down to section");
                assertTrue(incomeSection.HEADER.COMPONENT_CONTAINER.getLocation().getY() > 130, "Section header is not visible when scrolled down");
            });
        });
    }

    @Test
    @Description("IA-2535 Check If Income Group Title Is Visible At All Times")
    void checkIfJobGroupTitleIsVisibleAtAllTimes() {
        ApplicantView view = new ApplicantView();
        ApplicantIncomeCategory w2IncomeCategory = view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2);
        w2IncomeCategory.multiCurrentJobView().expandAllIncomeTypesForPrimaryGroup().COMPONENT_CONTAINER.scrollIntoView(false);
        List<IncomeTypeSection> primarySections = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeTypePrimaryGroupList();
        primarySections.get(primarySections.size() - 1).HEADER.COMPONENT_CONTAINER.scrollIntoView(false);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
            assertTrue(w2IncomeCategory.multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.COMPONENT_CONTAINER.getLocation().getY() < 120, "Automation test has not scrolled down to section");
            assertTrue(w2IncomeCategory.multiCurrentJobView().incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.COMPONENT_CONTAINER.getLocation().getY() > 80, "Section header is not visible when scrolled down");
        });
    }

    @Test
    @Description("IA-2535 Check If Income Category Title Is Visible At All Times")
    void checkIfIncomeCategoryTitleIsVisibleAtAllTimes() {
        ApplicantView view = new ApplicantView();
        ApplicantIncomeCategory w2IncomeCategory = view.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(W2);
        w2IncomeCategory.multiCurrentJobView().expandAllIncomeTypesForPrimaryGroup().COMPONENT_CONTAINER.scrollIntoView(false);
        List<IncomeTypeSection> primarySections = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView().incomeTypePrimaryGroupList();
        primarySections.get(primarySections.size() - 1).HEADER.COMPONENT_CONTAINER.scrollIntoView(false);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TEN_SECONDS, () -> {
            assertTrue(w2IncomeCategory.HEADER.COMPONENT_CONTAINER.getLocation().getY() < 70, "Automation test has not scrolled down to section");
            assertTrue(w2IncomeCategory.HEADER.COMPONENT_CONTAINER.getLocation().getY() > 30, "Section header is not visible when scrolled down");
        });
    }

    @Test
    @Description("IA-2826 deselecting any income, should not deselect applicant")
    void deselectingAnyIncomeShouldNotDeselectApplicant() {
        ApplicantView view = new ApplicantView();
        SideNavView sideView = new SideNavView();
        SideNavApplicant applicantCheckbox = sideView.sideNav.getApplicant(folderBuilderSecEmpTest.getBorrowerFullName());
        IncomeCategoryIncomeGroup incomeGroupCheckbox = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(W2)
                .multiCurrentJobView();

        applicantCheckbox.checkBox.setCheckboxValue(true);
        incomeGroupCheckbox.incomeTypeSecondaryNoNumberGroup(IncomePartType.BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(true);
        incomeGroupCheckbox.incomeTypeSecondaryNoNumberGroup(IncomePartType.OVERTIME).HEADER.CHECKBOX.setCheckboxValue(true);
        incomeGroupCheckbox.incomeTypeSecondaryNoNumberGroup(IncomePartType.BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(false);
        incomeGroupCheckbox.incomeTypeSecondaryNoNumberGroup(IncomePartType.OVERTIME).HEADER.CHECKBOX.setCheckboxValue(false);
        applicantCheckbox.checkBox.checkIfCheckboxIsSelected();
    }

    @Test
    @Description("IA-2997 Check if Primary Employment stays selected when all incomes are deselected")
    void checkIfPrimaryEmploymentStaysSelectedWhenAllIncomesAreDeselected(){
        ApplicantView view = new ApplicantView();
        IncomeCategoryIncomeGroup incomeGroupCheckbox = view.applicant(folderBuilderSecEmpTest.getBorrowerFullName().toLowerCase()).incomeCategory(W2)
                .multiCurrentJobView();
        incomeGroupCheckbox.incomeTypePrimaryGroup(IncomePartType.BASE_PAY).HEADER.CHECKBOX.setCheckboxValue(false);
        incomeGroupCheckbox.incomeTypePrimaryGroup(IncomePartType.OVERTIME).HEADER.CHECKBOX.setCheckboxValue(false);
        incomeGroupCheckbox.incomeTypePrimaryGroup(IncomePartType.COMMISSIONS).HEADER.CHECKBOX.setCheckboxValue(false);
        incomeGroupCheckbox.incomeTypePrimaryGroup(IncomePartType.BONUS).HEADER.CHECKBOX.setCheckboxValue(false);
        incomeGroupCheckbox.incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.CHECKBOX.checkIfCheckboxIsSelected();
    }
}
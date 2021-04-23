package com.capsilon.incomeanalyzer.automation.ui.componentTests.secondaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeGroup;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Check if Secondary and Primary view works correctly ")
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryJobsTabbedViewTest")
public class SecondaryJobsTabbedViewTest extends TestBaseUI {

    private final IAFolderBuilder folderBuilderSecEmpTest = createFolderBuilder("IAUIScndTbs");
    private final String thirdEmploymentName = "SecEmp2";
    private final BigDecimal paystubYTDOvertimeAmount = bigD(12000);

    @BeforeAll
    void generateFolderAndUploadDocuments() {
        folderBuilderSecEmpTest.generateSecondaryJobsLoanDocument()
                .addNewEmployment(thirdEmploymentName,
                        folderBuilderSecEmpTest.getBorrowerSecondJobStartDate(),
                        "", true, true, "0.00");

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(folderBuilderSecEmpTest.uiBuild());

        dataUpload = createUploadObject(folderBuilderSecEmpTest);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "180.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "20.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "100.50", "180.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "50.50", "64.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "30.00", "10.00", "20.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        folderBuilderSecEmpTest.getBorrowerSecondEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "20.00", "16.00", "296.00", "1,308.20"),
                        new PaystubData.PaystubIncomeRow(REGULAR, "80.50", "25.50", "1,193.28", "32,632.21"),
                        new PaystubData.PaystubIncomeRow(PTO, "17.52", "10.00", "0.00", "2,987.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderSecEmpTest.getBorrowerFullName(),
                        folderBuilderSecEmpTest.getBorrowerSSN(),
                        folderBuilderSecEmpTest.getBorrowerCollaboratorId(),
                        thirdEmploymentName,
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(PaystubIncomeGroups.OVERTIME, "", "", "250", paystubYTDOvertimeAmount.toString()),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "150", "1500")))
                .importDocumentList();
        refreshFolder();
    }

    @Test
    @Description("IA-2648 the names of anchors are Primary, Secondary 1, Secondary 2, etc.")
    void checkIfSecondaryAndPrimaryTabsArePresent() {
        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.PRIMARY_EMPLOYMENT_TAB.should(Condition.exist);
        applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.SECONDARY_EMPLOYMENT_TAB_1.should(Condition.exist);
        applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.SECONDARY_EMPLOYMENT_TAB_2.should(Condition.exist);
        applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.SECONDARY_EMPLOYMENT_TAB_1.should(Condition.exist);
        applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.PRIMARY_EMPLOYMENT_TAB.should(Condition.exist);
    }

    @Test
    @Description("IA-2648 after clicking on the anchor, the related section scrolls up in its latest state (expanded or collapsed)")
    void checkIfRelatedSectionScrollsUp() {
        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.SECONDARY_EMPLOYMENT_TAB_2.click();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            int incomeGroupVerticalLocation = applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                    .incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_2).HEADER.NAME.toWebElement().getLocation().getY();

            assertAll("check if view scrolls to secondary employment 2 in first applicant",
                    () -> assertTrue(incomeGroupVerticalLocation > 100,
                            "section group view was above browser window"),
                    () -> assertTrue(incomeGroupVerticalLocation < getWebDriver().manage().window().getSize().getHeight() - 300,
                            "section group view was below browser window")
            );
        });

        applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.PRIMARY_EMPLOYMENT_TAB.click();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            int incomeGroupVerticalLocation = applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                    .incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.NAME.toWebElement().getLocation().getY();

            assertAll("check if view scrolls to primary employment in first applicant",
                    () -> assertTrue(incomeGroupVerticalLocation > 100,
                            "section group view was above browser window"),
                    () -> assertTrue(incomeGroupVerticalLocation < getWebDriver().manage().window().getSize().getHeight() - 300,
                            "section group view was below browser window")
            );
        });

        applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.SECONDARY_EMPLOYMENT_TAB_1.click();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            int incomeGroupVerticalLocation = applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                    .incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).HEADER.NAME.toWebElement().getLocation().getY();

            assertAll("check if view scrolls to secondary employment 1 in second applicant",
                    () -> assertTrue(incomeGroupVerticalLocation > 100,
                            "section group view was above browser window"),
                    () -> assertTrue(incomeGroupVerticalLocation < getWebDriver().manage().window().getSize().getHeight() - 300,
                            "section group view was below browser window")
            );
        });

        applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.PRIMARY_EMPLOYMENT_TAB.click();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            int incomeGroupVerticalLocation = applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                    .incomeGroupWithoutExpanding(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).HEADER.NAME.toWebElement().getLocation().getY();

            assertAll("check if view scrolls to primary employment 2 in second applicant",
                    () -> assertTrue(incomeGroupVerticalLocation > 100,
                            "section group view was above browser window"),
                    () -> assertTrue(incomeGroupVerticalLocation < getWebDriver().manage().window().getSize().getHeight() - 300,
                            "section group view was below browser window")
            );
        });
    }

    @Test
    @Description("IA-2648 when the user scrolls, the section anchor he is in is highlighted")
    void checkIfSectionAnchorIsHighlightedWhenUserScrollsUp() {
        ApplicantView applicantView = new ApplicantView();

        applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                .incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_2).COMPONENT_CONTAINER.scrollIntoView(true);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.SECONDARY_EMPLOYMENT_TAB_2
                        .shouldHave(attributeContains("class", "active")));

        applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                .incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).COMPONENT_CONTAINER.scrollIntoView(true);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.PRIMARY_EMPLOYMENT_TAB
                        .shouldHave(attributeContains("class", "active")));

        applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                .incomeGroup(IncomeCategoryIncomeGroup.PRIMARY_GROUP_NAME).COMPONENT_CONTAINER.scrollIntoView(true);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                applicantView.applicant(folderBuilderSecEmpTest.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.PRIMARY_EMPLOYMENT_TAB
                        .shouldHave(attributeContains("class", "active")));

        applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).multiCurrentJobView()
                .incomeGroup(IncomeCategoryIncomeGroup.SEC_GROUP_NAME_NO_NUMBER).COMPONENT_CONTAINER.scrollIntoView(true);
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                applicantView.applicant(folderBuilderSecEmpTest.getCoBorrowerFullName()).incomeCategory(IncomePartCategory.W2).HEADER.SECONDARY_EMPLOYMENT_TAB_1
                        .shouldHave(attributeContains("class", "active")));
    }
}
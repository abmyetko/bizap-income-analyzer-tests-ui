package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document.DocumentsTooltip;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document.YearDocument;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.DateUtilities.formatDate;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.MM_DD_YYYY_F_SLASH;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.M_D_YY_F_SLASH;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory.W2;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.COMMISSIONS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Check if 2 Paystubs calculation works correct")
@Execution(CONCURRENT)
@ResourceLock(value = "TwoPaystubsTest")
class TwoPaystubsTest extends TestBaseUI {

    private final IAFolderBuilder iafolderBuilder = createFolderBuilder("IAUITwoPs");
    private final String newerDocEndDate = "06/30/" + YEAR_TO_DATE;
    private final String olderDocStartDate = "04/01/" + YEAR_TO_DATE;
    private final String olderDocEndDate = "04/30/" + YEAR_TO_DATE;
    ApplicantView applicantView;
    YearDocument yearDocument;

    @BeforeAll
    void loginToIABizappAndUploadDefaultApplicantData() {
        iafolderBuilder
                .setSignDate("02/28/" + YEAR_TO_DATE)
                .setBorrowerFirstName("John")
                .setBorrowerLastName("Homeowner")
                .setBorrowerCurrentEmployment("Awesome Computers Inc")
                .setBorrowerYearsOnThisJob("2")
                .setBorrowerPreviousEmployment("Previous Computer Inc")
                .setBorrowerPreviousJobStartDate("01/01/2015")
                .setBorrowerPreviousJobEndDate("06/30/2018")

                .setCoBorrowerFirstName("Mary")
                .setCoBorrowerLastName("Homeowner")
                .setCoBorrowerCurrentEmployment("Blue Younder Airlines Inc")
                .setCoBorrowerYearsOnThisJob("2")
                .setCoBorrowerPreviousEmployment("Previous Blue Inc")
                .setCoBorrowerPreviousJobStartDate("01/01/2015")
                .setCoBorrowerPreviousJobEndDate("06/30/2018")
                .generateLoanDocument();

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(iafolderBuilder.uiBuild());

        dataUpload = createUploadObject(iafolderBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        null,
                        newerDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1111", "4444"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "2000", "4444"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "2000", "4444"))
                        .setManualFrequency(IncomeFrequency.MONTHLY)
                        .setYtdGrossIncomeAmount("8888"))
                .addDocument(dataUpload.createCustomPaystub(
                        iafolderBuilder.getBorrowerFullName(),
                        iafolderBuilder.getBorrowerSSN(),
                        iafolderBuilder.getBorrowerCollaboratorId(),
                        iafolderBuilder.getBorrowerCurrentEmployment(),
                        olderDocStartDate,
                        olderDocEndDate,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "", "6000"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "", "6600"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "1000", "6600"))
                        .setYtdGrossIncomeAmount("19200"))
                .importDocumentList();

        refreshFolder();

        applicantView = new ApplicantView();

    }

    @Test
    @Order(1)
    @Description("IA-2316 Tooltip in Base Pay should correctly display information about used in calculation Paystubs")
    void checkBasePay2PsInformation() {
        yearDocument = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB);
        validateTooltip(yearDocument);
        assertEquals("$1,111.00", yearDocument.getDocumentLocatorsMap().get("Gross Pay").text());
        assertEquals("$1,111.00", yearDocument.getDocumentLocatorsMap().get("Projected Monthly Income").text());
        assertEquals("$1,000.00", yearDocument.getDocumentLocatorsMap().get("Actual YTD Avg Income").text());
    }

    @Test
    @Order(2)
    @Description("IA-2324 Tooltip in Commissions should correctly display information about used in calculation Paystubs")
    @EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
    void checkCommissions2PsInformation() {
        yearDocument = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.COMMISSIONS).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB);
        validateTooltip(yearDocument);
        assertEquals("$6,600.00", yearDocument.getDocumentLocatorsMap().get("Gross Pay").text());
        assertEquals("$1,100.00", yearDocument.getDocumentLocatorsMap().get("Actual YTD Avg Income").text());

    }

    @Test
    @Order(3)
    @Description("IA-2467 IA-2572 Tooltip in Bonus should correctly display information about used in calculation Paystubs")
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    void checkBonus2PsInformation() {
        yearDocument = applicantView.applicant(iafolderBuilder.getBorrowerFullName()).incomeCategory(W2)
                .onlyPrimaryJobView().incomeType(IncomePartType.BONUS).BODY.employment(iafolderBuilder.getBorrowerCurrentEmployment())
                .BODY.employmentYear(YEAR_TO_DATE.toString()).document(SummaryDocumentType.PAYSTUB);
        validateTooltip(yearDocument);
        assertEquals("$6,600.00", yearDocument.getDocumentLocatorsMap().get("Gross Pay").text());
        assertEquals("$1,100.00", yearDocument.getDocumentLocatorsMap().get("Actual YTD Avg Income").text());

    }

    private void validateTooltip(YearDocument yearDocument) {
        int firstDocId = 0;
        int secondDocId = 1;
        String missingDate = "N/A";
        assertEquals("Paystub(2)", yearDocument.DOCUMENT_NAME.getText(), "Should be displayed information about 2 Paystubs");

        DocumentsTooltip documentsTooltip = yearDocument.documentsTooltip();
        assertEquals(2, documentsTooltip.TOOLTIP_ROWS.size(), "Tooltip should have information about 2 Paystubs");
        assertEquals("Paystub", documentsTooltip.TOOLTIP_ROWS.get(0).DOCUMENT_NAME.getText());
        assertEquals("Paystub", documentsTooltip.TOOLTIP_ROWS.get(1).DOCUMENT_NAME.getText());
        if (!documentsTooltip.TOOLTIP_ROWS.get(0).START_DATE.getText().equals(missingDate)) {
            firstDocId = 1;
            secondDocId = 0;
        }
        assertEquals(missingDate, documentsTooltip.TOOLTIP_ROWS.get(firstDocId).START_DATE.getText());
        assertEquals(formatDate(MM_DD_YYYY_F_SLASH, newerDocEndDate, M_D_YY_F_SLASH), documentsTooltip.TOOLTIP_ROWS.get(firstDocId).END_DATE.getText());
        assertEquals(formatDate(MM_DD_YYYY_F_SLASH, olderDocStartDate, M_D_YY_F_SLASH), documentsTooltip.TOOLTIP_ROWS.get(secondDocId).START_DATE.getText());
        assertEquals(formatDate(MM_DD_YYYY_F_SLASH, olderDocEndDate, M_D_YY_F_SLASH), documentsTooltip.TOOLTIP_ROWS.get(secondDocId).END_DATE.getText());
    }
}

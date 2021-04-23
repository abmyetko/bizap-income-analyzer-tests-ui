package com.capsilon.incomeanalyzer.automation.ui.componentTests.primaryEmployment.applicantTests;

import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document.DocumentsTooltip;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document.YearDocument;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.ONE_YEAR_PRIOR;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.bigD;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.W2;
import static com.codeborne.selenide.Condition.text;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Payroll W2 company test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("iaCanonical")
@Execution(CONCURRENT)
@ResourceLock(value = "PayrollTooltipW2Test")
public class PayrollW2CompanyTest extends TestBaseUI {

    RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
    String payrollCompanyName = "Other Name Inc";
    private IAFolderBuilder folderBuilderPayroll = createFolderBuilder("IAUIPrllTst");

    @BeforeAll
    void setupTestFolder() {
        loginCreateLoanAndGoToIncomeAnalyzerBizapp(folderBuilderPayroll.generateLoanDocument().uiBuild());

        uploadCanonical(generateW2Document("W2-employment-name", folderBuilderPayroll.getBorrowerCurrentEmployment(),
                bigD(12000), bigD(0), bigD(0), bigD(0)));
        uploadCanonical(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(12000), bigD(0), bigD(0), bigD(0)));
        addAliasToCurrentEmployer(payrollCompanyName);
        refreshFolder();
    }

    @Test
    @Description("IA-2690 Check if income year W-2 documents have two documents present counter in their name")
    void checkIfTwoW2DocsArePresentInDocumentName() {
        ApplicantView view = new ApplicantView();
        view.applicant(folderBuilderPayroll.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(folderBuilderPayroll.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(W2).DOCUMENT_NAME.shouldHave(text("W2(2)"));
    }

    @Test
    @Description("IA-2690 Check if two W-2 documents tooltip with payroll company names is displayed after clicking document name")
    void checkIfPayrollCompaniesNamesArePresentAfterOpeningW2DocTooltip() {
        ApplicantView view = new ApplicantView();
        YearDocument yearDocument = view.applicant(folderBuilderPayroll.getBorrowerFullName()).incomeCategory(IncomePartCategory.W2).onlyPrimaryJobView().incomeType(IncomePartType.BASE_PAY).BODY
                .employment(folderBuilderPayroll.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString()).document(W2);

        yearDocument.DOCUMENT_NAME.shouldHave(text("W2(2)"));

        DocumentsTooltip documentsTooltip = yearDocument.documentsTooltip();
        assertEquals(2, documentsTooltip.TOOLTIP_ROWS.size(), "Tooltip should have information about 2 W-2");
        assertEquals(folderBuilderPayroll.getBorrowerCurrentEmployment(), documentsTooltip.TOOLTIP_ROWS.get(0).EMPLOYER_NAME.getText());
        assertEquals(payrollCompanyName, documentsTooltip.TOOLTIP_ROWS.get(1).EMPLOYER_NAME.getText());
    }

    private void addAliasToCurrentEmployer(String payrollCompanyName) {
        Integer applicantId = (Integer) ((HashMap<String, Object>) RestNotIARequests.getChecklistRules(folderBuilderPayroll.getFolderId()).then().extract().jsonPath().getList("applicants")
                .stream().filter(app -> ((HashMap<String, Object>) app).get("fullName").equals(folderBuilderPayroll.getBorrowerFullName())).findFirst().orElseThrow(NullPointerException::new)).get("applicantId");
        RestNotIARequests.addEmployerAlias(folderBuilderPayroll.getFolderId(), applicantId,
                RestGetLoanData.getApplicationData(folderBuilderPayroll.getFolderId())
                        .getApplicant(folderBuilderPayroll.getBorrowerFullName()).getIncome(folderBuilderPayroll.getBorrowerCurrentEmployment()).getEmployer().getName(),
                payrollCompanyName);
    }

    RestCanonicalDocument generateW2Document(String dataSourceId, String employmentName, BigDecimal wages, BigDecimal medicare, BigDecimal allocTips, BigDecimal totalTips) {
        RestCanonicalDocument generatedW2Document = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilderPayroll.getFolderId(), W2);
        canonicalDocumentMethods.setBorrowerAndJob(generatedW2Document, folderBuilderPayroll.getFolderId(),
                folderBuilderPayroll.getBorrowerFullName(),
                employmentName);
        canonicalDocumentMethods.setDatesForSelectedDocument(generatedW2Document,
                ONE_YEAR_PRIOR.toString(),
                null,
                null,
                W2);
        generatedW2Document.setDataSourceId(dataSourceId);
        generatedW2Document.getCanonicalPayload().setId(dataSourceId);
        canonicalDocumentMethods.setW2BasePayValues(generatedW2Document, bigD(wages), bigD(allocTips), bigD(medicare), bigD(0), bigD(totalTips));
        return generatedW2Document;
    }

    void uploadCanonical(RestCanonicalDocument canonicalDocument) {
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());
    }
}


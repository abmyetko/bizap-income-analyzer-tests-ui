package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestAvgIncomes;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncome;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeAvg;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.ONE_YEAR_PRIOR;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.bigD;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.W2;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("iaCanonical")
@Execution(CONCURRENT)
@ResourceLock(value = "PayrollW2Test")
public class PayrollW2Test extends TestBaseRest {

    private IAFolderBuilder folderBuilder = createFolderBuilder("IARPayrlW2");
    RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
    List<RestCanonicalDocument> documentList = new ArrayList<>();
    String payrollCompanyName = "Other Name Inc";

    @BeforeAll
    void setupTestFolder() {
        folderBuilder.generateLoanDocument().restBuild();
    }

    @BeforeEach
    void removeDocumentsFromFolder() {
        for (RestCanonicalDocument canonicalDocument : documentList) {
            canonicalDocument.setDataSourceStatus("REMOVED");
            canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());
        }
        documentList.clear();
    }

    @Test
    @Description("IA-2691 Check If Payroll Company W-2 With Same Income And No Tips Are Summed Up")
    void checkPayrollW2WithSameIncomeNoTips() {
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-employment-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(12000), bigD(0), bigD(0), bigD(0)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(12000), bigD(0), bigD(0), bigD(0)));

        addAliasToCurrentEmployer(payrollCompanyName);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestAvgIncomes incCategoryIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup()
                .getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR);
        RestPartIncome partIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR);

        assertAll("Payroll W-2 no tips",
                () -> assertEquals(bigD(2000), bigD(incCategoryIncome.getAvgMonthlyIncome()),
                        "Incorrect sum of avg incomes " + incCategoryIncome.getAvgMonthlyIncome()),
                () -> assertEquals(bigD(24000), bigD(incCategoryIncome.getGross()),
                        "Incorrect sum of gross incomes " + incCategoryIncome.getGross()),
                () -> assertEquals(bigD(24000), bigD(partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                        "Incorrect sum of document displayGrossPay " + partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                () -> assertEquals(bigD(2000), bigD(partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect sum of document monthlyAmountAvg " + partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                () -> assertEquals(2, partIncome.getAnnualSummaryDocument(W2).getDocIds().size(),
                        "Incorrect number of W-2 document ids " + partIncome.getAnnualSummaryDocument(W2).getDocIds().size())
        );
    }

    @Test
    @Description("IA-2691 Check If Payroll Company W-2 With Different Income And No Tips Are Summed Up")
    void checkPayrollW2WithDifferentIncomeNoTips() {
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-employment-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(13000), bigD(0), bigD(0), bigD(0)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(11000), bigD(0), bigD(0), bigD(0)));

        addAliasToCurrentEmployer(payrollCompanyName);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestAvgIncomes incCategoryIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup()
                .getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR);
        RestPartIncome partIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR);

        assertAll("Payroll W-2 no tips",
                () -> assertEquals(bigD(2000), bigD(incCategoryIncome.getAvgMonthlyIncome()),
                        "Incorrect sum of avg incomes " + incCategoryIncome.getAvgMonthlyIncome()),
                () -> assertEquals(bigD(24000), bigD(incCategoryIncome.getGross()),
                        "Incorrect sum of gross incomes " + incCategoryIncome.getGross()),
                () -> assertEquals(bigD(24000), bigD(partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                        "Incorrect sum of document displayGrossPay " + partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                () -> assertEquals(bigD(2000), bigD(partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect sum of document monthlyAmountAvg " + partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                () -> assertEquals(2, partIncome.getAnnualSummaryDocument(W2).getDocIds().size(),
                        "Incorrect number of W-2 document ids " + partIncome.getAnnualSummaryDocument(W2).getDocIds().size())
        );
    }

    @Test
    @Description("IA-2691 Check If Payroll Company W-2 With Different Income With All Fields In Different Fields And No Tips Are Summed Up")
    void checkPayrollW2WithDifferentIncomeDifferentFieldBothFieldsNoTips() {
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-employment-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(12000), bigD(13000), bigD(0), bigD(0)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(11000), bigD(10000), bigD(0), bigD(0)));

        addAliasToCurrentEmployer(payrollCompanyName);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestAvgIncomes incCategoryIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup()
                .getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR);
        RestPartIncome partIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR);

        assertAll("Payroll W-2 no tips",
                () -> assertEquals(bigD(2000), bigD(incCategoryIncome.getAvgMonthlyIncome()),
                        "Incorrect sum of avg incomes " + incCategoryIncome.getAvgMonthlyIncome()),
                () -> assertEquals(bigD(24000), bigD(incCategoryIncome.getGross()),
                        "Incorrect sum of gross incomes " + incCategoryIncome.getGross()),
                () -> assertEquals(bigD(24000), bigD(partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                        "Incorrect sum of document displayGrossPay " + partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                () -> assertEquals(bigD(2000), bigD(partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect sum of document monthlyAmountAvg " + partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                () -> assertEquals(2, partIncome.getAnnualSummaryDocument(W2).getDocIds().size(),
                        "Incorrect number of W-2 document ids " + partIncome.getAnnualSummaryDocument(W2).getDocIds().size())
        );
    }

    @Test
    @Description("IA-2691 Check If Payroll Company W-2 With Different Income In Different Fields And One Different Tip Each Are Summed Up")
    void checkPayrollW2WithDifferentIncomeDifferentFieldOneDiffTipEach() {
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-employment-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(0), bigD(12000), bigD(0), bigD(1000)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(12000), bigD(0), bigD(1000), bigD(0)));

        addAliasToCurrentEmployer(payrollCompanyName);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestAvgIncomes incCategoryIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup()
                .getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR);
        RestPartIncome partIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR);

        assertAll("Payroll W-2 no tips",
                () -> assertEquals(bigD(1833.33), bigD(incCategoryIncome.getAvgMonthlyIncome()),
                        "Incorrect sum of avg incomes " + incCategoryIncome.getAvgMonthlyIncome()),
                () -> assertEquals(bigD(22000), bigD(incCategoryIncome.getGross()),
                        "Incorrect sum of gross incomes " + incCategoryIncome.getGross()),
                () -> assertEquals(bigD(22000), bigD(partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                        "Incorrect sum of document displayGrossPay " + partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                () -> assertEquals(bigD(1833.33), bigD(partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect sum of document monthlyAmountAvg " + partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                () -> assertEquals(2, partIncome.getAnnualSummaryDocument(W2).getDocIds().size(),
                        "Incorrect number of W-2 document ids " + partIncome.getAnnualSummaryDocument(W2).getDocIds().size())
        );
    }

    @Test
    @Description("IA-2691 Check If Payroll Company W-2 With Same Income And All Tips Are Summed Up")
    void checkPayrollW2WithDifferentIncomeDifferentFieldAllTips() {
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-employment-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(13000), bigD(0), bigD(2000), bigD(1000)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(13000), bigD(0), bigD(4000), bigD(8000)));

        addAliasToCurrentEmployer(payrollCompanyName);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestAvgIncomes incCategoryIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup()
                .getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR);
        RestPartIncome partIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR);

        assertAll("Payroll W-2 no tips",
                () -> assertEquals(bigD(916.67), bigD(incCategoryIncome.getAvgMonthlyIncome()),
                        "Incorrect sum of avg incomes " + incCategoryIncome.getAvgMonthlyIncome()),
                () -> assertEquals(bigD(11000), bigD(incCategoryIncome.getGross()),
                        "Incorrect sum of gross incomes " + incCategoryIncome.getGross()),
                () -> assertEquals(bigD(11000), bigD(partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                        "Incorrect sum of document displayGrossPay " + partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                () -> assertEquals(bigD(916.67), bigD(partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect sum of document monthlyAmountAvg " + partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                () -> assertEquals(2, partIncome.getAnnualSummaryDocument(W2).getDocIds().size(),
                        "Incorrect number of W-2 document ids " + partIncome.getAnnualSummaryDocument(W2).getDocIds().size())
        );
    }

    @Test
    @Description("IA-2691 Check If Payroll Company W-2 Takes Only One Document For Payroll Company")
    void checkPayrollW2TakesOnlyOneDocumentForPayrollCompany() {
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-employment-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(13000), bigD(0), bigD(2000), bigD(1000)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(13000), bigD(0), bigD(4000), bigD(8000)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-payroll-name", payrollCompanyName,
                bigD(11000), bigD(0), bigD(1000), bigD(4000)));

        addAliasToCurrentEmployer(payrollCompanyName);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestAvgIncomes incCategoryIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup()
                .getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR);
        RestPartIncome partIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR);

        assertAll("Payroll W-2 no tips",
                () -> assertEquals(bigD(916.67), bigD(incCategoryIncome.getAvgMonthlyIncome()),
                        "Incorrect sum of avg incomes " + incCategoryIncome.getAvgMonthlyIncome()),
                () -> assertEquals(bigD(11000), bigD(incCategoryIncome.getGross()),
                        "Incorrect sum of gross incomes " + incCategoryIncome.getGross()),
                () -> assertEquals(bigD(11000), bigD(partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                        "Incorrect sum of document displayGrossPay " + partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                () -> assertEquals(bigD(916.67), bigD(partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect sum of document monthlyAmountAvg " + partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                () -> assertEquals(2, partIncome.getAnnualSummaryDocument(W2).getDocIds().size(),
                        "Incorrect number of W-2 document ids " + partIncome.getAnnualSummaryDocument(W2).getDocIds().size())
        );
    }

    @Test
    @Description("IA-2691 Check If W-2 For Same Employer Are Not Summed Up If Alias Was Added")
    void checkIfW2ForSameEmployerAreNotSummedUpIfAliasWasAdded() {
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-employment-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(13000), bigD(0), bigD(2000), bigD(1000)));
        uploadCanonicalAndAddToRemoveList(generateW2Document("W2-sec-empl-name", folderBuilder.getBorrowerCurrentEmployment(),
                bigD(13000), bigD(0), bigD(1000), bigD(0)));

        addAliasToCurrentEmployer(payrollCompanyName);

        RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestAvgIncomes incCategoryIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup()
                .getIncomeTypeBasePay().getAvgIncome(IncomeAvg.BASEPAY_YTD_AVG_PLUS_PREV_YR);
        RestPartIncome partIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay().getIncome(ONE_YEAR_PRIOR);

        assertAll("Payroll W-2 no tips",
                () -> assertEquals(bigD(1000), bigD(incCategoryIncome.getAvgMonthlyIncome()),
                        "Incorrect sum of avg incomes " + incCategoryIncome.getAvgMonthlyIncome()),
                () -> assertEquals(bigD(12000), bigD(incCategoryIncome.getGross()),
                        "Incorrect sum of gross incomes " + incCategoryIncome.getGross()),
                () -> assertEquals(bigD(12000), bigD(partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                        "Incorrect sum of document displayGrossPay " + partIncome.getAnnualSummaryDocument(W2).getDisplayGrossPay().getValue()),
                () -> assertEquals(bigD(1000), bigD(partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                        "Incorrect sum of document monthlyAmountAvg " + partIncome.getAnnualSummaryDocument(W2).getMonthlyAmountAvg().getValue()),
                () -> assertEquals(1, partIncome.getAnnualSummaryDocument(W2).getDocIds().size(),
                        "Incorrect number of W-2 document ids " + partIncome.getAnnualSummaryDocument(W2).getDocIds().size())
        );
    }

    private void addAliasToCurrentEmployer(String payrollCompanyName) {
        Integer applicantId = (Integer) ((HashMap<String, Object>) RestNotIARequests.getChecklistRules(folderBuilder.getFolderId()).then().extract().jsonPath().getList("applicants")
                .stream().filter(app -> ((HashMap<String, Object>) app).get("fullName").equals(folderBuilder.getBorrowerFullName())).findFirst().orElseThrow(NullPointerException::new)).get("applicantId");
        RestNotIARequests.addEmployerAlias(folderBuilder.getFolderId(), applicantId,
                RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                        .getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerCurrentEmployment()).getEmployer().getName(),
                payrollCompanyName);
    }

    RestCanonicalDocument generateW2Document(String dataSourceId, String employmentName, BigDecimal wages, BigDecimal medicare, BigDecimal allocTips, BigDecimal totalTips) {
        RestCanonicalDocument generatedW2Document = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromTemplate(folderBuilder.getFolderId(), W2);
        canonicalDocumentMethods.setBorrowerAndJob(generatedW2Document, folderBuilder.getFolderId(),
                folderBuilder.getBorrowerFullName(),
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

    void uploadCanonicalAndAddToRemoveList(RestCanonicalDocument canonicalDocument) {
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalDocument, canonicalDocument.getSiteGuid());
        documentList.add(canonicalDocument);
    }
}

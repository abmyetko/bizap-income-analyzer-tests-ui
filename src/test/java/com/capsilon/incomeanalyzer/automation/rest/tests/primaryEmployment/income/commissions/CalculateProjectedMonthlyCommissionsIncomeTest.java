package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.commissions;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.BIG_DECIMAL_PRECISION_EIGHT_POINTS;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.COMMISSIONS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;


@SuppressWarnings({"unchecked", "rawtypes"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "CalculateProjectedMonthlyCommissionsIncomeTest")
public class CalculateProjectedMonthlyCommissionsIncomeTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderCommissionsConventionalTest = createFolderBuilder("IARCoPMIcon");
    private final IAFolderBuilder folderBuilderCommissionsFHATest = createFolderBuilder("IARCoPMIfha");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderCommissionsConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderCommissionsFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]));
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder iaBuilder, MortgageType loanType) {
        iaBuilder.setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iaBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobBasePay(iaBuilder.getCoBorrowerSSN(), "0.10")
                .setPrimaryJobCommissions(iaBuilder.getCoBorrowerSSN(), "0.10")
                .restBuild();

        DataUploadObject dataUpload = createUploadObject(iaBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoePrevious(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + ONE_YEAR_PRIOR,
                        "1200",
                        "70",
                        "70",
                        "0"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + ONE_YEAR_PRIOR,
                        "1200",
                        "65",
                        "65",
                        "0"))
                .importDocumentList();

        RestGetLoanData.getApplicationData(iaBuilder.getFolderId());
    }

    @Order(1)
    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2159 Check Commissions Projected Monthly Income For Past Job Monthly Frequency")
    void checkCommissionsProjectedMonthlyIncomeForPastJobMonthlyFrequency(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertEquals(bigD(70, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getCommissions().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountCalculated().getValue(), "Incorrect calculation for Commissions Actual YTD Avg (for ONE_YEAR_PRIOR for Borrower)");
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2158 Check Commissions Projected Monthly Income For Past Job Weekly Frequency")
    void checkCommissionsProjectedMonthlyIncomeForPastJobWeeklyFrequency(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertEquals(bigD(281.67, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                .getIncome(folderBuilder.getCoBorrowerPreviousEmployment()).getCommissions().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountCalculated().getValue(), "Incorrect calculation for Commissions Actual YTD Avg (for ONE_YEAR_PRIOR for CoBorrower)");
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2160 Check Commissions Projected Monthly Income For Past Job Yearly Frequency")
    void checkCommissionsProjectedMonthlyIncomeForPastJobYearlyFrequency(IAFolderBuilder folderBuilder) {
        DataUploadObject dataUpload = createUploadObject(folderBuilder);
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.ANNUALLY,
                "01/01/" + TWO_YEARS_PRIOR,
                "04/30/" + ONE_YEAR_PRIOR,
                "1200",
                "65",
                "65",
                "0"));

        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertEquals(bigD(5.42, BIG_DECIMAL_PRECISION_EIGHT_POINTS), getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getCommissions().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE).getMonthlyAmountCalculated().getValue(), "Incorrect calculation for Commissions Actual YTD Avg (for ONE_YEAR_PRIOR for Borrower)");
    }
}
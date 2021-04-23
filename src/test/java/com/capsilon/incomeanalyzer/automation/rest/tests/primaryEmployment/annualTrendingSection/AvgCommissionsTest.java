package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.annualTrendingSection;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectIncome;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.COMMISSIONS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@Execution(CONCURRENT)
@ResourceLock(value = "AvgCommissionsTest")
public class AvgCommissionsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderEndDateFormatConventionalTest = createFolderBuilder("IARAvgCommC");
    private final IAFolderBuilder folderBuilderEndDateFormatFHATest = createFolderBuilder("IARAvgCommF");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderEndDateFormatConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderEndDateFormatFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    @Tag("integration")
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]));
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder iaBuilder, MortgageType loanType) {
        iaBuilder.setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("01/31/" + YEAR_TO_DATE)
                .setBorrowerYearsOnThisJob("4")
                .setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iaBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobBasePay(iaBuilder.getCoBorrowerSSN(), "0.10")
                .setPrimaryJobCommissions(iaBuilder.getCoBorrowerSSN(), "0.10")
                .restBuild();

        DataUploadObject dataUpload = createUploadObject(iaBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "03/01/" + YEAR_TO_DATE,
                        "03/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600.00", "1200.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1100.00", "13200.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600.00", "1200.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "1000.00", "12000.00")))
                .importDocumentList();

        RestGetLoanData.getApplicationData(iaBuilder.getFolderId());
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2153 IA-2161 Check If Average Commissions Calculations Are Correct For Only Current Job")
    void checkAvgCommissionsAreCorrectForCurrentJobOnly(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        RestIncomePart firstApplicantCurrentJobCommissionsIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions();

        RestIncomePart firstApplicantPreviousJobCommissionsIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                .getCommissions();

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantCurrentJobCommissionsIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantCurrentJobCommissionsIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(YEAR_TO_DATE).getId(), false);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(TWO_YEARS_PRIOR).getId(), false);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestIncomeType avgIncome = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions();

        assertAll("Avg Commission correct value - current job",
                () -> assertEquals(bigD(400), bigD(avgIncome.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Avg Commission for YEAR_TO_DATE for Borrower should equal 400"),
                () -> assertEquals(bigD(1100), bigD(avgIncome.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Avg Commission for ONE_YEAR_PRIOR for Borrower should equal 1100"),
                () -> assertEquals(bigD(1000), bigD(avgIncome.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Avg Commission for TWO_YEARS_PRIOR for Borrower should equal 1000")
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2153 IA-2161 Check If Average Commissions Calculations Are Correct For Previous And Current Job YTD Only")
    void checkAvgCommissionsAreCorrectForPreviousJobPlusYtdCurrent(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        RestIncomePart firstApplicantCurrentJobCommissionsIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions();
        RestIncomePart firstApplicantPreviousJobCommissionsIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                .getCommissions();

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantCurrentJobCommissionsIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
        selectIncome(primaryGroupId, firstApplicantCurrentJobCommissionsIncome.getIncome(TWO_YEARS_PRIOR).getId(), false);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestIncomeType avgIncome = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions();

        assertAll("Avg Commission correct value - previous job plus YTD current",
                () -> assertEquals(bigD(600), bigD(avgIncome.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Avg Commission for YEAR_TO_DATE for Borrower should equal 600"),
                () -> assertEquals(bigD(1000), bigD(avgIncome.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Avg Commission for ONE_YEAR_PRIOR for Borrower should equal 1100"),
                () -> assertEquals(bigD(1000), bigD(avgIncome.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Avg Commission for TWO_YEARS_PRIOR for Borrower should equal 1000")
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2153 IA-2161 Check If Average Commissions Calculations Are Correct For Current And Previous Job")
    void checkAvgCommissionsAreCorrectForCurrentAndPreviousJob(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        RestIncomePart firstApplicantCurrentJobCommissionsIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getCommissions();

        RestIncomePart firstApplicantPreviousJobCommissionsIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                .getCommissions();

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantCurrentJobCommissionsIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantCurrentJobCommissionsIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobCommissionsIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestIncomeType avgIncome = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions();

        assertAll("Avg Commission correct value - current job",
                () -> assertEquals(bigD(600), bigD(avgIncome.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Avg Commission for YEAR_TO_DATE for Borrower should equal 600"),
                () -> assertEquals(bigD(2100), bigD(avgIncome.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Avg Commission for ONE_YEAR_PRIOR for Borrower should equal 2100"),
                () -> assertEquals(bigD(2000), bigD(avgIncome.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Avg Commission for TWO_YEARS_PRIOR for Borrower should equal 2000")
        );
    }
}
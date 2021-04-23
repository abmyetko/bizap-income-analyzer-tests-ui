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
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectIncome;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@SuppressWarnings({"unchecked", "rawtypes"})
@Execution(CONCURRENT)
@ResourceLock(value = "AvgBasePayTest")
public class AvgBasePayTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderEndDateFormatConventionalTest = createFolderBuilder("IARAvgBaseC");
    private final IAFolderBuilder folderBuilderEndDateFormatFHATest = createFolderBuilder("IARAvgBaseF");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderEndDateFormatConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderEndDateFormatFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]));
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder iaBuilder, MortgageType loanType) {
        iaBuilder.setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("01/31/" + YEAR_TO_DATE)
                .setBorrowerYearsOnThisJob("4")
                .setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iaBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobBasePay(iaBuilder.getCoBorrowerSSN(), "0.10")
                .setPrimaryJobOvertime(iaBuilder.getCoBorrowerSSN(), "0.10")
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
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "1200.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1100.00", "13200.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "1200.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00")))
                .importDocumentList();

        RestGetLoanData.getApplicationData(iaBuilder.getFolderId());
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2181 Check Avg Base Pay Are Correct For Current Job Only")
    void checkAvgBasePayAreCorrectForCurrentJobOnly(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        RestIncomePart firstApplicantCurrentJobBasePayIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay();

        RestIncomePart firstApplicantPreviousJobBasePayIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                .getBasePay();

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantCurrentJobBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantCurrentJobBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(YEAR_TO_DATE).getId(), false);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), false);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestIncomeType avgIncome = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay();

        assertAll("Avg Base Pay correct value - current job",
                () -> assertEquals(bigD(400), bigD(avgIncome.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Avg Base Pay for YEAR_TO_DATE for Borrower should equal 400"),
                () -> assertEquals(bigD(1100), bigD(avgIncome.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Avg Base Pay for ONE_YEAR_PRIOR for Borrower should equal 1100"),
                () -> assertEquals(bigD(1000), bigD(avgIncome.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Avg Base Pay for TWO_YEARS_PRIOR for Borrower should equal 1000")
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2181 Check Avg Base Pay Are Correct For Previous Job Plus Ytd Current")
    void checkAvgBasePayAreCorrectForPreviousJobPlusYtdCurrent(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        RestIncomePart firstApplicantCurrentJobBasePayIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay();

        RestIncomePart firstApplicantPreviousJobBasePayIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                .getBasePay();

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantCurrentJobBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), false);
        selectIncome(primaryGroupId, firstApplicantCurrentJobBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), false);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        RestIncomeType avgIncome = getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay();

        assertAll("Avg Base Pay correct value - previous job plus YTD current",
                () -> assertEquals(bigD(600), bigD(avgIncome.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Avg Base Pay for YEAR_TO_DATE for Borrower should equal 600"),
                () -> assertEquals(bigD(1000), bigD(avgIncome.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Avg Base Pay for ONE_YEAR_PRIOR for Borrower should equal 1100"),
                () -> assertEquals(bigD(1000), bigD(avgIncome.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Avg Base Pay for TWO_YEARS_PRIOR for Borrower should equal 1000")
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Description("IA-2181 Check Avg Base Pay Are Correct For Current And Previous Job")
    void checkAvgBasePayAreCorrectForCurrentAndPreviousJob(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        RestIncomePart firstApplicantCurrentJobBasePayIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment())
                .getBasePay();

        RestIncomePart firstApplicantPreviousJobBasePayIncome = getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                .getBasePay();

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        selectIncome(primaryGroupId, firstApplicantCurrentJobBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantCurrentJobBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(YEAR_TO_DATE).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(ONE_YEAR_PRIOR).getId(), true);
        selectIncome(primaryGroupId, firstApplicantPreviousJobBasePayIncome.getIncome(TWO_YEARS_PRIOR).getId(), true);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            RestIncomeType avgIncome = response.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay();

            assertAll("Avg Base Pay correct value - current job",
                    () -> assertEquals(bigD(600), bigD(avgIncome.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Avg Base Pay for YEAR_TO_DATE for Borrower should equal 600"),
                    () -> assertEquals(bigD(2100), bigD(avgIncome.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Avg Base Pay for ONE_YEAR_PRIOR for Borrower should equal 2100"),
                    () -> assertEquals(bigD(2000), bigD(avgIncome.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Avg Base Pay for TWO_YEARS_PRIOR for Borrower should equal 2000")
            );
        });
    }
}
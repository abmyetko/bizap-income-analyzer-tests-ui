package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.OVERTIME;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@SuppressWarnings({"unchecked", "rawtypes"})
@Execution(CONCURRENT)
@ResourceLock(value = "PeriodEndDateFormatTest")
public class PeriodEndDateFormatTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderEndDateFormatConventionalTest = createFolderBuilder("IAREndDateC");
    private final IAFolderBuilder folderBuilderEndDateFormatFHATest = createFolderBuilder("IAREndDateF");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderEndDateFormatConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderEndDateFormatFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> {
            uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]);
        });
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder iaBuilder, MortgageType loanType) {
        iaBuilder.setBorrowerPreviousJobStartDate("01/01/" + TWO_YEARS_PRIOR)
                .setBorrowerPreviousJobEndDate("01/31/" + YEAR_TO_DATE)
                .setBorrowerYearsOnThisJob("4")
                .setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobCommissions(iaBuilder.getBorrowerSSN(), "0.10")
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
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "1200.00"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250.00", "3000.00"))
                        .setPayDate("03/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1100.00", "13200.00"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "230.00", "2760.00"))
                        .setPayDate("12/31/" + ONE_YEAR_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "200.00", "2400.00"))
                        .setPayDate("12/31/" + TWO_YEARS_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1200.00", "3600.00"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "250.00", "3000.00"))
                        .setPayDate("01/31/" + YEAR_TO_DATE))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1100.00", "13200.00"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "230.00", "2760.00"))
                        .setPayDate("12/31/" + ONE_YEAR_PRIOR))
                .addDocument(dataUpload.createCustomPaystub(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + TWO_YEARS_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1000.00", "12000.00"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "200.00", "2400.00"))
                        .setPayDate("12/31/" + TWO_YEARS_PRIOR))
                .importDocumentList();

        RestGetLoanData.getApplicationData(iaBuilder.getFolderId());
    }

    //CURRENT JOB
    @ParameterizedTest
    @MethodSource("loanDocStream")
    void checkIfEndDatesForPaystybCurrentJobInBasePayAreCorrect(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Paystub End Date in Base Pay current job",
                //Base Pay
                () -> assertEquals("03/31/" + YEAR_TO_DATE, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for YEAR_TO_DATE should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for ONE_YEAR_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for TWO_YEARS_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName())
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    void checkIfEndDatesForPaystybCurrentJobIOvertimeAreCorrect(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Paystub End Date in Overtime current job",
                //Base Pay
                () -> assertEquals("03/31/" + YEAR_TO_DATE, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for YEAR_TO_DATE should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for ONE_YEAR_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getOvertime().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for TWO_YEARS_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName())
        );
    }

    //PREVIOUS JOB
    @ParameterizedTest
    @MethodSource("loanDocStream")
    void checkIfEndDatesForPaystubPreviousJobInBasePayAreCorrect(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Paystub End Date in Base Pay previous job",
                //Base Pay
                () -> assertEquals("01/31/" + YEAR_TO_DATE, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for YEAR_TO_DATE should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for ONE_YEAR_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for TWO_YEARS_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName())
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    void checkIfEndDatesForPaystubPreviousJobInOvertimeAreCorrect(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Paystub End Date in Overtime previous job",
                //Base Pay
                () -> assertEquals("01/31/" + YEAR_TO_DATE, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for YEAR_TO_DATE should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + ONE_YEAR_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for ONE_YEAR_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName()),
                () -> assertEquals("12/31/" + TWO_YEARS_PRIOR, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getPayPeriodEndDate().getValue(), "Date format for TWO_YEARS_PRIOR should be MM/DD/YYY for " + folderBuilder.getBorrowerFullName())
        );
    }
}
package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
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

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;

@SuppressWarnings({"unchecked", "rawtypes"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
@ResourceLock(value = "VoePastJobShowingOnlyTerminationYearTest")
public class VoePastJobShowingOnlyTerminationYearTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderOvertimeConventionalTest = createFolderBuilder("IARVoeTDcon");
    private final IAFolderBuilder folderBuilderOvertimeFHATest = createFolderBuilder("IARVoeTDfha");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderOvertimeConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderOvertimeFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]));
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder iaBuilder, MortgageType loanType) {
        iaBuilder.setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobOvertime(iaBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobBasePay(iaBuilder.getCoBorrowerSSN(), "1.00")
                .setPrimaryJobOvertime(iaBuilder.getCoBorrowerSSN(), "0.10")
                .restBuild();

        DataUploadObject dataUpload = createUploadObject(iaBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "03/31/" + YEAR_TO_DATE,
                        "1200",
                        "3600",
                        "03/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1500")
                        .setPriorYearOvertime("2800")
                        .setTwoYearPriorOvertime("2500"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        iaBuilder.getBorrowerFullName(),
                        iaBuilder.getBorrowerSSN(),
                        iaBuilder.getBorrowerCollaboratorId(),
                        iaBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + TWO_YEARS_PRIOR,
                        "1200",
                        "65",
                        "0",
                        "0"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "1200",
                        "1200",
                        "01/31/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        iaBuilder.getCoBorrowerFullName(),
                        iaBuilder.getCoBorrowerSSN(),
                        iaBuilder.getCoBorrowerCollaboratorId(),
                        iaBuilder.getCoBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + ONE_YEAR_PRIOR,
                        "1200",
                        "65",
                        "0",
                        "0"))
                .importDocumentList();

        RestGetLoanData.getApplicationData(iaBuilder.getFolderId());
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(1)
    @Description("IA-1333 Check If Previous Job Two Years Prior VOE Is Visible In Two Years Prior Column Only")
    void checkIfVoeIsVisibleInTwoYearsPriorColumnOnly(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Voe is visible in TWO_YEARS_PRIOR column",
                //Base Pay
                () -> assertNotNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE should be visible in TWO_YEARS_PRIOR column for" + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in ONE_YEAR_PRIOR column for" + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in YEAR_TO_DATE column for" + folderBuilder.getBorrowerFullName()),
                //Overtime
                () -> assertNotNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE should be visible in TWO_YEARS_PRIOR column for" + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in ONE_YEAR_PRIOR column for" + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in YEAR_TO_DATE column for" + folderBuilder.getBorrowerFullName())
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(2)
    @Description("IA-1333 Check If Previous Job Prior Year VOE Is Visible In One Year Prior Column Only")
    void checkIfVoeIsVisibleInOneYearPriorColumnOnly(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Voe is visible in ONE_YEAR_PRIOR column",
                //Base Pay
                () -> assertNull(getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in TWO_YEARS_PRIOR column for" + folderBuilder.getCoBorrowerFullName()),
                () -> assertNotNull(getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE should be visible in ONE_YEAR_PRIOR column for" + folderBuilder.getCoBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in YEAR_TO_DATE column for" + folderBuilder.getCoBorrowerFullName()),
                //Overtime
                () -> assertNull(getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerPreviousEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in TWO_YEARS_PRIOR column for" + folderBuilder.getCoBorrowerFullName()),
                () -> assertNotNull(getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerPreviousEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE should be visible in ONE_YEAR_PRIOR column for" + folderBuilder.getCoBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncome(folderBuilder.getCoBorrowerPreviousEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in YEAR_TO_DATE column for" + folderBuilder.getCoBorrowerFullName())
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(3)
    @Description("IA-1333 Check If Previous Job Ytd VOE Is Visible In Ytd Column Only")
    void checkIfVoeIsVisibleInYearToDateColumnOnly(IAFolderBuilder folderBuilder) {
        DataUploadObject dataUpload = createUploadObject(folderBuilder);
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.WEEKLY,
                "01/01/" + THREE_YEARS_PRIOR,
                "04/30/" + YEAR_TO_DATE,
                "1200",
                "65",
                "0",
                "0"));

        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Voe is visible in YEAR_TO_DATE column",
                //Base Pay
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in TWO_YEARS_PRIOR column for " + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in ONE_YEAR_PRIOR column for " + folderBuilder.getBorrowerFullName()),
                () -> assertNotNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE should be visible in  YEAR_TO_DATE column for " + folderBuilder.getBorrowerFullName()),
                //Overtime
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in TWO_YEARS_PRIOR column for " + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in ONE_YEAR_PRIOR column for " + folderBuilder.getBorrowerFullName()),
                () -> assertNotNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE should be visible in  YEAR_TO_DATE column for " + folderBuilder.getBorrowerFullName())
        );
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(4)
    @Description("IA-1333 Check If Previous Job Ytd VOE With Only Base Pay Is Visible In Base Pay Ytd Column Only")
    void checkIfVoeIsVisibleInYearToDateColumnInBasePayOnly(IAFolderBuilder folderBuilder) {
        DataUploadObject dataUpload = createUploadObject(folderBuilder);
        dataUpload.removeDocumentsFromFolder();

        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.WEEKLY,
                "01/01/" + THREE_YEARS_PRIOR,
                "05/31/" + YEAR_TO_DATE,
                "1200",
                "0",
                "0",
                "0"));

        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("Voe is visible in YEAR_TO_DATE column",
                //Base Pay
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in TWO_YEARS_PRIOR column for " + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in ONE_YEAR_PRIOR column for" + folderBuilder.getBorrowerFullName()),
                () -> assertNotNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE should be visible in  YEAR_TO_DATE column for " + folderBuilder.getBorrowerFullName()),
                //Overtime
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in TWO_YEARS_PRIOR column for " + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in ONE_YEAR_PRIOR column for " + folderBuilder.getBorrowerFullName()),
                () -> assertNull(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getOvertime().getIncome(YEAR_TO_DATE).getAnnualSummaryDocument(SummaryDocumentType.VOE), "VOE shouldn't be visible in  YEAR_TO_DATE column for " + folderBuilder.getBorrowerFullName())
        );
    }

}

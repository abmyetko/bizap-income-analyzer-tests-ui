package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.DateUtilities.formatDate;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.MM_DD_YYYY_F_SLASH;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.YYYY_MM_DD_DASH;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "DocumentPrecedenceTest")
public class DocumentPrecedenceTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilder = createFolderBuilder("IADocPrec");
    private final String startDateVoe = "02/01/" + ONE_YEAR_PRIOR;
    private final String endDateVoe = "07/31/" + ONE_YEAR_PRIOR;
    private final String startDateEvoe = "03/01/" + ONE_YEAR_PRIOR;
    private final String endDateEvoe = "09/30/" + ONE_YEAR_PRIOR;
    private final String startDateUrla = "01/01/" + ONE_YEAR_PRIOR;
    private final String endDateUrla = "12/31/" + ONE_YEAR_PRIOR;

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        folderBuilder
                .setBorrowerPreviousJobStartDate(startDateUrla)
                .setBorrowerPreviousJobEndDate(endDateUrla)
                .generateLoanDocument()
                .restBuild();
        dataUpload = createUploadObject(folderBuilder);

    }

    @BeforeEach
    void cleanDocuments() {
        dataUpload.removeDocumentsFromFolder();
    }

    @Test
    @Description("IA-2986 - Voe take precedence over Evoe for past job")
    void VoeTakePrecedenceOverEvoeForPastJob() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEvoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        startDateEvoe,
                        endDateEvoe,
                        "2800",
                        "24000",
                        "12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        startDateVoe,
                        endDateVoe,
                        "1500",
                        "500",
                        "12000",
                        "0"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Employment period dates",
                    () -> assertEquals(formatDate(MM_DD_YYYY_F_SLASH, startDateVoe, YYYY_MM_DD_DASH),
                            response.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                                    .getEmployer()
                                    .getEmploymentPeriod()
                                    .getDateFrom(),
                            "DateFrom should be taken from Voe"),
                    () -> assertEquals(formatDate(MM_DD_YYYY_F_SLASH, endDateVoe, YYYY_MM_DD_DASH),
                            response.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                                    .getEmployer()
                                    .getEmploymentPeriod()
                                    .getDateTo(),
                            "DateTo should be taken from Voe")
            );
        });
    }

    @Test
    @Description("IA-2986 - Voe take precedence over URLA for past job")
    void VoeTakePrecedenceOverUrlaForPastJob() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        startDateVoe,
                        endDateVoe,
                        "1500",
                        "500",
                        "12000",
                        "0"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Employment period dates",
                    () -> assertEquals(formatDate(MM_DD_YYYY_F_SLASH, startDateVoe, YYYY_MM_DD_DASH),
                            response.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                                    .getEmployer()
                                    .getEmploymentPeriod()
                                    .getDateFrom(),
                            "DateFrom should be taken from Voe"),
                    () -> assertEquals(formatDate(MM_DD_YYYY_F_SLASH, endDateVoe, YYYY_MM_DD_DASH),
                            response.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                                    .getEmployer()
                                    .getEmploymentPeriod()
                                    .getDateTo(),
                            "DateTo should be taken from Voe")
            );
        });
    }

    @Test
    @Description("IA-2986 - Evoe take precedence over URLA for past job")
    void EvoeTakePrecedenceOverUrlaForPastJob() {
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEvoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        startDateEvoe,
                        endDateEvoe,
                        "2800",
                        "24000",
                        "12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .importDocumentList();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponse response = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
            assertAll("Employment period dates",
                    () -> assertEquals(formatDate(MM_DD_YYYY_F_SLASH, startDateEvoe, YYYY_MM_DD_DASH),
                            response.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                                    .getEmployer()
                                    .getEmploymentPeriod()
                                    .getDateFrom(),
                            "DateFrom should be taken from Evoe"),
                    () -> assertEquals(formatDate(MM_DD_YYYY_F_SLASH, endDateEvoe, YYYY_MM_DD_DASH),
                            response.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncome(folderBuilder.getBorrowerPreviousEmployment())
                                    .getEmployer()
                                    .getEmploymentPeriod()
                                    .getDateTo(),
                            "DateTo should be taken from Evoe")
            );
        });
    }

}
package com.capsilon.incomeanalyzer.automation.e2e.tests;

import com.capsilon.incomeanalyzer.automation.data.upload.PdfUpload;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.e2e.base.RestTestBaseE2E;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.datamapper.DataMapperEndpoint;
import com.capsilon.test.datamapper.model.DataMapperPayload;
import com.capsilon.test.datamapper.model.DataPoint;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableIfToggled(propertyName = PropertyToggles.UPDATE_EVENT_TOGGLE, isSiteGuidNeeded = true)
@Execution(CONCURRENT)
@ResourceLock(value = "DataMapperTest")
class DataMapperTest extends RestTestBaseE2E {

    private final IAFolderBuilder folderBuilderDtMpr = createFolderBuilder("IAE2EDatMap");
    private PdfUpload dataUpload;
    private RestGetResponse getResponse;

    @BeforeAll
    void importDocument() {
        folderBuilderDtMpr
                .setBorrowerCurrentEmployment("Awesome Computers Inc").setBorrowerYearsOnThisJob("3").setBorrowerMonthsOnThisJob("1")
                .setBorrowerPreviousEmployment("Bul Onder Inc").setBorrowerPreviousJobStartDate("01/01/2000").setBorrowerPreviousJobEndDate("03/31/2021")
                .setCoBorrowerCurrentEmployment("Capadilon One Inc").setCoBorrowerYearsOnThisJob("10").setCoBorrowerMonthsOnThisJob("1")
                .setBorrowerPreviousEmployment("Jajebie Komputers Inc").setCoBorrowerPreviousJobStartDate("01/01/1945").setCoBorrowerPreviousJobEndDate("12/31/2019");

        folderBuilderDtMpr.generateLoanDocument().restBuild();

        dataUpload = new PdfUpload(folderBuilderDtMpr, dvFolderClient);
        dataUpload
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderDtMpr.getBorrowerFullName(),
                        folderBuilderDtMpr.getBorrowerSSN(),
                        folderBuilderDtMpr.getBorrowerCollaboratorId(),
                        folderBuilderDtMpr.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow("Trololoo", "10.24", "40", "1000.00", "2000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderDtMpr.getCoBorrowerFullName(),
                        folderBuilderDtMpr.getCoBorrowerSSN(),
                        folderBuilderDtMpr.getCoBorrowerCollaboratorId(),
                        folderBuilderDtMpr.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow("Oololort", "", "", "1000.00", "2000.00")))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilderDtMpr.getFolderId());
    }

    @Test
    @Description("IA-2495 Check If Income Types Have Been Matched Correctly And Are Shown In Correct Years In IA Response")
    void checkIfIncomeTypesMappedUsingDataMapperHaveBeenExtractedCorrectly() {
        DataMapperPayload incomeTypes = DataMapperEndpoint.getPaystubIncomeTypes(folderBuilderDtMpr.getFolderId());
        DataPoint trololoIncome = incomeTypes.getGroup("UNMAPPED").getDataPoints().stream()
                .filter(dataPoint -> "Trololoo".equals(dataPoint.getLoanDataPointDetails().getOriginalValue().replaceAll("[{?}]", ""))).findFirst().orElseThrow(NullPointerException::new);
        DataPoint ololortIncome = incomeTypes.getGroup("UNMAPPED").getDataPoints().stream()
                .filter(dataPoint -> "Oololort".equals(dataPoint.getLoanDataPointDetails().getOriginalValue().replaceAll("[{?}]", ""))).findFirst().orElseThrow(NullPointerException::new);
        DataMapperEndpoint.mapData(folderBuilderDtMpr.getFolderId(), trololoIncome, "Base");
        DataMapperEndpoint.mapData(folderBuilderDtMpr.getFolderId(), ololortIncome, "Overtime");
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
                    getResponse = RestGetLoanData.getApplicationData(folderBuilderDtMpr.getFolderId());
                    assertAll("Data Mapper association",
                            () -> assertTrue(getResponse.getApplicant(folderBuilderDtMpr.getBorrowerFullName())
                                    .getIncome(folderBuilderDtMpr.getBorrowerCurrentEmployment())
                                    .getBasePay()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getIncluded(), "Borrower Base Pay paystub was not included"),
                            () -> assertEquals(bigD(1000), bigD(getResponse.getApplicant(folderBuilderDtMpr.getBorrowerFullName())
                                    .getIncome(folderBuilderDtMpr.getBorrowerCurrentEmployment())
                                    .getBasePay()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountCalculated().getValue()), "Base Pay paystub has incorrect projected monthly income value"),
                            () -> assertEquals(bigD(2000), bigD(getResponse.getApplicant(folderBuilderDtMpr.getBorrowerFullName())
                                    .getIncome(folderBuilderDtMpr.getBorrowerCurrentEmployment())
                                    .getBasePay()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()), "Base Pay paystub has incorrect actual avg income value"),
                            () -> assertEquals(bigD(40), bigD(getResponse.getApplicant(folderBuilderDtMpr.getBorrowerFullName())
                                    .getIncome(folderBuilderDtMpr.getBorrowerCurrentEmployment())
                                    .getBasePay()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getHours().getValue()), "Base Pay paystub has incorrect hours value"),
                            () -> assertTrue(getResponse.getApplicant(folderBuilderDtMpr.getCoBorrowerFullName())
                                    .getIncome(folderBuilderDtMpr.getCoBorrowerCurrentEmployment())
                                    .getOvertime()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getIncluded(), "Borrower Overtime paystub was not included"),
                            () -> assertEquals(bigD(2000), bigD(getResponse.getApplicant(folderBuilderDtMpr.getCoBorrowerFullName())
                                    .getIncome(folderBuilderDtMpr.getCoBorrowerCurrentEmployment())
                                    .getOvertime()
                                    .getIncome(YEAR_TO_DATE)
                                    .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getMonthlyAmountAvg().getValue()), "Overtime paystub has incorrect actual avg income value")
                    );
                }
        );
    }
}

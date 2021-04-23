package com.capsilon.incomeanalyzer.automation.e2e.tests;

import com.capsilon.incomeanalyzer.automation.data.upload.PdfUpload;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.e2e.base.RestTestBaseE2E;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.applicantassociation.AppAssEndpoint;
import com.capsilon.test.applicantassociation.model.Collaborator;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.List;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.test.applicantassociation.AppAssEndpoint.appAssGetCollaboratorsList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "ApplicantAssociationTest")
@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableIfToggled(propertyName = PropertyToggles.UPDATE_EVENT_TOGGLE, isSiteGuidNeeded = true)
class ApplicantAssociationTest extends RestTestBaseE2E {

    private final IAFolderBuilder folderBuilderAppAss = createFolderBuilder("IAE2EAppAss");
    private PdfUpload dataUpload;
    private RestGetResponse getResponse;

    @BeforeAll
    void importDocument() {
        folderBuilderAppAss
                .setBorrowerCurrentEmployment("Awesome Computers Inc").setBorrowerYearsOnThisJob("3").setBorrowerMonthsOnThisJob("1")
                .setBorrowerPreviousEmployment("Bul Onder Inc").setBorrowerPreviousJobStartDate("01/01/2000").setBorrowerPreviousJobEndDate("03/31/2021")
                .setCoBorrowerCurrentEmployment("Capadilon One Inc").setCoBorrowerYearsOnThisJob("10").setCoBorrowerMonthsOnThisJob("1")
                .setBorrowerPreviousEmployment("Jajebie Komputers Inc").setCoBorrowerPreviousJobStartDate("01/01/1945").setCoBorrowerPreviousJobEndDate("12/31/2019");

        folderBuilderAppAss.generateLoanDocument().restBuild();

        dataUpload = new PdfUpload(folderBuilderAppAss, dvFolderClient);
        dataUpload
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderAppAss.getBorrowerFullName(),
                        folderBuilderAppAss.getBorrowerSSN(),
                        folderBuilderAppAss.getBorrowerCollaboratorId(),
                        folderBuilderAppAss.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "10.24", "40", "1000.00", "2000.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        "Orphan App",
                        "666-99-6969",
                        folderBuilderAppAss.getBorrowerCollaboratorId(),
                        folderBuilderAppAss.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "10.24", "40", "1000.00", "2000.00")))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilderAppAss.getBorrowerFullName(),
                        folderBuilderAppAss.getBorrowerSSN(),
                        folderBuilderAppAss.getBorrowerCollaboratorId(),
                        folderBuilderAppAss.getBorrowerCurrentEmployment(),
                        IncomeFrequency.HOURLY,
                        "01/01/" + TWO_YEARS_PRIOR,
                        "01/31/" + YEAR_TO_DATE,
                        "10.00",
                        "600.00",
                        "01/31/" + YEAR_TO_DATE)
                        .setAvgHoursPerWeek("40")
                        .setPriorYearBasePay("6000.00")
                        .setTwoYearPriorBasePay("12000.00"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilderAppAss.getBorrowerFullName(),
                        folderBuilderAppAss.getBorrowerSSN(),
                        folderBuilderAppAss.getBorrowerCollaboratorId(),
                        folderBuilderAppAss.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "01/07/" + YEAR_TO_DATE,
                        "1500",
                        "500",
                        "12000",
                        "00"))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilderAppAss.getBorrowerFullName(),
                        folderBuilderAppAss.getBorrowerSSN(),
                        folderBuilderAppAss.getBorrowerCollaboratorId(),
                        folderBuilderAppAss.getBorrowerCurrentEmployment(),
                        "12000.00",
                        ONE_YEAR_PRIOR.toString()))
                .addDocument(dataUpload.createCustomW2(
                        "Orphan App",
                        "666-99-6969",
                        folderBuilderAppAss.getBorrowerCollaboratorId(),
                        folderBuilderAppAss.getBorrowerCurrentEmployment(),
                        "12000.00",
                        TWO_YEARS_PRIOR.toString()))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilderAppAss.getFolderId());
    }

    @Test
    @Description("IA-2047 Check If Applicant Association Metadata Is Stored In IA Response And Orphan Documents Are Matched Correctly")
    void checkIfAppAssMetadataIsStoredInIAResponseAndOrphanDocumentsAreMatchedCorrectly() {

        List<Collaborator> collaborators = associateFirstInactiveApplicant(folderBuilderAppAss.getBorrowerFullName());

        Collaborator orphanCollaborator = collaborators.stream()
                .filter(collaborator -> (collaborator.getAssociationIds().size() > 0 &&
                        collaborator.getAssociationIds().get(0).equals(getResponse.getApplicant(folderBuilderAppAss.getBorrowerFullName()).getRefId())))
                .findFirst().orElseThrow(NullPointerException::new);

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            getResponse = RestGetLoanData.getApplicationData(folderBuilderAppAss.getFolderId());
            assertAll("Applicant association",
                    () -> assertEquals(orphanCollaborator.getId(), getResponse.getApplicant(folderBuilderAppAss.getBorrowerFullName()).getAssociatedRefIds().get(0),
                            "Primary borrower did not have orphan borrower id associated to him"),
                    () -> assertTrue(getResponse.getApplicant(folderBuilderAppAss.getBorrowerFullName())
                            .getIncome(folderBuilderAppAss.getBorrowerCurrentEmployment())
                            .getBasePay()
                            .getIncome(ONE_YEAR_PRIOR)
                            .getAnnualSummaryDocument(SummaryDocumentType.PAYSTUB).getIncluded()),
                    () -> assertTrue(getResponse.getApplicant(folderBuilderAppAss.getBorrowerFullName())
                            .getIncome(folderBuilderAppAss.getBorrowerCurrentEmployment())
                            .getBasePay()
                            .getIncome(TWO_YEARS_PRIOR)
                            .getAnnualSummaryDocument(SummaryDocumentType.W2).getIncluded())
            );
                }
        );
    }

    List<Collaborator> associateFirstInactiveApplicant(String borrowerFullName) {
        List<Collaborator> appAssocCollaboratorList = appAssGetCollaboratorsList(folderBuilderAppAss.getFolderId());
        String orphanCollaboratorId = appAssocCollaboratorList.stream().filter(collaborator -> !collaborator.getActive()).findFirst().orElseThrow(NullPointerException::new).getId();
        String borrowerId = getResponse.getApplicant(borrowerFullName).getRefId();
        String borrowerClassificationType = appAssocCollaboratorList.stream().filter(collaborator -> borrowerId.equals(collaborator.getId())).findFirst().orElseThrow(NullPointerException::new)
                .getApplicantPairDetails().getClassificationType();

        appAssocCollaboratorList.stream().filter(collaborator -> borrowerId.equals(collaborator.getId())).findFirst().orElseThrow(NullPointerException::new)
                .addAssociationId(orphanCollaboratorId);
        appAssocCollaboratorList.stream().filter(collaborator -> orphanCollaboratorId.equals(collaborator.getId())).findFirst().orElseThrow(NullPointerException::new)
                .addAssociationId(borrowerId);
        appAssocCollaboratorList.stream().filter(collaborator -> orphanCollaboratorId.equals(collaborator.getId())).findFirst().orElseThrow(NullPointerException::new)
                .getApplicantPairDetails().setClassificationType(borrowerClassificationType);

        AppAssEndpoint.appAssPutCollaborators(folderBuilderAppAss.getFolderId(), appAssocCollaboratorList);

        return appAssocCollaboratorList;
    }
}

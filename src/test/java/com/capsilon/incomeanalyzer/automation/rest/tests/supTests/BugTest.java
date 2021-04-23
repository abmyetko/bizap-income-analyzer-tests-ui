package com.capsilon.incomeanalyzer.automation.rest.tests.supTests;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponseApplicant;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeEmployer;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.HashMap;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.selectApplicant;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestNotIARequests.addEmployerAlias;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "BugTest")
public class BugTest extends TestBaseRest {

    @Test
    @Description("IA-2440 Check If Adding Employer Alias To Primary Job With The Same Name As Previous Job Does Not Add Alias To Previous Job")
    void addingEmployerAliasToPrimaryJobSameAsPreviousJobNameShouldNotAddAliasToPreviousJob() {
        IAFolderBuilder folderBuilder = createFolderBuilder("IA_2440");
        final Integer[] applicantId = new Integer[1];
        folderBuilder.generateLoanDocument().restBuild();

        Retry.whileTrue(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_ONE_MINUTE, () ->
                        RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getRefId() == null,
                "Folder has not been created correctly");

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () ->
                applicantId[0] = (Integer) ((HashMap<String, Object>) RestNotIARequests.getChecklistRules(folderBuilder.getFolderId()).then().extract().jsonPath().getList("applicants")
                        .stream().filter(app -> ((HashMap<String, Object>) app).get("fullName").equals(folderBuilder.getBorrowerFullName())).findFirst().orElseThrow(NullPointerException::new)).get("applicantId"));

        addEmployerAlias(folderBuilder.getFolderId(), applicantId[0], folderBuilder.getBorrowerCurrentEmployment(), folderBuilder.getBorrowerPreviousEmployment());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponseApplicant primaryApplicant = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName());
            RestIncomeEmployer previousEmployer = primaryApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()).getEmployer();
            assertAll("Asserting employer alias",
                    () -> assertEquals(folderBuilder.getBorrowerPreviousEmployment(), primaryApplicant.getIncome(folderBuilder.getBorrowerCurrentEmployment()).getEmployer().getNameAliases().get(0)),
                    () -> assertEquals(0, previousEmployer.getNameAliases().size(),
                            String.format("Previous borrower %s has aliases: \n %s \n", previousEmployer.getName(), previousEmployer.getNameAliases()))
            );
        });
    }

    @Test
    @Description("IA-2548 Check If There Is No NPE And Folder Still Works If Previous Voe With Bonus Income Was Uploaded")
    void checkIfFolderStillWorksIfPreviousVoeWithBonusIncomeWasUploaded() {
        IAFolderBuilder folderBuilder = createFolderBuilder("IA_2548");
        folderBuilder.generateLoanDocument().restBuild();

        dataUpload = createUploadObject(folderBuilder);
        dataUpload.importDocument(dataUpload.createCustomVoePrevious(
                folderBuilder.getBorrowerFullName(),
                folderBuilder.getBorrowerSSN(),
                folderBuilder.getBorrowerCollaboratorId(),
                folderBuilder.getBorrowerPreviousEmployment(),
                IncomeFrequency.MONTHLY,
                "01/01/" + TWO_YEARS_PRIOR,
                "03/31/" + ONE_YEAR_PRIOR,
                "1200",
                "70",
                "70",
                "1542"));

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestPartIncomeAnnualSummary voeData = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(SummaryDocumentType.VOE);
            assertAll("Bonus previous voe NPE assertions",
                    () -> assertNotNull(voeData),
                    () -> assertEquals(bigD(1200), bigD(voeData.getDisplayGrossPay().getValue()))
            );
        });
    }

    @Test
    @Description("IA-2811 Protect applicant selection from loan update")
    void checkIfApplicantDeselectionDoesntChangeAfterLoanDocUpdate() {
        IAFolderBuilder folderBuilder = createFolderBuilder("IA_2811");
        folderBuilder.setSignDate("02/16/" + YEAR_TO_DATE).generateLoanDocument().restBuild();

        RestGetResponseApplicant applicant = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName());

        assertTrue(applicant.getSelected(), "Borrower should be selected");
        //deselect Borrower
        selectApplicant(applicant.getId(), false);

        RestGetResponseApplicant applicantAfterDeselection = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName());
        assertFalse(applicantAfterDeselection.getSelected(), "Borrower should be deselected");

        folderBuilder.setSignDate("02/20/" + YEAR_TO_DATE);
        folderBuilder.generateLoanDocument().uploadNewLoanDocument();

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestGetResponseApplicant applicantAfterUpdate = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName());
            String executionDate = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getExecutionDate();
            assertAll("Assertions after loan document upload",
                    () -> assertEquals(YEAR_TO_DATE + "-02-20", executionDate, "Execution date should be updated to 02/20/" + YEAR_TO_DATE),
                    () -> assertFalse(applicantAfterUpdate.getSelected(), "Borrower should be deselected after upload")
            );
        });
    }
    //
}
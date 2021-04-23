package com.capsilon.incomeanalyzer.automation.rest.tests.supTests;

import com.capsilon.common.utils.mismo.wrapper.MismoWrapper;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAMismoBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestUploadData.createLoanV2;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Disabled("disabled until MultiBorrower mismo documents are eligible to test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@ResourceLock(value = "MismoMultiBorrowerTest")
public class MismoMultiBorrowerTest extends TestBaseRest {
    private IAMismoBuilder mismoBuilder = new IAMismoBuilder("IARMultiBorrowerCheck");
    private String threeFirstName = "Penny";
    private String threeLastName = "Slow";
    private String threeSSN = "333-22-0990";
    private String threePriEmpName = "Hueh Co";
    private String threePreEmpName = "Hue Inc";
    private String threeCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String threePreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String threePreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;
    private String fourFirstName = "Harry";
    private String fourLastName = "Swift";
    private String fourSSN = "353-55-1234";
    private String fourPriEmpName = "Oh Co";
    private String fourPreEmpName = "Ohey Inc";
    private String fourCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String fourPreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String fourPreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;
    private String fiveFirstName = "Why";
    private String fiveLastName = "Ung";
    private String fiveSSN = "111-55-6455";
    private String fivePriEmpName = "Od Co";
    private String fivePreEmpName = "Oc Inc";
    private String fiveCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String fivePreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String fivePreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;
    private String sixFirstName = "Dothi";
    private String sixLastName = "Swift";
    private String sixSSN = "111-55-6857";
    private String sixPriEmpName = "Huey Co";
    private String sixPreEmpName = "Whey Inc";
    private String sixCurrentStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String sixPreviousJobStartDate = "01/01/" + THREE_YEARS_PRIOR;
    private String sixPreviousJobEndDate = "12/31/" + ONE_YEAR_PRIOR;

    @Test
    @EnableIfToggled(propertyName = PropertyToggles.ELLIE_FILTER_TOGGLE, isSiteGuidNeeded = true)
    void checkIfSixBorrowersAreExtractedFromMismoDocumentCorrectly() {
        mismoBuilder.generateLoanDocument();
        mismoBuilder.getMismoWrapper().getDealSets()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower();
        setBorrowerValues(2);
        setBorrowerValues(3);
        setBorrowerValues(4);
        setBorrowerValues(5);
        mismoBuilder.restBuild();
        assertEquals(6, RestGetLoanData.getApplicationData(mismoBuilder.getFolderId()).getApplicants().size());
    }

    @Test
    @EnableIfToggled(propertyName = PropertyToggles.ELLIE_FILTER_TOGGLE, isSiteGuidNeeded = true)
    void checkIfBorrowersCanBeAdded() {
        mismoBuilder.generateLoanDocument();
        MismoWrapper wrapper = mismoBuilder.getMismoWrapper();
        wrapper.getDealSets()
                .addBorrower()
                .addBorrower()
                .addBorrower();
        setBorrowerValues(2);
        setBorrowerValues(3);
        setBorrowerValues(4);
        mismoBuilder.restBuild();
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                assertEquals(5, RestGetLoanData.getApplicationData(mismoBuilder.getFolderId()).getApplicants().size()));
        wrapper.getDealSets()
                .addBorrower();
        setBorrowerValues(5);
        mismoBuilder.uploadNewLoanDocument();
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FORTY_SECONDS, () ->
                assertEquals(6, RestGetLoanData.getApplicationData(mismoBuilder.getFolderId()).getApplicants().size()));
    }

    @Test
    void checkIfBorrowersCanBeRemoved() {
        mismoBuilder.generateLoanDocument();
        mismoBuilder.getMismoWrapper().getDealSets()
                .addBorrower()
                .addBorrower()
                .addBorrower();
        setBorrowerValues(2);
        setBorrowerValues(3);
        setBorrowerValues(4);
        mismoBuilder.restBuild();
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () ->
                assertEquals(5, RestGetLoanData.getApplicationData(mismoBuilder.getFolderId()).getApplicants().size()));
        mismoBuilder.getMismoWrapper().getDealSets()
                .addBorrower();
        mismoBuilder.generateLoanDocument();
        MismoWrapper SecondWrapper = new MismoWrapper(mismoBuilder.getMismoBuilder());
        SecondWrapper.getDealSets()
                .addBorrower()
                .addBorrower();
        setBorrowerValues(2);
        setBorrowerValues(3);
        mismoBuilder.uploadNewLoanDocument();
        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FORTY_SECONDS, () ->
                assertEquals(4, RestGetLoanData.getApplicationData(mismoBuilder.getFolderId()).getApplicants().size()));
    }

    @Test
    @EnableIfToggled(propertyName = PropertyToggles.ELLIE_FILTER_TOGGLE, isSiteGuidNeeded = true)
    @Description("IA-3009 Check if IA does not recalculate if initial ellie is set to NO and mismo update is made")
    void checkNoIARecalculationWhenEllieIsNoAndLoanDocumentWasUpdated() {
        mismoBuilder.generateLoanDocument();
        mismoBuilder.getMismoWrapper().getDealSets()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower();
        String folderId = createLoanV2(mismoBuilder.generatePhysicalDocument());
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            String rspMessage = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .get(REST_URL + "/bizapps/incomeanalyzer/application-refid/{folderId}").then().extract().jsonPath().get("message");
            assertEquals("No application found with the ref: " + folderId, rspMessage,
                    "Folder was not opted-out for mismo with more than 6 borrowers. Check if this rule is still in effect");
        });
        mismoBuilder.getMismoWrapper().getDealSets()
                .addBorrower()
                .addBorrower()
                .addBorrower()
                .addBorrower();
        mismoBuilder.uploadNewLoanDocument(folderId);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            String rspMessage = given().spec(getRequestSpecBuilder())
                    .pathParam(PARAMETER_NAME_FOLDER_ID, folderId)
                    .get(REST_URL + "/bizapps/incomeanalyzer/application-refid/{folderId}").then().extract().jsonPath().get("message");
            assertEquals("No application found with the ref: " + folderId, rspMessage,
                    "Folder was not opted-out after update for mismo with more than 6 borrowers.");
        });
    }

    void setBorrowerValues(int borrowerId) {
        String firstName = "";
        String lastName = "";
        String fullName = "";
        String ssn = "";
        String primaryEmployer = "";
        String previousEmployer = "";
        String currentStartDate = "";
        String previousStartDate = "";
        String previousEndDate = "";
        switch (borrowerId) {
            case 2:
                firstName = threeFirstName;
                lastName = threeLastName;
                fullName = threeFirstName + " " + threeLastName;
                ssn = threeSSN.replace("-", "");
                primaryEmployer = threePriEmpName;
                previousEmployer = threePreEmpName;
                currentStartDate = threeCurrentStartDate;
                previousStartDate = threePreviousJobStartDate;
                previousEndDate = threePreviousJobEndDate;
                break;
            case 3:
                firstName = fourFirstName;
                lastName = fourLastName;
                fullName = fourFirstName + " " + threeLastName;
                ssn = fourSSN.replace("-", "");
                primaryEmployer = fourPriEmpName;
                previousEmployer = fourPreEmpName;
                currentStartDate = fourCurrentStartDate;
                previousStartDate = fourPreviousJobStartDate;
                previousEndDate = fourPreviousJobEndDate;
                break;
            case 4:
                firstName = fiveFirstName;
                lastName = fiveLastName;
                fullName = fiveFirstName + " " + threeLastName;
                ssn = fiveSSN.replace("-", "");
                primaryEmployer = fivePriEmpName;
                previousEmployer = fivePreEmpName;
                currentStartDate = fiveCurrentStartDate;
                previousStartDate = fivePreviousJobStartDate;
                previousEndDate = fivePreviousJobEndDate;
                break;
            case 5:
                firstName = sixFirstName;
                lastName = sixLastName;
                fullName = sixFirstName + " " + threeLastName;
                ssn = sixSSN.replace("-", "");
                primaryEmployer = sixPriEmpName;
                previousEmployer = sixPreEmpName;
                currentStartDate = sixCurrentStartDate;
                previousStartDate = sixPreviousJobStartDate;
                previousEndDate = sixPreviousJobEndDate;
                break;
            default:
                break;
        }
        mismoBuilder.setBuilderFirstName(borrowerId, firstName);
        mismoBuilder.setBuilderLastName(borrowerId, lastName);
        mismoBuilder.setBuilderFullName(borrowerId, fullName);
        mismoBuilder.setBuilderSSN(borrowerId, ssn);
        mismoBuilder.setBuilderEmployerName(borrowerId, 0, primaryEmployer);
        mismoBuilder.setBuilderEmployerName(borrowerId, 1, previousEmployer);
        mismoBuilder.setEmployerStartDate(borrowerId, 0, currentStartDate);
        mismoBuilder.setEmployerStartDate(borrowerId, 1, previousStartDate);
        mismoBuilder.setEmployerEndDate(borrowerId, 1, previousEndDate);
        mismoBuilder.setPreviousEmployerStatusType(borrowerId, 1);
    }


}
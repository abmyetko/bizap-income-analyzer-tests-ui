package com.capsilon.incomeanalyzer.automation.rest.tests.secondaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestApplicantIncome;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponseApplicant;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.Arrays;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryStoryTest")
public class SecondaryStoryTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilder = createFolderBuilder("IARScndrTst");
    private RestGetResponse getResponse;

    @BeforeAll
    public void generateDocs() {
        folderBuilder.generateSecondaryJobsLoanDocument()
                .restBuild();
        dataUpload = createUploadObject(folderBuilder);

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
    }

    @BeforeEach
    void restoreDefaults() {
        Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, TIMEOUT_TWO_MINUTES, () -> RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicants().forEach(applicant -> {
            restoreApplicantDefaults(applicant.getId());
            assertTrue(applicant.getTouchless(), String.format("Applicant %s could not be restored to touchless", applicant.getFirstName()));
        }));
    }

    @Test
    @Description("IA-2586 check if touchless flag works correctly when Secondary job is selected/deselected")
    void checkIfSecondaryJobSelectionsAffectsTouchlessFlag() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
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
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.WEEKLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "03/31/" + TWO_YEARS_PRIOR,
                        "1200",
                        "65",
                        "0",
                        "0"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerSecondEmployment(),
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
                .importDocumentList();

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponseApplicant firstApplicant = getResponse.getApplicant(folderBuilder.getBorrowerFullName());
            selectIncomeGroup(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getId(), false);
            getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

            assertFalse(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getTouchless());
        });
    }

    @Test
    @Description("IA-2646 Check if only two Classification Types are present when current voe is uploaded for previous job")
    void checkIfOnlyTwoClassificationTypesArePresentWhenCurrentVoeIsUploadedForPreviousJob() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getCoBorrowerFullName(),
                        folderBuilder.getCoBorrowerSSN(),
                        folderBuilder.getCoBorrowerCollaboratorId(),
                        folderBuilder.getCoBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        assertAll("check if there are only two ClassificationTypes for both applicants",
                () -> assertEquals("Primary", getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncomeCategoryW2().getPrimaryIncomeGroup().getClassificationType()),
                () -> assertEquals("Secondary", getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncomeCategoryW2().getSecondaryIncomeGroup().getClassificationType()),
                () -> assertEquals(2, getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncomeCategoryW2().getIncomeGroups().toArray().length),
                () -> assertEquals("Primary", getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncomeCategoryW2().getIncomeGroups().get(0).getClassificationType()),
                () -> assertEquals("Secondary", getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncomeCategoryW2().getIncomeGroups().get(1).getClassificationType()),
                () -> assertEquals(2, getResponse.getApplicant(folderBuilder.getCoBorrowerFullName())
                        .getIncomeCategoryW2().getIncomeGroups().toArray().length)
        );

    }

    @Test
    @Description("IA-2585 Calculate primary and secondary income section from income documents")
    void checkCalculationsForIncomeTypesInPrimaryAndSecondaryEmployment() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600.00", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerSecondEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600.00", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "600.00", "7200")))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            RestGetResponseApplicant firstApplicant = (getResponse.getApplicant(folderBuilder.getBorrowerFullName()));
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeOvertime().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeCommissions().getId(), true);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));

            assertAll("check if calculations for primary and secondary income types are correct",
                    () -> assertEquals(bigD(1800),
                            bigD(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2().getPrimaryIncomeGroup().getQualifyingIncome()), "check if Calculations for Primary employment are correct"),
                    () -> assertEquals(bigD(1800),
                            bigD(getResponse.getApplicant(folderBuilder.getBorrowerFullName())
                                    .getIncomeCategoryW2().getSecondaryIncomeGroup().getQualifyingIncome()), "check if Calculations for Secondary employment are correct")
            );
        });
    }

    @Test
    @Description("IA-2713 Check if previous employment is removed from incomeGroup if all incomes are deselected")
    void checkIfPreviousEmploymentIsRemovedFromIncomeGroupIfAllIncomesAreDeselected() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomVoePrevious(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "12000",
                        "6000",
                        "0",
                        "0"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerSecondEmployment(),
                        IncomeFrequency.MONTHLY,
                        "01/01/" + THREE_YEARS_PRIOR,
                        "12/31/" + YEAR_TO_DATE,
                        "2600",
                        "590",
                        "01/07/" + YEAR_TO_DATE)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("1200")
                        .setPriorYearOvertime("1000")
                        .setTwoYearPriorOvertime("1000"))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        "24000.00",
                        ONE_YEAR_PRIOR.toString()))
                .addDocument(dataUpload.createCustomW2(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerPreviousEmployment(),
                        "24000.00",
                        TWO_YEARS_PRIOR.toString()))
                .importDocumentList();

        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        String primaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

        String secondaryGroupId = Long.toString(getResponse
                .getApplicant(folderBuilder.getBorrowerFullName())
                .getIncome(folderBuilder.getBorrowerSecondEmployment()).getId());

        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponseApplicant firstApplicant = (getResponse.getApplicant(folderBuilder.getBorrowerFullName()));
            selectIncomeGroup(firstApplicant.getId(), primaryGroupId, true);
            selectIncomeGroup(firstApplicant.getId(), secondaryGroupId, true);

            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeOvertime().getId(), true);

            selectBorrowerIncome(primaryGroupId, firstApplicant.getIncome(folderBuilder.getBorrowerCurrentEmployment()), true);
            selectBorrowerIncome(primaryGroupId, firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()), true);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertTrue(
                    Arrays.stream(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeIds())
                            .anyMatch(firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()).getId()::equals), "Previous job id was not present in primary incomeGroup after selection");

            selectBasePayIncome(primaryGroupId, firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()), false);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertTrue(
                    Arrays.stream(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeIds())
                            .anyMatch(firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()).getId()::equals), "Previous job id was not present in primary incomeGroup after only base pay deselection");

            selectOvertimeIncome(primaryGroupId, firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()), false);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertTrue(
                    Arrays.stream(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeIds())
                            .noneMatch(firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()).getId()::equals), "Previous job id was present in primary incomeGroup after deselection");

            selectBorrowerIncome(secondaryGroupId, firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()), true);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertTrue(
                    Arrays.stream(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeIds())
                            .anyMatch(firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()).getId()::equals), "Previous job id was not present in secondary incomeGroup after selection");

            selectBorrowerIncome(secondaryGroupId, firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()), false);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));
            assertTrue(
                    Arrays.stream(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeIds())
                            .noneMatch(firstApplicant.getIncome(folderBuilder.getBorrowerPreviousEmployment()).getId()::equals), "Previous job id was present in secondary incomeGroup after deselection");
        });
    }

    @Test
    @EnableIfToggled(propertyName = BONUS_TOGGLE)
    @EnableIfToggled(propertyName = OVERTIME_TOGGLE)
    @EnableIfToggled(propertyName = COMMISSIONS_TOGGLE)
    @Description("IA-2685 IA-2744 check if primary and secondary income type is viewed regardless to checkbox and summed correctly")
    void checkIfIncomeTypesAreVisibleRegardlessToCheckbox() {
        dataUpload.removeDocumentsFromFolder();

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerSecondEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilder.getBorrowerFullName(),
                        folderBuilder.getBorrowerSSN(),
                        folderBuilder.getBorrowerCollaboratorId(),
                        folderBuilder.getBorrowerCurrentEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(OVERTIME, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(COMMISSIONS, "", "", "600", "7200"),
                        new PaystubData.PaystubIncomeRow(BONUS, "", "", "600", "7200")))
                .importDocumentList();
        getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

        Retry.tryRun(TIMEOUT_TWENTY_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponseApplicant firstApplicant = (getResponse.getApplicant(folderBuilder.getBorrowerFullName()));
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getId(), false);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getId(), false);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getId(), false);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getId(), false);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getId(), false);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeOvertime().getId(), false);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeCommissions().getId(), false);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBonus().getId(), false);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));

            assertAll("check if calculations for primary and secondary income types are correct",
                    () -> assertEquals(bigD(0),
                            bigD(firstApplicant.getIncomeCategoryW2().getQualifyingIncome()), "check if Calculations for both employments are correct"),
                    () -> assertEquals(bigD(0),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getQualifyingIncome()), "check if Calculations for Primary employment are correct"),
                    () -> assertEquals(bigD(0),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getQualifyingIncome()), "check if Calculations for Secondary employment are correct"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getQualifyingIncome()), "check if income type Base Pay are visible after deselecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getQualifyingIncome()), "check if income type Overtime are visible after deselecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getQualifyingIncome()), "check if income type Commissions are visible after deselecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getQualifyingIncome()), "check if income type Bonus are visible after deselecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getQualifyingIncome()), "check if income type Base Pay are visible after deselecting checkbox for secondary employment"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeOvertime().getQualifyingIncome()), "check if income type Overtime are visible after deselecting checkbox for secondary employment"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeCommissions().getQualifyingIncome()), "check if income type Commissions are visible after deselecting checkbox for secondary employment"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBonus().getQualifyingIncome()), "check if income type Bonus are visible after deselecting checkbox for secondary employment")
            );
        });

        Retry.tryRun(TIMEOUT_TWENTY_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            RestGetResponseApplicant firstApplicant = (getResponse.getApplicant(folderBuilder.getBorrowerFullName()));
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeOvertime().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeCommissions().getId(), true);
            selectIncomeType(firstApplicant.getId(), firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBonus().getId(), true);
            getResponse = RestGetLoanData.getApplicationData((folderBuilder.getFolderId()));

            assertAll("check if calculations for primary and secondary income types are correct",
                    () -> assertEquals(bigD(4800),
                            bigD(firstApplicant.getIncomeCategoryW2().getQualifyingIncome()), "check if Calculations for both employments are correct"),
                    () -> assertEquals(bigD(2400),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getQualifyingIncome()), "check if Calculations for Primary employment are correct"),
                    () -> assertEquals(bigD(2400),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getQualifyingIncome()), "check if Calculations for Secondary employment are correct"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBasePay().getQualifyingIncome()), "check if income type Base Pay are visible after deselecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeOvertime().getQualifyingIncome()), "check if income type Overtime are visible after deselecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeCommissions().getQualifyingIncome()), "check if income type Commissions are visible after deselecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus().getQualifyingIncome()), "check if income type Bonus are visible after selecting checkbox"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBasePay().getQualifyingIncome()), "check if income type Base Pay are visible after selecting checkbox for secondary employment"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeOvertime().getQualifyingIncome()), "check if income type Overtime are visible after selecting checkbox for secondary employment"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeCommissions().getQualifyingIncome()), "check if income type Commissions are visible after selecting checkbox for secondary employment"),
                    () -> assertEquals(bigD(600),
                            bigD(firstApplicant.getIncomeCategoryW2().getSecondaryIncomeGroup().getIncomeTypeBonus().getQualifyingIncome()), "check if income type Bonus are visible after selecting checkbox for secondary employment")
            );
        });
    }

    void selectBorrowerIncome(String groupId, RestApplicantIncome applicantIncome, boolean selected) {
        selectBasePayIncome(groupId, applicantIncome, selected);
        selectOvertimeIncome(groupId, applicantIncome, selected);
    }

    void selectBasePayIncome(String groupId, RestApplicantIncome applicantIncome, boolean selected) {
        selectIncome(groupId, applicantIncome.getBasePay().getIncome(YEAR_TO_DATE).getId(), selected);
        selectIncome(groupId, applicantIncome.getBasePay().getIncome(ONE_YEAR_PRIOR).getId(), selected);
        selectIncome(groupId, applicantIncome.getBasePay().getIncome(TWO_YEARS_PRIOR).getId(), selected);
    }

    void selectOvertimeIncome(String groupId, RestApplicantIncome applicantIncome, boolean selected) {
        selectIncome(groupId, applicantIncome.getOvertime().getIncome(YEAR_TO_DATE).getId(), selected);
        selectIncome(groupId, applicantIncome.getOvertime().getIncome(ONE_YEAR_PRIOR).getId(), selected);
        selectIncome(groupId, applicantIncome.getOvertime().getIncome(TWO_YEARS_PRIOR).getId(), selected);
    }
}
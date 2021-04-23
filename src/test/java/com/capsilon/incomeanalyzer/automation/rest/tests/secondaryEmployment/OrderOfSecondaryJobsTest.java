package com.capsilon.incomeanalyzer.automation.rest.tests.secondaryEmployment;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponseApplicant;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TWO_YEARS_PRIOR;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "OrderOfSecondaryJobsTest")
public class OrderOfSecondaryJobsTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderOrderSec = createFolderBuilder("IAROrderSec");
    private final String thirdEmploymentName = "ThirdEmp";

    @BeforeAll
    public void generateLoanDoc() {
        folderBuilderOrderSec.generateSecondaryJobsLoanDocument()
                .setBorrowerSecondJobStartDate("02/01/" + TWO_YEARS_PRIOR)
                .addNewEmployment(thirdEmploymentName, "03/01/" + TWO_YEARS_PRIOR, "", true, true, "0.00")
                .restBuild();
        dataUpload = createUploadObject(folderBuilderOrderSec, dvFolderClient);

        RestGetLoanData.getApplicationData(folderBuilderOrderSec.getFolderId());
    }

    @Test
    @Order(1)
    @Description("IA-2657 Check if jobs have correct order of classification type on the employment list")
    void checkIfJobsHaveCorrectOrderOfClassificationTypeOnTheEmploymentList() {
        RestGetResponseApplicant applicant = RestGetLoanData.getApplicationData(folderBuilderOrderSec.getFolderId()).getApplicant(folderBuilderOrderSec.getBorrowerFullName());

        assertAll("Classification Type assertion",
                () -> assertEquals("Primary", applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(0).getClassificationType(), "First job on the employment list should be Primary"),
                () -> assertEquals("Secondary", applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(1).getClassificationType(), "Second job on the employment list should be Secondary"),
                () -> assertEquals("Secondary", applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(2).getClassificationType(), "Third job on the employment list should be Secondary")
        );
    }

    @Test
    @Order(2)
    @Description("IA-2657 Check if jobs are in correct order on employment list")
    void checkIfJobsAreInCorrectOrderOnEmploymentList() {
        RestGetResponseApplicant applicant = RestGetLoanData.getApplicationData(folderBuilderOrderSec.getFolderId()).getApplicant(folderBuilderOrderSec.getBorrowerFullName());

        assertAll("Order of employment assertion",
                () -> assertEquals(String.valueOf(applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(0).getIncomeIds()[0]), String.valueOf(applicant.getIncome(folderBuilderOrderSec.getBorrowerCurrentEmployment()).getId()), "[Borrower] First id job on the list should be equal to primary job id[Dept of Interior]"),
                () -> assertEquals(String.valueOf(applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(1).getIncomeIds()[0]), String.valueOf(applicant.getIncome(folderBuilderOrderSec.getBorrowerSecondEmployment()).getId()), "[Borrower] Second id job on the list should be equal to Secondary 1 job id[Ouya Yebe Inc]"),
                () -> assertEquals(String.valueOf(applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(2).getIncomeIds()[0]), String.valueOf(applicant.getIncome(thirdEmploymentName).getId()), "Borrower]Third id job on the list should be equal to Secondary 2 job id[ThirdEmp]")
        );
    }

    @Test
    @Order(3)
    @Description("IA-2657 Check if after document upload order of employment will change")
    void checkIfAfterDocUploadOrderOfEmploymentWillChange() {
        dataUpload.importDocument(dataUpload.createCustomVoeCurrent(
                folderBuilderOrderSec.getBorrowerFullName(),
                folderBuilderOrderSec.getBorrowerSSN(),
                folderBuilderOrderSec.getBorrowerCollaboratorId(),
                thirdEmploymentName,
                IncomeFrequency.MONTHLY,
                "01/01/" + TWO_YEARS_PRIOR,
                "12/31/" + YEAR_TO_DATE,
                "2600",
                "590",
                "12/31/" + YEAR_TO_DATE)
                .setPriorYearBasePay("14000")
                .setTwoYearPriorBasePay("13000")
                .setYtdOvertime("1200")
                .setPriorYearOvertime("1000")
                .setTwoYearPriorOvertime("1000"));

        RestGetResponseApplicant applicant = RestGetLoanData.getApplicationData(folderBuilderOrderSec.getFolderId()).getApplicant(folderBuilderOrderSec.getBorrowerFullName());

        assertAll("Order of employment assertion",
                () -> assertEquals(String.valueOf(applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(0).getIncomeIds()[0]), String.valueOf(applicant.getIncome(folderBuilderOrderSec.getBorrowerCurrentEmployment()).getId()), "[Borrower] First id job on the list should be equal to primary job id[Dept of Interior]"),
                () -> assertEquals(String.valueOf(applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(1).getIncomeIds()[0]), String.valueOf(applicant.getIncome(folderBuilderOrderSec.getBorrowerSecondEmployment()).getId()), "[Borrower] Second id job on the list should be equal to Secondary 1 job id[ThirdEmp]"),
                () -> assertEquals(String.valueOf(applicant.getIncomeCategoryW2()
                        .getIncomeGroups().get(2).getIncomeIds()[0]), String.valueOf(applicant.getIncome(thirdEmploymentName).getId()), "Borrower]Third id job on the list should be equal to Secondary 2 job id[Ouya Yebe Inc]")
        );
    }
}
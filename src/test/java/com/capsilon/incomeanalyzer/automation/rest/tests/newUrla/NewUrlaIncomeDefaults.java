package com.capsilon.incomeanalyzer.automation.rest.tests.newUrla;

import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestApplicantIncomeCategory;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFNMBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@ResourceLock(value = "SampleTestNormalLock")
@EnableIfToggled(propertyName = BONUS_TOGGLE)
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "DefaultsTest")
public class NewUrlaIncomeDefaults extends TestBaseRest {

    private static final IAFNMBuilder fnmBuilderCalcTest = new IAFNMBuilder("IARDfltsTst");
    private static RestGetResponse getResponse;

    @BeforeAll
    void setupFolder() {
        fnmBuilderCalcTest.generateSecondaryJobsLoanDocument()
                .restBuild();
        dataUpload = createUploadObject(fnmBuilderCalcTest);
        getResponse = RestGetLoanData.getApplicationData(fnmBuilderCalcTest.getFolderId());

        fnmBuilderCalcTest.setDefaultsToNewUrla();
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemSingular().getIncomeItemDetail()
                .get(0).setIncomeTypeTotalAmount(bigD(1000));
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemSingular().getIncomeItemDetail()
                .get(1).setIncomeTypeTotalAmount(bigD(1000));
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemSingular().getIncomeItemDetail()
                .get(2).setIncomeTypeTotalAmount(bigD(1000));
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getIncomeItemSingular().getIncomeItemDetail()
                .get(3).setIncomeTypeTotalAmount(bigD(0));
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getIncomeItemSingular().getIncomeItemDetail()
                .get(0).setIncomeTypeTotalAmount(bigD(0));
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getIncomeItemSingular().getIncomeItemDetail()
                .get(1).setIncomeTypeTotalAmount(bigD(0));
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getIncomeItemSingular().getIncomeItemDetail()
                .get(2).setIncomeTypeTotalAmount(bigD(0));
        fnmBuilderCalcTest.canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getIncomeItemSingular().getIncomeItemDetail()
                .get(3).setIncomeTypeTotalAmount(bigD(0));
        fnmBuilderCalcTest.uploadNewUrlaToFolder();
    }

    @Test
    @Description("IA-2668 check if deafult income selections are correct")
    void checkIfIncomesAreSelectedCorrectlyByDefault() {

        Retry.tryRun(TIMEOUT_FIVE_SECONDS, TIMEOUT_FIFTEEN_SECONDS, () -> {
            RestApplicantIncomeCategory incomeGroup = RestGetLoanData.getApplicationData(fnmBuilderCalcTest.getFolderId())
                    .getApplicant(fnmBuilderCalcTest.getBorrowerFullName())
                    .getIncomeCategoryW2();

            assertAll("Asserting lowest income is selected",
                    () -> assertTrue(incomeGroup.getPrimaryIncomeGroup().getSelected()),
                    () -> assertTrue(incomeGroup.getPrimaryIncomeGroup().getIncomeTypeBasePay().getSelected()),
                    () -> assertTrue(incomeGroup.getPrimaryIncomeGroup().getIncomeTypeOvertime().getSelected()),
                    () -> assertTrue(incomeGroup.getPrimaryIncomeGroup().getIncomeTypeCommissions().getSelected()),
                    () -> assertFalse(incomeGroup.getPrimaryIncomeGroup().getIncomeTypeBonus().getSelected()),
                    () -> assertFalse(incomeGroup.getSecondaryIncomeGroup().getSelected()),
                    () -> assertFalse(incomeGroup.getSecondaryIncomeGroup().getIncomeTypeBonus().getSelected()),
                    () -> assertFalse(incomeGroup.getSecondaryIncomeGroup().getIncomeTypeOvertime().getSelected()),
                    () -> assertFalse(incomeGroup.getSecondaryIncomeGroup().getIncomeTypeCommissions().getSelected()),
                    () -> assertFalse(incomeGroup.getSecondaryIncomeGroup().getIncomeTypeBonus().getSelected()));
        });
    }

}


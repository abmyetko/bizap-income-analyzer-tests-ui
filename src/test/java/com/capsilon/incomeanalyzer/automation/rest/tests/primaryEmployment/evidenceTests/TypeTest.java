package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.evidenceTests;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@ResourceLock(value = "TypeTest")
class TypeTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderTypeTest = createFolderBuilder("IARestType");

    @BeforeAll
    void setupTestFolder() {
        folderBuilderTypeTest
                .setCoBorrowerPreviousJobEndDate("01/01/" + YEAR_TO_DATE)
                .generateLoanDocument().restBuild();
        dataUpload = createUploadObject(folderBuilderTypeTest);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderTypeTest.getBorrowerFullName(),
                        folderBuilderTypeTest.getBorrowerSSN(),
                        folderBuilderTypeTest.getBorrowerCollaboratorId(),
                        folderBuilderTypeTest.getBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/07/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderTypeTest.getCoBorrowerFullName(),
                        folderBuilderTypeTest.getCoBorrowerSSN(),
                        folderBuilderTypeTest.getCoBorrowerCollaboratorId(),
                        folderBuilderTypeTest.getCoBorrowerCurrentEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/07/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "15.00", "40.00", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        folderBuilderTypeTest.getCoBorrowerFullName(),
                        folderBuilderTypeTest.getCoBorrowerSSN(),
                        folderBuilderTypeTest.getCoBorrowerCollaboratorId(),
                        folderBuilderTypeTest.getCoBorrowerPreviousEmployment(),
                        "01/01/" + YEAR_TO_DATE,
                        "01/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "2595", "173.33", "2595", "2595")))
                .importDocumentList();
    }

    @Test
    void typeShouldBeSalariedForTheGivenDateRangeFromPaystub() {
        assertCorrectType(folderBuilderTypeTest.getBorrowerFullName(), folderBuilderTypeTest.getBorrowerCurrentEmployment(), IncomeType.SALARIED);
    }

    @Test
    void typeShouldBeHourlyForTheGivenDateAndHoursRateFromPaystub() {
        assertCorrectType(folderBuilderTypeTest.getCoBorrowerFullName(), folderBuilderTypeTest.getCoBorrowerCurrentEmployment(), IncomeType.HOURLY);
    }

    @Test
    void typeShouldBeSalariedForTheGivenDateRangeAndHoursRateFromPaystub() {
        assertCorrectType(folderBuilderTypeTest.getCoBorrowerFullName(), folderBuilderTypeTest.getCoBorrowerPreviousEmployment(), IncomeType.SALARIED);
    }

    public void assertCorrectType(String fullName, String employment, IncomeType expectedType) {
        assertCorrectType(fullName, employment, expectedType, YEAR_TO_DATE);
    }

    public void assertCorrectType(String fullName, String employment, IncomeType expectedType, Integer year) {
        assertEquals(expectedType,
                RestGetLoanData.getApplicationData(folderBuilderTypeTest.getFolderId())
                        .getApplicant(fullName)
                        .getIncome("W2", employment)
                        .getPart("Base Pay")
                        .getIncome(year)
                        .getAnnualSummary().get(0)
                        .getType().getValue());
    }
}

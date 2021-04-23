package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment.income.bonus;

import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomePart;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestIncomeType;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import com.capsilon.test.reportportal.Description;
import com.capsilon.test.ui.Retry;
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

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.BONUS_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.params.provider.Arguments.of;


@SuppressWarnings({"unchecked", "rawtypes"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableIfToggled(propertyName = BONUS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "BonusTest")
public class BonusTest extends TestBaseRest {

    private final IAFolderBuilder folderBuilderBonusConventionalTest = createFolderBuilder("IARBonusCov");
    private final IAFolderBuilder folderBuilderBonusFHATest = createFolderBuilder("IARBonusFha");

    private Stream loanDocStream() {
        return Stream.of(
                of(folderBuilderBonusConventionalTest, MortgageType.CONVENTIONAL), //Conventional Loan
                of(folderBuilderBonusFHATest, MortgageType.FHA) //FHA Loan
        );
    }

    @BeforeAll
    void generateFolder() {
        loanDocStream().forEach(streamLoanDoc -> uploadLoanDocAndIncomeDocuments((IAFolderBuilder) ((Arguments) streamLoanDoc).get()[0], (MortgageType) ((Arguments) streamLoanDoc).get()[1]));
    }

    void uploadLoanDocAndIncomeDocuments(IAFolderBuilder folderBuilder, MortgageType loanType) {
        folderBuilder.setMortgageAppliedFor(loanType)
                .generateLoanDocumentWithNoIncome()
                .setPrimaryJobBasePay(folderBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobBonus(folderBuilder.getBorrowerSSN(), "0.10")
                .setPrimaryJobBasePay(folderBuilder.getCoBorrowerSSN(), "0.10")
                .setPrimaryJobBonus(folderBuilder.getCoBorrowerSSN(), "0.10")
                .restBuild();

        DataUploadObject dataUpload = createUploadObject(folderBuilder);

        Retry.whileTrue(TIMEOUT_TWENTY_SECONDS, TIMEOUT_ONE_MINUTE, () -> {
            dataUpload.clearDocuments()
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getBorrowerFullName(),
                            folderBuilder.getBorrowerSSN(),
                            folderBuilder.getBorrowerCollaboratorId(),
                            folderBuilder.getBorrowerCurrentEmployment(),
                            "03/01/" + YEAR_TO_DATE,
                            "03/31/" + YEAR_TO_DATE,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750"),
                            new PaystubData.PaystubIncomeRow(MONTHLY_BONUS, "", "", "250", "750")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getBorrowerFullName(),
                            folderBuilder.getBorrowerSSN(),
                            folderBuilder.getBorrowerCollaboratorId(),
                            folderBuilder.getBorrowerCurrentEmployment(),
                            "12/01/" + ONE_YEAR_PRIOR,
                            "12/31/" + ONE_YEAR_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1200", "3600"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "3000")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getBorrowerFullName(),
                            folderBuilder.getBorrowerSSN(),
                            folderBuilder.getBorrowerCollaboratorId(),
                            folderBuilder.getBorrowerCurrentEmployment(),
                            "12/01/" + TWO_YEARS_PRIOR,
                            "12/31/" + TWO_YEARS_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750")))
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
                            .setYtdBonus("1500")
                            .setPriorYearBonus("2800")
                            .setTwoYearPriorBonus("2500"))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getCoBorrowerFullName(),
                            folderBuilder.getCoBorrowerSSN(),
                            folderBuilder.getCoBorrowerCollaboratorId(),
                            folderBuilder.getCoBorrowerCurrentEmployment(),
                            "03/01/" + YEAR_TO_DATE,
                            "03/31/" + YEAR_TO_DATE,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "250", "750")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getCoBorrowerFullName(),
                            folderBuilder.getCoBorrowerSSN(),
                            folderBuilder.getCoBorrowerCollaboratorId(),
                            folderBuilder.getCoBorrowerCurrentEmployment(),
                            "12/01/" + ONE_YEAR_PRIOR,
                            "12/31/" + ONE_YEAR_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "7200"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "150", "1800")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getCoBorrowerFullName(),
                            folderBuilder.getCoBorrowerSSN(),
                            folderBuilder.getCoBorrowerCollaboratorId(),
                            folderBuilder.getCoBorrowerCurrentEmployment(),
                            "12/01/" + TWO_YEARS_PRIOR,
                            "12/31/" + TWO_YEARS_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "4000", "4000"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "125", "125")))
                    .addDocument(dataUpload.createCustomEVoeCurrent(
                            folderBuilder.getCoBorrowerFullName(),
                            folderBuilder.getCoBorrowerSSN(),
                            folderBuilder.getCoBorrowerCollaboratorId(),
                            folderBuilder.getCoBorrowerCurrentEmployment(),
                            IncomeFrequency.MONTHLY,
                            "01/01/" + TWO_YEARS_PRIOR,
                            "01/31/" + YEAR_TO_DATE,
                            "1200",
                            "1200",
                            "01/31/" + YEAR_TO_DATE)
                            .setPriorYearBasePay("14000")
                            .setTwoYearPriorBasePay("13000")
                            .setYtdBonus("200")
                            .setPriorYearBonus("1800")
                            .setTwoYearPriorBonus("1500"))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getBorrowerFullName(),
                            folderBuilder.getBorrowerSSN(),
                            folderBuilder.getBorrowerCollaboratorId(),
                            folderBuilder.getBorrowerPreviousEmployment(),
                            "03/01/" + YEAR_TO_DATE,
                            "03/31/" + YEAR_TO_DATE,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1", "1"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "400", "500"),
                            new PaystubData.PaystubIncomeRow(MONTHLY_BONUS, "", "", "400", "500")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getBorrowerFullName(),
                            folderBuilder.getBorrowerSSN(),
                            folderBuilder.getBorrowerCollaboratorId(),
                            folderBuilder.getBorrowerPreviousEmployment(),
                            "12/01/" + ONE_YEAR_PRIOR,
                            "12/31/" + ONE_YEAR_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1", "1"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "400", "15690")))
                    .addDocument(dataUpload.createCustomPaystub(
                            folderBuilder.getBorrowerFullName(),
                            folderBuilder.getBorrowerSSN(),
                            folderBuilder.getBorrowerCollaboratorId(),
                            folderBuilder.getBorrowerPreviousEmployment(),
                            "12/01/" + TWO_YEARS_PRIOR,
                            "12/31/" + TWO_YEARS_PRIOR,
                            new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1", "1"),
                            new PaystubData.PaystubIncomeRow(BONUS, "", "", "400", "750")))
                    .importDocumentList();
            return RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getDocuments().size() < 3;
        }, "Documents were not present in IA response after three tries");
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(1)
    @Description("IA-1223 Check Total Months Worked, Avg Bonus Income And Trending values in Bonus Annual Trending section")
    void checkBonusAnnualTrendingSectionValues(IAFolderBuilder folderBuilder) {
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {

            RestIncomePart borrowerBonus = RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                    .getApplicant(folderBuilder.getBorrowerFullName()).getIncome(folderBuilder.getBorrowerPreviousEmployment()).getBonus();
            String primaryGroupId = Long.toString(RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                    .getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncome(folderBuilder.getBorrowerCurrentEmployment()).getId());

            selectIncome(primaryGroupId, borrowerBonus.getIncome(YEAR_TO_DATE).getId(), true);
            selectIncome(primaryGroupId, borrowerBonus.getIncome(ONE_YEAR_PRIOR).getId(), true);
            selectIncome(primaryGroupId, borrowerBonus.getIncome(TWO_YEARS_PRIOR).getId(), true);

            RestIncomeType borrowerBonusCategory = RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                    .getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeBonus();
            RestIncomeType coBorrowerBonusCategory = RestGetLoanData.getApplicationData(folderBuilder.getFolderId())
                    .getApplicant(folderBuilder.getCoBorrowerFullName())
                    .getIncomeCategoryW2()
                    .getPrimaryIncomeGroup()
                    .getIncomeTypeBonus();

            assertAll("Annual Trending assertions",
                    () -> assertEquals(bigD(208.33),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Incorrect Avg Monthly Income value for borrower"),
                    () -> assertEquals(bigD(1540.83),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Incorrect Avg Monthly Income value for borrower"),
                    () -> assertEquals(bigD(125.00),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Incorrect Avg Monthly Income value for borrower"),

                    () -> assertEquals(bigD(12),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getMonths()), "Incorrect Months Worked value for borrower"),
                    () -> assertEquals(bigD(12),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getMonths()), "Incorrect Months Worked value for borrower"),
                    () -> assertEquals(bigD(12),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getMonths()), "Incorrect Months Worked value for borrower"),

                    () -> assertEquals(bigD(-86.48),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getTrending()), "Incorrect Trending value for borrower"),
                    () -> assertEquals(bigD(1132.67),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getTrending()), "Incorrect Trending value for borrower"),
                    () -> assertNull(
                            borrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getTrending(), "Incorrect Trending value for borrower"),

                    () -> assertEquals(bigD(2500),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getGross()), "Incorrect Gross value for borrower"),
                    () -> assertEquals(bigD(18490),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getGross()), "Incorrect Gross value for borrower"),
                    () -> assertEquals(bigD(1500),
                            bigD(borrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getGross()), "Incorrect Gross value for borrower"),

                    () -> assertEquals(bigD(16.67),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getAvgMonthlyIncome()), "Incorrect Avg Monthly Income value for coBorrower"),
                    () -> assertEquals(bigD(150),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getAvgMonthlyIncome()), "Incorrect Avg Monthly Income value for coBorrower"),
                    () -> assertEquals(bigD(10.416),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getAvgMonthlyIncome()), "Incorrect Avg Monthly Income value for coBorrower"),

                    () -> assertEquals(bigD(12),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getMonths()), "Incorrect Months Worked value for coBorrower"),
                    () -> assertEquals(bigD(12),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getMonths()), "Incorrect Months Worked value for coBorrower"),
                    () -> assertEquals(bigD(12),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getMonths()), "Incorrect Months Worked value for coBorrower"),

                    () -> assertEquals(bigD(-88.89),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getTrending()), "Incorrect Trending value for coBorrower"),
                    () -> assertEquals(bigD(1340),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getTrending()), "Incorrect Trending value for coBorrower"),
                    () -> assertNull(
                            coBorrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getTrending(), "Incorrect Trending value for coBorrower"),

                    () -> assertEquals(bigD(200),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(YEAR_TO_DATE).getGross()), "Incorrect Gross value for coBorrower"),
                    () -> assertEquals(bigD(1800),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(ONE_YEAR_PRIOR).getGross()), "Incorrect Gross value for coBorrower"),
                    () -> assertEquals(bigD(125),
                            bigD(coBorrowerBonusCategory.getTotalSelectedIncome(TWO_YEARS_PRIOR).getGross()), "Incorrect Gross value for coBorrower")
            );
        });
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(2)
    @Description("IA-2516 Check If Bonus Income Is Equal To Zero When Section Is Deselected")
    void checkIfBonusIncomeDoesNotChangeWhenSectionIsDeselected(IAFolderBuilder folderBuilder) {
        RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());
        selectIncomeType(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getId(),
                getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                IncomePartType.BONUS, false);
        Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
            RestIncomeType firstApplicantBonus = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                    .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus();

            assertAll("Bonus income type section assertions",
                    () -> assertFalse(firstApplicantBonus.getQualified(), "Bonus section is qualified"),
                    () -> assertFalse(firstApplicantBonus.getSelected(), "Bonus section is selected"),
                    () -> assertEquals(
                            bigD(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2()
                                    .getPrimaryIncomeGroup().getIncomeTypeBonus().getQualifyingIncome()),
                            bigD(firstApplicantBonus.getQualifyingIncome()), "Bonus section qualifying income value is not equal to zero")
            );
        });
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(2)
    @Description("IA-2516 Check If Bonus Income Is Correct When Section Is Selected")
    void checkIfBonusIncomeIsCorrectWhenSectionIsSelected(IAFolderBuilder folderBuilder) {
        Retry.tryRun(TIMEOUT_FORTY_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            restoreApplicantDefaults(RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName()).getId());
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

            selectIncomeGroup(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getId(),
                    getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                    true);
            selectIncomeType(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getId(),
                    getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                    IncomePartType.BONUS, true);
            Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
                RestIncomeType firstApplicantBonus = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus();

                assertAll("Bonus income type section assertions",
                        () -> assertTrue(firstApplicantBonus.getQualified(), "Bonus section is not qualified"),
                        () -> assertTrue(firstApplicantBonus.getSelected(), "Bonus section is not selected"),
                        () -> assertEquals(bigD(125.00),
                                bigD(firstApplicantBonus.getQualifyingIncome()), "Incorrect bonus section qualifying income value")
                );
            });
        });
    }

    @ParameterizedTest
    @MethodSource("loanDocStream")
    @Order(3)
    @Description("IA-2516 Check If Bonus Income Is Equal To Zero When No Documents Are Present And Section Is Selected")
    void checkIfBonusIncomeIsZeroWhenNoDocumentsArePresentAndSectionIsSelected(IAFolderBuilder folderBuilder) {
        Retry.tryRun(TIMEOUT_FORTY_SECONDS, TIMEOUT_TWO_MINUTES, () -> {
            DataUploadObject dataUpload = createUploadObject(folderBuilder);
            dataUpload.removeDocumentsFromFolder();
            RestGetResponse getResponse = RestGetLoanData.getApplicationData(folderBuilder.getFolderId());

            selectIncomeType(getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getId(),
                    getResponse.getApplicant(folderBuilder.getBorrowerFullName()).getIncomeCategoryW2().getPrimaryIncomeGroup().getId(),
                    IncomePartType.BONUS, true);
            Retry.tryRun(TIMEOUT_TEN_SECONDS, TIMEOUT_FORTY_SECONDS, () -> {
                RestIncomeType firstApplicantBonus = RestGetLoanData.getApplicationData(folderBuilder.getFolderId()).getApplicant(folderBuilder.getBorrowerFullName())
                        .getIncomeCategoryW2().getPrimaryIncomeGroup().getIncomeTypeBonus();

                assertAll("Bonus income type section assertions",
                        () -> assertFalse(firstApplicantBonus.getQualified(), "Bonus section is qualified"),
                        () -> assertTrue(firstApplicantBonus.getSelected(), "Bonus section is not selected"),
                        () -> assertEquals(bigD(0),
                                bigD(firstApplicantBonus.getQualifyingIncome()), "Bonus section qualifying income value is not equal to zero")
                );
            });
        });
    }
}

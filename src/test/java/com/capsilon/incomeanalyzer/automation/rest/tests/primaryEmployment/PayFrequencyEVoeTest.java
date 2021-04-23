package com.capsilon.incomeanalyzer.automation.rest.tests.primaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.base.TestBaseRest;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestPartIncomeAnnualSummary;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.test.reportportal.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.BI_WEEKLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.MONTHLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.EVOE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType.VOE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SuppressWarnings({"unchecked", "rawtypes"})
@Execution(CONCURRENT)
@ResourceLock(value = "PayFrequencyEVoeTest")
public class PayFrequencyEVoeTest extends TestBaseRest {

    private final IAFolderBuilder loanPayFrequencyEVoe = createFolderBuilder("IARFreqEvoe");

    @BeforeAll
    void uploadLoanDocAndIncomeDocuments() {
        loanPayFrequencyEVoe
                .setBorrowerYearsOnThisJob("4")
                .generateLoanDocument().restBuild();

        dataUpload = createUploadObject(loanPayFrequencyEVoe);

        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomEVoeCurrent(
                        loanPayFrequencyEVoe.getBorrowerFullName(),
                        loanPayFrequencyEVoe.getBorrowerSSN(),
                        loanPayFrequencyEVoe.getBorrowerCollaboratorId(),
                        loanPayFrequencyEVoe.getBorrowerCurrentEmployment(),
                        MONTHLY,
                        "05/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        "2800",
                        "24000",
                        "12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("26000")
                        .setTwoYearPriorBasePay("20000")
                        .setRate("2800"))
                .addDocument(dataUpload.createCustomVoeCurrent(
                        loanPayFrequencyEVoe.getCoBorrowerFullName(),
                        loanPayFrequencyEVoe.getCoBorrowerSSN(),
                        loanPayFrequencyEVoe.getCoBorrowerCollaboratorId(),
                        loanPayFrequencyEVoe.getCoBorrowerCurrentEmployment(),
                        BI_WEEKLY,
                        "07/01/" + TWO_YEARS_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        "1200",
                        "1200",
                        "12/31/" + ONE_YEAR_PRIOR)
                        .setPriorYearBasePay("14000")
                        .setTwoYearPriorBasePay("13000")
                        .setYtdOvertime("200")
                        .setPriorYearOvertime("1800")
                        .setTwoYearPriorOvertime("1500"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanPayFrequencyEVoe.getBorrowerFullName(),
                        loanPayFrequencyEVoe.getBorrowerSSN(),
                        loanPayFrequencyEVoe.getBorrowerCollaboratorId(),
                        loanPayFrequencyEVoe.getBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .addDocument(dataUpload.createCustomPaystub(
                        loanPayFrequencyEVoe.getCoBorrowerFullName(),
                        loanPayFrequencyEVoe.getCoBorrowerSSN(),
                        loanPayFrequencyEVoe.getCoBorrowerCollaboratorId(),
                        loanPayFrequencyEVoe.getCoBorrowerCurrentEmployment(),
                        "07/01/" + YEAR_TO_DATE,
                        "07/15/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(HOLIDAY, "", "", "296.00", ""),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "1,193.28", ""),
                        new PaystubData.PaystubIncomeRow(PTO, "", "", "20.00", ""))
                        .setYtdGrossIncomeAmount("1265"))
                .importDocumentList();
    }

    @Test
    @Description("IA-1991 Checking pay frequency for Voe and Evoe in one year prior")
    void checkFrequencyForVoeAndEvoeInOneYearPrior() {
        RestPartIncomeAnnualSummary evoeFrequencyYTDBor = RestGetLoanData.getApplicationData(loanPayFrequencyEVoe.getFolderId()).getApplicant(loanPayFrequencyEVoe.getBorrowerFullName())
                .getIncome(loanPayFrequencyEVoe.getBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(EVOE);
        RestPartIncomeAnnualSummary voeFrequencyYTDCoBor = RestGetLoanData.getApplicationData(loanPayFrequencyEVoe.getFolderId()).getApplicant(loanPayFrequencyEVoe.getCoBorrowerFullName())
                .getIncome(loanPayFrequencyEVoe.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(ONE_YEAR_PRIOR).getAnnualSummaryDocument(VOE);

        assertAll("Frequency assertion",
                () -> assertEquals(MONTHLY, evoeFrequencyYTDBor.getFrequency().getValue(), "[Borrower] Frequency for EVOE in ONE_YEAR_PRIOR should be Monthly"),
                () -> assertEquals(BI_WEEKLY, voeFrequencyYTDCoBor.getFrequency().getValue(), "[CoBorrower] Frequency for VOE in ONE_YEAR_PRIOR should be Bi-weekly"));
    }

    @Test
    @Description("IA-1991 Checking pay frequency for Voe and Evoe in two years prior")
    void checkFrequencyForVoeAndEvoeInTwoYearsPrior() {
        RestPartIncomeAnnualSummary evoeFrequencyYTDBor = RestGetLoanData.getApplicationData(loanPayFrequencyEVoe.getFolderId()).getApplicant(loanPayFrequencyEVoe.getBorrowerFullName())
                .getIncome(loanPayFrequencyEVoe.getBorrowerCurrentEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(EVOE);
        RestPartIncomeAnnualSummary voeFrequencyYTDCoBor = RestGetLoanData.getApplicationData(loanPayFrequencyEVoe.getFolderId()).getApplicant(loanPayFrequencyEVoe.getCoBorrowerFullName())
                .getIncome(loanPayFrequencyEVoe.getCoBorrowerCurrentEmployment()).getBasePay().getIncome(TWO_YEARS_PRIOR).getAnnualSummaryDocument(VOE);

        assertAll("Frequency assertion",
                () -> assertEquals(MONTHLY, evoeFrequencyYTDBor.getFrequency().getValue(), "[Borrower] Frequency for EVOE in TWO_YEARS_PRIOR should be Monthly"),
                () -> assertEquals(BI_WEEKLY, voeFrequencyYTDCoBor.getFrequency().getValue(), "[CoBorrower] Frequency for VOE in TWO_YEARS_PRIOR should be Bi-weekly"));
    }
}
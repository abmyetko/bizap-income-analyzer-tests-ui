package com.capsilon.incomeanalyzer.automation.ui.componentTests.secondaryEmployment;

import com.capsilon.incomeanalyzer.automation.data.upload.data.PaystubData;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.ui.base.TestBaseUI;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.IncomeCategoryIncomeGroup;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.IncomeTypeSectionBody;
import com.capsilon.incomeanalyzer.automation.ui.view.ApplicantView;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.conditions.EnableIfToggled;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.capsilon.test.reportportal.Description;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartCategory.W2;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles.SECONDARY_JOBS_TOGGLE;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@DisplayName("Secondary Dif fYtd Test")
@EnableIfToggled(propertyName = SECONDARY_JOBS_TOGGLE)
@Execution(CONCURRENT)
@ResourceLock(value = "SecondaryDiffYtdTest")
public class SecondaryDiffYtdTest extends TestBaseUI {

    private final IAFolderBuilder loanDiffYtdBuilder = createFolderBuilder("IAUIDiffYtd");
    private final String thirdEmploymentName = "SecEmp2";

    @BeforeAll
    public void generateDocs() {
        loanDiffYtdBuilder
                .setCoBorrowerPreviousJobStartDate("01/01/" + THREE_YEARS_PRIOR)
                .setCoBorrowerPreviousJobEndDate("12/31/" + ONE_YEAR_PRIOR)
                .generateSecondaryJobsLoanDocument()
                .addNewEmployment(thirdEmploymentName, loanDiffYtdBuilder.getCoBorrowerSecondJobStartDate(),
                        "", true, true, "0.00");

        loginCreateLoanAndGoToIncomeAnalyzerBizapp(loanDiffYtdBuilder.uiBuild());

        dataUpload = createUploadObject(loanDiffYtdBuilder);
        dataUpload.clearDocuments()
                .addDocument(dataUpload.createCustomPaystub(
                        loanDiffYtdBuilder.getBorrowerFullName(),
                        loanDiffYtdBuilder.getBorrowerSSN(),
                        loanDiffYtdBuilder.getBorrowerCollaboratorId(),
                        loanDiffYtdBuilder.getBorrowerCurrentEmployment(),
                        "01/01/" + (YEAR_TO_DATE + 1),
                        "01/31/" + (YEAR_TO_DATE + 1),
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanDiffYtdBuilder.getBorrowerFullName(),
                        loanDiffYtdBuilder.getBorrowerSSN(),
                        loanDiffYtdBuilder.getBorrowerCollaboratorId(),
                        loanDiffYtdBuilder.getBorrowerSecondEmployment(),
                        "12/01/" + YEAR_TO_DATE,
                        "12/31/" + YEAR_TO_DATE,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanDiffYtdBuilder.getBorrowerFullName(),
                        loanDiffYtdBuilder.getBorrowerSSN(),
                        loanDiffYtdBuilder.getBorrowerCollaboratorId(),
                        thirdEmploymentName,
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanDiffYtdBuilder.getBorrowerFullName(),
                        loanDiffYtdBuilder.getBorrowerSSN(),
                        loanDiffYtdBuilder.getBorrowerCollaboratorId(),
                        loanDiffYtdBuilder.getBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanDiffYtdBuilder.getCoBorrowerFullName(),
                        loanDiffYtdBuilder.getCoBorrowerSSN(),
                        loanDiffYtdBuilder.getCoBorrowerCollaboratorId(),
                        loanDiffYtdBuilder.getCoBorrowerCurrentEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "600.00")))
                .addDocument(dataUpload.createCustomPaystub(
                        loanDiffYtdBuilder.getCoBorrowerFullName(),
                        loanDiffYtdBuilder.getCoBorrowerSSN(),
                        loanDiffYtdBuilder.getCoBorrowerCollaboratorId(),
                        loanDiffYtdBuilder.getCoBorrowerPreviousEmployment(),
                        "12/01/" + ONE_YEAR_PRIOR,
                        "12/31/" + ONE_YEAR_PRIOR,
                        new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600.00", "7200")))
                .importDocumentList();
        refreshFolder();

        RestGetResponse rsp = RestGetLoanData.getApplicationData(loanDiffYtdBuilder.getFolderId());
    }

    @Test
    @Description("IA-2633 Check If Years Are Correct For Next Year Ytd Value")
    void checkIfYearsAreCorrectForNextYearYtdValue() {
        ApplicantView view = new ApplicantView();
        IncomeCategoryIncomeGroup incomeGroups = view.applicant(loanDiffYtdBuilder.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView();
        IncomeTypeSectionBody primaryIncomeSection = incomeGroups.incomeGroupByCurrentEmployerName(loanDiffYtdBuilder.getBorrowerCurrentEmployment(), true)
                .incomeType(IncomePartType.BASE_PAY).BODY;

        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(Integer.toString(YEAR_TO_DATE + 1))
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);

        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(Integer.toString(YEAR_TO_DATE + 1))
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
    }

    @Test
    @Description("IA-2633 Check If Years Are Correct For This Year Ytd Value")
    void checkIfYearsAreCorrectForThisYearYtdValue() {
        ApplicantView view = new ApplicantView();
        IncomeCategoryIncomeGroup incomeGroups = view.applicant(loanDiffYtdBuilder.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView();
        IncomeTypeSectionBody primaryIncomeSection = incomeGroups.incomeGroupByCurrentEmployerName(loanDiffYtdBuilder.getBorrowerSecondEmployment(), true)
                .incomeType(IncomePartType.BASE_PAY).BODY;

        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerSecondEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerSecondEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerSecondEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);

        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
    }

    @Test
    @Description("IA-2633 Check If Years Are Correct For Prior Year Ytd Value")
    void checkIfYearsAreCorrectForPriorYearYtdValue() {
        ApplicantView view = new ApplicantView();
        IncomeCategoryIncomeGroup incomeGroups = view.applicant(loanDiffYtdBuilder.getBorrowerFullName()).incomeCategory(W2).multiCurrentJobView();
        IncomeTypeSectionBody primaryIncomeSection = incomeGroups.incomeGroupByCurrentEmployerName(thirdEmploymentName, true)
                .incomeType(IncomePartType.BASE_PAY).BODY;

        primaryIncomeSection.employment(thirdEmploymentName).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(thirdEmploymentName).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(thirdEmploymentName).BODY.employmentYear(THREE_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);

        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getBorrowerPreviousEmployment()).BODY.employmentYear(THREE_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
    }

    @Test
    @Description("IA-2633 Check If Years Are Correct For CoBorrower Prior Year Ytd Value")
    void checkIfYearsAreCorrectForCoBorrowerPriorYearYtdValue() {
        ApplicantView view = new ApplicantView();
        IncomeCategoryIncomeGroup incomeGroups = view.applicant(loanDiffYtdBuilder.getCoBorrowerFullName()).incomeCategory(W2).multiCurrentJobView();
        IncomeTypeSectionBody primaryIncomeSection = incomeGroups.incomeGroupByCurrentEmployerName(loanDiffYtdBuilder.getCoBorrowerCurrentEmployment(), true)
                .incomeType(IncomePartType.BASE_PAY).BODY;

        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerCurrentEmployment()).BODY.employmentYear(THREE_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);

        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerPreviousEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerPreviousEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerPreviousEmployment()).BODY.employmentYear(THREE_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
    }

    @Test
    @Description("IA-2633 Check If Years Are Correct For CoBorrower Secondary Income With No Current Job Document")
    void checkIfYearsAreCorrectForCoBorrowerSecondaryIncomeWithNoDocument() {
        ApplicantView view = new ApplicantView();
        IncomeCategoryIncomeGroup incomeGroups = view.applicant(loanDiffYtdBuilder.getCoBorrowerFullName()).incomeCategory(W2).multiCurrentJobView();
        IncomeTypeSectionBody primaryIncomeSection = incomeGroups.incomeGroupByCurrentEmployerName(loanDiffYtdBuilder.getCoBorrowerSecondEmployment(), true)
                .incomeType(IncomePartType.BASE_PAY).BODY;

        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerSecondEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerSecondEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerSecondEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);

        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerPreviousEmployment()).BODY.employmentYear(YEAR_TO_DATE.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerPreviousEmployment()).BODY.employmentYear(ONE_YEAR_PRIOR.toString())
                .document(SummaryDocumentType.PAYSTUB).COMPONENT_CONTAINER.should(Condition.exist);
        primaryIncomeSection.employment(loanDiffYtdBuilder.getCoBorrowerPreviousEmployment()).BODY.employmentYear(TWO_YEARS_PRIOR.toString())
                .COMPONENT_CONTAINER.should(Condition.exist);
    }
}
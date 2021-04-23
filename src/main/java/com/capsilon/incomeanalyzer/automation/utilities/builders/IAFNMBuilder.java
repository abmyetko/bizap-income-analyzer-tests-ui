package com.capsilon.incomeanalyzer.automation.utilities.builders;

import com.capsilon.common.utils.fnmbuilder.FNMBuilder;
import com.capsilon.common.utils.mismo.structure.MESSAGE;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.document.datapoints.RestCanonicalDocument;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.enums.LoanDocumentTypeOfIncome;
import com.capsilon.test.ui.Retry;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.methods.RestUploadData.createLoanV2;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.DateUtilities.getDateFromSignDate;

public class IAFNMBuilder extends IAFolderBuilder<IAFNMBuilder> {//NOSONAR

    private final String currentLoanDocumentFilePath;
    private final RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();
    public RestCanonicalDocument canonicalUrlaDocument;
    FNMBuilder builder = new FNMBuilder();

    public IAFNMBuilder() {
        super(String.format(".\\target\\fnm-folder\\%s\\generatedFnmFile.fnm", LocalDateTime.now().toLocalDate()));
        currentLoanDocumentFilePath = String.format("%s%s.fnm", DEFAULT_FILEPATH.substring(0, DEFAULT_FILEPATH.lastIndexOf('.')), random.nextInt());
    }

    public IAFNMBuilder(String fnmCaseNumber) {
        super(String.format(".\\target\\fnm-folder\\%s\\generatedFnmFile.fnm", LocalDateTime.now().toLocalDate()));
        this.loanCaseNumber = fnmCaseNumber;
        currentLoanDocumentFilePath = String.format("%s%s%s.fnm", DEFAULT_FILEPATH.substring(0, DEFAULT_FILEPATH.lastIndexOf('.')), random.nextInt(), fnmCaseNumber);
    }

    public IAFNMBuilder restBuild() {
        restBuild(currentLoanDocumentFilePath);
        return this;
    }

    public IAFNMBuilder restBuild(String loanFilePath) {
        generateFolder(loanFilePath);
        return this;
    }

    public String uiBuild() {
        return uiBuild(currentLoanDocumentFilePath);
    }

    public String uiBuild(String loanFilePath) {
        return generateFolder(loanFilePath);
    }

    @Override
    protected String generateFolder(String loanFilePath) {
        File fnmFile = generatePhysicalDocument(loanFilePath);

        folderId = createLoanV2(fnmFile);

        Retry.tryRun(TIMEOUT_TWENTY_SECONDS, TIMEOUT_THREE_MINUTES, () -> RestGetLoanData.getApplicationData(getFolderId()));

        List<String> collaboratorIds = RestGetLoanData.getApplicationData(getFolderId()).getCollaboratorIds();
        borrowerCollaboratorId = collaboratorIds.get(0);
        if (collaboratorIds.size() > 1)
            coBorrowerCollaboratorId = collaboratorIds.get(1);

        return folderId;
    }

    public File generatePhysicalDocument() {
        return generatePhysicalDocument(currentLoanDocumentFilePath);
    }

    public File generatePhysicalDocument(String filePath) {
        File fnmFile = new File(filePath);
        builder.build(fnmFile.getAbsolutePath());
        return fnmFile;
    }

    @Override
    public IAFNMBuilder generateLoanDocument() {
        builder = new FNMBuilder();
        setLoanDocumentDefaultValues();
        return this;
    }

    @Override
    public IAFNMBuilder generateSecondaryJobsLoanDocument() {
        builder = new FNMBuilder();
        setLoanDocumentDefaultValues();
        this.builder.employmentInformation()
                .addNewEmployment(borrowerSecondEmployment,
                        transformDateToFnmFormat(borrowerSecondJobStartDate),
                        transformDateToFnmFormat(borrowerSecondJobEndDate),
                        true,
                        borrowerSecondJobIsCurrent,
                        "10.00");
        this.builder.employmentInformation()
                .addNewEmployment(coBorrowerSecondEmployment,
                        transformDateToFnmFormat(coBorrowerPreviousJobStartDate),
                        transformDateToFnmFormat(coBorrowerSecondJobEndDate),
                        false,
                        coBorrowerSecondJobIsCurrent,
                        "10.00");
        return this;
    }

    @Override
    protected IAFNMBuilder setLoanDocumentDefaultValues() {
        this.builder.other()
                .setTransactionID(loanCaseNumber);
        this.builder.typeOfMortgageAndTermsOfLoan()
                .setMortgageAppliedFor(mortgageAppliedFor.numCode)
                .setCaseNumber(loanCaseNumber);
        this.builder.borrowerInformation()
                .setBorrowerFirstName(borrowerFirstName)
                .setBorrowerLastName(borrowerLastName)
                .setCoBorrowerFirstName(coBorrowerFirstName)
                .setCoBorrowerLastName(coBorrowerLastName);
        if (!borrowerSSN.equalsIgnoreCase(DEFAULT_BORROWER_SSN)) {
            builder.borrowerInformation().setApplicantMainSocialSecurityNumber(borrowerSSN.replace("-", ""));
        }
        if (!coBorrowerSSN.equalsIgnoreCase(DEFAULT_CO_BORROWER_SSN)) {
            builder.borrowerInformation().setCoApplicantMainSocialSecurityNumber(coBorrowerSSN.replace("-", ""));
        }
        this.builder.employmentInformation()
                .setCurrentEmploymentNameForBorrower(borrowerCurrentEmployment)
                .setCurrentEmploymentNameForCoBorrower(coBorrowerCurrentEmployment)
                .setBorrowersYearsOnThisJobForBorrower(borrowerYearsOnThisJob)
                .setBorrowersYearsOnThisJobForCoBorrower(coBorrowerYearsOnThisJob)
                .setBorrowersMonthsOnThisJobForBorrower(borrowerMonthsOnThisJob)
                .setBorrowersMonthsOnThisJobForCoBorrower(coBorrowerMonthsOnThisJob)
                .setPreviousEmploymentNameForBorrower(borrowerPreviousEmployment)
                .setPreviousEmploymentNameForCoBorrower(coBorrowerPreviousEmployment)
                .setBorrowersPreviousJobStartDate(transformDateToFnmFormat(borrowerPreviousJobStartDate))
                .setBorrowersPreviousJobEndDate(transformDateToFnmFormat(borrowerPreviousJobEndDate))
                .setCoBorrowersPreviousJobStartDate(transformDateToFnmFormat(coBorrowerPreviousJobStartDate))
                .setCoBorrowersPreviousJobEndDate(transformDateToFnmFormat(coBorrowerPreviousJobEndDate));
        this.builder.informationForGovernmentMonitoringPurposes()
                .setSignDate(transformDateToFnmFormat(signDate));
        return this;
    }


    public IAFNMBuilder setPrimaryJobBasePay(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(applicantSSN, LoanDocumentTypeOfIncome.BASE, value);
        return this;
    }

    public IAFNMBuilder setPrimaryJobOvertime(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(applicantSSN, LoanDocumentTypeOfIncome.OVERTIME, value);
        return this;
    }

    public IAFNMBuilder setPrimaryJobCommissions(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(applicantSSN, LoanDocumentTypeOfIncome.COMMISSIONS, value);
        return this;
    }

    public IAFNMBuilder setPrimaryJobBonus(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(applicantSSN, LoanDocumentTypeOfIncome.BONUS, value);
        return this;
    }

    @Override
    public IAFNMBuilder generateLoanDocumentWithNoIncome() {
        this.builder = new FNMBuilder("/fnmbuilder/No_Type_Of_income_fnm_v2.fnm");
        setLoanDocumentDefaultValues();
        return this;
    }

    @Override
    public IAFNMBuilder generateLoanDocumentWithOneApplicant() {
        this.builder = new FNMBuilder("/fnmbuilder/One_Applicant_fnm_v2.fnm");
        this.builder.other()
                .setTransactionID(loanCaseNumber);
        this.builder.typeOfMortgageAndTermsOfLoan()
                .setCaseNumber(loanCaseNumber);
        this.builder.borrowerInformation()
                .setBorrowerFirstName(borrowerFirstName)
                .setBorrowerLastName(borrowerLastName);
        if (!borrowerSSN.equalsIgnoreCase(DEFAULT_BORROWER_SSN)) {
            this.builder.borrowerInformation().setApplicantMainSocialSecurityNumber(borrowerSSN.replace("-", ""));
        }
        this.builder.employmentInformation()
                .setCurrentEmploymentNameForBorrower(borrowerCurrentEmployment)
                .setBorrowersYearsOnThisJobForBorrower(borrowerYearsOnThisJob)
                .setBorrowersMonthsOnThisJobForBorrower(borrowerMonthsOnThisJob)
                .setPreviousEmploymentNameForBorrower(borrowerPreviousEmployment)
                .setBorrowersPreviousJobStartDate(transformDateToFnmFormat(borrowerPreviousJobStartDate))
                .setBorrowersPreviousJobEndDate(transformDateToFnmFormat(borrowerPreviousJobEndDate));
        this.builder.informationForGovernmentMonitoringPurposes()
                .setSignDate(transformDateToFnmFormat(signDate));
        return this;
    }


    @Override
    public IAFNMBuilder addLoanDocumentTypeOfIncome(String applicantSSN, LoanDocumentTypeOfIncome typeOfIncome, String value) {
        String leadingSpaces = "               ";
        builder.addNewRow("05I" + applicantSSN.replace("-", "") + typeOfIncome.numCode + leadingSpaces.substring(0, leadingSpaces.length() - value.length()) + value);
        return this;
    }

    @Override
    public IAFNMBuilder addLoanDocumentTypeOfIncome(String employer, String applicantSSN, LoanDocumentTypeOfIncome typeOfIncome, String value) {
        return null;
    }


    public void setDefaultsToNewUrla() {
        canonicalUrlaDocument = canonicalDocumentMethods.generateDefaultCanonicalDocumentFromFile(folderId, "/sampleCanonicalDocuments/NewUrla.json");

        String formattedSignDate = formatBuilderDateToCanonical(signDate);
        String borrowerCurrentStartDate = getDateFromSignDate(Integer.parseInt(borrowerYearsOnThisJob), Integer.parseInt(borrowerMonthsOnThisJob), Integer.parseInt(signDate.substring(3, 5)), signDate);
        String coBorrowerCurrentStartDate = getDateFromSignDate(Integer.parseInt(coBorrowerYearsOnThisJob), Integer.parseInt(coBorrowerMonthsOnThisJob), Integer.parseInt(signDate.substring(3, 5)), signDate);

        canonicalUrlaDocument.getCanonicalPayload().getLoanOriginator().getExecution().getExecutionDetail().setExecutionDate(formattedSignDate);
        canonicalUrlaDocument.setDataSourceStatus("UPDATED");
        canonicalUrlaDocument.setConversionDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnn")));

        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).setCollaboratorId(borrowerCollaboratorId);
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).setCollaboratorId(coBorrowerCollaboratorId);
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getExecution().getExecutionDetail().setExecutionDate(formattedSignDate);
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getExecution().getExecutionDetail().setExecutionDate(formattedSignDate);

        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getName().setFirstName(getBorrowerFirstName());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getName().setLastName(getBorrowerLastName());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getName().setFullName(getBorrowerFullName());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getTaxpayerIdentifier().get(0).setTaxpayerIdentifierValue(getBorrowerSSN().replace("-", ""));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getLegalEntity().getLegalEntityDetail().setFullName(getBorrowerCurrentEmployment());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(0).getEmployment().setEmploymentStartDate(formatBuilderDateToCanonical(borrowerCurrentStartDate));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getLegalEntity().getLegalEntityDetail().setFullName(getBorrowerSecondEmployment());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(1).getEmployment().setEmploymentStartDate(formatBuilderDateToCanonical(getBorrowerSecondJobStartDate()));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(2).getLegalEntity().getLegalEntityDetail().setFullName(getBorrowerPreviousEmployment());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(2).getEmployment().setEmploymentStartDate(formatBuilderDateToCanonical(getBorrowerPreviousJobStartDate()));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().get(2).getEmployment().setEmploymentEndDate(formatBuilderDateToCanonical(getBorrowerPreviousJobEndDate()));

        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getName().setFirstName(getCoBorrowerFirstName());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getName().setLastName(getCoBorrowerLastName());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getName().setFullName(getCoBorrowerFullName());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getTaxpayerIdentifier().get(0).setTaxpayerIdentifierValue(getCoBorrowerSSN().replace("-", ""));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().get(0).getLegalEntity().getLegalEntityDetail().setFullName(getCoBorrowerCurrentEmployment());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().get(0).getEmployment().setEmploymentStartDate(formatBuilderDateToCanonical(coBorrowerCurrentStartDate));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().get(1).getLegalEntity().getLegalEntityDetail().setFullName(getCoBorrowerSecondEmployment());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().get(1).getEmployment().setEmploymentStartDate(formatBuilderDateToCanonical(getCoBorrowerSecondJobStartDate()));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().get(2).getLegalEntity().getLegalEntityDetail().setFullName(getCoBorrowerPreviousEmployment());
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().get(2).getEmployment().setEmploymentStartDate(formatBuilderDateToCanonical(getCoBorrowerPreviousJobStartDate()));
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().get(2).getEmployment().setEmploymentEndDate(formatBuilderDateToCanonical(getCoBorrowerPreviousJobEndDate()));

    }

    public void removeSecondaryFromNewUrlaJobs() {
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(0).getEmployer().remove(1);
        canonicalUrlaDocument.getCanonicalPayload().getApplicant().get(1).getEmployer().remove(1);
    }

    public void uploadNewUrlaToFolder() {
        canonicalDocumentMethods.uploadCanonicalDocument(canonicalUrlaDocument, canonicalUrlaDocument.getSiteGuid());

    }

    public IAFNMBuilder addNewEmployment(String employment, String startDate, String endDate, Boolean isFirstApplicant, Boolean isCurrentEmployment, String monthlyIncome) {
        this.builder.employmentInformation().addNewEmployment(employment,
                transformDateToFnmFormat(startDate),
                transformDateToFnmFormat(endDate),
                isFirstApplicant,
                isCurrentEmployment,
                monthlyIncome);
        return this;
    }

    @Override
    public FNMBuilder getFnmBuilder() {
        return builder;
    }

    @Override
    public JAXBElement<MESSAGE> getMismoBuilder() {
        throw new NullPointerException("There is no Mismo Builder in IAFNMBuilder.class");
    }

    @Override
    public void uploadNewLoanDocument() {
        RestChangeRequests.uploadNewFnm(folderId, getFnmBuilder());
    }


}
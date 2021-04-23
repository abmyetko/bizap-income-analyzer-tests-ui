package com.capsilon.incomeanalyzer.automation.utilities.builders;


import com.capsilon.common.utils.fnmbuilder.FNMBuilder;
import com.capsilon.common.utils.mismo.structure.MESSAGE;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestCanonicalDocumentMethods;
import com.capsilon.incomeanalyzer.automation.utilities.enums.LoanDocumentTypeOfIncome;
import com.capsilon.incomeanalyzer.automation.utilities.enums.MortgageType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.xml.bind.JAXBElement;
import java.security.SecureRandom;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.DateUtilities.formatDate;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.MM_DD_YYYY_F_SLASH;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats.YYYY_MM_DD_DASH;

@Data
@Accessors(chain = true)
public abstract class IAFolderBuilder<T extends IAFolderBuilder<T>> {

    protected static final String LAST_DAY_OF_YEAR = "12/31/";
    protected static final String FIRST_DAY_OF_YEAR = "01/01/";
    protected static final String LAST_DAY_OF_JANUARY = "01/31/";
    protected static final String DEFAULT_PREVIOUS_YR_END_DATE = LAST_DAY_OF_YEAR + ONE_YEAR_PRIOR;
    protected static final int JOB_NAME_MAX_SIZE = 35;
    protected static final String DEFAULT_BORROWER_SSN = "999-40-5000";
    protected static final String DEFAULT_CO_BORROWER_SSN = "500-22-2000";
    public final String DEFAULT_FILEPATH;
    protected final SecureRandom random = new SecureRandom();
    protected String loanCaseNumber = "IATests";
    protected String signDate = "02/28/" + YEAR_TO_DATE;
    protected String borrowerFirstName = "Mary";
    protected String borrowerLastName = "Homeowner";
    protected String borrowerCurrentEmployment = "Dept of Interior Inc";
    protected String borrowerPreviousEmployment = "Awesome Computers";
    protected String borrowerCollaboratorId;
    protected String borrowerSSN = DEFAULT_BORROWER_SSN;
    protected String borrowerYearsOnThisJob = "10";
    protected String borrowerMonthsOnThisJob = "10";
    protected String borrowerPreviousJobStartDate = FIRST_DAY_OF_YEAR + FOUR_YEARS_PRIOR;
    protected String borrowerPreviousJobEndDate = LAST_DAY_OF_YEAR + YEAR_TO_DATE;
    protected String borrowerSecondEmployment = "Ouya Yebe Inc";
    protected String borrowerSecondJobStartDate = FIRST_DAY_OF_YEAR + THREE_YEARS_PRIOR;
    protected String borrowerSecondJobEndDate;
    protected Boolean borrowerSecondJobIsCurrent = true;
    protected String coBorrowerFirstName = "Erica";
    protected String coBorrowerLastName = "Lambert";
    protected String coBorrowerCurrentEmployment = "Capsilon One Inc";
    protected String coBorrowerPreviousEmployment = "Not Awesome Computers";
    protected String coBorrowerCollaboratorId;
    protected String coBorrowerSSN = DEFAULT_CO_BORROWER_SSN;
    protected String coBorrowerYearsOnThisJob = "15";
    protected String coBorrowerMonthsOnThisJob = "5";
    protected String coBorrowerPreviousJobStartDate = FIRST_DAY_OF_YEAR + TWO_YEARS_PRIOR;
    protected String coBorrowerPreviousJobEndDate = LAST_DAY_OF_YEAR + TWO_YEARS_PRIOR;
    protected String coBorrowerSecondEmployment = "Ouya Houya Inc";
    protected String coBorrowerSecondJobStartDate = FIRST_DAY_OF_YEAR + THREE_YEARS_PRIOR;
    protected String coBorrowerSecondJobEndDate;
    protected Boolean coBorrowerSecondJobIsCurrent = true;
    protected String folderId;
    protected MortgageType mortgageAppliedFor = MortgageType.CONVENTIONAL;
    protected Map<String, String> generatedDocuments;
    protected RestCanonicalDocumentMethods canonicalDocumentMethods = new RestCanonicalDocumentMethods();

    public IAFolderBuilder(String defaultFilePath) {
        DEFAULT_FILEPATH = defaultFilePath;
    }

    public abstract T restBuild();

    public abstract T restBuild(String loanFilePath);

    public abstract String uiBuild();

    public abstract String uiBuild(String loanFilePath);

    protected abstract String generateFolder(String loanFilePath);

    public abstract T generateLoanDocument();

    public abstract T generateSecondaryJobsLoanDocument();

    protected abstract T setLoanDocumentDefaultValues();

    public abstract T setPrimaryJobBasePay(String applicantSSN, String value);

    public abstract T setPrimaryJobOvertime(String applicantSSN, String value);

    public abstract T setPrimaryJobCommissions(String applicantSSN, String value);

    public abstract T setPrimaryJobBonus(String applicantSSN, String value);

    public abstract T generateLoanDocumentWithNoIncome();

    public abstract T generateLoanDocumentWithOneApplicant();

    public abstract T addLoanDocumentTypeOfIncome(String applicantSSN, LoanDocumentTypeOfIncome typeOfIncome, String value);

    public abstract T addLoanDocumentTypeOfIncome(String employer, String applicantSSN, LoanDocumentTypeOfIncome typeOfIncome, String value);

    public abstract T addNewEmployment(String employment, String startDate, String endDate, Boolean isFirstApplicant, Boolean isCurrentEmployment, String monthlyIncome);

    public abstract FNMBuilder getFnmBuilder();

    public abstract JAXBElement<MESSAGE> getMismoBuilder();

    public abstract void uploadNewLoanDocument();

    public String formatBuilderDateToCanonical(String builderDate) {
        return formatDate(MM_DD_YYYY_F_SLASH, builderDate, YYYY_MM_DD_DASH);
    }

    public int getRandomNumber(int bound) {
        return random.nextInt(bound);
    }

    public String getBorrowerFullName() {
        return borrowerFirstName + " " + borrowerLastName;
    }

    public String getCoBorrowerFullName() {
        return coBorrowerFirstName + " " + coBorrowerLastName;
    }
}

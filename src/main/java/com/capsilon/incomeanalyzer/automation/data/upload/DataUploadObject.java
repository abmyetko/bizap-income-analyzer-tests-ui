package com.capsilon.incomeanalyzer.automation.data.upload;

import com.capsilon.automation.dv.helpers.AsyncDVClient;
import com.capsilon.incomeanalyzer.automation.data.upload.data.*;
import com.capsilon.incomeanalyzer.automation.rest.RestCommons;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.ONE_YEAR_PRIOR;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.YEAR_TO_DATE;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency.MONTHLY;
import static com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups.REGULAR;

@SuppressWarnings({"squid:S00107", "unchecked", "rawtypes"})
public abstract class DataUploadObject<T extends DataUploadObject<T>> {

    protected final List<DocumentData> documentList = new ArrayList<>();
    protected String folderId;
    protected AsyncDVClient dvFolderClient;
    private int documentCount = 1;

    private String borrowerFullName;
    private String borrowerSSN;
    private String borrowerCollaboratorId;
    private String borrowerCurrentEmployment;
    private String borrowerPreviousEmployment;

    private String coBorrowerFullName;
    private String coBorrowerSSN;
    private String coBorrowerCollaboratorId;
    private String coBorrowerCurrentEmployment;
    private String coBorrowerPreviousEmployment;

    public DataUploadObject(IAFolderBuilder iafolderBuilder, AsyncDVClient dvFolderClient) {
        this(iafolderBuilder);
        this.dvFolderClient = dvFolderClient;
    }

    public DataUploadObject(IAFolderBuilder iafolderBuilder) {
        folderId = iafolderBuilder.getFolderId();

        borrowerFullName = iafolderBuilder.getBorrowerFirstName() + " " + iafolderBuilder.getBorrowerLastName();
        borrowerSSN = iafolderBuilder.getBorrowerSSN();
        borrowerCollaboratorId = iafolderBuilder.getBorrowerCollaboratorId();

        borrowerCurrentEmployment = iafolderBuilder.getBorrowerCurrentEmployment();
        borrowerPreviousEmployment = iafolderBuilder.getBorrowerPreviousEmployment();

        coBorrowerFullName = iafolderBuilder.getCoBorrowerFirstName() + " " + iafolderBuilder.getCoBorrowerLastName();
        coBorrowerSSN = iafolderBuilder.getCoBorrowerSSN();
        coBorrowerCollaboratorId = iafolderBuilder.getCoBorrowerCollaboratorId();
        coBorrowerCurrentEmployment = iafolderBuilder.getCoBorrowerCurrentEmployment();
        coBorrowerPreviousEmployment = iafolderBuilder.getCoBorrowerPreviousEmployment();

        documentCount += iafolderBuilder.getRandomNumber(SIX);
    }

    public List<DocumentData> getDocumentList() {
        return documentList; //NOSONAR
    }

    public T clearDocuments() {
        documentList.clear();
        return (T) this;
    }

    public T addDocument(DocumentData document) {
        documentList.add(document);
        return (T) this;
    }

    public T importDocument(DocumentData document) {
        importDocument(document, folderId);
        return (T) this;
    }

    public T importDocument(DocumentData document, int expectedStatusCode) {
        importDocument(document, folderId, expectedStatusCode);
        return (T) this;
    }

    public T importDocumentList() {
        importDocumentList(getGsonConfig());
        return (T) this;
    }

    public T importDocumentList(String folderId) {
        importDocumentList(getGsonConfig(), folderId);
        return (T) this;
    }

    public abstract T importDocument(DocumentData document, String folderId);

    public abstract T importDocument(DocumentData document, String folderId, int expectedStatusCode);

    public abstract T importDocumentList(Gson gsonConfig);

    public abstract T importDocumentList(Gson gsonConfig, String folderId);

    public abstract T removeDocumentsFromFolder();

    public PaystubData createDefaultPaystub() {
        return createDefaultPaystub(true, true, YEAR_TO_DATE);
    }

    public PaystubData createDefaultPaystub(Boolean primaryBorrower, Boolean currentJob, Integer documentEndYear) {
        String[] borrowerDetails = getBorrowerIncomeData(primaryBorrower, currentJob);
        String start = documentEndYear + (YEAR_TO_DATE.equals(documentEndYear) ? "-03-01" : "-12-01");
        String end = documentEndYear + (YEAR_TO_DATE.equals(documentEndYear) ? RestCommons.LAST_MARCH : "-12-31");
        return createCustomPaystub(borrowerDetails[ZERO], borrowerDetails[ONE], borrowerDetails[TWO], borrowerDetails[THREE], start, end,
                new PaystubData.PaystubIncomeRow(REGULAR, "", "", "600", "3000"));
    }

    @SuppressWarnings("squid:S923")
    public PaystubData createCustomPaystub(String borrowerName, String borrowerSSN, String collaboratorId, String employerName, String documentPeriodStartDate,
                                           String documentPeriodEndDate, PaystubData.PaystubIncomeRow... incomes) {
        PaystubData paystubData = new PaystubData("paystub" + docCount());
        paystubData.setBorrowerFullName(borrowerName)
                .setBorrowerSSN(borrowerSSN)
                .setCollaboratorId(collaboratorId)
                .setEmployerName(employerName)
                .setStartDate(documentPeriodStartDate)
                .setEndDate(documentPeriodEndDate)
                .setPayDate(documentPeriodEndDate);
        if (incomes != null) {
            for (PaystubData.PaystubIncomeRow income : incomes) {
                paystubData.addIncome(income);
            }
        }
        return paystubData;
    }

    public W2Data createDefaultW2() {
        return createDefaultW2(true, true, ONE_YEAR_PRIOR);
    }

    public W2Data createDefaultW2(Boolean primaryBorrower, Boolean currentJob, Integer year) {
        String[] borrowerDetails = getBorrowerIncomeData(primaryBorrower, currentJob);
        return createCustomW2(borrowerDetails[ZERO], borrowerDetails[ONE], borrowerDetails[TWO], borrowerDetails[THREE], "3000", year.toString());
    }

    public W2Data createCustomW2(String borrowerName, String borrowerSSN, String collaboratorId, String employerName,
                                 String wagesAndOtherCompensationAmount, String year) {
        W2Data w2Data = new W2Data("W2" + docCount());
        w2Data.setBorrowerFullName(borrowerName)
                .setBorrowerSSN(borrowerSSN)
                .setCollaboratorId(collaboratorId)
                .setEmployerName(employerName)
                .setYear(year)
                .setWagesTipsOtherCompensation(wagesAndOtherCompensationAmount);
        return w2Data;
    }

    public VoeCurrentData createDefaultVoeCurrent() {
        return createDefaultVoeCurrent(true, true, YEAR_TO_DATE);
    }

    public VoeCurrentData createDefaultVoeCurrent(Boolean primaryBorrower, Boolean currentJob, Integer documentEndYear) {
        String[] borrowerDetails = getBorrowerIncomeData(primaryBorrower, currentJob);
        String start = documentEndYear + RestCommons.LAST_MARCH;
        String end = (documentEndYear - THREE) + RestCommons.FIRST_JANUARY;
        return createCustomVoeCurrent(borrowerDetails[ZERO], borrowerDetails[ONE], borrowerDetails[TWO], borrowerDetails[THREE], MONTHLY, start, end, "600", "3000", end);
    }

    public VoeCurrentData createCustomVoeCurrent(String borrowerName, String borrowerSSN, String collaboratorId, String employerName, IncomeFrequency paymentPeriodType,
                                                 String employmentStartDate, String yearToDateIncomeThroughDate,
                                                 String baseIncomeAmount, String yearToDateBaseIncomeAmount, String executionDate) {
        VoeCurrentData voeData = new VoeCurrentData("voe" + docCount());
        voeData.setBorrowerFullName(borrowerName)
                .setBorrowerSSN(borrowerSSN)
                .setCollaboratorId(collaboratorId)
                .setEmployerName(employerName)
                .setFrequency(paymentPeriodType)
                .setEmploymentStartDate(employmentStartDate)
                .setYtdIncomeThruDate(yearToDateIncomeThroughDate)
                .setSignatureDate(executionDate)
                .setCurrentGrossBasePayAmount(baseIncomeAmount)
                .setYtdBasePay(yearToDateBaseIncomeAmount);

        return voeData;
    }

    public VoeFullData createCustomVoeFull(String borrowerName, String borrowerSSN, String collaboratorId, String employerName, IncomeFrequency paymentPeriodType,
                                           String employmentStartDate, String yearToDateIncomeThroughDate,
                                           String baseIncomeAmount, String yearToDateBaseIncomeAmount, String executionDate,

                                           IncomeFrequency paymentPeriodTypePrevious, String startDate, String endDate,
                                           String baseIncomeAmountPrevious, String overtimeIncomeAmountPrevious, String commissionsIncomeAmountPrevious,
                                           String bonusIncomeAmountPrevious) {
        VoeFullData voeData = new VoeFullData("voe" + docCount());
        voeData.setBorrowerFullName(borrowerName)
                .setBorrowerSSN(borrowerSSN)
                .setCollaboratorId(collaboratorId)
                .setEmployerName(employerName)
                .setFrequency(paymentPeriodType)
                .setEmploymentStartDate(employmentStartDate)
                .setYtdIncomeThruDate(yearToDateIncomeThroughDate)
                .setSignatureDate(executionDate)
                .setCurrentGrossBasePayAmount(baseIncomeAmount)
                .setYtdBasePay(yearToDateBaseIncomeAmount);
        voeData
                .setExecutionDate(endDate)
                .setPeriodType(paymentPeriodTypePrevious)
                .setHiredDate(startDate)
                .setTerminatedDate(endDate)
                .setBaseWageAmount(baseIncomeAmountPrevious)
                .setOvertimeWageAmount(overtimeIncomeAmountPrevious)
                .setCommissionWageAmount(commissionsIncomeAmountPrevious)
                .setBonusWageAmount(bonusIncomeAmountPrevious);

        return voeData;
    }

    public VoePreviousData createDefaultVoePrevious() {
        return createDefaultVoePrevious(true, true, YEAR_TO_DATE);
    }

    public VoePreviousData createDefaultVoePrevious(Boolean primaryBorrower, Boolean currentJob, Integer documentEndYear) {
        String[] borrowerDetails = getBorrowerIncomeData(primaryBorrower, currentJob);
        String start = documentEndYear + RestCommons.LAST_MARCH;
        String end = (documentEndYear - THREE) + RestCommons.FIRST_JANUARY;
        return createCustomVoePrevious(borrowerDetails[ZERO], borrowerDetails[ONE], borrowerDetails[TWO], borrowerDetails[THREE], MONTHLY,
                start, end,
                "1200", "1000",
                "600", "100");
    }

    public VoePreviousData createCustomVoePrevious(String borrowerName, String borrowerSSN, String collaboratorId, String employerName, IncomeFrequency paymentPeriodType,
                                                   String startDate, String endDate,
                                                   String baseIncomeAmount, String overtimeIncomeAmount,
                                                   String commissionsIncomeAmount, String bonusIncomeAmount) {
        VoePreviousData voeData = new VoePreviousData("voe" + docCount());
        voeData.setBorrowerFullName(borrowerName)
                .setBorrowerSSN(borrowerSSN)
                .setCollaboratorId(collaboratorId)
                .setEmployerName(employerName)
                .setExecutionDate(endDate)
                .setPeriodType(paymentPeriodType)
                .setHiredDate(startDate)
                .setTerminatedDate(endDate)
                .setBaseWageAmount(baseIncomeAmount)
                .setOvertimeWageAmount(overtimeIncomeAmount)
                .setCommissionWageAmount(commissionsIncomeAmount)
                .setBonusWageAmount(bonusIncomeAmount);

        return voeData;
    }

    public EVoeCurrentData createDefaultEVoe() {
        return createDefaultEVoe(true, true, YEAR_TO_DATE);
    }

    public EVoeCurrentData createDefaultEVoe(Boolean primaryBorrower, Boolean currentJob, Integer documentEndYear) {
        String[] borrowerDetails = getBorrowerIncomeData(primaryBorrower, currentJob);
        String start = documentEndYear + RestCommons.LAST_MARCH;
        String end = (documentEndYear - THREE) + RestCommons.FIRST_JANUARY;
        return createCustomEVoeCurrent(borrowerDetails[ZERO], borrowerDetails[ONE], borrowerDetails[TWO], borrowerDetails[THREE], MONTHLY,
                start, end,
                "1200", "3600", end);
    }

    public EVoeCurrentData createCustomEVoeCurrent(String borrowerName, String borrowerSSN, String collaboratorId, String employerName, IncomeFrequency frequencyType,
                                                   String employmentStartDate, String yearToDateIncomeThroughDate,
                                                   String baseIncomeAmount, String yearToDateBaseIncomeAmount, String verificationDate) {
        EVoeCurrentData evoeData = new EVoeCurrentData("evoe" + docCount());
        evoeData.setBorrowerFullName(borrowerName)
                .setBorrowerSSN(borrowerSSN)
                .setCollaboratorId(collaboratorId)
                .setEmployerName(employerName)
                .setPayRateFrequency(frequencyType)
                .setFrequency(frequencyType)
                .setOriginalHireDate(employmentStartDate)
                .setIncomeThruDate(yearToDateIncomeThroughDate)
                .setSignatureDate(verificationDate)
                .setCurrentGrossBasePayAmount(baseIncomeAmount)
                .setYtdBasePay(yearToDateBaseIncomeAmount);

        return evoeData;
    }

    public EVoePreviousData createCustomEvoePrevious(String borrowerName, String borrowerSSN, String collaboratorId, String employerName,
                                                     String employmentStartDate, String employmentEndDate,
                                                     String ytdBaseIncome, String ytdTotalIncome, String verificationDate) {
        EVoePreviousData evoeData = new EVoePreviousData("evoe" + docCount());
        evoeData.setBorrowerFullName(borrowerName)
                .setBorrowerSSN(borrowerSSN)
                .setCollaboratorId(collaboratorId)
                .setEmployerName(employerName)
                .setMostRecentStartDate(employmentStartDate)
                .setOriginalHireDate(employmentStartDate)
                .setEndDate(employmentEndDate)
                .setIncomeThruDate(verificationDate)
                .setSignatureDate(verificationDate)
                .setYtdBasePay(ytdBaseIncome)
                .setYtdTotal(ytdTotalIncome);

        return evoeData;
    }

    private int docCount() {
        return documentCount++;
    }

    public String[] getBorrowerIncomeData(Boolean primaryBorrower, Boolean currentJob) {
        String[] borrowerData = new String[THREE];
        borrowerData[ZERO] = primaryBorrower ? borrowerFullName : coBorrowerFullName;
        borrowerData[ONE] = primaryBorrower ? borrowerSSN : coBorrowerSSN;
        borrowerData[TWO] = primaryBorrower ? borrowerCollaboratorId : coBorrowerCollaboratorId;
        borrowerData[THREE] = primaryBorrower ?
                currentJob ? borrowerCurrentEmployment : borrowerPreviousEmployment :
                currentJob ? coBorrowerCurrentEmployment : coBorrowerPreviousEmployment;
        return borrowerData;
    }
}

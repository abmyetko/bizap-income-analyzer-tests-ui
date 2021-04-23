package com.capsilon.incomeanalyzer.automation.utilities.builders;

import com.capsilon.common.utils.fnmbuilder.FNMBuilder;
import com.capsilon.common.utils.mismo.structure.*;
import com.capsilon.common.utils.mismo.utils.MismoGenerator;
import com.capsilon.common.utils.mismo.wrapper.MismoWrapper;
import com.capsilon.incomeanalyzer.automation.rest.data.loan.refid.RestGetResponse;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestChangeRequests;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import com.capsilon.incomeanalyzer.automation.utilities.enums.LoanDocumentTypeOfIncome;
import com.capsilon.test.ui.Retry;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.capsilon.common.utils.mismo.utils.XMLDateWrapper.localDateTimeToMismoDate;
import static com.capsilon.incomeanalyzer.automation.rest.methods.RestUploadData.createLoanV2;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;
import static com.capsilon.incomeanalyzer.automation.utilities.DateUtilities.getDateFromSignDate;
import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.replaceDollarsAndCommas;

public class IAMismoBuilder extends IAFolderBuilder<IAMismoBuilder> {//NOSONAR

    private final String currentLoanDocumentFilePath;
    JAXBElement<MESSAGE> builder = new MismoGenerator().unMarshal();
    MismoWrapper mismoWrapper;

    public MismoWrapper getMismoWrapper() {
        if (mismoWrapper == null)
            generateMismoWrapper();
        return mismoWrapper;
    }

    public IAMismoBuilder generateMismoWrapper() {
        mismoWrapper = new MismoWrapper(builder);
        return this;
    }

    public IAMismoBuilder() {
        super(String.format(".\\target\\mismo-folder\\%s\\generatedMismoFile.xml", LocalDateTime.now().toLocalDate()));
        currentLoanDocumentFilePath = String.format("%s%s.xml", DEFAULT_FILEPATH.substring(0, DEFAULT_FILEPATH.lastIndexOf('.')), random.nextInt());
    }

    public IAMismoBuilder(String loanCaseNumber) {
        super(String.format(".\\target\\mismo-folder\\%s\\generatedMismoFile.xml", LocalDateTime.now().toLocalDate()));
        this.loanCaseNumber = loanCaseNumber;
        currentLoanDocumentFilePath = String.format("%s%s%s.xml", DEFAULT_FILEPATH.substring(0, DEFAULT_FILEPATH.lastIndexOf('.')), random.nextInt(), loanCaseNumber);
    }

    public IAMismoBuilder restBuild() {
        restBuild(currentLoanDocumentFilePath);
        return this;
    }

    @Override
    public IAMismoBuilder restBuild(String loanFilePath) {
        generateFolder(loanFilePath);
        return this;
    }

    public String uiBuild() {
        return uiBuild(currentLoanDocumentFilePath);
    }

    @Override
    public String uiBuild(String loanFilePath) {
        return generateFolder(loanFilePath);
    }

    protected String generateFolder(String loanFilePath) {
        File file = generatePhysicalDocument(loanFilePath);

        folderId = createLoanV2(file);

        Retry.tryRun(TIMEOUT_TWENTY_SECONDS, TIMEOUT_THREE_MINUTES, () -> RestGetLoanData.getApplicationData(getFolderId()));

        RestGetResponse rsp = RestGetLoanData.getApplicationData(getFolderId());
        borrowerCollaboratorId = rsp.getApplicant(getBorrowerFullName()).getRefId();
        if (rsp.getApplicants().size() > 1)
            coBorrowerCollaboratorId = rsp.getApplicant(getCoBorrowerFullName()).getRefId();

        return folderId;
    }

    public File generatePhysicalDocument() {
        return generatePhysicalDocument(currentLoanDocumentFilePath);
    }


    public File generatePhysicalDocument(String loanFilePath) {
        File filePath = new File(loanFilePath);
        if (filePath.getParentFile() != null) {
            filePath.getParentFile().mkdirs();
        }
        return new MismoGenerator().marshal(builder, loanFilePath);
    }

    public IAMismoBuilder generateLoanDocument() {
        builder = new MismoGenerator().unMarshal();
        setLoanDocumentDefaultValues();
        return this;
    }

    @Override
    public IAMismoBuilder generateSecondaryJobsLoanDocument() {
        builder = new MismoGenerator().unMarshal();
        setLoanDocumentDefaultValues();
        mismoWrapper.getDealSets()
                .addEmployer(0)
                .addEmployer(1);
        String borrowerCurrentStartDate = getDateFromSignDate(Integer.parseInt(borrowerYearsOnThisJob), Integer.parseInt(borrowerMonthsOnThisJob), Integer.parseInt(signDate.substring(3, 5)), signDate);
        String coBorrowerCurrentStartDate = getDateFromSignDate(Integer.parseInt(coBorrowerYearsOnThisJob), Integer.parseInt(coBorrowerMonthsOnThisJob), Integer.parseInt(signDate.substring(3, 5)), signDate);

        setBorrowerSecondaryEmployerStartDate(borrowerCurrentStartDate);
        setCoBorrowerSecondaryEmployerStartDate(coBorrowerCurrentStartDate);
        setBorrowerSecondaryEmployerName(borrowerSecondEmployment);
        setCoBorrowerSecondaryEmployerName(coBorrowerSecondEmployment);
        return this;
    }

    @Override
    protected IAMismoBuilder setLoanDocumentDefaultValues() {
        generateMismoWrapper();
        mismoWrapper.getDealSets()
                .addEmployer(0)
                .addEmployer(1);
        setBuilderSignDate(signDate);
        setBorrowerDefaultValues();
        setCoBorrowerDefaultValues();
        setBorrowerPreviousEmployerStatusType();
        setCoBorrowerPreviousEmployerStatusType();
        setBuilderLoanNumber(loanCaseNumber);
        setBuilderMortgageAppliedFor(mortgageAppliedFor.textName);

        return this;
    }

    public IAMismoBuilder setCoBorrowerDefaultValues() {
        String coBorrowerCurrentStartDate = getDateFromSignDate(Integer.parseInt(coBorrowerYearsOnThisJob), Integer.parseInt(coBorrowerMonthsOnThisJob), Integer.parseInt(signDate.substring(3, 5)), signDate);
        setBuilderCoBorrowerFirstName(coBorrowerFirstName);
        setBuilderCoBorrowerLastName(coBorrowerLastName);
        setBuilderCoBorrowerFullName(getCoBorrowerFullName());
        setBuilderCoBorrowerSSN(coBorrowerSSN.replace("-", ""));
        setCoBorrowerCurrentEmployerName(coBorrowerCurrentEmployment);
        setCoBorrowerPreviousEmployerName(coBorrowerPreviousEmployment);
        setCoBorrowerCurrentEmployerStartDate(coBorrowerCurrentStartDate);
        setCoBorrowerPreviousEmployerStartDate(coBorrowerPreviousJobStartDate);
        setCoBorrowerPreviousEmployerEndDate(coBorrowerPreviousJobEndDate);
        return this;
    }

    public IAMismoBuilder setBorrowerDefaultValues() {
        String borrowerCurrentStartDate = getDateFromSignDate(Integer.parseInt(borrowerYearsOnThisJob), Integer.parseInt(borrowerMonthsOnThisJob), Integer.parseInt(signDate.substring(3, 5)), signDate);
        setBuilderBorrowerFirstName(borrowerFirstName);
        setBuilderBorrowerLastName(borrowerLastName);
        setBuilderBorrowerFullName(getBorrowerFullName());
        setBuilderBorrowerSSN(borrowerSSN.replace("-", ""));
        setBorrowerCurrentEmployerName(borrowerCurrentEmployment);
        setBorrowerPreviousEmployerName(borrowerPreviousEmployment);
        setBorrowerCurrentEmployerStartDate(borrowerCurrentStartDate);
        setBorrowerPreviousEmployerStartDate(borrowerPreviousJobStartDate);
        setBorrowerPreviousEmployerEndDate(borrowerPreviousJobEndDate);
        return this;
    }

    @Override
    public IAMismoBuilder setPrimaryJobBasePay(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(mismoWrapper.getDealSets().getBorrowerBySSN(applicantSSN.replace("-", "")).getROLES().getROLE().get(0).getBORROWER().getEMPLOYERS().getEMPLOYER().get(0)
                        .getLEGALENTITY().getLEGALENTITYDETAIL().getFullName().getValue().getValue(),
                applicantSSN, LoanDocumentTypeOfIncome.BASE, value);
        return this;
    }

    @Override
    public IAMismoBuilder setPrimaryJobOvertime(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(mismoWrapper.getDealSets().getBorrowerBySSN(applicantSSN.replace("-", "")).getROLES().getROLE().get(0).getBORROWER().getEMPLOYERS().getEMPLOYER().get(0)
                        .getLEGALENTITY().getLEGALENTITYDETAIL().getFullName().getValue().getValue(),
                applicantSSN, LoanDocumentTypeOfIncome.OVERTIME, value);
        return this;
    }

    @Override
    public IAMismoBuilder setPrimaryJobCommissions(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(mismoWrapper.getDealSets().getBorrowerBySSN(applicantSSN.replace("-", "")).getROLES().getROLE().get(0).getBORROWER().getEMPLOYERS().getEMPLOYER().get(0)
                        .getLEGALENTITY().getLEGALENTITYDETAIL().getFullName().getValue().getValue(),
                applicantSSN, LoanDocumentTypeOfIncome.COMMISSIONS, value);
        return this;
    }

    @Override
    public IAMismoBuilder setPrimaryJobBonus(String applicantSSN, String value) {
        addLoanDocumentTypeOfIncome(mismoWrapper.getDealSets().getBorrowerBySSN(applicantSSN.replace("-", "")).getROLES().getROLE().get(0).getBORROWER().getEMPLOYERS().getEMPLOYER().get(0)
                        .getLEGALENTITY().getLEGALENTITYDETAIL().getFullName().getValue().getValue(),
                applicantSSN, LoanDocumentTypeOfIncome.BONUS, value);
        return this;
    }

    @Override
    public IAMismoBuilder generateLoanDocumentWithNoIncome() {
        builder = new MismoGenerator().unMarshal();
        setLoanDocumentDefaultValues();

        List<CURRENTINCOMEITEM> borrowerItemList = builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(0)
                .getROLES().getROLE().get(0).getBORROWER().getCURRENTINCOME().getCURRENTINCOMEITEMS().getCURRENTINCOMEITEM();
        List<CURRENTINCOMEITEM> coBorrowerItemList = builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(1)
                .getROLES().getROLE().get(0).getBORROWER().getCURRENTINCOME().getCURRENTINCOMEITEMS().getCURRENTINCOMEITEM();

        List<String> itemLabels = builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(0)
                .getROLES().getROLE().get(0).getBORROWER().getCURRENTINCOME().getCURRENTINCOMEITEMS().getCURRENTINCOMEITEM().stream().map(CURRENTINCOMEITEM::getLabel).collect(Collectors.toList());
        itemLabels.addAll(builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(1)
                .getROLES().getROLE().get(0).getBORROWER().getCURRENTINCOME().getCURRENTINCOMEITEMS().getCURRENTINCOMEITEM().stream().map(CURRENTINCOMEITEM::getLabel).collect(Collectors.toList()));
        List<RELATIONSHIP> relationships = builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getRELATIONSHIPS().getRELATIONSHIP().stream()
                .filter(relationship -> itemLabels.stream().anyMatch(itemLabel -> itemLabel.equals(relationship.getFrom()))).collect(Collectors.toList());
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getRELATIONSHIPS().getRELATIONSHIP().removeAll(relationships);
        borrowerItemList.removeAll(borrowerItemList);
        coBorrowerItemList.removeAll(coBorrowerItemList);
        return this;
    }

    @Override
    public IAMismoBuilder generateLoanDocumentWithOneApplicant() {
        builder = new MismoGenerator().unMarshal();
        setLoanDocumentDefaultValues();
        List<PARTY> parties = builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY();
        getAllRelationships().removeAll(findRelationshipsTo(parties.get(1).getROLES().getROLE().get(0).getLabel()));
        parties.remove(1);

        return this;
    }

    @Override
    public IAMismoBuilder addLoanDocumentTypeOfIncome(String applicantSSN, LoanDocumentTypeOfIncome typeOfIncome, String value) {
        addLoanDocumentTypeOfIncome(mismoWrapper.getDealSets().getBorrowerBySSN(applicantSSN.replace("-", "")).getROLES().getROLE().get(0).getBORROWER().getEMPLOYERS().getEMPLOYER()
                .get(0).getLEGALENTITY().getLEGALENTITYDETAIL().getFullName().getValue().getValue(), applicantSSN, typeOfIncome, value);
        return this;
    }

    @Override
    public IAMismoBuilder addLoanDocumentTypeOfIncome(String employer, String applicantSSN, LoanDocumentTypeOfIncome typeOfIncome, String value) {
        IncomeEnum incomeEnum = new IncomeEnum();
        incomeEnum.setValue(IncomeBase.fromValue(typeOfIncome.textName));
        int applicantIndex = mismoWrapper.getDealSets().getBorrowerBySSN(applicantSSN.replace("-", "")).getROLES().getROLE().get(0).getSequenceNumber() - 1;
        mismoWrapper.getDealSets().addCurrentIncomeItem(mismoWrapper.getDealSets().getEmployerByName(applicantIndex, employer).getLabel(),
                applicantIndex,
                incomeEnum,
                getMismoAmount(bigD(replaceDollarsAndCommas(value))));
        return this;
    }

    @Override
    public IAMismoBuilder addNewEmployment(String employment, String startDate, String endDate, Boolean isFirstApplicant, Boolean isCurrentEmployment, String monthlyIncome) {
        mismoWrapper.getDealSets().addEmployer(employment, startDate, endDate, isFirstApplicant, isCurrentEmployment, monthlyIncome);
        return this;
    }

    @Override
    public FNMBuilder getFnmBuilder() {
        throw new NullPointerException("There is no FNMBuilder in MismoGenerator.class");
    }

    @Override
    public JAXBElement<MESSAGE> getMismoBuilder() {
        return builder;
    }

    @Override
    public void uploadNewLoanDocument() {
        uploadNewLoanDocument(getFolderId());
    }

    public void uploadNewLoanDocument(String folderId) {
        File generator = new MismoGenerator().marshal(getMismoBuilder(), String.format("%sUPDATED%s.xml", DEFAULT_FILEPATH.substring(0, DEFAULT_FILEPATH.lastIndexOf('.')), random.nextInt()));
        RestChangeRequests.uploadNewMismo(folderId, generator);
    }

    public IAMismoBuilder setBuilderSignDate(String signDate) {
        LocalDateTime time = LocalDateTime.of(LocalDate.parse(signDate, DateTimeFormatter.ofPattern("MM/dd/yyyy")), LocalTime.now());
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getLOANS().getLOAN().get(0).getDOCUMENTSPECIFICDATASETS()
                .getDOCUMENTSPECIFICDATASET().get(0).getURLA().getURLADETAIL().getApplicationSignedByLoanOriginatorDate().setValue(localDateTimeToMismoDate(time));
        return this;
    }

    public IAMismoBuilder setBuilderLoanNumber(String loanNumber) {
        MISMOIdentifier identifier = new MISMOIdentifier();
        identifier.setValue(loanNumber);
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getLOANS().getLOAN().get(0).getLOANIDENTIFIERS().getLOANIDENTIFIER().get(0)
                .getLoanIdentifier().setValue(identifier);
        return this;
    }

    public IAMismoBuilder setBuilderMortgageAppliedFor(String mismoMortgageAppliedFor) {
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getLOANS().getLOAN().get(0)
                .getTERMSOFLOAN().getMortgageType().setValue(getMortgageEnum(mismoMortgageAppliedFor));

        return this;
    }

    public IAMismoBuilder setBorrowerCurrentEmployerStartDate(String signDate) {
        setEmployerStartDate(0, 0, signDate);
        return this;
    }

    public IAMismoBuilder setCoBorrowerCurrentEmployerStartDate(String signDate) {
        setEmployerStartDate(1, 0, signDate);
        return this;
    }

    public IAMismoBuilder setBorrowerPreviousEmployerStartDate(String signDate) {
        setEmployerStartDate(0, 1, signDate);
        return this;
    }

    public IAMismoBuilder setCoBorrowerPreviousEmployerStartDate(String signDate) {
        setEmployerStartDate(1, 1, signDate);
        return this;
    }

    public IAMismoBuilder setBorrowerSecondaryEmployerStartDate(String signDate) {
        setEmployerStartDate(0, 2, signDate);
        return this;
    }

    public IAMismoBuilder setCoBorrowerSecondaryEmployerStartDate(String signDate) {
        setEmployerStartDate(1, 2, signDate);
        return this;
    }

    public IAMismoBuilder setEmployerStartDate(int borrowerId, int employerId, String signDate) {
        if (signDate != null && !signDate.isEmpty()) {
            LocalDateTime time = LocalDateTime.of(LocalDate.parse(signDate, DateTimeFormatter.ofPattern("MM/dd/yyyy")), LocalTime.now());
            builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE().get(0)
                    .getBORROWER().getEMPLOYERS().getEMPLOYER().get(employerId).getEMPLOYMENT()
                    .getEmploymentStartDate().setValue(localDateTimeToMismoDate(time));
        } else {
            builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE().get(0)
                    .getBORROWER().getEMPLOYERS().getEMPLOYER().get(employerId).getEMPLOYMENT()
                    .setEmploymentStartDate(null);
        }
        return this;
    }

    public IAMismoBuilder setBorrowerPreviousEmployerEndDate(String signDate) {
        return setEmployerEndDate(0, 1, signDate);
    }

    public IAMismoBuilder setCoBorrowerPreviousEmployerEndDate(String signDate) {
        return setEmployerEndDate(1, 1, signDate);
    }

    public IAMismoBuilder setEmployerEndDate(int borrowerId, int employerId, String signDate) {
        if (signDate != null && !signDate.isEmpty()) {
            LocalDateTime time = LocalDateTime.of(LocalDate.parse(signDate, DateTimeFormatter.ofPattern("MM/dd/yyyy")), LocalTime.now());
            builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE().get(0)
                    .getBORROWER().getEMPLOYERS().getEMPLOYER().get(employerId).getEMPLOYMENT()
                    .setEmploymentEndDate(new JAXBElement<>(new QName("http://www.mismo.org/residential/2009/schemas", "EmploymentEndDate"), MISMODate.class, EMPLOYMENT.class,
                            localDateTimeToMismoDate(time)));
        } else {
            builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE().get(0)
                    .getBORROWER().getEMPLOYERS().getEMPLOYER().get(employerId).getEMPLOYMENT()
                    .setEmploymentEndDate(null);
        }
        return this;
    }

    public IAMismoBuilder setBorrowerPreviousEmployerStatusType() {
        return setPreviousEmployerStatusType(0, 1);
    }

    public IAMismoBuilder setCoBorrowerPreviousEmployerStatusType() {
        return setPreviousEmployerStatusType(1, 1);
    }

    public IAMismoBuilder setPreviousEmployerStatusType(Integer borrowerId, Integer employerId) {
        EmploymentStatusEnum classificationEnum = new EmploymentStatusEnum();
        classificationEnum.setValue(EmploymentStatusBase.PREVIOUS);
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE().get(0)
                .getBORROWER().getEMPLOYERS().getEMPLOYER().get(employerId).getEMPLOYMENT()
                .getEmploymentStatusType().setValue(classificationEnum);
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE().get(0)
                .getBORROWER().getEMPLOYERS().getEMPLOYER().get(employerId).getEMPLOYMENT()
                .setEmploymentClassificationType(null);
        return this;
    }

    public IAMismoBuilder setBorrowerCurrentEmployerName(String employerName) {
        setBuilderEmployerName(0, 0, employerName);
        return this;
    }

    public IAMismoBuilder setCoBorrowerCurrentEmployerName(String employerName) {
        setBuilderEmployerName(1, 0, employerName);
        return this;
    }

    public IAMismoBuilder setBorrowerPreviousEmployerName(String employerName) {
        setBuilderEmployerName(0, 1, employerName);
        return this;
    }

    public IAMismoBuilder setCoBorrowerPreviousEmployerName(String employerName) {
        setBuilderEmployerName(1, 1, employerName);
        return this;
    }

    public IAMismoBuilder setBorrowerSecondaryEmployerName(String employerName) {
        setBuilderEmployerName(0, 2, employerName);
        return this;
    }

    public IAMismoBuilder setCoBorrowerSecondaryEmployerName(String employerName) {
        setBuilderEmployerName(1, 2, employerName);
        return this;
    }

    public IAMismoBuilder setBuilderEmployerName(int borrowerId, int employerId, String employerName) {
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE().get(0)
                .getBORROWER().getEMPLOYERS().getEMPLOYER().get(employerId).getLEGALENTITY().getLEGALENTITYDETAIL().getFullName().setValue(getMismoString(employerName));
        return this;
    }

    public IAMismoBuilder setBuilderBorrowerSSN(String ssnValue) {
        return setBuilderSSN(0, ssnValue);
    }

    public IAMismoBuilder setBuilderCoBorrowerSSN(String ssnValue) {
        return setBuilderSSN(1, ssnValue);
    }

    public IAMismoBuilder setBuilderSSN(int borrowerId, String ssnValue) {
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getTAXPAYERIDENTIFIERS()
                .getTAXPAYERIDENTIFIER().get(0).getTaxpayerIdentifierValue().setValue(getMismoNumericString(ssnValue));
        return this;
    }

    public IAMismoBuilder setBuilderBorrowerFirstName(String firstName) {
        return setBuilderFirstName(0, firstName);
    }

    public IAMismoBuilder setBuilderCoBorrowerFirstName(String firstName) {
        return setBuilderFirstName(1, firstName);
    }

    public IAMismoBuilder setBuilderFirstName(int borrowerId, String firstName) {
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getINDIVIDUAL()
                .getNAME().getFirstName().setValue(getMismoString(firstName));
        return this;
    }

    public IAMismoBuilder setBuilderBorrowerLastName(String lastName) {
        return setBuilderLastName(0, lastName);
    }

    public IAMismoBuilder setBuilderCoBorrowerLastName(String lastName) {
        return setBuilderLastName(1, lastName);
    }

    public IAMismoBuilder setBuilderLastName(int borrowerId, String lastName) {
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getINDIVIDUAL()
                .getNAME().getLastName().setValue(getMismoString(lastName));
        return this;
    }

    public IAMismoBuilder setBuilderBorrowerFullName(String fullName) {
        return setBuilderFullName(0, fullName);
    }

    public IAMismoBuilder setBuilderCoBorrowerFullName(String fullName) {
        return setBuilderFullName(1, fullName);
    }

    public IAMismoBuilder setBuilderFullName(int borrowerId, String fullName) {
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getINDIVIDUAL()
                .getNAME().getFullName().setValue(getMismoString(fullName));
        return this;
    }

    private MISMOString getMismoString(String value) {
        MISMOString misString = new MISMOString();
        misString.setValue(value);
        return misString;
    }

    private MISMONumericString getMismoNumericString(String value) {
        MISMONumericString misString = new MISMONumericString();
        misString.setValue(value);
        return misString;
    }

    private MISMOAmount getMismoAmount(BigDecimal value) {
        MISMOAmount misString = new MISMOAmount();
        misString.setValue(value);
        return misString;
    }

    private MortgageEnum getMortgageEnum(String value) {
        MortgageEnum misString = new MortgageEnum();
        misString.setValue(MortgageBase.fromValue(value));
        return misString;
    }

    public String findRelationship(String label) {
        return Objects.requireNonNull(
                getAllRelationships().stream()
                        .filter(relationship -> label.equals(relationship.getFrom()))
                        .findFirst().orElseThrow(() -> new NullPointerException("Missing relationship with xlink:from = " + label)))
                .getTo();
    }

    public List<RELATIONSHIP> findRelationshipsTo(String label) {
        return Objects.requireNonNull(
                getAllRelationships().stream()
                        .filter(relationship -> label.equals(relationship.getTo())).collect(Collectors.toList()));
    }

    public List<RELATIONSHIP> getAllRelationships() {
        return Objects.requireNonNull(
                builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getRELATIONSHIPS().getRELATIONSHIP());
    }

    public EMPLOYER findEmployer(int borrowerId, String label) {
        return builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE()
                .get(0).getBORROWER().getEMPLOYERS().getEMPLOYER().stream()
                .filter(employer -> label.equals(employer.getLabel()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Missing employer with xlink:label = " + label));
    }

    public IAMismoBuilder setCurrentIncomeMonthlyTotalAmount(int borrowerId, IncomeBase incomeBase, EmploymentStatusBase employmentStatusBase, BigDecimal currentIncomeMonthlyTotalAmount) {
        builder.getValue().getDEALSETS().getDEALSET().get(0).getDEALS().getDEAL().get(0).getPARTIES().getPARTY().get(borrowerId).getROLES().getROLE()
                .get(0).getBORROWER().getCURRENTINCOME().getCURRENTINCOMEITEMS().getCURRENTINCOMEITEM().stream()
                .filter(it -> it.getCURRENTINCOMEITEMDETAIL().getIncomeType().getValue().getValue() == incomeBase)
                .filter(currentIncomeItem -> findEmployer(borrowerId, findRelationship(currentIncomeItem.getLabel()))
                        .getEMPLOYMENT().getEmploymentStatusType().getValue().getValue() == employmentStatusBase)
                .findFirst().orElseThrow(() -> new NullPointerException("Missing income details with income base: " + incomeBase.value() + " for " + employmentStatusBase.value() + " employment"))
                .getCURRENTINCOMEITEMDETAIL().getCurrentIncomeMonthlyTotalAmount().setValue(getMismoAmount(currentIncomeMonthlyTotalAmount));
        return this;
    }

    public IAMismoBuilder setBorrowerIncomeVerified(Boolean flag){
        mismoWrapper.getDealSets().setBorrowerIsVerified(0, flag);
        return this;
    }

    public IAMismoBuilder setCoBorrowerIncomeVerified(Boolean flag){
        mismoWrapper.getDealSets().setBorrowerIsVerified(1, flag);
        return this;
    }
}
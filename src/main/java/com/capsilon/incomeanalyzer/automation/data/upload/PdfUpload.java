package com.capsilon.incomeanalyzer.automation.data.upload;

import com.capsilon.automation.dv.helpers.AsyncDVClient;
import com.capsilon.incomeanalyzer.automation.data.upload.data.*;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.katalyst.business.objects.EFolder;
import com.capsilon.test.commons.utils.pdfbuilder.PdfBuilder;
import com.capsilon.test.commons.utils.pdfbuilder.dto.*;
import com.capsilon.test.ui.Retry;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.common.endpoint.FlamsEndpoint.getDvFolderFriendlyId;
import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.*;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;

@SuppressWarnings("squid:S1200")
public class PdfUpload extends DataUploadObject<PdfUpload> {

    private final EFolder dvFolder;

    public PdfUpload(IAFolderBuilder iafolderBuilder, AsyncDVClient dvFolderClient) {
        super(iafolderBuilder, dvFolderClient);

        dvFolder = dvFolderClient.findByFriendlyId(getDvFolderFriendlyId(folderId));
        dvFolderClient.updateFolder(dvFolder);
    }

    @Override
    public PdfUpload importDocument(DocumentData document, String folderId) {
        return importDocument(document, folderId, STATUS_CREATED);
    }

    public PdfUpload importDocument(DocumentData document, String folderId, int statusCode) {
        String[] documents = {createPdfDocument(document)};
        uploadDocAndWaitForIngestion(documents);
        return this;
    }

    public PdfUpload importDocumentList(Gson gsonConfig) {
        importDocumentList(gsonConfig, folderId);
        return this;
    }

    public PdfUpload importDocumentList(Gson gsonConfig, String folderId) {
        List<String> documents = new ArrayList<>();
        documentList.forEach(document -> documents.add(createPdfDocument(document)));
        String[] hue = new String[documents.size()];
        documents.toArray(hue);
        uploadDocAndWaitForIngestion(hue);
        return this;
    }

    public PdfUpload removeDocumentsFromFolder() {
        if (!dvFolderClient.getAllDocumentsIds(dvFolder.getId()).isEmpty()) {
            dvFolderClient.withdrawAllDocuments(dvFolder.getId());
        }
        return this;
    }

    public String createPdfDocument(DocumentData document) {
        String docsD;
        PdfBuilder builder = new PdfBuilder();
        if (PaystubData.class.equals(document.getClass())) {
            docsD = builder.generatePaystub(createPdfPaystub((PaystubData) document)).getAbsolutePath();
        } else if (W2Data.class.equals(document.getClass())) {
            docsD = builder.generateW2(createPdfW2((W2Data) document)).getAbsolutePath();
        } else if (VoeCurrentData.class.equals(document.getClass())) {
            docsD = builder.generateVOE(createPdfVoeCurrent((VoeCurrentData) document)).getAbsolutePath();
        } else if (VoePreviousData.class.equals(document.getClass())) {
            docsD = builder.generateVOE(createPdfVoePrevious((VoePreviousData) document)).getAbsolutePath();
        } else if (VoeFullData.class.equals(document.getClass())) {
            docsD = builder.generateVOE(createPdfVoeAll((VoeFullData) document)).getAbsolutePath();
        } else if (EVoeCurrentData.class.equals(document.getClass())) {
            docsD = builder.generateEVOE(createPdfEVoeCurrent((EVoeCurrentData) document)).getAbsolutePath();
        } else {
            docsD = builder.generateEVOE(createPdfEVoePrevious((EVoePreviousData) document)).getAbsolutePath();
        }
        return docsD;
    }

    @SuppressWarnings({"squid:S109", "squid:S128", "squid:MethodCyclomaticComplexity"})
    private PaystubBase createPdfPaystub(PaystubData paystubData) {
        if (paystubData.getIncomes().size() <= THREE) {
            Paystub paystub = new Paystub();
            List<Paystub.Earning> earningList = new ArrayList<>();
            paystubData.getIncomes().forEach(income -> earningList.add(new Paystub.Earning()
                    .setType(income.getType())
                    .setRate(income.getRate())
                    .setHours(income.getHours())
                    .setThisPeriod(income.getPeriodAmount())
                    .setYearToDate(income.getYearToDateAmount())));
            switch (earningList.size()) {
                case 3:
                    paystub.setEarningAttendBonus(earningList.get(2));
                case 2:
                    paystub.setEarningCommissions(earningList.get(1));
                case 1:
                    paystub.setEarningRegular(earningList.get(0));
                    break;
                default:
                    break;
            }
            paystub.setEmployeeAddress1(paystubData.getBorrowerAddress1())
                    .setEmployeeAddress2(paystubData.getBorrowerAddress2())
                    .setEmployerAddress1(paystubData.getEmployerAddress1())
                    .setEmployerAddress2(paystubData.getEmployerAddress2())
//                    .setEarningsSummaryThisPeriod("2419.20")
//                    .setEarningsSummaryThisPeriodNet("1855.65")
//                    .setEarningsSummaryYearToDateNet("16376.40")
                    .setEarningsSummaryYearToDate(paystubData.getYtdGrossIncomeAmount())
                    .setPayDate(paystubData.getPayDate())
                    .setPeriodStartDate(paystubData.getStartDate())
                    .setPeriodEndingDate(paystubData.getEndDate())
                    .setEmployeeName(paystubData.getBorrowerFullName())
                    .setEmployeeSocialSecurityNumber(paystubData.getBorrowerSSN())
                    .setEmployerName(paystubData.getEmployerName());
            return paystub;
        } else {
            PaystubBig paystub = new PaystubBig();
            List<Paystub.Earning> earningList = new ArrayList<>();
            paystubData.getIncomes().forEach(income -> earningList.add(new Paystub.Earning()
                    .setType(income.getType())
                    .setRate(income.getRate())
                    .setHours(income.getHours())
                    .setThisPeriod(income.getPeriodAmount())
                    .setYearToDate(income.getYearToDateAmount())));
            switch (earningList.size()) {
                case 6:
                    paystub.setEarning6(earningList.get(5));
                case 5:
                    paystub.setEarning5(earningList.get(4));
                case 4:
                    paystub.setEarning4(earningList.get(3));
                case 3:
                    paystub.setEarning3(earningList.get(2));
                case 2:
                    paystub.setEarning2(earningList.get(1));
                case 1:
                    paystub.setEarning1(earningList.get(0));
                    break;
                default:
                    break;
            }
            paystub.setEmployeeAddress1(paystubData.getBorrowerAddress1())
                    .setEmployeeAddress2(paystubData.getBorrowerAddress2())
                    .setEmployerAddress1(paystubData.getEmployerAddress1())
                    .setEmployerAddress2(paystubData.getEmployerAddress2())
//                    .setEarningsSummaryThisPeriod("2419.20")
//                    .setEarningsSummaryThisPeriodNet("1855.65")
//                    .setEarningsSummaryYearToDateNet("16376.40")
                    .setEarningsSummaryYearToDate(paystubData.getYtdGrossIncomeAmount())
                    .setPayDate(paystubData.getPayDate())
                    .setPeriodStartDate(paystubData.getStartDate())
                    .setPeriodEndingDate(paystubData.getEndDate())
                    .setEmployeeName(paystubData.getBorrowerFullName())
                    .setEmployeeSocialSecurityNumber(paystubData.getBorrowerSSN())
                    .setEmployerName(paystubData.getEmployerName());
            return paystub;
        }
    }

    private W2Base createPdfW2(W2Data w2Data) {
        W2 w2 = new W2();
        w2.setF99Year(w2Data.getYear())
                .setFa1EmployeesSocialSecurityNumber(w2Data.getBorrowerSSN())
                .setFc1EmployerAddressLine(w2Data.getEmployerName())
                .setFc2EmployerAddressLine(w2Data.getEmployerAddress1())
                .setFc3EmployerAddressLine(w2Data.getEmployerAddress2())
                .setF01WagesTipsOtherCompensation(w2Data.getWagesTipsOtherCompensation())
                .setF03SocialSecurityWages(w2Data.getSocialSecurityWages())
                .setF05MedicareWages(w2Data.getMedicareWages())
                .setF07SocialSecurityTips(w2Data.getSocialSecurityTips())
                .setF02FederalIncomeTax(w2Data.getFederalIncomeTax())
                .setF04SocialSecurityTax(w2Data.getSocialSecurityTax())
                .setF06MedicareTax(w2Data.getMedicareTax())
                .setF08AllocatedTips(w2Data.getAllocatedTips())
                .setF16StateWagesTipsEtc(w2Data.getStateWagesTipsEtc())
                .setF17StateIncomeTax(w2Data.getStateIncomeTax())
                .setFe1EmployeesAddressLine(w2Data.getBorrowerFullName())
                .setFe2EmployeesAddressLine(w2Data.getBorrowerAddress1())
                .setFe3EmployeesAddressLine(w2Data.getBorrowerAddress2());
        return w2;
    }

    @SuppressWarnings("squid:S138")
    private FannieMaeVOE createPdfVoeCurrent(VoeCurrentData voeCurrentData) {
        Integer yearToDate = Integer.parseInt(voeCurrentData.getYtdIncomeThruDate()
                .substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END));
        FannieMaeVOE.EmployerAuthorizedSignature signature = new FannieMaeVOE.EmployerAuthorizedSignature();
        signature.setF27L1Title(voeCurrentData.getSignatureTitle())
                .setF26L1Signature(voeCurrentData.getSignatureSignature())
                .setF28L1SignatureDate(voeCurrentData.getSignatureDate())
                .setF29L1Name(voeCurrentData.getSignatureName())
                .setF30L1PhoneNo(voeCurrentData.getSignaturePhoneNo());

        FannieMaeVOE.GrossEarningsYearToDate earningsYearToDate = new FannieMaeVOE.GrossEarningsYearToDate();
        earningsYearToDate.setF12d1ThruDate(voeCurrentData.getYtdIncomeThruDate())
                .setF1202BasePay(voeCurrentData.getYtdBasePay())
                .setF1203Overtime(voeCurrentData.getYtdOvertime())
                .setF1204Commission(voeCurrentData.getYtdCommission())
                .setF1205Bonus(voeCurrentData.getYtdBonus())
                .setF1206Total(voeCurrentData.getYtdTotal());

        FannieMaeVOE.GrossEarningsPastYear1 earningsPastYear1 = new FannieMaeVOE.GrossEarningsPastYear1();
        earningsPastYear1.setF12y1Year(Integer.toString(yearToDate - ONE))
                .setF1207BasePay(voeCurrentData.getPriorYearBasePay())
                .setF1208Overtime(voeCurrentData.getPriorYearOvertime())
                .setF1209Commission(voeCurrentData.getPriorYearCommission())
                .setF1210Bonus(voeCurrentData.getPriorYearBonus())
                .setF1211Total(voeCurrentData.getPriorYearTotal());

        FannieMaeVOE.GrossEarningsPastYear2 earningsPastYear2 = new FannieMaeVOE.GrossEarningsPastYear2();
        earningsPastYear2.setF12y2Year(Integer.toString(yearToDate - TWO))
                .setF1212BasePay(voeCurrentData.getTwoYearPriorBasePay())
                .setF1213Overtime(voeCurrentData.getTwoYearPriorOvertime())
                .setF1214Commission(voeCurrentData.getTwoYearPriorCommission())
                .setF1215Bonus(voeCurrentData.getTwoYearPriorBonus())
                .setF1216Total(voeCurrentData.getTwoYearPriorTotal());

        FannieMaeVOE.Request request = new FannieMaeVOE.Request();
        request.setF01L1EmployerAddress(voeCurrentData.getEmployerName())
                .setF01L2EmployerAddress(voeCurrentData.getEmployerAddress1())
                .setF01L3EmployerAddress(voeCurrentData.getEmployerAddress2())
                .setF02L1LenderAddress(voeCurrentData.getLenderName())
                .setF02L2LenderAddress(voeCurrentData.getLenderAddress1())
                .setF02L3LenderAddress(voeCurrentData.getLenderAddress2())
                .setF04L1LenderTitle(voeCurrentData.getLenderTitle())
                .setF06L1LendersNumber(voeCurrentData.getLenderNumber())
                .setF05L1LenderSignatureDate(voeCurrentData.getLenderSignatureDate())
                .setF03L1LenderSignature(voeCurrentData.getLenderSignature())
                .setF07L1ApplicantAddress(voeCurrentData.getBorrowerFullName())
                .setF07L2ApplicantAddress(voeCurrentData.getBorrowerAddress1())
                .setF07L3ApplicantAddress(voeCurrentData.getBorrowerAddress2())
                .setF08L1ApplicantSignature(voeCurrentData.getBorrowerSignature());

        FannieMaeVOE.PresentEmployment presentEmployment = new FannieMaeVOE.PresentEmployment();
        presentEmployment.setF09L1EmploymentDate(voeCurrentData.getEmploymentStartDate())
                .setF10L1PresentPosition(voeCurrentData.getPresentPosition())
                .setF11L1ProbabilityOfContinuedEmployment(voeCurrentData.getProbabilityOfContinuousEmployment())
                .setF1201CurrentGrossBasePayAmount(voeCurrentData.getCurrentGrossBasePayAmount())
                .setF15L1AverageHoursPerWeek(voeCurrentData.getAvgHoursPerWeek())
                .setPastYear1(earningsPastYear1)
                .setPastYear2(earningsPastYear2)
                .setYearToDate(earningsYearToDate);

        switch (voeCurrentData.getFrequency()) {
            case HOURLY:
                presentEmployment.setF12C5Hourly("X");
                break;
            case WEEKLY:
                presentEmployment.setF12C4Weekly("X");
                break;
            case MONTHLY:
                presentEmployment.setF12C3Monthly("X");
                break;
            case ANNUALLY:
                presentEmployment.setF12C2Annual("X");
                break;
            case BI_WEEKLY:
            case SEMI_MONTHLY:
            case QUARTERLY:
            case SEMI_ANNUALLY:
                presentEmployment.setF12C6Other("X")
                        .setF12A7OtherSpecify(voeCurrentData.getFrequency().text);
                break;
            default:
                break;
        }

        FannieMaeVOE voe = new FannieMaeVOE();
        voe.setRequest(request)
                .setAuthorizedSignature(signature)
                .setPresentEmployment(presentEmployment);
        return voe;
    }

    private FannieMaeVOE createPdfVoePrevious(VoePreviousData voePreviousData) {
        FannieMaeVOE.EmployerAuthorizedSignature signature = new FannieMaeVOE.EmployerAuthorizedSignature();
        signature.setF27L1Title(voePreviousData.getSignatureTitle())
                .setF26L1Signature(voePreviousData.getSignatureSignature())
                .setF28L1SignatureDate(voePreviousData.getSignatureDate())
                .setF29L1Name(voePreviousData.getSignatureName())
                .setF30L1PhoneNo(voePreviousData.getSignaturePhoneNo());

        FannieMaeVOE.Request request = new FannieMaeVOE.Request();
        request.setF01L1EmployerAddress(voePreviousData.getEmployerName())
                .setF01L2EmployerAddress(voePreviousData.getEmployerAddress1())
                .setF01L3EmployerAddress(voePreviousData.getEmployerAddress2())
                .setF02L1LenderAddress(voePreviousData.getLenderName())
                .setF02L2LenderAddress(voePreviousData.getLenderAddress1())
                .setF02L3LenderAddress(voePreviousData.getLenderAddress2())
                .setF04L1LenderTitle(voePreviousData.getLenderTitle())
                .setF06L1LendersNumber(voePreviousData.getLenderNumber())
                .setF05L1LenderSignatureDate(voePreviousData.getLenderSignatureDate())
                .setF03L1LenderSignature(voePreviousData.getLenderSignature())
                .setF07L1ApplicantAddress(voePreviousData.getBorrowerFullName())
                .setF07L2ApplicantAddress(voePreviousData.getBorrowerAddress1())
                .setF07L3ApplicantAddress(voePreviousData.getBorrowerAddress2())
                .setF08L1ApplicantSignature(voePreviousData.getBorrowerSignature());

        FannieMaeVOE.PreviousEmployment previousEmployment = new FannieMaeVOE.PreviousEmployment();
        previousEmployment.setF21L1HiredDate(voePreviousData.getHiredDate())
                .setF22L1TerminatedDate(voePreviousData.getTerminatedDate())
                .setF23V1BaseWageAmount(voePreviousData.getBaseWageAmount() + " " + voePreviousData.getPeriodType().text)
                .setF23V2OvertimeWageAmount(voePreviousData.getOvertimeWageAmount() + " " + voePreviousData.getPeriodType().text)
                .setF23V3CommissionWageAmount(voePreviousData.getCommissionWageAmount() + " " + voePreviousData.getPeriodType().text)
                .setF23V4BonusWageAmount(voePreviousData.getBonusWageAmount() + " " + voePreviousData.getPeriodType().text)
                .setF24L1ReasonForLeaving(voePreviousData.getReasonForLeaving())
                .setF25L1PositionHeld(voePreviousData.getPositionHeld());

        FannieMaeVOE voe = new FannieMaeVOE();
        voe.setRequest(request);
        voe.setAuthorizedSignature(signature);
        voe.setPreviousEmployment(previousEmployment);
        return voe;
    }

    private FannieMaeVOE createPdfVoeAll(VoeFullData voeFullData) {
        Integer yearToDate = Integer.parseInt(voeFullData.getYtdIncomeThruDate()
                .substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END));
        FannieMaeVOE.EmployerAuthorizedSignature signature = new FannieMaeVOE.EmployerAuthorizedSignature();
        signature.setF27L1Title(voeFullData.getSignatureTitle())
                .setF26L1Signature(voeFullData.getSignatureSignature())
                .setF28L1SignatureDate(voeFullData.getSignatureDate())
                .setF29L1Name(voeFullData.getSignatureName())
                .setF30L1PhoneNo(voeFullData.getSignaturePhoneNo());

        FannieMaeVOE.GrossEarningsYearToDate earningsYearToDate = new FannieMaeVOE.GrossEarningsYearToDate();
        earningsYearToDate.setF12d1ThruDate(voeFullData.getYtdIncomeThruDate())
                .setF1202BasePay(voeFullData.getYtdBasePay())
                .setF1203Overtime(voeFullData.getYtdOvertime())
                .setF1204Commission(voeFullData.getYtdCommission())
                .setF1205Bonus(voeFullData.getYtdBonus())
                .setF1206Total(voeFullData.getYtdTotal());

        FannieMaeVOE.GrossEarningsPastYear1 earningsPastYear1 = new FannieMaeVOE.GrossEarningsPastYear1();
        earningsPastYear1.setF12y1Year(Integer.toString(yearToDate - ONE))
                .setF1207BasePay(voeFullData.getPriorYearBasePay())
                .setF1208Overtime(voeFullData.getPriorYearOvertime())
                .setF1209Commission(voeFullData.getPriorYearCommission())
                .setF1210Bonus(voeFullData.getPriorYearBonus())
                .setF1211Total(voeFullData.getPriorYearTotal());

        FannieMaeVOE.GrossEarningsPastYear2 earningsPastYear2 = new FannieMaeVOE.GrossEarningsPastYear2();
        earningsPastYear2.setF12y2Year(Integer.toString(yearToDate - TWO))
                .setF1212BasePay(voeFullData.getTwoYearPriorBasePay())
                .setF1213Overtime(voeFullData.getTwoYearPriorOvertime())
                .setF1214Commission(voeFullData.getTwoYearPriorCommission())
                .setF1215Bonus(voeFullData.getTwoYearPriorBonus())
                .setF1216Total(voeFullData.getTwoYearPriorTotal());

        FannieMaeVOE.Request request = new FannieMaeVOE.Request();
        request.setF01L1EmployerAddress(voeFullData.getEmployerName())
                .setF01L2EmployerAddress(voeFullData.getEmployerAddress1())
                .setF01L3EmployerAddress(voeFullData.getEmployerAddress2())
                .setF02L1LenderAddress(voeFullData.getLenderName())
                .setF02L2LenderAddress(voeFullData.getLenderAddress1())
                .setF02L3LenderAddress(voeFullData.getLenderAddress2())
                .setF04L1LenderTitle(voeFullData.getLenderTitle())
                .setF06L1LendersNumber(voeFullData.getLenderNumber())
                .setF05L1LenderSignatureDate(voeFullData.getLenderSignatureDate())
                .setF03L1LenderSignature(voeFullData.getLenderSignature())
                .setF07L1ApplicantAddress(voeFullData.getBorrowerFullName())
                .setF07L2ApplicantAddress(voeFullData.getBorrowerAddress1())
                .setF07L3ApplicantAddress(voeFullData.getBorrowerAddress2())
                .setF08L1ApplicantSignature(voeFullData.getBorrowerSignature());

        FannieMaeVOE.PresentEmployment presentEmployment = new FannieMaeVOE.PresentEmployment();
        presentEmployment.setF09L1EmploymentDate(voeFullData.getEmploymentStartDate())
                .setF10L1PresentPosition(voeFullData.getPresentPosition())
                .setF11L1ProbabilityOfContinuedEmployment(voeFullData.getProbabilityOfContinuousEmployment())
                .setF1201CurrentGrossBasePayAmount(voeFullData.getCurrentGrossBasePayAmount())
                .setF15L1AverageHoursPerWeek(voeFullData.getAvgHoursPerWeek())
                .setPastYear1(earningsPastYear1)
                .setPastYear2(earningsPastYear2)
                .setYearToDate(earningsYearToDate);

        switch (voeFullData.getFrequency()) {
            case HOURLY:
                presentEmployment.setF12C5Hourly("X");
                break;
            case WEEKLY:
                presentEmployment.setF12C4Weekly("X");
                break;
            case MONTHLY:
                presentEmployment.setF12C3Monthly("X");
                break;
            case ANNUALLY:
                presentEmployment.setF12C2Annual("X");
                break;
            case BI_WEEKLY:
            case SEMI_MONTHLY:
            case QUARTERLY:
            case SEMI_ANNUALLY:
                presentEmployment.setF12C6Other("X")
                        .setF12A7OtherSpecify(voeFullData.getFrequency().text);
                break;
            default:
                break;
        }

        FannieMaeVOE.PreviousEmployment previousEmployment = new FannieMaeVOE.PreviousEmployment();
        previousEmployment.setF21L1HiredDate(voeFullData.getHiredDate())
                .setF22L1TerminatedDate(voeFullData.getTerminatedDate())
                .setF23V1BaseWageAmount(voeFullData.getBaseWageAmount() + " " + voeFullData.getPeriodType().text)
                .setF23V2OvertimeWageAmount(voeFullData.getOvertimeWageAmount() + " " + voeFullData.getPeriodType().text)
                .setF23V3CommissionWageAmount(voeFullData.getCommissionWageAmount() + " " + voeFullData.getPeriodType().text)
                .setF23V4BonusWageAmount(voeFullData.getBonusWageAmount() + " " + voeFullData.getPeriodType().text)
                .setF24L1ReasonForLeaving(voeFullData.getReasonForLeaving())
                .setF25L1PositionHeld(voeFullData.getPositionHeld());

        FannieMaeVOE voe = new FannieMaeVOE();
        voe.setRequest(request)
                .setAuthorizedSignature(signature)
                .setPresentEmployment(presentEmployment)
                .setPreviousEmployment(previousEmployment);
        return voe;
    }

    private EVOEBase createPdfEVoeCurrent(EVoeCurrentData eVoeCurrentData) {
        Integer yearToDate = Integer.parseInt(eVoeCurrentData.getIncomeThruDate()
                .substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END));

        EVOEEquifaxVerificationTwoPages.OrderDetails orderInformation = new EVOEEquifaxVerificationTwoPages.OrderDetails();
        orderInformation.setVerifiedOn(eVoeCurrentData.getVerificationDate())
                .setVerificationType(eVoeCurrentData.getVerificationType())
                .setPermissiblePurpose(eVoeCurrentData.getPermissiblePurpose())
                .setReferenceNumber(eVoeCurrentData.getReferenceNumber())
                .setTrackingNumber(eVoeCurrentData.getTrackingNumber())
                .setSignDate(eVoeCurrentData.getSignDate());

        EVOEEquifaxVerificationTwoPages.Employer employer = new EVOEEquifaxVerificationTwoPages.Employer();
        employer.setName(eVoeCurrentData.getEmployerName())
                .setHeadquartersAddress(eVoeCurrentData.getHeadquartersAddress())
                .setAddress1(eVoeCurrentData.getEmployerAddress1())
                .setAddress2(eVoeCurrentData.getEmployerAddress2())
                .setCity(eVoeCurrentData.getCity())
                .setState(eVoeCurrentData.getState())
                .setZipCode(eVoeCurrentData.getZipCode())
                .setDisclaimer(eVoeCurrentData.getDisclaimer());

        EVOEEquifaxVerificationTwoPages.Employment employment = new EVOEEquifaxVerificationTwoPages.Employment();
        employment.setDivision(eVoeCurrentData.getDivision())
                .setEmploymentStatus(eVoeCurrentData.getEmploymentStatus())
                .setMostRecentStartDate(eVoeCurrentData.getMostRecentStartDate())
                .setOriginalHireDate(eVoeCurrentData.getOriginalHireDate())
                .setTimeOnJob(eVoeCurrentData.getTimeOnJob())
                .setJobTitle(eVoeCurrentData.getJobTitle());

        EVOEEquifaxVerificationTwoPages.Income income = new EVOEEquifaxVerificationTwoPages.Income();
        income.setRate(eVoeCurrentData.getRate())
                .setFrequency(eVoeCurrentData.getPayRateFrequency().text)
                .setHours(eVoeCurrentData.getHours())
                .setPayFrequency(eVoeCurrentData.getPayFrequency().text)
                .setLastAmountOfPayIncrease(eVoeCurrentData.getLastAmountOfPayIncrease())
                .setNextAmountOfPayIncrease(eVoeCurrentData.getNextAmountOfPayIncrease());

        EVOEEquifaxVerificationTwoPages.Earning earning1 = new EVOEEquifaxVerificationTwoPages.Earning();
        EVOEEquifaxVerificationTwoPages.Earning earning2 = new EVOEEquifaxVerificationTwoPages.Earning();
        EVOEEquifaxVerificationTwoPages.Earning earning3 = new EVOEEquifaxVerificationTwoPages.Earning();
        earning1.setYear(yearToDate.toString())
                .setBasePay(eVoeCurrentData.getYtdBasePay())
                .setOvertime(eVoeCurrentData.getYtdOvertime())
                .setCommissions(eVoeCurrentData.getYtdCommission())
                .setBonus(eVoeCurrentData.getYtdBonus())
                .setOther(eVoeCurrentData.getYtdOther())
                .setTotalIncome(eVoeCurrentData.getYtdTotal());

        earning2.setYear(Integer.toString(yearToDate - ONE))
                .setBasePay(eVoeCurrentData.getPriorYearBasePay())
                .setOvertime(eVoeCurrentData.getPriorYearOvertime())
                .setCommissions(eVoeCurrentData.getPriorYearCommission())
                .setBonus(eVoeCurrentData.getPriorYearBonus())
                .setOther(eVoeCurrentData.getPriorYearOther())
                .setTotalIncome(eVoeCurrentData.getPriorYearTotal());

        earning3.setYear(Integer.toString(yearToDate - TWO))
                .setBasePay(eVoeCurrentData.getTwoYearPriorBasePay())
                .setOvertime(eVoeCurrentData.getTwoYearPriorOvertime())
                .setCommissions(eVoeCurrentData.getTwoYearPriorCommission())
                .setBonus(eVoeCurrentData.getTwoYearPriorBonus())
                .setOther(eVoeCurrentData.getTwoYearPriorOther())
                .setTotalIncome(eVoeCurrentData.getTwoYearPriorTotal());

        income.setIncomeOne(earning1);
        income.setIncomeTwo(earning2);
        income.setIncomeThree(earning3);

        EVOEEquifaxVerificationTwoPages.Applicant applicant = new EVOEEquifaxVerificationTwoPages.Applicant();
        applicant.setEmployerName(eVoeCurrentData.getEmployerName())
                .setEmploymentStartDate(eVoeCurrentData.getMostRecentStartDate())
                .setEmploymentStatus(eVoeCurrentData.getEmploymentStatus())
                .setName(eVoeCurrentData.getBorrowerFullName())
                .setNameBig(eVoeCurrentData.getBorrowerFullName())
                .setSsn(eVoeCurrentData.getBorrowerSSN());

        EVOEEquifaxVerificationTwoPages evoe = new EVOEEquifaxVerificationTwoPages();
        evoe.setOrderInformation(orderInformation)
                .setEmployer(employer)
                .setEmployment(employment)
                .setIncome(income)
                .setApplicant(applicant);
        return evoe;
    }

    private EVOEBase createPdfEVoePrevious(EVoePreviousData eVoePreviousData) {
        Integer yearToDate = Integer.parseInt(eVoePreviousData.getIncomeThruDate()
                .substring(YEAR_SUBSTRING_INDEX_START, YEAR_SUBSTRING_INDEX_END));

        EVOECreditPlus.OrderDetails orderDetails = new EVOECreditPlus.OrderDetails();
        orderDetails.setOrderNumber("123456")
                .setOrderedBy("Not Implemented Inc")
                .setDateOrdered(eVoePreviousData.getVerificationDate())
                .setOrderType("Employment plus Income")
                .setVendorReferenceNumber("123456789")
                .setReferenceNumber(eVoePreviousData.getReferenceNumber())
                .setApplicantName(eVoePreviousData.getBorrowerFullName())
                .setEmploymentFilters("Previous Employers")
                .setDateLastUpdated(eVoePreviousData.getVerificationDate());

        EVOECreditPlus.Employment employmentDetails = new EVOECreditPlus.Employment();
        employmentDetails.setVerificationType(eVoePreviousData.getVerificationType())
                .setPermissiblePurpose(eVoePreviousData.getPermissiblePurpose())
                .setInformationCurrentAsOf(eVoePreviousData.getVerificationDate())
                .setName(eVoePreviousData.getEmployerName())
                .setAddress1(eVoePreviousData.getEmployerAddress1())
                .setAddress2(eVoePreviousData.getEmployerAddress2())
                .setDisclaimer(eVoePreviousData.getDisclaimer());

        EVOECreditPlus.Applicant employeeDetails = new EVOECreditPlus.Applicant();
        employeeDetails.setDivisionNumber("1234")
                .setName(eVoePreviousData.getBorrowerFullName())
                .setSsn(eVoePreviousData.getBorrowerSSN())
                .setEmploymentStatus("No Longer Employed")
                .setMostRecentStartDate(eVoePreviousData.getMostRecentStartDate())
                .setEmploymentEndDate(eVoePreviousData.getEndDate())
                .setOriginalHireDate(eVoePreviousData.getOriginalHireDate())
                .setTimeOnJob(eVoePreviousData.getTimeOnJob())
                .setJobTitle(eVoePreviousData.getJobTitle())
                .setRate(eVoePreviousData.getRate() + " " + eVoePreviousData.getPayRateFrequency())
                .setHours(eVoePreviousData.getHours());

        EVOECreditPlus.Earning earning1 = new EVOECreditPlus.Earning();
        EVOECreditPlus.Earning earning2 = new EVOECreditPlus.Earning();
        EVOECreditPlus.Earning earning3 = new EVOECreditPlus.Earning();
        earning1.setYear(yearToDate.toString())
                .setBasePay(eVoePreviousData.getYtdBasePay())
                .setOvertime(eVoePreviousData.getYtdOvertime())
                .setCommission(eVoePreviousData.getYtdCommission())
                .setBonus(eVoePreviousData.getYtdBonus())
                .setOtherIncome(eVoePreviousData.getYtdOther())
                .setTotalPay(eVoePreviousData.getYtdTotal());

        earning2.setYear(Integer.toString(yearToDate - ONE))
                .setBasePay(eVoePreviousData.getPriorYearBasePay())
                .setOvertime(eVoePreviousData.getPriorYearOvertime())
                .setCommission(eVoePreviousData.getPriorYearCommission())
                .setBonus(eVoePreviousData.getPriorYearBonus())
                .setOtherIncome(eVoePreviousData.getPriorYearOther())
                .setTotalPay(eVoePreviousData.getPriorYearTotal());

        earning3.setYear(Integer.toString(yearToDate - TWO))
                .setBasePay(eVoePreviousData.getTwoYearPriorBasePay())
                .setOvertime(eVoePreviousData.getTwoYearPriorOvertime())
                .setCommission(eVoePreviousData.getTwoYearPriorCommission())
                .setBonus(eVoePreviousData.getTwoYearPriorBonus())
                .setOtherIncome(eVoePreviousData.getTwoYearPriorOther())
                .setTotalPay(eVoePreviousData.getTwoYearPriorTotal());


        EVOECreditPlus.Income incomeDetails = new EVOECreditPlus.Income();
        incomeDetails.setIncomeOne(earning1);
        incomeDetails.setIncomeTwo(earning2);
        incomeDetails.setIncomeThree(earning3);

        EVOECreditPlus evoe = new EVOECreditPlus();
        evoe.setOrderDetails(orderDetails)
                .setApplicant(employeeDetails)
                .setEmployment(employmentDetails)
                .setIncome(incomeDetails);
        return evoe;
    }

    private void uploadDocAndWaitForIngestion(String[] docs) {
        dvFolderClient.uploadDocumentsToFolder(dvFolder.getId(), docs);

        Retry.whileTrue(TIMEOUT_TEN_SECONDS, TIMEOUT_TEN_MINUTES, () -> {
                    if (dvFolderClient.isAnyUncompletedWorkItem(dvFolder.getId()))
                        dvFolderClient.completeWorkItem(dvFolder.getId());
                    return !dvFolderClient.areAllDocumentsIngested(dvFolder.getId());
                },
                "Failed to ingest documents to MDM!");
    }

}

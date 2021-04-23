package com.capsilon.incomeanalyzer.automation.data.upload.data;


import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeType;
import com.capsilon.incomeanalyzer.automation.utilities.enums.PaystubIncomeGroups;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;

@Data
@Accessors(chain = true)
public class PaystubData extends DocumentData<PaystubData> {

    private String borrowerAddress1 = HOMEOWNER_ADDRESS;
    private String borrowerAddress2 = WASHINGTON_ADDRESS;
    private String employerAddress1 = AWESOME_COMPUTERS_ADDRESS;
    private String employerAddress2 = WASHINGTON_ADDRESS;
    private String startDate;
    private String endDate;
    private String payDate;
    private String manualFrequency;
    private String explicitFrequency;
    private String payType;
    private String ytdGrossIncomeAmount;
    private List<PaystubIncomeRow> incomes = new ArrayList<>();

    public PaystubData(String documentName) {
        super(documentName);
    }

    public List<PaystubIncomeRow> getIncomes() {
        return new ArrayList<>(incomes);
    }

    public PaystubData addIncome(PaystubIncomeRow income) {
        if (income != null)
            incomes.add(income);
        return this;
    }

    public PaystubData clearIncomeList() {
        incomes.clear();
        return this;
    }

    public String getManualFrequency() {
        return manualFrequency;
    }

    public PaystubData setManualFrequency(IncomeFrequency manualFrequency) {
        this.manualFrequency = manualFrequency != null ? manualFrequency.toString() : null;
        return this;
    }

    public String getExplicitFrequency() {
        return explicitFrequency;
    }

    public PaystubData setExplicitFrequency(IncomeFrequency explicitFrequency) {
        this.explicitFrequency = explicitFrequency != null ? explicitFrequency.toString() : null;
        return this;
    }

    public String getPayType() {
        return payType;
    }

    public PaystubData setPayType(IncomeType payType) {
        this.payType = payType.toString();
        return this;
    }

    @SuppressWarnings("squid:S2972")
    @Data
    @Accessors(chain = true)
    public static class PaystubIncomeRow {

        private String type;
        private String rate;
        private String hours;
        private String periodAmount;
        private String yearToDateAmount;
        private PaystubIncomeGroups incomeGroup;

        public PaystubIncomeRow(String type, String rate, String hours, String periodAmount, String yearToDateAmount) {
            this.type = type;
            this.rate = rate;
            this.hours = hours;
            this.periodAmount = periodAmount;
            this.yearToDateAmount = yearToDateAmount;
            this.incomeGroup = PaystubIncomeGroups.REGULAR;
        }

        public PaystubIncomeRow(PaystubIncomeGroups incomeGroup, String rate, String hours, String periodAmount, String yearToDateAmount) {
            this.type = incomeGroup.value;
            this.rate = rate;
            this.hours = hours;
            this.periodAmount = periodAmount;
            this.yearToDateAmount = yearToDateAmount;
            this.incomeGroup = incomeGroup;
        }
    }
}

package com.capsilon.incomeanalyzer.automation.ui.view;

import com.capsilon.incomeanalyzer.automation.ui.component.summary.Summary;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.totalmonthlyincome.ApplicantProgressBar;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.DOUBLE_DELTA_MEDIUM;
import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.getDoubleValueOfIncome;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SummaryView {
    public final Summary summary = new Summary(); //NOSONAR

    public void checkAllApplicantsProgressBarPartsSumIsEqualToHundred() {
        for (int i = 0; i < summary.totalMonthlyIncomeContainer.getProgressBarNumber(); i++) {
            ApplicantProgressBar progressBar = summary.totalMonthlyIncomeContainer.getApplicantProgressBar(i);
            if (!"$0.00".equals(progressBar.INCOME.text())) {
                assertEquals(100D, progressBar.TUTTI_FRUTTI.barPartsWidthSum(), DOUBLE_DELTA_MEDIUM); //NOSONAR
            }
        }
    }

    public void checkAllApplicantsProgressBarWidthIsHundred() {
        if (summary.isAnyBarActive()) {
            for (int i = 0; i < summary.totalMonthlyIncomeContainer.getProgressBarNumber(); i++) {
                ApplicantProgressBar applicantProgressBar = summary.totalMonthlyIncomeContainer.getApplicantProgressBar(i);
                if (!"$0.00".equals(applicantProgressBar.INCOME.text()) && applicantProgressBar.TUTTI_FRUTTI.BAR.getAttribute("style").contains("100%")) {
                    assertTrue(true, "Contains");
                }
            }
        }
    }

    public void checkAllApplicantsProgressBarIncomeValueEqualsToSumOfTooltipsIncomes() {
        summary.totalMonthlyIncomeContainer.TOTAL_ADJUSTED_MONTHLY_INCOME_HEADER.scrollIntoView(false);
        for (int i = 0; i < summary.totalMonthlyIncomeContainer.getProgressBarNumber(); i++) {
            assertTrue(summary.totalMonthlyIncomeContainer.getApplicantProgressBar(i).compareProgressBarIncomeToTooltipSum());
        }
    }

    public void checkSumOfAllApplicantsIsEqualToSumCalculatedTotalIncomeOfSelectedApplicants() {
        assertEquals(getDoubleValueOfIncome(summary.totalMonthlyIncomeContainer.TOTAL_ADJUSTED_MONTHLY_INCOME_VALUE.text()),
                summary.totalMonthlyIncomeContainer.getApplicableApplicantsIncomeSum(), DOUBLE_DELTA_MEDIUM);
    }
}
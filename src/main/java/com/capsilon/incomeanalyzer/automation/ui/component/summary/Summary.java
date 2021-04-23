package com.capsilon.incomeanalyzer.automation.ui.component.summary;

import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.ui.component.alert.ApplicantAlerts;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.summary.ApplicantAccordion;
import com.capsilon.incomeanalyzer.automation.ui.component.summary.totalmonthlyincome.TotalMonthlyIncome;
import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static com.codeborne.selenide.CollectionCondition.sizeLessThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class Summary {

    public final SelenideElement TAB_BUTTON = $("#summary-nav-button > span");
    public final SelenideElement COMPONENT_CONTAINER = $("ia-summary-page");
    private final ElementsCollection applicantAccordionTabs = COMPONENT_CONTAINER.$$x(".//ia-applicant-incomes");
    public final TotalMonthlyIncome totalMonthlyIncomeContainer = new TotalMonthlyIncome(COMPONENT_CONTAINER.$x(".//mat-card")); //NOSONAR
    private final ElementsCollection BARS_WRAPPERS_EMPTY = COMPONENT_CONTAINER.$$x(".//div[@class='bars-wrapper empty']");
    private final ElementsCollection BARS_WRAPPERS = COMPONENT_CONTAINER.$$x(".//div[contains(@class,'bars-wrapper')]");

    public int getApplicantAccordionTabCount() {
        return applicantAccordionTabs.size();
    }

    public void goToSummaryTab() {
        TAB_BUTTON.scrollIntoView(true).click();
        TAB_BUTTON.$x("./parent::a").shouldHave(attributeContains("class", "active"));
        Retry.tryRun(() -> IncomeAnalyzerPage.tabsWithoutSelected(TAB_BUTTON.$x("./parent::a").getAttribute("id")).shouldHave(sizeLessThan(1)));
    }

    public Summary shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        TAB_BUTTON.shouldBe(visible);
        totalMonthlyIncomeContainer.shouldBeDisplayed();
        return this;
    }

    public ApplicantAccordion getApplicantAccordion(int index) {
        return new ApplicantAccordion(COMPONENT_CONTAINER.$$x(".//ia-applicant-incomes/ia-foldable/mat-expansion-panel").get(index));
    }

    public void checkApplicantsAccordionIncomesEqualToPartsSum() {
        ApplicantAccordion appAccord;
        expandAccordions();
        for (int i = 0; i < getApplicantAccordionTabCount(); i++) {
            appAccord = getApplicantAccordion(i);
            appAccord.checkIfSumOfIncomeTypesEqualsToApplicantIncome();
        }
    }

    public boolean isAnyBarActive() {
        return BARS_WRAPPERS_EMPTY.size() != BARS_WRAPPERS.size();
    }

    public void checkApplicantsIncomePartsNames() {
        ApplicantAccordion appAccord;
        for (int i = 0; i < getApplicantAccordionTabCount(); i++) {
            appAccord = getApplicantAccordion(i);
            for (int j = 0; j < appAccord.getIncomeTypes().getIncomeTypesNumber(); j++) {
                appAccord.getIncomeTypes().getIncomeById(j).checkIncomeParts();
            }
        }
    }

    public void expandAccordions() {
        ApplicantAccordion appAccord;
        for (int i = 0; i < getApplicantAccordionTabCount(); i++) {
            appAccord = getApplicantAccordion(i);
            appAccord.getPanel().expandAccordion();
            for (int j = 0; j < appAccord.getIncomeTypes().getIncomeTypesNumber(); j++) {
                appAccord.getIncomeTypes().getIncomeById(j).getPanel().expandAccordion();
            }
        }
    }

    public void collapseAccordions() {
        ApplicantAccordion appAccord;
        for (int i = 0; i < getApplicantAccordionTabCount(); i++) {
            appAccord = getApplicantAccordion(i);
            for (int j = 0; j < appAccord.getIncomeTypes().getIncomeTypesNumber(); j++) {
                appAccord.getIncomeTypes().getIncomeById(j).getPanel().collapseAccordion();
            }
            appAccord.getPanel().collapseAccordion();
        }
    }

    public ApplicantAlerts getAlerts() {
        return new ApplicantAlerts(COMPONENT_CONTAINER.$x(".//div[contains(@class,'alerts')]"));
    }
}

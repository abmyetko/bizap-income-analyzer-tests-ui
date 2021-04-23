package com.capsilon.incomeanalyzer.automation.ui.component.applicant;

import com.capsilon.incomeanalyzer.automation.ui.component.restore.defaults.RestoreDefaults;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.CheckBox;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static com.codeborne.selenide.Selenide.$x;

public class ApplicantIncomeCategoryHeader {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement CATEGORY_NAME;
    public final SelenideElement CATEGORY_TOTAL_INCOME;
    public final CheckBox CHECKBOX;
    public final SelenideElement RESTORE_DEFAULTS_BUTTON;
    public final SelenideElement PRIMARY_EMPLOYMENT_TAB;
    public final SelenideElement SECONDARY_EMPLOYMENT_TAB_1;
    public final SelenideElement SECONDARY_EMPLOYMENT_TAB_2;

    public ApplicantIncomeCategoryHeader(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        CATEGORY_NAME = componentContainer.$x("./h2");
        CATEGORY_TOTAL_INCOME = componentContainer.$x("./div[@class='category-total']");
        CHECKBOX = new CheckBox(componentContainer.$x("./mat-checkbox"));
        RESTORE_DEFAULTS_BUTTON = componentContainer.$x("./ia-restore-defaults-button/button");
        PRIMARY_EMPLOYMENT_TAB = componentContainer.$x("./ia-employment-tabs/div/div[1]");
        SECONDARY_EMPLOYMENT_TAB_1 = componentContainer.$x("./ia-employment-tabs/div/div[2]");
        SECONDARY_EMPLOYMENT_TAB_2 = componentContainer.$x("./ia-employment-tabs/div/div[3]");
    }

    public ApplicantIncomeCategoryHeader scrollIntoView(boolean alignToTop) {
        COMPONENT_CONTAINER.scrollIntoView(alignToTop);
        return this;
    }

    public ApplicantIncomeCategoryHeader scrollIntoView(String scrollIntoViewOptions) {
        COMPONENT_CONTAINER.scrollIntoView(scrollIntoViewOptions);
        return this;
    }

    public ApplicantIncomeCategoryHeader scrollIntoViewCenter() {
        COMPONENT_CONTAINER.scrollIntoView("{block: \"center\"}");
        return this;
    }

    public RestoreDefaults restoreDefaults() {
        RESTORE_DEFAULTS_BUTTON.shouldNotHave(attributeContains("disabled", "true")).scrollIntoView(true).click();
        return new RestoreDefaults();
    }
}

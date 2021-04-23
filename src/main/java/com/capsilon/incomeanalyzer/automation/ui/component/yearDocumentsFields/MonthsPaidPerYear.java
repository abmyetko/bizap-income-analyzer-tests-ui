package com.capsilon.incomeanalyzer.automation.ui.component.yearDocumentsFields;

import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;

public class MonthsPaidPerYear {

    public final SelenideElement MONTHS_PAID_BUTTON;
    public final SelenideElement SEARCH_BOX;
    public final ElementsCollection DROPDOWN_FIELDS;
    public final ElementsCollection DROPDOWN_OPTIONS;
    public final SelenideElement LABEL;
    public final SelenideElement FIELD_OVERLAY;
    public final SelenideElement SELECTED_LABEL;

    public MonthsPaidPerYear(SelenideElement componentContainer) {
        MONTHS_PAID_BUTTON = componentContainer;
        DROPDOWN_FIELDS = Selenide.$$x("//div[contains(@class,'mat-select-panel')]");
        DROPDOWN_OPTIONS = DROPDOWN_FIELDS.get(1).$$x(".//mat-option[@role='option']");
        FIELD_OVERLAY = MONTHS_PAID_BUTTON.$x(".//div[contains(@class,'field-overlay')]");
        SEARCH_BOX = DROPDOWN_FIELDS.get(0).$x(".//input[@placeholder='Search']");
        SELECTED_LABEL = MONTHS_PAID_BUTTON.$x(".//span[contains(@class,'mat-select-value-text')]");
        LABEL = MONTHS_PAID_BUTTON.$x(".//mat-select");
    }

    public void selectFromDropdown(String value) {
        DROPDOWN_OPTIONS.find(Condition.exactText(value)).click();
    }

    public SelenideElement getItemFromListByValue(String value) {
        Retry.whileTrue(TIMEOUT_FORTY_SECONDS, ()->{
            openDropdown();
            return !DROPDOWN_OPTIONS.find(Condition.exactText(value)).exists();
            }  ,"There is no such Option");
        return DROPDOWN_OPTIONS.find(Condition.exactText(value));
    }

    public SelenideElement getArrowIcon() {
        return MONTHS_PAID_BUTTON.$x(".//div[contains(@class,'mat-select-arrow')]");
    }

    public void selectedValueShouldBe(String value) {
        LABEL.shouldBe(Condition.exactText(value));
    }

    public void searchUsingSearchBox(String value) {
        SEARCH_BOX.sendKeys(value);
    }

    public MonthsPaidPerYear changeDropDownVisibility() {
        if (!DROPDOWN_OPTIONS.get(0).exists()) {
            MONTHS_PAID_BUTTON.click();
        } else {
            selectFromDropdown(SELECTED_LABEL.getText());
        }
        return this;
    }

    public String getCursorStateOnButton() {
        return FIELD_OVERLAY.getCssValue("cursor");
    }

    public void openDropdown(){
        if (!DROPDOWN_OPTIONS.get(0).exists()) {
            MONTHS_PAID_BUTTON.click();
        }
    }

}

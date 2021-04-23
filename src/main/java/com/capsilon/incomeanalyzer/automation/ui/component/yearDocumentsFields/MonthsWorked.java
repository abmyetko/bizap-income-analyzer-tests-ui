package com.capsilon.incomeanalyzer.automation.ui.component.yearDocumentsFields;

import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.*;

public class MonthsWorked {

    public final SelenideElement MONTHS_WORKED_BUTTON;
    public final ElementsCollection DROPDOWN_FIELDS;
    public final SelenideElement SEARCH_BOX;
    public final SelenideElement LABEL;
    public final SelenideElement ERROR_LABEL;
    public final SelenideElement DEFAULT_MONTH_WORKED_VALUE;


    public MonthsWorked(SelenideElement componentContainer) {
        MONTHS_WORKED_BUTTON = componentContainer;
        DROPDOWN_FIELDS = Selenide.$$x("//div[contains(@class,'mat-select-panel')]");
        SEARCH_BOX = DROPDOWN_FIELDS.get(0).$x(".//input");
        LABEL = MONTHS_WORKED_BUTTON.$x(".//mat-select");
        ERROR_LABEL = DROPDOWN_FIELDS.get(1).$x(".//div[contains(@class, 'custom-field-error')]");
        DEFAULT_MONTH_WORKED_VALUE = DROPDOWN_FIELDS.get(0).$x(".//span[contains(@class, 'mat-option-text')]");
    }

    public MonthsWorked setCustomValue(String value) {
        if (!DROPDOWN_FIELDS.get(0).exists()) {
            MONTHS_WORKED_BUTTON.click();
        }
        Retry.whileTrue(TIMEOUT_FIVE_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            SEARCH_BOX.click();
            SEARCH_BOX.setValue(value);
            return !value.equals(SEARCH_BOX.getValue());
        }, "Value could not be set in Months worked input box");
        Retry.whileTrue(TIMEOUT_TWO_SECONDS, TIMEOUT_TWENTY_SECONDS, () -> {
            if (SEARCH_BOX.exists()) {
                SEARCH_BOX.pressEnter();
                return true;
            } else {
                return false;
            }
        }, "Months worked input box could not be closed");
        return this;
    }

    public MonthsWorked selectedValueShouldBe(String value) {
        LABEL.shouldBe(Condition.exactText(value));
        return this;
    }

    public MonthsWorked openDropdown(){
        if (!DROPDOWN_FIELDS.get(0).exists()) {
            MONTHS_WORKED_BUTTON.click();
        }
        return this;
    }
}

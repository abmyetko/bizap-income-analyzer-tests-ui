package com.capsilon.incomeanalyzer.automation.ui.component.summary.summary;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Condition.visible;

public class Parts {

    public final HashMap<String, String> incomePartsMap; //NOSONAR
    private final SelenideElement COMPONENT_CONTAINER;
    private final ElementsCollection INCOME_PARTS;

    public Parts(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer; //NOSONAR
        INCOME_PARTS = COMPONENT_CONTAINER.$$x(".//li");
        incomePartsMap = (HashMap<String, String>) getIncomeParts(); //NOSONAR
    }

    public Parts shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        INCOME_PARTS.get(0).shouldBe(visible);
        return this;
    }

    public Map<String, String> getIncomeParts() {
        HashMap<String, String> hashMap = new HashMap<>();
        for (SelenideElement part : INCOME_PARTS) {
            hashMap.put(part.$x(".//span[1]").text(), part.$x(".//span[2]").text());
        }
        return hashMap;
    }
}

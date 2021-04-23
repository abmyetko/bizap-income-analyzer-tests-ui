package com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.evidence;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public class EvidenceDetails {

    public final SelenideElement HEADER;
    public final SelenideElement ATTACH_DOCUMENT_BUTTON;
    private final int evidenceNumber;
    private final SelenideElement COMPONENT_CONTAINER;
    private final ElementsCollection PROPERTIES;

    public EvidenceDetails(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        HEADER = componentContainer.$x(".//div[@class='box hd val']");
        PROPERTIES = componentContainer.$$x(".//div[@class='box val']");
        evidenceNumber = componentContainer.$$x(".//ia-document-list/div").size();
        ATTACH_DOCUMENT_BUTTON = componentContainer.$x(".//button[contains(@class,'link-document')]");
    }

    public int getEvidenceItemNumber() {
        return evidenceNumber;
    }

    public EvidenceItem getEvidenceItemById(int index) {
        return new EvidenceItem(COMPONENT_CONTAINER.$$x(".//ia-document-list/div").get(index));
    }

    public String getPropertyValueById(int index) {
        return PROPERTIES.get(index).text().trim();
    }
}

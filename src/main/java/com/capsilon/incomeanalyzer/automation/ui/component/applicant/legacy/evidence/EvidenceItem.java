package com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.evidence;

import com.codeborne.selenide.SelenideElement;

public class EvidenceItem {

    public final SelenideElement NAME;
    public final SelenideElement WHO_ATTACHED;
    public final SelenideElement YEAR;
    public final SelenideElement MORE;
    private final SelenideElement COMPONENT_CONTAINER; //NOSONAR

    public EvidenceItem(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer; //NOSONAR
        NAME = componentContainer.$x("./div[1]");
        WHO_ATTACHED = componentContainer.$x("./div[2]");
        YEAR = componentContainer.$x("./div[3]");
        MORE = componentContainer.$x(".//i");
    }
}

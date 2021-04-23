package com.capsilon.incomeanalyzer.automation.ui.component.alert;

import com.codeborne.selenide.SelenideElement;

public class ApplicantAlert {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement APPLICANT_NAME;
    public final SelenideElement ALERT_INFO_ICON;
    public final SelenideElement ALERT_TEXT;

    public ApplicantAlert(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        APPLICANT_NAME = componentContainer.$x(".//p[@class='applicant']");
        ALERT_INFO_ICON = componentContainer.$x(".//svg-icon[@class='icon']");
        ALERT_TEXT = componentContainer.$x(".//div[contains(@class,'text')]");
    }
}

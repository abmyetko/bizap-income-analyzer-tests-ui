package com.capsilon.incomeanalyzer.automation.ui.component.alert;

import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

public class ApplicantAlerts {

    public final SelenideElement COMPONENT_CONTAINER;
    public final ElementsCollection ALERTS_COLLECTION;

    public ApplicantAlerts(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        ALERTS_COLLECTION = COMPONENT_CONTAINER.$$x(".//div[contains(@class,'error-label')]");
    }

    public int getNumberOfAlerts() {
        if (COMPONENT_CONTAINER.$x(".//div[contains(@class,'error-label')]").exists())
            return ALERTS_COLLECTION.size();
        else
            return 0;
    }

    public ApplicantAlert getAlertById(int id) {
        return new ApplicantAlert(ALERTS_COLLECTION.get(id));
    }

    public ApplicantAlerts waitForGivenNumberOfAlerts(int expectedNumberOfAlerts) {
        return waitForGivenNumberOfAlerts((int) Configuration.timeout, expectedNumberOfAlerts);
    }

    public ApplicantAlerts waitForGivenNumberOfAlerts(int timeout, int expectedNumberOfAlerts) {
        Retry.whileTrue(timeout, () -> getNumberOfAlerts() != expectedNumberOfAlerts, "Failed to get correct number of alerts!");
        return this;
    }

    public List<ApplicantAlert> getAlertsList() {
        List<ApplicantAlert> collection = new ArrayList<>();
        for (SelenideElement alert : ALERTS_COLLECTION) {
            collection.add(new ApplicantAlert(alert));
        }
        return collection;
    }
}
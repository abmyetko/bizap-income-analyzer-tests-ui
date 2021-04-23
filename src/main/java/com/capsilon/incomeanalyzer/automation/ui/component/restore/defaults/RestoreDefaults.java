package com.capsilon.incomeanalyzer.automation.ui.component.restore.defaults;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class RestoreDefaults {

    public final SelenideElement COMPONENT_CONTAINER = $("mat-dialog-container.mat-dialog-container");
    public final SelenideElement HEADER = COMPONENT_CONTAINER.$("header");
    public final SelenideElement RESTORE_MESSAGE = COMPONENT_CONTAINER.$("mat-dialog-content");
    public final SelenideElement BUTTON_RESTORE = COMPONENT_CONTAINER.$("button.restore-button");
    public final SelenideElement BUTTON_CANCEL = COMPONENT_CONTAINER.$("button.cancel-button");
}

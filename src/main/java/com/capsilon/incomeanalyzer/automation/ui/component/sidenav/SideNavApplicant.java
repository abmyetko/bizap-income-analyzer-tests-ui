package com.capsilon.incomeanalyzer.automation.ui.component.sidenav;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;

public class SideNavApplicant {

    public final CheckBox checkBox;
    public final SelenideElement tag;
    private final SelenideElement COMPONENT_CONTAINER;

    public SideNavApplicant(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        checkBox = new CheckBox(componentContainer.$x(".//mat-checkbox"), componentContainer.$("span.name"));
        tag = componentContainer.$x("./div[@class='tag ng-star-inserted']");
    }

    public SideNavApplicant shouldBeDisplayed() {
        COMPONENT_CONTAINER.shouldBe(visible);
        checkBox.shouldBeDisplayed();
        return this;
    }

    public SideNavApplicant checkApplicantCheckbox() {
        checkBox.setCheckboxValue(true);
        return this;
    }

    public SideNavApplicant unCheckApplicantCheckbox() {
        checkBox.setCheckboxValue(false);
        return this;
    }

    public SideNavApplicant shouldBeDisabled() {
        checkBox.checkIfCheckboxIsDisabled();
        return this;
    }
}

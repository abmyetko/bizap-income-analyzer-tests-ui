package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.$x;

public class EmploymentHeader {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement NAME;
    public final ElementsCollection TAGS;
    public final SnippetComponent DATE_RANGE;

    public static final SelenideElement aliasTooltipComponent = $x("//body//mat-tooltip-component");

    public EmploymentHeader(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        NAME = COMPONENT_CONTAINER.$x(".//h4");
        TAGS = COMPONENT_CONTAINER.$$x(".//ia-tags/div");
        DATE_RANGE = new SnippetComponent(COMPONENT_CONTAINER.$x(".//ia-snippet"));
    }

    public List<String> getAliases() {
        String nameText = NAME.getText();
        List<String> aliases = new ArrayList<>();
        aliases.add(nameText.substring(nameText.indexOf('[') + 1, nameText.indexOf(']')));

        NAME.scrollIntoView("{block: \"center\"}");
        new Actions(NAME.getWrappedDriver()).moveToElement(NAME.getWrappedElement()).perform();
        aliases.addAll(Arrays.asList(aliasTooltipComponent.getText().split("\n")));

        return aliases;
    }
}

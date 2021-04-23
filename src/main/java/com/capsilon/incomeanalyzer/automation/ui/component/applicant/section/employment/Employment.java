package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment;

import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomePartType;
import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

public class Employment {

    public final SelenideElement COMPONENT_CONTAINER;
    public final EmploymentHeader HEADER;
    public final EmploymentBody BODY;

    public Employment(SelenideElement componentContainer, IncomePartType incomePartType) {
        COMPONENT_CONTAINER = componentContainer;
        HEADER = new EmploymentHeader(COMPONENT_CONTAINER.$x("./ia-employer"));
        BODY = new EmploymentBody(COMPONENT_CONTAINER.$x("./ia-employment-" + incomePartType.toString().replace('_', '-').toLowerCase(Locale.US)));
    }
}

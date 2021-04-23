package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year;

import com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document.YearDocument;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.CheckBox;
import com.capsilon.incomeanalyzer.automation.utilities.enums.SummaryDocumentType;
import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

public class EmploymentYear {

    public final SelenideElement COMPONENT_CONTAINER;
    public final CheckBox CHECKBOX;
    public final SelenideElement HEADER;
    public final String DOCUMENT_LOCATOR = ".//ia-employment-annual-summary[translate(@data-testid, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='%s']";

    public EmploymentYear(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        HEADER = COMPONENT_CONTAINER.$x(".//div[@header='']");
        CHECKBOX = new CheckBox(HEADER.$x(".//mat-checkbox"));
    }

    public YearDocument document(SummaryDocumentType documentType) {
        return new YearDocument(COMPONENT_CONTAINER.$x(String.format(DOCUMENT_LOCATOR,
                documentType.toString().toLowerCase(Locale.US))));
    }
}
package com.capsilon.incomeanalyzer.automation.ui.component.applicant.section.employment.year.document;

import com.capsilon.incomeanalyzer.automation.ui.component.yearDocumentsFields.MonthsPaidPerYear;
import com.capsilon.incomeanalyzer.automation.ui.component.yearDocumentsFields.MonthsWorked;
import com.capsilon.incomeanalyzer.automation.ui.component.yearDocumentsFields.PayFrequency;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;

import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.trimStringAfterChar;

public class YearDocument {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement DOCUMENT_NAME;
    public final ElementsCollection VALUE_FIELDS;
    public final ElementsCollection SNIPPET_FIELDS;
    public final ElementsCollection RADIO_BUTTON_FIELDS;
    public final ElementsCollection ALL_FIELDS;
    public final SelenideElement MONTHS_PAID_BUTTON;
    private final DocumentsTooltip DOCUMENTS_TOOLTIP;
    private final SelenideElement MONTHS_WORKED_BUTTON;
    private final SelenideElement PAY_FREQUENCY_BUTTON;
    private final String MONTHS_PAID_DATA_TEST = "Months Paid Per Year";
    private final String MONTHS_WORKED_DATA_TEST = "Months Worked";
    private final String PAY_FREQUENCY_DATA_TEST = "Pay Frequency";

    public YearDocument(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer;
        DOCUMENT_NAME = COMPONENT_CONTAINER.$x(".//ia-cell-with-content[@data-testid='Document Name']/ia-documents-tooltip/div");
        DOCUMENTS_TOOLTIP = new DocumentsTooltip(COMPONENT_CONTAINER.$("ia-documents-tooltip popper-content"));
        SNIPPET_FIELDS = COMPONENT_CONTAINER.$$x(".//ia-snippet/parent::ia-cell-with-content");
        RADIO_BUTTON_FIELDS = COMPONENT_CONTAINER.$$x(".//mat-radio-button/parent::ia-cell-with-content");
        VALUE_FIELDS = COMPONENT_CONTAINER.$$x(".//ia-cell-with-content[not(@data-testid='Document Name') and " +
                "not(descendant::mat-radio-button) and " +
                "not(descendant::ia-snippet)]");
        ALL_FIELDS = COMPONENT_CONTAINER.$$x(".//ia-cell-with-content");
        MONTHS_PAID_BUTTON = COMPONENT_CONTAINER.$x(String.format(".//ia-cell-with-content[@data-testid='%s']", MONTHS_PAID_DATA_TEST));
        MONTHS_WORKED_BUTTON = COMPONENT_CONTAINER.$x(String.format(".//ia-cell-with-content[@data-testid='%s']", MONTHS_WORKED_DATA_TEST));
        PAY_FREQUENCY_BUTTON = COMPONENT_CONTAINER.$x(String.format(".//ia-cell-with-content[@data-testid='%s']", PAY_FREQUENCY_DATA_TEST));
    }

    public DocumentsTooltip documentsTooltip() {
        DOCUMENT_NAME.click();
        return DOCUMENTS_TOOLTIP;
    }

    public Map<String, String> getDocumentValuesMap() {
        return getDocumentValuesMap(ALL_FIELDS);
    }

    private Map<String, String> getDocumentValuesMap(ElementsCollection collection) {
        HashMap<String, String> mapCollection = new HashMap<>();
        collection.forEach(field -> {
            if (MONTHS_PAID_DATA_TEST.equals(field.getAttribute("data-testid")) && !field.findAll(By.xpath(".//ia-field-overlay")).isEmpty()) {
                mapCollection.put(field.getAttribute("data-testid"), trimStringAfterChar(field.$x(".//ia-field-overlay").getText(), '\n'));
            } else {
                mapCollection.put(field.getAttribute("data-testid"), trimStringAfterChar(field.getText(), '\n'));
            }
        });
        return mapCollection;
    }

    public Map<String, SelenideElement> getDocumentLocatorsMap() {
        return getDocumentLocatorsMap(ALL_FIELDS);
    }

    public Map<String, SelenideElement> getDocumentLocatorsMap(ElementsCollection collection) {
        HashMap<String, SelenideElement> mapCollection = new HashMap<>();
        collection.forEach(field -> mapCollection.put(field.getAttribute("data-testid"), field));
        return mapCollection;
    }

    public MonthsPaidPerYear monthsPaidPerYear() {
        return new MonthsPaidPerYear(MONTHS_PAID_BUTTON);
    }

    public MonthsWorked monthsWorked() {
        return new MonthsWorked(MONTHS_WORKED_BUTTON);
    }

    public PayFrequency payFrequency() {
        return new PayFrequency(PAY_FREQUENCY_BUTTON);
    }

}

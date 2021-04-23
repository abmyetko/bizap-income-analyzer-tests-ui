package com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.income;

import com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.evidence.DateField;
import com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.evidence.MatCalendar;
import com.capsilon.incomeanalyzer.automation.ui.component.sidenav.CheckBox;
import com.capsilon.test.run.confiuration.BizappsConfig;
import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.TimeoutException;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_FIFTEEN_SECONDS;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$x;

public class AddJobDialog {

    public final SelenideElement COMPONENT_CONTAINER;
    public final SelenideElement HEADER;
    public final SelenideElement BUTTON_SAVE;
    public final SelenideElement BUTTON_CANCEL;
    public final SelenideElement INPUT_JOB_NAME;
    public final DateField INPUT_START_DATE;
    public final DateField INPUT_END_DATE;
    public final CheckBox CHECKBOX_PRIMARY_EMPLOYER;
    public final CheckBox CHECKBOX_CURRENT_EMPLOYER;


    public AddJobDialog() {
        COMPONENT_CONTAINER = $x("//div[@class='cdk-overlay-container']//mat-dialog-container//ancestor::div[@class='cdk-overlay-container']");
        HEADER = COMPONENT_CONTAINER.$x(".//h1");
        BUTTON_SAVE = COMPONENT_CONTAINER.$x(".//button[@id='save-btn']");
        BUTTON_CANCEL = COMPONENT_CONTAINER.$x(".//button[@id='cancel-btn']");
        INPUT_JOB_NAME = COMPONENT_CONTAINER.$x(".//input[@id='jobName']");
        INPUT_START_DATE = new DateField(COMPONENT_CONTAINER.$x(".//input[@id='startDate']//ancestor::div[@class='mat-form-field-flex']"));
        INPUT_END_DATE = new DateField(COMPONENT_CONTAINER.$x(".//input[@id='endDate']//ancestor::div[@class='mat-form-field-flex']"));
        CHECKBOX_PRIMARY_EMPLOYER = new CheckBox(COMPONENT_CONTAINER.$x(".//mat-checkbox[@id='primaryEmployer']"));
        CHECKBOX_CURRENT_EMPLOYER = new CheckBox(COMPONENT_CONTAINER.$x(".//mat-checkbox[@id='currentEmployer']"));
    }

    public void selectDateByMatCalendar(SelenideElement inputField, int day, int month, int year) {
        MatCalendar calendar = new MatCalendar();
        inputField.$x("./parent::div/following-sibling::div").click();
        calendar.selectDate(day, month, year);
        inputField.shouldHave(text(month + "/" + day + "/" + year));
    }


    public SelenideElement getAddJobOption() {
        return COMPONENT_CONTAINER.$x(".//mat-option[contains(@class,'add-option')]");
    }

    public ElementsCollection getAutocompleteOptions() {
        return COMPONENT_CONTAINER.$$x(".//mat-option[contains(@class,'mat-option') and not(contains(@class,'add-option'))]");
    }

    public void addCurrentJob(String jobName, String startDate) {
        CHECKBOX_CURRENT_EMPLOYER.setCheckboxValue(true);
        INPUT_START_DATE.selectDate(startDate);
        inputText(INPUT_JOB_NAME, jobName);

        getAddJobOption().click();
        INPUT_START_DATE.INPUT.shouldHave(value(startDate));
        INPUT_JOB_NAME.shouldHave(value(jobName));
        BUTTON_SAVE.click();
    }

    public void addPrimaryJob(String jobName, String startDate) {
        CHECKBOX_PRIMARY_EMPLOYER.setCheckboxValue(true);
        CHECKBOX_CURRENT_EMPLOYER.setCheckboxValue(true);
        INPUT_START_DATE.selectDate(startDate);
        inputText(INPUT_JOB_NAME, jobName);

        getAddJobOption().click();
        INPUT_START_DATE.INPUT.shouldHave(value(startDate));
        INPUT_JOB_NAME.shouldHave(value(jobName));
        BUTTON_SAVE.click();
    }

    public void addPreviousJob(String jobName, String startDate, String endDate) {
        INPUT_START_DATE.selectDate(startDate);
        INPUT_END_DATE.selectDate(endDate);
        inputText(INPUT_JOB_NAME, jobName);

        getAddJobOption().click();
        INPUT_START_DATE.INPUT.shouldHave(value(startDate));
        INPUT_END_DATE.INPUT.shouldHave(value(endDate));
        INPUT_JOB_NAME.shouldHave(value(jobName));
        BUTTON_SAVE.click();
    }

    public void addPreviousJobByAutocomplete(String jobNamePart, String jobToSelect, String startDate, String endDate) {
        INPUT_START_DATE.selectDate(startDate);
        INPUT_END_DATE.selectDate(endDate);
        inputText(INPUT_JOB_NAME, jobNamePart);
        try {
            Retry.whileTrue(TIMEOUT_FIFTEEN_SECONDS,
                    () -> getAutocompleteOptions().filterBy(Condition.exactText(jobToSelect)).isEmpty(),
                    "No change");
            //@formatter:off
        } catch (TimeoutException ignore) {} //NOSONAR
        //@formatter:on
        ElementsCollection matchingJobNames = getAutocompleteOptions().filterBy(Condition.exactText(jobToSelect));
        if (matchingJobNames.isEmpty()) {
            INPUT_START_DATE.selectDate(startDate);
            INPUT_END_DATE.selectDate(endDate);
            inputText(INPUT_JOB_NAME, jobToSelect);
        } else {
            getAutocompleteOptions().filterBy(Condition.exactText(jobToSelect)).get(0).click();
        }
        INPUT_START_DATE.INPUT.shouldHave(value(startDate));
        INPUT_END_DATE.INPUT.shouldHave(value(endDate));
        INPUT_JOB_NAME.shouldHave(value(jobToSelect));
        BUTTON_SAVE.click();
    }

    private void inputText(SelenideElement inputField, String inputValue) {
        String browserId = BizappsConfig.getString("bizapps.selenium.browser", "ie");
        inputField.clear();
        if ("ie".equals(browserId) || "edge".equals(browserId)) {
            inputField.setValue(inputValue);
        } else {
            inputField.sendKeys(inputValue);
        }

    }
}

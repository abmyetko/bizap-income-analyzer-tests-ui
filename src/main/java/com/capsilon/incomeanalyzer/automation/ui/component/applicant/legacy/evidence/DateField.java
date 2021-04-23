package com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.evidence;

import com.capsilon.test.run.confiuration.BizappsConfig;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.value;

public class DateField {

    public final SelenideElement INPUT;
    public final SelenideElement DATE_PICKER_BUTTON;
    private final SelenideElement COMPONENT_CONTAINER; //NOSONAR

    public DateField(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer; //NOSONAR
        INPUT = componentContainer.$x(".//input");
        DATE_PICKER_BUTTON = componentContainer.$x(".//mat-datepicker-toggle");
    }

    public void selectDate(int day, int month, int year) {
        MatCalendar calendar = new MatCalendar();

        COMPONENT_CONTAINER.click();

        calendar.selectDate(day, month, year);

        INPUT.shouldHave(value(month + "/" + day + "/" + year));
    }

    public void selectDate(String fullDate) {
        MatCalendar calendar = new MatCalendar();

        String browserId = BizappsConfig.getString("bizapps.selenium.browser", "ie");
        if ("ie".equals(browserId) || "edge".equals(browserId)) {
            INPUT.click();
        } else {
            COMPONENT_CONTAINER.click();
        }

        calendar.selectDate(fullDate);

        INPUT.shouldHave(value(fullDate));
    }
}

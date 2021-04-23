package com.capsilon.incomeanalyzer.automation.ui.component.applicant.legacy.evidence;

import com.capsilon.test.ui.Retry;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.ARIA_LABEL;
import static com.capsilon.test.ui.components.Conditions.attributeContains;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$x;

public class MatCalendar {

    private static final String MONTH_AND_YEAR = "month and year";
    private static final int THREE = 3;
    private static final int TWO = 2;
    public final SelenideElement SPAN_SELECTION_BUTTON;
    public final SelenideElement SPAN_PREVIOUS_PERIOD;
    public final SelenideElement SPAN_NEXT_PERIOD;
    public final SelenideElement HEADER;
    public final SelenideElement BODY;
    public final SelenideElement MONTH_VIEW;
    private final List<String> months = new ArrayList<>();
    private final SelenideElement MONTH_VIEW_SELECTED_MONTH;
    private final ElementsCollection MONTH_VIEW_DAYS;
    private final SelenideElement YEAR_VIEW;
    private final SelenideElement YEAR_MONTHS_VIEW;

    public MatCalendar() {
        SelenideElement componentContainer = $x("//mat-calendar");
        HEADER = componentContainer.$x("./mat-calendar-header");
        BODY = componentContainer.$x("./div");
        MONTH_VIEW = BODY.$x("./mat-month-view");
        MONTH_VIEW_SELECTED_MONTH = MONTH_VIEW.$x(".//td[not(@aria-label)]");
        MONTH_VIEW_DAYS = MONTH_VIEW.$$x(".//td[@aria-label]");
        YEAR_VIEW = BODY.$x("./mat-multi-year-view");
        YEAR_MONTHS_VIEW = BODY.$x("./mat-year-view");
        SPAN_SELECTION_BUTTON = HEADER.$x(".//button[contains(@class,'period')]");
        SPAN_PREVIOUS_PERIOD = HEADER.$x(".//button[contains(@class,'previous')]");
        SPAN_NEXT_PERIOD = HEADER.$x(".//button[contains(@class,'next')]");
        months.addAll(Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"));
    }

    private SelenideElement getYear(int year) {
        return YEAR_VIEW.$x(".//td[@aria-label='" + year + "']");
    }

    private SelenideElement getMonth(String month) {
        return YEAR_MONTHS_VIEW.$x(".//td/div[text()='" + month + "']");
    }

    public void selectThisYearDate(int dayNumber, int monthNumber) {
        if (!SPAN_SELECTION_BUTTON.getAttribute(ARIA_LABEL).contains(MONTH_AND_YEAR)) {
            Retry.tryRun(() -> {
                SPAN_SELECTION_BUTTON.click();
                SPAN_SELECTION_BUTTON.shouldHave(attributeContains(ARIA_LABEL, MONTH_AND_YEAR));
            });
        }

        int numberOfTurns = monthNumber - 1 - months.indexOf(MONTH_VIEW_SELECTED_MONTH.text());
        int startMonth = months.indexOf(MONTH_VIEW_SELECTED_MONTH.text());
        if (numberOfTurns > 0) {
            for (int i = 1; i <= numberOfTurns; i++) {
                SPAN_NEXT_PERIOD.click();
                MONTH_VIEW_SELECTED_MONTH.shouldHave(text(months.get(startMonth + i)));
            }
        } else {
            for (int i = numberOfTurns; i < 0; i++) {
                SPAN_NEXT_PERIOD.click();
                MONTH_VIEW_SELECTED_MONTH.shouldHave(text(months.get(startMonth - (startMonth - i))));
            }
        }

        MONTH_VIEW_DAYS.get(dayNumber + 1).click();
    }

    public void selectDate(int dayNumber, int monthNumber, int yearNumber) {
        if (SPAN_SELECTION_BUTTON.getAttribute(ARIA_LABEL).contains(MONTH_AND_YEAR)) {
            Retry.tryRun(() -> {
                SPAN_SELECTION_BUTTON.click();
                SPAN_SELECTION_BUTTON.shouldNotHave(attributeContains(ARIA_LABEL, MONTH_AND_YEAR));
            });
        }

        if (!getYear(yearNumber).exists()) {
            Retry.whileTrue(() -> {
                SPAN_PREVIOUS_PERIOD.click();
                return !getYear(yearNumber).exists();
            });
        }

        getYear(yearNumber).click();

        getMonth(months.get(monthNumber - 1)).click();

        MONTH_VIEW_DAYS.get(dayNumber -1).click();
    }

    public void selectDate(String fullDate) {
        String[] split = fullDate.split("/", THREE);

        int dayNumber = Integer.parseInt(split[1]);
        int monthNumber = Integer.parseInt(split[0]);
        int yearNumber = Integer.parseInt(split[TWO]);

        selectDate(dayNumber, monthNumber, yearNumber);
    }

}

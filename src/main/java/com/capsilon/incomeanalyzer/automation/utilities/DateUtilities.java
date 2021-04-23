package com.capsilon.incomeanalyzer.automation.utilities;

import com.capsilon.incomeanalyzer.automation.utilities.enums.DateFormats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;


public final class DateUtilities {

    private DateUtilities() {
    }

    public static String formatDate(DateFormats sourceFormat, String dateToConvert, DateFormats expectedFormat) {
        try {
            return new SimpleDateFormat(expectedFormat.dateFormat).format(new SimpleDateFormat(sourceFormat.dateFormat).parse(dateToConvert));
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: " + dateToConvert + " to new format!");
        }
    }

    public static String getDateFromNow(int yearsBack, int monthsBack, int dayOfMonth) {
        return getDateFromSignDate(yearsBack, monthsBack, dayOfMonth, LocalDateTime.now().toString());
    }

    public static String getDateFromSignDate(int yearsBack, int monthsBack, int dayOfMonth, String signDate) {
        int month = LocalDate.parse(signDate, DateTimeFormatter.ofPattern("MM/dd/yyyy")).getMonthValue() - monthsBack;
        int overflowYear = 0;
        while (month <= 0) {
            month += 12;
            overflowYear++;
        }

        int year = LocalDate.parse(signDate, DateTimeFormatter.ofPattern("MM/dd/yyyy")).getYear() - yearsBack - overflowYear;

        YearMonth yearMonth = YearMonth.of(year, month);
        if (yearMonth.lengthOfMonth() < dayOfMonth) {
            dayOfMonth = yearMonth.lengthOfMonth();
        }

        NumberFormat formatter = new DecimalFormat("00");
        return "mm/dd/yyyy".replaceFirst("mm", formatter.format(month))
                .replaceFirst("dd", formatter.format(dayOfMonth))
                .replaceFirst("yyyy", Integer.toString(year));
    }
}

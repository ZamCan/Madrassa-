package com.zamcan.madrassa.core.calendar;

import java.time.LocalDate;

/**
 * Formats EduNoor dates for display. Pure java - callers inject the
 * localized month-name arrays from string resources, so all three
 * languages (Swahili/English/Arabic) flow through one formatter.
 */
public final class EduNoorDateFormatter {

    private EduNoorDateFormatter() {
    }

    /** e.g. "13 Rabi al-Thani 1448 H" (names provided by resources). */
    public static String formatHijri(
            EduNoorCalendars.HijriDate hijri,
            String[] hijriMonthNames
    ) {
        return hijri.day + " "
                + hijriMonthNames[hijri.month - 1] + " "
                + hijri.year + " H";
    }

    /** e.g. "24 Septemba 2026" (names provided by resources). */
    public static String formatGregorian(
            LocalDate date,
            String[] gregorianMonthNames
    ) {
        return date.getDayOfMonth() + " "
                + gregorianMonthNames[date.getMonthValue() - 1] + " "
                + date.getYear();
    }
}

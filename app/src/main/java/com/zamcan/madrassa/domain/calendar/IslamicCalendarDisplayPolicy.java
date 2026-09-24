package com.zamcan.madrassa.domain.calendar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/** Display-only policy; it never changes the device's civil calendar. */
public final class IslamicCalendarDisplayPolicy {

    private IslamicCalendarDisplayPolicy() {
    }

    public static String dualDate(
            LocalDate date,
            Locale locale
    ) {
        if (date == null) {
            return "";
        }

        IslamicCalendarEngine.HijriDate hijri =
                IslamicCalendarEngine.fromGregorian(date);

        String civil = date.format(
                DateTimeFormatter.ofLocalizedDate(
                        FormatStyle.MEDIUM
                ).withLocale(locale == null ? Locale.getDefault() : locale)
        );

        return civil + " • " + hijri;
    }
}

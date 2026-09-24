package com.zamcan.madrassa.domain.calendar;

import com.zamcan.madrassa.core.calendar.EduNoorCalendars;

import java.time.LocalDate;

/**
 * Domain-facing Islamic calendar facade.
 *
 * <p>The detailed Umm al-Qura table and its documented out-of-range
 * approximation remain owned by {@link EduNoorCalendars}; this class
 * keeps services and widgets independent of the UI package.</p>
 */
public final class IslamicCalendarEngine {

    private IslamicCalendarEngine() {
    }

    public static HijriDate fromGregorian(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        EduNoorCalendars.HijriDate value =
                EduNoorCalendars.toHijri(date);

        return new HijriDate(
                value.year,
                value.month,
                value.day
        );
    }

    public static LocalDate toGregorian(HijriDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        return EduNoorCalendars.toGregorian(
                new EduNoorCalendars.HijriDate(
                        date.year,
                        date.month,
                        date.day
                )
        );
    }

    public static int monthLength(int year, int month) {
        return EduNoorCalendars.hijriMonthLength(year, month);
    }

    public static final class HijriDate {
        public final int year;
        public final int month;
        public final int day;

        public HijriDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        @Override
        public String toString() {
            return day + "/" + month + "/" + year;
        }
    }
}

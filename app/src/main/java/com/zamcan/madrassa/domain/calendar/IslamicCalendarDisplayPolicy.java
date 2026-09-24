package com.zamcan.madrassa.domain.calendar;

import java.time.LocalDate;

/** Display policy for a dual civil/Islamic calendar; never changes the device's civil calendar. */
public final class IslamicCalendarDisplayPolicy {
 private IslamicCalendarDisplayPolicy(){}
 public static String dualDate(LocalDate date){
  IslamicCalendarEngine.HijriDate h=IslamicCalendarEngine.fromGregorian(date);
  return date+" • "+h;
 }
}
package com.zamcan.madrassa.domain.salah;

import com.zamcan.madrassa.domain.geography.LocationProfile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class SalahReminderEngine {
 private SalahReminderEngine(){}
 public static List<PrayerTime> today(LocalDate date,LocationProfile location){return PrayerTimesCalculator.calculate(date,location);}
 public static PrayerTime.Prayer dueNow(LocalDateTime now,LocationProfile location,PrayerTime.Prayer alreadyTriggered){return AdhanTriggerPolicy.duePrayer(now,today(now.toLocalDate(),location),alreadyTriggered);}
 /** Never plays before the scheduled time; allows a short delayed-delivery window. */
 public static boolean mayPlayAdhan(LocalDateTime now,PrayerTime scheduled){
  if(now==null||scheduled==null)return false;
  LocalDateTime due=LocalDateTime.of(now.toLocalDate(),scheduled.time);
  return !now.isBefore(due)&&now.isBefore(due.plusMinutes(5));
 }
}

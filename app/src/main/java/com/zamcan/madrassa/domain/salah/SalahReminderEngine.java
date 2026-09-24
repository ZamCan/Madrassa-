package com.zamcan.madrassa.domain.salah;

import com.zamcan.madrassa.domain.geography.LocationProfile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Single source of truth for daily Salah schedule and exact-time Adhan decisions. */
public final class SalahReminderEngine {
 private SalahReminderEngine(){}
 public static List<PrayerTime> today(LocalDate date,LocationProfile location){return PrayerTimesCalculator.calculate(date,location);}
 public static PrayerTime.Prayer dueNow(LocalDateTime now,LocationProfile location,PrayerTime.Prayer alreadyTriggered){
  return AdhanTriggerPolicy.duePrayer(now,today(now.toLocalDate(),location),alreadyTriggered);
 }
 public static boolean mayPlayAdhan(LocalDateTime now,PrayerTime scheduled){
  if(now==null||scheduled==null)return false;
  return !now.toLocalTime().isBefore(scheduled.time)
      && now.toLocalTime().isBefore(scheduled.time.plusMinutes(1));
 }
}

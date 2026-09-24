package com.zamcan.madrassa.salah;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.zamcan.madrassa.domain.geography.LocationProfile;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.salah.PrayerTimesCalculator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Schedules local alarms. No network service is required. */
public final class SalahAlarmScheduler {
 private SalahAlarmScheduler(){}
 public static void scheduleDay(Context context,LocalDate date,LocationProfile location){
  if(context==null||date==null||location==null)throw new IllegalArgumentException("context/date/location required");
  AlarmManager alarms=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);if(alarms==null)return;
  List<PrayerTime> schedule=PrayerTimesCalculator.calculate(date,location);
  for(PrayerTime p:schedule){
   if(p.prayer==PrayerTime.Prayer.SUNRISE)continue;
   long when=date.atTime(p.time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
   if(when<=System.currentTimeMillis())continue;
   Intent i=new Intent(context,AdhanAlarmReceiver.class).putExtra("prayer",p.prayer.name())
     .putExtra("country",location.countryCode).putExtra("city",location.city)
     .putExtra("lat",location.latitude).putExtra("lon",location.longitude).putExtra("offset",location.utcOffsetHours);
   int request=(date.toEpochDay()*10+(p.prayer.ordinal()+1))>Integer.MAX_VALUE?Math.abs((date.toString()+p.prayer.name()).hashCode()):(int)(date.toEpochDay()*10+(p.prayer.ordinal()+1));
   PendingIntent pi=PendingIntent.getBroadcast(context,request,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   if(android.os.Build.VERSION.SDK_INT>=31 && !alarms.canScheduleExactAlarms()) alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
   else alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
  }
 }
}

package com.zamcan.madrassa.salah;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import com.zamcan.madrassa.domain.geography.LocationProfile;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.salah.PrayerTimesCalculator;
import com.zamcan.madrassa.domain.salah.SalahReminderEngine;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/** Receives a local alarm and refuses playback before the calculated prayer time. */
public final class AdhanAlarmReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){
  if(intent==null)return;
  String prayerName=intent.getStringExtra("prayer");
  if(prayerName==null)return;
  double lat=intent.getDoubleExtra("lat",0);
  double lon=intent.getDoubleExtra("lon",0);
  double offset=intent.getDoubleExtra("offset",0);
  LocationProfile location=new LocationProfile(intent.getStringExtra("country"),intent.getStringExtra("city"),lat,lon,offset);
  PrayerTime.Prayer prayer;
  try{prayer=PrayerTime.Prayer.valueOf(prayerName);}catch(Exception e){return;}
  LocalDate date=LocalDate.now();
  List<PrayerTime> schedule=PrayerTimesCalculator.calculate(date,location);
  PrayerTime target=null;for(PrayerTime p:schedule)if(p.prayer==prayer){target=p;break;}
  if(target==null||!SalahReminderEngine.mayPlayAdhan(LocalDateTime.now(),target))return;
  final TextToSpeech[] holder=new TextToSpeech[1];
  holder[0]=new TextToSpeech(context.getApplicationContext(),status->{
   if(status==TextToSpeech.SUCCESS){
    holder[0].setLanguage(new Locale("ar"));
    String text="الله أكبر، الله أكبر. أشهد أن لا إله إلا الله. أشهد أن محمدًا رسول الله. حي على الصلاة. حي على الفلاح. الله أكبر، الله أكبر. لا إله إلا الله.";
    if(prayer==PrayerTime.Prayer.FAJR)text="الله أكبر، الله أكبر. أشهد أن لا إله إلا الله. أشهد أن محمدًا رسول الله. حي على الصلاة. حي على الفلاح. الصلاة خير من النوم. الصلاة خير من النوم. الله أكبر، الله أكبر. لا إله إلا الله.";
    holder[0].speak(text,TextToSpeech.QUEUE_FLUSH,null,"edunoor_adhan");
   }
  });
 }
}

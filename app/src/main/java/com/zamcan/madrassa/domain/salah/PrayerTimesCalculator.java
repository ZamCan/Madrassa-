package com.zamcan.madrassa.domain.salah;

import com.zamcan.madrassa.domain.geography.LocationProfile;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Offline solar calculation; settings keep method/madhhab/high-latitude behavior configurable. */
public final class PrayerTimesCalculator {
 private static final double PI=Math.PI;
 private PrayerTimesCalculator(){}
 public static List<PrayerTime> calculate(LocalDate date,LocationProfile loc){return calculate(date,loc,PrayerCalculationSettings.defaultSettings());}
 public static List<PrayerTime> calculate(LocalDate date,LocationProfile loc,PrayerCalculationSettings settings){
  if(date==null||loc==null||settings==null)throw new IllegalArgumentException("date/location/settings required");
  int n=date.getDayOfYear();double g=2*PI/365.0*(n-1);
  double eq=229.18*(0.000075+0.001868*Math.cos(g)-0.032077*Math.sin(g)-0.014615*Math.cos(2*g)-0.040849*Math.sin(2*g));
  double dec=0.006918-0.399912*Math.cos(g)+0.070257*Math.sin(g)-0.006758*Math.cos(2*g)+0.000907*Math.sin(2*g)-0.002697*Math.cos(3*g)+0.00148*Math.sin(3*g);
  double noon=720-4*loc.longitude-eq+loc.utcOffsetHours*60;
  double sunrise=event(noon,loc.latitude,dec,-0.833,false,settings.highLatitudeAdjustment);
  double sunset=event(noon,loc.latitude,dec,-0.833,true,settings.highLatitudeAdjustment);
  double fajr=event(noon,loc.latitude,dec,-settings.method.fajrAngle,false,settings.highLatitudeAdjustment);
  double isha=event(noon,loc.latitude,dec,-settings.method.ishaAngle,true,settings.highLatitudeAdjustment);
  double asr=asr(noon,loc.latitude,dec,settings.asrMadhhab.shadow);
  List<PrayerTime> out=new ArrayList<>();
  out.add(new PrayerTime(PrayerTime.Prayer.FAJR,toTime(fajr)));out.add(new PrayerTime(PrayerTime.Prayer.SUNRISE,toTime(sunrise)));
  out.add(new PrayerTime(PrayerTime.Prayer.DHUHR,toTime(noon)));out.add(new PrayerTime(PrayerTime.Prayer.ASR,toTime(asr)));
  out.add(new PrayerTime(PrayerTime.Prayer.MAGHRIB,toTime(sunset)));out.add(new PrayerTime(PrayerTime.Prayer.ISHA,toTime(isha)));
  return out;
 }
 private static double event(double noon,double lat,double dec,double altitude,boolean evening,boolean high){
  double phi=Math.toRadians(lat),z=Math.toRadians(90-altitude);
  double cosH=(Math.cos(z)-Math.sin(phi)*Math.sin(dec))/(Math.cos(phi)*Math.cos(dec));
  if(cosH>1)return high?noon-(evening?0:720):noon;
  if(cosH<-1)return high?noon+(evening?720:0):noon;
  double h=Math.toDegrees(Math.acos(cosH));return noon+(evening?4*h:-4*h);
 }
 private static double asr(double noon,double lat,double dec,double shadow){
  double phi=Math.toRadians(lat);
  double altitude=-Math.toDegrees(Math.atan(1.0/(shadow+Math.tan(Math.abs(phi-dec)))));
  return event(noon,lat,dec,altitude,true,true);
 }
 private static LocalTime toTime(double m){long t=Math.round(m);t=((t%1440)+1440)%1440;return LocalTime.of((int)(t/60),(int)(t%60));}
}
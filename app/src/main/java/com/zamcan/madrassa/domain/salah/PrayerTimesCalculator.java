package com.zamcan.madrassa.domain.salah;

import com.zamcan.madrassa.domain.geography.LocationProfile;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Offline solar calculation using fixed Fajr/Isha 18-degree convention. */
public final class PrayerTimesCalculator {
 private static final double PI=Math.PI;
 private PrayerTimesCalculator(){}
 public static List<PrayerTime> calculate(LocalDate date,LocationProfile loc){
  if(date==null||loc==null)throw new IllegalArgumentException("date/location required");
  int n=date.getDayOfYear(); double g=2*PI/365.0*(n-1);
  double eq=229.18*(0.000075+0.001868*Math.cos(g)-0.032077*Math.sin(g)-0.014615*Math.cos(2*g)-0.040849*Math.sin(2*g));
  double dec=0.006918-0.399912*Math.cos(g)+0.070257*Math.sin(g)-0.006758*Math.cos(2*g)+0.000907*Math.sin(2*g)-0.002697*Math.cos(3*g)+0.00148*Math.sin(3*g);
  double noon=720-4*loc.longitude-eq+loc.utcOffsetHours*60;
  double rise=event(noon,loc.latitude,dec,-0.833,false),set=event(noon,loc.latitude,dec,-0.833,true);
  double fajr=event(noon,loc.latitude,dec,-18,false),isha=event(noon,loc.latitude,dec,-18,true);
  double asr=asr(noon,loc.latitude,dec);
  List<PrayerTime> out=new ArrayList<>();
  out.add(new PrayerTime(PrayerTime.Prayer.FAJR,toTime(fajr)));
  out.add(new PrayerTime(PrayerTime.Prayer.SUNRISE,toTime(rise)));
  out.add(new PrayerTime(PrayerTime.Prayer.DHUHR,toTime(noon)));
  out.add(new PrayerTime(PrayerTime.Prayer.ASR,toTime(asr)));
  out.add(new PrayerTime(PrayerTime.Prayer.MAGHRIB,toTime(set)));
  out.add(new PrayerTime(PrayerTime.Prayer.ISHA,toTime(isha)));
  return out;
 }
 private static double event(double noon,double lat,double dec,double altitude,boolean evening){
  double phi=Math.toRadians(lat),z=Math.toRadians(90-altitude);
  double cosH=(Math.cos(z)-Math.sin(phi)*Math.sin(dec))/(Math.cos(phi)*Math.cos(dec));
  if(cosH>=1)return noon-(evening?0:720);
  if(cosH<=-1)return noon+(evening?720:0);
  double h=Math.toDegrees(Math.acos(cosH));
  return noon+(evening?4*h:-4*h);
 }
 private static double asr(double noon,double lat,double dec){
  double phi=Math.toRadians(lat);
  double altitude=-Math.toDegrees(Math.atan(1.0/(1.0+Math.tan(Math.abs(phi-dec)))));
  return event(noon,lat,dec,altitude,true);
 }
 private static LocalTime toTime(double m){
  long t=Math.round(m);t=((t%1440)+1440)%1440;
  return LocalTime.of((int)(t/60),(int)(t%60));
 }
}

package com.zamcan.madrassa.domain.salah;

/** Deterministic, user-configurable prayer calculation parameters. */
public final class PrayerCalculationSettings {
 public enum Method { MUSLIM_WORLD_LEAGUE(18.0,17.0), ISNA(15.0,15.0), EGYPT(19.5,17.5), KARACHI(18.0,18.0);
  final double fajrAngle,ishaAngle; Method(double f,double i){fajrAngle=f;ishaAngle=i;}
 }
 public enum AsrMadhhab { STANDARD(1.0), HANAFI(2.0); final double shadow; AsrMadhhab(double s){shadow=s;} }
 public final Method method; public final AsrMadhhab asrMadhhab; public final boolean highLatitudeAdjustment;
 public PrayerCalculationSettings(Method m,AsrMadhhab a,boolean h){method=m;asrMadhhab=a;highLatitudeAdjustment=h;}
 public static PrayerCalculationSettings defaultSettings(){return new PrayerCalculationSettings(Method.MUSLIM_WORLD_LEAGUE,AsrMadhhab.STANDARD,true);}
}
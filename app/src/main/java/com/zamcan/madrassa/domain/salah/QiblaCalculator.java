package com.zamcan.madrassa.domain.salah;

/** Deterministic great-circle bearing from device location to the Kaaba. */
public final class QiblaCalculator {
 private static final double KAABA_LAT=21.422487, KAABA_LON=39.826206;
 private QiblaCalculator(){}
 public static double bearingDegrees(double latitude,double longitude){
  double p1=Math.toRadians(latitude),p2=Math.toRadians(KAABA_LAT);
  double dl=Math.toRadians(KAABA_LON-longitude);
  double y=Math.sin(dl)*Math.cos(p2);
  double x=Math.cos(p1)*Math.sin(p2)-Math.sin(p1)*Math.cos(p2)*Math.cos(dl);
  return (Math.toDegrees(Math.atan2(y,x))+360.0)%360.0;
 }
 public static String cardinal(double bearing){
  String[] dirs={"N","NE","E","SE","S","SW","W","NW"};
  return dirs[(int)Math.round(bearing/45.0)%8];
 }
}
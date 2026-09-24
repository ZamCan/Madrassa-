package com.zamcan.madrassa.domain.calendar;

import java.time.LocalDate;

public final class IslamicCalendarEngine {
 private IslamicCalendarEngine(){}
 public static HijriDate fromGregorian(LocalDate date){if(date==null)throw new IllegalArgumentException("date is required");return fromJulianDay(gregorianToJulianDay(date.getYear(),date.getMonthValue(),date.getDayOfMonth()));}
 private static HijriDate fromJulianDay(int jd){int l=jd-1948440+10632;int n=(l-1)/10631;l=l-10631*n+354;int j=((10985-l)/5316)*((50*l)/17719)+(l/5670)*((43*l)/15238);l=l-((30-j)/15)*((17719*j)/50)-(j/16)*((15238*j)/43)+29;int m=(24*l)/709;int d=l-(709*m)/24;int y=30*n+j-30;return new HijriDate(y,m,d);}
 private static int gregorianToJulianDay(int y,int m,int d){int a=(14-m)/12;int yy=y+4800-a;int mm=m+12*a-3;return d+(153*mm+2)/5+365*yy+yy/4-yy/100+yy/400-32045;}
 public static final class HijriDate{public final int year,month,day;public HijriDate(int y,int m,int d){year=y;month=m;day=d;}public String monthName(){String[] n={"Muharram","Safar","Rabi al-Awwal","Rabi al-Thani","Jumada al-Awwal","Jumada al-Thani","Rajab","Sha'ban","Ramadan","Shawwal","Dhu al-Qi'dah","Dhu al-Hijjah"};return month>=1&&month<=12?n[month-1]:"";}public String toString(){return day+" "+monthName()+" "+year+" AH";}}
}

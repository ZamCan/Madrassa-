package com.zamcan.madrassa.domain.salah;
import java.time.LocalTime;
public final class PrayerTime {public enum Prayer{FAJR,SUNRISE,DHUHR,ASR,MAGHRIB,ISHA}public final Prayer prayer;public final LocalTime time;public PrayerTime(Prayer p,LocalTime t){prayer=p;time=t;}}

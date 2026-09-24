package com.zamcan.madrassa.domain.salah;

/** Local preference model for Salah reminders; audio is never forced. */
public final class AdhanSettings {
 public final boolean enabled;
 public final boolean useVoice;
 public final boolean useFajrSpecial;
 public final int iqamaMinutes;
 public AdhanSettings(boolean enabled,boolean useVoice,boolean useFajrSpecial,int iqamaMinutes){
  this.enabled=enabled;this.useVoice=useVoice;this.useFajrSpecial=useFajrSpecial;this.iqamaMinutes=Math.max(0,iqamaMinutes);
 }
 public static AdhanSettings defaults(){return new AdhanSettings(false,true,true,10);}
}
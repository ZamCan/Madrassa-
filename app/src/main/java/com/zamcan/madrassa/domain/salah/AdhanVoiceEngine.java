package com.zamcan.madrassa.domain.salah;

import java.util.ArrayList;
import java.util.List;

/** Builds the exact words to be spoken; Android TTS/audio is kept replaceable. */
public final class AdhanVoiceEngine {
 private AdhanVoiceEngine(){}
 public static List<String> script(PrayerTime.Prayer prayer){
  List<String> lines=new ArrayList<>(AdhanScript.adhanLines());
  if(prayer==PrayerTime.Prayer.FAJR){lines.add(10,AdhanScript.fajrAdhanAddition().get(0));lines.add(11,AdhanScript.fajrAdhanAddition().get(1));}
  return lines;
 }
}

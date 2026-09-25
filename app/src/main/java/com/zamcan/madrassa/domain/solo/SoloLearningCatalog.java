package com.zamcan.madrassa.domain.solo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Offline-first Solo curriculum map; content payloads can be added without changing navigation. */
public final class SoloLearningCatalog {
 public static final class Area {public final String id,title,subtitle;public Area(String i,String t,String s){id=i;title=t;subtitle=s;}}
 private SoloLearningCatalog(){}
 public static List<Area> areas(){return Collections.unmodifiableList(Arrays.asList(
  new Area("QURAN","Qur'an","Surahs, reading, ayah study, tajwid and Hifz revision"),
  new Area("SALAH","Salah","Prayer times, wudu, prayer learning and reminders"),
  new Area("HADITH","Hadith","Hadith reading, themes and learning notes"),
  new Area("TAWHEED","Tawheed","Foundations of belief and Islamic creed"),
  new Area("FIQH","Fiqh","Worship and everyday Islamic jurisprudence"),
  new Area("BARAZANJ","Barazanj","Seerah, praise and traditional devotional reading"),
  new Area("BOOKS","Books","Structured books, documents, audio and study resources"),
  new Area("ADAB","Adab","Character, manners and daily practice")
  ));}
 public static List<String> studyModes(){return Collections.unmodifiableList(Arrays.asList("READ","LISTEN","REVIEW","MEMORISE","REFLECT"));}
}
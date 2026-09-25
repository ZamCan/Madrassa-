package com.zamcan.madrassa.domain.knowledge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Navigation-level knowledge map; authoritative texts remain separate content assets. */
public final class IslamicKnowledgeCatalog {
 private IslamicKnowledgeCatalog(){}
 public static List<String> subjects(){return Collections.unmodifiableList(Arrays.asList(
  "Qur'an","Tajwid","Hifz","Tafsir","Hadith","Tawheed","Aqidah","Fiqh",
  "Seerah","Barazanj","Adab","Arabic","Salah","Dua","Islamic Calendar","General Knowledge"
  ));}
 public static List<String> resourceTypes(){return Collections.unmodifiableList(Arrays.asList(
  "LESSON","BOOK","TEXT","DOCUMENT","AUDIO","IMAGE","QURAN_REFERENCE","HADITH_REFERENCE"
  ));}
}
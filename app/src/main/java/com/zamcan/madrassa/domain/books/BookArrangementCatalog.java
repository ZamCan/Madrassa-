package com.zamcan.madrassa.domain.books;

import java.util.Arrays;import java.util.Collections;import java.util.List;
public final class BookArrangementCatalog {private BookArrangementCatalog(){}public static List<String> categories(){return Collections.unmodifiableList(Arrays.asList("Qur'an","Tajwid","Hifz","Tafsir","Hadith","Aqidah / Tawheed","Fiqh","Seerah","Adab","Arabic","General"));}public static List<String> resourceTypes(){return Collections.unmodifiableList(Arrays.asList("TEXT","BOOK","AUDIO","DOCUMENT","IMAGE","QURAN_REF"));}}

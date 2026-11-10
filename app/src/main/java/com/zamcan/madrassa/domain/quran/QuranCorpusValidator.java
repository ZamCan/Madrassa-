package com.zamcan.madrassa.domain.quran;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Structural validation for a Quran corpus imported from an independently
 * verified and redistribution-licensed source.
 *
 * This class does NOT provide, authenticate or certify the Arabic text.
 * The standard Kufan verse numbering used here has 6,236 verses.
 */
public final class QuranCorpusValidator {
    private static final int[] VERSES = {
        7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,
        112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,
        54,53,89,59,37,35,38,29,18,45,60,49,62,55,78,96,29,22,24,13,
        14,11,11,18,12,12,30,52,52,44,28,28,20,56,40,31,50,40,46,42,
        29,19,36,25,22,17,19,26,30,20,15,21,11,8,8,19,5,8,8,11,
        11,8,3,9,5,4,7,3,6,3,5,4,5,6
    };

    private QuranCorpusValidator() {}

    public static int expectedVerses(int surah) {
        if (surah < 1 || surah > VERSES.length) {
            throw new IllegalArgumentException("Invalid surah: " + surah);
        }
        return VERSES[surah - 1];
    }

    public static int expectedTotalVerses() {
        int total = 0;
        for (int count : VERSES) total += count;
        return total;
    }

    /** Immutable representation of one imported verse. */
    public static final class Verse {
        public final int surah;
        public final int ayah;
        public final String arabic;
        public Verse(int surah, int ayah, String arabic) {
            this.surah = surah;
            this.ayah = ayah;
            this.arabic = arabic;
        }
    }

    /**
     * Rejects missing, duplicate, out-of-order, empty or structurally invalid
     * verses. The caller must additionally validate provenance and SHA-256.
     */
    public static void validate(List<Verse> verses) {
        if (verses == null || verses.size() != expectedTotalVerses()) {
            throw new IllegalArgumentException("Quran corpus must contain exactly 6236 verses");
        }
        Set<Long> seen = new HashSet<>();
        int previousSurah = 1;
        int previousAyah = 0;
        for (Verse verse : verses) {
            if (verse == null || verse.surah < 1 || verse.surah > 114
                    || verse.ayah < 1 || verse.ayah > expectedVerses(verse.surah)
                    || verse.arabic == null || verse.arabic.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid or empty Quran verse");
            }
            long key = ((long) verse.surah << 32) | verse.ayah;
            if (!seen.add(key)) throw new IllegalArgumentException("Duplicate Quran verse");
            if (verse.surah < previousSurah
                    || (verse.surah == previousSurah && verse.ayah != previousAyah + 1)
                    || (verse.surah > previousSurah
                        && (verse.surah != previousSurah + 1 || verse.ayah != 1
                            || previousAyah != expectedVerses(previousSurah)))) {
                throw new IllegalArgumentException("Out-of-order or missing Quran verse");
            }
            previousSurah = verse.surah;
            previousAyah = verse.ayah;
        }
        if (previousSurah != 114 || previousAyah != expectedVerses(114)) {
            throw new IllegalArgumentException("Incomplete Quran corpus");
        }
    }
}

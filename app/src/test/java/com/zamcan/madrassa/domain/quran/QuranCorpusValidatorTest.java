package com.zamcan.madrassa.domain.quran;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class QuranCorpusValidatorTest {
    private static List<QuranCorpusValidator.Verse> complete() {
        List<QuranCorpusValidator.Verse> verses = new ArrayList<>();
        for (int s = 1; s <= 114; s++) {
            for (int a = 1; a <= QuranCorpusValidator.expectedVerses(s); a++) {
                verses.add(new QuranCorpusValidator.Verse(s, a, "آية"));
            }
        }
        return verses;
    }

    @Test public void canonicalCounts() {
        assertEquals(114, QuranKnowledgeCatalog.surahCount());
        assertEquals(6236, QuranCorpusValidator.expectedTotalVerses());
        assertEquals(7, QuranCorpusValidator.expectedVerses(1));
        assertEquals(286, QuranCorpusValidator.expectedVerses(2));
        assertEquals(6, QuranCorpusValidator.expectedVerses(114));
    }

    @Test public void acceptsCompleteStructurallyValidCorpus() {
        QuranCorpusValidator.validate(complete());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingVerse() {
        List<QuranCorpusValidator.Verse> verses = complete();
        verses.remove(10);
        QuranCorpusValidator.validate(verses);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDuplicateVerse() {
        List<QuranCorpusValidator.Verse> verses = complete();
        verses.set(1, verses.get(0));
        QuranCorpusValidator.validate(verses);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutOfOrderVerses() {
        List<QuranCorpusValidator.Verse> verses = complete();
        java.util.Collections.swap(verses, 0, 1);
        QuranCorpusValidator.validate(verses);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankArabicText() {
        List<QuranCorpusValidator.Verse> verses = complete();
        verses.set(0, new QuranCorpusValidator.Verse(1, 1, " "));
        QuranCorpusValidator.validate(verses);
    }
}

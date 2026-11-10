package com.zamcan.madrassa.domain.quran;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class QuranAssetIntegrityTest {
    private static ByteArrayInputStream input(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    @Test public void sha256MatchesKnownVector() throws IOException {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                QuranAssetIntegrity.sha256(input("abc")));
    }

    @Test public void acceptsValidDigest() throws IOException {
        QuranAssetIntegrity.verify(input("abc"),
                "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
    }

    @Test(expected = SecurityException.class)
    public void rejectsTamperedAsset() throws IOException {
        QuranAssetIntegrity.verify(input("abd"),
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingDigest() throws IOException {
        QuranAssetIntegrity.verify(input("abc"), null);
    }
}

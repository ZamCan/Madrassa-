package com.zamcan.madrassa.domain.geography;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Small bundled fallback catalogue used when location permission is absent. */
public final class LocationCatalog {

    private LocationCatalog() {
    }

    public static LocationProfile defaultProfile() {
        return darEsSalaam();
    }

    public static LocationProfile darEsSalaam() {
        return new LocationProfile(
                "TZ",
                "Dar es Salaam",
                -6.7924,
                39.2083,
                3.0
        );
    }

    public static LocationProfile morogoro() {
        return new LocationProfile(
                "TZ",
                "Morogoro",
                -6.8278,
                37.6591,
                3.0
        );
    }

    public static LocationProfile nairobi() {
        return new LocationProfile(
                "KE",
                "Nairobi",
                -1.2921,
                36.8219,
                3.0
        );
    }

    public static LocationProfile kampala() {
        return new LocationProfile(
                "UG",
                "Kampala",
                0.3476,
                32.5825,
                3.0
        );
    }

    public static LocationProfile cairo() {
        return new LocationProfile(
                "EG",
                "Cairo",
                30.0444,
                31.2357,
                2.0
        );
    }

    public static List<LocationProfile> priorityLocations() {
        return Collections.unmodifiableList(Arrays.asList(
                darEsSalaam(),
                morogoro(),
                nairobi(),
                kampala(),
                cairo()
        ));
    }
}

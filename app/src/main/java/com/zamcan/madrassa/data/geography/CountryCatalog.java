package com.zamcan.madrassa.data.geography;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CountryCatalog {

    private static final String DEFAULT_COUNTRY = "TZ";

    private static final Set<String> PRIORITY_COUNTRIES =
            new HashSet<>();

    static {
        PRIORITY_COUNTRIES.add("TZ");
        PRIORITY_COUNTRIES.add("KE");
        PRIORITY_COUNTRIES.add("UG");

        /*
         * Arabic-country priority group.
         * The catalog will eventually contain the
         * complete world-country dataset.
         */
        PRIORITY_COUNTRIES.add("EG");
        PRIORITY_COUNTRIES.add("SA");
        PRIORITY_COUNTRIES.add("AE");
        PRIORITY_COUNTRIES.add("OM");
        PRIORITY_COUNTRIES.add("QA");
        PRIORITY_COUNTRIES.add("KW");
        PRIORITY_COUNTRIES.add("BH");
        PRIORITY_COUNTRIES.add("JO");
        PRIORITY_COUNTRIES.add("PS");
        PRIORITY_COUNTRIES.add("IQ");
        PRIORITY_COUNTRIES.add("SY");
        PRIORITY_COUNTRIES.add("YE");
        PRIORITY_COUNTRIES.add("LY");
        PRIORITY_COUNTRIES.add("TN");
        PRIORITY_COUNTRIES.add("DZ");
        PRIORITY_COUNTRIES.add("MA");
        PRIORITY_COUNTRIES.add("SD");
        PRIORITY_COUNTRIES.add("SO");
        PRIORITY_COUNTRIES.add("DJ");
        PRIORITY_COUNTRIES.add("KM");
        PRIORITY_COUNTRIES.add("MR");
    }

    private CountryCatalog() {
    }

    public static String getDefaultCountryCode() {
        return DEFAULT_COUNTRY;
    }

    public static List<Country> sort(
            List<Country> input
    ) {
        List<Country> result =
                new ArrayList<>(input);

        Collections.sort(
                result,
                new Comparator<Country>() {

                    @Override
                    public int compare(
                            Country a,
                            Country b
                    ) {
                        int rankA =
                                priorityRank(a);

                        int rankB =
                                priorityRank(b);

                        if (rankA != rankB) {
                            return Integer.compare(
                                    rankA,
                                    rankB
                            );
                        }

                        return a.name.compareToIgnoreCase(
                                b.name
                        );
                    }
                }
        );

        return result;
    }

    private static int priorityRank(
            Country country
    ) {
        if (country == null ||
                country.code == null) {
            return 1000;
        }

        if (DEFAULT_COUNTRY.equalsIgnoreCase(
                country.code
        )) {
            return 0;
        }

        if ("KE".equalsIgnoreCase(
                country.code
        )) {
            return 10;
        }

        if ("UG".equalsIgnoreCase(
                country.code
        )) {
            return 20;
        }

        if (PRIORITY_COUNTRIES.contains(
                country.code.toUpperCase()
        )) {
            return 30;
        }

        return 100;
    }
}

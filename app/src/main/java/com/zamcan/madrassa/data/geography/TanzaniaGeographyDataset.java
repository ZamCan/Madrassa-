package com.zamcan.madrassa.data.geography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Offline Tanzania geography for registration.
 *
 * Batch V7.3 — replaces the earlier empty stub so the
 * registration form can offer a real region -> district cascade
 * without any network access (local-first product rule).
 *
 * Coverage:
 *   REGION   complete — all 31 Tanzanian regions (26 mainland,
 *            5 Zanzibar).
 *   DISTRICT first-cut offline dataset for every region. The
 *            field stays editable, so a district that is not in
 *            this list can still be typed.
 *   WARD / LOCALITY intentionally not bundled yet: those levels
 *            return no children and the form falls back to free
 *            text until the authoritative dataset import.
 *
 * This file is data only — no product rules live here. Add a new
 * version of the data by extending the map, never by changing
 * the GeographyDataset contract.
 */
public class TanzaniaGeographyDataset implements GeographyDataset {

    private static final String COUNTRY_CODE = "TZ";

    /*
     * Region -> districts, in display order.
     * Keys are the region names a Madrassa administrator picks.
     */
    private static final Map<String, String[]> DISTRICTS =
            new LinkedHashMap<>();

    static {
        DISTRICTS.put("Arusha", new String[]{
                "Arusha DC", "Arusha MC", "Karatu",
                "Meru", "Monduli", "Ngorongoro"
        });

        DISTRICTS.put("Dar es Salaam", new String[]{
                "Ilala", "Kinondoni", "Kigamboni",
                "Temeke", "Ubungo"
        });

        DISTRICTS.put("Dodoma", new String[]{
                "Bahi", "Chamwino", "Chemba", "Dodoma DC",
                "Dodoma MC", "Kondoa", "Kongwa", "Mpwapwa"
        });

        DISTRICTS.put("Geita", new String[]{
                "Bukombe", "Chato", "Geita DC", "Geita TC",
                "Nyang'hwale"
        });

        DISTRICTS.put("Iringa", new String[]{
                "Iringa DC", "Iringa MC", "Kilolo", "Mafinga"
        });

        DISTRICTS.put("Kagera", new String[]{
                "Biharamulo", "Bukoba DC", "Bukoba MC",
                "Karagwe", "Kyerwa", "Muleba", "Ngara"
        });

        DISTRICTS.put("Katavi", new String[]{
                "Katavi", "Mlele", "Mpanda DC", "Mpanda TC"
        });

        DISTRICTS.put("Kigoma", new String[]{
                "Buhigwe", "Kakonko", "Kasulu DC", "Kasulu TC",
                "Kigoma DC", "Kigoma Ujiji TC"
        });

        DISTRICTS.put("Kilimanjaro", new String[]{
                "Hai", "Moshi DC", "Moshi MC", "Mwanga",
                "Rombo", "Same"
        });

        DISTRICTS.put("Lindi", new String[]{
                "Kilwa", "Lindi DC", "Lindi MC",
                "Nachingwea DC", "Nachingwea TC", "Ruangwa"
        });

        DISTRICTS.put("Manyara", new String[]{
                "Babati DC", "Babati TC", "Hanang", "Kiteto",
                "Mbulu", "Simanjiro"
        });

        DISTRICTS.put("Mara", new String[]{
                "Bunda", "Butiama", "Musoma DC", "Musoma MC",
                "Rorya", "Serengeti", "Tarime"
        });

        DISTRICTS.put("Mbeya", new String[]{
                "Busokelo", "Chunya", "Kyela", "Mbeya DC",
                "Mbeya MC", "Rungwe"
        });

        DISTRICTS.put("Morogoro", new String[]{
                "Gairo", "Kilosa", "Malinyi", "Morogoro DC",
                "Morogoro MC", "Mvomero", "Ulanga"
        });

        DISTRICTS.put("Mtwara", new String[]{
                "Masasi DC", "Masasi TC", "Mtwara DC",
                "Mtwara MC", "Newala", "Tandahimba"
        });

        DISTRICTS.put("Mwanza", new String[]{
                "Buchosa", "Ilemela", "Magu", "Mwanza DC",
                "Nyamagana", "Sengerema"
        });

        DISTRICTS.put("Njombe", new String[]{
                "Makambako TC", "Njombe DC", "Njombe MC",
                "Wanging'ombe"
        });

        DISTRICTS.put("Pwani", new String[]{
                "Bagamoyo", "Kibaha DC", "Kibaha TC", "Mafia",
                "Mkuranga", "Rufiji"
        });

        DISTRICTS.put("Rukwa", new String[]{
                "Kalambo", "Nkasi", "Sumbawanga DC",
                "Sumbawanga TC"
        });

        DISTRICTS.put("Ruvuma", new String[]{
                "Madaba", "Mbinga DC", "Mbinga TC", "Namtumbo",
                "Songea DC", "Songea MC", "Tunduru"
        });

        DISTRICTS.put("Shinyanga", new String[]{
                "Kahama DC", "Kahama TC", "Kishapu",
                "Shinyanga DC", "Shinyanga TC"
        });

        DISTRICTS.put("Simiyu", new String[]{
                "Bariadi DC", "Bariadi TC", "Itilima", "Maswa",
                "Meatu"
        });

        DISTRICTS.put("Singida", new String[]{
                "Ikungi", "Iramba", "Itigi", "Manyoni",
                "Singida DC", "Singida MC"
        });

        DISTRICTS.put("Songwe", new String[]{
                "Ileje", "Mbozi", "Momba", "Songwe DC"
        });

        DISTRICTS.put("Tabora", new String[]{
                "Igunga", "Kaliua", "Nzega DC", "Nzega TC",
                "Sikonge", "Tabora MC", "Urambo"
        });

        DISTRICTS.put("Tanga", new String[]{
                "Handeni DC", "Handeni TC", "Korogwe DC",
                "Korogwe TC", "Lushoto", "Muheza", "Pangani",
                "Tanga MC"
        });

        /* Zanzibar — Unguja */
        DISTRICTS.put("Kaskazini Unguja", new String[]{
                "Kaskazini A", "Kaskazini B"
        });

        DISTRICTS.put("Kusini Unguja", new String[]{
                "Kati", "Kusini"
        });

        DISTRICTS.put("Mjini Magharibi", new String[]{
                "Magharibi", "Mjini"
        });

        /* Zanzibar — Pemba */
        DISTRICTS.put("Kaskazini Pemba", new String[]{
                "Micheweni", "Wete"
        });

        DISTRICTS.put("Kusini Pemba", new String[]{
                "Chake Chake", "Pujini"
        });
    }

    private final List<LocationNode> regions =
            new ArrayList<>();

    private final Map<String, List<LocationNode>> districtsByRegion =
            new LinkedHashMap<>();

    public TanzaniaGeographyDataset() {

        for (Map.Entry<String, String[]> entry
                : DISTRICTS.entrySet()) {

            String regionName = entry.getKey();

            String regionId = COUNTRY_CODE
                    + "-REG-"
                    + slug(regionName);

            LocationNode region = new LocationNode(
                    regionId,
                    COUNTRY_CODE,
                    regionName,
                    COUNTRY_CODE,
                    AdministrativeLevel.REGION
            );

            regions.add(region);

            List<LocationNode> districts = new ArrayList<>();

            for (String districtName : entry.getValue()) {

                districts.add(
                        new LocationNode(
                                regionId
                                        + "-D-"
                                        + slug(districtName),
                                regionId,
                                districtName,
                                COUNTRY_CODE,
                                AdministrativeLevel.DISTRICT
                        )
                );
            }

            districtsByRegion.put(
                    regionId,
                    districts
            );
        }
    }

    private static String slug(String name) {

        String value = name
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-");

        if (value.endsWith("-")) {
            value = value.substring(0, value.length() - 1);
        }

        return value.toUpperCase();
    }

    @Override
    public String getCountryCode() {
        return COUNTRY_CODE;
    }

    @Override
    public List<LocationNode> getRootLocations() {
        return new ArrayList<>(regions);
    }

    @Override
    public List<LocationNode> getChildren(
            String parentId,
            AdministrativeLevel childLevel
    ) {

        if (parentId == null || childLevel == null) {
            return new ArrayList<>();
        }

        if (childLevel == AdministrativeLevel.DISTRICT) {

            List<LocationNode> districts =
                    districtsByRegion.get(parentId);

            if (districts == null) {
                return new ArrayList<>();
            }

            return new ArrayList<>(districts);
        }

        /*
         * WARD / LOCALITY are not bundled yet — the registration
         * form falls back to free text for those levels.
         */
        return new ArrayList<>();
    }

    @Override
    public List<AdministrativeLevel> getSupportedLevels() {
        return Arrays.asList(
                AdministrativeLevel.COUNTRY,
                AdministrativeLevel.REGION,
                AdministrativeLevel.DISTRICT,
                AdministrativeLevel.WARD,
                AdministrativeLevel.LOCALITY
        );
    }
}

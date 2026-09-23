package com.zamcan.madrassa.data.geography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalGeographyRepository
        implements GeographyRepository {

    private final List<Country> countries =
            new ArrayList<>();

    private final GeographyDatasetRegistry
            datasetRegistry;

    public LocalGeographyRepository() {

        datasetRegistry =
                new GeographyDatasetRegistry();

        countries.add(
                new Country(
                        "TZ",
                        "Tanzania",
                        true,
                        true,
                        true,
                        "+255"
                )
        );

        countries.add(
                new Country(
                        "KE",
                        "Kenya",
                        false,
                        true,
                        true,
                        "+254"
                )
        );

        countries.add(
                new Country(
                        "UG",
                        "Uganda",
                        false,
                        true,
                        true,
                        "+256"
                )
        );

        countries.add(
                new Country(
                        "EG",
                        "Egypt",
                        false,
                        true,
                        true,
                        "+20"
                )
        );
    }

    /*
     * Dialling prefix lookup for the phone country-code picker.
     * Returns null when the catalog has no such country or no
     * dialling prefix for it.
     */
    public Country findByDialCode(String dialCode) {

        if (dialCode == null || dialCode.trim().isEmpty()) {
            return null;
        }

        String wanted = dialCode.trim();

        for (Country country : countries) {

            if (wanted.equals(country.dialCode)) {
                return country;
            }
        }

        return null;
    }

    @Override
    public List<Country> getCountries() {
        return CountryCatalog.sort(countries);
    }

    @Override
    public List<LocationNode> getRootLocations(
            String countryCode
    ) {

        GeographyDataset dataset =
                datasetRegistry.get(countryCode);

        if (dataset == null) {
            return new ArrayList<>();
        }

        List<LocationNode> roots =
                dataset.getRootLocations();

        if (roots == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(roots);
    }

    @Override
    public List<LocationNode> getChildren(
            String countryCode,
            String parentId,
            AdministrativeLevel childLevel
    ) {

        if (countryCode == null ||
                parentId == null ||
                childLevel == null) {
            return new ArrayList<>();
        }

        GeographyDataset dataset =
                datasetRegistry.get(countryCode);

        if (dataset == null) {
            return new ArrayList<>();
        }

        List<LocationNode> children =
                dataset.getChildren(
                        parentId,
                        childLevel
                );

        if (children == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(children);
    }

    @Override
    public boolean hasStructuredData(
            String countryCode
    ) {

        if (countryCode == null) {
            return false;
        }

        for (Country country : countries) {

            if (country.code.equalsIgnoreCase(
                    countryCode
            )) {
                /*
                 * Structured data exists only when the catalog
                 * entry says so AND a local dataset is actually
                 * registered — a catalog promise alone must not
                 * switch the form into picker mode.
                 */
                return country.structuredDataAvailable
                        && datasetRegistry.hasDataset(
                                country.code
                        );
            }
        }

        return false;
    }

    @Override
    public List<AdministrativeLevel>
    getSupportedLevels(
            String countryCode
    ) {

        if (countryCode == null) {
            return new ArrayList<>();
        }

        GeographyDataset dataset =
                datasetRegistry.get(countryCode);

        if (dataset != null) {
            return new ArrayList<>(
                    dataset.getSupportedLevels()
            );
        }

        return Arrays.asList(
                AdministrativeLevel.COUNTRY,
                AdministrativeLevel.REGION,
                AdministrativeLevel.DISTRICT,
                AdministrativeLevel.LOCALITY
        );
    }
}

package com.zamcan.madrassa.data.geography;

import java.util.HashMap;
import java.util.Map;

public class GeographyDatasetRegistry {

    private final Map<String, GeographyDataset> datasets =
            new HashMap<>();

    public GeographyDatasetRegistry() {
        register(new TanzaniaGeographyDataset());
    }

    public void register(GeographyDataset dataset) {
        if (dataset == null ||
                dataset.getCountryCode() == null) {
            return;
        }

        datasets.put(
                dataset.getCountryCode().toUpperCase(),
                dataset
        );
    }

    public GeographyDataset get(
            String countryCode
    ) {
        if (countryCode == null) {
            return null;
        }

        return datasets.get(
                countryCode.toUpperCase()
        );
    }

    public boolean hasDataset(
            String countryCode
    ) {
        return get(countryCode) != null;
    }
}

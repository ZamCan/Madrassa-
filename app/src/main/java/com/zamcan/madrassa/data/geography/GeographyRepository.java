package com.zamcan.madrassa.data.geography;

import java.util.List;

public interface GeographyRepository {

    List<Country> getCountries();

    List<LocationNode> getRootLocations(
            String countryCode
    );

    List<LocationNode> getChildren(
            String countryCode,
            String parentId,
            AdministrativeLevel childLevel
    );

    boolean hasStructuredData(
            String countryCode
    );

    List<AdministrativeLevel> getSupportedLevels(
            String countryCode
    );
}

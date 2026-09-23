package com.zamcan.madrassa.data.geography;

import java.util.List;

public interface GeographyDataset {

    String getCountryCode();

    List<LocationNode> getRootLocations();

    List<LocationNode> getChildren(
            String parentId,
            AdministrativeLevel childLevel
    );

    List<AdministrativeLevel> getSupportedLevels();
}

package com.zamcan.madrassa.data.geography;

import java.util.ArrayList;
import java.util.List;

public class LocationNode {

    public String id;
    public String parentId;
    public String name;
    public String countryCode;
    public AdministrativeLevel level;

    public List<LocationNode> children =
            new ArrayList<>();

    public LocationNode() {
    }

    public LocationNode(
            String id,
            String parentId,
            String name,
            String countryCode,
            AdministrativeLevel level
    ) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.countryCode = countryCode;
        this.level = level;
    }
}

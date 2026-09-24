package com.zamcan.madrassa.domain.geography;

/** Immutable, offline location input for calendar and prayer calculations. */
public final class LocationProfile {

    public final String countryCode;
    public final String city;
    public final double latitude;
    public final double longitude;
    public final double utcOffsetHours;

    public LocationProfile(
            String countryCode,
            String city,
            double latitude,
            double longitude,
            double utcOffsetHours
    ) {
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90.0
                || latitude > 90.0
                || longitude < -180.0
                || longitude > 180.0
                || !Double.isFinite(utcOffsetHours)
                || utcOffsetHours < -12.0
                || utcOffsetHours > 14.0) {
            throw new IllegalArgumentException(
                    "location coordinates/offset are invalid"
            );
        }

        this.countryCode = countryCode == null ? "" : countryCode;
        this.city = city == null ? "" : city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.utcOffsetHours = utcOffsetHours;
    }

    @Override
    public String toString() {
        return (city.isEmpty() ? countryCode : city)
                + " (" + latitude + ", " + longitude + ")";
    }
}

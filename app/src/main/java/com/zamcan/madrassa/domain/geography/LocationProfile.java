package com.zamcan.madrassa.domain.geography;

/** Immutable location input used by local calendar and Salah calculations. */
public final class LocationProfile {
    public final String countryCode, city;
    public final double latitude, longitude, utcOffsetHours;
    public LocationProfile(String countryCode,String city,double latitude,double longitude,double utcOffsetHours){this.countryCode=countryCode;this.city=city;this.latitude=latitude;this.longitude=longitude;this.utcOffsetHours=utcOffsetHours;}
}

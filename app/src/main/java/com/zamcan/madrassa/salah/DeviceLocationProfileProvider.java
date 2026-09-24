package com.zamcan.madrassa.salah;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import com.zamcan.madrassa.domain.geography.LocationCatalog;
import com.zamcan.madrassa.domain.geography.LocationProfile;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Local-only device location adapter. Falls back to the configured Dar es Salaam profile. */
public final class DeviceLocationProfileProvider {
    private DeviceLocationProfileProvider() {}

    public static LocationProfile current(Context context) {
        LocationProfile fallback = LocationCatalog.darEsSalaam();
        if (context == null) return fallback;

        boolean fine = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) return fallback;

        try {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (manager == null) return fallback;

            Location best = null;
            if (fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                best = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (best == null && manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                best = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (best == null) return fallback;

            double offset = ZonedDateTime.now(ZoneId.systemDefault())
                    .getOffset().getTotalSeconds() / 3600.0;
            return new LocationProfile(
                    fallback.countryCode,
                    "Device",
                    best.getLatitude(),
                    best.getLongitude(),
                    offset
            );
        } catch (SecurityException | RuntimeException ignored) {
            return fallback;
        }
    }
}

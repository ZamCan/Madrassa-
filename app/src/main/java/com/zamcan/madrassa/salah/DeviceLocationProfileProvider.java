package com.zamcan.madrassa.salah;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import com.zamcan.madrassa.domain.geography.LocationProfile;

import java.util.List;
import java.util.TimeZone;

/**
 * Optional last-known device location provider.
 *
 * <p>It never requests permission or starts a blocking location update.
 * The UI can request permission separately; when permission is absent or
 * the platform has no last fix, the caller keeps its bundled fallback.</p>
 */
public final class DeviceLocationProfileProvider {

    private DeviceLocationProfileProvider() {
    }

    public static boolean hasPermission(Context context) {
        if (context == null) {
            return false;
        }

        return context.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static LocationProfile lastKnown(
            Context context
    ) {
        if (!hasPermission(context)) {
            return null;
        }

        LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        if (manager == null) {
            return null;
        }

        try {
            List<String> providers = manager.getProviders(true);
            if (providers == null) {
                return null;
            }

            Location best = null;

            for (String provider : providers) {
                Location candidate = manager.getLastKnownLocation(provider);
                if (candidate == null) {
                    continue;
                }
                if (best == null
                        || candidate.getTime() > best.getTime()) {
                    best = candidate;
                }
            }

            if (best == null) {
                return null;
            }

            double offset = TimeZone.getDefault().getOffset(
                    System.currentTimeMillis()
            ) / 3_600_000.0;

            return new LocationProfile(
                    "",
                    "",
                    best.getLatitude(),
                    best.getLongitude(),
                    offset
            );
        } catch (SecurityException denied) {
            return null;
        } catch (RuntimeException unavailable) {
            return null;
        }
    }
}

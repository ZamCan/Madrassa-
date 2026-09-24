package com.zamcan.madrassa.domain.salah;

/** Great-circle bearing from a coordinate to the Kaaba. */
public final class QiblaCalculator {

    private static final double KAABA_LATITUDE = 21.422487;
    private static final double KAABA_LONGITUDE = 39.826206;

    private QiblaCalculator() {
    }

    public static double bearingDegrees(
            double latitude,
            double longitude
    ) {
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90.0
                || latitude > 90.0
                || longitude < -180.0
                || longitude > 180.0) {
            throw new IllegalArgumentException(
                    "latitude/longitude are invalid"
            );
        }

        double from = Math.toRadians(latitude);
        double to = Math.toRadians(KAABA_LATITUDE);
        double deltaLongitude = Math.toRadians(
                KAABA_LONGITUDE - longitude
        );

        double y = Math.sin(deltaLongitude) * Math.cos(to);
        double x = Math.cos(from) * Math.sin(to)
                - Math.sin(from) * Math.cos(to)
                * Math.cos(deltaLongitude);

        return (Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0;
    }

    public static String cardinal(double bearing) {
        if (!Double.isFinite(bearing)) {
            return "—";
        }

        String[] directions = {
                "N", "NE", "E", "SE", "S", "SW", "W", "NW"
        };
        int index = (int) Math.round(
                ((bearing % 360.0 + 360.0) % 360.0) / 45.0
        ) % directions.length;
        return directions[index];
    }
}

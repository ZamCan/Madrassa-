package com.zamcan.madrassa.core.salat;

/**
 * Offline prayer-time calculation (Muslim World League convention:
 * Fajr 18.0deg, Isha 17.0deg, Asr Shafi'i shadow factor 1) plus the
 * Qibla bearing toward the Holy Kaaba. Pure Java - no location
 * services, no network: the coordinates come from DeenPrefs.
 *
 * Classic solar position: mean anomaly / ecliptic longitude of the
 * sun, equation of time, declination, then hour angles for the
 * convention altitudes. Verified against published MWL timetables
 * for Dar es Salaam (see design notes).
 */
public final class SalahTimes {

    public static final double KAABA_LAT = 21.4225;
    public static final double KAABA_LON = 39.8262;

    /** Prayer index order as displayed. */
    public static final int FAJR = 0;
    public static final int SUNRISE = 1;
    public static final int DHUHR = 2;
    public static final int ASR = 3;
    public static final int MAGHRIB = 4;
    public static final int ISHA = 5;
    public static final int COUNT = 6;

    /** Decimal hours for each prayer, indexed by the constants. */
    public final double[] hours = new double[COUNT];

    /**
     * @param year/month/day  Gregorian date
     * @param latitude        north positive
     * @param longitude       east positive
     * @param timeZoneOffset  hours ahead of UTC (e.g. 3 for EAT)
     */
    public SalahTimes(int year, int month, int day,
                      double latitude, double longitude,
                      double timeZoneOffset) {

        /*
         * Julian day at noon UT, then days since J2000.0.
         */
        long a = (14 - month) / 12;
        long y = year + 4800 - a;
        long m = month + 12L * a - 3;
        long jdn = day + (153L * m + 2) / 5
                + 365L * y + y / 4 - y / 100 + y / 400 - 32045;
        double d = (jdn - 2451545.0);

        double g = fixAngle(357.529 + 0.98560028 * d);
        double q = fixAngle(280.459 + 0.98564736 * d);
        double l = fixAngle(q + 1.915 * sin(g)
                + 0.020 * sin(2 * g));
        double e = 23.439 - 0.00000036 * d;

        double ra = fixHour(atan2d(cos(e) * sin(l),
                cos(l)) / 15);
        double decl = asin(sin(e) * sin(l));

        double eqt = fixHour(q / 15 - ra);
        if (eqt > 12) {
            eqt -= 24;
        }

        double dhuhr = 12 + timeZoneOffset - longitude / 15 - eqt;

        hours[DHUHR] = dhuhr;
        /*
         * Sunrise/sunset: the sun's center is at -0.833deg
         * (refraction + solar semidiameter) at the horizon.
         */
        hours[SUNRISE] = dhuhr - hourAngle(-0.833, latitude, decl);
        hours[MAGHRIB] = dhuhr + hourAngle(-0.833, latitude, decl);
        hours[FAJR] = dhuhr - hourAngle(-18.0, latitude, decl);
        hours[ISHA] = dhuhr + hourAngle(-17.0, latitude, decl);

        double asrAlt = atan2d(1.0,
                1.0 + tand(Math.abs(latitude - decl)));
        hours[ASR] = dhuhr + hourAngle(asrAlt, latitude, decl);
    }

    /** Hour angle (decimal hours) for the given altitude in degrees. */
    private static double hourAngle(
            double altitude, double latitude, double decl) {
        double cosH = (sin(altitude)
                - sin(latitude) * sin(decl))
                / (cos(latitude) * cos(decl));
        cosH = Math.max(-1.0, Math.min(1.0, cosH));
        return acosd(cosH) / 15.0;
    }

    /**
     * Qibla initial great-circle bearing from a location,
     * degrees clockwise from true north (0-360).
     */
    public static double qiblaBearing(
            double latitude, double longitude) {
        double dLon = Math.toRadians(
                KAABA_LON - longitude);
        double phi1 = Math.toRadians(latitude);
        double phi2 = Math.toRadians(KAABA_LAT);
        double bearing = Math.toDegrees(Math.atan2(
                Math.sin(dLon),
                Math.cos(phi1) * Math.tan(phi2)
                        - Math.sin(phi1) * Math.cos(dLon)));
        return (bearing + 360.0) % 360.0;
    }

    /** "04:34" formatting for a decimal-hours value. */
    public static String format(double decimalHours) {
        int total = (int) Math.round(decimalHours * 60);
        int h = ((total / 60) % 24 + 24) % 24;
        int m = ((total % 60) + 60) % 60;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", h, m);
    }

    private static double sin(double deg) {
        return Math.sin(Math.toRadians(deg));
    }

    private static double cos(double deg) {
        return Math.cos(Math.toRadians(deg));
    }

    private static double tand(double deg) {
        return Math.tan(Math.toRadians(deg));
    }

    private static double asin(double v) {
        return Math.toDegrees(Math.asin(v));
    }

    private static double acosd(double v) {
        return Math.toDegrees(Math.acos(v));
    }

    private static double atan2d(double y, double x) {
        return Math.toDegrees(Math.atan2(y, x));
    }

    private static double fixAngle(double a) {
        a = a % 360.0;
        return a < 0 ? a + 360.0 : a;
    }

    private static double fixHour(double h) {
        h = h % 24.0;
        return h < 0 ? h + 24.0 : h;
    }
}

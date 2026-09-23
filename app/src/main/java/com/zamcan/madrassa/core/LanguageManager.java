package com.zamcan.madrassa.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public final class LanguageManager {

    private static final String PREFS = "edunoor_settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "sw";

    private LanguageManager() {
    }

    public static String getLanguage(Context context) {
        return context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public static void setLanguage(Context context, String language) {
        context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, language)
                .apply();
    }

    public static Context wrap(Context context) {

        String language = getLanguage(context);

        Locale locale = Locale.forLanguageTag(language);
        Locale.setDefault(locale);

        Configuration configuration =
                new Configuration(context.getResources().getConfiguration());

        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }
}

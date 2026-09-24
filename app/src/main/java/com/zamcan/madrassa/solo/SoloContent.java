package com.zamcan.madrassa.solo;

import android.content.Context;

import com.zamcan.madrassa.core.LanguageManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Offline Solo Learning content library.
 *
 * The full trilingual curriculum (Wudu, Salah, Dhikr, Qur'an)
 * ships as assets/solo/content.json: every string carries all
 * three languages ({sw, en, ar}) so the in-app language switch
 * applies instantly without reloading anything. Audio files live beside
 * it in assets/solo/audio/. The app treats these
 * files as bundled lesson audio; it does not independently certify
 * them as authentic Qur'an recitation.
 */
public final class SoloContent {

    /** A learning step: visual + localized text + optional lesson audio. */
    public static final class Step {

        public final String image;
        public final String arabic;
        public final String translit;
        public final String audio;
        public final String title;
        public final String body;

        Step(JSONObject o, String lang) {
            image = o.optString("image", null);
            arabic = o.optString("arabic", "");
            translit = o.optString("translit", "");
            audio = o.optString("audio", null);
            title = text(o.optJSONObject("title"), lang);
            body = text(o.optJSONObject("body"), lang);
        }
    }

    /** A lesson = an ordered list of steps. */
    public static final class Lesson {

        public final String id;
        public final String title;
        public final String icon;
        public final List<Step> steps;

        Lesson(JSONObject o, String lang) {
            id = o.optString("id");
            title = text(o.optJSONObject("title"), lang);
            icon = o.optString("icon", null);
            steps = new ArrayList<>();
            JSONArray arr = o.optJSONArray("steps");
            for (int i = 0; arr != null && i < arr.length(); i++) {
                steps.add(new Step(arr.optJSONObject(i), lang));
            }
        }
    }

    /** A top learning area (Wudu / Salah / Qur'an). */
    public static final class Category {

        public final String id;
        public final String symbol;
        public final String title;
        public final String desc;
        public final List<Lesson> lessons;

        Category(JSONObject o, String lang) {
            id = o.optString("id");
            symbol = o.optString("symbol", "✦");
            title = text(o.optJSONObject("title"), lang);
            desc = text(o.optJSONObject("desc"), lang);
            lessons = new ArrayList<>();
            JSONArray arr = o.optJSONArray("lessons");
            for (int i = 0; arr != null && i < arr.length(); i++) {
                lessons.add(new Lesson(arr.optJSONObject(i), lang));
            }
        }
    }

    private final List<Category> categories = new ArrayList<>();

    public SoloContent(Context context) {
        String lang = LanguageManager.getLanguage(context);
        try (InputStream in =
                     context.getAssets().open("solo/content.json")) {

            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
            }

            JSONObject root = new JSONObject(sb.toString());
            JSONArray arr = root.optJSONArray("categories");
            for (int i = 0; arr != null && i < arr.length(); i++) {
                categories.add(new Category(arr.optJSONObject(i), lang));
            }
        } catch (Exception error) {
            // Content ships inside the APK; a read failure here would
            // be a packaging bug, surfaced as an empty library rather
            // than a crash.
        }
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Category category(String id) {
        for (Category category : categories) {
            if (category.id.equals(id)) {
                return category;
            }
        }
        return null;
    }

    /** Locale field pick with Swahili-first fallback chain. */
    static String text(JSONObject map, String lang) {
        if (map == null) {
            return "";
        }
        String value = map.optString(lang, "");
        if (value.isEmpty()) {
            value = map.optString("sw", "");
        }
        if (value.isEmpty()) {
            value = map.optString("en", "");
        }
        return value;
    }
}

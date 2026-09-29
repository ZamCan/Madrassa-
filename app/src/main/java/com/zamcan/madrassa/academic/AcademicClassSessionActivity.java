package com.zamcan.madrassa.academic;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Academic Class Studio.
 * Deliberately restrained: the academic content carries the identity;
 * decoration stays quiet so the screen feels like a professional
 * learning workspace rather than a themed poster.
 */
public final class AcademicClassSessionActivity extends Activity {
    private final List<JSONObject> lessons = new ArrayList<>();
    private LinearLayout content;
    private int current = 0;
    private String phase = "opening";

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private TextView text(String value, float size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(getColor(com.zamcan.madrassa.R.color.edunoor_walnut));
        t.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setIncludeFontPadding(false);
        t.setPadding(dp(2), dp(3), dp(2), dp(3));
        return t;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        load();
        build();
    }

    private void load() {
        try {
            InputStream in = getAssets().open("academic/foundation_curriculum.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) raw.append(line);
            JSONObject root = new JSONObject(raw.toString());
            JSONArray array = root.getJSONArray("lessons");
            for (int i = 0; i < array.length(); i++) lessons.add(array.getJSONObject(i));
        } catch (Exception ignored) {
            lessons.clear();
        }
    }

    private void build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(10), dp(18), dp(18));
        root.setBackgroundResource(com.zamcan.madrassa.R.drawable.edunoor_canvas);

        TextView back = text("‹", 28, false);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setOnClickListener(v -> finish());
        root.addView(back, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView title = text("Academic Class", 21, true);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView subtitle = text(
                "Teach • practise • assess • revise",
                11, false);
        subtitle.setTextColor(getColor(com.zamcan.madrassa.R.color.edunoor_muted));
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(28)));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(8), 0, dp(14));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        render();
    }

    private void render() {
        content.removeAllViews();
        if (lessons.isEmpty()) {
            content.addView(text("Academic knowledge pack unavailable.", 14, true));
            return;
        }

        JSONObject lesson = lessons.get(current);
        String subject = lesson.optString("subject", "").toUpperCase();
        JSONObject titleObject = lesson.optJSONObject("title");
        String title = titleObject == null ? lesson.optString("title") :
                titleObject.optString("sw", lesson.optString("title"));
        int duration = lesson.optInt("duration_minutes", 45);

        addHeader(subject, title, duration);
        addPhaseRail();
        addCurrentPhase(lesson);
        addObjectives(lesson);
        addAssessment(lesson);
        addKnowledge(lesson);
        addNavigation();
    }

    private void addHeader(String subject, String title, int duration) {
        LinearLayout card = card();
        TextView s = text(subject + "  ·  " + duration + " MIN", 10, true);
        s.setTextColor(getColor(com.zamcan.madrassa.R.color.edunoor_clay));
        card.addView(s);
        card.addView(text(title, 19, true), new LinearLayout.LayoutParams(-1, dp(34)));
        TextView meta = text("Ustadh session  ·  evidence of learning before mastery", 10, false);
        meta.setTextColor(getColor(com.zamcan.madrassa.R.color.edunoor_muted));
        card.addView(meta);
        content.addView(card, params(10));
    }

    private void addPhaseRail() {
        String[] phases = {"opening","revision","teach","demonstrate","practice","assessment","homework"};
        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.HORIZONTAL);
        rail.setPadding(0, 0, 0, dp(4));
        for (String p : phases) {
            TextView b = text(p.equals(phase) ? "• " + p : p, 8.5f, p.equals(phase));
            b.setGravity(Gravity.CENTER);
            b.setPadding(dp(4), dp(6), dp(4), dp(6));
            if (p.equals(phase)) b.setTextColor(getColor(com.zamcan.madrassa.R.color.edunoor_clay));
            b.setOnClickListener(v -> { phase = p; render(); });
            rail.addView(b, new LinearLayout.LayoutParams(0, dp(34), 1));
        }
        content.addView(rail, params(2));
    }

    private void addCurrentPhase(JSONObject lesson) {
        LinearLayout card = card();
        String body = "";
        try {
            if ("assessment".equals(phase)) {
                body = join(lesson.optJSONArray("assessment"));
            } else if ("homework".equals(phase)) {
                body = lesson.optString("homework");
            } else {
                JSONArray flow = lesson.optJSONArray("teacher_flow");
                for (int i = 0; i < flow.length(); i++) {
                    JSONObject item = flow.getJSONObject(i);
                    if (phase.equals(item.optString("phase"))) {
                        body = item.optString("sw") + "\n\n" + item.optString("en");
                        break;
                    }
                }
                if (body.isEmpty()) body = "Select a class phase above.";
            }
        } catch (Exception ignored) {}
        TextView label = text("CLASS PHASE  ·  " + phase.toUpperCase(), 9, true);
        label.setTextColor(getColor(com.zamcan.madrassa.R.color.edunoor_clay));
        card.addView(label);
        TextView bodyView = text(body, 14, false);
        bodyView.setPadding(0, dp(8), 0, 0);
        card.addView(bodyView);
        content.addView(card, params(10));
    }

    private void addObjectives(JSONObject lesson) {
        LinearLayout card = card();
        card.addView(text("LEARNING OBJECTIVES", 9, true));
        card.addView(text(join(lesson.optJSONArray("objectives")), 13, false));
        content.addView(card, params(10));
    }

    private void addAssessment(JSONObject lesson) {
        LinearLayout card = card();
        card.addView(text("EVIDENCE CHECK", 9, true));
        card.addView(text(join(lesson.optJSONArray("assessment")), 13, false));
        content.addView(card, params(10));
    }

    private void addKnowledge(JSONObject lesson) {
        LinearLayout card = card();
        card.addView(text("CORE KNOWLEDGE", 9, true));
        JSONObject k = lesson.optJSONObject("knowledge");
        if (k != null) {
            addLanguageBlock(card, "SW", k.optString("sw"));
            addLanguageBlock(card, "EN", k.optString("en"));
            addLanguageBlock(card, "AR", k.optString("ar"));
        }
        JSONArray refs = lesson.optJSONArray("references");
        if (refs != null) {
            card.addView(text("SOURCE NOTES", 9, true));
            for (int i = 0; i < refs.length(); i++) {
                JSONObject ref = refs.optJSONObject(i);
                if (ref == null) continue;
                card.addView(text(ref.optString("type") + "  ·  " + ref.optString("ref")
                        + (ref.optString("note").isEmpty() ? "" : "\n" + ref.optString("note")), 10, false));
            }
        }
        content.addView(card, params(10));
    }

    private void addLanguageBlock(LinearLayout card, String language, String value) {
        TextView label = text(language, 8.5f, true);
        label.setTextColor(getColor(com.zamcan.madrassa.R.color.edunoor_muted));
        label.setPadding(0, dp(7), 0, dp(2));
        card.addView(label);
        card.addView(text(value, 12.5f, false));
    }

    private void addNavigation() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        TextView previous = button("PREVIOUS");
        previous.setEnabled(current > 0);
        previous.setOnClickListener(v -> { current--; phase = "opening"; render(); });

        TextView next = button(current + 1 < lessons.size() ? "NEXT LESSON" : "END OF PACK");
        next.setEnabled(current + 1 < lessons.size());
        next.setOnClickListener(v -> { current++; phase = "opening"; render(); });

        row.addView(previous, new LinearLayout.LayoutParams(0, dp(46), 1));
        row.addView(next, new LinearLayout.LayoutParams(0, dp(46), 1));
        content.addView(row, params(10));
    }

    private TextView button(String label) {
        TextView b = text(label, 10, true);
        b.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(getColor(com.zamcan.madrassa.R.color.edunoor_surface));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), getColor(com.zamcan.madrassa.R.color.edunoor_border));
        b.setBackground(bg);
        return b;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15), dp(13), dp(15), dp(13));
        c.setBackgroundResource(com.zamcan.madrassa.R.drawable.edunoor_canvas);
        return c;
    }

    private LinearLayout.LayoutParams params(int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(bottom);
        return p;
    }

    private String join(JSONArray array) {
        if (array == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            if (b.length() > 0) b.append("\n\n");
            b.append("• ").append(array.optString(i));
        }
        return b.toString();
    }
}

package com.zamcan.madrassa.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.SoloLearningActivity;
import com.zamcan.madrassa.academic.AcademicClassSessionActivity;
import com.zamcan.madrassa.ui.components.EduNoorButton;

public final class DemoDashboardActivity extends Activity {
    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        return t;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        boolean parent = "parent".equals(getIntent().getStringExtra("demo_role"));
        int walnut = getColor(R.color.edunoor_walnut);
        int muted = getColor(R.color.edunoor_muted);
        int surface = getColor(R.color.edunoor_surface);

        getWindow().setStatusBarColor(surface);
        getWindow().setNavigationBarColor(surface);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(18), dp(20), dp(24));
        content.setBackgroundColor(surface);

        TextView back = text("‹", 30, walnut, true);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        content.addView(back, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView demo = text(getString(R.string.demo_badge), 11,
                getColor(R.color.edunoor_clay), true);
        demo.setGravity(Gravity.START);
        content.addView(demo, new LinearLayout.LayoutParams(-1, dp(28)));

        TextView title = text(
                parent ? getString(R.string.demo_parent_title)
                        : getString(R.string.demo_ustadh_title),
                25, walnut, true);
        content.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView scope = text(
                parent ? getString(R.string.demo_parent_scope)
                        : getString(R.string.demo_ustadh_scope),
                13, muted, false);
        scope.setPadding(0, 0, 0, dp(12));
        content.addView(scope, new LinearLayout.LayoutParams(-1, -2));

        if (parent) {
            card(content, getString(R.string.demo_child_card_title),
                    getString(R.string.demo_child_card_body));
            card(content, getString(R.string.demo_learning_card_title),
                    getString(R.string.demo_learning_card_body));
            card(content, getString(R.string.demo_fees_card_title),
                    getString(R.string.demo_fees_card_body));
        } else {
            card(content, getString(R.string.demo_class_card_title),
                    getString(R.string.demo_class_card_body));
            card(content, getString(R.string.demo_students_card_title),
                    getString(R.string.demo_students_card_body));
            card(content, getString(R.string.demo_academic_card_title),
                    getString(R.string.demo_academic_card_body));
            card(content, getString(R.string.demo_fees_card_title),
                    getString(R.string.demo_ustadh_fees_card_body));
        }

        TextView studio = EduNoorButton.primary(this, getString(R.string.demo_open_class_studio));
        studio.setOnClickListener(v -> startActivity(
                new Intent(this, AcademicClassSessionActivity.class)));
        content.addView(studio, buttonParams());

        TextView solo = EduNoorButton.secondary(this, getString(R.string.demo_open_solo));
        solo.setOnClickListener(v -> startActivity(
                new Intent(this, SoloLearningActivity.class)));
        content.addView(solo, buttonParams());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);
    }

    private void card(LinearLayout root, String title, String body) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(12), dp(15), dp(12));
        card.setBackgroundResource(R.drawable.edunoor_canvas);

        TextView a = text(title, 15, getColor(R.color.edunoor_walnut), true);
        TextView b = text(body, 12, getColor(R.color.edunoor_muted), false);
        b.setPadding(0, dp(5), 0, 0);
        card.addView(a, new LinearLayout.LayoutParams(-1, -2));
        card.addView(b, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(9);
        root.addView(card, p);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
        p.topMargin = dp(8);
        return p;
    }
}

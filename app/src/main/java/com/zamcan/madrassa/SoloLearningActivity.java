package com.zamcan.madrassa;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.ui.components.EduNoorCard;
import com.zamcan.madrassa.ui.components.EduNoorProgressView;
import com.zamcan.madrassa.ui.components.EduNoorStateView;

public class SoloLearningActivity extends Activity {

    private int dp(float value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    private TextView text(
            String value,
            float size,
            int color,
            boolean bold
    ) {
        TextView v = new TextView(this);

        v.setText(value == null ? "" : value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.START
        );

        v.setTypeface(
                Typeface.create(
                        "sans",
                        bold
                                ? Typeface.BOLD
                                : Typeface.NORMAL
                )
        );

        v.setIncludeFontPadding(true);

        return v;
    }

    private void pressEffect(View view) {
        view.setClickable(true);
        view.setFocusable(true);

        view.setOnTouchListener((v, event) -> {

            if (event.getAction() ==
                    android.view.MotionEvent.ACTION_DOWN) {

                v.animate()
                        .scaleX(0.985f)
                        .scaleY(0.985f)
                        .setDuration(70)
                        .start();

            } else if (
                    event.getAction() ==
                            android.view.MotionEvent.ACTION_UP ||
                    event.getAction() ==
                            android.view.MotionEvent.ACTION_CANCEL
            ) {

                v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start();
            }

            return false;
        });
    }

    private LinearLayout sectionTitle(
            String title,
            String description
    ) {

        LinearLayout section =
                new LinearLayout(this);

        section.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView titleView =
                text(
                        title,
                        16,
                        getColor(R.color.edunoor_walnut),
                        true
                );

        section.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        if (description != null &&
                !description.trim().isEmpty()) {

            TextView descriptionView =
                    text(
                            description,
                            10.5f,
                            getColor(R.color.edunoor_muted),
                            false
                    );

            LinearLayout.LayoutParams descriptionParams =
                    new LinearLayout.LayoutParams(
                            -1,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            descriptionParams.topMargin = dp(2);

            section.addView(
                    descriptionView,
                    descriptionParams
            );
        }

        return section;
    }

    /*
     * Saved EduNoor language for this screen too — Solo Learning
     * must follow the same Swahili/English/Arabic choice as the
     * landing page.
     */
    @Override
    protected void attachBaseContext(
            android.content.Context newBase
    ) {
        super.attachBaseContext(
                LanguageManager.wrap(newBase)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int background =
                getColor(R.color.edunoor_background);

        int surface =
                getColor(R.color.edunoor_surface);

        int walnut =
                getColor(R.color.edunoor_walnut);

        int clay =
                getColor(R.color.edunoor_clay);

        int muted =
                getColor(R.color.edunoor_muted);

        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(background);

        /*
         * =====================================================
         * HEADER
         * =====================================================
         */

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(16),
                dp(7),
                dp(16),
                dp(7)
        );

        TextView back =
                text(
                        "‹",
                        30,
                        clay,
                        false
                );

        back.setGravity(Gravity.CENTER);

        back.setOnClickListener(
                v -> finish()
        );

        pressEffect(back);

        header.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(48)
                )
        );

        LinearLayout headerWords =
                new LinearLayout(this);

        headerWords.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView heading =
                text(
                        getString(R.string.solo_learning_title),
                        16,
                        walnut,
                        true
                );

        TextView subtitle =
                text(
                        getString(R.string.solo_learning_subtitle),
                        10.5f,
                        muted,
                        false
                );

        headerWords.addView(
                heading,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        headerWords.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        header.addView(
                headerWords,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * =====================================================
         * SCROLLABLE LEARNING AREA
         * =====================================================
         */

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(20)
        );

        /*
         * INTRODUCTION
         */

        LinearLayout intro =
                new LinearLayout(this);

        intro.setOrientation(
                LinearLayout.VERTICAL
        );

        intro.setPadding(
                dp(18),
                dp(16),
                dp(18),
                dp(16)
        );

        intro.setBackground(
                getDrawable(R.drawable.edunoor_canvas)
        );

        TextView introTitle =
                text(
                        getString(R.string.solo_learning_welcome),
                        18,
                        walnut,
                        true
                );

        introTitle.setGravity(
                Gravity.CENTER_VERTICAL
        );

        intro.addView(
                introTitle,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        TextView introText =
                text(
                        getString(R.string.solo_learning_offline),
                        11.5f,
                        muted,
                        false
                );

        introText.setMaxLines(4);

        LinearLayout.LayoutParams introTextParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        introTextParams.topMargin = dp(6);

        intro.addView(
                introText,
                introTextParams
        );

        content.addView(
                intro,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * LEARNING AREAS
         */

        content.addView(
                sectionTitle(
                        getString(R.string.solo_learning_areas),
                        getString(R.string.solo_learning_areas_subtitle)
                ),
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout quran =
                EduNoorCard.create(
                        this,
                        getString(R.string.solo_quran_eyebrow),
                        getString(R.string.solo_quran_title),
                        getString(R.string.solo_quran_description),
                        "›"
                );

        quran.setOnClickListener(
                v -> showMessage(
                        getString(R.string.solo_quran_title),
                        getString(R.string.solo_quran_placeholder)
                )
        );

        /*
         * Cards wrap their own content now: the shared component
         * needs ~97dp for eyebrow + title + two description
         * lines, so a forced 82dp was clipping every description.
         */
        content.addView(
                quran,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout islamicStudies =
                EduNoorCard.create(
                        this,
                        getString(R.string.solo_islamic_eyebrow),
                        getString(R.string.solo_islamic_title),
                        getString(R.string.solo_islamic_description),
                        "›"
                );

        islamicStudies.setOnClickListener(
                v -> showMessage(
                        getString(R.string.solo_islamic_title),
                        getString(R.string.solo_content_placeholder)
                )
        );

        LinearLayout.LayoutParams islamicParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        islamicParams.topMargin = dp(10);

        content.addView(
                islamicStudies,
                islamicParams
        );

        LinearLayout books =
                EduNoorCard.create(
                        this,
                        getString(R.string.solo_books_eyebrow),
                        getString(R.string.solo_books_title),
                        getString(R.string.solo_books_description),
                        "›"
                );

        books.setOnClickListener(
                v -> showMessage(
                        getString(R.string.solo_books_title),
                        getString(R.string.solo_content_placeholder)
                )
        );

        LinearLayout.LayoutParams booksParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        booksParams.topMargin = dp(10);

        content.addView(
                books,
                booksParams
        );

        /*
         * PROGRESS
         */

        LinearLayout.LayoutParams progressSectionParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        progressSectionParams.topMargin = dp(18);

        content.addView(
                sectionTitle(
                        getString(R.string.solo_progress_title),
                        getString(R.string.solo_progress_subtitle)
                ),
                progressSectionParams
        );

        LinearLayout progress =
                EduNoorProgressView.create(
                        this,
                        getString(R.string.solo_progress_learning),
                        0,
                        0
                );

        content.addView(
                progress,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * STATE FOUNDATION
         *
         * This is deliberately a reusable UI state rather than
         * pretending that content has already been bundled.
         */

        LinearLayout state =
                EduNoorStateView.create(
                        this,
                        getString(R.string.solo_content_ready_title),
                        getString(R.string.solo_content_ready_message)
                );

        LinearLayout.LayoutParams stateParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        stateParams.topMargin = dp(14);

        content.addView(
                state,
                stateParams
        );

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    private void showMessage(
            String title,
            String message
    ) {

        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        getString(R.string.dialog_ok),
                        null
                )
                .show();
    }
}

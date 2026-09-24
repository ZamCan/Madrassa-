package com.zamcan.madrassa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.core.calendar.EduNoorCalendars;
import com.zamcan.madrassa.core.calendar.EduNoorDateFormatter;
import com.zamcan.madrassa.core.deen.DeenPrefs;
import com.zamcan.madrassa.solo.LessonActivity;
import com.zamcan.madrassa.solo.SoloContent;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.EduNoorCard;
import com.zamcan.madrassa.ui.components.EduNoorStateView;

import java.time.LocalDate;

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
        back.setContentDescription(
                getString(R.string.navigation_back)
        );

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
         * DUAL CALENDAR - every EduNoor day carries both systems:
         * Hijri above, Miladi (Gregorian) beneath, both from the
         * bundled Umm al-Qura engine, fully offline and localized.
         */
        root.addView(
                buildCalendarStrip(),
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

        LinearLayout tools = new LinearLayout(this);
        tools.setOrientation(LinearLayout.HORIZONTAL);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams toolsParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        toolsParams.topMargin = dp(10);
        toolsParams.bottomMargin = dp(4);

        TextView qibla = EduNoorButton.secondary(
                this,
                getString(R.string.solo_qibla)
        );
        qibla.setOnClickListener(v -> startActivity(
                new Intent(this, IslamicToolsActivity.class)
        ));
        tools.addView(qibla, new LinearLayout.LayoutParams(0, dp(48), 1));

        TextView salah = EduNoorButton.secondary(
                this,
                getString(R.string.solo_salah)
        );
        LinearLayout.LayoutParams salahParams =
                new LinearLayout.LayoutParams(0, dp(48), 1);
        salahParams.leftMargin = dp(7);
        salah.setOnClickListener(v -> startActivity(
                new Intent(this, IslamicToolsActivity.class)
        ));
        tools.addView(salah, salahParams);
        content.addView(tools, toolsParams);

        /*
         * =====================================================
         * LEARNING AREAS - real offline curriculum categories,
         * loaded from the bundled trilingual content library.
         * =====================================================
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

        SoloContent contentLibrary = new SoloContent(this);

        int categoryIndex = 0;

        for (SoloContent.Category category : contentLibrary.getCategories()) {

            LinearLayout card =
                    EduNoorCard.create(
                            this,
                            category.symbol + "  " + category.title,
                            category.title,
                            category.desc,
                            "›"
                    );

            card.setOnClickListener(
                    v -> openCategory(category)
            );

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            -1,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.topMargin = categoryIndex == 0 ? 0 : dp(10);

            content.addView(
                    card,
                    cardParams
            );

            categoryIndex++;
        }

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
                        getString(R.string.solo_content_unavailable)
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

        LinearLayout progressState =
                EduNoorStateView.create(
                        this,
                        getString(R.string.solo_progress_unavailable_title),
                        getString(R.string.solo_progress_unavailable_message)
                );

        content.addView(
                progressState,
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

    /*
     * =========================================================
     * DUAL CALENDAR STRIP
     * =========================================================
     */

    private LinearLayout buildCalendarStrip() {

        LocalDate today = LocalDate.now();
        EduNoorCalendars.HijriDate hijri =
                EduNoorCalendars.toHijri(
                        today.plusDays(
                                DeenPrefs.hijriAdjust(this)));

        String[] hijriMonths = getResources()
                .getStringArray(R.array.calendar_hijri_months);
        String[] gregorianMonths = getResources()
                .getStringArray(R.array.calendar_gregorian_months);

        String hijriLine =
                EduNoorDateFormatter.formatHijri(hijri, hijriMonths);
        String gregorianLine =
                EduNoorDateFormatter.formatGregorian(today, gregorianMonths);

        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.VERTICAL);
        strip.setGravity(Gravity.CENTER);
        strip.setPadding(dp(18), dp(12), dp(18), dp(12));
        strip.setBackground(getDrawable(R.drawable.edunoor_canvas));

        TextView hijriText = text("☾  " + hijriLine, 13,
                getColor(R.color.edunoor_gold_deep), true);
        hijriText.setGravity(Gravity.CENTER);

        TextView gregorianText = text(gregorianLine, 10.5f,
                getColor(R.color.edunoor_muted), false);
        gregorianText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams gregorianParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        gregorianParams.topMargin = dp(3);

        strip.addView(
                hijriText,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        strip.addView(gregorianText, gregorianParams);

        /*
         * The strip is the door to the full dual calendar page:
         * month grid with Hijri beneath every day, Ramadan
         * count-up/count-down, Ijumaa card and opt-in device
         * calendar events.
         */
        strip.setOnClickListener(v ->
                startActivity(new Intent(this,
                        com.zamcan.madrassa.core.calendar
                                .CalendarActivity.class)));

        pressEffect(strip);

        LinearLayout stripWrap = new LinearLayout(this);
        stripWrap.setOrientation(LinearLayout.VERTICAL);
        stripWrap.setPadding(dp(16), dp(8), dp(16), 0);
        stripWrap.addView(
                strip,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return stripWrap;
    }

    /*
     * =========================================================
     * LESSON ENTRY - single-lesson categories open directly;
     * multi-lesson ones present the bundled lesson list.
     * =========================================================
     */

    private void openCategory(SoloContent.Category category) {

        if (category.lessons.size() == 1) {
            openLesson(category, category.lessons.get(0));
            return;
        }

        String[] titles = new String[category.lessons.size()];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = category.lessons.get(i).title;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(category.symbol + "  " + category.title)
                .setItems(
                        titles,
                        (dialog, which) ->
                                openLesson(
                                        category,
                                        category.lessons.get(which)
                                )
                )
                .setNegativeButton(
                        getString(R.string.registration_cancel),
                        null
                )
                .show();
    }

    private void openLesson(
            SoloContent.Category category,
            SoloContent.Lesson lesson
    ) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra(LessonActivity.EXTRA_CATEGORY_ID, category.id);
        intent.putExtra(LessonActivity.EXTRA_LESSON_ID, lesson.id);
        startActivity(intent);
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

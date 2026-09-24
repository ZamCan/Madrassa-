package com.zamcan.madrassa.solo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.ui.components.EduNoorButton;

/**
 * One Solo Learning lesson, walked step by step.
 *
 * Every step shows: a posture/symbol visual (optional), the Arabic
 * line (RTL) where one exists, a transliteration, and the localized
 * meaning. Recitation audio plays straight from bundled assets, so
 * the whole lesson works with no network at all.
 */
public class LessonActivity extends Activity {

    public static final String EXTRA_LESSON_ID = "lesson_id";
    public static final String EXTRA_CATEGORY_ID = "category_id";

    private SoloContent.Category category;
    private SoloContent.Lesson lesson;

    private SoloAudioPlayer audioPlayer;

    private ImageView stepImage;
    private TextView arabicLine;
    private TextView translitLine;
    private TextView bodyLine;
    private TextView audioButton;
    private TextView stepCounter;
    private LinearLayout dotsRow;
    private TextView prevButton;
    private TextView nextButton;

    private int stepIndex = 0;

    private int dp(float value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private TextView text(String value, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    /*
     * Saved EduNoor language - this screen follows the same
     * Swahili/English/Arabic choice as the rest of the app.
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

        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        String categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String lessonId = getIntent().getStringExtra(EXTRA_LESSON_ID);

        SoloContent content = new SoloContent(this);
        category = content.category(categoryId);
        lesson = null;

        if (category != null) {
            for (SoloContent.Lesson candidate : category.lessons) {
                if (candidate.id.equals(lessonId)) {
                    lesson = candidate;
                    break;
                }
            }
        }

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(background);

        if (lesson == null || lesson.steps.isEmpty()) {
            // Content ships inside the APK; reaching here would be
            // a wiring bug, surfaced gently instead of crashing.
            LinearLayout fallback = new LinearLayout(this);
            fallback.setOrientation(LinearLayout.VERTICAL);
            fallback.setGravity(Gravity.CENTER);
            fallback.addView(
                    text(
                            getString(R.string.solo_content_placeholder),
                            13,
                            getColor(R.color.edunoor_walnut),
                            false
                    )
            );
            root.addView(
                    fallback,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    )
            );
            setContentView(root);
            return;
        }

        audioPlayer = new SoloAudioPlayer();
        buildUi(root);
        showStep(0);

        setContentView(root);
    }

    private void buildUi(LinearLayout root) {

        /*
         * =====================================================
         * HEADER - back chevron + category eyebrow + lesson title
         * =====================================================
         */
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(7), dp(16), dp(7));

        TextView back = text("‹", 30,
                getColor(R.color.edunoor_clay), false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        header.addView(
                back,
                new LinearLayout.LayoutParams(dp(42), dp(48))
        );

        LinearLayout headerWords = new LinearLayout(this);
        headerWords.setOrientation(LinearLayout.VERTICAL);

        TextView eyebrow = text(
                category.title + "  " + category.symbol,
                10.5f,
                getColor(R.color.edunoor_gold_deep),
                true
        );
        TextView heading = text(lesson.title, 16,
                getColor(R.color.edunoor_walnut), true);

        headerWords.addView(
                eyebrow,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        headerWords.addView(
                heading,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        header.addView(
                headerWords,
                new LinearLayout.LayoutParams(0, -1, 1)
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * =====================================================
         * SCROLLING STEP AREA on the parchment canvas
         * =====================================================
         */
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(10), dp(20), dp(18));

        LinearLayout stepCard = new LinearLayout(this);
        stepCard.setOrientation(LinearLayout.VERTICAL);
        stepCard.setGravity(Gravity.CENTER_HORIZONTAL);
        stepCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        stepCard.setBackground(
                getDrawable(R.drawable.edunoor_canvas)
        );

        stepImage = new ImageView(this);
        stepImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        stepCard.addView(
                stepImage,
                new LinearLayout.LayoutParams(dp(190), dp(190))
        );

        arabicLine = text("", 22,
                getColor(R.color.edunoor_ink), false);
        arabicLine.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams arabicParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        arabicParams.topMargin = dp(6);
        stepCard.addView(arabicLine, arabicParams);

        translitLine = text("", 12,
                getColor(R.color.edunoor_gold_deep), true);
        translitLine.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams translitParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        translitParams.topMargin = dp(8);
        stepCard.addView(translitLine, translitParams);

        bodyLine = text("", 13.5f,
                getColor(R.color.edunoor_ink_soft), false);
        bodyLine.setGravity(Gravity.CENTER);
        bodyLine.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams bodyParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        bodyParams.topMargin = dp(10);
        stepCard.addView(bodyLine, bodyParams);

        audioButton = EduNoorButton.primary(this,
                getString(R.string.solo_lesson_listen));
        LinearLayout.LayoutParams audioParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(46)
                );
        audioParams.topMargin = dp(16);
        audioButton.setOnClickListener(v -> toggleAudio());
        stepCard.addView(audioButton, audioParams);

        content.addView(
                stepCard,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * STEP PROGRESS DOTS
         */
        dotsRow = new LinearLayout(this);
        dotsRow.setGravity(Gravity.CENTER);
        dotsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams dotsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        dotsParams.topMargin = dp(14);
        content.addView(dotsRow, dotsParams);

        stepCounter = text("", 11,
                getColor(R.color.edunoor_muted), false);
        stepCounter.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams counterParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        counterParams.topMargin = dp(6);
        content.addView(stepCounter, counterParams);

        /*
         * PREV / NEXT NAVIGATION
         */
        LinearLayout navRow = new LinearLayout(this);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams navParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        navParams.topMargin = dp(12);

        prevButton = EduNoorButton.secondary(this,
                getString(R.string.solo_lesson_prev));
        nextButton = EduNoorButton.primary(this,
                getString(R.string.solo_lesson_next));

        LinearLayout.LayoutParams prevParams =
                new LinearLayout.LayoutParams(0, dp(46), 1);
        prevParams.rightMargin = dp(5);

        LinearLayout.LayoutParams nextParams =
                new LinearLayout.LayoutParams(0, dp(46), 1);
        nextParams.leftMargin = dp(5);

        prevButton.setOnClickListener(v -> {
            if (stepIndex > 0) {
                showStep(stepIndex - 1);
            }
        });
        nextButton.setOnClickListener(v -> {
            if (stepIndex < lesson.steps.size() - 1) {
                showStep(stepIndex + 1);
            }
        });

        navRow.addView(prevButton, prevParams);
        navRow.addView(nextButton, nextParams);
        content.addView(navRow, navParams);

        scroll.addView(
                content,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );
    }

    @SuppressLint("DiscouragedApi")
    private void showStep(int index) {

        audioPlayer.stop();
        stepIndex = index;
        SoloContent.Step step = lesson.steps.get(index);

        /*
         * Visual (optional - Qur'an steps carry recitation instead
         * of posture art by design).
         */
        if (step.image != null && !step.image.isEmpty()) {
            int resId = getResources().getIdentifier(
                    step.image, "drawable", getPackageName());
            if (resId != 0) {
                stepImage.setVisibility(View.VISIBLE);
                stepImage.setImageResource(resId);
            } else {
                stepImage.setVisibility(View.GONE);
            }
        } else {
            stepImage.setVisibility(View.GONE);
        }

        // Arabic line (RTL) - hidden when the step has none.
        if (!TextUtils.isEmpty(step.arabic)) {
            arabicLine.setVisibility(View.VISIBLE);
            arabicLine.setText(step.arabic);
            arabicLine.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        } else {
            arabicLine.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(step.translit)) {
            translitLine.setVisibility(View.VISIBLE);
            translitLine.setText(step.translit);
        } else {
            translitLine.setVisibility(View.GONE);
        }

        bodyLine.setText(step.body);

        // Recitation button only for steps that carry audio.
        if (step.audio != null && !step.audio.isEmpty()) {
            audioButton.setVisibility(View.VISIBLE);
            audioButton.setText(getString(R.string.solo_lesson_listen));
        } else {
            audioButton.setVisibility(View.GONE);
        }

        // Progress dots: filled for the current step.
        dotsRow.removeAllViews();
        for (int i = 0; i < lesson.steps.size(); i++) {
            TextView dot = new TextView(this);
            dot.setText(i == index ? "●" : "○");
            dot.setTextSize(11);
            dot.setTextColor(i == index
                    ? getColor(R.color.edunoor_gold_deep)
                    : getColor(R.color.edunoor_muted));
            dot.setPadding(dp(4), 0, dp(4), 0);
            dotsRow.addView(dot);
        }

        stepCounter.setText(getString(
                R.string.solo_lesson_step_of,
                index + 1,
                lesson.steps.size()
        ));

        prevButton.setEnabled(index > 0);
        prevButton.setAlpha(index > 0 ? 1f : 0.45f);

        boolean last = index == lesson.steps.size() - 1;
        nextButton.setText(getString(last
                ? R.string.solo_lesson_done
                : R.string.solo_lesson_next));
        if (last) {
            nextButton.setOnClickListener(v -> finish());
        }
    }

    private void toggleAudio() {

        SoloContent.Step step = lesson.steps.get(stepIndex);
        if (step.audio == null || step.audio.isEmpty()) {
            return;
        }

        if (audioPlayer.isPlaying(step.audio)) {
            audioPlayer.stop();
            audioButton.setText(getString(R.string.solo_lesson_listen));
        } else if (audioPlayer.play(this, step.audio)) {
            audioButton.setText(getString(R.string.solo_lesson_stop));
        } else {
            Toast.makeText(this,
                    getString(R.string.solo_lesson_audio_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioPlayer != null) {
            audioPlayer.stop();
            if (audioButton != null) {
                audioButton.setText(getString(R.string.solo_lesson_listen));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
    }
}

package com.zamcan.madrassa;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

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

        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER);

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

    /*
     * Professional thumb-friendly press behaviour.
     */
    private void pressEffect(View view) {

        view.setClickable(true);
        view.setFocusable(true);

        view.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        v.animate()
                                .scaleX(0.985f)
                                .scaleY(0.985f)
                                .setDuration(70)
                                .start();

                    } else if (
                            event.getAction() ==
                                    MotionEvent.ACTION_UP ||
                            event.getAction() ==
                                    MotionEvent.ACTION_CANCEL
                    ) {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start();
                    }

                    return false;
                }
        );
    }

    private LinearLayout roleCard(
            String title,
            String subtitle,
            boolean primary
    ) {

        int walnut =
                getColor(R.color.edunoor_walnut);

        int clay =
                getColor(R.color.edunoor_clay);

        int gold =
                getColor(R.color.edunoor_gold);

        int goldSoft =
                getColor(R.color.edunoor_gold_soft);

        int muted =
                getColor(R.color.edunoor_muted);

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        /*
         * Extra horizontal breathing room.
         */
        card.setPadding(
                dp(15),
                0,
                dp(9),
                0
        );

        card.setMinimumHeight(
                dp(68)
        );

        card.setBackgroundResource(
                primary
                        ? R.drawable.parent_card
                        : R.drawable.ustadh_card
        );

        TextView symbol =
                text(
                        primary ? "◈" : "◇",
                        19,
                        primary ? gold : clay,
                        true
                );

        card.addView(
                symbol,
                new LinearLayout.LayoutParams(
                        dp(40),
                        dp(52)
                )
        );

        LinearLayout words =
                new LinearLayout(this);

        words.setOrientation(
                LinearLayout.VERTICAL
        );

        words.setGravity(
                Gravity.CENTER_VERTICAL
        );

        words.setPadding(
                dp(7),
                0,
                dp(4),
                0
        );

        TextView titleView =
                text(
                        title,
                        15,
                        primary
                                ? Color.WHITE
                                : walnut,
                        true
                );

        titleView.setGravity(
                Gravity.LEFT |
                Gravity.CENTER_VERTICAL
        );

        TextView subtitleView =
                text(
                        subtitle,
                        10.5f,
                        primary
                                ? goldSoft
                                : muted,
                        false
                );

        subtitleView.setGravity(
                Gravity.LEFT |
                Gravity.CENTER_VERTICAL
        );

        words.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                )
        );

        words.addView(
                subtitleView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(21)
                )
        );

        card.addView(
                words,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        TextView arrow =
                text(
                        "›",
                        27,
                        primary
                                ? Color.WHITE
                                : clay,
                        false
                );

        arrow.setGravity(Gravity.CENTER);

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(32),
                        dp(52)
                )
        );

        pressEffect(card);

        card.setOnClickListener(
                v -> {

                    if (primary) {

                        Toast.makeText(
                                this,
                                "Parent access will open here.",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Ustadh access will open here.",
                                Toast.LENGTH_SHORT
                        ).show();

                    }
                }
        );

        return card;
    }

    /*
     * Small utility action.
     */
    private TextView utility(
            String value
    ) {

        TextView item =
                text(
                        value,
                        10,
                        getColor(
                                R.color.edunoor_muted
                        ),
                        false
                );

        item.setGravity(
                Gravity.CENTER
        );

        item.setMinimumHeight(
                dp(44)
        );

        pressEffect(item);

        return item;
    }

    private void showMessage(
            String title,
            String message
    ) {

        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    private void showLanguageDialog() {

        final String[] languages = {
                "Kiswahili",
                "English",
                "العربية"
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle(
                        getString(
                                R.string.language
                        )
                )
                .setItems(
                        languages,
                        (dialog, which) -> {

                            String selected =
                                    languages[which];

                            Toast.makeText(
                                    this,
                                    selected,
                                    Toast.LENGTH_SHORT
                            ).show();

                            /*
                             * Full runtime language switching
                             * will be connected in the
                             * localization stage.
                             */
                        }
                )
                .show();
    }

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        Window window = getWindow();

        int background =
                getColor(
                        R.color.edunoor_background
                );

        int surface =
                getColor(
                        R.color.edunoor_surface
                );

        int border =
                getColor(
                        R.color.edunoor_border
                );

        int walnut =
                getColor(
                        R.color.edunoor_walnut
                );

        int clay =
                getColor(
                        R.color.edunoor_clay
                );

        int gold =
                getColor(
                        R.color.edunoor_gold
                );

        int muted =
                getColor(
                        R.color.edunoor_muted
                );

        /*
         * SYSTEM BARS
         */
        window.setStatusBarColor(background);
        window.setNavigationBarColor(background);

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        /*
         * ROOT
         *
         * The root remains plain parchment.
         */
        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                background
        );

        root.setPadding(
                dp(16),
                dp(7),
                dp(16),
                dp(6)
        );

        /*
         * =====================================================
         * TOP IDENTITY
         * =====================================================
         */
        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        ImageView mark =
                new ImageView(this);

        mark.setImageResource(
                R.drawable.edunoor_icon
        );

        mark.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        header.addView(
                mark,
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(46)
                )
        );

        LinearLayout brand =
                new LinearLayout(this);

        brand.setOrientation(
                LinearLayout.VERTICAL
        );

        brand.setGravity(
                Gravity.CENTER_VERTICAL
        );

        brand.setPadding(
                dp(11),
                0,
                0,
                0
        );

        TextView appName =
                text(
                        "MADRASSA",
                        16,
                        walnut,
                        true
                );

        appName.setGravity(
                Gravity.LEFT |
                Gravity.CENTER_VERTICAL
        );

        TextView management =
                text(
                        getString(
                                R.string.brand_subtitle
                        ),
                        11,
                        clay,
                        true
                );

        management.setTextDirection(
                View.TEXT_DIRECTION_RTL
        );

        management.setGravity(
                Gravity.LEFT |
                Gravity.CENTER_VERTICAL
        );

        brand.addView(
                appName,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(22)
                )
        );

        brand.addView(
                management,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(18)
                )
        );

        header.addView(
                brand,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        TextView headerMark =
                text(
                        "✦",
                        15,
                        gold,
                        false
                );

        headerMark.setGravity(
                Gravity.CENTER
        );

        header.addView(
                headerMark,
                new LinearLayout.LayoutParams(
                        dp(32),
                        dp(44)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );

        /*
         * =====================================================
         * MIDDLE CANVAS
         * =====================================================
         */
        FrameLayout canvas =
                new FrameLayout(this);

        canvas.setBackgroundResource(
                R.drawable.edunoor_canvas
        );

        /*
         * Content is deliberately slightly lower.
         */
        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        content.setPadding(
                dp(17),
                dp(27),
                dp(17),
                dp(10)
        );

        /*
         * HERO GROUP
         */
        TextView bismillah =
                text(
                        getString(
                                R.string.greeting
                        ),
                        20,
                        clay,
                        true
                );

        bismillah.setTextDirection(
                View.TEXT_DIRECTION_RTL
        );

        content.addView(
                bismillah,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        TextView hero =
                text(
                        getString(
                                R.string.hero_title
                        ),
                        18,
                        walnut,
                        true
                );

        hero.setTextDirection(
                View.TEXT_DIRECTION_RTL
        );

        hero.setMaxLines(2);

        LinearLayout.LayoutParams heroParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        heroParams.topMargin =
                dp(4);

        content.addView(
                hero,
                heroParams
        );

        TextView tagline =
                text(
                        getString(
                                R.string.hero_description
                        ),
                        12.5f,
                        muted,
                        true
                );

        LinearLayout.LayoutParams taglineParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                );

        taglineParams.topMargin =
                dp(4);

        content.addView(
                tagline,
                taglineParams
        );

        /*
         * GOLD ORNAMENT
         */
        LinearLayout ornament =
                new LinearLayout(this);

        ornament.setGravity(
                Gravity.CENTER
        );

        TextView left =
                text(
                        "—",
                        12,
                        gold,
                        false
                );

        TextView diamond =
                text(
                        "◆",
                        8,
                        gold,
                        true
                );

        TextView right =
                text(
                        "—",
                        12,
                        gold,
                        false
                );

        ornament.addView(
                left,
                new LinearLayout.LayoutParams(
                        dp(35),
                        dp(20)
                )
        );

        ornament.addView(
                diamond,
                new LinearLayout.LayoutParams(
                        dp(16),
                        dp(20)
                )
        );

        ornament.addView(
                right,
                new LinearLayout.LayoutParams(
                        dp(35),
                        dp(20)
                )
        );

        LinearLayout.LayoutParams ornamentParams =
                new LinearLayout.LayoutParams(
                        dp(86),
                        dp(20)
                );

        ornamentParams.topMargin =
                dp(8);

        content.addView(
                ornament,
                ornamentParams
        );

        /*
         * =====================================================
         * ROLE CARDS
         * =====================================================
         */
        LinearLayout roles =
                new LinearLayout(this);

        roles.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams rolesParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(146)
                );

        rolesParams.topMargin =
                dp(13);

        rolesParams.bottomMargin =
                dp(0);

        LinearLayout parent =
                roleCard(
                        getString(
                                R.string.parent_title
                        ),
                        getString(
                                R.string.parent_subtitle
                        ),
                        true
                );

        roles.addView(
                parent,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(68)
                )
        );

        LinearLayout ustadh =
                roleCard(
                        getString(
                                R.string.ustadh_title
                        ),
                        getString(
                                R.string.ustadh_subtitle
                        ),
                        false
                );

        LinearLayout.LayoutParams ustadhParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(68)
                );

        ustadhParams.topMargin =
                dp(10);

        roles.addView(
                ustadh,
                ustadhParams
        );

        content.addView(
                roles,
                rolesParams
        );

        /*
         * =====================================================
         * FLEXIBLE SPACE
         *
         * Pushes EDU NOOR toward the bottom of
         * the middle canvas.
         * =====================================================
         */
        SpaceSpacer spacer =
                new SpaceSpacer(this);

        content.addView(
                spacer,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        /*
         * =====================================================
         * LOWER EDU NOOR IDENTITY
         * =====================================================
         */
        LinearLayout lower =
                new LinearLayout(this);

        lower.setGravity(
                Gravity.CENTER
        );

        TextView leftMark =
                text(
                        "۞",
                        14,
                        getColor(
                                R.color.edunoor_dusty_blue
                        ),
                        false
                );

        TextView middle =
                text(
                        "EDU NOOR",
                        9,
                        muted,
                        true
                );

        TextView rightMark =
                text(
                        "۞",
                        14,
                        getColor(
                                R.color.edunoor_teal
                        ),
                        false
                );

        lower.addView(
                leftMark,
                new LinearLayout.LayoutParams(
                        dp(28),
                        dp(24)
                )
        );

        lower.addView(
                middle,
                new LinearLayout.LayoutParams(
                        dp(75),
                        dp(24)
                )
        );

        lower.addView(
                rightMark,
                new LinearLayout.LayoutParams(
                        dp(28),
                        dp(24)
                )
        );

        content.addView(
                lower,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        canvas.addView(
                content,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        root.addView(
                canvas,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        /*
         * =====================================================
         * BOTTOM UTILITY BAR
         * =====================================================
         */
        LinearLayout bottom =
                new LinearLayout(this);

        bottom.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bottom.setGravity(
                Gravity.CENTER
        );

        bottom.setPadding(
                dp(3),
                0,
                dp(3),
                0
        );

        GradientDrawable bottomBg =
                new GradientDrawable();

        bottomBg.setColor(surface);
        bottomBg.setCornerRadius(
                dp(13)
        );

        bottomBg.setStroke(
                dp(1),
                border
        );

        bottom.setBackground(
                bottomBg
        );

        TextView language =
                utility(
                        getString(
                                R.string.language
                        )
                );

        language.setOnClickListener(
                v -> showLanguageDialog()
        );

        TextView privacy =
                utility(
                        getString(
                                R.string.privacy
                        )
                );

        privacy.setOnClickListener(
                v -> showMessage(
                        getString(
                                R.string.privacy
                        ),
                        "Your information is handled securely by Madrassa — EduNoor."
                )
        );

        TextView terms =
                utility(
                        getString(
                                R.string.terms
                        )
                );

        terms.setOnClickListener(
                v -> showMessage(
                        getString(
                                R.string.terms
                        ),
                        "Madrassa — EduNoor terms and conditions will be displayed here."
                )
        );

        TextView help =
                utility(
                        getString(
                                R.string.help
                        )
                );

        help.setOnClickListener(
                v -> showMessage(
                        getString(
                                R.string.help
                        ),
                        "For assistance, please contact your Madrassa administrator."
                )
        );

        bottom.addView(
                language,
                new LinearLayout.LayoutParams(
                        0,
                        dp(44),
                        1
                )
        );

        bottom.addView(
                privacy,
                new LinearLayout.LayoutParams(
                        0,
                        dp(44),
                        1
                )
        );

        bottom.addView(
                terms,
                new LinearLayout.LayoutParams(
                        0,
                        dp(44),
                        1
                )
        );

        bottom.addView(
                help,
                new LinearLayout.LayoutParams(
                        0,
                        dp(44),
                        1
                )
        );

        root.addView(
                bottom,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        setContentView(root);
    }

    /*
     * Lightweight spacer view.
     */
    private static class SpaceSpacer
            extends View {

        public SpaceSpacer(
                android.content.Context context
        ) {
            super(context);
        }
    }
}

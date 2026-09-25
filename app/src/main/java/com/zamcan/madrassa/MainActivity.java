package com.zamcan.madrassa;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.core.deen.DeenPrefs;
import com.zamcan.madrassa.ui.components.EduNoorCard;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zamcan.madrassa.core.calendar.EduNoorCalendars;
import com.zamcan.madrassa.core.calendar.EduNoorDateFormatter;

import java.time.LocalDate;

public class MainActivity extends Activity {

    private int dp(float value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    /*
     * Design-system spacing token accessor (values/dimens.xml).
     * Landing-page rhythm comes from the authored tokens instead
     * of scattered magic numbers, so the page grid has one
     * source of truth.
     */
    private int dim(int resource) {
        return getResources().getDimensionPixelSize(resource);
    }

    private TextView text(
            CharSequence value,
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

    /*
     * Gateway cards (Solo Learning / Parent / Ustadh).
     *
     * All three are built by the shared EduNoorCard.gateway()
     * component. New gateway surfaces use the current V7 glass
     * recipes; legacy card drawables are not used for this
     * production landing surface.
     *
     * Text wraps and the height is a minimum, not a fixed value,
     * so longer translations and larger system font scaling grow
     * the card instead of clipping it.
     */
    private LinearLayout soloCard() {

        LinearLayout card =
                EduNoorCard.gateway(
                        this,
                        "✦",
                        getColor(R.color.edunoor_teal),
                        R.drawable.solo_card,
                        getString(R.string.solo_card_title),
                        getColor(R.color.edunoor_walnut),
                        getString(R.string.solo_card_subtitle),
                        getColor(R.color.edunoor_muted),
                        getColor(R.color.edunoor_teal)
                );

        /*
         * 3F-A6: Solo stays independent and offline-first — the
         * card opens the learning area, never an account login.
         */
        card.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                SoloLearningActivity.class
                        )
                )
        );

        return card;
    }

    private LinearLayout roleCard(
            String title,
            String subtitle,
            boolean primary
    ) {

        LinearLayout card =
                EduNoorCard.gateway(
                        this,
                        primary ? "◈" : "◇",
                        primary
                                ? getColor(R.color.edunoor_gold)
                                : getColor(R.color.edunoor_clay),
                        primary
                                ? R.drawable.glass_card_primary
                                : R.drawable.glass_card_secondary,
                        title,
                        getColor(R.color.edunoor_text_light),
                        subtitle,
                        primary
                                ? getColor(R.color.edunoor_gold_soft)
                                : getColor(R.color.edunoor_text_faded),
                        primary
                                ? getColor(R.color.edunoor_gold_bright)
                                : getColor(R.color.edunoor_gold)
                );

        card.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                primary
                                        ? com.zamcan.madrassa.auth.parent.ParentLoginActivity.class
                                        : com.zamcan.madrassa.auth.ustadh.UstadhLoginActivity.class
                        )
                )
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
                        11,
                        getColor(
                                R.color.edunoor_muted
                        ),
                        false
                );

        item.setGravity(
                Gravity.CENTER
        );

        /* minimum 48dp touch target for the utility bar */
        item.setMinimumHeight(
                dp(48)
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
                        getString(R.string.dialog_ok),
                        null
                )
                .show();
    }

    /*
     * Header language toggle retired: the bare code chip ("SW")
     * was redundant with the footer language switcher and read as
     * visual noise in the identity bar. The footer item remains
     * the single, discoverable way to switch language.
     */

    private void showIslamicHub() {
        String[] items = {
                getString(R.string.landing_qibla),
                getString(R.string.landing_salah),
                getString(R.string.landing_adhan),
                getString(R.string.landing_calendar)
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.landing_islamic_hub))
                .setItems(items, (dialog, which) -> {
                    if (which == 3) {
                        startActivity(new Intent(
                                this,
                                com.zamcan.madrassa.core.calendar
                                        .CalendarActivity.class
                        ));
                    } else {
                        startActivity(new Intent(
                                this,
                                IslamicToolsActivity.class
                        ));
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show();
    }

    private void showLanguageDialog() {
        final String[] languageNames = {
                "Kiswahili",
                "English",
                "العربية"
        };

        final String[] languageCodes = {
                "sw",
                "en",
                "ar"
        };

        String current =
                LanguageManager.getLanguage(this);

        int checked = 0;

        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(current)) {
                checked = i;
                break;
            }
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(
                        getString(R.string.language)
                )
                .setSingleChoiceItems(
                        languageNames,
                        checked,
                        (dialog, which) -> {

                            String selected =
                                    languageCodes[which];

                            LanguageManager.setLanguage(
                                    this,
                                    selected
                            );

                            dialog.dismiss();

                            recreate();
                        }
                )
                .show();
    }

    /*
     * Applies the saved Swahili/English/Arabic locale to this
     * screen. Without this the language picker on the landing
     * page could not change anything here: getString() would
     * keep resolving against the device locale instead of the
     * user's EduNoor choice.
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

        /*
         * V7 brand canvas: cream girih pattern with gilded gold
         * artifacts (design/pipeline.py -> drawable-nodpi).
         */
        root.setBackgroundResource(
                R.drawable.edunoor_landing_bg
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
         *
         * Layout, leading to trailing:
         *   1. identity plate  — the brand mark, the wordmark
         *      MADRASSA and the Arabic "إدارة" directly beneath
         *      it, all carried on one milky-ivory plate that
         *      lifts off the girih canvas behind it. The plate
         *      owns its own background so the wordmark never
         *      competes with the pattern for legibility.
         *   2. Kaaba           — hung at the trailing edge on a
         *      raised, shadowed soil tile so it reads as a 3D
         *      object and stays a clear, clickable affordance.
         *
         * The bare language code ("SW") is intentionally gone
         * from this bar: the footer carries the full language
         * switcher, and a lone code here was visual noise.
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

        /*
         * ---- identity plate -------------------------------------
         */
        LinearLayout plate =
                new LinearLayout(this);

        plate.setOrientation(
                LinearLayout.HORIZONTAL
        );

        plate.setGravity(
                Gravity.CENTER_VERTICAL
        );

        plate.setPadding(
                dp(7),
                dp(6),
                dp(13),
                dp(6)
        );

        GradientDrawable plateBg =
                new GradientDrawable();

        plateBg.setColor(
                getColor(R.color.edunoor_plate)
        );

        plateBg.setCornerRadius(
                getResources().getDimensionPixelSize(
                        R.dimen.card_radius
                )
        );

        plateBg.setStroke(
                dp(1),
                getColor(R.color.edunoor_plate_edge)
        );

        plate.setBackground(plateBg);

        ImageView mark =
                new ImageView(this);

        mark.setImageResource(
                R.drawable.brand_icon
        );

        mark.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        mark.setContentDescription(
                getString(R.string.brand_title)
        );

        plate.addView(
                mark,
                new LinearLayout.LayoutParams(
                        dp(38),
                        dp(38)
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
                        getString(R.string.brand_title),
                        16,
                        walnut,
                        true
                );

        appName.setGravity(
                Gravity.START |
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
                Gravity.START |
                Gravity.CENTER_VERTICAL
        );

        brand.addView(
                appName,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        brand.addView(
                management,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        plate.addView(
                brand,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        header.addView(
                plate,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        /*
         * ---- Kaaba, hung at the trailing edge ------------------
         */
        FrameLayout hubTile =
                new FrameLayout(this);

        LinearLayout.LayoutParams hubTileParams =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                );

        /*
         * Hangs off the trailing edge of the bar so it reads as
         * a suspended 3D object, not a flat inline icon.
         */
        hubTileParams.gravity =
                Gravity.END | Gravity.CENTER_VERTICAL;

        hubTileParams.setMargins(
                0,
                dp(4),
                dp(-10),
                dp(4)
        );

        GradientDrawable hubBg =
                new GradientDrawable();

        hubBg.setColor(
                getColor(R.color.edunoor_soil)
        );

        hubBg.setCornerRadius(
                dp(17)
        );

        hubBg.setStroke(
                dp(1),
                getColor(R.color.edunoor_gold_deep)
        );

        hubTile.setBackground(hubBg);

        /*
         * A small lift gives the tile physical depth without a
         * heavy drop.
         */
        hubTile.setElevation(dp(8));

        ImageView islamicHub = new ImageView(this);

        islamicHub.setImageResource(R.drawable.edunoor_kaaba);

        islamicHub.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        islamicHub.setPadding(
                dp(9),
                dp(9),
                dp(9),
                dp(9)
        );

        hubTile.addView(
                islamicHub,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        /*
         * The whole tile is the target, not just the glyph, so
         * the touch area clears the 48dp accessibility minimum.
         */
        pressEffect(hubTile);

        hubTile.setOnClickListener(
                v -> showIslamicHub()
        );

        hubTile.setContentDescription(
                getString(R.string.landing_islamic_hub)
        );

        header.addView(
                hubTile,
                hubTileParams
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
         * MIDDLE CANVAS
         * =====================================================
         */
        FrameLayout canvas =
                new FrameLayout(this);

        canvas.setBackgroundResource(
                R.drawable.glass_panel
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
                dp(20),
                dp(17),
                dp(16)
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
                        LinearLayout.LayoutParams.WRAP_CONTENT
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
                        LinearLayout.LayoutParams.WRAP_CONTENT
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
                        LinearLayout.LayoutParams.WRAP_CONTENT
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

        /*
         * GOLD ORNAMENT — purely decorative: silent to
         * accessibility services, wrap-height cells so the
         * glyphs scale without clipping.
         */
        left.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        diamond.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        right.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        left.setMinimumHeight(dp(20));
        diamond.setMinimumHeight(dp(20));
        right.setMinimumHeight(dp(20));

        ornament.addView(
                left,
                new LinearLayout.LayoutParams(
                        dp(35),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        ornament.addView(
                diamond,
                new LinearLayout.LayoutParams(
                        dp(16),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        ornament.addView(
                right,
                new LinearLayout.LayoutParams(
                        dp(35),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout.LayoutParams ornamentParams =
                new LinearLayout.LayoutParams(
                        dp(86),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        ornamentParams.topMargin =
                dim(R.dimen.space_sm);

        content.addView(
                ornament,
                ornamentParams
        );

        /*
         * =====================================================
         * SOLO + ROLE CARDS
         *
         * 3F-A6:
         * Solo Learning is intentionally above the account
         * gateways. Solo remains independent and offline-first.
         * =====================================================
         */
        /*
         * Gateway cards share one geometry (68dp each) so no card
         * is cut on small screens. Height wraps content; side
         * margins centre the group on the page.
         */
        LinearLayout roles =
                new LinearLayout(this);

        roles.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams rolesParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        /*
         * Hero -> gateway-group gap uses the authored
         * role_area_gap token (15dp), replacing the ad-hoc 13dp
         * so the page rhythm matches the documented grid.
         */
        rolesParams.topMargin =
                dim(R.dimen.role_area_gap);

        rolesParams.bottomMargin =
                dp(0);

        rolesParams.leftMargin =
                dim(R.dimen.space_sm);

        rolesParams.rightMargin =
                dim(R.dimen.space_sm);

        LinearLayout solo =
                soloCard();

        roles.addView(
                solo,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

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

        LinearLayout.LayoutParams parentParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parentParams.topMargin =
                dim(R.dimen.role_card_gap);

        roles.addView(
                parent,
                parentParams
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
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        ustadhParams.topMargin =
                dim(R.dimen.role_card_gap);

        roles.addView(
                ustadh,
                ustadhParams
        );

        content.addView(
                roles,
                rolesParams
        );
        /*
         * Books ornament: the open Qur'an flanked by the sister
         * sciences (Tawheed, Fiqh, Hadith) as diamond-set covers.
         * Generated by design/pipeline.py (translucent gold alpha),
         * it rests in the flexible space between the gateway cards
         * and the EDU NOOR mark: purposeful Islamic identity,
         * silent to accessibility services, no layout risk (wrap
         * height inside the scrollable canvas).
         */
        ImageView booksOrnament =
                new ImageView(this);

        booksOrnament.setImageResource(
                R.drawable.ornament_books
        );

        booksOrnament.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        LinearLayout.LayoutParams booksParams =
                new LinearLayout.LayoutParams(
                        dp(264),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        booksParams.gravity =
                Gravity.CENTER_HORIZONTAL;

        booksParams.topMargin =
                dim(R.dimen.space_md);

        content.addView(
                booksOrnament,
                booksParams
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
                        getString(R.string.splash_brand),
                        10.5f,
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

        /*
         * Decorative brand row: the marks are silent to
         * accessibility services; the wordmark itself is real
         * text. Cells wrap and carry minimum heights so larger
         * font scales grow the row instead of clipping it.
         */
        leftMark.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        rightMark.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        leftMark.setMinimumHeight(dp(24));
        middle.setMinimumHeight(dp(24));
        rightMark.setMinimumHeight(dp(24));

        lower.addView(
                leftMark,
                new LinearLayout.LayoutParams(
                        dp(28),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        lower.addView(
                middle,
                new LinearLayout.LayoutParams(
                        dp(75),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        lower.addView(
                rightMark,
                new LinearLayout.LayoutParams(
                        dp(28),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        content.addView(
                lower,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * Hijri colophon line: one quiet line under the identity
         * mark carrying today's Islamic date only (Umm al-Qura
         * engine, offline). Gold crescent + faded text so it stays
         * silent against the dark glass.
         */
        EduNoorCalendars.HijriDate todayHijri =
                EduNoorCalendars.toHijri(
                        DeenPrefs.adjustedToday(this));

        String hijriLine = "☾  " + EduNoorDateFormatter.formatHijri(
                todayHijri,
                getResources()
                        .getStringArray(R.array.calendar_hijri_months)
        );

        SpannableString hijriStyled = new SpannableString(hijriLine);
        hijriStyled.setSpan(
                new ForegroundColorSpan(
                        getColor(R.color.edunoor_gold)
                ),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        TextView hijriColophon =
                text(
                        hijriStyled,
                        10.5f,
                        muted,
                        false
                );

        hijriColophon.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams hijriParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        hijriParams.topMargin =
                dim(R.dimen.space_sm);

        content.addView(
                hijriColophon,
                hijriParams
        );

        canvas.addView(
                content,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        /*
         * Scrollable middle canvas: on short screens the gateway
         * cards scroll instead of being cut; on tall screens the
         * spacer still pushes the EDU NOOR mark to the bottom.
         */
        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);

        scroll.addView(
                canvas,
                new ScrollView.LayoutParams(
                        -1,
                        -2
                )
        );

        root.addView(
                scroll,
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
                getResources()
                        .getDimensionPixelSize(
                                R.dimen.card_radius
                        )
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
                                R.string.language_switch_label
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
                        getString(
                                R.string.privacy_message
                        )
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
                        getString(
                                R.string.terms_message
                        )
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
                        getString(
                                R.string.help_message
                        )
                )
        );

        bottom.addView(
                language,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        bottom.addView(
                privacy,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        bottom.addView(
                terms,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        bottom.addView(
                help,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        /*
         * Wraps content instead of forcing 48dp: each column
         * already declares a 48dp minimum touch target, so the
         * bar stays 48dp tall normally but grows rather than
         * clipping a wrapped label at large font scales.
         */
        root.addView(
                bottom,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
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

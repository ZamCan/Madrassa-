package com.zamcan.madrassa.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zamcan.madrassa.R;

public final class EduNoorCard {

    private EduNoorCard() {
    }

    private static int dp(Context context, float value) {
        return (int) (
                value * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private static TextView text(
            Context context,
            String value,
            float size,
            int color,
            boolean bold
    ) {
        TextView view = new TextView(context);

        view.setText(value == null ? "" : value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.START
        );
        view.setTypeface(
                Typeface.create(
                        "sans",
                        bold
                                ? Typeface.BOLD
                                : Typeface.NORMAL
                )
        );
        view.setIncludeFontPadding(true);

        return view;
    }

    private static void pressEffect(View view) {
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
     * Gateway card used by the landing page for the three primary
     * pathways: Solo Learning, Parent and Ustadh.
     *
     * One geometry for all three (padding system, symbol column,
     * type scale, arrow column, press behaviour); identity comes
     * from the background drawable plus the accent colours, so
     * the cards stay one family while remaining recognisable.
     *
     * Title and subtitle wrap instead of using fixed row heights,
     * and the card itself only declares a minimum height from
     * R.dimen.role_card_height — longer translations and larger
     * system font scaling therefore grow the card instead of
     * clipping text. Text is start-aligned, so Arabic RTL mirrors
     * correctly.
     */
    public static LinearLayout gateway(
            Context context,
            String symbol,
            int symbolColor,
            int backgroundRes,
            String title,
            int titleColor,
            String subtitle,
            int subtitleColor,
            int arrowColor
    ) {

        LinearLayout card =
                new LinearLayout(context);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(context, 15),
                0,
                dp(context, 9),
                0
        );

        card.setMinimumHeight(
                context.getResources()
                        .getDimensionPixelSize(
                                R.dimen.role_card_height
                        )
        );

        card.setBackgroundResource(
                backgroundRes
        );

        TextView symbolView =
                text(
                        context,
                        symbol,
                        19,
                        symbolColor,
                        true
                );

        symbolView.setGravity(Gravity.CENTER);

        card.addView(
                symbolView,
                new LinearLayout.LayoutParams(
                        dp(context, 40),
                        dp(context, 52)
                )
        );

        LinearLayout words =
                new LinearLayout(context);

        words.setOrientation(
                LinearLayout.VERTICAL
        );

        words.setGravity(
                Gravity.CENTER_VERTICAL
        );

        words.setPadding(
                dp(context, 7),
                0,
                dp(context, 4),
                0
        );

        TextView titleView =
                text(
                        context,
                        title,
                        15,
                        titleColor,
                        true
                );

        TextView subtitleView =
                text(
                        context,
                        subtitle,
                        10.5f,
                        subtitleColor,
                        false
                );

        LinearLayout.LayoutParams titleParams =
                wrapContent();

        LinearLayout.LayoutParams subtitleParams =
                wrapContent();

        titleParams.topMargin = dp(context, 2);
        subtitleParams.bottomMargin = dp(context, 2);

        words.addView(titleView, titleParams);
        words.addView(subtitleView, subtitleParams);

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
                        context,
                        "›",
                        27,
                        arrowColor,
                        false
                );

        arrow.setGravity(Gravity.CENTER);

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(context, 32),
                        dp(context, 52)
                )
        );

        pressEffect(card);

        return card;
    }

    private static LinearLayout.LayoutParams wrapContent() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    public static LinearLayout create(
            Context context,
            String eyebrow,
            String title,
            String description,
            String trailing
    ) {

        int surface =
                context.getColor(
                        R.color.edunoor_surface
                );

        int walnut =
                context.getColor(
                        R.color.edunoor_walnut
                );

        int muted =
                context.getColor(
                        R.color.edunoor_muted
                );

        int gold =
                context.getColor(
                        R.color.edunoor_gold
                );

        int border =
                context.getColor(
                        R.color.edunoor_border
                );

        LinearLayout card =
                new LinearLayout(context);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(context, 16),
                dp(context, 11),
                dp(context, 11),
                dp(context, 11)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(surface);
        background.setCornerRadius(
                dp(context, 14)
        );
        background.setStroke(
                dp(context, 1),
                border
        );

        card.setBackground(background);

        LinearLayout words =
                new LinearLayout(context);

        words.setOrientation(
                LinearLayout.VERTICAL
        );

        words.setGravity(
                Gravity.CENTER_VERTICAL
        );

        if (eyebrow != null &&
                !eyebrow.trim().isEmpty()) {

            TextView eyebrowView =
                    text(
                            context,
                            eyebrow,
                            9.5f,
                            gold,
                            true
                    );

            LinearLayout.LayoutParams eyebrowParams =
                    new LinearLayout.LayoutParams(
                            -1,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            eyebrowParams.bottomMargin = dp(context, 2);

            words.addView(
                    eyebrowView,
                    eyebrowParams
            );
        }

        TextView titleView =
                text(
                        context,
                        title,
                        15,
                        walnut,
                        true
                );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(context, 1);

        words.addView(
                titleView,
                titleParams
        );

        if (description != null &&
                !description.trim().isEmpty()) {

            TextView descriptionView =
                    text(
                            context,
                            description,
                            10.5f,
                            muted,
                            false
                    );

            descriptionView.setMaxLines(2);

            LinearLayout.LayoutParams descriptionParams =
                    new LinearLayout.LayoutParams(
                            -1,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            descriptionParams.topMargin = dp(context, 3);

            words.addView(
                    descriptionView,
                    descriptionParams
            );
        }

        card.addView(
                words,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        TextView action =
                text(
                        context,
                        trailing == null
                                ? "›"
                                : trailing,
                        trailing == null
                                ? 26
                                : 11,
                        gold,
                        true
                );

        action.setGravity(
                Gravity.CENTER
        );

        card.addView(
                action,
                new LinearLayout.LayoutParams(
                        dp(context, 38),
                        -1
                )
        );

        pressEffect(card);

        return card;
    }
}

package com.zamcan.madrassa.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zamcan.madrassa.R;

public final class EduNoorProgressView {

    private EduNoorProgressView() {
    }

    private static int dp(Context context, float value) {
        return (int) (
                value * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    public static LinearLayout create(
            Context context,
            String label,
            int completed,
            int total
    ) {

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

        int track =
                context.getColor(
                        R.color.edunoor_surface_warm
                );

        LinearLayout root =
                new LinearLayout(context);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(context, 2),
                dp(context, 4),
                dp(context, 2),
                dp(context, 4)
        );

        LinearLayout heading =
                new LinearLayout(context);

        heading.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView labelView =
                new TextView(context);

        labelView.setText(
                label == null ? "" : label
        );

        labelView.setTextSize(11);
        labelView.setTextColor(walnut);
        labelView.setTypeface(
                Typeface.create(
                        "sans",
                        Typeface.BOLD
                )
        );

        TextView valueView =
                new TextView(context);

        int safeTotal =
                Math.max(0, total);

        int safeCompleted =
                Math.max(
                        0,
                        Math.min(
                                completed,
                                safeTotal
                        )
                );

        valueView.setText(
                safeCompleted +
                " / " +
                safeTotal
        );

        valueView.setTextSize(10);
        valueView.setTextColor(muted);
        valueView.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.END
        );

        heading.addView(
                labelView,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        /*
         * Fixed 55dp minimum keeps the counter aligned across
         * states, but the row itself now wraps instead of
         * clipping a longer translated label.
         */
        valueView.setMinWidth(
                dp(context, 55)
        );

        LinearLayout.LayoutParams valueParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        heading.addView(
                valueView,
                valueParams
        );

        LinearLayout.LayoutParams headingParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        headingParams.bottomMargin = dp(context, 4);

        root.addView(
                heading,
                headingParams
        );

        LinearLayout bar =
                new LinearLayout(context);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        GradientDrawable barBackground =
                new GradientDrawable();

        barBackground.setColor(track);
        barBackground.setCornerRadius(
                dp(context, 4)
        );

        bar.setBackground(barBackground);

        LinearLayout fill =
                new LinearLayout(context);

        fill.setBackground(
                createFill(
                        context,
                        gold
                )
        );

        int widthWeight =
                safeTotal == 0
                        ? 0
                        : safeCompleted;

        int remainingWeight =
                safeTotal == 0
                        ? 1
                        : safeTotal - safeCompleted;

        if (widthWeight > 0) {
            bar.addView(
                    fill,
                    new LinearLayout.LayoutParams(
                            0,
                            -1,
                            widthWeight
                    )
            );
        }

        if (remainingWeight > 0) {
            TextView empty =
                    new TextView(context);

            bar.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            0,
                            -1,
                            remainingWeight
                    )
            );
        }

        root.addView(
                bar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(context, 7)
                )
        );

        return root;
    }

    private static GradientDrawable createFill(
            Context context,
            int color
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(
                dp(context, 4)
        );

        return drawable;
    }
}

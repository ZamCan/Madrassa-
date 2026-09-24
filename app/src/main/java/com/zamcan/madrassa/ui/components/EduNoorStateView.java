package com.zamcan.madrassa.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zamcan.madrassa.R;

public final class EduNoorStateView {

    private EduNoorStateView() {
    }

    private static int dp(Context context, float value) {
        return (int) (
                value * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    public static LinearLayout create(
            Context context,
            String title,
            String message
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

        int border =
                context.getColor(
                        R.color.edunoor_border
                );

        LinearLayout root =
                new LinearLayout(context);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                dp(context, 24),
                dp(context, 24),
                dp(context, 24),
                dp(context, 24)
        );

        /*
         * Keeps the panel looking like a deliberate card even
         * when both lines are short, while still growing for
         * longer translations or larger system font scaling.
         */
        root.setMinimumHeight(
                dp(context, 130)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(surface);
        background.setCornerRadius(
                context.getResources()
                        .getDimensionPixelSize(
                                R.dimen.card_radius
                        )
        );
        background.setStroke(
                dp(context, 1),
                border
        );

        root.setBackground(background);

        TextView titleView =
                new TextView(context);

        titleView.setText(
                title == null ? "" : title
        );

        titleView.setTextSize(15);
        titleView.setTextColor(walnut);
        titleView.setTypeface(
                Typeface.create(
                        "sans",
                        Typeface.BOLD
                )
        );

        titleView.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams titleRowParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        titleRowParams.bottomMargin = dp(context, 8);

        root.addView(
                titleView,
                titleRowParams
        );

        TextView messageView =
                new TextView(context);

        messageView.setText(
                message == null ? "" : message
        );

        messageView.setTextSize(11);
        messageView.setTextColor(muted);
        messageView.setGravity(
                Gravity.CENTER
        );

        messageView.setMaxLines(6);

        root.addView(
                messageView,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return root;
    }
}

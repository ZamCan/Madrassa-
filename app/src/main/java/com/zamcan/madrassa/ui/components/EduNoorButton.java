package com.zamcan.madrassa.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.sound.EduNoorSounds;

public final class EduNoorButton {

    private EduNoorButton() {
    }

    private static int dp(Context context, float value) {
        return (int) (
                value * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    public static TextView primary(
            Context context,
            String label
    ) {
        return create(
                context,
                label,
                true
        );
    }

    public static TextView secondary(
            Context context,
            String label
    ) {
        return create(
                context,
                label,
                false
        );
    }

    /*
     * Locked/loading state for async submissions. The press guard
     * already blocks interaction; this makes the lock VISIBLE, so
     * "the button is working / unavailable" is perceivable and
     * every screen announces it the same way.
     */
    public static void setLocked(
            TextView button,
            boolean locked
    ) {
        button.setEnabled(!locked);
        button.setAlpha(locked ? 0.45f : 1f);
    }

    private static TextView create(
            Context context,
            String label,
            boolean primary
    ) {

        int walnut =
                context.getColor(
                        R.color.edunoor_walnut
                );

        int clay =
                context.getColor(
                        R.color.edunoor_clay
                );

        int gold =
                context.getColor(
                        R.color.edunoor_gold
                );

        int surface =
                context.getColor(
                        R.color.edunoor_surface
                );

        int border =
                context.getColor(
                        R.color.edunoor_border
                );

        TextView button =
                new TextView(context);

        button.setText(
                label == null ? "" : label
        );

        button.setTextSize(13);
        button.setTypeface(
                Typeface.create(
                        "sans",
                        Typeface.BOLD
                )
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setMinHeight(
                dp(context, 48)
        );

        button.setPadding(
                dp(context, 18),
                dp(context, 8),
                dp(context, 18),
                dp(context, 8)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setCornerRadius(
                dp(context, 12)
        );

        if (primary) {
            background.setColor(clay);
            button.setTextColor(
                    context.getColor(R.color.edunoor_white)
            );
        } else {
            background.setColor(surface);
            background.setStroke(
                    dp(context, 1),
                    border
            );
            button.setTextColor(walnut);
        }

        button.setBackground(background);

        button.setClickable(true);
        button.setFocusable(true);

        button.setOnTouchListener(
                (v, event) -> {

                    /*
                     * Disabled guard: an async login or
                     * submission locks the button, and a press
                     * during that window must do nothing at all
                     * — no press animation and no click.
                     */
                    if (!v.isEnabled()) {
                        return true;
                    }

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        EduNoorSounds.tap(context);

                        v.animate()
                                .scaleX(0.98f)
                                .scaleY(0.98f)
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

        return button;
    }
}

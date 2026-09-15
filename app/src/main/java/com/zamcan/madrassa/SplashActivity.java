package com.zamcan.madrassa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

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

        TextView v =
                new TextView(this);

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

        return v;
    }

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(
                getColor(
                        R.color.edunoor_walnut
                )
        );

        getWindow().setNavigationBarColor(
                getColor(
                        R.color.edunoor_walnut
                )
        );

        getWindow().getDecorView()
                .setSystemUiVisibility(0);

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        GradientDrawable background =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[] {
                                getColor(
                                        R.color.edunoor_walnut
                                ),
                                getColor(
                                        R.color.edunoor_clay_dark
                                ),
                                getColor(
                                        R.color.edunoor_walnut
                                )
                        }
                );

        root.setBackground(background);

        ImageView icon =
                new ImageView(this);

        icon.setImageResource(
                R.drawable.edunoor_icon
        );

        icon.setAlpha(0f);
        icon.setScaleX(0.82f);
        icon.setScaleY(0.82f);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(86),
                        dp(86)
                )
        );

        TextView bismillah =
                text(
                        getString(
                                R.string.greeting
                        ),
                        21,
                        getColor(
                                R.color.edunoor_gold_soft
                        ),
                        true
                );

        bismillah.setAlpha(0f);

        LinearLayout.LayoutParams bismillahParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(34)
                );

        bismillahParams.topMargin =
                dp(18);

        root.addView(
                bismillah,
                bismillahParams
        );

        TextView welcome =
                text(
                        "مَرْحَبًا بِكُمْ",
                        20,
                        Color.WHITE,
                        true
                );

        welcome.setAlpha(0f);

        LinearLayout.LayoutParams welcomeParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(38)
                );

        welcomeParams.topMargin =
                dp(3);

        root.addView(
                welcome,
                welcomeParams
        );

        TextView brand =
                text(
                        "EDU NOOR",
                        13,
                        getColor(
                                R.color.edunoor_gold
                        ),
                        true
                );

        brand.setAlpha(0f);

        root.addView(
                brand,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(26)
                )
        );

        TextView product =
                text(
                        "Madrassa Management",
                        11,
                        getColor(
                                R.color.edunoor_gold_soft
                        ),
                        false
                );

        product.setAlpha(0f);

        root.addView(
                product,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                )
        );

        setContentView(root);

        /*
         * Gentle professional entrance.
         */
        icon.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .start();

        bismillah.animate()
                .alpha(1f)
                .setStartDelay(220)
                .setDuration(350)
                .start();

        welcome.animate()
                .alpha(1f)
                .setStartDelay(320)
                .setDuration(350)
                .start();

        brand.animate()
                .alpha(1f)
                .setStartDelay(400)
                .setDuration(350)
                .start();

        product.animate()
                .alpha(1f)
                .setStartDelay(450)
                .setDuration(350)
                .start();

        /*
         * Short boot period.
         *
         * This will later become the real initialization
         * point for database/security/session checks.
         */
        new Handler().postDelayed(
                () -> {

                    Intent intent =
                            new Intent(
                                    SplashActivity.this,
                                    MainActivity.class
                            );

                    startActivity(intent);

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );

                    finish();

                },
                1100
        );
    }
}

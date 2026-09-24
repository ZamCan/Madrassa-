package com.zamcan.madrassa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.repository.MadrassaRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends Activity {

    /*
     * Minimum time the branded entrance is allowed to play.
     * This is animation pacing only — the status line below
     * is driven by the real initialization work, not by a
     * scripted timer.
     */
    private static final long ENTRANCE_MS = 900L;

    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    private final Handler ui = new Handler();

    private TextView stepView;

    private volatile boolean workFinished = false;
    private boolean entranceFinished = false;
    private boolean navigated = false;

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

        getWindow().setStatusBarColor(
                getColor(
                        R.color.edunoor_emerald_deep
                )
        );

        getWindow().setNavigationBarColor(
                getColor(
                        R.color.edunoor_emerald_deep
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

        /*
         * V7 brand canvas: emerald night gradient with glowing gold
         * artifacts (design/pipeline.py -> drawable-nodpi).
         */
        root.setBackgroundResource(
                R.drawable.edunoor_splash_bg
        );

        ImageView icon =
                new ImageView(this);

        icon.setImageResource(
                R.drawable.brand_icon
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
                        getString(R.string.splash_welcome),
                        20,
                        getColor(R.color.edunoor_white),
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
                        getString(R.string.splash_brand),
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
                        getString(R.string.splash_tagline),
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

        /*
         * REAL INITIALIZATION STATUS — each line shown here is
         * the work genuinely running at that moment (opening
         * the local store, loading records, checking their
         * status), rather than a synthetic progress bar.
         */
        stepView = text(
                "",
                10,
                getColor(R.color.edunoor_gold_soft),
                false
        );

        stepView.setAlpha(0f);

        LinearLayout.LayoutParams stepParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(24)
                );

        stepParams.topMargin = dp(10);

        root.addView(stepView, stepParams);

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

        stepView.animate()
                .alpha(1f)
                .setStartDelay(600)
                .setDuration(300)
                .start();

        /*
         * The boot period is no longer a fixed sleep: the
         * entrance plays while the real initialization below
         * runs in parallel, and the screen advances only when
         * both are finished.
         */
        ui.postDelayed(
                () -> {
                    entranceFinished = true;
                    advanceIfReady();
                },
                ENTRANCE_MS
        );

        worker.execute(this::runInitialization);
    }

    /*
     * Real startup work (spec §11). Every announce() call names
     * work that genuinely happens right after it — local-first,
     * on-device only, no network.
     */
    private void runInitialization() {

        String statusLine;

        try {
            /*
             * Step 1 — open the local store. On a fresh install
             * this creates (or migrates) the schema, which is
             * the real first cost of a session.
             */
            announce(R.string.init_step_database);

            EduNoorDatabase database =
                    new EduNoorDatabase(this);

            database.getWritableDatabase();

            /*
             * Step 2 — load the Madrassa records this device
             * actually holds, so the app starts from real data.
             */
            announce(R.string.init_step_madrassa);

            java.util.List<Madrassa> madrassas =
                    new MadrassaRepository(this).findAll();

            /*
             * Step 3 — report the approval status genuinely
             * stored for this device's Madrassa.
             */
            announce(R.string.init_step_ustadh_status);

            Madrassa current = madrassas.isEmpty()
                    ? null
                    : madrassas.get(0);

            statusLine = current == null ||
                    current.approvalStatus
                            == ApprovalStatus.ACTIVE
                    ? getString(R.string.init_session_ready)
                    : getString(
                            R.string.login_madrassa_inactive
                    );

        } catch (RuntimeException error) {
            /*
             * Local-first resilience: a storage problem must
             * degrade into an honest message, never a crash.
             */
            statusLine = getString(R.string.login_failed);
        }

        final String finalStatus = statusLine;

        ui.post(() -> {
            workFinished = true;

            stepView.setText(finalStatus);

            advanceIfReady();
        });
    }

    private void announce(int messageRes) {
        ui.post(() -> stepView.setText(getString(messageRes)));
    }

    /*
     * Advances only when both the real work and the designed
     * entrance are finished, so a fast device moves fast and a
     * slow device is never cut off mid-load.
     */
    private void advanceIfReady() {

        if (!workFinished
                || !entranceFinished
                || navigated) {
            return;
        }

        if (isFinishing() || isDestroyed()) {
            return;
        }

        navigated = true;

        startActivity(
                new Intent(
                        SplashActivity.this,
                        MainActivity.class
                )
        );

        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        worker.shutdownNow();
        ui.removeCallbacksAndMessages(null);
    }
}

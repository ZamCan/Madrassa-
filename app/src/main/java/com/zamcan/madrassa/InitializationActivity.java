package com.zamcan.madrassa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.ParentRepository;
import com.zamcan.madrassa.data.repository.UstadhRepository;
import com.zamcan.madrassa.domain.academic.AcademicCoreServiceFactory;
import com.zamcan.madrassa.domain.authorization.UstadhAccessPolicy;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.programme.ProgrammeSetupService;
import com.zamcan.madrassa.domain.session.ParentSession;
import com.zamcan.madrassa.domain.session.ParentSessionFactory;
import com.zamcan.madrassa.ui.components.EduNoorButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * EduNoor branded initialization transition (spec §11).
 *
 * Shown immediately after successful authentication. Each line
 * it displays is the real work being done at that moment —
 * opening the local SQLite store, loading the Madrassa record,
 * checking its approval status, or building the parent session
 * — not a scripted progress bar over a fixed sleep.
 *
 * The screen only advances once the work is finished (and the
 * designed entrance animation has played), so a fast device
 * moves fast and a slow device is never cut off mid-load.
 *
 * Local-first: everything here reads the on-device database.
 * No network is touched.
 */
public class InitializationActivity extends Activity {

    private static final String EXTRA_ROLE = "edunoor_role";
    private static final String EXTRA_MADRASSA_ID = "edunoor_madrassa_id";
    private static final String EXTRA_PARENT_ID = "edunoor_parent_id";
    private static final String EXTRA_USTADH_ID = "edunoor_ustadh_id";

    private static final String ROLE_USTADH = "ustadh";
    private static final String ROLE_PARENT = "parent";

    /*
     * Minimum time the branded entrance is allowed to play.
     * This is animation pacing only; the initialization work
     * itself reports its own real state below.
     */
    private static final long ENTRANCE_MS = 700L;

    public static Intent forUstadh(
            Context context,
            String madrassaId
    ) {
        return forUstadh(context, madrassaId, null);
    }

    public static Intent forUstadh(
            Context context,
            String madrassaId,
            String ustadhId
    ) {
        Intent intent =
                new Intent(context, InitializationActivity.class);

        intent.putExtra(EXTRA_ROLE, ROLE_USTADH);
        intent.putExtra(EXTRA_MADRASSA_ID, madrassaId);
        intent.putExtra(EXTRA_USTADH_ID, ustadhId);

        return intent;
    }

    public static Intent forParent(
            Context context,
            String parentId
    ) {
        Intent intent =
                new Intent(context, InitializationActivity.class);

        intent.putExtra(EXTRA_ROLE, ROLE_PARENT);
        intent.putExtra(EXTRA_PARENT_ID, parentId);

        return intent;
    }

    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    private final Handler ui = new Handler();

    private TextView stepView;
    private TextView karibu;
    private TextView madrassaNameView;
    private TextView roleView;
    private TextView statusView;
    private TextView noteView;
    private TextView continueButton;

    private LinearLayout readyPanel;

    private String role;
    private String madrassaId;
    private String parentId;
    private String ustadhId;

    private boolean sessionUsable;
    private volatile boolean workFinished = false;
    private boolean entranceFinished = false;

    @Override
    protected void attachBaseContext(
            android.content.Context newBase
    ) {
        super.attachBaseContext(
                LanguageManager.wrap(newBase)
        );
    }

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
        TextView view = new TextView(this);

        view.setText(value == null ? "" : value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);

        view.setTypeface(
                Typeface.create(
                        "sans",
                        bold ? Typeface.BOLD : Typeface.NORMAL
                )
        );

        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        role = getIntent().getStringExtra(EXTRA_ROLE);
        madrassaId = getIntent().getStringExtra(EXTRA_MADRASSA_ID);
        parentId = getIntent().getStringExtra(EXTRA_PARENT_ID);
        ustadhId = getIntent().getStringExtra(EXTRA_USTADH_ID);

        int walnut = getColor(R.color.edunoor_walnut);
        int gold = getColor(R.color.edunoor_gold);
        int goldSoft = getColor(R.color.edunoor_gold_soft);
        int clayDark = getColor(R.color.edunoor_clay_dark);

        getWindow().setStatusBarColor(walnut);
        getWindow().setNavigationBarColor(walnut);
        getWindow().getDecorView().setSystemUiVisibility(0);

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(26), dp(24), dp(26), dp(24));

        root.setBackgroundResource(
                R.drawable.edunoor_splash_bg
        );

        /*
         * BRAND IDENTITY — same mark and wordmark as the
         * splash screen, so startup and post-login share
         * one visual language.
         */
        ImageView icon = new ImageView(this);

        icon.setImageResource(R.drawable.brand_icon);
        icon.setScaleX(0.86f);
        icon.setScaleY(0.86f);
        icon.setAlpha(0f);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(76),
                        dp(76)
                )
        );

        TextView brand = text(
                getString(R.string.splash_brand),
                13,
                gold,
                true
        );

        brand.setAlpha(0f);

        LinearLayout.LayoutParams brandParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        brandParams.topMargin = dp(12);

        root.addView(brand, brandParams);

        /*
         * REAL INITIALIZATION STATUS
         *
         * Replaced line by line by the actual work below.
         */
        stepView = text(
                "",
                12,
                goldSoft,
                false
        );

        stepView.setAlpha(0f);

        LinearLayout.LayoutParams stepParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        stepParams.topMargin = dp(18);

        root.addView(stepView, stepParams);

        /*
         * WELCOME PANEL — revealed when the work above is
         * actually complete. For parents the Madrassa name is
         * the prominent element, as required by the spec.
         */
        readyPanel = new LinearLayout(this);

        readyPanel.setOrientation(LinearLayout.VERTICAL);
        readyPanel.setGravity(Gravity.CENTER);
        readyPanel.setVisibility(View.GONE);

        karibu = text(
                getString(R.string.init_welcome),
                14,
                goldSoft,
                false
        );

        madrassaNameView = text(
                "",
                22,
                getColor(R.color.edunoor_white),
                true
        );

        roleView = text(
                "",
                12,
                gold,
                true
        );

        LinearLayout.LayoutParams roleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        roleParams.topMargin = dp(4);

        statusView = text(
                "",
                11,
                goldSoft,
                false
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(10);

        noteView = text(
                getString(R.string.init_next_stage),
                11,
                goldSoft,
                false
        );

        noteView.setAlpha(0.85f);
        noteView.setMaxLines(3);

        LinearLayout.LayoutParams noteParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        noteParams.topMargin = dp(8);
        noteParams.leftMargin = dp(10);
        noteParams.rightMargin = dp(10);

        continueButton = EduNoorButton.primary(
                this,
                getString(R.string.dialog_ok)
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50)
                );

        buttonParams.topMargin = dp(18);

        readyPanel.addView(karibu);
        readyPanel.addView(madrassaNameView);
        readyPanel.addView(roleView, roleParams);
        readyPanel.addView(statusView, statusParams);
        readyPanel.addView(noteView, noteParams);
        readyPanel.addView(continueButton, buttonParams);

        LinearLayout.LayoutParams readyParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        readyParams.topMargin = dp(20);

        root.addView(readyPanel, readyParams);

        setContentView(root);

        continueButton.setOnClickListener(v -> {
            if (!sessionUsable) {
                return;
            }

            if (ROLE_PARENT.equals(role)
                    && parentId != null
                    && madrassaId != null) {
                startActivity(RoleDashboardActivity.forParent(
                        this,
                        parentId,
                        madrassaId
                ));
            } else if (ROLE_USTADH.equals(role)
                    && madrassaId != null
                    && ustadhId != null) {
                startActivity(RoleDashboardActivity.forUstadh(
                        this,
                        madrassaId,
                        ustadhId
                ));
            } else {
                return;
            }

            setResult(RESULT_OK);
            finish();
        });

        /*
         * Entrance — the brand marks settle in first; the work
         * below runs in parallel on a background thread.
         */
        icon.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(320).start();

        brand.animate().alpha(1f)
                .setStartDelay(120).setDuration(280).start();

        stepView.animate().alpha(1f)
                .setStartDelay(240).setDuration(260).start();

        ui.postDelayed(
                () -> {
                    entranceFinished = true;
                    revealIfReady();
                },
                ENTRANCE_MS
        );

        worker.execute(this::runInitialization);
    }

    /*
     * The real initialization sequence. Every announce() call
     * names work that is genuinely performed right after it.
     */
    private void runInitialization() {

        String madrassaName = null;
        String statusLine = "";
        boolean usable = false;

        try {
            if ((!ROLE_PARENT.equals(role)
                    && !ROLE_USTADH.equals(role))
                    || (ROLE_PARENT.equals(role)
                    && (parentId == null || parentId.trim().isEmpty()))
                    || (ROLE_USTADH.equals(role)
                    && (madrassaId == null
                    || madrassaId.trim().isEmpty()
                    || ustadhId == null
                    || ustadhId.trim().isEmpty()))) {
                throw new SecurityException("Invalid session scope.");
            }

            /*
             * Step 1 — open the local database. This creates or
             * upgrades the on-device schema, which is the real
             * first cost of a session on a fresh install.
             */
            announce(R.string.init_step_database);

            EduNoorDatabase database =
                    new EduNoorDatabase(this);

            database.getWritableDatabase();

            /*
             * Step 2 — load the record this session is about.
             */
            if (ROLE_PARENT.equals(role)) {

                announce(R.string.init_step_parent_account);

                Parent parent =
                        new ParentRepository(database)
                                .findById(parentId);

                if (parent == null || !parent.active) {
                    statusLine =
                            getString(R.string.login_invalid_credentials);
                } else {

                    announce(R.string.init_step_madrassa);

                    Madrassa madrassa =
                            new MadrassaRepository(this)
                                    .findById(parent.madrassaId);

                    if (madrassa == null
                            || !TenantPolicy.sameMadrassa(
                            parent.madrassaId,
                            madrassa.id
                    )
                            || madrassa.approvalStatus
                            != com.zamcan.madrassa.data.model
                            .ApprovalStatus.ACTIVE) {
                        statusLine =
                                getString(R.string.login_madrassa_inactive);
                    } else {
                        madrassaId = parent.madrassaId;
                        madrassaName = madrassa.name;

                        /*
                         * Step 3 — build the parent session, which
                         * validates the account and snapshots the
                         * students this parent may access.
                         */
                        announce(R.string.init_step_parent_session);

                        ParentSession session =
                                new ParentSessionFactory()
                                        .create(parent);

                        statusLine =
                                getString(
                                        R.string.init_session_ready
                                );
                        usable = true;
                    }
                }

            } else {

                announce(R.string.init_step_madrassa);

                Madrassa madrassa =
                        new MadrassaRepository(this)
                                .findById(madrassaId);

                Ustadh ustadh =
                        new UstadhRepository(getApplicationContext())
                                .findById(ustadhId);

                if (madrassa == null
                        || ustadh == null
                        || !UstadhAccessPolicy.belongsToMadrassa(
                        ustadh,
                        madrassa.id
                )) {
                    statusLine =
                            getString(R.string.login_invalid_credentials);
                } else {
                    madrassaName = madrassa.name;

                    /*
                     * Step 3 — confirm the Madrassa is actually
                     * approved and active on this device.
                     */
                    announce(R.string.init_step_ustadh_status);

                    if (!ustadh.active
                            || madrassa.approvalStatus
                            != com.zamcan.madrassa.data.model
                            .ApprovalStatus.ACTIVE) {

                        statusLine =
                                getString(
                                        R.string
                                                .login_madrassa_inactive
                                );
                    } else {
                        AcademicCoreServiceFactory.forMadrassa(
                                database,
                                madrassa.id
                        );
                        new ProgrammeSetupService(
                                new com.zamcan.madrassa.data.repository
                                        .ProgrammeRepository(database)
                        ).ensureMadrassaDefaults(madrassa.id);

                        statusLine =
                                getString(
                                        R.string
                                                .init_session_ready
                                );
                        usable = true;
                    }
                }
            }

        } catch (RuntimeException error) {
            /*
             * Local-first resilience: a storage problem must
             * degrade into an honest message, never a crash.
             */
            statusLine =
                    getString(R.string.login_failed);
        }

        final String finalName =
                madrassaName == null
                        ? getString(R.string.app_name)
                        : madrassaName;

        final String finalStatus = statusLine;
        final boolean finalUsable = usable;

        ui.post(() -> {
            workFinished = true;
            sessionUsable = finalUsable;

            madrassaNameView.setText(finalName);
            statusView.setText(finalStatus);
            continueButton.setEnabled(finalUsable);
            continueButton.setAlpha(finalUsable ? 1f : 0.45f);

            roleView.setText(
                    ROLE_PARENT.equals(role)
                            ? getString(R.string.parent_role)
                            : getString(R.string.ustadh_role)
            );

            revealIfReady();
        });
    }

    private void announce(int messageRes) {
        ui.post(() -> stepView.setText(getString(messageRes)));
    }

    /*
     * Shows the welcome panel only when both the real work and
     * the designed entrance have finished.
     */
    private void revealIfReady() {

        if (!workFinished || !entranceFinished) {
            return;
        }

        stepView.animate().alpha(0f).setDuration(200).start();

        readyPanel.setVisibility(View.VISIBLE);
        readyPanel.setAlpha(0f);
        readyPanel.animate()
                .alpha(1f)
                .setStartDelay(120)
                .setDuration(300)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        worker.shutdownNow();
        ui.removeCallbacksAndMessages(null);
    }
}

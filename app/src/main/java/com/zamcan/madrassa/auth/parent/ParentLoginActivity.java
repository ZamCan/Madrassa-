package com.zamcan.madrassa.auth.parent;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zamcan.madrassa.InitializationActivity;
import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.core.validation.EduNoorRules;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.repository.CredentialRepository;
import com.zamcan.madrassa.data.repository.ParentRepository;
import com.zamcan.madrassa.domain.auth.Pbkdf2PasswordVerifier;
import com.zamcan.madrassa.domain.auth.parent.ParentAuthenticationResult;
import com.zamcan.madrassa.domain.auth.parent.ParentAuthenticationService;
import com.zamcan.madrassa.domain.authorization.parent.ParentIdentityResolverImpl;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.FloatingOutlineField;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParentLoginActivity extends Activity {

    private FloatingOutlineField phone;
    private FloatingOutlineField password;
    private TextView loginButton;

    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    private boolean busy = false;

    private int dp(float value) {
        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
                + 0.5f
        );
    }

    private TextView text(
            String value,
            float size,
            int color,
            boolean bold
    ) {
        TextView t = new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        t.setTypeface(
                Typeface.create(
                        "sans",
                        bold
                                ? Typeface.BOLD
                                : Typeface.NORMAL
                )
        );

        t.setGravity(Gravity.CENTER);

        return t;
    }

    /*
     * Single-line text row that wraps its content. Rows used to
     * declare fixed heights, which clipped longer translations
     * and larger system font scaling.
     */
    private void addRow(LinearLayout root, TextView row) {
        root.addView(
                row,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int walnut = getColor(R.color.edunoor_walnut);
        final int clay = getColor(R.color.edunoor_clay);
        final int ivory = getColor(R.color.edunoor_surface);
        final int muted = getColor(R.color.edunoor_muted);
        final int gold = getColor(R.color.edunoor_gold);

        getWindow().setStatusBarColor(ivory);
        getWindow().setNavigationBarColor(ivory);

        /*
         * Keyboard behaviour: the form content lives in a
         * ScrollView (windowSoftInputMode=adjustResize), so when
         * the IME opens the window shrinks and the focused field
         * and the primary button stay reachable by scrolling
         * instead of being covered.
         */
        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setBackgroundColor(ivory);

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        root.setPadding(
                dp(24),
                dp(18),
                dp(24),
                dp(20)
        );

        root.setBackgroundColor(ivory);

        /*
         * BRAND
         */
        TextView brand = text(
                getString(R.string.brand_title),
                20,
                walnut,
                true
        );

        addRow(root, brand);

        TextView arabic = text(
                getString(R.string.brand_subtitle),
                15,
                getColor(R.color.edunoor_gold_deep),
                true
        );

        arabic.setTextDirection(
                View.TEXT_DIRECTION_RTL
        );

        addRow(root, arabic);

        TextView ornament = text(
                "✦",
                16,
                gold,
                false
        );

        ornament.setPadding(0, dp(8), 0, dp(4));

        addRow(root, ornament);

        /*
         * TITLE
         */
        TextView title = text(
                getString(R.string.parent_role),
                25,
                walnut,
                true
        );

        title.setPadding(0, dp(8), 0, 0);

        addRow(root, title);

        /*
         * DEV ONLY: long-press the title to fill the demo
         * credentials (see core/dev/DevSeed).
         */
        if (com.zamcan.madrassa.core.dev.DevSeed.ENABLED) {
            title.setOnLongClickListener(v -> {
                phone.setValue(
                        "0713 000 222");
                password.setValue(
                        com.zamcan.madrassa.core.dev.DevSeed
                                .PARENT_PASSWORD);
                android.widget.Toast.makeText(this,
                        getString(R.string.dev_filled),
                        android.widget.Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        TextView subtitle = text(
                getString(R.string.parent_login_subtitle),
                14,
                muted,
                false
        );

        subtitle.setPadding(
                0,
                dp(4),
                0,
                dp(10)
        );

        addRow(root, subtitle);

        /*
         * PHONE
         */
        phone = new FloatingOutlineField(this);

        phone.setLabel(
                getString(R.string.phone_number)
        );

        phone.setPhoneMode();

        phone.setCountryPrefix(
                EduNoorRules.DEFAULT_DIAL_CODE
        );

        phone.setExample(
                getString(R.string.field_example_phone)
        );

        /*
         * Wrapping height: the 58dp input plus room for the
         * inline validation line without clipping.
         */
        root.addView(
                phone,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        addGap(root, 12);

        /*
         * PASSWORD
         */
        password = new FloatingOutlineField(this);

        password.setLabel(
                getString(R.string.password)
        );

        password.setPasswordMode();
        password.setExample("••••••");

        /*
         * Keyboard "Done" submits the form, so the login can be
         * completed without leaving the keyboard. "Done"/"Next"
         * on the phone field advances to the password field so
         * the two-step form flows without dismissing the
         * keyboard.
         */
        password.setOnDoneAction(v -> attemptLogin());
        phone.setOnDoneAction(
                v -> password.getEditText().requestFocus()
        );

        root.addView(
                password,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * PASSWORD REQUIREMENT
         */
        TextView requirement = text(
                getString(R.string.parent_password_requirement),
                12,
                muted,
                false
        );

        requirement.setGravity(
                Gravity.START | Gravity.CENTER_VERTICAL
        );

        requirement.setPadding(
                dp(4),
                dp(3),
                dp(4),
                0
        );

        addRow(root, requirement);

        addGap(root, 8);

        /*
         * LOGIN — the shared EduNoor design-system button
         * instead of a platform-tinted widget, so the primary
         * action looks the same on every screen.
         */
        loginButton = EduNoorButton.primary(
                this,
                getString(R.string.login)
        );

        loginButton.setTextSize(15);

        root.addView(
                loginButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(54)
                )
        );

        /*
         * RESET
         */
        TextView reset = text(
                getString(R.string.forgot_password),
                14,
                clay,
                true
        );

        reset.setPadding(
                0,
                dp(13),
                0,
                0
        );

        reset.setClickable(true);

        addRow(root, reset);

        /*
         * PHONE INFORMATION
         */
        TextView info = text(
                getString(R.string.parent_phone_hint),
                12,
                muted,
                false
        );

        info.setPadding(
                0,
                dp(7),
                0,
                0
        );

        addRow(root, info);

        /*
         * REAL LOGIN
         *
         * Validation happens first, then the credential check
         * runs against the local repository layer on a
         * background thread (PBKDF2 is deliberately expensive),
         * and the result drives the branded initialization
         * transition on success.
         */
        loginButton.setOnClickListener(v -> attemptLogin());

        /*
         * PASSWORD RESET
         *
         * Event-based OTP will be connected later.
         */
        reset.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    getString(R.string.reset_v71_message),
                    Toast.LENGTH_SHORT
            ).show();
        });

        scroll.addView(
                root,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(scroll);
    }

    private void attemptLogin() {

        if (busy) {
            return;
        }

        String typed = phone.getValue().trim();
        String pass = password.getValue();

        phone.setErrorState(false);
        password.setErrorState(false);

        if (typed.isEmpty()) {

            phone.setErrorMessage(
                    getString(R.string.enter_phone)
            );
            phone.getEditText().requestFocus();

            return;
        }

        /*
         * Parent accounts belong to registered Madrassa phone
         * numbers, so the entered number must be a valid
         * number for the selected dialling prefix.
         */
        if (!EduNoorRules.validPhone(
                EduNoorRules.DEFAULT_DIAL_CODE,
                typed
        )) {

            phone.setErrorMessage(
                    getString(R.string.login_invalid_phone)
            );
            phone.getEditText().requestFocus();

            return;
        }

        if (pass.isEmpty()) {

            password.setErrorMessage(
                    getString(R.string.enter_password)
            );
            password.getEditText().requestFocus();

            return;
        }

        /*
         * Normalise once here: older accounts stored without
         * the +255 prefix still match, and the repository
         * layer receives one canonical form.
         */
        final String normalised =
                EduNoorRules.normalizePhone(
                        EduNoorRules.DEFAULT_DIAL_CODE,
                        typed
                );

        busy = true;
        EduNoorButton.setLocked(loginButton, true);
        loginButton.setText(
                getString(R.string.login_in_progress)
        );

        worker.execute(() -> {

            ParentAuthenticationResult result;

            try {

                ParentRepository parents =
                        new ParentRepository(
                                new EduNoorDatabase(
                                        getApplicationContext()
                                )
                        );

                ParentAuthenticationService service =
                        new ParentAuthenticationService(
                                new ParentIdentityResolverImpl(
                                        parents
                                ),
                                new CredentialRepository(
                                        new EduNoorDatabase(
                                                getApplicationContext()
                                        )
                                ),
                                parents,
                                new Pbkdf2PasswordVerifier()
                        );

                result = service.authenticate(
                        normalised,
                        pass
                );

            } catch (RuntimeException error) {
                result = ParentAuthenticationResult.failed();
            }

            final ParentAuthenticationResult outcome = result;

            runOnUiThread(() -> {

                busy = false;
                EduNoorButton.setLocked(loginButton, false);
                loginButton.setText(
                        getString(R.string.login)
                );

                handleLoginResult(outcome);
            });
        });
    }

    private void handleLoginResult(
            ParentAuthenticationResult result
    ) {

        if (result == null) {
            Toast.makeText(
                    this,
                    getString(R.string.login_failed),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        switch (result.status) {

            case SUCCESS:

                startActivity(
                        InitializationActivity.forParent(
                                this,
                                result.parent.id
                        )
                );

                break;

            case PASSWORD_CHANGE_REQUIRED:

                /*
                 * First login with an activation password: the
                 * guided password-change flow lands in the
                 * next stage (spec §10).
                 */
                Toast.makeText(
                        this,
                        getString(
                                R.string
                                        .login_password_change_required
                        ),
                        Toast.LENGTH_LONG
                ).show();

                break;

            case ACCOUNT_INACTIVE:

                Toast.makeText(
                        this,
                        getString(
                                R.string.login_account_inactive
                        ),
                        Toast.LENGTH_LONG
                ).show();

                break;

            case INVALID_CREDENTIALS:

                password.setErrorState(true);
                password.getEditText().requestFocus();

                Toast.makeText(
                        this,
                        getString(
                                R.string
                                        .login_invalid_credentials
                        ),
                        Toast.LENGTH_LONG
                ).show();

                break;

            default:

                Toast.makeText(
                        this,
                        getString(R.string.login_failed),
                        Toast.LENGTH_SHORT
                ).show();

                break;
        }
    }

    private void addGap(
            LinearLayout root,
            int heightDp
    ) {
        View gap = new View(this);

        root.addView(
                gap,
                new LinearLayout.LayoutParams(
                        1,
                        dp(heightDp)
                )
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        worker.shutdownNow();
    }
}

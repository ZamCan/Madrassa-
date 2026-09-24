package com.zamcan.madrassa.auth.ustadh;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.zamcan.madrassa.data.repository.MadrassaPhoneRepository;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.UstadhRepository;
import com.zamcan.madrassa.domain.auth.AuthenticationResult;
import com.zamcan.madrassa.domain.auth.Pbkdf2PasswordVerifier;
import com.zamcan.madrassa.domain.auth.UstadhAuthenticationService;
import com.zamcan.madrassa.domain.authorization.AccountIdentityResolverImpl;
import com.zamcan.madrassa.domain.authorization.MadrassaIdentityResolverImpl;
import com.zamcan.madrassa.domain.authorization.UstadhIdentifierPolicy;
import com.zamcan.madrassa.registration.MadrassaRegistrationActivity;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.FloatingOutlineField;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UstadhLoginActivity extends Activity {

    private FloatingOutlineField identifierField;
    private FloatingOutlineField passwordField;
    private TextView loginButton;

    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    private boolean busy = false;

    private int dp(float value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
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
                        bold ? Typeface.BOLD : Typeface.NORMAL
                )
        );
        t.setGravity(Gravity.CENTER);

        return t;
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

    /*
     * Wrapping text row: fixed-height rows clipped longer
     * translations and larger font scaling.
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

        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

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
                getString(R.string.ustadh_login_title),
                25,
                walnut,
                true
        );

        title.setPadding(0, dp(8), 0, 0);

        addRow(root, title);

        TextView subtitle = text(
                getString(R.string.ustadh_login_subtitle),
                14,
                muted,
                false
        );

        subtitle.setPadding(0, dp(4), 0, dp(10));

        addRow(root, subtitle);

        /*
         * FIELD 1
         *
         * One identifier:
         * Madrassa name OR phone number.
         */

        identifierField = new FloatingOutlineField(this);

        identifierField.setLabel(
                getString(R.string.ustadh_identifier)
        );

        identifierField.setTextMode();

        /*
         * The field accepts either the Madrassa name or its phone
         * number, so the in-box example shows both forms instead
         * of borrowing the phone-only example.
         */
        identifierField.setExample(
                getString(R.string.field_example_identifier)
        );

        /*
         * Wrapping height: the 58dp input plus room for the
         * inline validation line without clipping.
         */
        root.addView(
                identifierField,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        addGap(root, 12);

        /*
         * FIELD 2
         *
         * Password.
         */

        passwordField = new FloatingOutlineField(this);

        passwordField.setLabel(
                getString(R.string.password)
        );

        passwordField.setPasswordMode();

        /*
         * Keyboard "Done" submits the form. On the identifier
         * field it advances to the password field, keeping the
         * two-step flow inside one keyboard session.
         */
        passwordField.setOnDoneAction(v -> attemptLogin());
        identifierField.setOnDoneAction(
                v -> passwordField.getEditText().requestFocus()
        );

        root.addView(
                passwordField,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView requirement = text(
                getString(R.string.password_requirements_ustadh),
                12,
                muted,
                false
        );

        requirement.setGravity(
                Gravity.START | Gravity.CENTER_VERTICAL
        );

        requirement.setPadding(
                dp(4),
                dp(2),
                dp(4),
                0
        );

        addRow(root, requirement);

        addGap(root, 8);

        /*
         * LOGIN — shared EduNoor design-system button.
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

        reset.setPadding(0, dp(12), 0, 0);
        reset.setClickable(true);

        addRow(root, reset);

        /*
         * REGISTRATION PROMPT
         */

        TextView notRegistered = text(
                getString(R.string.not_registered),
                12,
                muted,
                false
        );

        addRow(root, notRegistered);

        /*
         * REGISTER MADRASSA — secondary design-system button.
         */

        TextView register = EduNoorButton.secondary(
                this,
                getString(R.string.register_madrassa)
        );

        root.addView(
                register,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(52)
                )
        );

        /*
         * REAL LOGIN ENTRY POINT
         *
         * Validation first, then the credential check runs
         * against the local repository layer on a background
         * thread (PBKDF2 is deliberately expensive).
         */

        loginButton.setOnClickListener(v -> attemptLogin());

        /*
         * PASSWORD RESET
         *
         * OTP recovery is implemented in the
         * authentication/security stage.
         */

        reset.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    getString(
                            R.string.reset_v71_message
                    ),
                    Toast.LENGTH_SHORT
            ).show();
        });

        /*
         * REAL REGISTRATION NAVIGATION
         */

        register.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            UstadhLoginActivity.this,
                            MadrassaRegistrationActivity.class
                    );

            startActivity(intent);
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

        final String typedIdentifier =
                identifierField.getValue().trim();

        final String pass =
                passwordField.getValue();

        identifierField.setErrorState(false);
        passwordField.setErrorState(false);

        if (typedIdentifier.isEmpty()) {

            identifierField.setErrorMessage(
                    getString(R.string.enter_ustadh_identifier)
            );
            identifierField.getEditText().requestFocus();

            return;
        }

        if (pass.isEmpty()) {

            passwordField.setErrorMessage(
                    getString(R.string.enter_password)
            );
            passwordField.getEditText().requestFocus();

            return;
        }

        final String identifier =
                normaliseIdentifier(typedIdentifier);

        /*
         * Deliberately NOT validated against the new-password
         * policy: this is an existing credential being
         * verified, not a password being created.
         */

        busy = true;
        EduNoorButton.setLocked(loginButton, true);
        loginButton.setText(
                getString(R.string.login_in_progress)
        );

        worker.execute(() -> {

            AuthenticationResult result;

            try {
                result = authenticationService()
                        .authenticate(identifier, pass);

            } catch (RuntimeException error) {
                result = AuthenticationResult.failed();
            }

            final AuthenticationResult outcome = result;

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

    /*
     * A national-format phone ("0712 345 678", "255 712 345 678")
     * is lifted to its international form so it matches the way
     * numbers are stored. An identifier that already carries an
     * international prefix (or is a Madrassa name) is passed
     * through untouched — a foreign +254 number must never be
     * re-written as a +255 one.
     */
    private String normaliseIdentifier(String value) {

        if (!UstadhIdentifierPolicy.looksLikePhone(value)) {
            return value;
        }

        if (value.startsWith("+")
                || value.startsWith("00")) {
            return value;
        }

        return EduNoorRules.normalizePhone(
                EduNoorRules.DEFAULT_DIAL_CODE,
                value
        );
    }

    private UstadhAuthenticationService authenticationService() {

        EduNoorDatabase database =
                new EduNoorDatabase(
                        getApplicationContext()
                );

        MadrassaRepository madrassaRepository =
                new MadrassaRepository(
                        getApplicationContext()
                );

        UstadhRepository ustadhRepository =
                new UstadhRepository(
                        getApplicationContext()
                );

        return new UstadhAuthenticationService(
                new AccountIdentityResolverImpl(
                        new MadrassaIdentityResolverImpl(
                                madrassaRepository,
                                new MadrassaPhoneRepository(
                                        getApplicationContext()
                                ),
                                ustadhRepository
                        ),
                        ustadhRepository
                ),
                new CredentialRepository(database),
                madrassaRepository,
                ustadhRepository,
                new Pbkdf2PasswordVerifier()
        );
    }

    private void handleLoginResult(
            AuthenticationResult result
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
                        InitializationActivity.forUstadh(
                                this,
                                result.madrassa.id,
                                result.ustadh != null
                                        ? result.ustadh.id
                                        : null
                        )
                );

                break;

            case PASSWORD_CHANGE_REQUIRED:

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

            case MADRASSA_INACTIVE:

                Toast.makeText(
                        this,
                        getString(
                                R.string
                                        .login_madrassa_inactive
                        ),
                        Toast.LENGTH_LONG
                ).show();

                break;

            case INVALID_CREDENTIALS:

                passwordField.setErrorState(true);
                passwordField.getEditText().requestFocus();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        worker.shutdownNow();
    }
}

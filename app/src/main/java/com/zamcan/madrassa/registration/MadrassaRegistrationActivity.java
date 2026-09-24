package com.zamcan.madrassa.registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.core.validation.EduNoorRules;
import com.zamcan.madrassa.data.geography.AdministrativeLevel;
import com.zamcan.madrassa.data.geography.Country;
import com.zamcan.madrassa.data.geography.LocalGeographyRepository;
import com.zamcan.madrassa.data.geography.LocationNode;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.PendingRegistrationCredentialRepository;
import com.zamcan.madrassa.data.repository.RegistrationTransactionRepository;
import com.zamcan.madrassa.domain.auth.Pbkdf2PasswordVerifier;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.registration.RegistrationService;
import com.zamcan.madrassa.domain.registration.RegistrationSubmission;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.FloatingOutlineField;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MadrassaRegistrationActivity extends Activity {

    private static final String COUNTRY_CODE = "TZ";

    private static final int STEP_IDENTITY = 1;
    private static final int STEP_ADMINISTRATION = 2;
    private static final int STEP_SECURITY = 3;
    private static final int STEP_REVIEW = 4;
    private static final int STEP_SUBMITTED = 5;

    private int currentStep = STEP_IDENTITY;

    private Madrassa madrassa;

    /*
     * Offline location source: drives the region -> district
     * cascade in step 1 and the country-code list on the phone
     * fields in step 2. No network, no cloud lookup.
     */
    private LocalGeographyRepository geography;

    private String selectedRegionId;
    private String selectedDial =
            EduNoorRules.DEFAULT_DIAL_CODE;

    private ScrollView scroll;
    private LinearLayout content;
    private TextView stepLabel;
    private TextView title;
    private TextView subtitle;
    private TextView backButton;
    private TextView nextButton;

    /*
     * Submission hashes the initial password with PBKDF2
     * (210,000 iterations), so it always runs on this worker
     * thread and never on the main thread. While it runs the
     * Next button is disabled, so a double tap cannot send the
     * same registration twice.
     */
    private final ExecutorService submitWorker =
            Executors.newSingleThreadExecutor();

    private boolean submitting = false;

    private FloatingOutlineField madrassaName;
    private FloatingOutlineField type;
    private FloatingOutlineField administrationType;
    private FloatingOutlineField region;
    private FloatingOutlineField district;
    private FloatingOutlineField ward;
    private FloatingOutlineField area;
    private FloatingOutlineField landmark;

    private FloatingOutlineField ustadhCount;
    private FloatingOutlineField headUstadh;
    private FloatingOutlineField phone;
    private FloatingOutlineField secondaryPhone;
    private FloatingOutlineField email;
    private FloatingOutlineField masjidName;
    private FloatingOutlineField masjidLocation;

    private FloatingOutlineField password;
    private FloatingOutlineField confirmPassword;

    private int dp(float value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    /*
     * A picker opens its own dialog, so the soft keyboard is
     * dismissed first instead of being left floating over the
     * list the administrator is about to choose from.
     */
    private void hideKeyboard() {

        View focus = getCurrentFocus();

        if (focus == null) {
            return;
        }

        InputMethodManager methodManager =
                (InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE
                );

        if (methodManager != null) {
            methodManager.hideSoftInputFromWindow(
                    focus.getWindowToken(),
                    0
            );
        }

        focus.clearFocus();
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

        return t;
    }

    private void addGap(int heightDp) {
        View gap = new View(this);

        content.addView(
                gap,
                new LinearLayout.LayoutParams(
                        1,
                        dp(heightDp)
                )
        );
    }

    private FloatingOutlineField field(
            String label,
            boolean passwordMode,
            boolean phoneMode
    ) {
        FloatingOutlineField f =
                new FloatingOutlineField(this);

        f.setLabel(label);

        if (passwordMode) {
            f.setPasswordMode();
        } else if (phoneMode) {
            f.setPhoneMode();
        } else {
            f.setTextMode();
        }

        /*
         * Wrapping height: the field's own input is 58dp tall,
         * so this stays a full-size control while letting a
         * longer label or an RTL translation grow instead of
         * being clipped.
         */
        content.addView(
                f,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        addGap(10);

        return f;
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

        madrassa = new Madrassa();
        madrassa.approvalStatus = ApprovalStatus.DRAFT;

        geography = new LocalGeographyRepository();

        buildBaseLayout();
        showStep(STEP_IDENTITY);
    }

    private void buildBaseLayout() {

        final int walnut = getColor(R.color.edunoor_walnut);
        final int clay = getColor(R.color.edunoor_clay);
        final int ivory = getColor(R.color.edunoor_surface);
        final int muted = getColor(R.color.edunoor_muted);

        getWindow().setStatusBarColor(ivory);
        getWindow().setNavigationBarColor(ivory);

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ivory);
        root.setPadding(
                dp(20),
                dp(16),
                dp(20),
                dp(12)
        );

        /*
         * HEADER
         */

        TextView brand = text(
                getString(R.string.brand_title),
                19,
                walnut,
                true
        );

        brand.setGravity(Gravity.CENTER);

        root.addView(
                brand,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView arabic = text(
                getString(R.string.brand_subtitle),
                14,
                getColor(R.color.edunoor_gold_deep),
                true
        );

        arabic.setGravity(Gravity.CENTER);
        arabic.setTextDirection(
                View.TEXT_DIRECTION_RTL
        );

        LinearLayout.LayoutParams arabicParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        arabicParams.topMargin = dp(2);

        root.addView(arabic, arabicParams);

        stepLabel = text(
                "",
                12,
                clay,
                true
        );

        stepLabel.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams stepLabelParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        stepLabelParams.topMargin = dp(6);

        root.addView(stepLabel, stepLabelParams);

        title = text(
                "",
                23,
                walnut,
                true
        );

        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(4);

        root.addView(title, titleParams);

        subtitle = text(
                "",
                13,
                muted,
                false
        );

        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(2);

        root.addView(subtitle, subtitleParams);

        /*
         * SCROLLABLE FORM AREA
         */

        scroll = new ScrollView(this);

        scroll.setFillViewport(true);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(2),
                dp(4),
                dp(2),
                dp(12)
        );

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        /*
         * NAVIGATION
         */

        LinearLayout navigation = new LinearLayout(this);

        navigation.setOrientation(
                LinearLayout.HORIZONTAL
        );

        navigation.setGravity(
                Gravity.CENTER_VERTICAL
        );

        backButton = EduNoorButton.secondary(
                this,
                getString(R.string.registration_back)
        );

        navigation.addView(
                backButton,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        View navGap = new View(this);

        navigation.addView(
                navGap,
                new LinearLayout.LayoutParams(
                        dp(10),
                        1
                )
        );

        nextButton = EduNoorButton.primary(
                this,
                getString(R.string.registration_next)
        );

        navigation.addView(
                nextButton,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        root.addView(
                navigation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        backButton.setOnClickListener(v -> {

            if (currentStep > STEP_IDENTITY
                    && currentStep < STEP_SUBMITTED) {

                currentStep--;
                showStep(currentStep);

            } else {

                finish();
            }
        });

        nextButton.setOnClickListener(v ->
                handleNext()
        );

        setContentView(root);
    }

    private void showStep(int step) {

        currentStep = step;

        content.removeAllViews();

        if (step == STEP_IDENTITY) {
            showIdentity();
        } else if (step == STEP_ADMINISTRATION) {
            showAdministration();
        } else if (step == STEP_SECURITY) {
            showSecurity();
        } else if (step == STEP_REVIEW) {
            showReview();
        } else {
            showSubmitted();
        }

        scroll.post(() ->
                scroll.fullScroll(View.FOCUS_UP)
        );
    }

    private void showIdentity() {

        stepLabel.setText(
                getString(R.string.registration_step_1)
        );

        title.setText(
                getString(R.string.registration_identity_title)
        );

        subtitle.setText(
                getString(R.string.registration_identity_subtitle)
        );

        madrassaName = field(
                getString(R.string.registration_madrassa_name),
                false,
                false
        );

        type = field(
                getString(R.string.registration_madrassa_type),
                false,
                false
        );

        administrationType = field(
                getString(R.string.registration_administration_type),
                false,
                false
        );

        region = field(
                getString(R.string.registration_region),
                false,
                false
        );

        district = field(
                getString(R.string.registration_district),
                false,
                false
        );

        ward = field(
                getString(R.string.registration_ward),
                false,
                false
        );

        area = field(
                getString(R.string.registration_area),
                false,
                false
        );

        landmark = field(
                getString(R.string.registration_landmark),
                false,
                false
        );

        /*
         * Location logic (Batch V7.3): region and district are
         * picked from the offline geography dataset so the same
         * spelling reaches review, storage and reports. Ward and
         * area stay free text until the ward-level dataset lands.
         * If a country ships without a dataset the two fields
         * simply remain typed inputs.
         */
        if (geography.hasStructuredData(COUNTRY_CODE)) {

            region.setPickerMode(v -> pickRegion());
            district.setPickerMode(v -> pickDistrict());

        } else {
            region.setTypingMode();
            district.setTypingMode();
        }

        ward.setExample(
                getString(R.string.field_example_ward)
        );

        restoreIdentity();

        backButton.setVisibility(View.VISIBLE);
        backButton.setText(
                getString(R.string.registration_cancel)
        );

        nextButton.setText(
                getString(R.string.registration_next)
        );
    }

    /*
     * Step restore: the form is rebuilt on every navigation, so
     * captured values are written back into the fresh fields.
     * Going Back no longer empties the Madrassa details.
     */
    private void restoreIdentity() {

        restore(madrassaName, madrassa.name);
        restore(type, madrassa.type);
        restore(
                administrationType,
                madrassa.administrationType
        );
        restore(region, madrassa.region);
        restore(district, madrassa.district);
        restore(ward, madrassa.ward);
        restore(area, madrassa.area);
        restore(landmark, madrassa.nearbyLandmark);

        resolveSelectedRegion();
    }

    private void restore(
            FloatingOutlineField field,
            String stored
    ) {

        if (stored != null && !stored.trim().isEmpty()) {
            field.setValue(stored);
        }
    }

    /*
     * Matches a restored region name back to its dataset id so a
     * district list is still available after Back/Next.
     */
    private void resolveSelectedRegion() {

        selectedRegionId = null;

        String chosen = value(region);

        if (chosen.isEmpty()) {
            return;
        }

        for (LocationNode node
                : geography.getRootLocations(COUNTRY_CODE)) {

            if (node.name.equalsIgnoreCase(chosen)) {
                selectedRegionId = node.id;
                break;
            }
        }
    }

    private void pickRegion() {

        hideKeyboard();

        final java.util.List<LocationNode> regions =
                geography.getRootLocations(COUNTRY_CODE);

        if (regions.isEmpty()) {
            region.setTypingMode();
            region.getEditText().requestFocus();
            return;
        }

        final String[] names =
                new String[regions.size()];

        for (int i = 0; i < regions.size(); i++) {
            names[i] = regions.get(i).name;
        }

        int checked = -1;

        for (int i = 0; i < names.length; i++) {
            if (names[i].equalsIgnoreCase(value(region))) {
                checked = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        R.string.registration_select_region
                )
                .setSingleChoiceItems(
                        names,
                        checked,
                        (dialog, which) -> {

                            region.setValue(names[which]);
                            selectedRegionId =
                                    regions.get(which).id;

                            /* cascade: a new region invalidates
                               the district and ward below it */
                            district.setValue("");
                            ward.setValue("");

                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        R.string.registration_cancel,
                        null
                )
                .show();
    }

    private void pickDistrict() {

        hideKeyboard();

        if (selectedRegionId == null) {
            resolveSelectedRegion();
        }

        if (selectedRegionId == null) {

            Toast.makeText(
                    this,
                    getString(
                            R.string
                                    .registration_pick_region_first
                    ),
                    Toast.LENGTH_SHORT
            ).show();

            /* open the region list straight away */
            pickRegion();
            return;
        }

        final java.util.List<LocationNode> districts =
                geography.getChildren(
                        COUNTRY_CODE,
                        selectedRegionId,
                        AdministrativeLevel.DISTRICT
                );

        /*
         * No district list for this region in the offline
         * dataset: fall back to typing rather than blocking the
         * administrator.
         */
        if (districts.isEmpty()) {
            district.setTypingMode();
            district.getEditText().requestFocus();
            return;
        }

        final String[] names =
                new String[districts.size()];

        for (int i = 0; i < districts.size(); i++) {
            names[i] = districts.get(i).name;
        }

        int checked = -1;

        for (int i = 0; i < names.length; i++) {
            if (names[i].equalsIgnoreCase(value(district))) {
                checked = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        R.string.registration_select_district
                )
                .setSingleChoiceItems(
                        names,
                        checked,
                        (dialog, which) -> {
                            district.setValue(names[which]);
                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        R.string.registration_cancel,
                        null
                )
                .show();
    }

    private void showAdministration() {

        stepLabel.setText(
                getString(R.string.registration_step_2)
        );

        title.setText(
                getString(R.string.registration_administration_title)
        );

        subtitle.setText(
                getString(R.string.registration_administration_subtitle)
        );

        ustadhCount = field(
                getString(R.string.registration_ustadh_count),
                false,
                false
        );

        ustadhCount.setNumberMode();

        headUstadh = field(
                getString(R.string.registration_head_ustadh),
                false,
                false
        );

        phone = field(
                getString(R.string.registration_phone),
                false,
                true
        );

        phone.setCountryPrefix(selectedDial);
        phone.setPrefixPicker(v -> pickDialCode());
        phone.setExample(
                getString(R.string.field_example_phone)
        );

        secondaryPhone = field(
                getString(R.string.registration_secondary_phone),
                false,
                true
        );

        secondaryPhone.setCountryPrefix(selectedDial);
        secondaryPhone.setPrefixPicker(v -> pickDialCode());
        secondaryPhone.setExample(
                getString(R.string.field_example_phone)
        );

        email = field(
                getString(R.string.registration_email),
                false,
                false
        );

        email.setEmailMode();

        masjidName = field(
                getString(R.string.registration_masjid_name),
                false,
                false
        );

        masjidLocation = field(
                getString(R.string.registration_masjid_location),
                false,
                false
        );

        restoreAdministration();

        backButton.setVisibility(View.VISIBLE);
        backButton.setText(
                getString(R.string.registration_back)
        );

        nextButton.setText(
                getString(R.string.registration_next)
        );
    }

    /*
     * Draft copies of the values that are not part of the Madrassa
     * record itself (head-ustadh name before the account exists,
     * and the step-3 passwords) so Back/Next never loses them.
     */
    private String draftHeadUstadh;
    private String draftPassword;
    private String draftConfirmPassword;

    private void restoreAdministration() {

        if (madrassa.ustadhCount > 0) {
            ustadhCount.setValue(
                    String.valueOf(madrassa.ustadhCount)
            );
        }

        restore(headUstadh, draftHeadUstadh);
        restorePhone(phone, madrassa.phone);
        restorePhone(secondaryPhone, madrassa.secondaryPhone);
        restore(email, madrassa.email);
        restore(masjidName, madrassa.masjidName);
        restore(masjidLocation, madrassa.masjidLocation);
    }

    /*
     * A stored number already carries the dialling prefix; the
     * field shows the prefix separately, so only the national
     * part is written back.
     */
    private void restorePhone(
            FloatingOutlineField field,
            String stored
    ) {

        if (stored == null || stored.trim().isEmpty()) {
            return;
        }

        String typed = stored.trim();

        if (typed.startsWith(selectedDial)) {
            typed = typed
                    .substring(selectedDial.length())
                    .trim();
        }

        field.setValue(typed);
    }

    /*
     * Country-code list for the phone fields. The selection is
     * shared: a Madrassa administrator picks one country and both
     * numbers follow it, then validation and storage use the same
     * prefix.
     */
    private void pickDialCode() {

        hideKeyboard();

        final java.util.List<Country> countries =
                geography.getCountries();

        final String[] labels =
                new String[countries.size()];

        int checked = 0;

        for (int i = 0; i < countries.size(); i++) {

            Country country = countries.get(i);

            labels[i] = country.dialCode.isEmpty()
                    ? country.name
                    : country.name + "   " + country.dialCode;

            if (country.dialCode.equals(selectedDial)) {
                checked = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        R.string
                                .registration_select_country_code
                )
                .setSingleChoiceItems(
                        labels,
                        checked,
                        (dialog, which) -> {

                            Country chosen =
                                    countries.get(which);

                            if (chosen.dialCode.isEmpty()) {
                                return;
                            }

                            selectedDial = chosen.dialCode;

                            phone.setCountryPrefix(selectedDial);
                            secondaryPhone.setCountryPrefix(
                                    selectedDial
                            );

                            phone.setErrorState(false);
                            secondaryPhone.setErrorState(
                                    false
                            );

                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        R.string.registration_cancel,
                        null
                )
                .show();
    }

    private void showSecurity() {

        stepLabel.setText(
                getString(R.string.registration_step_3)
        );

        title.setText(
                getString(R.string.registration_security_title)
        );

        subtitle.setText(
                getString(R.string.registration_security_subtitle)
        );

        password = field(
                getString(R.string.registration_password),
                true,
                false
        );

        confirmPassword = field(
                getString(R.string.registration_confirm_password),
                true,
                false
        );

        TextView rules = text(
                getString(
                        R.string.password_requirements_ustadh
                ),
                12,
                getColor(R.color.edunoor_muted),
                false
        );

        /*
         * Live password commentary: strength plus exactly what
         * is still missing (length, letters, numbers, or a
         * special character such as , % @ #).
         */
        password.getEditText().addTextChangedListener(
                new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                        password.setErrorState(false);
                        rules.setText(
                                passwordFeedback(
                                        s.toString()
                                )
                        );
                    }
                }
        );

        rules.setPadding(
                dp(4),
                0,
                dp(4),
                dp(10)
        );

        content.addView(
                rules,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        restore(password, draftPassword);
        restore(confirmPassword, draftConfirmPassword);

        backButton.setVisibility(View.VISIBLE);
        backButton.setText(
                getString(R.string.registration_back)
        );

        nextButton.setText(
                getString(R.string.registration_review)
        );
    }

    private void showReview() {

        stepLabel.setText(
                getString(R.string.registration_step_4)
        );

        title.setText(
                getString(R.string.registration_review_title)
        );

        subtitle.setText(
                getString(R.string.registration_review_subtitle)
        );

        addReviewSection(
                getString(R.string.registration_identity_section)
        );

        addReviewRow(
                getString(R.string.registration_madrassa_name),
                value(madrassaName)
        );

        addReviewRow(
                getString(R.string.registration_madrassa_type),
                value(type)
        );

        addReviewRow(
                getString(R.string.registration_administration_type),
                value(administrationType)
        );

        addReviewRow(
                getString(R.string.registration_region),
                value(region)
        );

        addReviewRow(
                getString(R.string.registration_district),
                value(district)
        );

        addReviewRow(
                getString(R.string.registration_ward),
                value(ward)
        );

        addReviewRow(
                getString(R.string.registration_area),
                value(area)
        );

        addReviewRow(
                getString(R.string.registration_landmark),
                value(landmark)
        );

        addReviewSection(
                getString(
                        R.string.registration_administration_section
                )
        );

        addReviewRow(
                getString(R.string.registration_ustadh_count),
                value(ustadhCount)
        );

        addReviewRow(
                getString(R.string.registration_head_ustadh),
                value(headUstadh)
        );

        addReviewRow(
                getString(R.string.registration_phone),
                value(phone)
        );

        addReviewRow(
                getString(R.string.registration_secondary_phone),
                value(secondaryPhone)
        );

        addReviewRow(
                getString(R.string.registration_email),
                value(email)
        );

        addReviewRow(
                getString(R.string.registration_masjid_name),
                value(masjidName)
        );

        addReviewRow(
                getString(R.string.registration_masjid_location),
                value(masjidLocation)
        );

        addReviewSection(
                getString(R.string.registration_security_section)
        );

        addReviewRow(
                getString(R.string.registration_status),
                getString(R.string.registration_draft)
        );

        backButton.setVisibility(View.VISIBLE);
        backButton.setText(
                getString(R.string.registration_back)
        );

        nextButton.setText(
                getString(R.string.registration_submit)
        );
    }

    private void addReviewSection(String value) {

        TextView section = text(
                value,
                15,
                getColor(R.color.edunoor_clay),
                true
        );

        section.setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(8)
        );

        content.addView(
                section,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private void addReviewRow(
            String label,
            String value
    ) {

        TextView row = text(
                label + ": " + value,
                14,
                getColor(R.color.edunoor_walnut),
                false
        );

        row.setPadding(
                dp(8),
                dp(6),
                dp(8),
                dp(6)
        );

        row.setGravity(
                Gravity.START | Gravity.CENTER_VERTICAL
        );

        content.addView(
                row,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private String value(
            FloatingOutlineField field
    ) {
        if (field == null) {
            return "—";
        }

        String value = field.getValue().trim();

        return value.isEmpty()
                ? "—"
                : value;
    }

    private void showSubmitted() {

        stepLabel.setText(
                getString(R.string.registration_step_5)
        );

        title.setText(
                getString(R.string.registration_submitted_title)
        );

        subtitle.setText(
                getString(R.string.registration_submitted_subtitle)
        );

        TextView status = text(
                getString(
                        R.string.registration_submitted_status
                ),
                16,
                getColor(R.color.edunoor_walnut),
                true
        );

        status.setGravity(Gravity.CENTER);
        status.setPadding(
                dp(12),
                dp(20),
                dp(12),
                dp(20)
        );

        content.addView(
                status,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView explanation = text(
                getString(
                        R.string.registration_pending_review
                ),
                14,
                getColor(R.color.edunoor_muted),
                false
        );

        explanation.setGravity(Gravity.CENTER);
        explanation.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(20)
        );

        content.addView(
                explanation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        backButton.setVisibility(View.GONE);

        nextButton.setText(
                getString(R.string.registration_close)
        );
    }

    private void handleNext() {

        if (currentStep == STEP_IDENTITY) {

            if (!validateIdentity()) {
                return;
            }

            captureIdentity();
            currentStep = STEP_ADMINISTRATION;
            showStep(currentStep);
            return;
        }

        if (currentStep == STEP_ADMINISTRATION) {

            if (!validateAdministration()) {
                return;
            }

            captureAdministration();
            currentStep = STEP_SECURITY;
            showStep(currentStep);
            return;
        }

        if (currentStep == STEP_SECURITY) {

            if (!validateSecurity()) {
                return;
            }

            currentStep = STEP_REVIEW;
            showStep(currentStep);
            return;
        }

        if (currentStep == STEP_REVIEW) {

            submitRegistration();
            return;
        }

        if (currentStep == STEP_SUBMITTED) {
            finish();
        }
    }

    private boolean validateIdentity() {

        if (!required(
                madrassaName,
                R.string.registration_enter_madrassa_name
        )) {
            return false;
        }

        if (!required(
                type,
                R.string.registration_enter_madrassa_type
        )) {
            return false;
        }

        if (!required(
                administrationType,
                R.string.registration_enter_administration_type
        )) {
            return false;
        }

        if (!required(
                region,
                R.string.registration_enter_region
        )) {
            return false;
        }

        if (!required(
                district,
                R.string.registration_enter_district
        )) {
            return false;
        }

        if (!required(
                ward,
                R.string.registration_enter_ward
        )) {
            return false;
        }

        if (!required(
                area,
                R.string.registration_enter_area
        )) {
            return false;
        }

        return true;
    }

    private boolean validateAdministration() {

        if (!required(
                ustadhCount,
                R.string.registration_enter_ustadh_count
        )) {
            return false;
        }

        if (parseCount(value(ustadhCount)) < 1) {
            ustadhCount.setErrorMessage(
                    getString(R.string.registration_invalid_count)
            );

            ustadhCount.requestFocus();
            return false;
        }

        if (!required(
                headUstadh,
                R.string.registration_enter_head_ustadh
        )) {
            return false;
        }

        if (!required(
                phone,
                R.string.enter_phone
        )) {
            return false;
        }

        if (!EduNoorRules.validPhone(
                selectedDial,
                phone.getValue()
        )) {
            phone.setErrorMessage(
                    getString(R.string.registration_invalid_phone)
            );

            phone.requestFocus();
            return false;
        }

        if (!required(
                email,
                R.string.registration_enter_email
        )) {
            return false;
        }

        if (!EduNoorRules.validEmail(value(email))) {
            email.setErrorMessage(
                    getString(R.string.registration_invalid_email)
            );

            email.requestFocus();
            return false;
        }

        if (!secondaryPhone.getValue().trim().isEmpty()
                && !EduNoorRules.validPhone(
                        selectedDial,
                        secondaryPhone.getValue()
                )) {
            secondaryPhone.setErrorMessage(
                    getString(R.string.registration_invalid_phone)
            );

            secondaryPhone.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateSecurity() {

        String pass = password.getValue();
        String confirm = confirmPassword.getValue();

        /* keep the typed secrets in the step drafts so a trip
           to Back and forward again does not clear them */
        draftPassword = pass;
        draftConfirmPassword = confirm;

        java.util.List<String> missing =
                EduNoorRules.missingUstadhPasswordParts(pass);

        if (!missing.isEmpty()) {

            password.setErrorMessage(passwordFeedback(pass));

            password.requestFocus();
            return false;
        }

        if (!pass.equals(confirm)) {

            confirmPassword.setErrorMessage(
                    getString(R.string.registration_password_mismatch)
            );

            confirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /*
     * Registration password commentary shared by the live
     * strength watcher and the submit-time error message:
     * strength word plus the first missing requirement, so a
     * failed password always says exactly what is missing.
     */
    private String passwordFeedback(String pass) {

        int strength =
                EduNoorRules.ustadhPasswordStrength(pass);

        String level;

        if (strength >= 3) {
            level = getString(
                    R.string.password_strength_strong
            );
        } else if (strength == 2) {
            level = getString(
                    R.string.password_strength_fair
            );
        } else {
            level = getString(
                    R.string.password_strength_weak
            );
        }

        java.util.List<String> missing =
                EduNoorRules.missingUstadhPasswordParts(pass);

        if (missing.isEmpty()) {
            return level;
        }

        return level + " — " + getString(
                missingMessageId(missing.get(0))
        );
    }

    private int missingMessageId(String code) {

        if ("letter".equals(code)) {
            return R.string.password_missing_letter;
        }

        if ("number".equals(code)) {
            return R.string.password_missing_number;
        }

        if ("special".equals(code)) {
            return R.string.password_missing_special;
        }

        return R.string.password_missing_length;
    }

    private boolean required(
            FloatingOutlineField field,
            int messageId
    ) {

        if (field.getValue().trim().isEmpty()) {

            /*
             * Say exactly what is missing, anchored at the field,
             * instead of a transient toast.
             */
            field.setErrorMessage(
                    getString(messageId)
            );

            field.requestFocus();
            return false;
        }

        return true;
    }

    private void captureIdentity() {

        /* raw values: "—" is only a review placeholder and must
           never be written into the Madrassa record */
        madrassa.name =
                madrassaName.getValue().trim();
        madrassa.type =
                type.getValue().trim();
        madrassa.administrationType =
                administrationType.getValue().trim();
        madrassa.region =
                region.getValue().trim();
        madrassa.district =
                district.getValue().trim();
        madrassa.ward =
                ward.getValue().trim();
        madrassa.area =
                area.getValue().trim();
        madrassa.nearbyLandmark =
                landmark.getValue().trim();
    }

    private void captureAdministration() {

        madrassa.ustadhCount =
                parseCount(value(ustadhCount));

        String head = value(headUstadh);

        draftHeadUstadh =
                "—".equals(head) ? "" : head;

        /*
         * RegistrationPolicy requires a Head Ustadh reference,
         * and RegistrationService.activate() later reads this
         * field as the head Ustadh's full name — so the name
         * typed here belongs on the record, not only in the
         * step draft (which is what keeps it across Back/Next).
         */
        madrassa.headUstadhId = draftHeadUstadh;

        String primary = phone.getValue().trim();

        madrassa.phone = primary.isEmpty()
                ? ""
                : EduNoorRules.normalizePhone(
                        selectedDial,
                        primary
                );

        String secondary =
                secondaryPhone.getValue().trim();

        madrassa.secondaryPhone = secondary.isEmpty()
                ? ""
                : EduNoorRules.normalizePhone(
                        selectedDial,
                        secondary
                );

        String storedEmail = value(email);

        madrassa.email =
                "—".equals(storedEmail) ? "" : storedEmail;

        String masjid = value(masjidName);
        String masjidPlace = value(masjidLocation);

        madrassa.masjidName =
                "—".equals(masjid) ? "" : masjid;
        madrassa.masjidLocation =
                "—".equals(masjidPlace) ? "" : masjidPlace;
    }

    private int parseCount(String value) {

        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    /*
     * Submission now goes through the domain RegistrationService
     * instead of painting SUBMITTED onto the record by hand, so
     * the same rules the backend will enforce (required fields,
     * password policy, duplicate name, DRAFT/REJECTED state) run
     * here, and the record plus its hashed pending credential are
     * written in one local transaction.
     *
     * The service hashes the initial password with PBKDF2
     * (210,000 iterations), so it runs on submitWorker and the
     * Next button is locked out until the result comes back.
     */
    private void submitRegistration() {

        captureIdentity();
        captureAdministration();

        if (submitting) {
            return;
        }

        submitting = true;

        EduNoorButton.setLocked(nextButton, true);
        nextButton.setAlpha(0.5f);

        final String initialPassword = draftPassword;

        submitWorker.execute(() -> {

            OperationResult<Madrassa> result;

            try {
                result = registrationService().submit(
                        new RegistrationSubmission(
                                madrassa,
                                initialPassword
                        )
                );

            } catch (RuntimeException error) {
                result = OperationResult.failed(
                        "registration_save_failed",
                        "Madrassa registration could not be saved."
                );
            }

            final OperationResult<Madrassa> outcome = result;

            runOnUiThread(() -> {

                submitting = false;

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                EduNoorButton.setLocked(nextButton, false);
                nextButton.setAlpha(1f);

                if (outcome.isSuccess()) {

                    currentStep = STEP_SUBMITTED;
                    showStep(currentStep);

                    Toast.makeText(
                            this,
                            getString(
                                    R.string
                                            .registration_submitted_message
                            ),
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                /*
                 * The service marks the in-memory record SUBMITTED
                 * before it writes, so a failed save has to be put
                 * back to DRAFT — otherwise a retry would be
                 * refused by canSubmit() and the administrator
                 * could never submit this Madrassa again.
                 */
                madrassa.approvalStatus =
                        ApprovalStatus.DRAFT;
                madrassa.submittedAt = 0L;

                showSubmissionError(outcome.getCode());
            });
        });
    }

    private RegistrationService registrationService() {

        return new RegistrationService(
                new MadrassaRepository(
                        getApplicationContext()
                ),
                new PendingRegistrationCredentialRepository(
                        getApplicationContext()
                ),
                new RegistrationTransactionRepository(
                        getApplicationContext()
                ),
                new Pbkdf2PasswordVerifier()
        );
    }

    /*
     * Every failure the service can return is translated into
     * the localized message for that exact case, so a rejected
     * submission always says what went wrong instead of a
     * generic error.
     */
    private void showSubmissionError(String code) {

        int messageId =
                R.string.registration_invalid_data;

        if ("madrassa_name_exists".equals(code)) {
            messageId =
                    R.string.registration_name_exists;

        } else if ("registration_save_failed".equals(code)) {
            messageId =
                    R.string.registration_save_failed;

        } else if ("invalid_initial_password".equals(code)) {
            messageId =
                    R.string
                            .password_requirements_ustadh;
        }

        Toast.makeText(
                this,
                getString(messageId),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        submitWorker.shutdownNow();
    }
}

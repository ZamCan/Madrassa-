package com.zamcan.madrassa.ui.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zamcan.madrassa.R;

public class FloatingOutlineField extends FrameLayout {

    private final EditText input;
    private final TextView label;
    private final TextView prefix;
    private final TextView visibilityToggle;
    private final TextView chevron;

    /*
     * Every colour resolves from the single EduNoor palette in
     * res/values/colors.xml so the field can never drift from
     * the rest of the design system.
     */
    private final int normalBorder;
    private final int focusBorder;
    private final int errorBorder;

    private final int textColor;
    private final int mutedColor;
    private final int surfaceColor;

    private String labelText = "";
    private String exampleText = "";
    private String prefixCode = "";
    private boolean error = false;
    private boolean passwordHidden = true;
    private boolean prefixPicker = false;

    public FloatingOutlineField(Context context) {
        this(context, null);
    }

    public FloatingOutlineField(Context context, AttributeSet attrs) {
        super(context, attrs);

        setClipChildren(false);
        setClipToPadding(false);

        normalBorder = context.getColor(R.color.edunoor_border);
        focusBorder = context.getColor(R.color.edunoor_dusty_blue);
        errorBorder = context.getColor(R.color.edunoor_clay);

        textColor = context.getColor(R.color.edunoor_ink);
        mutedColor = context.getColor(R.color.edunoor_muted);
        surfaceColor = context.getColor(R.color.edunoor_surface);

        input = new EditText(context);
        label = new TextView(context);
        prefix = new TextView(context);
        visibilityToggle = new TextView(context);
        chevron = new TextView(context);

        build();
    }

    private void build() {

        setPadding(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        LayoutParams inputParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
        );

        inputParams.leftMargin = dp(14);
        inputParams.rightMargin = dp(14);

        input.setLayoutParams(inputParams);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setTextColor(textColor);
        input.setHintTextColor(mutedColor);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setPadding(
                dp(2),
                dp(4),
                dp(2),
                0
        );

        /*
         * Typing again clears a stale red error border, so a
         * corrected value never keeps complaining at the user.
         */
        input.addTextChangedListener(new TextWatcher() {
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
            public void afterTextChanged(Editable s) {
                if (error) {
                    error = false;
                    updateBorder();
                }
            }
        });

        addView(input);

        /*
         * Country-code prefix (e.g. +255). Hidden unless
         * setCountryPrefix() is used. Sits inside the field on
         * the left; the input padding below clears it.
         */
        prefix.setTextColor(textColor);
        prefix.setTextSize(16);
        prefix.setSingleLine(true);
        prefix.setGravity(Gravity.CENTER_VERTICAL);
        prefix.setTypeface(
                android.graphics.Typeface.create(
                        "sans",
                        android.graphics.Typeface.BOLD
                )
        );

        LayoutParams prefixParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(58)
        );

        prefixParams.leftMargin = dp(30);

        prefix.setLayoutParams(prefixParams);
        prefix.setVisibility(GONE);

        addView(prefix);

        /*
         * Password show/hide toggle. Text glyph only, no emoji.
         * Shown only for password fields.
         *
         * The toggle is deliberately confined to a 46dp strip on
         * the right edge (FrameLayout END gravity). It must never
         * span the full width: as a clickable view it would sit
         * above the EditText and swallow every tap, leaving the
         * password field impossible to focus. The input keeps a
         * 44dp right padding so typed text clears the glyph.
         */
        visibilityToggle.setTextColor(mutedColor);
        visibilityToggle.setTextSize(16);
        visibilityToggle.setSingleLine(true);
        visibilityToggle.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.END
        );
        visibilityToggle.setPadding(
                dp(6),
                0,
                dp(10),
                0
        );

        LayoutParams toggleParams = new LayoutParams(
                dp(46),
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        toggleParams.gravity =
                Gravity.END | Gravity.CENTER_VERTICAL;

        visibilityToggle.setLayoutParams(toggleParams);
        visibilityToggle.setVisibility(GONE);

        visibilityToggle.setOnClickListener(v -> togglePassword());

        addView(visibilityToggle);

        /*
         * Picker indicator (small ▾ at the right edge). Shown only
         * for fields backed by a selection list such as region or
         * district; the input itself is then non-focusable and a
         * tap opens the list.
         */
        chevron.setText("▾");
        chevron.setTextColor(mutedColor);
        chevron.setTextSize(14);

        /*
         * The chevron only decorates the picker; the prefix text
         * next to it carries the information, so it is silent to
         * accessibility services.
         */
        chevron.setImportantForAccessibility(
                IMPORTANT_FOR_ACCESSIBILITY_NO
        );
        chevron.setSingleLine(true);
        chevron.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.END
        );
        chevron.setPadding(
                dp(8),
                0,
                dp(16),
                0
        );
        chevron.setVisibility(GONE);

        LayoutParams chevronParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
        );

        chevronParams.leftMargin = dp(14);
        chevronParams.rightMargin = dp(14);

        chevron.setLayoutParams(chevronParams);

        addView(chevron);

        /*
         * The floated label is deliberately smaller and lighter
         * than the in-box hint: 11sp muted, slight letter
         * spacing, normal weight. Its surface-colour chip cuts
         * the label into the top border line.
         */
        label.setTextColor(mutedColor);
        label.setTextSize(11f);
        label.setLetterSpacing(0.04f);
        label.setSingleLine(true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(
                dp(5),
                0,
                dp(5),
                0
        );
        label.setBackgroundColor(surfaceColor);

        LayoutParams labelParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(22)
        );

        labelParams.leftMargin = dp(17);
        labelParams.topMargin = dp(-1);

        label.setLayoutParams(labelParams);

        addView(label);

        label.setVisibility(INVISIBLE);

        input.setOnFocusChangeListener((view, focused) -> {
            updateVisualState(
                    focused || input.length() > 0
            );
        });

        updateVisualState(false);
    }

    /*
     * Persistent floating label.
     *
     * The field name stays visible on the top border at all
     * times instead of only after the first tap: an empty,
     * untouched form must still say what every box is for.
     * The grey example (e.g. the phone format) lives inside
     * the box as the hint, so name and example never compete
     * for the same line.
     */
    public void setLabel(String text) {

        labelText = text == null ? "" : text;
        label.setText(labelText);

        applyLabelAndHint();
    }

    private void applyLabelAndHint() {

        label.setVisibility(
                labelText.trim().isEmpty()
                        ? INVISIBLE
                        : VISIBLE
        );

        input.setHint(exampleText);
    }

    /*
     * Grey example shown inside the box (e.g. phone format).
     * The floated label keeps the field name; the example
     * remains as the faint in-box hint.
     */
    public void setExample(String example) {
        exampleText = example == null ? "" : example;

        applyLabelAndHint();
    }

    /*
     * Fixed country-code prefix shown inside the field.
     * getValue() returns the code plus the typed number.
     */
    public void setCountryPrefix(String code) {
        prefixCode = code == null ? "" : code.trim();

        if (prefixCode.isEmpty()) {
            prefix.setVisibility(GONE);
            input.setPadding(
                    dp(2),
                    dp(4),
                    input.getPaddingRight(),
                    0
            );
            return;
        }

        prefix.setText(
                prefixPicker
                        ? prefixCode + " ▾"
                        : prefixCode
        );
        prefix.setVisibility(VISIBLE);
        input.setPadding(
                dp(66),
                dp(4),
                input.getPaddingRight(),
                0
        );
    }

    public String getCountryPrefix() {
        return prefixCode;
    }

    /*
     * Turns the country-code prefix into a selector: tapping the
     * code (and its ▾) opens the country list instead of the
     * keyboard.
     */
    public void setPrefixPicker(final OnClickListener onPick) {
        prefixPicker = true;

        if (!prefixCode.isEmpty()) {
            prefix.setText(prefixCode + " ▾");
        }

        prefix.setClickable(true);
        prefix.setFocusable(false);

        prefix.setOnClickListener(v -> {
            if (onPick != null) {
                onPick.onClick(this);
            }
        });
    }

    /*
     * Picker-backed field (region, district, country): the box no
     * longer opens a keyboard, a tap runs onPick and the right
     * edge shows a ▾ so the behaviour is discoverable. The field
     * stays editable through setValue(), which is also how a
     * value typed on a device without dataset coverage is kept.
     */
    public void setPickerMode(final OnClickListener onPick) {

        input.setFocusable(false);
        input.setCursorVisible(false);
        input.setClickable(true);
        input.setPadding(
                input.getPaddingLeft(),
                dp(4),
                dp(30),
                0
        );

        chevron.setVisibility(VISIBLE);

        View.OnClickListener handler = v -> {
            if (onPick != null) {
                onPick.onClick(this);
            }
        };

        input.setOnClickListener(handler);
        chevron.setOnClickListener(handler);
    }

    /*
     * Programmatic value: used by pickers and by step restore when
     * the user navigates back. Clears any stale error state.
     */
    public void setValue(String text) {

        input.setText(text == null ? "" : text);
        error = false;

        updateVisualState(
                input.length() > 0 || input.hasFocus()
        );
    }

    /*
     * Puts the field back to a normal typed input: used when the
     * offline dataset has no list for a level (or no dataset at
     * all) so the value can still be entered by hand.
     */
    public void setTypingMode() {

        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.setCursorVisible(true);
        input.setClickable(false);
        input.setOnClickListener(null);

        chevron.setOnClickListener(null);
        chevron.setVisibility(GONE);

        input.setPadding(
                input.getPaddingLeft(),
                dp(4),
                prefix.getVisibility() == VISIBLE
                        ? input.getPaddingRight()
                        : dp(2),
                0
        );
    }

    @Override
    public String toString() {
        return getValue();
    }

    public EditText getEditText() {
        return input;
    }

    public String getValue() {
        String typed = input.getText().toString().trim();

        if (prefix.getVisibility() == VISIBLE
                && !prefixCode.isEmpty()) {

            if (!typed.isEmpty() && !typed.startsWith("+")) {
                return prefixCode + " " + typed;
            }
        }

        return typed;
    }

    public String getTypedValue() {
        return input.getText().toString().trim();
    }

    public void setPhoneMode() {
        input.setInputType(
                InputType.TYPE_CLASS_PHONE
        );
    }

    public void setPasswordMode() {
        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        passwordHidden = true;
        visibilityToggle.setText("○");
        visibilityToggle.setContentDescription(
                getContext().getString(
                        R.string.show_password
                )
        );
        visibilityToggle.setVisibility(VISIBLE);
        input.setPadding(
                input.getPaddingLeft(),
                dp(4),
                dp(44),
                0
        );
    }

    private void togglePassword() {
        passwordHidden = !passwordHidden;

        int selection = input.getSelectionEnd();

        if (passwordHidden) {
            input.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            visibilityToggle.setText("○");
            visibilityToggle.setContentDescription(
                    getContext().getString(
                            R.string.show_password
                    )
            );
        } else {
            input.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            visibilityToggle.setText("◉");
            visibilityToggle.setContentDescription(
                    getContext().getString(
                            R.string.hide_password
                    )
            );
        }

        input.setSelection(
                Math.max(0, Math.min(selection, input.length()))
        );
    }

    public void setTextMode() {
        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );
    }

    /*
     * Numeric field (e.g. number of Ustadh): digits only, so the
     * phone keypad opens instead of the full keyboard and no
     * letters can be typed into a counted value.
     */
    public void setNumberMode() {
        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    }

    /*
     * E-mail field: e-mail keyboard, no auto-capitalisation of
     * the local part, no distracting sentence behaviour.
     */
    public void setEmailMode() {
        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    }

    /*
     * Last field of a form: the keyboard shows "Done" instead of
     * "Next" and pressing it runs the given action (typically
     * the submit/login button), so the form can be completed
     * without reaching for the button.
     */
    public void setOnDoneAction(final OnClickListener onDone) {

        input.setImeOptions(EditorInfo.IME_ACTION_DONE);

        input.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE
                    && onDone != null) {

                onDone.onClick(this);
                return true;
            }

            return false;
        });
    }

    public void setErrorState(boolean value) {
        error = value;
        updateBorder();
    }

    private void updateVisualState(boolean floating) {

        /*
         * Label visibility no longer depends on focus or
         * content (see applyLabelAndHint); focus only drives
         * the border treatment below.
         */
        applyLabelAndHint();

        updateBorder();
    }

    private void updateBorder() {

        boolean active =
                input.hasFocus() ||
                input.length() > 0;

        int strokeColor;

        if (error) {
            strokeColor = errorBorder;
        } else if (active) {
            strokeColor = focusBorder;
        } else {
            strokeColor = normalBorder;
        }

        GradientDrawable border = new GradientDrawable();

        border.setColor(surfaceColor);
        border.setCornerRadius(dp(24));

        border.setStroke(
                active || error ? dp(2) : dp(1),
                strokeColor
        );

        setBackground(border);
    }

    private int dp(int value) {
        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
                + 0.5f
        );
    }
}

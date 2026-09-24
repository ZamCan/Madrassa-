package com.zamcan.madrassa.core.calendar;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.core.deen.DeenPrefs;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * EduNoor dual calendar page.
 *
 * A quiet month grid: the Gregorian month the phone lives in, with
 * the Islamic (Umm al-Qura) date resting beneath every single day -
 * fully offline. Above the grid: Hijri month span of the shown
 * month. Below it: special-month line (Ramadan count-up /
 * count-down, Eid and Arafah countdowns, sacred month names), the
 * Ijumaa card on Fridays (wish + the ayat of Surah Al-Jumu'ah),
 * and - only if the user opts in - their own device-calendar
 * events for the coming week.
 */
public class CalendarActivity extends Activity {

    private static final int REQUEST_READ_CALENDAR = 41;
    private static final int EVENTS_DAYS_AHEAD = 7;

    private LinearLayout gridBox;
    private TextView hijriSpanTitle;
    private TextView monthTitle;
    private LinearLayout bannerBox;
    private LinearLayout fridayBox;
    private LinearLayout eventsBox;

    private YearMonth displayed;

    private int dp(float value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private TextView text(CharSequence value, float sizeSp,
                          int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(
                Typeface.DEFAULT,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );
        return view;
    }

    @Override
    protected void attachBaseContext(
            android.content.Context newBase
    ) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int background = getColor(R.color.edunoor_background);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        displayed = YearMonth.now();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        /*
         * HEADER
         */
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(7), dp(16), dp(7));

        TextView back = text("‹", 30,
                getColor(R.color.edunoor_clay), false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        header.addView(
                back,
                new LinearLayout.LayoutParams(dp(42), dp(48))
        );

        LinearLayout headerWords = new LinearLayout(this);
        headerWords.setOrientation(LinearLayout.VERTICAL);
        headerWords.addView(
                text(getString(R.string.calendar_title), 16,
                        getColor(R.color.edunoor_walnut), true),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        headerWords.addView(
                text(getString(R.string.calendar_subtitle), 10.5f,
                        getColor(R.color.edunoor_muted), false),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        header.addView(
                headerWords,
                new LinearLayout.LayoutParams(0, -1, 1)
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        /*
         * SCROLLING BODY
         */
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(6), dp(16), dp(20));

        content.addView(
                buildNavCard(),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        gridBox = new LinearLayout(this);
        gridBox.setOrientation(LinearLayout.VERTICAL);
        content.addView(
                gridBox,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        bannerBox = new LinearLayout(this);
        bannerBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams bannerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        bannerParams.topMargin = dp(12);
        content.addView(bannerBox, bannerParams);

        fridayBox = new LinearLayout(this);
        fridayBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams fridayParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        fridayParams.topMargin = dp(10);
        content.addView(fridayBox, fridayParams);

        eventsBox = new LinearLayout(this);
        eventsBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams eventsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        eventsParams.topMargin = dp(10);
        content.addView(eventsBox, eventsParams);

        scroll.addView(
                content,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1)
        );

        setContentView(root);

        refreshGrid();
        refreshBanner();
        refreshFriday();
        refreshEvents();
    }

    /*
     * =========================================================
     * MONTH NAVIGATION CARD
     * =========================================================
     */
    private View buildNavCard() {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        card.setBackground(getDrawable(R.drawable.edunoor_canvas));

        LinearLayout navRow = new LinearLayout(this);
        navRow.setGravity(Gravity.CENTER);
        navRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView prev = text("‹", 24,
                getColor(R.color.edunoor_gold_deep), true);
        prev.setGravity(Gravity.CENTER);
        prev.setOnClickListener(v -> {
            displayed = displayed.minusMonths(1);
            refreshGrid();
        });

        TextView next = text("›", 24,
                getColor(R.color.edunoor_gold_deep), true);
        next.setGravity(Gravity.CENTER);
        next.setOnClickListener(v -> {
            displayed = displayed.plusMonths(1);
            refreshGrid();
        });

        monthTitle = text("", 15,
                getColor(R.color.edunoor_ink), true);
        monthTitle.setGravity(Gravity.CENTER);

        navRow.addView(
                prev,
                new LinearLayout.LayoutParams(dp(44), dp(40))
        );
        navRow.addView(
                monthTitle,
                new LinearLayout.LayoutParams(0, dp(40), 1)
        );
        navRow.addView(
                next,
                new LinearLayout.LayoutParams(dp(44), dp(40))
        );

        card.addView(
                navRow,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        hijriSpanTitle = text("", 10.5f,
                getColor(R.color.edunoor_gold_deep), false);
        hijriSpanTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams spanParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        spanParams.topMargin = dp(2);
        card.addView(hijriSpanTitle, spanParams);

        return card;
    }

    /*
     * =========================================================
     * GRID REFRESH
     * =========================================================
     */
    private void refreshGrid() {

        String[] gregorianMonths = getResources()
                .getStringArray(R.array.calendar_gregorian_months);
        String[] hijriMonths = getResources()
                .getStringArray(R.array.calendar_hijri_months);
        String[] weekdays = getResources()
                .getStringArray(R.array.calendar_weekdays);

        monthTitle.setText(getString(R.string.calendar_month_title,
                gregorianMonths[displayed.getMonthValue() - 1],
                displayed.getYear()));

        LocalDate first = displayed.atDay(1);
        LocalDate last = displayed.atEndOfMonth();
        EduNoorCalendars.HijriDate h1 =
                EduNoorCalendars.toHijri(first);
        EduNoorCalendars.HijriDate h2 =
                EduNoorCalendars.toHijri(last);

        String span;
        if (h1.year == h2.year && h1.month == h2.month) {
            span = "☾  " + hijriMonths[h1.month - 1] + " " + h1.year;
        } else {
            span = "☾  " + hijriMonths[h1.month - 1] + " – "
                    + hijriMonths[h2.month - 1] + " " + h2.year;
        }
        hijriSpanTitle.setText(span);

        /*
         * Weekday initials, Monday-first.
         */
        LinearLayout weekRow = new LinearLayout(this);
        weekRow.setOrientation(LinearLayout.HORIZONTAL);
        for (String initial : weekdays) {
            TextView cell = text(initial, 9.5f,
                    getColor(R.color.edunoor_muted), true);
            cell.setGravity(Gravity.CENTER);
            weekRow.addView(
                    cell,
                    new LinearLayout.LayoutParams(0, dp(24), 1)
            );
        }

        gridBox.removeAllViews();

        LinearLayout gridCard = new LinearLayout(this);
        gridCard.setOrientation(LinearLayout.VERTICAL);
        gridCard.setPadding(dp(10), dp(8), dp(10), dp(10));
        gridCard.setBackground(getDrawable(R.drawable.edunoor_canvas));
        gridCard.addView(
                weekRow,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        LocalDate today = LocalDate.now();
        int hijriShift = DeenPrefs.hijriAdjust(this);
        int leadingBlanks = first.getDayOfWeek().getValue() - 1;

        LinearLayout row = null;

        for (int day = 1; day <= last.getDayOfMonth(); day++) {

            int position = leadingBlanks + day - 1;

            if (position % 7 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                gridCard.addView(
                        row,
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dp(38))
                );
            }

            LocalDate date = displayed.atDay(day);
            EduNoorCalendars.HijriDate hijri =
                    EduNoorCalendars.toHijri(
                            date.plusDays(hijriShift));

            boolean hijriMonthStart = hijri.day == 1;
            boolean isToday = date.equals(today);

            String plain = day + "\n" + hijri.day;
            SpannableString styled = new SpannableString(plain);
            int split = plain.indexOf('\n');
            styled.setSpan(
                    new RelativeSizeSpan(0.6f),
                    split + 1,
                    plain.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            styled.setSpan(
                    new ForegroundColorSpan(getColor(
                            hijriMonthStart
                                    ? R.color.edunoor_gold_deep
                                    : R.color.edunoor_muted)),
                    split + 1,
                    plain.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            TextView cell = text(styled, 13,
                    isToday
                            ? getColor(R.color.edunoor_ink)
                            : getColor(R.color.edunoor_ink_soft),
                    isToday);
            cell.setGravity(Gravity.CENTER);

            if (isToday) {
                GradientDrawable pill = new GradientDrawable();
                pill.setColor(getColor(R.color.edunoor_gold_soft));
                pill.setStroke(dp(1),
                        getColor(R.color.edunoor_gold_deep));
                pill.setCornerRadius(dp(9));
                cell.setBackground(pill);
            }

            row.addView(
                    cell,
                    new LinearLayout.LayoutParams(0, -1, 1)
            );
        }

        gridBox.addView(
                gridCard,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
    }

    /*
     * =========================================================
     * SPECIAL MONTHS - Ramadan count-up/count-down, Eids,
     * Arafah, sacred months. One quiet line, today-based.
     * =========================================================
     */
    private void refreshBanner() {

        bannerBox.removeAllViews();

        LocalDate today = LocalDate.now();
        LocalDate hijriToday = today.plusDays(
                DeenPrefs.hijriAdjust(this));
        EduNoorCalendars.HijriDate hijri =
                EduNoorCalendars.toHijri(hijriToday);

        String message = specialMessage(hijriToday, hijri);
        if (message == null) {
            bannerBox.setVisibility(View.GONE);
            return;
        }
        bannerBox.setVisibility(View.VISIBLE);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(16), dp(11), dp(16), dp(11));
        card.setBackground(getDrawable(R.drawable.edunoor_canvas));

        TextView line = text("☾  " + message, 12.5f,
                getColor(R.color.edunoor_gold_deep), true);
        line.setGravity(Gravity.CENTER);
        card.addView(
                line,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        bannerBox.addView(
                card,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
    }

    /** Priority-ordered special-day message, or null for none. */
    private String specialMessage(
            LocalDate today,
            EduNoorCalendars.HijriDate hijri
    ) {
        String[] hijriMonths = getResources()
                .getStringArray(R.array.calendar_hijri_months);

        if (hijri.month == 9) {
            int dayOfEid = EduNoorCalendars
                    .hijriMonthLength(hijri.year, 9) - hijri.day;
            return getString(R.string.calendar_ramadan_active,
                    hijri.day)
                    + "  •  "
                    + getString(R.string.calendar_eid_fitr, dayOfEid);
        }

        long toRamadan = daysUntil(hijri.year
                + (hijri.month < 9 ? 0 : 1), 9, 1);
        if (toRamadan <= 60) {
            return getString(R.string.calendar_ramadan_countdown,
                    toRamadan);
        }

        long toFitr = daysUntil(hijri.year
                + (hijri.month < 10 ? 0 : 1), 10, 1);
        if (toFitr <= 30) {
            return getString(R.string.calendar_eid_fitr, toFitr);
        }

        int adhaYear = hijri.year
                + ((hijri.month == 12 && hijri.day >= 10) ? 1 : 0);
        long toArafah = daysUntil(adhaYear, 12, 9);
        long toAdha = daysUntil(adhaYear, 12, 10);

        if (toArafah >= 0 && toArafah <= 30) {
            return getString(R.string.calendar_arafah, toArafah);
        }
        if (toAdha >= 0 && toAdha <= 30) {
            return getString(R.string.calendar_eid_adha, toAdha);
        }

        if (hijri.month == 1 || hijri.month == 7
                || hijri.month == 11 || hijri.month == 12) {
            return getString(R.string.calendar_sacred_month,
                    hijriMonths[hijri.month - 1]);
        }

        return null;
    }

    /** Days from today to the given Hijri date (next occurrence). */
    private long daysUntil(int hijriYear, int month, int day) {

        LocalDate today = LocalDate.now();
        for (int year = hijriYear; year <= hijriYear + 1; year++) {
            LocalDate date = EduNoorCalendars.toGregorian(
                    new EduNoorCalendars.HijriDate(year, month, day));
            long days = ChronoUnit.DAYS.between(today, date);
            if (days >= 0) {
                return days;
            }
        }
        return Long.MAX_VALUE;
    }

    /*
     * =========================================================
     * IJUMAA - on Friday only: wish + rotating ayah from
     * Surah Al-Jumu'ah (62:9-11), the Qur'anic ayat of the day.
     * =========================================================
     */
    @android.annotation.SuppressLint("DiscouragedApi")
    private void refreshFriday() {

        fridayBox.removeAllViews();

        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek().getValue() != 5) {
            fridayBox.setVisibility(View.GONE);
            return;
        }
        fridayBox.setVisibility(View.VISIBLE);

        /*
         * Stable rotation: one ayah per Friday of the year.
         */
        int weekOfYear = today.getDayOfYear() / 7;
        int verse = weekOfYear % 3 + 1;

        String arabic = getString(
                resource("friday_v" + verse + "_arabic"));
        String translit = getString(
                resource("friday_v" + verse + "_translit"));
        String meaning = getString(
                resource("friday_v" + verse + "_text"));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(18), dp(14), dp(18), dp(14));
        card.setBackground(getDrawable(R.drawable.edunoor_canvas));

        TextView wish = text("☾  " +
                getString(R.string.friday_wish), 15,
                getColor(R.color.edunoor_gold_deep), true);
        wish.setGravity(Gravity.CENTER);
        card.addView(
                wish,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        TextView blessing = text(
                getString(R.string.friday_blessing), 11.5f,
                getColor(R.color.edunoor_ink_soft), false);
        blessing.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams blessingParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        blessingParams.topMargin = dp(4);
        card.addView(blessing, blessingParams);

        TextView label = text(
                getString(R.string.friday_verses_label), 10,
                getColor(R.color.edunoor_muted), true);
        label.setGravity(Gravity.CENTER);
        label.setAllCaps(true);
        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dp(12);
        card.addView(label, labelParams);

        TextView arabicLine = text(arabic, 19,
                getColor(R.color.edunoor_ink), false);
        arabicLine.setGravity(Gravity.CENTER);
        arabicLine.setTextDirection(
                View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        LinearLayout.LayoutParams arabicParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        arabicParams.topMargin = dp(10);
        card.addView(arabicLine, arabicParams);

        TextView translitLine = text(translit, 11,
                getColor(R.color.edunoor_gold_deep), true);
        translitLine.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams translitParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        translitParams.topMargin = dp(8);
        card.addView(translitLine, translitParams);

        TextView meaningLine = text(meaning, 12,
                getColor(R.color.edunoor_ink_soft), false);
        meaningLine.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams meaningParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        meaningParams.topMargin = dp(8);
        card.addView(meaningLine, meaningParams);

        fridayBox.addView(
                card,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
    }

    @android.annotation.SuppressLint("DiscouragedApi")
    private int resource(String name) {
        return getResources().getIdentifier(
                name, "string", getPackageName());
    }

    /*
     * =========================================================
     * DEVICE CALENDAR - opt-in. Without the grant the card is a
     * single quiet invitation; with it, the coming week's events
     * rest beneath the grid. Nothing ever leaves the device.
     * =========================================================
     */
    private void refreshEvents() {

        eventsBox.removeAllViews();

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        card.setBackground(getDrawable(R.drawable.edunoor_canvas));

        boolean granted = checkSelfPermission(
                Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;

        TextView title = text(
                getString(R.string.calendar_events_title), 12.5f,
                getColor(R.color.edunoor_walnut), true);
        card.addView(
                title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        if (!granted) {

            TextView invite = text(
                    getString(R.string.calendar_events_invite),
                    11.5f,
                    getColor(R.color.edunoor_muted), false);
            LinearLayout.LayoutParams inviteParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            inviteParams.topMargin = dp(6);
            card.addView(invite, inviteParams);

            TextView connect = text(
                    getString(R.string.calendar_events_connect),
                    12.5f,
                    getColor(R.color.edunoor_gold_deep), true);
            connect.setGravity(Gravity.CENTER);
            GradientDrawable connectChip = new GradientDrawable();
            connectChip.setColor(getColor(R.color.edunoor_gold_soft));
            connectChip.setStroke(dp(1),
                    getColor(R.color.edunoor_gold_deep));
            connectChip.setCornerRadius(dp(9));
            connect.setBackground(connectChip);
            LinearLayout.LayoutParams connectParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(38));
            connectParams.topMargin = dp(10);
            connect.setGravity(Gravity.CENTER);
            connect.setOnClickListener(v ->
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.READ_CALENDAR},
                            REQUEST_READ_CALENDAR));
            card.addView(connect, connectParams);

            eventsBox.addView(
                    card,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            return;
        }

        card.addView(
                buildEventRows(),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        eventsBox.addView(
                card,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
    }

    private View buildEventRows() {

        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(4), 0, 0);

        List<String[]> events = readDeviceEvents();

        if (events.isEmpty()) {
            TextView empty = text(
                    getString(R.string.calendar_events_empty),
                    11.5f,
                    getColor(R.color.edunoor_muted), false);
            LinearLayout.LayoutParams emptyParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            emptyParams.topMargin = dp(6);
            rows.addView(empty, emptyParams);
            return rows;
        }

        String[] gregorianMonths = getResources()
                .getStringArray(R.array.calendar_gregorian_months);

        String currentDay = "";

        for (String[] event : events) {

            long begin = Long.parseLong(event[1]);
            boolean allDay = "1".equals(event[2]);
            LocalDate day = java.time.Instant
                    .ofEpochMilli(begin)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String dayKey = day.getDayOfMonth() + " "
                    + gregorianMonths[day.getMonthValue() - 1];

            if (!dayKey.equals(currentDay)) {
                currentDay = dayKey;
                TextView dayHeading = text(dayKey, 11,
                        getColor(R.color.edunoor_gold_deep), true);
                LinearLayout.LayoutParams dayParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                dayParams.topMargin = dp(8);
                rows.addView(dayHeading, dayParams);
            }

            String when;
            if (allDay) {
                when = getString(R.string.calendar_events_all_day);
            } else {
                java.time.ZonedDateTime time = java.time.Instant
                        .ofEpochMilli(begin)
                        .atZone(ZoneId.systemDefault());
                when = java.lang.String.format(
                        java.util.Locale.ROOT, "%02d:%02d",
                        time.getHour(), time.getMinute());
            }

            SpannableString row = new SpannableString(
                    when + "  •  " + event[0]);
            row.setSpan(
                    new ForegroundColorSpan(getColor(
                            R.color.edunoor_gold_deep)),
                    0,
                    when.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            TextView line = text(row, 11.5f,
                    getColor(R.color.edunoor_ink_soft), false);
            LinearLayout.LayoutParams lineParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            lineParams.topMargin = dp(3);
            rows.addView(line, lineParams);
        }

        return rows;
    }

    /**
     * Reads the coming week's events (title, begin, all-day) from
     * the device calendar provider. Read-only, nothing stored.
     */
    private List<String[]> readDeviceEvents() {

        List<String[]> events = new ArrayList<>();

        long now = System.currentTimeMillis();
        long until = now + EVENTS_DAYS_AHEAD * 24L * 60L * 60L * 1000L;

        Uri.Builder builder = CalendarContract.Instances
                .CONTENT_URI.buildUpon();
        builder.appendPath("when")
                .appendPath(Long.toString(now))
                .appendPath(Long.toString(until));

        try (Cursor cursor = getContentResolver().query(
                builder.build(),
                new String[]{
                        CalendarContract.Instances.TITLE,
                        CalendarContract.Instances.BEGIN,
                        CalendarContract.Instances.ALL_DAY},
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC"
        )) {
            if (cursor == null) {
                return events;
            }
            while (cursor.moveToNext() && events.size() < 20) {
                events.add(new String[]{
                        cursor.getString(0) == null
                                ? "" : cursor.getString(0),
                        Long.toString(cursor.getLong(1)),
                        Integer.toString(cursor.getInt(2))
                });
            }
        } catch (Exception error) {
            // Permission may have just been revoked or the
            // provider unavailable - the card simply stays quiet.
        }

        return events;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_CALENDAR) {
            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        getString(
                                R.string.calendar_events_granted),
                        Toast.LENGTH_SHORT).show();
            }
            refreshEvents();
        }
    }
}

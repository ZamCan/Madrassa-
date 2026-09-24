package com.zamcan.madrassa.role;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.core.calendar.CalendarActivity;
import com.zamcan.madrassa.core.sound.EduNoorSounds;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.FeeRepository;
import com.zamcan.madrassa.data.repository.LearningProgressRepository;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.ParentRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.data.repository.UstadhRepository;
import com.zamcan.madrassa.ui.components.EduNoorButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Post-login role dashboard - the screen InitializationActivity
 * now hands over to. One shell, two roles:
 *
 * - Parent: own children, their classes and Qur'an/Hifz levels,
 *   outstanding fees (madeni) and learning progress.
 * - Ustadh: the whole madrassa - students, classes, fees and
 *   progress - under the madrassa ID they logged into.
 *
 * Every number is read live from the local store; nothing is
 * mocked and nothing leaves the device.
 */
public class RoleDashboardActivity extends Activity {

    public static final String ROLE_PARENT = "parent";
    public static final String ROLE_USTADH = "ustadh";

    public static final String EXTRA_ROLE = "dashboard_role";
    public static final String EXTRA_MADRASSA_ID =
            "dashboard_madrassa_id";
    public static final String EXTRA_ACTOR_ID =
            "dashboard_actor_id";

    private String role;
    private String madrassaId;
    private String actorId;

    private LinearLayout statRow;

    private int dp(float value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private TextView text(String value, float sizeSp,
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

        role = getIntent().getStringExtra(EXTRA_ROLE);
        madrassaId = getIntent().getStringExtra(EXTRA_MADRASSA_ID);
        actorId = getIntent().getStringExtra(EXTRA_ACTOR_ID);

        int background = getColor(R.color.edunoor_background);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        root.addView(header(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(8), dp(16), dp(20));

        /*
         * WELCOME - resolved live from the local store.
         */
        TextView welcome = text("", 15,
                getColor(R.color.edunoor_walnut), true);
        welcome.setTag("welcome");
        content.addView(welcome, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView madrassaLine = text("", 10.5f,
                getColor(R.color.edunoor_muted), false);
        madrassaLine.setTag("madrassa");
        LinearLayout.LayoutParams madrassaParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        madrassaParams.topMargin = dp(2);
        content.addView(madrassaLine, madrassaParams);

        /*
         * LIVE STATS - filled by loadStats().
         */
        statRow = new LinearLayout(this);
        statRow.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams statParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        statParams.topMargin = dp(14);
        content.addView(statRow, statParams);

        /*
         * SECTION NAVIGATION.
         */
        LinearLayout sections = new LinearLayout(this);
        sections.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sectionsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        sectionsParams.topMargin = dp(16);

        buildSections(sections);
        content.addView(sections, sectionsParams);

        scroll.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);

        loadIdentity(welcome, madrassaLine);
        loadStats();
    }

    /*
     * =========================================================
     * HEADER
     * =========================================================
     */
    private View header() {

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(7), dp(16), dp(7));

        TextView back = text("‹", 30,
                getColor(R.color.edunoor_clay), false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        header.addView(back,
                new LinearLayout.LayoutParams(dp(42), dp(48)));

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.addView(
                text(getString(R.string.dashboard_title), 16,
                        getColor(R.color.edunoor_walnut), true),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        words.addView(
                text(getString(isParent()
                        ? R.string.dashboard_subtitle_parent
                        : R.string.dashboard_subtitle_ustadh),
                        10.5f,
                        getColor(R.color.edunoor_muted), false),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        header.addView(words,
                new LinearLayout.LayoutParams(0, -1, 1));

        TextView chip = text(getString(isParent()
                        ? R.string.role_parent
                        : R.string.role_ustadh),
                10.5f,
                getColor(R.color.edunoor_gold_deep), true);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(
                getDrawable(R.drawable.edunoor_canvas));
        chip.setPadding(dp(10), dp(4), dp(10), dp(4));
        header.addView(chip,
                new LinearLayout.LayoutParams(-2, -2));

        return header;
    }

    private boolean isParent() {
        return ROLE_PARENT.equals(role);
    }

    /*
     * =========================================================
     * IDENTITY - actor + madrassa, live from the store.
     * =========================================================
     */
    private void loadIdentity(
            TextView welcome, TextView madrassaLine) {

        Store.newThread(() -> {

            EduNoorDatabase database =
                    new EduNoorDatabase(this);

            String actorName = "";
            String madrassaName = "";
            String idLine = "";

            if (isParent()) {

                Parent parent = new ParentRepository(database)
                        .findById(actorId);

                if (parent != null) {
                    actorName = parent.fullName;
                    madrassaId = parent.madrassaId != null
                            ? parent.madrassaId
                            : madrassaId;
                }
            } else {

                Ustadh ustadh = new UstadhRepository(this)
                        .findById(actorId);

                if (ustadh != null) {
                    actorName = ustadh.fullName;
                    madrassaId = ustadh.madrassaId != null
                            ? ustadh.madrassaId
                            : madrassaId;
                }
            }

            if (madrassaId != null && !madrassaId.isEmpty()) {
                Madrassa madrassa = new MadrassaRepository(this)
                        .findById(madrassaId);
                if (madrassa != null) {
                    madrassaName = madrassa.name;
                    idLine = getString(
                            R.string.dashboard_madrassa_id,
                            madrassa.id);
                }
            }

            String welcomeText = getString(
                    R.string.dashboard_welcome,
                    actorName.isEmpty()
                            ? getString(isParent()
                            ? R.string.role_parent
                            : R.string.role_ustadh)
                            : actorName);

            String madrassaText = madrassaName
                    + (idLine.isEmpty() ? "" : "  •  " + idLine);

            runOnUiThread(() -> {
                welcome.setText(welcomeText);
                madrassaLine.setText(madrassaText);
            });
        }).start();
    }

    /*
     * =========================================================
     * LIVE STATS - children/students, classes, madeni, progress.
     * =========================================================
     */
    private void loadStats() {

        Store.newThread(() -> {

            EduNoorDatabase database =
                    new EduNoorDatabase(this);

            StudentRepository students =
                    new StudentRepository(database);
            ClassRepository classes =
                    new ClassRepository(database);
            FeeRepository fees = new FeeRepository(database);
            LearningProgressRepository progress =
                    new LearningProgressRepository(database);

            int peopleCount;
            int classCount;
            double madeni;
            int progressCount;

            if (isParent()) {

                List<Student> children =
                        students.findByParent(actorId);
                peopleCount = children.size();

                List<Fee> childFees = new ArrayList<>();
                for (Student child : children) {
                    childFees.addAll(
                            fees.findByStudent(child.id));
                }
                madeni = FeeRepository.outstanding(childFees);

                progressCount = 0;
                for (Student child : children) {
                    progressCount += progress
                            .findByLearner(child.id).size();
                }

                classCount = 0;
                for (Student child : children) {
                    if (child.classId != null
                            && !child.classId.isEmpty()) {
                        classCount++;
                    }
                }
            } else {

                List<Student> all =
                        students.findByMadrassa(madrassaId);
                peopleCount = all.size();
                classCount = classes
                        .findActiveByMadrassa(madrassaId).size();
                madeni = FeeRepository.outstanding(
                        fees.findByMadrassa(madrassaId));

                progressCount = 0;
                for (Student student : all) {
                    progressCount += progress
                            .findByLearner(student.id).size();
                }
            }

            final int fPeople = peopleCount;
            final int fClasses = classCount;
            final double fMadeni = madeni;
            final int fProgress = progressCount;

            runOnUiThread(() -> {

                statRow.removeAllViews();

                LinearLayout row1 = new LinearLayout(this);
                row1.setOrientation(LinearLayout.HORIZONTAL);

                row1.addView(statCell(
                        String.valueOf(fPeople),
                        getString(isParent()
                                ? R.string.stat_children
                                : R.string.stat_students)),
                        cellWeight());

                row1.addView(statCell(
                        String.valueOf(fClasses),
                        getString(R.string.stat_classes)),
                        cellWeight());

                statRow.addView(row1, stretch());

                LinearLayout row2 = new LinearLayout(this);
                row2.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams row2Params =
                        stretch();
                row2Params.topMargin = dp(10);

                row2.addView(statCell(
                        String.format(Locale.ROOT, "%,.0f", fMadeni),
                        getString(R.string.stat_madeni)),
                        cellWeight());

                row2.addView(statCell(
                        String.valueOf(fProgress),
                        getString(R.string.stat_progress)),
                        cellWeight());

                statRow.addView(row2, row2Params);
            });
        }).start();
    }

    private LinearLayout.LayoutParams stretch() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams cellWeight() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, -2, 1);
        params.rightMargin = dp(5);
        return params;
    }

    private View statCell(String value, String label) {

        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView number = text(value, 20,
                getColor(R.color.edunoor_gold_deep), true);
        number.setGravity(Gravity.CENTER);

        TextView name = text(label, 10.5f,
                getColor(R.color.edunoor_muted), false);
        name.setGravity(Gravity.CENTER);

        cell.setBackground(getDrawable(R.drawable.edunoor_canvas));

        cell.addView(number, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        cell.addView(name, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        return cell;
    }

    /*
     * =========================================================
     * SECTIONS - each opens a live list screen.
     * =========================================================
     */
    private void buildSections(LinearLayout sections) {

        if (isParent()) {

            sections.addView(sectionButton(
                    getString(R.string.section_children),
                    RoleListActivity.SECTION_STUDENTS));

            sections.addView(sectionButton(
                    getString(R.string.section_fees),
                    RoleListActivity.SECTION_FEES));

            sections.addView(sectionButton(
                    getString(R.string.section_progress),
                    RoleListActivity.SECTION_PROGRESS));
        } else {

            sections.addView(sectionButton(
                    getString(R.string.section_students),
                    RoleListActivity.SECTION_STUDENTS));

            sections.addView(sectionButton(
                    getString(R.string.section_classes),
                    RoleListActivity.SECTION_CLASSES));

            sections.addView(sectionButton(
                    getString(R.string.section_progress),
                    RoleListActivity.SECTION_PROGRESS));

            sections.addView(sectionButton(
                    getString(R.string.section_fees),
                    RoleListActivity.SECTION_FEES));
        }

        TextView calendar = EduNoorButton.secondary(this,
                getString(R.string.section_calendar));

        LinearLayout.LayoutParams calendarParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(46));
        calendarParams.topMargin = dp(10);

        calendar.setOnClickListener(v -> {
            EduNoorSounds.page(this);
            startActivity(
                    new Intent(this, CalendarActivity.class));
        });

        sections.addView(calendar, calendarParams);
    }

    private View sectionButton(String label, String section) {

        TextView button = EduNoorButton.secondary(this, label);

        button.setOnClickListener(v -> {
            EduNoorSounds.page(this);

            Intent intent =
                    new Intent(this, RoleListActivity.class);
            intent.putExtra(RoleListActivity.EXTRA_SECTION, section);
            intent.putExtra(EXTRA_ROLE, role);
            intent.putExtra(EXTRA_MADRASSA_ID, madrassaId);
            intent.putExtra(EXTRA_ACTOR_ID, actorId);
            startActivity(intent);
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(46));
        params.topMargin = dp(10);

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(button, params);

        return wrap;
    }

    /** Tiny thread factory for store reads. */
    private static final class Store {
        static Thread newThread(Runnable action) {
            return new Thread(action, "edunoor-dashboard");
        }
    }
}

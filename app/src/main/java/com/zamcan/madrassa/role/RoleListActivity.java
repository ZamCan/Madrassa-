package com.zamcan.madrassa.role;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.Lesson;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.FeeRepository;
import com.zamcan.madrassa.data.repository.LearningProgressRepository;
import com.zamcan.madrassa.data.repository.LessonRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.data.repository.UstadhRepository;

import java.util.List;
import java.util.Locale;

/**
 * Live section list behind every dashboard button: students
 * (with class, Qur'an and Hifz levels), classes, fees with
 * statuses, and learning progress. All rows come straight from
 * the local store under the same madrassa ID.
 */
public class RoleListActivity extends Activity {

    public static final String EXTRA_SECTION = "section";

    public static final String SECTION_STUDENTS = "students";
    public static final String SECTION_CLASSES = "classes";
    public static final String SECTION_FEES = "fees";
    public static final String SECTION_PROGRESS = "progress";

    private static final int ROW_CAP = 60;

    private String section;
    private String role;
    private String madrassaId;
    private String actorId;

    private LinearLayout rows;

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

        section = getIntent().getStringExtra(EXTRA_SECTION);
        role = getIntent().getStringExtra(
                RoleDashboardActivity.EXTRA_ROLE);
        madrassaId = getIntent().getStringExtra(
                RoleDashboardActivity.EXTRA_MADRASSA_ID);
        actorId = getIntent().getStringExtra(
                RoleDashboardActivity.EXTRA_ACTOR_ID);

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
        header.addView(back,
                new LinearLayout.LayoutParams(dp(42), dp(48)));

        header.addView(
                text(sectionTitle(), 16,
                        getColor(R.color.edunoor_walnut), true),
                new LinearLayout.LayoutParams(0, -1, 1)
        );

        root.addView(header, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(6), dp(16), dp(20));

        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        content.addView(rows, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        scroll.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);

        loadRows();
    }

    private String sectionTitle() {

        if (SECTION_CLASSES.equals(section)) {
            return getString(R.string.section_classes);
        }
        if (SECTION_FEES.equals(section)) {
            return getString(R.string.section_fees);
        }
        if (SECTION_PROGRESS.equals(section)) {
            return getString(R.string.section_progress);
        }
        return getString(isParent()
                ? R.string.section_children
                : R.string.section_students);
    }

    private boolean isParent() {
        return RoleDashboardActivity.ROLE_PARENT.equals(role);
    }

    private void loadRows() {

        new Thread(() -> {

            EduNoorDatabase database =
                    new EduNoorDatabase(this);

            List<Student> people =
                    new StudentRepository(database)
                            .findByParent(actorId);

            if (!isParent()) {
                people = new StudentRepository(database)
                        .findByMadrassa(madrassaId);
            }

            final List<Student> finalPeople = people;

            runOnUiThread(() -> render(finalPeople));
        }).start();
    }

    private void render(List<Student> people) {

        rows.removeAllViews();

        if (SECTION_CLASSES.equals(section)) {
            renderClasses();
        } else if (SECTION_FEES.equals(section)) {
            renderFees(people);
        } else if (SECTION_PROGRESS.equals(section)) {
            renderProgressPicker(people);
        } else {
            renderStudents(people);
        }
    }

    /*
     * =========================================================
     * STUDENTS - name, class, Qur'an level, Hifz level.
     * =========================================================
     */
    private void renderStudents(List<Student> people) {

        EduNoorDatabase database = new EduNoorDatabase(this);
        ClassRepository classes = new ClassRepository(database);

        if (people.isEmpty()) {
            empty(getString(R.string.list_empty_students));
            return;
        }

        int shown = Math.min(people.size(), ROW_CAP);

        for (int i = 0; i < shown; i++) {

            Student student = people.get(i);

            String className = "";
            if (student.classId != null
                    && !student.classId.isEmpty()) {
                ClassGroup group =
                        classes.findById(student.classId);
                if (group != null) {
                    className = group.name;
                }
            }

            rows.addView(listRow(
                    student.fullName,
                    className,
                    levelChip(student)
            ));
        }

        if (people.size() > ROW_CAP) {
            caption(getString(R.string.list_capped, ROW_CAP));
        }
    }

    /** Qur'an + Hifz levels as one gold chip text. */
    private String levelChip(Student student) {

        StringBuilder chip = new StringBuilder();

        if (student.quranLevel != null
                && !student.quranLevel.isEmpty()) {
            chip.append(getString(R.string.level_quran,
                    student.quranLevel));
        }
        if (student.hifzLevel != null
                && !student.hifzLevel.isEmpty()) {
            if (chip.length() > 0) {
                chip.append("  •  ");
            }
            chip.append(getString(R.string.level_hifz,
                    student.hifzLevel));
        }

        return chip.toString();
    }

    /*
     * =========================================================
     * CLASSES - name, code, teacher.
     * =========================================================
     */
    private void renderClasses() {

        new Thread(() -> {

            EduNoorDatabase database = new EduNoorDatabase(this);

            List<ClassGroup> groups =
                    new ClassRepository(database)
                            .findActiveByMadrassa(madrassaId);

            UstadhRepository ustadhs =
                    new UstadhRepository(this);

            int shown = Math.min(groups.size(), ROW_CAP);

            StringBuilder lines = new StringBuilder();

            for (int i = 0; i < shown; i++) {

                ClassGroup group = groups.get(i);

                String teacher = "";
                if (group.teacherId != null
                        && !group.teacherId.isEmpty()) {
                    com.zamcan.madrassa.data.model.Ustadh ustadh =
                            ustadhs.findById(group.teacherId);
                    if (ustadh != null) {
                        teacher = ustadh.fullName;
                    }
                }

                String line = group.name
                        + (group.code == null
                        || group.code.isEmpty()
                        ? "" : "  (" + group.code + ")")
                        + (teacher.isEmpty()
                        ? "" : " — " + teacher);

                lines.append(line).append('\n');
            }

            runOnUiThread(() -> {

                rows.removeAllViews();

                if (groups.isEmpty()) {
                    empty(getString(R.string.list_empty_classes));
                    return;
                }

                for (int i = 0; i < shown; i++) {

                    ClassGroup group = groups.get(i);

                    rows.addView(listRow(group.name,
                            group.code == null ? "" : group.code,
                            ""));
                }
            });
        }).start();
    }

    /*
     * =========================================================
     * FEES - type, amount, deadline, status; parent scoped to
     * own children, ustadh to the madrassa.
     * =========================================================
     */
    private void renderFees(List<Student> people) {

        new Thread(() -> {

            EduNoorDatabase database = new EduNoorDatabase(this);

            FeeRepository fees = new FeeRepository(database);

            java.util.List<Fee> all = new java.util.ArrayList<>();

            if (isParent()) {
                for (Student child : people) {
                    all.addAll(fees.findByStudent(child.id));
                }
            } else {
                all.addAll(fees.findByMadrassa(madrassaId));
            }

            double madeni = FeeRepository.outstanding(all);

            int shown = Math.min(all.size(), ROW_CAP);

            runOnUiThread(() -> {

                rows.removeAllViews();

                if (all.isEmpty()) {
                    empty(getString(R.string.list_empty_fees));
                    return;
                }

                LinearLayout total = new LinearLayout(this);
                total.setOrientation(LinearLayout.VERTICAL);
                total.setGravity(Gravity.CENTER);
                total.setPadding(dp(14), dp(10), dp(14), dp(10));
                total.setBackground(
                        getDrawable(R.drawable.edunoor_canvas));

                TextView totalLabel = text(
                        getString(R.string.fees_outstanding), 11,
                        getColor(R.color.edunoor_muted), false);
                totalLabel.setGravity(Gravity.CENTER);

                TextView totalValue = text(
                        String.format(Locale.ROOT, "%,.0f", madeni),
                        19,
                        getColor(madeni > 0
                                ? R.color.edunoor_clay
                                : R.color.edunoor_gold_deep),
                        true);
                totalValue.setGravity(Gravity.CENTER);

                total.addView(totalLabel);
                total.addView(totalValue);

                rows.addView(total, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                for (int i = 0; i < shown; i++) {

                    Fee fee = all.get(i);

                    rows.addView(listRow(
                            fee.type + "  •  "
                                    + String.format(
                                    Locale.ROOT, "%,.0f",
                                    fee.amount),
                            fee.deadline == null
                                    ? "" : fee.deadline,
                            feeStatusChip(fee.status)
                    ));
                }

                if (all.size() > ROW_CAP) {
                    caption(getString(
                            R.string.list_capped, ROW_CAP));
                }
            });
        }).start();
    }

    private String feeStatusChip(
            com.zamcan.madrassa.data.model.FeeStatus status) {

        switch (status) {
            case PAID:
            case CONFIRMED:
                return getString(R.string.fee_paid);
            case PENDING:
                return getString(R.string.fee_pending);
            case OVERDUE:
                return getString(R.string.fee_overdue);
            case CANCELLED:
                return getString(R.string.fee_cancelled);
            case LOCKED:
                return getString(R.string.fee_locked);
            default:
                return getString(R.string.fee_unpaid);
        }
    }

    /*
     * =========================================================
     * PROGRESS - parent: pick a child; ustadh: pick a student;
     * then that learner's lesson progress rows.
     * =========================================================
     */
    private void renderProgressPicker(List<Student> people) {

        if (people.isEmpty()) {
            empty(getString(R.string.list_empty_students));
            return;
        }

        String[] names = new String[people.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = people.get(i).fullName;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.progress_pick))
                .setItems(names, (dialog, which) ->
                        renderProgress(people.get(which)))
                .setNegativeButton(
                        getString(R.string.registration_cancel),
                        (dialog, which) -> finish())
                .show();
    }

    private void renderProgress(Student student) {

        new Thread(() -> {

            EduNoorDatabase database = new EduNoorDatabase(this);

            List<LearningProgress> progress =
                    new LearningProgressRepository(database)
                            .findByLearner(student.id);

            LessonRepository lessons =
                    new LessonRepository(database);

            int shown = Math.min(progress.size(), ROW_CAP);

            String[] titles = new String[shown];
            String[] states = new String[shown];

            for (int i = 0; i < shown; i++) {

                LearningProgress entry = progress.get(i);

                String lessonName = entry.lessonId;
                Lesson lesson = lessons.findById(entry.lessonId);
                if (lesson != null && lesson.name != null) {
                    lessonName = lesson.name;
                }

                titles[i] = lessonName;
                states[i] = progressChip(entry.status);
            }

            runOnUiThread(() -> {

                rows.removeAllViews();

                rows.addView(listRow(
                        student.fullName,
                        getString(R.string.progress_of),
                        ""));

                if (progress.isEmpty()) {
                    empty(getString(
                            R.string.list_empty_progress));
                    return;
                }

                for (int i = 0; i < shown; i++) {
                    rows.addView(
                            listRow(titles[i], "", states[i]));
                }

                if (progress.size() > ROW_CAP) {
                    caption(getString(
                            R.string.list_capped, ROW_CAP));
                }
            });
        }).start();
    }

    private String progressChip(
            com.zamcan.madrassa.data.model.ProgressStatus status) {

        switch (status) {
            case STARTED:
                return getString(R.string.progress_started);
            case IN_PROGRESS:
                return getString(R.string.progress_in_progress);
            case COMPLETED:
                return getString(R.string.progress_completed);
            case REVIEWING:
                return getString(R.string.progress_reviewing);
            case NEEDS_REVIEW:
                return getString(R.string.progress_needs_review);
            default:
                return getString(R.string.progress_not_started);
        }
    }

    /*
     * =========================================================
     * ROW / STATE HELPERS
     * =========================================================
     */
    private View listRow(String title, String subtitle,
                         String chip) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(10), dp(14), dp(10));

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(8);

        LinearLayout line = new LinearLayout(this);
        line.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = text(title, 13,
                getColor(R.color.edunoor_ink), true);
        titleView.setGravity(Gravity.CENTER_VERTICAL);

        line.addView(titleView, new LinearLayout.LayoutParams(
                0, -2, 1));

        if (chip != null && !chip.isEmpty()) {
            TextView chipView = text(chip, 10,
                    getColor(R.color.edunoor_gold_deep), true);
            chipView.setGravity(Gravity.END);
            line.addView(chipView, new LinearLayout.LayoutParams(
                    -2, -2));
        }

        row.addView(line, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subtitleView = text(subtitle, 10.5f,
                    getColor(R.color.edunoor_muted), false);
            LinearLayout.LayoutParams subtitleParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            subtitleParams.topMargin = dp(2);
            row.addView(subtitleView, subtitleParams);
        }

        row.setBackground(getDrawable(R.drawable.edunoor_canvas));

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(row, rowParams);

        return wrap;
    }

    private void empty(String message) {

        TextView empty = text(message, 12.5f,
                getColor(R.color.edunoor_muted), false);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, dp(28), 0, dp(28));

        rows.addView(empty, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void caption(String message) {

        TextView note = text(message, 10,
                getColor(R.color.edunoor_muted), false);
        note.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);

        rows.addView(note, params);
    }
}

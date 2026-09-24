package com.zamcan.madrassa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.FeeRepository;
import com.zamcan.madrassa.data.repository.LearningProgressRepository;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.ParentRepository;
import com.zamcan.madrassa.data.repository.ProgrammeRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.data.repository.UstadhRepository;
import com.zamcan.madrassa.domain.authorization.UstadhAccessPolicy;
import com.zamcan.madrassa.domain.authorization.parent.ParentAccessPolicy;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.EduNoorCard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Read-only, tenant-scoped role landing surface.
 *
 * <p>The screen presents records that are already in the local database;
 * it does not pretend that an unbuilt CRUD workflow succeeded. Parent
 * data is filtered through the same parent/child policy used by the
 * domain layer, while Ustadh data is bound to the authenticated Ustadh
 * and Madrassa IDs.</p>
 */
public final class RoleDashboardActivity extends Activity {

    private static final String EXTRA_ROLE = "edunoor_role";
    private static final String EXTRA_MADRASSA_ID = "edunoor_madrassa_id";
    private static final String EXTRA_PARENT_ID = "edunoor_parent_id";
    private static final String EXTRA_USTADH_ID = "edunoor_ustadh_id";

    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler();

    private String role;
    private String madrassaId;
    private String parentId;
    private String ustadhId;
    private LinearLayout content;
    private TextView scopeSubtitle;

    public static Intent forParent(
            Activity activity,
            String parentId,
            String madrassaId
    ) {
        return new Intent(activity, RoleDashboardActivity.class)
                .putExtra(EXTRA_ROLE, "parent")
                .putExtra(EXTRA_PARENT_ID, parentId)
                .putExtra(EXTRA_MADRASSA_ID, madrassaId);
    }

    public static Intent forUstadh(
            Activity activity,
            String madrassaId,
            String ustadhId
    ) {
        return new Intent(activity, RoleDashboardActivity.class)
                .putExtra(EXTRA_ROLE, "ustadh")
                .putExtra(EXTRA_MADRASSA_ID, madrassaId)
                .putExtra(EXTRA_USTADH_ID, ustadhId);
    }

    private int dp(float value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private TextView text(
            CharSequence value,
            float size,
            int color,
            boolean bold
    ) {
        TextView view = new TextView(this);
        view.setText(value == null ? "" : value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        view.setTypeface(Typeface.create(
                "sans",
                bold ? Typeface.BOLD : Typeface.NORMAL
        ));
        view.setIncludeFontPadding(true);
        return view;
    }

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        role = getIntent().getStringExtra(EXTRA_ROLE);
        madrassaId = getIntent().getStringExtra(EXTRA_MADRASSA_ID);
        parentId = getIntent().getStringExtra(EXTRA_PARENT_ID);
        ustadhId = getIntent().getStringExtra(EXTRA_USTADH_ID);

        int background = getColor(R.color.edunoor_background);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.edunoor_landing_bg);
        root.setPadding(dp(16), dp(10), dp(16), dp(18));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("‹", 30,
                getColor(R.color.edunoor_clay), false);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription(getString(R.string.navigation_back));
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(44), dp(48)));

        TextView title = text(
                "parent".equals(role)
                        ? getString(R.string.dashboard_parent)
                        : getString(R.string.dashboard_ustadh),
                21,
                getColor(R.color.edunoor_walnut),
                true
        );
        header.addView(title, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        root.addView(header, new LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        scopeSubtitle = text(
                getString(R.string.dashboard_local_scope),
                11,
                getColor(R.color.edunoor_muted),
                false
        );
        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(4);
        root.addView(scopeSubtitle, subtitleParams);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(12), 0, 0);

        TextView loading = text(
                getString(R.string.dashboard_loading),
                12,
                getColor(R.color.edunoor_muted),
                false
        );
        loading.setGravity(Gravity.CENTER);
        content.addView(loading, new LinearLayout.LayoutParams(-1, dp(64)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        if (!validIntent()) {
            showDenied();
            return;
        }

        worker.execute(this::loadSnapshot);
    }

    private boolean validIntent() {
        if (madrassaId == null || madrassaId.trim().isEmpty()) {
            return false;
        }

        if ("parent".equals(role)) {
            return parentId != null && !parentId.trim().isEmpty();
        }

        return "ustadh".equals(role)
                && ustadhId != null
                && !ustadhId.trim().isEmpty();
    }

    private void showDenied() {
        content.removeAllViews();
        TextView message = text(
                getString(R.string.dashboard_access_denied),
                13,
                getColor(R.color.edunoor_clay),
                true
        );
        message.setGravity(Gravity.CENTER);
        content.addView(message, new LinearLayout.LayoutParams(-1, dp(100)));
    }

    private void loadSnapshot() {
        Snapshot snapshot = null;
        String error = null;

        try {
            EduNoorDatabase database = new EduNoorDatabase(this);
            Madrassa madrassa =
                    new MadrassaRepository(this).findById(madrassaId);

            if (madrassa == null
                    || madrassa.approvalStatus != ApprovalStatus.ACTIVE) {
                throw new SecurityException("Madrassa scope is unavailable.");
            }

            if ("parent".equals(role)) {
                snapshot = loadParent(database, madrassa);
            } else {
                snapshot = loadUstadh(database, madrassa);
            }
        } catch (RuntimeException exception) {
            error = getString(R.string.dashboard_data_unavailable);
        }

        final Snapshot result = snapshot;
        final String finalError = error;
        ui.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (result == null) {
                showError(finalError);
            } else {
                render(result);
            }
        });
    }

    private Snapshot loadParent(
            EduNoorDatabase database,
            Madrassa madrassa
    ) {
        Parent parent = new ParentRepository(database).findById(parentId);

        if (parent == null
                || !parent.active
                || !TenantPolicy.sameMadrassa(
                parent.madrassaId,
                madrassa.id
        )) {
            throw new SecurityException("Parent scope is unavailable.");
        }

        StudentRepository students = new StudentRepository(database);
        List<Student> children = new ArrayList<>();

        for (Student student : students.findByParent(parent.id)) {
            if (ParentAccessPolicy.canAccessStudent(parent, student)
                    && TenantPolicy.sameMadrassa(
                    student.madrassaId,
                    madrassa.id
            )) {
                children.add(student);
            }
        }

        List<Fee> fees = new ArrayList<>();
        FeeRepository feeRepository = new FeeRepository(database);
        for (Fee fee : feeRepository.findByMadrassa(madrassa.id)) {
            if (containsStudent(children, fee.studentId)
                    && TenantPolicy.sameMadrassa(
                    fee.madrassaId,
                    madrassa.id
            )) {
                fees.add(fee);
            }
        }

        int progressCount = countProgress(
                new LearningProgressRepository(database),
                children,
                madrassa.id
        );

        return new Snapshot(
                madrassa.name,
                children,
                new ClassRepository(database).findByMadrassa(madrassa.id),
                new ArrayList<>(),
                fees,
                progressCount,
                false
        );
    }

    private Snapshot loadUstadh(
            EduNoorDatabase database,
            Madrassa madrassa
    ) {
        Ustadh ustadh = new UstadhRepository(getApplicationContext())
                .findById(ustadhId);

        if (ustadh == null
                || !ustadh.active
                || !UstadhAccessPolicy.belongsToMadrassa(
                ustadh,
                madrassa.id
        )) {
            throw new SecurityException("Ustadh scope is unavailable.");
        }

        StudentRepository students = new StudentRepository(database);
        List<Student> allStudents =
                students.findByMadrassa(madrassa.id);
        List<Fee> fees =
                new FeeRepository(database).findByMadrassa(madrassa.id);
        int progressCount = countProgress(
                new LearningProgressRepository(database),
                allStudents,
                madrassa.id
        );

        return new Snapshot(
                madrassa.name,
                allStudents,
                new ClassRepository(database).findByMadrassa(madrassa.id),
                new ProgrammeRepository(database).findByMadrassa(madrassa.id),
                fees,
                progressCount,
                true
        );
    }

    private int countProgress(
            LearningProgressRepository repository,
            List<Student> students,
            String tenantId
    ) {
        int count = 0;
        for (Student student : students) {
            for (LearningProgress progress :
                    repository.findByLearner(student.id)) {
                if (TenantPolicy.sameMadrassa(
                        progress.madrassaId,
                        tenantId
                )) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean containsStudent(
            List<Student> students,
            String studentId
    ) {
        for (Student student : students) {
            if (student.id.equals(studentId)) {
                return true;
            }
        }
        return false;
    }

    private void showError(String message) {
        content.removeAllViews();
        TextView view = text(
                message == null
                        ? getString(R.string.dashboard_data_unavailable)
                        : message,
                13,
                getColor(R.color.edunoor_clay),
                true
        );
        view.setGravity(Gravity.CENTER);
        content.addView(view, new LinearLayout.LayoutParams(-1, dp(100)));
    }

    private void render(Snapshot snapshot) {
        content.removeAllViews();

        if (scopeSubtitle != null) {
            scopeSubtitle.setText(getString(
                    R.string.dashboard_scope_value,
                    snapshot.madrassaName
            ));
        }

        addSummary(snapshot);

        if (snapshot.ustadh) {
            addAction(
                    getString(R.string.dashboard_view_students),
                    getString(R.string.dashboard_students_subtitle),
                    () -> showStudents(snapshot.students)
            );
            addAction(
                    getString(R.string.dashboard_view_classes),
                    getString(R.string.dashboard_classes_subtitle),
                    () -> showClasses(snapshot.classes, snapshot.students)
            );
            addAction(
                    getString(R.string.dashboard_view_academic),
                    getString(R.string.dashboard_academic_subtitle),
                    () -> showAcademic(snapshot.programmes, snapshot.students)
            );
            addAction(
                    getString(R.string.dashboard_view_fees),
                    getString(R.string.dashboard_fees_subtitle),
                    () -> showFees(snapshot.fees, snapshot.students)
            );
        } else {
            addAction(
                    getString(R.string.dashboard_view_children),
                    getString(R.string.dashboard_children_subtitle),
                    () -> showStudents(snapshot.students)
            );
            addAction(
                    getString(R.string.dashboard_view_fees),
                    getString(R.string.dashboard_fees_subtitle),
                    () -> showFees(snapshot.fees, snapshot.students)
            );
        }

        addAction(
                getString(R.string.dashboard_salah_qibla),
                getString(R.string.dashboard_salah_subtitle),
                () -> startActivity(new Intent(
                        this,
                        IslamicToolsActivity.class
                ))
        );
        addAction(
                getString(R.string.dashboard_calendar),
                getString(R.string.dashboard_calendar_subtitle),
                () -> startActivity(new Intent(
                        this,
                        com.zamcan.madrassa.core.calendar.CalendarActivity.class
                ))
        );

        TextView landing = EduNoorButton.secondary(
                this,
                getString(R.string.dashboard_back_landing)
        );
        LinearLayout.LayoutParams landingParams =
                new LinearLayout.LayoutParams(-1, -2);
        landingParams.topMargin = dp(10);
        landing.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        content.addView(landing, landingParams);
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        return params;
    }

    private void addSummary(Snapshot snapshot) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(10));
        panel.setBackgroundResource(R.drawable.edunoor_canvas);

        TextView title = text(
                getString(R.string.dashboard_snapshot),
                15,
                getColor(R.color.edunoor_walnut),
                true
        );
        panel.addView(title);

        addSummaryRow(
                panel,
                getString(R.string.dashboard_students),
                String.valueOf(snapshot.students.size())
        );
        addSummaryRow(
                panel,
                getString(R.string.dashboard_classes),
                String.valueOf(snapshot.classes.size())
        );
        addSummaryRow(
                panel,
                getString(R.string.dashboard_programmes),
                String.valueOf(snapshot.programmes.size())
        );
        addSummaryRow(
                panel,
                getString(R.string.dashboard_fees),
                String.valueOf(snapshot.fees.size())
        );
        addSummaryRow(
                panel,
                getString(R.string.dashboard_progress),
                String.valueOf(snapshot.progressCount)
        );

        content.addView(panel, cardParams());
    }

    private void addSummaryRow(
            LinearLayout panel,
            String label,
            String value
    ) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(34));

        TextView labelView = text(
                label,
                12.5f,
                getColor(R.color.edunoor_muted),
                false
        );
        row.addView(labelView, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView valueView = text(
                value,
                14,
                getColor(R.color.edunoor_walnut),
                true
        );
        valueView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        row.addView(valueView, new LinearLayout.LayoutParams(
                dp(48),
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        panel.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addAction(
            String title,
            String subtitle,
            Runnable action
    ) {
        LinearLayout card = EduNoorCard.create(
                this,
                "",
                title,
                subtitle,
                "›"
        );
        card.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(8);
        content.addView(card, params);
    }

    private void showStudents(List<Student> students) {
        StringBuilder message = new StringBuilder();
        for (Student student : students) {
            if (message.length() > 0) {
                message.append("\n\n");
            }
            message.append(student.fullName == null
                    ? getString(R.string.dashboard_unnamed_student)
                    : student.fullName);
            message.append("\n")
                    .append(getString(R.string.dashboard_class_value))
                    .append(": ")
                    .append(student.classId == null
                            ? getString(R.string.dashboard_not_available)
                            : student.classId)
                    .append("\n")
                    .append(getString(R.string.dashboard_quran_level))
                    .append(": ")
                    .append(student.quranLevel == null
                            ? getString(R.string.dashboard_not_available)
                            : student.quranLevel);
        }
        showDialog(
                getString(R.string.dashboard_students),
                message.length() == 0
                        ? getString(R.string.dashboard_no_students)
                        : message.toString()
        );
    }

    private void showClasses(
            List<ClassGroup> classes,
            List<Student> students
    ) {
        StringBuilder message = new StringBuilder();
        for (ClassGroup group : classes) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(group.name == null
                    ? getString(R.string.dashboard_not_available)
                    : group.name);
            if (group.code != null && !group.code.trim().isEmpty()) {
                message.append("  (").append(group.code).append(")");
            }
            int count = 0;
            for (Student student : students) {
                if (group.id.equals(student.classId)) {
                    count++;
                }
            }
            message.append("  •  ").append(count);
        }
        showDialog(
                getString(R.string.dashboard_classes),
                message.length() == 0
                        ? getString(R.string.dashboard_no_classes)
                        : message.toString()
        );
    }

    private void showAcademic(
            List<Programme> programmes,
            List<Student> students
    ) {
        StringBuilder message = new StringBuilder();
        for (Programme programme : programmes) {
            if (message.length() > 0) {
                message.append("\n\n");
            }
            message.append(programme.name);
            if (!programme.active) {
                message.append("  •  ")
                        .append(getString(R.string.dashboard_inactive));
            }
        }
        if (message.length() == 0) {
            message.append(getString(R.string.dashboard_no_programmes));
        }
        showDialog(
                getString(R.string.dashboard_academic),
                message.toString()
        );
    }

    private void showFees(
            List<Fee> fees,
            List<Student> students
    ) {
        StringBuilder message = new StringBuilder();
        for (Fee fee : fees) {
            if (message.length() > 0) {
                message.append("\n");
            }
            String studentName = fee.studentId;
            for (Student student : students) {
                if (student.id.equals(fee.studentId)) {
                    studentName = student.fullName;
                    break;
                }
            }
            message.append(studentName)
                    .append("  •  ")
                    .append(fee.type)
                    .append("  •  ")
                    .append(feeStatusLabel(fee.status));
        }
        showDialog(
                getString(R.string.dashboard_fees),
                message.length() == 0
                        ? getString(R.string.dashboard_no_fees)
                        : message.toString()
        );
    }

    private String feeStatusLabel(com.zamcan.madrassa.data.model.FeeStatus status) {
        if (status == null) {
            return getString(R.string.dashboard_not_available);
        }

        switch (status) {
            case UNPAID:
                return getString(R.string.fee_status_unpaid);
            case PENDING:
                return getString(R.string.fee_status_pending);
            case CONFIRMED:
                return getString(R.string.fee_status_confirmed);
            case PAID:
                return getString(R.string.fee_status_paid);
            case OVERDUE:
                return getString(R.string.fee_status_overdue);
            case CANCELLED:
                return getString(R.string.fee_status_cancelled);
            case LOCKED:
                return getString(R.string.fee_status_locked);
            default:
                return getString(R.string.dashboard_not_available);
        }
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        worker.shutdownNow();
        ui.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static final class Snapshot {
        final String madrassaName;
        final List<Student> students;
        final List<ClassGroup> classes;
        final List<Programme> programmes;
        final List<Fee> fees;
        final int progressCount;
        final boolean ustadh;

        Snapshot(
                String madrassaName,
                List<Student> students,
                List<ClassGroup> classes,
                List<Programme> programmes,
                List<Fee> fees,
                int progressCount,
                boolean ustadh
        ) {
            this.madrassaName = madrassaName;
            this.students = students;
            this.classes = classes;
            this.programmes = programmes;
            this.fees = fees;
            this.progressCount = progressCount;
            this.ustadh = ustadh;
        }
    }
}

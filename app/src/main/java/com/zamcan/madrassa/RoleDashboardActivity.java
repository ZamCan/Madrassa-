package com.zamcan.madrassa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.FeeRepository;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.EduNoorCard;

import java.util.List;

public final class RoleDashboardActivity extends Activity {
    private static final String ROLE = "role";
    private static final String MADRASSA_ID = "madrassa_id";
    private static final String PARENT_ID = "parent_id";
    private String role, madrassaId, parentId;
    private EduNoorDatabase database;
    private StudentRepository students;
    private ClassRepository classes;
    private FeeRepository fees;

    public static Intent forUstadh(Activity a, String madrassaId) {
        return new Intent(a, RoleDashboardActivity.class)
                .putExtra(ROLE, "ustadh").putExtra(MADRASSA_ID, madrassaId);
    }

    public static Intent forParent(Activity a, String parentId, String madrassaId) {
        return new Intent(a, RoleDashboardActivity.class)
                .putExtra(ROLE, "parent").putExtra(PARENT_ID, parentId)
                .putExtra(MADRASSA_ID, madrassaId);
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        return t;
    }

    @Override protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LanguageManager.wrap(base));
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        role = getIntent().getStringExtra(ROLE);
        madrassaId = getIntent().getStringExtra(MADRASSA_ID);
        parentId = getIntent().getStringExtra(PARENT_ID);

        database = new EduNoorDatabase(this);
        students = new StudentRepository(database);
        classes = new ClassRepository(database);
        fees = new FeeRepository(database);

        build();
    }

    private void build() {
        int bg=getColor(R.color.edunoor_background), surface=getColor(R.color.edunoor_surface);
        int walnut=getColor(R.color.edunoor_walnut), clay=getColor(R.color.edunoor_clay);
        int muted=getColor(R.color.edunoor_muted), gold=getColor(R.color.edunoor_gold);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(12),dp(16),dp(18));
        root.setBackgroundResource(R.drawable.edunoor_landing_bg);

        TextView title=text(
                "ustadh".equals(role) ? getString(R.string.dashboard_ustadh) : getString(R.string.dashboard_parent),
                22, walnut, true);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));

        TextView subtitle=text(getString(R.string.dashboard_local_madrassa),12,muted,false);
        root.addView(subtitle,new LinearLayout.LayoutParams(-1,dp(30)));

        List<Student> list = "parent".equals(role) ? students.findByParent(parentId) : students.findByMadrassa(madrassaId);
        int classCount=classes.findByMadrassa(madrassaId).size();
        List<Fee> feeList=fees.findByMadrassa(madrassaId);
        int debt=0;
        for(Fee f:feeList) if(f.status != null && f.status.name().equals("UNPAID")) debt++;

        LinearLayout summary=new LinearLayout(this);
        summary.setOrientation(LinearLayout.VERTICAL);
        summary.addView(metric(getString(R.string.dashboard_students),String.valueOf(list.size()),surface,walnut));
        summary.addView(metric(getString(R.string.dashboard_classes),String.valueOf(classCount),surface,clay));
        summary.addView(metric(getString(R.string.dashboard_debts),String.valueOf("parent".equals(role)?parentDebts(list):debt),surface,gold));
        root.addView(summary);

        TextView section=text(getString(R.string.dashboard_navigation),16,walnut,true);
        section.setPadding(0,dp(14),0,dp(6)); root.addView(section);

        if("ustadh".equals(role)) {
            addAction(root,getString(R.string.dashboard_management),getString(R.string.dashboard_management_sub),()->startActivity(new Intent(this,MadrassaOperationsActivity.class).putExtra("madrassa_id",madrassaId)));
            addAction(root,getString(R.string.dashboard_students),getString(R.string.dashboard_students_sub),()->showStudents(list));
            addAction(root,getString(R.string.dashboard_classes),getString(R.string.dashboard_classes_sub),()->showClasses());
            addAction(root,getString(R.string.dashboard_progress),getString(R.string.dashboard_progress_sub),()->showStudents(list));
            addAction(root,getString(R.string.dashboard_fees),getString(R.string.dashboard_fees_sub),()->showFees(feeList));
            addAction(root,getString(R.string.dashboard_quran_levels),getString(R.string.dashboard_quran_levels_sub),()->showLevels(list));
        } else {
            addAction(root,getString(R.string.dashboard_children),getString(R.string.dashboard_children_sub),()->showStudents(list));
            addAction(root,getString(R.string.dashboard_progress),getString(R.string.dashboard_progress_sub),()->showLevels(list));
            addAction(root,getString(R.string.dashboard_fees),getString(R.string.dashboard_fees_sub),()->showFeesForStudents(list));
            addAction(root,getString(R.string.dashboard_classes),getString(R.string.dashboard_classes_sub),()->showClasses());
            addAction(root,getString(R.string.dashboard_quran_levels),getString(R.string.dashboard_quran_levels_sub),()->showLevels(list));
        }

        addAction(root,getString(R.string.solo_salah),getString(R.string.dashboard_salah_sub),()->startActivity(new Intent(this,SoloLearningActivity.class)));
        addAction(root,getString(R.string.dashboard_calendar),getString(R.string.dashboard_calendar_sub),()->startActivity(new Intent(this,com.zamcan.madrassa.core.calendar.CalendarActivity.class)));

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true); scroll.addView(root);
        setContentView(scroll);
    }

    private TextView metric(String label,String value,int bg,int accent){
        TextView t=text(label+"  •  "+value,14,accent,true);
        t.setPadding(dp(14),dp(11),dp(14),dp(11));
        android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();
        d.setColor(bg); d.setCornerRadius(dp(12)); t.setBackground(d);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48)); p.bottomMargin=dp(7); t.setLayoutParams(p);
        return t;
    }

    private void addAction(LinearLayout root,String title,String sub,final Runnable action){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        android.graphics.drawable.GradientDrawable cardBg=new android.graphics.drawable.GradientDrawable();
        cardBg.setColor(getColor(R.color.edunoor_surface));
        cardBg.setCornerRadius(dp(12));
        cardBg.setStroke(dp(1),getColor(R.color.edunoor_border));
        card.setBackground(cardBg);
        card.setPadding(dp(14),dp(8),dp(14),dp(8));
        TextView a=text(title,15,getColor(R.color.edunoor_walnut),true);
        TextView b=text(sub,11,getColor(R.color.edunoor_muted),false);
        card.addView(a,new LinearLayout.LayoutParams(-1,dp(30)));
        card.addView(b,new LinearLayout.LayoutParams(-1,dp(34)));
        card.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(72)); p.bottomMargin=dp(8); root.addView(card,p);
    }

    private int parentDebts(List<Student> list){
        int n=0; for(Student s:list) for(Fee f:fees.findByStudent(s.id)) if(f.status!=null&&f.status.name().equals("UNPAID")) n++; return n;
    }

    private void showManagement(List<Student> list,int classCount,List<Fee> feeList){
        show(getString(R.string.dashboard_management),getString(R.string.dashboard_management_detail,list.size(),classCount,feeList.size()));
    }

    private void showStudents(List<Student> list){
        StringBuilder b=new StringBuilder();
        for(Student s:list) b.append(s.fullName).append(" — ").append(s.quranLevel)
                .append(" / ").append(s.hifzLevel).append("\n");
        show(getString(R.string.dashboard_students),b.length()==0?getString(R.string.dashboard_none):b.toString());
    }

    private void showClasses(){ show(getString(R.string.dashboard_classes),joinClasses()); }
    private String joinClasses(){ StringBuilder b=new StringBuilder(); for(com.zamcan.madrassa.data.model.ClassGroup c:classes.findByMadrassa(madrassaId)) b.append(c.code==null?"":c.code+" — ").append(c.name).append("\n"); return b.length()==0?getString(R.string.dashboard_none):b.toString(); }

    private void showFees(List<Fee> list){ StringBuilder b=new StringBuilder(); for(Fee f:list) b.append(f.type).append(" • ").append(f.amount).append(" • ").append(f.status).append("\n"); show(getString(R.string.dashboard_fees),b.length()==0?getString(R.string.dashboard_none):b.toString()); }
    private void showFeesForStudents(List<Student> list){ StringBuilder b=new StringBuilder(); for(Student s:list) for(Fee f:fees.findByStudent(s.id)) b.append(s.fullName).append(" • ").append(f.type).append(" • ").append(f.status).append("\n"); show(getString(R.string.dashboard_fees),b.length()==0?getString(R.string.dashboard_none):b.toString()); }
    private void showLevels(List<Student> list){ StringBuilder b=new StringBuilder(); for(Student s:list) b.append(s.fullName).append("\n  Qur'an: ").append(s.quranLevel).append("\n  Hifz: ").append(s.hifzLevel).append("\n"); show(getString(R.string.dashboard_quran_levels),b.length()==0?getString(R.string.dashboard_none):b.toString()); }
    private void show(String title,String message){ new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton(getString(R.string.dialog_ok),null).show(); }
}
package com.zamcan.madrassa;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.FeeRepository;
import com.zamcan.madrassa.data.repository.LearningProgressRepository;
import com.zamcan.madrassa.data.repository.ProgrammeRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.domain.academic.ClassManagementService;
import com.zamcan.madrassa.domain.authorization.MadrassaAccessContext;
import com.zamcan.madrassa.domain.common.IdGenerator;
import com.zamcan.madrassa.domain.students.StudentClassAssignmentService;
import java.util.List;

public final class MadrassaOperationsActivity extends Activity {
    private String madrassaId;
    private EduNoorDatabase db;
    private StudentRepository students;
    private ClassRepository classes;
    private FeeRepository fees;
    private LearningProgressRepository progress;
    private ProgrammeRepository programmes;

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView t(String s,float z,boolean b){
        TextView v=new TextView(this); v.setText(s); v.setTextSize(z);
        v.setTextColor(getColor(R.color.edunoor_walnut)); v.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        v.setTypeface(android.graphics.Typeface.create("sans",b?1:0)); return v;
    }
    @Override protected void attachBaseContext(android.content.Context c){super.attachBaseContext(LanguageManager.wrap(c));}

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        madrassaId=getIntent().getStringExtra("madrassa_id");
        if(madrassaId==null||madrassaId.trim().isEmpty()){finish();return;}
        db=new EduNoorDatabase(this);
        students=new StudentRepository(db); classes=new ClassRepository(db); fees=new FeeRepository(db);
        progress=new LearningProgressRepository(db); programmes=new ProgrammeRepository(db);
        build();
    }

    private void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(12),dp(16),dp(20)); root.setBackgroundResource(R.drawable.edunoor_landing_bg);
        TextView back=t("‹",30,true); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->finish());
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(44)));
        root.addView(t(getString(R.string.dashboard_management),23,true),new LinearLayout.LayoutParams(-1,dp(48)));
        root.addView(t(getString(R.string.dashboard_management_detail,students.findByMadrassa(madrassaId).size(),classes.findByMadrassa(madrassaId).size(),fees.findByMadrassa(madrassaId).size()),12,false),new LinearLayout.LayoutParams(-1,dp(40)));

        action(root,getString(R.string.dashboard_students),getString(R.string.dashboard_students_sub),()->showStudents());
        action(root,getString(R.string.dashboard_classes),getString(R.string.dashboard_classes_sub),()->showClasses());
        action(root,getString(R.string.dashboard_progress),getString(R.string.dashboard_progress_sub),()->showProgress());
        action(root,getString(R.string.dashboard_fees),getString(R.string.dashboard_fees_sub),()->showFees());
        action(root,getString(R.string.dashboard_quran_levels),getString(R.string.dashboard_quran_levels_sub),()->showLevels());
        action(root,getString(R.string.dashboard_programmes),getString(R.string.dashboard_programmes_sub),()->showProgrammes());
        action(root,getString(R.string.dashboard_academic_content),getString(R.string.dashboard_academic_content_sub),()->showAcademicContent());
        action(root,getString(R.string.dashboard_add_class),getString(R.string.dashboard_add_class_sub),()->addClass());

        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.addView(root); setContentView(scroll);
    }

    private void action(LinearLayout root,String a,String b,final Runnable r){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(14),dp(7),dp(14),dp(7));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        bg.setColor(getColor(R.color.edunoor_surface)); bg.setCornerRadius(dp(14)); bg.setStroke(dp(1),getColor(R.color.edunoor_border)); c.setBackground(bg);
        TextView x=t(a,15,true), y=t(b,11,false); c.addView(x,new LinearLayout.LayoutParams(-1,dp(30))); c.addView(y,new LinearLayout.LayoutParams(-1,dp(34)));
        c.setOnClickListener(v->r.run()); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(72));p.bottomMargin=dp(8);root.addView(c,p);
    }
    private void showStudents(){StringBuilder b=new StringBuilder(); for(Student s:students.findByMadrassa(madrassaId)) b.append(s.fullName).append("\n  Class: ").append(className(s.classId)).append("\n  Qur'an: ").append(s.quranLevel).append("  •  Hifz: ").append(s.hifzLevel).append("\n\n"); show(getString(R.string.dashboard_students),empty(b));}
    private void showLevels(){StringBuilder b=new StringBuilder(); for(Student s:students.findByMadrassa(madrassaId)) b.append(s.fullName).append(" — ").append(s.quranLevel).append(" / ").append(s.hifzLevel).append("\n"); show(getString(R.string.dashboard_quran_levels),empty(b));}
    private void showClasses(){StringBuilder b=new StringBuilder(); for(ClassGroup c:classes.findByMadrassa(madrassaId)) b.append(c.code==null?"":c.code+" — ").append(c.name).append(" • ").append(countInClass(c.id)).append(" students\n"); show(getString(R.string.dashboard_classes),empty(b));}
    private void showProgress(){StringBuilder b=new StringBuilder(); for(Student s:students.findByMadrassa(madrassaId)){List<LearningProgress> p=progress.findByLearner(s.id);b.append(s.fullName).append(": "); if(p.isEmpty()) b.append("NO_PROGRESS"); else {int done=0,started=0,review=0;for(LearningProgress x:p){if(x.status==com.zamcan.madrassa.data.model.ProgressStatus.COMPLETED)done++;if(x.status==com.zamcan.madrassa.data.model.ProgressStatus.IN_PROGRESS)started++;if(x.status==com.zamcan.madrassa.data.model.ProgressStatus.REVIEWING||x.status==com.zamcan.madrassa.data.model.ProgressStatus.NEEDS_REVIEW)review++;}b.append("completed=").append(done).append(", in-progress=").append(started).append(", review=").append(review);}b.append("\n");}show(getString(R.string.dashboard_progress),empty(b));}
    private void showFees(){StringBuilder b=new StringBuilder();for(Fee f:fees.findByMadrassa(madrassaId))if(f.status!=null&&f.status.name().equals("UNPAID"))b.append(f.studentId).append(" • ").append(f.type).append(" • ").append(f.amount).append("\n");show(getString(R.string.dashboard_fees),empty(b));}
    private void showProgrammes(){StringBuilder b=new StringBuilder();for(Programme p:programmes.findByMadrassa(madrassaId))b.append(p.name).append(p.active?"":" [inactive]").append("\n");show(getString(R.string.dashboard_programmes),empty(b));}
    private void showAcademicContent(){StringBuilder b=new StringBuilder();for(Programme p:programmes.findByMadrassa(madrassaId)){b.append(p.name).append("\n  Courses: ").append(new com.zamcan.madrassa.data.repository.CourseRepository(db).findByProgramme(p.id).size()).append("\n");}show(getString(R.string.dashboard_academic_content),empty(b));}
    private void addClass(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(4),dp(4),dp(4),0);
        EditText name=new EditText(this);name.setHint(getString(R.string.dashboard_class_name));box.addView(name);
        EditText code=new EditText(this);code.setHint(getString(R.string.dashboard_class_code));box.addView(code);
        new AlertDialog.Builder(this).setTitle(getString(R.string.dashboard_add_class)).setView(box).setNegativeButton(getString(R.string.dialog_cancel),null).setPositiveButton(getString(R.string.dialog_save),(d,w)->{
            try{ClassGroup x=new ClassGroup();x.id=IdGenerator.newId();x.madrassaId=madrassaId;x.name=name.getText().toString();x.code=code.getText().toString();x.active=true;x.orderIndex=classes.findByMadrassa(madrassaId).size();new ClassManagementService(classes,new MadrassaAccessContext(madrassaId)).create(x);build();}catch(Exception e){show(getString(R.string.dashboard_add_class),e.getMessage()==null?"Save failed.":e.getMessage());}
        }).show();
    }
    private int countInClass(String id){int n=0;for(Student s:students.findByMadrassa(madrassaId))if(id.equals(s.classId))n++;return n;}
    private String className(String id){if(id==null)return "—";ClassGroup c=classes.findById(id);return c==null?"—":c.name;}
    private String empty(StringBuilder b){return b.length()==0?getString(R.string.dashboard_none):b.toString();}
    private void show(String title,String message){new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton(getString(R.string.dialog_ok),null).show();}
}
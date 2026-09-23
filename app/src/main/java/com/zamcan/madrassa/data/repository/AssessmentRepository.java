package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Assessment;
import com.zamcan.madrassa.data.model.ProgressStatus;
import com.zamcan.madrassa.domain.repository.AssessmentStore;
import java.util.ArrayList;
import java.util.List;

public final class AssessmentRepository implements AssessmentStore {
 private final EduNoorDatabase database; public AssessmentRepository(EduNoorDatabase d){if(d==null)throw new IllegalArgumentException("database is required");database=d;}
 public Assessment findById(String id){if(blank(id))return null;Cursor c=database.getReadableDatabase().query("assessments",null,"id = ?",new String[]{id.trim()},null,null,null,"1");try{return c.moveToFirst()?map(c):null;}finally{c.close();}}
 public List<Assessment> findByStudent(String id){return find("student_id = ?",id);} public List<Assessment> findByAssignment(String id){return find("assignment_id = ?",id);} public List<Assessment> findByMadrassa(String id){return find("madrassa_id = ?",id);}
 private List<Assessment> find(String w,String id){List<Assessment> o=new ArrayList<>();if(blank(id))return o;Cursor c=database.getReadableDatabase().query("assessments",null,w,new String[]{id.trim()},null,null,"recorded_at DESC");try{while(c.moveToNext())o.add(map(c));}finally{c.close();}return o;}
 public boolean save(Assessment x){validate(x);return database.getWritableDatabase().insert("assessments",null,values(x))!=-1;} public boolean update(Assessment x){validate(x);return database.getWritableDatabase().update("assessments",values(x),"id = ?",new String[]{x.id.trim()})==1;}
 private Assessment map(Cursor c){Assessment x=new Assessment();x.id=c.getString(c.getColumnIndexOrThrow("id"));x.madrassaId=text(c,"madrassa_id");x.studentId=c.getString(c.getColumnIndexOrThrow("student_id"));x.assignmentId=c.getString(c.getColumnIndexOrThrow("assignment_id"));x.programmeId=text(c,"programme_id");x.score=c.getDouble(c.getColumnIndexOrThrow("score"));x.feedback=text(c,"feedback");x.status=parse(c.getString(c.getColumnIndexOrThrow("status")));x.recordedAt=c.getLong(c.getColumnIndexOrThrow("recorded_at"));x.recordedBy=text(c,"recorded_by");return x;}
 private ContentValues values(Assessment x){ContentValues v=new ContentValues();v.put("id",x.id.trim());if(x.madrassaId==null)v.putNull("madrassa_id");else v.put("madrassa_id",x.madrassaId.trim());v.put("student_id",x.studentId.trim());v.put("assignment_id",x.assignmentId.trim());if(x.programmeId==null)v.putNull("programme_id");else v.put("programme_id",x.programmeId.trim());v.put("score",x.score);if(x.feedback==null)v.putNull("feedback");else v.put("feedback",x.feedback);v.put("status",x.status.name());v.put("recorded_at",x.recordedAt);if(x.recordedBy==null)v.putNull("recorded_by");else v.put("recorded_by",x.recordedBy.trim());return v;}
 private static ProgressStatus parse(String s){try{return ProgressStatus.valueOf(s);}catch(Exception e){return ProgressStatus.NOT_STARTED;}} private static String text(Cursor c,String n){int i=c.getColumnIndex(n);return i<0||c.isNull(i)?null:c.getString(i);} private static boolean blank(String v){return v==null||v.trim().isEmpty();} private static void validate(Assessment x){if(x==null||blank(x.id)||blank(x.studentId)||blank(x.assignmentId)||x.status==null||x.score<0)throw new IllegalArgumentException("assessment data is incomplete");}
}
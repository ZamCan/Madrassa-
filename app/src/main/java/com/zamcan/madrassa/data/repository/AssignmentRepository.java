package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Assignment;
import com.zamcan.madrassa.domain.repository.AssignmentStore;
import java.util.ArrayList;
import java.util.List;

public final class AssignmentRepository implements AssignmentStore {
 private final EduNoorDatabase database; public AssignmentRepository(EduNoorDatabase d){if(d==null)throw new IllegalArgumentException("database is required");database=d;}
 public Assignment findById(String id){if(blank(id))return null;Cursor c=database.getReadableDatabase().query("assignments",null,"id = ?",new String[]{id.trim()},null,null,null,"1");try{return c.moveToFirst()?map(c):null;}finally{c.close();}}
 public List<Assignment> findByProgramme(String id){return find("programme_id = ?",id);} public List<Assignment> findByLesson(String id){return find("lesson_id = ?",id);} public List<Assignment> findByMadrassa(String id){return find("madrassa_id = ?",id);}
 private List<Assignment> find(String w,String id){List<Assignment> o=new ArrayList<>();if(blank(id))return o;Cursor c=database.getReadableDatabase().query("assignments",null,w,new String[]{id.trim()},null,null,"due_date ASC, title ASC");try{while(c.moveToNext())o.add(map(c));}finally{c.close();}return o;}
 public boolean save(Assignment x){validate(x);return database.getWritableDatabase().insert("assignments",null,values(x))!=-1;} public boolean update(Assignment x){validate(x);return database.getWritableDatabase().update("assignments",values(x),"id = ?",new String[]{x.id.trim()})==1;}
 private Assignment map(Cursor c){Assignment x=new Assignment();x.id=c.getString(c.getColumnIndexOrThrow("id"));x.madrassaId=text(c,"madrassa_id");x.programmeId=c.getString(c.getColumnIndexOrThrow("programme_id"));x.courseId=text(c,"course_id");x.unitId=text(c,"unit_id");x.lessonId=text(c,"lesson_id");x.title=c.getString(c.getColumnIndexOrThrow("title"));x.description=text(c,"description");x.maxPoints=c.getDouble(c.getColumnIndexOrThrow("max_points"));x.dueDate=c.getLong(c.getColumnIndexOrThrow("due_date"));x.active=c.getInt(c.getColumnIndexOrThrow("active"))!=0;return x;}
 private ContentValues values(Assignment x){ContentValues v=new ContentValues();v.put("id",x.id.trim());if(x.madrassaId==null)v.putNull("madrassa_id");else v.put("madrassa_id",x.madrassaId.trim());v.put("programme_id",x.programmeId.trim());put(v,"course_id",x.courseId);put(v,"unit_id",x.unitId);put(v,"lesson_id",x.lessonId);v.put("title",x.title.trim());put(v,"description",x.description);v.put("max_points",x.maxPoints);v.put("due_date",x.dueDate);v.put("active",x.active?1:0);return v;} private static void put(ContentValues v,String k,String x){if(x==null)v.putNull(k);else v.put(k,x.trim());} private static String text(Cursor c,String n){int i=c.getColumnIndex(n);return i<0||c.isNull(i)?null:c.getString(i);} private static boolean blank(String v){return v==null||v.trim().isEmpty();} private static void validate(Assignment x){if(x==null||blank(x.id)||blank(x.programmeId)||blank(x.title)||x.maxPoints<0)throw new IllegalArgumentException("assignment data is incomplete");}
}
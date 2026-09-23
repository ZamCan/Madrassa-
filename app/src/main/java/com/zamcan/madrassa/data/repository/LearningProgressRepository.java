package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.ProgressStatus;
import com.zamcan.madrassa.domain.repository.LearningProgressStore;
import java.util.ArrayList;
import java.util.List;

public final class LearningProgressRepository implements LearningProgressStore {
 private final EduNoorDatabase database; public LearningProgressRepository(EduNoorDatabase d){if(d==null)throw new IllegalArgumentException("database is required");database=d;}
 public LearningProgress findById(String id){if(blank(id))return null;Cursor c=database.getReadableDatabase().query("learning_progress",null,"id = ?",new String[]{id.trim()},null,null,null,"1");try{return c.moveToFirst()?map(c):null;}finally{c.close();}}
 public List<LearningProgress> findByLearner(String id){return find("learner_id = ?",id);} public List<LearningProgress> findByLesson(String id){return find("lesson_id = ?",id);}
 public LearningProgress findByLearnerAndLesson(String learnerId,String lessonId){
  if(blank(learnerId)||blank(lessonId))return null;
  Cursor c=database.getReadableDatabase().query("learning_progress",null,
    "learner_id = ? AND lesson_id = ?",new String[]{learnerId.trim(),lessonId.trim()},
    null,null,"updated_at DESC","1");
  try{return c.moveToFirst()?map(c):null;}finally{c.close();}
 }
 private List<LearningProgress> find(String w,String id){List<LearningProgress> o=new ArrayList<>();if(blank(id))return o;Cursor c=database.getReadableDatabase().query("learning_progress",null,w,new String[]{id.trim()},null,null,"updated_at DESC");try{while(c.moveToNext())o.add(map(c));}finally{c.close();}return o;}
 public boolean save(LearningProgress x){validate(x);return database.getWritableDatabase().insert("learning_progress",null,values(x))!=-1;} public boolean update(LearningProgress x){validate(x);return database.getWritableDatabase().update("learning_progress",values(x),"id = ?",new String[]{x.id.trim()})==1;}
 private LearningProgress map(Cursor c){LearningProgress x=new LearningProgress();x.id=c.getString(c.getColumnIndexOrThrow("id"));x.learnerId=c.getString(c.getColumnIndexOrThrow("learner_id"));x.lessonId=c.getString(c.getColumnIndexOrThrow("lesson_id"));x.madrassaId=text(c,"madrassa_id");x.status=parse(c.getString(c.getColumnIndexOrThrow("status")));x.updatedAt=c.getLong(c.getColumnIndexOrThrow("updated_at"));return x;}
 private ContentValues values(LearningProgress x){ContentValues v=new ContentValues();v.put("id",x.id.trim());v.put("learner_id",x.learnerId.trim());v.put("lesson_id",x.lessonId.trim());if(x.madrassaId==null)v.putNull("madrassa_id");else v.put("madrassa_id",x.madrassaId.trim());v.put("status",x.status.name());
  // Version-6 upgrades retain state TEXT NOT NULL. Fresh version-7 databases
  // have only status. Keep the legacy storage column in sync when present;
  // ProgressStatus remains the sole domain representation.
  if (hasLegacyStateColumn()) v.put("state", x.status.name());
  v.put("updated_at",x.updatedAt);return v;}

 private boolean hasLegacyStateColumn() {
  Cursor cursor = database.getReadableDatabase().rawQuery(
    "PRAGMA table_info(learning_progress)", null);
  try {
   int nameIndex = cursor.getColumnIndexOrThrow("name");
   while (cursor.moveToNext()) {
    if ("state".equalsIgnoreCase(cursor.getString(nameIndex))) return true;
   }
   return false;
  } finally {
   cursor.close();
  }
 }
 private static ProgressStatus parse(String s){
  if ("STARTED".equalsIgnoreCase(s)) return ProgressStatus.IN_PROGRESS;
  try{return ProgressStatus.valueOf(s);}catch(Exception e){return ProgressStatus.NOT_STARTED;}
 } private static String text(Cursor c,String n){int i=c.getColumnIndex(n);return i<0||c.isNull(i)?null:c.getString(i);} private static boolean blank(String v){return v==null||v.trim().isEmpty();} private static void validate(LearningProgress x){if(x==null||blank(x.id)||blank(x.learnerId)||blank(x.lessonId)||x.status==null)throw new IllegalArgumentException("learning progress data is incomplete");}
}
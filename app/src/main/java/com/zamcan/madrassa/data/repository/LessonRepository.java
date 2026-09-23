package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Lesson;
import com.zamcan.madrassa.domain.repository.LessonStore;
import java.util.ArrayList;
import java.util.List;

public final class LessonRepository implements LessonStore {
    private final EduNoorDatabase database;
    public LessonRepository(EduNoorDatabase database){if(database==null)throw new IllegalArgumentException("database is required");this.database=database;}
    public Lesson findById(String id){if(blank(id))return null;Cursor c=database.getReadableDatabase().query("lessons",null,"id = ?",new String[]{id.trim()},null,null,null,"1");try{return c.moveToFirst()?map(c):null;}finally{c.close();}}
    public List<Lesson> findByUnit(String id){List<Lesson> out=new ArrayList<>();if(blank(id))return out;Cursor c=database.getReadableDatabase().query("lessons",null,"unit_id = ?",new String[]{id.trim()},null,null,"order_index ASC, name ASC");try{while(c.moveToNext())out.add(map(c));}finally{c.close();}return out;}
    public boolean save(Lesson x){validate(x);return database.getWritableDatabase().insert("lessons",null,values(x))!=-1;}
    public boolean update(Lesson x){validate(x);return database.getWritableDatabase().update("lessons",values(x),"id = ?",new String[]{x.id.trim()})==1;}
    private Lesson map(Cursor c){Lesson x=new Lesson();x.id=c.getString(c.getColumnIndexOrThrow("id"));x.madrassaId=text(c,"madrassa_id");x.unitId=c.getString(c.getColumnIndexOrThrow("unit_id"));x.name=c.getString(c.getColumnIndexOrThrow("name"));x.contentType=c.getString(c.getColumnIndexOrThrow("content_type"));x.orderIndex=c.getInt(c.getColumnIndexOrThrow("order_index"));x.active=c.getInt(c.getColumnIndexOrThrow("active"))!=0;return x;}
    private ContentValues values(Lesson x){ContentValues v=new ContentValues();v.put("id",x.id.trim());if(x.madrassaId==null)v.putNull("madrassa_id");else v.put("madrassa_id",x.madrassaId.trim());v.put("unit_id",x.unitId.trim());v.put("name",x.name.trim());v.put("content_type",x.contentType.trim());v.put("order_index",x.orderIndex);v.put("active",x.active?1:0);return v;}
    private static String text(Cursor c,String n){int i=c.getColumnIndex(n);return i<0||c.isNull(i)?null:c.getString(i);}private static boolean blank(String v){return v==null||v.trim().isEmpty();}private static void validate(Lesson x){if(x==null||blank(x.id)||blank(x.unitId)||blank(x.name)||blank(x.contentType))throw new IllegalArgumentException("lesson data is incomplete");}
}
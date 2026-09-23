package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Course;
import com.zamcan.madrassa.domain.repository.CourseStore;
import java.util.ArrayList;
import java.util.List;

public final class CourseRepository implements CourseStore {
    private final EduNoorDatabase database;
    public CourseRepository(EduNoorDatabase database) { if (database == null) throw new IllegalArgumentException("database is required"); this.database = database; }
    public Course findById(String id) { if (blank(id)) return null; Cursor c=database.getReadableDatabase().query("courses",null,"id = ?",new String[]{id.trim()},null,null,null,"1"); try { return c.moveToFirst()?map(c):null; } finally { c.close(); } }
    public List<Course> findByMadrassa(String id) { return find("madrassa_id = ?",id); }
    public List<Course> findByProgramme(String id) { return find("programme_id = ?",id); }
    private List<Course> find(String where,String id) { List<Course> out=new ArrayList<>(); if(blank(id)) return out; Cursor c=database.getReadableDatabase().query("courses",null,where,new String[]{id.trim()},null,null,"level_number ASC, name ASC"); try { while(c.moveToNext()) out.add(map(c)); } finally { c.close(); } return out; }
    public boolean save(Course x) { validate(x); return database.getWritableDatabase().insert("courses",null,values(x)) != -1; }
    public boolean update(Course x) { validate(x); return database.getWritableDatabase().update("courses",values(x),"id = ?",new String[]{x.id.trim()}) == 1; }
    private Course map(Cursor c) { Course x=new Course(); x.id=c.getString(c.getColumnIndexOrThrow("id")); x.madrassaId=text(c,"madrassa_id"); x.programmeId=c.getString(c.getColumnIndexOrThrow("programme_id")); x.name=c.getString(c.getColumnIndexOrThrow("name")); x.levelNumber=c.getInt(c.getColumnIndexOrThrow("level_number")); x.active=c.getInt(c.getColumnIndexOrThrow("active")) != 0; return x; }
    private ContentValues values(Course x) { ContentValues v=new ContentValues(); v.put("id",x.id.trim()); if(x.madrassaId==null)v.putNull("madrassa_id");else v.put("madrassa_id",x.madrassaId.trim()); v.put("programme_id",x.programmeId.trim()); v.put("name",x.name.trim()); v.put("level_number",x.levelNumber); v.put("active",x.active?1:0); return v; }
    private static String text(Cursor c,String n){int i=c.getColumnIndex(n);return i<0||c.isNull(i)?null:c.getString(i);}
    private static boolean blank(String v){return v==null||v.trim().isEmpty();}
    private static void validate(Course x){if(x==null||blank(x.id)||blank(x.programmeId)||blank(x.name))throw new IllegalArgumentException("course data is incomplete");}
}
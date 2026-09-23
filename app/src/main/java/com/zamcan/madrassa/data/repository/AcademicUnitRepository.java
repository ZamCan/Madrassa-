package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.AcademicUnit;
import com.zamcan.madrassa.domain.repository.AcademicUnitStore;
import java.util.ArrayList;
import java.util.List;

public final class AcademicUnitRepository implements AcademicUnitStore {
    private final EduNoorDatabase database;
    public AcademicUnitRepository(EduNoorDatabase database){if(database==null)throw new IllegalArgumentException("database is required");this.database=database;}
    public AcademicUnit findById(String id){if(blank(id))return null;Cursor c=database.getReadableDatabase().query("academic_units",null,"id = ?",new String[]{id.trim()},null,null,null,"1");try{return c.moveToFirst()?map(c):null;}finally{c.close();}}
    public List<AcademicUnit> findByCourse(String id){List<AcademicUnit> out=new ArrayList<>();if(blank(id))return out;Cursor c=database.getReadableDatabase().query("academic_units",null,"course_id = ?",new String[]{id.trim()},null,null,"order_index ASC, name ASC");try{while(c.moveToNext())out.add(map(c));}finally{c.close();}return out;}
    public boolean save(AcademicUnit x){validate(x);return database.getWritableDatabase().insert("academic_units",null,values(x))!=-1;}
    public boolean update(AcademicUnit x){validate(x);return database.getWritableDatabase().update("academic_units",values(x),"id = ?",new String[]{x.id.trim()})==1;}
    private AcademicUnit map(Cursor c){AcademicUnit x=new AcademicUnit();x.id=c.getString(c.getColumnIndexOrThrow("id"));x.madrassaId=text(c,"madrassa_id");x.courseId=c.getString(c.getColumnIndexOrThrow("course_id"));x.name=c.getString(c.getColumnIndexOrThrow("name"));x.orderIndex=c.getInt(c.getColumnIndexOrThrow("order_index"));return x;}
    private ContentValues values(AcademicUnit x){ContentValues v=new ContentValues();v.put("id",x.id.trim());if(x.madrassaId==null)v.putNull("madrassa_id");else v.put("madrassa_id",x.madrassaId.trim());v.put("course_id",x.courseId.trim());v.put("name",x.name.trim());v.put("order_index",x.orderIndex);return v;}
    private static String text(Cursor c,String n){int i=c.getColumnIndex(n);return i<0||c.isNull(i)?null:c.getString(i);} private static boolean blank(String v){return v==null||v.trim().isEmpty();} private static void validate(AcademicUnit x){if(x==null||blank(x.id)||blank(x.courseId)||blank(x.name))throw new IllegalArgumentException("academic unit data is incomplete");}
}
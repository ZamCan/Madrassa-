package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.LearningMaterial;
import com.zamcan.madrassa.domain.repository.LearningMaterialStore;
import java.util.ArrayList;
import java.util.List;

public final class LearningMaterialRepository implements LearningMaterialStore {
 private final EduNoorDatabase database; public LearningMaterialRepository(EduNoorDatabase d){if(d==null)throw new IllegalArgumentException("database is required");database=d;}
 public LearningMaterial findById(String id){if(blank(id))return null;Cursor c=database.getReadableDatabase().query("learning_materials",null,"id = ?",new String[]{id.trim()},null,null,null,"1");try{return c.moveToFirst()?map(c):null;}finally{c.close();}}
 public List<LearningMaterial> findByProgramme(String id){return find("programme_id = ?",id);} public List<LearningMaterial> findByMadrassa(String id){return find("madrassa_id = ?",id);}
 private List<LearningMaterial> find(String w,String id){List<LearningMaterial> o=new ArrayList<>();if(blank(id))return o;Cursor c=database.getReadableDatabase().query("learning_materials",null,w,new String[]{id.trim()},null,null,"title ASC");try{while(c.moveToNext())o.add(map(c));}finally{c.close();}return o;}
 public boolean save(LearningMaterial x){validate(x);return database.getWritableDatabase().insert("learning_materials",null,values(x))!=-1;} public boolean update(LearningMaterial x){validate(x);return database.getWritableDatabase().update("learning_materials",values(x),"id = ?",new String[]{x.id.trim()})==1;}
 private LearningMaterial map(Cursor c){LearningMaterial x=new LearningMaterial();x.id=c.getString(c.getColumnIndexOrThrow("id"));x.madrassaId=text(c,"madrassa_id");x.programmeId=c.getString(c.getColumnIndexOrThrow("programme_id"));x.title=c.getString(c.getColumnIndexOrThrow("title"));x.author=text(c,"author");x.type=c.getString(c.getColumnIndexOrThrow("type"));x.content=c.getString(c.getColumnIndexOrThrow("content"));x.active=c.getInt(c.getColumnIndexOrThrow("active"))!=0;return x;}
 private ContentValues values(LearningMaterial x){ContentValues v=new ContentValues();v.put("id",x.id.trim());if(x.madrassaId==null)v.putNull("madrassa_id");else v.put("madrassa_id",x.madrassaId.trim());v.put("programme_id",x.programmeId.trim());v.put("title",x.title.trim());if(x.author==null)v.putNull("author");else v.put("author",x.author.trim());v.put("type",x.type.trim());v.put("content",x.content);v.put("active",x.active?1:0);return v;} private static String text(Cursor c,String n){int i=c.getColumnIndex(n);return i<0||c.isNull(i)?null:c.getString(i);} private static boolean blank(String v){return v==null||v.trim().isEmpty();} private static void validate(LearningMaterial x){if(x==null||blank(x.id)||blank(x.programmeId)||blank(x.title)||blank(x.type)||x.content==null)throw new IllegalArgumentException("learning material data is incomplete");}
}
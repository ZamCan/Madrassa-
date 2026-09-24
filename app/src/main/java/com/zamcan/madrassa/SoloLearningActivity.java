package com.zamcan.madrassa;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.domain.calendar.IslamicCalendarDisplayPolicy;
import com.zamcan.madrassa.domain.geography.LocationCatalog;
import com.zamcan.madrassa.domain.quran.QuranKnowledgeCatalog;
import com.zamcan.madrassa.domain.salah.QiblaCalculator;
import com.zamcan.madrassa.domain.salah.SalahReminderEngine;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.solo.SoloLearningCatalog;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.EduNoorCard;
import com.zamcan.madrassa.ui.components.EduNoorProgressView;
import java.time.LocalDate;
import java.util.List;

public class SoloLearningActivity extends Activity {
 private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,float size,int color,boolean bold){TextView v=new TextView(this);v.setText(s==null?"":s);v.setTextSize(size);v.setTextColor(color);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));v.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);v.setIncludeFontPadding(true);return v;}
 @Override protected void attachBaseContext(android.content.Context base){super.attachBaseContext(LanguageManager.wrap(base));}
 @Override protected void onCreate(Bundle state){
  super.onCreate(state);
  int bg=getColor(R.color.edunoor_background), surface=getColor(R.color.edunoor_surface), walnut=getColor(R.color.edunoor_walnut), muted=getColor(R.color.edunoor_muted), gold=getColor(R.color.edunoor_gold), clay=getColor(R.color.edunoor_clay);
  getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(bg);
  LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),dp(8),dp(12),dp(8));
  TextView back=text("‹",30,clay,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(42),dp(48)));
  LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);
  words.addView(text(getString(R.string.solo_learning_title),17,walnut,true));words.addView(text(getString(R.string.solo_learning_subtitle),10.5f,muted,false));
  head.addView(words,new LinearLayout.LayoutParams(0,-2,1));
  TextView date=text(IslamicCalendarDisplayPolicy.dualDate(LocalDate.now()),10,muted,false);date.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);head.addView(date,new LinearLayout.LayoutParams(dp(112),-2));
  root.addView(head,new LinearLayout.LayoutParams(-1,-2));
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(15),dp(8),dp(15),dp(22));
  LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(18),dp(17),dp(18),dp(17));hero.setBackgroundResource(R.drawable.edunoor_canvas);
  hero.addView(text(getString(R.string.solo_learning_welcome),20,walnut,true));hero.addView(text(getString(R.string.solo_learning_offline),11,muted,false),new LinearLayout.LayoutParams(-1,-2));
  LinearLayout tools=new LinearLayout(this);tools.setGravity(Gravity.CENTER_VERTICAL);tools.setPadding(0,dp(10),0,0);
  TextView q=EduNoorButton.secondary(this,getString(R.string.solo_qibla));q.setOnClickListener(v->showQibla());tools.addView(q,new LinearLayout.LayoutParams(0,-2,1));
  TextView a=EduNoorButton.secondary(this,getString(R.string.solo_salah));a.setOnClickListener(v->showSalah());LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,-2,1);ap.leftMargin=dp(7);tools.addView(a,ap);
  hero.addView(tools);c.addView(hero,new LinearLayout.LayoutParams(-1,-2));
  LinearLayout title=new LinearLayout(this);title.setOrientation(LinearLayout.VERTICAL);title.setPadding(0,dp(18),0,dp(7));title.addView(text(getString(R.string.solo_learning_areas),16,walnut,true));title.addView(text(getString(R.string.solo_learning_areas_subtitle),10.5f,muted,false));c.addView(title);
  for(SoloLearningCatalog.Area area:SoloLearningCatalog.areas()){LinearLayout card=EduNoorCard.create(this,area.id,area.title,area.subtitle,"›");card.setOnClickListener(v->showArea(area));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(9);c.addView(card,lp);}
  c.addView(text(getString(R.string.solo_quran_structure),14,walnut,true),new LinearLayout.LayoutParams(-1,-2));
  c.addView(text(getString(R.string.solo_quran_structure_detail,QuranKnowledgeCatalog.surahCount(),QuranKnowledgeCatalog.juzCount()),10.5f,muted,false),new LinearLayout.LayoutParams(-1,-2));
  LinearLayout prog=EduNoorProgressView.create(this,getString(R.string.solo_progress_learning),0,SoloLearningCatalog.areas().size());LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.topMargin=dp(14);c.addView(prog,pp);
  c.addView(text(getString(R.string.solo_design_note),10,muted,false),new LinearLayout.LayoutParams(-1,-2));
  scroll.addView(c);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
 }
 private void showArea(SoloLearningCatalog.Area area){new AlertDialog.Builder(this).setTitle(area.title).setMessage(area.subtitle+"\n\n"+getString(R.string.solo_modes)).setPositiveButton(getString(R.string.dialog_ok),null).show();}
 private void showQibla(){double b=QiblaCalculator.bearingDegrees(LocationCatalog.darEsSalaam().latitude,LocationCatalog.darEsSalaam().longitude);new AlertDialog.Builder(this).setTitle(getString(R.string.solo_qibla)).setMessage(getString(R.string.solo_qibla_result,b,QiblaCalculator.cardinal(b))).setPositiveButton(getString(R.string.dialog_ok),null).show();}
 private void showSalah(){List<PrayerTime> times=SalahReminderEngine.today(LocalDate.now(),LocationCatalog.darEsSalaam());StringBuilder s=new StringBuilder();for(PrayerTime p:times)if(p.prayer!=PrayerTime.Prayer.SUNRISE)s.append(p.prayer.name()).append("  ").append(p.time).append("\n");new AlertDialog.Builder(this).setTitle(getString(R.string.solo_salah)).setMessage(s.toString()+"\n"+getString(R.string.solo_adhan_off_default)).setPositiveButton(getString(R.string.dialog_ok),null).show();}
}
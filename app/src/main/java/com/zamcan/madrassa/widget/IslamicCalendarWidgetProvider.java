package com.zamcan.madrassa.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import com.zamcan.madrassa.R;
import com.zamcan.madrassa.domain.calendar.IslamicCalendarEngine;
import java.time.LocalDate;

/** Compact home/lock-screen-capable Android widget surface where the device supports widgets. */
public final class IslamicCalendarWidgetProvider extends AppWidgetProvider {
 @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
  IslamicCalendarEngine.HijriDate h=IslamicCalendarEngine.fromGregorian(LocalDate.now());
  for(int id:ids){
   RemoteViews v=new RemoteViews(context.getPackageName(),R.layout.widget_islamic_calendar);
   v.setTextViewText(R.id.widget_gregorian,LocalDate.now().toString());
   v.setTextViewText(R.id.widget_hijri,h.toString());
   manager.updateAppWidget(id,v);
  }
 }
}
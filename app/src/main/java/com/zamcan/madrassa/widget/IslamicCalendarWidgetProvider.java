package com.zamcan.madrassa.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.domain.calendar.IslamicCalendarDisplayPolicy;
import com.zamcan.madrassa.domain.calendar.IslamicCalendarEngine;

import java.time.LocalDate;
import java.util.Locale;

/** Compact offline dual-calendar widget. */
public final class IslamicCalendarWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager manager,
            int[] appWidgetIds
    ) {
        if (context == null || manager == null || appWidgetIds == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        String date = IslamicCalendarDisplayPolicy.dualDate(
                today,
                Locale.getDefault()
        );

        for (int id : appWidgetIds) {
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_islamic_calendar
            );
            views.setTextViewText(R.id.widget_dual_date, date);
            views.setTextViewText(
                    R.id.widget_hijri,
                    IslamicCalendarEngine.fromGregorian(today).toString()
            );
            manager.updateAppWidget(id, views);
        }
    }
}

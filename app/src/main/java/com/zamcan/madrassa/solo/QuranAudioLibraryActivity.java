package com.zamcan.madrassa.solo;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import java.io.IOException;
import java.util.Arrays;

/**
 * Direct MP3 playback of the existing bundled educational clips.
 * Audio provenance is not independently certified as Quran recitation.
 */
public final class QuranAudioLibraryActivity extends Activity {
    private final SoloAudioPlayer player = new SoloAudioPlayer();
    private TextView active;
    private int dp(int n) { return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
    @Override protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LanguageManager.wrap(base));
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.edunoor_background));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(24),dp(20),dp(24));
        TextView title = new TextView(this);
        title.setText("Qur'an • MP3");
        title.setTextSize(25); title.setTypeface(null,Typeface.BOLD);
        title.setTextColor(getColor(R.color.edunoor_walnut));
        root.addView(title);
        TextView note = new TextView(this);
        note.setText("Offline educational audio samples. Verified full recitation and tafsir libraries are not installed yet.");
        note.setTextSize(12); note.setPadding(0,dp(8),0,dp(18));
        root.addView(note);
        try {
            String[] files = getAssets().list("solo/audio");
            if (files != null) {
                Arrays.sort(files);
                for (String file : files) {
                    if (!file.endsWith(".mp3") || !file.matches("[0-9]{6}\\.mp3")) continue;
                    int surah = Integer.parseInt(file.substring(0,3));
                    int ayah = Integer.parseInt(file.substring(3,6));
                    TextView row = new TextView(this);
                    row.setText(String.format(java.util.Locale.ROOT,"▶  %s  •  %d:%d",
                            com.zamcan.madrassa.domain.quran.QuranKnowledgeCatalog.surahName(surah),
                            surah,ayah));
                    row.setTextSize(16); row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setTextColor(getColor(R.color.edunoor_walnut));
                    row.setPadding(dp(16),dp(10),dp(16),dp(10));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,dp(58));
                    params.bottomMargin=dp(6);
                    root.addView(row,params);
                    row.setOnClickListener(v -> {
                        if (player.isPlaying(file)) {
                            player.stop(); row.setText(row.getText().toString().replace("■","▶"));
                        } else if (player.play(this,file)) {
                            if (active != null) active.setSelected(false);
                            active=row; row.setSelected(true);
                        } else {
                            android.widget.Toast.makeText(this,"Audio unavailable",android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        } catch (IOException e) {
            note.setText("Audio library cannot be opened.");
        }
        scroll.addView(root);
        setContentView(scroll);
    }
    @Override protected void onStop() { player.release(); super.onStop(); }
}

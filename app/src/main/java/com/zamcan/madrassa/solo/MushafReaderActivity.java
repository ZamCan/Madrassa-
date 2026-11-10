package com.zamcan.madrassa.solo;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.domain.quran.QuranAssetIntegrity;
import com.zamcan.madrassa.domain.quran.QuranCorpusValidator;
import com.zamcan.madrassa.domain.quran.QuranKnowledgeCatalog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parchment Mushaf reader. Only displays corpus text after checksum and
 * structural validation. Never substitutes lesson snippets for scripture.
 * Expected assets: quran/corpus.json and quran/manifest.json.
 */
public final class MushafReaderActivity extends Activity {
    private int surah = 1;
    private List<QuranCorpusValidator.Verse> corpus;
    private LinearLayout body;
    private TextView title;
    private int dp(int n) { return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
    @Override protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LanguageManager.wrap(base));
    }
    private TextView text(String s, int size, int color) {
        TextView v = new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color);
        v.setGravity(Gravity.CENTER); return v;
    }
    private GradientDrawable paper() {
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xfffcf5e5,0xfff3e8cf,0xfffcf7eb});
        bg.setCornerRadius(dp(9)); bg.setStroke(dp(2),0xffc9ac72);
        return bg;
    }
    private String readAsset(String path) throws Exception {
        try (InputStream in=getAssets().open(path);
             java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()) {
            byte[] b=new byte[8192]; int n;
            while ((n=in.read(b))!=-1) out.write(b,0,n);
            return out.toString("UTF-8");
        }
    }
    private List<QuranCorpusValidator.Verse> verifiedCorpus() throws Exception {
        JSONObject manifest = new JSONObject(readAsset("quran/manifest.json"));
        if (!"quran/corpus.json".equals(manifest.getString("asset"))
                || manifest.optInt("verseCount") != 6236
                || manifest.optInt("surahCount") != 114
                || manifest.optString("source").trim().isEmpty()
                || manifest.optString("license").trim().isEmpty()) {
            throw new SecurityException("Missing provenance");
        }
        try (InputStream in=getAssets().open("quran/corpus.json")) {
            QuranAssetIntegrity.verify(in,manifest.getString("sha256"));
        }
        JSONArray arr = new JSONArray(readAsset("quran/corpus.json"));
        List<QuranCorpusValidator.Verse> verses = new ArrayList<>(arr.length());
        for (int i=0;i<arr.length();i++) {
            JSONObject v=arr.getJSONObject(i);
            verses.add(new QuranCorpusValidator.Verse(v.getInt("surah"),
                    v.getInt("ayah"),v.getString("arabic")));
        }
        QuranCorpusValidator.validate(verses);
        return verses;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xffe8decb);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(18),dp(14),dp(18));
        LinearLayout nav=new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView previous=text("‹",30,0xff70533b);
        TextView next=text("›",30,0xff70533b);
        title=text("",17,0xff59402c); title.setTypeface(null,Typeface.BOLD);
        nav.addView(previous,new LinearLayout.LayoutParams(dp(48),dp(48)));
        nav.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        nav.addView(next,new LinearLayout.LayoutParams(dp(48),dp(48)));
        root.addView(nav);
        body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18),dp(28),dp(18),dp(32));
        body.setBackground(paper());
        root.addView(body,new LinearLayout.LayoutParams(-1,-2));
        scroll.addView(root);
        setContentView(scroll);
        try { corpus=verifiedCorpus(); }
        catch (Exception unavailable) { corpus=null; }
        previous.setOnClickListener(v->{ if(surah>1){surah--;render();scroll.smoothScrollTo(0,0);} });
        next.setOnClickListener(v->{ if(surah<114){surah++;render();scroll.smoothScrollTo(0,0);} });
        render();
    }
    private void render() {
        body.removeAllViews();
        title.setText(QuranKnowledgeCatalog.surahName(surah)+"  ·  "+surah+"/114");
        if(corpus==null) {
            TextView unavailable=text("Verified Qur'an pages are not installed. Add a licensed, checksum-verified 6,236-ayah corpus before reading.",15,0xff70533b);
            unavailable.setPadding(dp(8),dp(65),dp(8),dp(65));
            body.addView(unavailable);
            return;
        }
        TextView heading=text("۞  "+QuranKnowledgeCatalog.surahName(surah)+"  ۞",21,0xff6c4c2b);
        heading.setPadding(0,0,0,dp(22));
        body.addView(heading);
        StringBuilder passage=new StringBuilder();
        for(QuranCorpusValidator.Verse verse:corpus) {
            if(verse.surah==surah) {
                passage.append(verse.arabic).append("  ﴿")
                        .append(verse.ayah).append("﴾  ");
            }
        }
        TextView verses=text(passage.toString(),25,0xff342b24);
        verses.setTextDirection(View.TEXT_DIRECTION_RTL);
        verses.setGravity(Gravity.RIGHT);
        verses.setLineSpacing(dp(14),1.25f);
        verses.setTypeface(Typeface.create("serif",Typeface.NORMAL));
        body.addView(verses);
    }
}

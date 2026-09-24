package com.zamcan.madrassa.core.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.deen.DeenPrefs;

/**
 * EduNoor UI sound engine. Four synthesized, deliberately quiet
 * cues (see res/raw): tap, page, delete and the boot chime.
 * Every cue respects the Deen preference toggle; volumes are
 * kept low so sound stays a whisper of feedback, never noise.
 */
public final class EduNoorSounds {

    private static SoundPool pool;
    private static int tapId;
    private static int pageId;
    private static int deleteId;
    private static int bootId;
    private static boolean loaded;

    private EduNoorSounds() {
    }

    private static synchronized void ensure(Context context) {

        if (pool != null) {
            return;
        }

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        pool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attributes)
                .build();

        pool.setOnLoadCompleteListener(
                (soundPool, sampleId, status) ->
                        loaded = status == 0);

        tapId = pool.load(context, R.raw.sound_tap, 1);
        pageId = pool.load(context, R.raw.sound_page, 1);
        deleteId = pool.load(context, R.raw.sound_delete, 1);
        bootId = pool.load(context, R.raw.sound_boot, 1);
    }

    /** Soft tick for primary button presses. */
    public static void tap(Context context) {
        play(context, tapId, 0.30f);
    }

    /** Quiet page-turn for step and screen transitions. */
    public static void page(Context context) {
        play(context, pageId, 0.32f);
    }

    /** Low soft thock for deletions and stopping actions. */
    public static void delete(Context context) {
        play(context, deleteId, 0.38f);
    }

    /** Warm three-note chime for the boot entrance. */
    public static void chime(Context context) {
        play(context, bootId, 0.45f);
    }

    private static void play(Context context, int soundId, float volume) {

        if (context == null
                || soundId == 0
                || !DeenPrefs.isSoundOn(context)) {
            return;
        }

        ensure(context.getApplicationContext());

        if (loaded && pool != null) {
            pool.play(soundId, volume, volume, 1, 0, 1f);
        }
    }
}

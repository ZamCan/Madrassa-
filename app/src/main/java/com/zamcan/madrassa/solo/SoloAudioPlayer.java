package com.zamcan.madrassa.solo;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

/**
 * Minimal offline player for bundled lesson audio
 * (assets/solo/audio). One instance drives one playing clip at a
 * time; release() must be called from the Activity lifecycle.
 */
public final class SoloAudioPlayer {

    private MediaPlayer player;
    private String playingAsset;

    public boolean isPlaying(String asset) {
        return playingAsset != null
                && playingAsset.equals(asset)
                && player != null
                && player.isPlaying();
    }

    /** Starts (or restarts) an asset clip. Returns true on success. */
    public boolean play(Context context, String asset) {

        stop();

        try {
            player = new MediaPlayer();
            player.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
            );
            player.setDataSource(
                    context.getAssets().openFd("solo/audio/" + asset)
            );
            player.prepare();
            player.setOnCompletionListener(mp -> stop());
            player.start();
            playingAsset = asset;
            return true;
        } catch (Exception error) {
            release();
            return false;
        }
    }

    public void stop() {
        if (player != null) {
            try {
                player.stop();
            } catch (Exception ignored) {
                // already stopped
            }
            player.release();
            player = null;
        }
        playingAsset = null;
    }

    public void release() {
        stop();
    }
}

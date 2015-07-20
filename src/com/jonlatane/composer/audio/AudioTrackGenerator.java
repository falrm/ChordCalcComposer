package com.jonlatane.composer.audio;

import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by jonlatane on 7/19/15.
 */
public interface AudioTrackGenerator {
    int NATIVE_OUTPUT_SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC); //per second

    AudioTrack getAudioTrackFor(int note);

    @Override int hashCode();
}

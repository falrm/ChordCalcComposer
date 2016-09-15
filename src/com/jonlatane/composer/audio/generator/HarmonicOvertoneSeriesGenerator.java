package com.jonlatane.composer.audio.generator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.jonlatane.composer.audio.AudioTrackCache;
import com.jonlatane.composer.audio.AudioTrackGenerator;
import com.jonlatane.composer.music.harmony.Chord;

import java.util.Arrays;

/**
 * Created by jonlatane on 7/19/15.
 */
public class HarmonicOvertoneSeriesGenerator implements AudioTrackGenerator {
    /** The frequencies of the notes C4-B4 */
    private static final double[] FREQUENCIES = {261.625625, 277.1825, 293.665, 311.1275, 329.6275, 349.22875, 369.995, 391.995, 415.305, 440, 466.16375, 493.88375};
    private final static Double[] DEFAULT_OVERTONES = {70., 30., 30., 10., 10., 20., 20., 1.};

    /** All HarmonicOvertoneSeriesGenerators share the same {@link #audioSessionId} id and {@link #equalizer} */
    public Integer audioSessionId = null;
    /** All HarmonicOvertoneSeriesGenerators share the same {@link #audioSessionId} id and {@link #equalizer} */
    public Equalizer equalizer = null;
    Double[] overtones;

    private static final String TAG = "HOSGenerator";

    public HarmonicOvertoneSeriesGenerator() {
        this(DEFAULT_OVERTONES);
    }

    public HarmonicOvertoneSeriesGenerator(Double[] overtones) {
        this.overtones = overtones;
        setupEqualizer();
    }

    public Double[] getOvertones() {
        return overtones;
    }

    public void setOvertones(Double... overtones) {
        this.overtones = overtones;
    }

    private void setupEqualizer() {
        // Get an audio session ID from a track without one
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                NATIVE_OUTPUT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, 16,
                AudioTrack.MODE_STATIC);
        audioSessionId = track.getAudioSessionId();
        track.flush();
        track.release();

        equalizer = new Equalizer(1, audioSessionId);
        equalizer.setEnabled(true);
        short bands = equalizer.getNumberOfBands();
        Log.d("EqualizerSample", "NumberOfBands: " + bands);

        short min = equalizer.getBandLevelRange()[0];
        short max = equalizer.getBandLevelRange()[1];
        short span = (short)(max - min);
        short midBand = (short)(bands/2);

        for (short i = 0; i < bands; i++) {
            Log.d("EqualizerSample", i + String.valueOf(equalizer.getCenterFreq(i) / 1000) + "Hz");
            //equalizer.setBandLevel(i, (short)((minEQLevel + maxEQLevel) / 2));
            float arctanCurveFactor = (float) (-.38 * Math.atan((float)(i-midBand)) + .5);

            equalizer.setBandLevel(i, (short) (min + (arctanCurveFactor * span)));
        }
    }

    @Override
    public AudioTrack getAudioTrackFor(int n) {
        int pitchClass = Chord.TWELVETONE.mod(n);
        double octavesFromMiddle = ((double) (n - pitchClass)) / ((double) 12);
        double freq = FREQUENCIES[pitchClass] * Math.pow(2, octavesFromMiddle);

        double period = ((double)1)/freq;
        int numFrames = (int)Math.round(period * NATIVE_OUTPUT_SAMPLE_RATE);

        Log.d(TAG, "Creating track for note " + n + " length " + numFrames);

        // Generate the audio sample from a sine wave
        double[] sample = new double[numFrames];
        byte[] generatedSnd = new byte[2 * numFrames];

        // Normalize the overtone series given so we don't overload the speaker
        double overtoneRatioSum = 0;
        double[] overtonesNormalized = new double[overtones.length];
        for(double d : overtones)
            overtoneRatioSum += d;
        for(int i = 0; i < overtones.length; i++)
            overtonesNormalized[i] = overtones[i]/overtoneRatioSum;

        // Generate our tone sample based on the normalized overtone series
        for (int k = 0; k < numFrames; ++k) {
            sample[k] = 0;
            for(int i = 0; i < overtonesNormalized.length; i++)
                sample[k] += overtonesNormalized[i] *  Math.sin((i+1) * 2 * Math.PI * (k) / (NATIVE_OUTPUT_SAMPLE_RATE / freq));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        // Try to make a new AudioTrack. If not possible, go through our
        // list of last used notes and
        // eliminate the LRU and try again
        AudioTrack track = null;
        while (track == null) {
            try {
                track = new AudioTrack(AudioManager.STREAM_MUSIC,
                        NATIVE_OUTPUT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_DEFAULT,
                        AudioFormat.ENCODING_PCM_16BIT, 2 * numFrames,
                        AudioTrack.MODE_STATIC, audioSessionId);
                track.write(generatedSnd, 0, generatedSnd.length);
                track.setLoopPoints(0, numFrames, -1);
                if(track.getState() != AudioTrack.STATE_INITIALIZED) {
                    Log.e(TAG, "Track state: " + track.getState());
                    throw new Exception();
                }
            } catch (Throwable e) {
                if(track != null) {
                    track.flush();
                    track.release();
                    track = null;
                }
                AudioTrackCache.releaseOne();
            }
        }

        return track;
    }

    public int hashCode() {
        return Arrays.hashCode(overtones);
    }
}

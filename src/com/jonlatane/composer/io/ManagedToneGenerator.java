package com.jonlatane.composer.io;

import java.util.*;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.jonlatane.composer.music.harmony.Chord;

/**
 * The ManagedToneGenerator generates AudioTracks in real time.  For speed, we keep
 * the most recently used ones (as many as Android will let us make
 * 
 * @author Jon
 *
 */
public class ManagedToneGenerator {
	private static String TAG = "ToneGenerator";
	
	// The frequencies of C4-B4 (the middle octave)
	private static final double[] freqs = {261.625625, 277.1825, 293.665, 311.1275, 329.6275, 349.22875, 369.995, 391.995, 415.305, 440, 466.16375, 493.88375};
	private static final int sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC); //per second
	private static int _audioSession = 2375;
	private static Equalizer _equalizer = new Equalizer(1,_audioSession);
	static {
		_equalizer.setEnabled(true);
		short bands = _equalizer.getNumberOfBands();
        Log.d("EqualizerSample", "NumberOfBands: " + bands);

        short min = _equalizer.getBandLevelRange()[0];
        short max = _equalizer.getBandLevelRange()[1];
        short span = (short)(max - min);          
        short midBand = (short)(bands/2);
        
        for (short i = 0; i < bands; i++) {
          Log.d("EqualizerSample", i + String.valueOf(_equalizer.getCenterFreq(i) / 1000) + "Hz");
          //_equalizer.setBandLevel(i, (short)((minEQLevel + maxEQLevel) / 2));
          float arctanCurveFactor = (float) (-.38 * Math.atan((float)(i-midBand)) + .5);

          _equalizer.setBandLevel(i, (short)(min + (arctanCurveFactor * span) ));
        }

	}
	
	public static class Cache {
		private static HashMap<Integer,HashMap<Integer,AudioTrack>> _data = new HashMap<Integer,HashMap<Integer,AudioTrack>>();
		private static LinkedList<Pair<Integer,Integer>> _recentlyUsedNotes = new LinkedList<Pair<Integer,Integer>>();
				
		/**
		 * Get a looping AudioTrack exactly one period long for the given note
		 * @param n the fundamental frequency
		 * @param overtones an array from which overtones will be added
		 * @return
		 */
		public static AudioTrack getAudioTrackForNote(int n, Double[] overtones) {
			// Find the hashcode of the overtone series
			int tonesHashCode = Arrays.hashCode(overtones);
			
			// cacheLocation.first tells us the instrument/overtone series (via the hash of the Double[])
			// cacheLocation.second tells us the specific note
			Pair<Integer,Integer> cacheLocation = new Pair<Integer,Integer>(tonesHashCode, n);
			
			// Update list of recently used notes with this first
			_recentlyUsedNotes.remove(cacheLocation);
			_recentlyUsedNotes.addFirst(cacheLocation);
			
			
			
			// See if this note is in our cache
			HashMap<Integer,AudioTrack> instrumentNotes = _data.get(cacheLocation.first);
			if(instrumentNotes == null) {
				instrumentNotes = new HashMap<Integer,AudioTrack>();
				_data.put(cacheLocation.first, instrumentNotes);
			}
			AudioTrack result = instrumentNotes.get(n);
			
			// If not, generate it
			if( result == null ) {
				int pitchClass = Chord.TWELVETONE.getPitchClass(n);
				double octavesFromMiddle = ((double) (n - pitchClass)) / ((double) 12);
				double freq = freqs[pitchClass] * Math.pow(2, octavesFromMiddle);
				
				double period = ((double)1)/freq;
				int numFrames = (int)Math.round(period * sampleRate);
				
				Log.d(TAG,"Creating track for note " + n + " length " + numFrames);
				
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
						sample[k] += overtonesNormalized[i] *  Math.sin(i * 2 * Math.PI * (k) / (sampleRate / freq));
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
								sampleRate, AudioFormat.CHANNEL_OUT_DEFAULT,
								AudioFormat.ENCODING_PCM_16BIT, 2 * numFrames,
								AudioTrack.MODE_STATIC, _audioSession);
						track.write(generatedSnd, 0, generatedSnd.length);
						track.setLoopPoints(0, numFrames, -1);
					} catch (Throwable e) {
						if(track != null) {
							track.release();
							track = null;
						}
						releaseOne();
					}
				}
				
				instrumentNotes.put(n, track);
				
				result = track;
			}
			
			return result;
		}
		
		/**
		 * Release all AudioTracks created by this cache
		 * 
		 * @return
		 */
		public static boolean releaseAll() {
			boolean result = releaseOne();
			boolean shouldLoopAgain = result;
			while(shouldLoopAgain == true) {
				shouldLoopAgain = releaseOne();
			}
			return result;
		}
		
		
		public static boolean releaseOne() {
			boolean result = true;
			try {
				Pair<Integer, Integer> lruNote = _recentlyUsedNotes.removeLast();
				_data.get(lruNote.first).get(lruNote.second).release();
				_data.get(lruNote.first).remove(lruNote.second);
			} catch( Throwable e ) {
				result = false;
			}
			return result;
		}
	}
	
	private Double[] _overtones;
	public ManagedToneGenerator(Double... defaultOvertones) {
		_overtones = defaultOvertones;
	}

	public AudioTrack getAudioTrackForNote(int n) {
		return Cache.getAudioTrackForNote(n, _overtones);
	}
	
	public AudioTrack getAudioTrackForNote(int n, Double... overtones) {
		return Cache.getAudioTrackForNote(n, overtones);
	}
	
	
}

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
	
	private static class Cache {
		private static SparseArray<SparseArray<AudioTrack>> _data = new SparseArray<SparseArray<AudioTrack>>();
		private static LinkedList<Pair<Integer,Integer>> _recentlyUsedNotes = new LinkedList<Pair<Integer,Integer>>();
		
		static int tonesHashCode(Double[] overtones) {
			return Arrays.hashCode(overtones);
		}
		
		/**
		 * Get a looping AudioTrack exactly one period long for the given note.  This may be generated,
		 * or it may come from the cache. The track requested will become the MRU element.
		 * 
		 * @param n the fundamental frequency
		 * @param overtones an array from which overtones will be added.
		 * 		overtones[0] is the strength of the fundamental frequency, [1..n] is that of all others.
		 * 		The track volume will be normalized so values only relate to each other.
		 * @return the requested AudioTrack
		 */
		static AudioTrack getAudioTrackForNote(int n, Double[] overtones) {
			// Find the hashcode of the overtone series
			int tonesHashCode = tonesHashCode(overtones);
			
			// cacheLocation.first tells us the instrument/overtone series (via the hash of the Double[])
			// cacheLocation.second tells us the specific note
			Pair<Integer,Integer> cacheLocation = new Pair<Integer,Integer>(tonesHashCode, n);
			
			// Update list of recently used notes with this first
			_recentlyUsedNotes.remove(cacheLocation);
			_recentlyUsedNotes.addFirst(cacheLocation);
			
			
			
			// See if this note is in our cache
			SparseArray<AudioTrack> instrumentNotes = _data.get(cacheLocation.first);
			if(instrumentNotes == null) {
				instrumentNotes = new SparseArray<AudioTrack>();
				_data.put(cacheLocation.first, instrumentNotes);
			}
			AudioTrack result = instrumentNotes.get(n);
			
			// If not, generate it
			if( result == null ) {
				int pitchClass = Chord.TWELVETONE.mod(n);
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
						sample[k] += overtonesNormalized[i] *  Math.sin((i+1) * 2 * Math.PI * (k) / (sampleRate / freq));
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
						if(track.getState() != AudioTrack.STATE_INITIALIZED)
							throw new Exception();
					} catch (Throwable e) {
						if(track != null) {
							track.flush();
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
		 * @return false unless there was an error
		 */
		public static boolean releaseAll() {
			boolean result = releaseOne();
			boolean shouldLoopAgain = result;
			while(shouldLoopAgain == true) {
				shouldLoopAgain = releaseOne();
			}
			return result;
		}
		
		/**
		 * Release all AudioTracks using the given overtone series
		 * 
		 * @param overtones
		 * @return true on success
		 */
		public static boolean releaseAll(Double[] overtones) {
			SparseArray<AudioTrack> instrumentNotes = _data.get(tonesHashCode(overtones));
			if(instrumentNotes == null)
				return true;
			boolean result = true;
			for(int i=0; i < instrumentNotes.size(); i++) {
				try {
					AudioTrack goodbyeCruelWorld = instrumentNotes.valueAt(i);
					goodbyeCruelWorld.stop();
					goodbyeCruelWorld.flush();
					goodbyeCruelWorld.release();
				} catch( Throwable e ) {
					result = false;
				}
			}
			instrumentNotes.clear();
			return result;
		}
		
		/**
		 * Release the least recently used AudioTrack resource to free up resources
		 * 
		 * @return true if a track was successfully removed
		 */
		public static boolean releaseOne() {
			boolean result = true;
			try {
				Pair<Integer, Integer> lruNote = _recentlyUsedNotes.removeLast();
				AudioTrack goodbyeCruelWorld = _data.get(lruNote.first).get(lruNote.second);
				goodbyeCruelWorld.flush();
				goodbyeCruelWorld.release();
				_data.get(lruNote.first).remove(lruNote.second);
			} catch( Throwable e ) {
				result = false;
			}
			return result;
		}
		
		public static void normalizeVolumes() {
			float max = AudioTrack.getMaxVolume();
			float min = AudioTrack.getMinVolume();
			float span = max - min;
			
			//Set<AudioTrack> currentlyPlaying = new HashSet<AudioTrack>();
			SparseArray<List<AudioTrack>> currentlyPlaying = new SparseArray<List<AudioTrack>>();
			//for(Map.Entry<Integer,HashMap<Integer, AudioTrack>> e : _data.entrySet()) {
			for(int i = 0; i < _data.size(); i++) {
				SparseArray<AudioTrack> noteTracks = _data.valueAt(i);
				//for(AudioTrack t : e.getValue().values()) {
				for(int j = 0; j < noteTracks.size(); j++) {
					AudioTrack t = noteTracks.valueAt(j); 
					if(t.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
						int n = _data.keyAt(i);
						List<AudioTrack> l = currentlyPlaying.get(n);
						if(l == null) {
							l = new LinkedList<AudioTrack>();
							currentlyPlaying.put(n,l);
						}
						l.add(t);
					}
				}
			}
			
			for(int i = 0; i < currentlyPlaying.size(); i++) {
				int n = currentlyPlaying.keyAt(i);
				List<AudioTrack> l = currentlyPlaying.valueAt(i);
				for(AudioTrack t : l) {
					// Lower notes are amped up so turn them down a bit with this factor
					float arctanCurveFactor = (float) (.05 * Math.atan((float)(n+5)/88.0) + .9);
					// This number between 0 and 1 by which we will increase volume
					float totalNumNotesRedFactor = (float)( 1 / (float)currentlyPlaying.size());
					float adjusted = min 
							+ (float)( span * arctanCurveFactor * totalNumNotesRedFactor );
					Log.i(TAG,max + " " + min + "Normalizing volume to " + adjusted);
					t.setStereoVolume(adjusted, adjusted);
				}
			}
		}
	}
	
	private static Double[] _defaultOvertones = {70., 30., 30., 10., 10., 20., 20., 1.};
	//private static Double[] _defaultOvertones = {70., 0., 0., 0., 0., 0., 0., 0. };
	
	Double[] overtones;
	
	/**
	 * An instance of a ManagedToneGenerator shares the same cache as all others.  Its only unique property is the overtone series
	 * used to generate its tambre.  Most useful instance methods access static methods using the overtone series provided.
	 * 
	 * @param overtones
	 */
	public ManagedToneGenerator(Double... overtones) {
		if(overtones.length == 0)
			overtones = _defaultOvertones.clone();
		
		this.overtones = _defaultOvertones;
	}

	public static AudioTrack getAudioTrackForNote(int n) {
		return Cache.getAudioTrackForNote(n, _defaultOvertones);
	}
	
	public AudioTrack getCustomAudioTrackForNote(int n) {
		return Cache.getAudioTrackForNote(n, overtones);
	}
	
	public void releaseAllMyTracks() {
		Cache.releaseAll(overtones);
	}
	
	public static AudioTrack getAudioTrackForNote(int n, Double... overtones) {
		return Cache.getAudioTrackForNote(n, overtones);
	}

	public static void normalizeVolumes() {
		Cache.normalizeVolumes();
	}
	
	public static void releaseAudioResources() {
		Cache.releaseAll();
	}
}

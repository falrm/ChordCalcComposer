package com.jonlatane.composer.input;

import java.util.*;

import com.jonlatane.composer.ChordDisplayActivity;
import com.jonlatane.composer.R;
import com.jonlatane.composer.music.harmony.*;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class KeyboardIOHandler implements OnLongClickListener, OnTouchListener {
	private static String TAG = "KBDIO";
	
	private static int[] _keys = new int[] 
			{ R.id.keyA0, R.id.keyAS0, R.id.keyB0,
		R.id.keyC1, R.id.keyCS1, R.id.keyD1, R.id.keyDS1,R.id.keyE1, R.id.keyF1, R.id.keyFS1, R.id.keyG1,R.id.keyGS1, R.id.keyA1, R.id.keyAS1, R.id.keyB1,
		R.id.keyC2, R.id.keyCS2, R.id.keyD2, R.id.keyDS2,R.id.keyE2, R.id.keyF2, R.id.keyFS2, R.id.keyG2,R.id.keyGS2, R.id.keyA2, R.id.keyAS2, R.id.keyB2,
		R.id.keyC3, R.id.keyCS3, R.id.keyD3, R.id.keyDS3,R.id.keyE3, R.id.keyF3, R.id.keyFS3, R.id.keyG3,R.id.keyGS3, R.id.keyA3, R.id.keyAS3, R.id.keyB3,
		R.id.keyC4, R.id.keyCS4, R.id.keyD4, R.id.keyDS4,R.id.keyE4, R.id.keyF4, R.id.keyFS4, R.id.keyG4,R.id.keyGS4, R.id.keyA4, R.id.keyAS4, R.id.keyB4,
		R.id.keyC5, R.id.keyCS5, R.id.keyD5, R.id.keyDS5,R.id.keyE5, R.id.keyF5, R.id.keyFS5, R.id.keyG5,R.id.keyGS5, R.id.keyA5, R.id.keyAS5, R.id.keyB5,
		R.id.keyC6, R.id.keyCS6, R.id.keyD6, R.id.keyDS6,R.id.keyE6, R.id.keyF6, R.id.keyFS6, R.id.keyG6,R.id.keyGS6, R.id.keyA6, R.id.keyAS6, R.id.keyB6,
		R.id.keyC7, R.id.keyCS7, R.id.keyD7, R.id.keyDS7,R.id.keyE7, R.id.keyF7, R.id.keyFS7, R.id.keyG7,R.id.keyGS7, R.id.keyA7, R.id.keyAS7, R.id.keyB7,
		R.id.keyC8
			};
				
	private static SparseIntArray _keysInverse = new SparseIntArray();
	
	private static HashSet<Integer> blackKeys = new HashSet<Integer>();
	static {
		for( int i = 0; i < _keys.length; i = i +  1 ) {
			_keysInverse.put(_keys[i], i-39);
		}
		blackKeys.add(1);
		blackKeys.add(3);
		blackKeys.add(6);
		blackKeys.add(8);
		blackKeys.add(10);
	}
	
	// Harmonic input-related fields
	private boolean _harmonicMode = false;
	private boolean _cancelHarmonicLongPressRootSelection = false;
	private Integer _harmonicRoot = null;
	
	private Set<Integer> _currentlyPressed = Collections.synchronizedSet(new HashSet<Integer>());
	private Activity _myActivity;
	
	public KeyboardIOHandler(Activity a) {
		_myActivity = a;
		for(int k : _keys) {
			Button b = (Button)(a.findViewById(k));
			b.setOnTouchListener(this);
			b.setOnLongClickListener(this);
		}
		((KeyboardScroller)a.findViewById(R.id.kbScroller)).setKeyboardIOHander(this);
	}
	
	// The frequencies of C4-B4 (the middle octave)
	private static final double[] freqs = {261.625625, 277.1825, 293.665, 311.1275, 329.6275, 349.22875, 369.995, 391.995, 415.305, 440, 466.16375, 493.88375};
	private static final int sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC); //per second
	private static HashMap<Integer,AudioTrack> _noteTracks = new HashMap<Integer, AudioTrack>();
	private static LinkedList<Integer> _recentlyUsedNotes = new LinkedList<Integer>();
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
	
	public static AudioTrack getAudioTrackForNote(int n) {
		return getAudioTrackForNote(n, new double[] {70, 30, 30, 10, 10, 20, 20, 1});//, 23, 23, 43, 32, 43, 32, 34, 20, 29, 26  });
	}
	
	/**
	 * Because there's 88 notes and Android only lets us have 32 (<32?) AudioTracks, we must keep a cache of 
	 * @param n the note (-39 to 48, with C4 = 0)
	 * @return a looping AudioTrack that plays the given note
	 */
	public static AudioTrack getAudioTrackForNote(int n, double[] overtones) {
		// Update our recently used notes
		_recentlyUsedNotes.remove((Integer)n);
		_recentlyUsedNotes.addFirst(n);
		
		// See if this note is in our cache
		AudioTrack result = _noteTracks.get(n);
		
		// If not, generate it
		if( result == null ) {
			int pitchClass = Chord.TWELVETONE.getPitchClass(n);
			double octavesFromMiddle = ((double) (n - pitchClass)) / ((double) 12);
			double freq = freqs[pitchClass] * Math.pow(2, octavesFromMiddle);
			
			double sevenPeriods = 7*((double)1)/freq;
			int numFrames = (int)Math.round(sevenPeriods * sampleRate);
			
			Log.d(TAG,"Creating track for note " + n + " length " + numFrames);
			
			// Generate the audio sample from a sine wave
			double[] sample = new double[numFrames];
			byte[] generatedSnd = new byte[2 * numFrames];
			
			double overtoneRatioSum = 0;
			for(double d : overtones)
				overtoneRatioSum += d;
					
			double[] overtonesNormalized = new double[overtones.length];
			for(int i = 0; i < overtones.length; i++)
				overtonesNormalized[i] = overtones[i]/overtoneRatioSum;
			
			
			
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
					int lruNote = _recentlyUsedNotes.removeLast();
					_noteTracks.get(lruNote).release();
					_noteTracks.remove(lruNote);
				}
			}
			
			_noteTracks.put(n, track);
			
			result = track;
		}
		
		return result;
	}
	
	public void harmonicModeOn() {
		_harmonicMode = true;
		_harmonicRoot = null;
	}
	
	
	public void harmonicModeOff() {
		clearHarmonicRoot();
		_harmonicMode = false;
	}
	
	public boolean isHarmonic() {
		return _harmonicMode;
	}

	void normalizeVolume() {
		float max = AudioTrack.getMaxVolume();
		float min = AudioTrack.getMinVolume();
		float span = max - min;
		
		for(int n : _currentlyPressed) {
			AudioTrack t = getAudioTrackForNote(n);
			
			// Lower notes are amped up so turn them down a bit with this factor
			float arctanCurveFactor = (float) (.05 * Math.atan((float)(n+5)/88.0) + .9);
			//if( n < 0 )
				//arctanCurveFactor = 1;
			// This number between 0 and 1 by which we will increase volume
			float totalNumNotesRedFactor = (float)( 1 / (float)_currentlyPressed.size());
			//if( n < 0 )
				//totalNumNotesRedFactor = 1;
			
			float adjusted = min 
					+ (float)( span * arctanCurveFactor * totalNumNotesRedFactor );
			
			Log.i(TAG,max + " " + min + "Normalizing volume to " + adjusted);
			t.setStereoVolume(adjusted, adjusted);
		}
	}
	void liftNote(int n) {
		if( _harmonicMode ) {
			//Log.i(TAG, "Harmonic Root:")
			if(getHarmonicRoot() != null && getHarmonicRoot()==n) {
				clearHarmonicRoot();
			}
			if(_cancelHarmonicLongPressRootSelection && _currentlyPressed.size() == 1)
				_cancelHarmonicLongPressRootSelection = false;
		}
		_currentlyPressed.remove(n);
		normalizeVolume();
		getAudioTrackForNote(n).pause();
		getAudioTrackForNote(n).setPlaybackHeadPosition(0);
	}
	
	void pressNote(int n) {
		if( _harmonicMode ) {
			if(getHarmonicRoot() == null) {
				if(_currentlyPressed.size() == 0)
					_cancelHarmonicLongPressRootSelection = false;
				else
					_cancelHarmonicLongPressRootSelection = true;
			}
			Log.i(TAG, "Got key Press " + harmonicInfo());
		} else {
			// Melodic mode, one note at a time!
			for( int m : _currentlyPressed ) {
				liftNote(m);
			}
		}
		_currentlyPressed.add(n);
		normalizeVolume();
		getAudioTrackForNote(n).play();
		_recentlyUsedNotes.remove((Integer)n);
		_recentlyUsedNotes.addFirst((Integer)n);
		
		// The magic
		if( _myActivity.getClass().equals(ChordDisplayActivity.class) ) {
			((ChordDisplayActivity)_myActivity).updateChordDisplay();
		}
	}
	
	@Override
	public boolean onLongClick(View arg0) {
		//Log.i(TAG, "Got LongClick");
		if(_harmonicMode && (getHarmonicRoot() == null)) {
			if(!_cancelHarmonicLongPressRootSelection && _currentlyPressed.size() == 1) {
				//_harmonicRoot = _keysInverse.get(arg0.getId());
				setHarmonicRoot(_keysInverse.get(arg0.getId()));
			}
		}
		return false;
	}

	public synchronized void setHarmonicRoot(int note) {
		clearHarmonicRoot();
		Log.i(TAG,"Setting Root to " + Key.CMajor.getNoteName(note) + "(" + note + ")");
		_harmonicRoot = note;
		if(blackKeys.contains(Chord.TWELVETONE.getPitchClass(_harmonicRoot)))
			((Button)_myActivity.findViewById(_keys[_harmonicRoot+39])).setBackgroundResource(R.drawable.rootblackkey);
		else
			((Button)_myActivity.findViewById(_keys[_harmonicRoot+39])).setBackgroundResource(R.drawable.rootwhitekey);
	}
	
	public synchronized void clearHarmonicRoot() {
		//Log.i(TAG,"Clearing Root");
		if(_harmonicRoot != null) {
			if(blackKeys.contains(Chord.TWELVETONE.getPitchClass(_harmonicRoot)))
				((Button)_myActivity.findViewById(_keys[_harmonicRoot+39])).setBackgroundResource(R.drawable.blackkey);
			else
				((Button)_myActivity.findViewById(_keys[_harmonicRoot+39])).setBackgroundResource(R.drawable.whitekey);
			
			_harmonicRoot = null;
		}
	}
	
	public Integer getHarmonicRoot() {
		return _harmonicRoot;
	}
	
	// Utility method in case the keyboard is scrolled when keys are pressed.
	void catchRogues() {
		if(getHarmonicRoot() != null) {
			Button b = (Button)_myActivity.findViewById(_keys[getHarmonicRoot()+39]);
			if(!b.isPressed()) {
				Log.i(TAG,"Cleared Root");
				clearHarmonicRoot();
			}
		}
		Iterator<Integer> iter = _currentlyPressed.iterator();
		/*for( int i : _currentlyPressed ) {*/
		while(iter.hasNext()) {
			int n = iter.next();
			Button b = (Button)_myActivity.findViewById(_keys[n+39]);
			if(!b.isPressed()) {
				iter.remove();
				liftNote(n);
			}
		}
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		catchRogues();
		Log.i(TAG, "onTouch IOHandler");

		boolean result = false;
		
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			pressNote(_keysInverse.get(arg0.getId()));
			Log.i(TAG, "Got key Press " + harmonicInfo());
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			liftNote(_keysInverse.get(arg0.getId()));
		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE 
				&& event.getPointerCount() != 1 
				&& getPressedKeys().size() > 1) {
			Log.i(TAG,"onTouch Disallow");
			result = true;
		}
		
		return result;
	}
	
	public PitchSet getPressedKeys() {
		return new PitchSet(_currentlyPressed);
	}
	
	public Chord getChord() {
		Chord c = new Chord(_currentlyPressed);
		c.setRoot(getHarmonicRoot());
		return c;
	}
	
	public String harmonicInfo() {
		String result = "";
		if(_harmonicMode) {
			result += "H: ";
			if(_harmonicRoot != null)
				result += "R:" + _harmonicRoot;
			result += "[";
			for(int i : _currentlyPressed) {
				result += i + ",";
			}
			result += "]";
			
			result += " " + getHarmonicChord();
		} else {
			result += "M: ";
			result += "[";
			for(int i : _currentlyPressed) {
				result += i + ",";
			}
			result += "]";
		}
		
		return result;
	}
	
	public String getHarmonicChord() {
		String result = "";
		Chord c = new Chord();
		for(int i : _currentlyPressed) {
			c.add(i);
		}
		Log.i(TAG,"Constructed Chord:" + c.toString());
		if(_harmonicMode) {
			if (_harmonicRoot != null) {
				Log.i(TAG, "Getting chord for pre-specified root: " + _harmonicRoot);
				Pair<String,Integer> p = Key.guessNameInC(c, _harmonicRoot);
				result += p.first + ": " + p.second;
			} else {
				Log.i(TAG, "No root specified, getting chord");
				Pair<String,Integer> p = Key.guessNameInC(c);
				result += p.first + ": " + p.second;
			}
		}
		
		
		return result;
	}
}

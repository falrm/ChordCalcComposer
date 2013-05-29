package com.jonlatane.composer.io;

import java.util.*;

import com.jonlatane.composer.R;
import com.jonlatane.composer.music.harmony.*;

import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;

public class KeyboardIOHandler implements OnLongClickListener, OnTouchListener {
	private static String TAG = "KBDIO";
	
	// References to other things in this package
	private final KeyboardScroller _kbs;
	
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
	static {
		for( int i = 0; i < _keys.length; i = i +  1 ) {
			_keysInverse.put(_keys[i], i-39);
		}
	}
	
	// Harmonic input-related fields
	private boolean _harmonicMode = false;
	private boolean _cancelHarmonicLongPressRootSelection = false;
	
	//private Integer _harmonicRoot = null;
	private Chord _harmonicChord = null;
	
	private Set<Integer> _currentlyPressed = Collections.synchronizedSet(new HashSet<Integer>());
	private ManagedToneGenerator _myToneGenerator = new ManagedToneGenerator();
	private TwelthKeyboardFragment _myFragment;
	
	public KeyboardIOHandler(TwelthKeyboardFragment f, View v) {
		_myFragment = f;
		for(int k : _keys) {
			Button b = (Button)(v.findViewById(k));
			b.setOnTouchListener(this);
			b.setOnLongClickListener(this);
			ViewTreeObserver o = b.getViewTreeObserver();
			
			// Make sure we don't get stuck keys
			o.addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					catchRogues();
					return true;
				}
			});
		}
		_kbs = ((KeyboardScroller)v.findViewById(R.id.kbScroller));
		_kbs.setKeyboardIOHander(this);
	}
	
	public void harmonicModeOn() {
		_harmonicMode = true;
		_harmonicChord = null;
	}
	
	
	public void harmonicModeOff() {
		clearHarmonicRoot();
		_harmonicMode = false;
	}
	
	public boolean isHarmonic() {
		return _harmonicMode;
	}

	/*void normalizeVolume() {
		float max = AudioTrack.getMaxVolume();
		float min = AudioTrack.getMinVolume();
		float span = max - min;
		
		for(int n : _currentlyPressed) {
			AudioTrack t = ManagedToneGenerator.getAudioTrackForNote(n);
			
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
	}*/
	
	void liftNote(int n) {
		if( _harmonicMode ) {
			//Log.i(TAG, "Harmonic Root:")
			if(_cancelHarmonicLongPressRootSelection && _currentlyPressed.size() == 1)
				_cancelHarmonicLongPressRootSelection = false;
		}
		_currentlyPressed.remove(n);

		if(getHarmonicRoot() != null && _currentlyPressed.size() == 0) {
			clearHarmonicRoot();
		}
		
		AudioTrack t = _myToneGenerator.getCustomAudioTrackForNote(n);
		t.pause();
		ManagedToneGenerator.normalizeVolumes();
		t.setPlaybackHeadPosition(0);
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
		_myToneGenerator.getCustomAudioTrackForNote(n).play();
		ManagedToneGenerator.normalizeVolumes();
		//_recentlyUsedNotes.remove((Integer)n);
		//_recentlyUsedNotes.addFirst((Integer)n);
				
		// The magic
		if( _harmonicMode ) {
			_myFragment.updateChordDisplay();
		}
	}
	
	@Override
	public boolean onLongClick(View arg0) {
		if(_harmonicMode && (getHarmonicRoot() == null)) {
			if(!_cancelHarmonicLongPressRootSelection && _currentlyPressed.size() == 1) {
				setHarmonicRoot(_keysInverse.get(arg0.getId()));
			}
		}
		return false;
	}
	
	/**
	 * Highlights a dominant 7 chord with the given root.
	 * 
	 * @param note
	 */
	public void setHarmonicRoot(final int note) {
		new AsyncTask<String,Integer,Chord>() {
			@Override
			protected Chord doInBackground(String... params) {
				return Chord.getChordByName( Key.CMajor.getNoteName(note) + "7" );
			}
			
			@Override
			protected void onPostExecute(Chord result) {
		        setHarmonicChord(result);
		     }
		}.execute(Key.CMajor.getNoteName(note) + "7");
	}
	
	/**
	 * Highlights the given chord on the keyboard
	 * @param c
	 */
	public void setHarmonicChord(Chord c) {
		_harmonicChord = c;
		if(_harmonicChord != null) {
			Log.i(TAG,"Highlighting chord " + Key.CMajor.getNoteName(_harmonicChord.getRoot()) + _harmonicChord.toString());

			for(int id : _keys) {
				Button b = (Button)_myFragment.getView().findViewById(id);
				int n = _keysInverse.get(id);
				int nClass = Chord.TWELVETONE.mod(n);
				boolean isRoot = (c.getRoot() != null && nClass == c.getRoot());
				boolean isBlack = isBlack(n);
				boolean isInChord = c.contains(nClass);
				if(isBlack) {
					if(isRoot) {
						b.setBackgroundResource(R.drawable.rootblackkey);
					} else if(isInChord) {
						b.setBackgroundResource(R.drawable.highlightedblackkey);
					} else {
						b.setBackgroundResource(R.drawable.blackkey);
					}
				} else {
					if(isRoot) {
						b.setBackgroundResource(R.drawable.rootwhitekey);
					} else if(isInChord) {
						b.setBackgroundResource(R.drawable.highlightedwhitekey);
					} else {
						b.setBackgroundResource(R.drawable.whitekey);
					}
				}
			}
		} else {
			Log.i(TAG, "Clearing highlights");
			for(int id : _keys) {
				int n = _keysInverse.get(id);
				if(isBlack(n)) {
					((Button)_myFragment.getView().findViewById(id)).setBackgroundResource(R.drawable.blackkey);
				} else {
					((Button)_myFragment.getView().findViewById(id)).setBackgroundResource(R.drawable.whitekey);
				}
			}
		}
	}
	
	boolean isBlack(int note) {
		int nClass = Chord.TWELVETONE.mod(note);
		return (nClass == 1 || nClass == 3 || nClass == 6  || nClass == 8  || nClass == 10);
	}
	
	public synchronized void clearHarmonicRoot() {
		//Log.i(TAG,"Clearing Root");
		setHarmonicChord(null);
	}
	
	public Integer getHarmonicRoot() {
		return (_harmonicChord == null) ? null : _harmonicChord.getRoot();
	}
	
	// Utility method in case the keyboard is scrolled when keys are pressed.
	void catchRogues() {
		if(_myFragment.getView() != null) {
			Iterator<Integer> iter = _currentlyPressed.iterator();
			while(iter.hasNext()) {
				int n = iter.next();
				Button b = (Button)_myFragment.getView().findViewById(_keys[n+39]);
				if(!b.isPressed()) {
					iter.remove();
					liftNote(n);
				}
			}
		}
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		catchRogues();
		Log.i(TAG, "onTouch IOHandler.  This should be from a Button on the keyboard.");
		
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
		Log.i(TAG,"Found Chord " + c.toString());
		return c;
	}
	
	public String harmonicInfo() {
		String result = "";
		if(_harmonicMode) {
			result += "H: ";
			if(_harmonicChord != null && _harmonicChord.getRoot() != null)
				result += "R:" + _harmonicChord.getRoot();
			result += "[";
			for(int i : _currentlyPressed) {
				result += i + ",";
			}
			result += "]";
			
			//result += " " + getHarmonicChord();
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
	
	/*public String getHarmonicChord() {
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
	}*/
}

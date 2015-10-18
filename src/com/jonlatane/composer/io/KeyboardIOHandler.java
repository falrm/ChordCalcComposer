package com.jonlatane.composer.io;

import android.content.Context;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;

import com.jonlatane.composer.R;
import com.jonlatane.composer.audio.AudioTrackCache;
import com.jonlatane.composer.audio.AudioTrackGenerator;
import com.jonlatane.composer.audio.generator.HarmonicOvertoneSeriesGenerator;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.Key;
import com.jonlatane.composer.music.harmony.PitchSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class KeyboardIOHandler implements OnLongClickListener, OnTouchListener {
	private static String TAG = "KBDIO";

	private static int[] KEY_IDS = new int[]
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
	private static SparseIntArray KEY_IDS_INVERSE = new SparseIntArray();
	static {
		for( int i = 0; i < KEY_IDS.length; i = i +  1 ) {
			KEY_IDS_INVERSE.put(KEY_IDS[i], i - 39);
		}
	}
	
	// Harmonic input-related fields
	private boolean harmonicMode = false;
	private boolean cancelHarmonicLongPressRootSelection = false;
	
	private Chord harmonicChord = null;
	
	private Set<Integer> currentlyPressed = Collections.synchronizedSet(new HashSet<Integer>());
	AudioTrackGenerator trackGenerator = new HarmonicOvertoneSeriesGenerator();
	private TwelthKeyboardFragment keyboardFragment;
	private final KeyboardScroller keyboardScroller;
	
	public KeyboardIOHandler(TwelthKeyboardFragment f, View v) {
		keyboardFragment = f;
		for(int k : KEY_IDS) {
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
		keyboardScroller = ((KeyboardScroller)v.findViewById(R.id.kbScroller));
	}
	
	public void harmonicModeOn() {
		harmonicMode = true;
		harmonicChord = null;
	}
	
	
	public void harmonicModeOff() {
		clearHarmonicRoot();
		harmonicMode = false;
	}
	
	public boolean isHarmonic() {
		return harmonicMode;
	}
	
	void liftNote(int n) {
		synchronized(currentlyPressed) {
			if(harmonicMode) {
				//Log.i(TAG, "Harmonic Root:")
				if(cancelHarmonicLongPressRootSelection && currentlyPressed.size() == 1)
					cancelHarmonicLongPressRootSelection = false;
			}
			currentlyPressed.remove(n);
		}

		if(getHarmonicRoot() != null && currentlyPressed.size() == 0) {
			clearHarmonicRoot();
		}
		
		AudioTrack t = AudioTrackCache.getAudioTrackForNote(n, trackGenerator);
		t.pause();
		AudioTrackCache.normalizeVolumes();
		//t.setPlaybackHeadPosition(0);
		//t.reloadStaticData();
	}
	void pressNote(int n) {
		synchronized(currentlyPressed) {
			if(harmonicMode) {
				if(getHarmonicRoot() == null) {
					if(currentlyPressed.size() == 0)
						cancelHarmonicLongPressRootSelection = false;
					else
						cancelHarmonicLongPressRootSelection = true;
				}
				Log.i(TAG, "Got key Press " + harmonicInfo());
			} else {
				// Melodic mode, one note at a time!
				for( int m : currentlyPressed) {
					liftNote(m);
				}
			}
			currentlyPressed.add(n);
		}
		//_toneGenerator.getCustomAudioTrackForNote(n).play();
		AudioTrackCache.getAudioTrackForNote(n, trackGenerator).play();
		AudioTrackCache.normalizeVolumes();

		// The magic
		if(harmonicMode) {
			keyboardFragment.updateChordDisplay();
		}
	}
	
	@Override
	public boolean onLongClick(View view) {
		if(harmonicMode && (getHarmonicRoot() == null)) {
			if(!cancelHarmonicLongPressRootSelection && currentlyPressed.size() == 1) {
				Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(50);
				setHarmonicRoot(KEY_IDS_INVERSE.get(view.getId()));
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
		harmonicChord = c;
		if(harmonicChord != null) {
			Log.i(TAG,"Highlighting chord " + Key.CMajor.getNoteName(harmonicChord.getRoot()) + harmonicChord.toString());

			for(int id : KEY_IDS) {
				Button b = (Button) keyboardFragment.getView().findViewById(id);
				int n = KEY_IDS_INVERSE.get(id);
				int nClass = Chord.TWELVETONE.mod(n);
				boolean isRoot = (c.getRoot() != null && nClass == c.getRoot());
				boolean isBlack = isBlack(n);
				boolean isInChord = c.contains(nClass);
				if(isBlack) {
					if(isRoot) {
						b.setBackgroundResource(R.drawable.key_black_highlighted_root);
					} else if(isInChord) {
						b.setBackgroundResource(R.drawable.key_black_highlighted);
					} else {
						b.setBackgroundResource(R.drawable.key_black);
					}
				} else {
					if(isRoot) {
						b.setBackgroundResource(R.drawable.key_white_highlighted_root);
					} else if(isInChord) {
						b.setBackgroundResource(R.drawable.key_white_highlighted);
					} else {
						b.setBackgroundResource(R.drawable.key_white);
					}
				}
			}
		} else {
			Log.i(TAG, "Clearing highlights");
			for(int id : KEY_IDS) {
				int n = KEY_IDS_INVERSE.get(id);
				if(isBlack(n)) {
					((Button) keyboardFragment.getView().findViewById(id)).setBackgroundResource(R.drawable.key_black);
				} else {
					((Button) keyboardFragment.getView().findViewById(id)).setBackgroundResource(R.drawable.key_white);
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
		return (harmonicChord == null) ? null : harmonicChord.getRoot();
	}
	
	// Utility method in case the keyboard is scrolled when keys are pressed.
	void catchRogues() {
		if(keyboardFragment.getView() != null) {
			Iterator<Integer> iter = currentlyPressed.iterator();
			while(iter.hasNext()) {
				int n = iter.next();
				Button b = (Button) keyboardFragment.getView().findViewById(KEY_IDS[n+39]);
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
			pressNote(KEY_IDS_INVERSE.get(arg0.getId()));
			Log.i(TAG, "Got key Press " + harmonicInfo());
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			liftNote(KEY_IDS_INVERSE.get(arg0.getId()));
		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE 
				&& event.getPointerCount() != 1 
				&& getPressedKeys().size() > 1) {
			Log.i(TAG,"onTouch Disallow");
			result = true;
		}
		
		return result;
	}
	
	public PitchSet getPressedKeys() {
		return new PitchSet(currentlyPressed);
	}
	
	public Chord getChord() {
		Chord c = new Chord(currentlyPressed);
		c.setRoot(getHarmonicRoot());
		Log.i(TAG,"Found Chord " + c.toString());
		return c;
	}
	
	public String harmonicInfo() {
		String result = "";
		if(harmonicMode) {
			result += "H: ";
			if(harmonicChord != null && harmonicChord.getRoot() != null)
				result += "R:" + harmonicChord.getRoot();
			result += "[";
			for(int i : currentlyPressed) {
				result += i + ",";
			}
			result += "]";
			
			//result += " " + getHarmonicChord();
		} else {
			result += "M: ";
			result += "[";
			for(int i : currentlyPressed) {
				result += i + ",";
			}
			result += "]";
		}
		
		return result;
	}
	
	/*public String getHarmonicChord() {
		String result = "";
		Chord c = new Chord();
		for(int i : currentlyPressed) {
			c.add(i);
		}
		Log.i(TAG,"Constructed Chord:" + c.toString());
		if(harmonicMode) {
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

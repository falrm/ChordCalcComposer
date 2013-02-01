package com.jonlatane.composer.input;

import java.util.*;

import com.jonlatane.composer.R;
import com.jonlatane.composer.R.drawable;
import com.jonlatane.composer.R.id;
import com.jonlatane.composer.music.harmony.*;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;

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

	@Override
	public boolean onLongClick(View arg0) {
		Log.i(TAG, "Got LongClick");
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
		Log.i(TAG,"Clearing Root");
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
	private void catchRogues() {
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
			int i = iter.next();
			Button b = (Button)_myActivity.findViewById(_keys[i+39]);
			if(!b.isPressed()) {
				iter.remove();
			}
		}
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		catchRogues();
		if( _harmonicMode ) {
			if(arg1.getActionMasked() == MotionEvent.ACTION_DOWN) {
				_currentlyPressed.add(_keysInverse.get(arg0.getId()));
				if(getHarmonicRoot() == null) {
					if(_currentlyPressed.size() == 1)
						_cancelHarmonicLongPressRootSelection = false;
					else
						_cancelHarmonicLongPressRootSelection = true;
				}
				Log.i(TAG, "Got key Press " + harmonicInfo());
			}
			if(arg1.getActionMasked() == MotionEvent.ACTION_UP) {
				_currentlyPressed.remove(_keysInverse.get(arg0.getId()));
				//Log.i(TAG, "Harmonic Root:")
				if(getHarmonicRoot() != null && getHarmonicRoot()==_keysInverse.get(arg0.getId())) {
					clearHarmonicRoot();
				}
				if(_cancelHarmonicLongPressRootSelection && _currentlyPressed.size() == 0)
					_cancelHarmonicLongPressRootSelection = false;
			}
		} else {
			Log.i(TAG, "MotionEvent in Melodic Mode");
			if(arg1.getActionMasked() == MotionEvent.ACTION_DOWN) {
				_currentlyPressed.clear();
				_currentlyPressed.add(_keysInverse.get(arg0.getId()));

				//Log.i(TAG, "Got key Press " + harmonicInfo());
			}
		}
		return false;
	}
	
	public PitchSet getPressedKeys() {
		return new PitchSet(_currentlyPressed);
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

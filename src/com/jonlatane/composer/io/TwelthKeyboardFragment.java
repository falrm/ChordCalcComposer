package com.jonlatane.composer.io;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jonlatane.composer.R;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.Key;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is the widget for the keyboard input system.  This is a Fragment that can be added to the bottom of any layout to provide
 * a hideable keyboard overlay.  Some of its features include melodic (single-note) input, chord-guessing harmonic input (associating,
 * essentially, combinations of keys with -/M7 "characteristics" to guess "names" of chords.
 * @author Jon Latane
 *
 */
public class TwelthKeyboardFragment extends Fragment {
	private static final int[] _slots = {R.id.bestChord, R.id.second, R.id.third, R.id.fourth, R.id.fifth, R.id.sixth, R.id.seventh, R.id.eighth, R.id.ninth, R.id.tenth, R.id.eleventh};
	private static final String TAG = "TwelthKeyboardFragment";
	private Integer _initialRhythmAreaWidth;
	private KeyboardScroller _keyboardScroller;
	private KeyboardIOHandler _myKbdIO;
	private HorizontalScrollView _chordScroller;
	private Key _keyToNameFrom = Key.CMajor;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        final View result = inflater.inflate(R.layout.twelthkeyboard, container, false);
        
        //Set up basic stuff from layout
        _initialRhythmAreaWidth = result.findViewById(R.id.rhythmButtonArea).getLayoutParams().width;
        _keyboardScroller = (KeyboardScroller)result.findViewById(R.id.kbScroller);
		_myKbdIO = new KeyboardIOHandler(this, result);
		_myKbdIO.harmonicModeOn();
		_chordScroller = (HorizontalScrollView)result.findViewById(R.id.chordScroller);
        
		//Set up the keyboardScroller itself
		_keyboardScroller = (KeyboardScroller)result.findViewById(R.id.kbScroller);
		_keyboardScroller.setKeyboardIOHander(_myKbdIO);
		
        return result;
    }
	
	// Keep track of when chord updates are started so only the most recent operation will update the views
	private static long mostRecentUCDInitializationTime;
	private class UpdateChordDisplay extends AsyncTask<Chord, Integer, List<String>> {
		TreeMap<Integer,List<String>> data;
		private long myInitializationTime;
		
		
		public UpdateChordDisplay() {
			myInitializationTime = System.currentTimeMillis();
			mostRecentUCDInitializationTime = myInitializationTime;
		}
		
		@Override
		protected List<String> doInBackground(Chord... c) {
			data = Key.getRootLikelihoodsAndNames(Key.CChromatic, c[0], _keyToNameFrom);
			List<String> result = new LinkedList<String>();
			for(Map.Entry<Integer,List<String>> e : data.descendingMap().entrySet() ) {
				for( String s : e.getValue() ) {
					result.add(s);
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<String> values) {
			int idx = 0;
			if(mostRecentUCDInitializationTime == myInitializationTime) {
				for( String s : values ) {
					if( idx >= _slots.length) break;
					
					TextView v = (TextView)getView().findViewById(_slots[idx++]);
					v.setText(s);
				}
				_chordScroller.scrollTo(0,0);
			}
		}


	 }
	public void updateChordDisplay() {
		new UpdateChordDisplay().execute(_myKbdIO.getChord());
	}
	
	private class WidthEvaluator extends IntEvaluator {

	    private View v;
	    public WidthEvaluator(View v) {
	        this.v = v;
	    }

	    @SuppressLint("NewApi")
		@Override
	    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
	        int num = (Integer)super.evaluate(fraction, startValue, endValue);
	        ViewGroup.LayoutParams params = v.getLayoutParams();
	        params.width = num;
	        v.setLayoutParams(params);
	        return num;
	    }
	}
	

	public boolean rhythmicModeIsEnabled() {
		LinearLayout l = (LinearLayout)getView().findViewById(R.id.rhythmButtonArea);
		boolean result = l.getWidth() != 0;
		Log.i(TAG,"rhythmicModeIsEnabled:"+result);
		return result;
	}
	
	private void enableRhythmicMode(View v) {
		final LinearLayout l = (LinearLayout)getView().findViewById(R.id.rhythmButtonArea);
		ValueAnimator.ofObject(new WidthEvaluator(l), l.getWidth(), _initialRhythmAreaWidth).start();
	}
	
	public void enableRhythmicMode() {
		//final LinearLayout l = (LinearLayout)getView().findViewById(R.id.rhythmButtonArea);
		//ValueAnimator.ofObject(new WidthEvaluator(l), l.getWidth(), _initialRhythmAreaWidth).start();
		enableRhythmicMode(getView());
	}
	private void disableRhythmicMode(View v) {
		final LinearLayout l = (LinearLayout)v.findViewById(R.id.rhythmButtonArea);
		ValueAnimator.ofObject(new WidthEvaluator(l), l.getWidth(), 0).start();
	}
	public void disableRhythmicMode() {
		//final LinearLayout l = (LinearLayout)getView().findViewById(R.id.rhythmButtonArea);
		//ValueAnimator.ofObject(new WidthEvaluator(l), l.getWidth(), 0).start();
		disableRhythmicMode(getView());
	}
	public boolean toggleRhythmicMode() {
		if(rhythmicModeIsEnabled())
			disableRhythmicMode();
		else
			enableRhythmicMode();
		return rhythmicModeIsEnabled();
	}
	
	private void enableHarmonicMode(View r) {
		final View v = r.findViewById(R.id.chordScroller);
		v.animate().translationY(0);
		_myKbdIO.harmonicModeOn();
	}
	public void enableHarmonicMode() {
		//final View v = getView().findViewById(R.id.chordScroller);
		//v.animate().translationY(0);
		//_myKbdIO.harmonicModeOn();
		enableHarmonicMode(getView());
	}
	
	private void disableHarmonicMode(View r) {
		final View v = r.findViewById(R.id.chordScroller);
		v.animate().translationY(v.getHeight());
		_myKbdIO.harmonicModeOff();
	}
	public void disableHarmonicMode() {
		//final View v = getView().findViewById(R.id.chordScroller);
		//v.animate().translationY(v.getHeight());
		//_myKbdIO.harmonicModeOff();
		disableHarmonicMode(getView());
	}
	public boolean toggleHarmonicMode() {
		//if(getView().findViewById(R.id.chordScroller).getHeight() != 0)
		if(_myKbdIO.isHarmonic())
			disableHarmonicMode();
		else
			enableHarmonicMode();
		return _myKbdIO.isHarmonic();
	}
	
	public boolean keyboardIsEnabled() {
		return getView().getTranslationY() == 0;
	}
	public void hideKeyboardFragment() {
		final View v = getView();
		v.animate().translationY(v.getHeight());
	}
	public void showKeyboardFragment() {
		final View v = getView();
		v.animate().translationY(0);
	}
	public void toggleKeyboardFragment() {
		if(!keyboardIsEnabled())
			showKeyboardFragment();
		else
			hideKeyboardFragment();
	}
	
	public void highlightChord(Chord c) {
		_myKbdIO.setHarmonicChord(c);
	}
	public void clearChordHighlights() {
		_myKbdIO.setHarmonicChord(null);
	}

	public Key getKeyToNameFrom() {
		return _keyToNameFrom;
	}

	public void setKeyToNameFrom(Key _keyToNameFrom) {
		this._keyToNameFrom = _keyToNameFrom;
	}
}

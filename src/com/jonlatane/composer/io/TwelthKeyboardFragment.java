package com.jonlatane.composer.io;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jonlatane.composer.R;
import com.jonlatane.composer.R.layout;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.Key;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TwelthKeyboardFragment extends Fragment {
	private static final int[] _slots = {R.id.bestChord, R.id.second, R.id.third, R.id.fourth, R.id.fifth, R.id.sixth, R.id.seventh, R.id.eighth, R.id.ninth, R.id.tenth, R.id.eleventh};
	private static final String TAG = "TwelthKeyboardFragment";
	private Integer _initialRhythmAreaWidth;
	private KeyboardScroller _keyboardScroller;
	private KeyboardIOHandler _myKbdIO;
	private HorizontalScrollView _chordScroller;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.twelthkeyboard, container, false);
        
        //do stuff?
        _initialRhythmAreaWidth = result.findViewById(R.id.rhythmButtonArea).getLayoutParams().width;
        _keyboardScroller = (KeyboardScroller)result.findViewById(R.id.kbScroller);
		_myKbdIO = new KeyboardIOHandler(this, result);
		_myKbdIO.harmonicModeOn();
		_chordScroller = (HorizontalScrollView)result.findViewById(R.id.chordScroller);
        

		_keyboardScroller = (KeyboardScroller)result.findViewById(R.id.kbScroller);
		_keyboardScroller.setKeyboardIOHander(_myKbdIO);
		
        return result;
    }
	//The system calls this when it's time for the fragment to draw its user interface for the first time. To draw a UI for your fragment, you must return a View from this method that is the root of your fragment's layout. You can return null if the fragment does not provide a UI.
	public void onPause() {
		super.onPause();
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
			data = Key.getRootLikelihoodsAndNamesInC(Key.CChromatic, c[0]);
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
	
	public boolean rhythmicModeIsEnabled() {
		LinearLayout l = (LinearLayout)getView().findViewById(R.id.rhythmButtonArea);
		boolean result = l.getWidth() != 0;
		Log.i(TAG,"rhythmicModeIsEnabled:"+result);
		return result;
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
	private class HeightEvaluator extends IntEvaluator {

	    private View v;
	    public HeightEvaluator(View v) {
	        this.v = v;
	    }

	    @SuppressLint("NewApi")
		@Override
	    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
	        int num = (Integer)super.evaluate(fraction, startValue, endValue);
	        ViewGroup.LayoutParams params = v.getLayoutParams();
	        params.height = num;
	        v.setLayoutParams(params);
	        return num;
	    }
	}
	
	public void enableRhythmicMode() {
		final LinearLayout l = (LinearLayout)getView().findViewById(R.id.rhythmButtonArea);
		ValueAnimator.ofObject(new WidthEvaluator(l), l.getWidth(), _initialRhythmAreaWidth).start();
	}
	public void disableRhythmicMode() {
		final LinearLayout l = (LinearLayout)getView().findViewById(R.id.rhythmButtonArea);
		ValueAnimator.ofObject(new WidthEvaluator(l), l.getWidth(), 0).start();
	}
	public boolean toggleRhythmicMode() {
		if(rhythmicModeIsEnabled())
			disableRhythmicMode();
		else
			enableRhythmicMode();
		return rhythmicModeIsEnabled();
	}
	
	
	@SuppressLint("NewApi")
	public void enableHarmonicMode() {
		final View v = getView().findViewById(R.id.chordScroller);
		//ValueAnimator.ofObject(new HeightEvaluator(v), v.getHeight(), _initialChordScrollerHeight).start();
		v.animate().y(0);
		_myKbdIO.harmonicModeOn();
	}
	@SuppressLint("NewApi")
	public void disableHarmonicMode() {
		final View v = getView().findViewById(R.id.chordScroller);
		//ValueAnimator.ofObject(new HeightEvaluator(v), v.getHeight(), 0).start();
		v.animate().y(v.getHeight());
		_myKbdIO.harmonicModeOff();
	}
	public boolean toggleHarmonicMode() {
		//if(getView().findViewById(R.id.chordScroller).getHeight() != 0)
		if(_myKbdIO.isHarmonic())
			disableHarmonicMode();
		else
			enableHarmonicMode();
		return true;
	}
}

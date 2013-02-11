package com.jonlatane.composer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jonlatane.composer.input.*;
import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.harmony.*;
import com.jonlatane.composer.music.coverings.*;

import android.app.*;
import android.graphics.Canvas;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

/**
 * The ScoreEditingActivity allows interaction with a Score.  You can scroll horizontally at all times.  To make a
 * selection, use a two finger touch.  This works in conjunction with RhythmMaps to draw - it adds subdivisions to the model
 * at barlines so that ties can be effectively rendered (meaning dotted-quarter-half vs. half-dotted-quarter tie is up to the
 * renderer).  This is advantageous because adding 
 */
public class ChordDisplayActivity extends Activity
{
	private KeyboardIOHandler _myKbdIO;
	private ManagedToneGenerator _tg;
	private static final String TAG = "ChordDisplayActivity";

	private static final int[] _slots = {R.id.bestChord, R.id.second, R.id.third, R.id.fourth, R.id.fifth, R.id.sixth, R.id.seventh, R.id.eighth, R.id.ninth, R.id.tenth, R.id.eleventh};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//SurfaceView view = new SurfaceView(this);
		setContentView(R.layout.chorddisplayactivity);
		//setContentView(view);
		
		_myKbdIO = new KeyboardIOHandler(this);
		_myKbdIO.harmonicModeOn();
		KeyboardScroller scroller = (KeyboardScroller)findViewById(R.id.kbScroller);
		scroller.setKeyboardIOHander(_myKbdIO);
	}
	
	private class UpdateChordDisplay extends AsyncTask<Chord, Integer, List<String>> {
		TreeMap<Integer,List<String>> data;
		
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
			for( String s : values ) {
				if( idx >= _slots.length) break;
				
				TextView v = (TextView)findViewById(_slots[idx++]);
				v.setText(s);
			}
		}


	 }
	public void updateChordDisplay() {
		new UpdateChordDisplay().execute(_myKbdIO.getChord());
		/*Thread t = new Thread() {
			@Override
			public void run() {
				Chord c = new Chord(_myKbdIO.getPressedKeys());
				TreeMap<Integer,List<String>> data = Key.getRootLikelihoodsAndNamesInC(Key.CChromatic, c);
				int idx = 0;
				for(Map.Entry<Integer,List<String>> e : data.descendingMap().entrySet() ) {
					for( String s : e.getValue() ) {
						if( idx > 11 ) break;
						TextView v = (TextView)findViewById(_slots[idx++]);
						
						v.setText(s);
					}
				}
			}
		};
		t.start();*/
	}
}

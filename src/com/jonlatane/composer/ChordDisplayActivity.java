package com.jonlatane.composer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.devsmart.android.ui.HorizontalListView;
import com.jonlatane.composer.io.*;
import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.harmony.*;
import com.jonlatane.composer.music.coverings.*;

import android.app.*;
import android.graphics.Canvas;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;

/**
 * The ScoreEditingActivity allows interaction with a Score.  You can scroll horizontally at all times.  To make a
 * selection, use a two finger touch.  This works in conjunction with RhythmMaps to draw - it adds subdivisions to the model
 * at barlines so that ties can be effectively rendered (meaning dotted-quarter-half vs. half-dotted-quarter tie is up to the
 * renderer).  This is advantageous because adding 
 */
public class ChordDisplayActivity extends Activity
{
	private TwelthKeyboardFragment _myKeyboard;
	//private KeyboardIOHandler _myKbdIO;
	//private KeyboardScroller _keyboardScroller;
	//private HorizontalScrollView _chordScroller;
	private ManagedToneGenerator _tg;
	
	public int NUMFINGERSDOWN = 0;
	
	private static final String TAG = "ChordDisplayActivity";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.chorddisplayactivity);
		
		// Set up the keyboard
		_myKeyboard = (TwelthKeyboardFragment)getFragmentManager().findFragmentById(R.id.chordDisplayActivityKb);
		//_myKbdIO = new KeyboardIOHandler(this);
		//_myKbdIO.harmonicModeOn();
		
		//_keyboardScroller = (KeyboardScroller)findViewById(R.id.kbScroller);
		//_keyboardScroller.setKeyboardIOHander(_myKbdIO);
		
		//_chordScroller = (HorizontalScrollView)findViewById(R.id.chordDisplayScroller);
		
		//RelativeLayout root= (RelativeLayout) findViewById(R.id.chordDisplayActivity);
		
		// Set up lead sheet display
		HorizontalListView listview = (HorizontalListView) findViewById(R.id.leadSheet);  
        listview.setAdapter(_adapter);  
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.chorddisplaymenu, menu);
	    return true;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.toggleRhythmAB:
	    	_myKeyboard.toggleRhythmicMode();
	    	break;
	    case R.id.toggleChordsAB:
	    	_myKeyboard.toggleHarmonicMode();
	    	break;
	    default:
	      break;
	    }

	    return true;
	  } 
	
	private static String[] _dataObjects = new String[]{ 
		"CM7", "D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7",
		"D-7", "G7", "CM7"};
	
	private BaseAdapter _adapter = new BaseAdapter() {  
		  
        @Override  
        public int getCount() {  
            return _dataObjects.length;  
        }  
  
        @Override  
        public Object getItem(int position) {  
            return null;  
        }  
  
        @Override  
        public long getItemId(int position) {  
            return 0;  
        }  
  
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
            View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, null);  
            TextView title = (TextView) retval.findViewById(R.id.leadSheetItemChord);  
            title.setText(_dataObjects[position]);  
              
            return retval;  
        }  
          
    };  

	
	@Override
	public void onPause()
	{
		super.onPause();
	    ManagedToneGenerator.Cache.releaseAll();
	}

	@Override
	public void onResume()
	{
	    super.onResume();
	}
	
	
}

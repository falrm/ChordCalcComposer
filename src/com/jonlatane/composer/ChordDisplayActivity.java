package com.jonlatane.composer;

import java.util.TreeMap;

import com.jonlatane.composer.input.KeyboardIOHandler;
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
	
	private static float XOFFSET = 20;
	private static final String TAG = "ChordDisplayActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//SurfaceView view = new SurfaceView(this);
		setContentView(R.layout.chorddisplayactivity);
		//setContentView(view);
		
		_myKbdIO = new KeyboardIOHandler(this);
		_myKbdIO.harmonicModeOn();
	}
}

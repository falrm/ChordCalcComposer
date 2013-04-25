package com.jonlatane.composer;

import java.util.Arrays;

import com.jonlatane.composer.io.*;
import com.jonlatane.composer.music.harmony.*;
import android.app.*;
import android.content.Context;
import android.media.AudioManager;
import android.os.*;
import android.util.*;
import android.view.*;

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
	
	public int NUMFINGERSDOWN = 0;
	
	private static final String TAG = "ChordDisplayActivity";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Debugging stuff - to be moved elsewhere
		Log.d(TAG, Chord.getChordByName("C"+Chord.diminished+"7add11#13").toString());
		Chord cA1 = Chord.getChordByName("Ab7");
		Chord cA2 = Chord.getChordByName("G7");
		Chord cA3 = Chord.getChordByName("C");
		
		cA3.NOTENAMES = new String[] {"C", "E", "G"};
		VoiceLeading.fillEnharmonics(cA2, cA3);
		VoiceLeading.fillEnharmonics(cA1, cA2);
		
		Log.d(TAG, Arrays.toString(cA1.NOTENAMES) + cA1.toString());
		Log.d(TAG, Arrays.toString(cA2.NOTENAMES) + cA2.toString());
		Log.d(TAG, Arrays.toString(cA3.NOTENAMES) + cA3.toString());
		
		Chord cB1 = Chord.getChordByName("Ab7");
		Chord cB2 = Chord.getChordByName("C");
		Chord cB3 = Chord.getChordByName("G7");
		Chord cB4 = Chord.getChordByName("C");
		
		cB4.NOTENAMES = new String[] {"C", "E", "G"};
		VoiceLeading.fillEnharmonics(cB3, cB4);
		VoiceLeading.fillEnharmonics(cB2, cB3);
		VoiceLeading.fillEnharmonics(cB1, cB2);
		
		Log.d(TAG, Arrays.toString(cB1.NOTENAMES) + cB1.toString());
		Log.d(TAG, Arrays.toString(cB2.NOTENAMES) + cB2.toString());
		Log.d(TAG, Arrays.toString(cB3.NOTENAMES) + cB3.toString());
		Log.d(TAG, Arrays.toString(cB4.NOTENAMES) + cB4.toString());
		
		// Load layout
		setContentView(R.layout.chorddisplayactivity);
		
		// Set up the keyboard
		_myKeyboard = (TwelthKeyboardFragment)getFragmentManager().findFragmentById(R.id.chordDisplayActivityKb);
		
		// Set up lead sheet display
		//HorizontalListView listview = (HorizontalListView) findViewById(R.id.leadSheet);  
        //listview.setAdapter(_adapter);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	    switch (keyCode) {
	    case KeyEvent.KEYCODE_VOLUME_UP:
	        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
	                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
	        return true;
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
	                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
	        return true;
	    default:
	    	return super.onKeyDown(keyCode, event);
	    }
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		_myKeyboard.onSaveInstanceState(outState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//_myKeyboard.onRestoreInstanceState(savedInstanceState);
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
	    case R.id.toggleKeyboardAB:
	    	_myKeyboard.toggleKeyboardFragment();
	    	break;
	    default:
	      break;
	    }

	    return true;
	  } 

	
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

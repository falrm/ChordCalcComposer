package com.jonlatane.composer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.jonlatane.composer.io.ToneControllerFragment;
import com.jonlatane.composer.io.TwelthKeyboardFragment;
import com.jonlatane.composer.audio.AudioTrackCache;
import com.jonlatane.composer.audio.generator.HarmonicOvertoneSeriesGenerator;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.Enharmonics;
import com.jonlatane.composer.music.harmony.Key;
import com.jonlatane.composer.music.harmony.PitchSet;

import java.util.Arrays;

/**
 * The ScoreEditingActivity allows interaction with a Score.  You can scroll horizontally at all times.  To make a
 * selection, use a two finger touch.  This works in conjunction with RhythmMaps to draw - it adds subdivisions to the model
 * at barlines so that ties can be effectively rendered (meaning dotted-quarter-half vs. half-dotted-quarter tie is up to the
 * renderer).  This is advantageous because adding 
 */
public class BaseKeyboardActivity extends Activity
{
	protected TwelthKeyboardFragment keyboard;
	protected ToneControllerFragment toneController;
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
		PitchSet ps = PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"});
		Enharmonics.fillEnharmonics(ps, Key.CMajor);
		String test1 = "";
		for(int nameInd = 0; nameInd < ps.NOTENAMES.length; nameInd++) {
			test1 += ps.NOTENAMES[nameInd] + " ";
		}
		Log.i(TAG, "fillEnharmonics test:" + test1);
		
		Log.d(TAG, Chord.getChordByName("C"+Chord.diminished+"7add11#13").toString());
		Chord cA1 = Chord.getChordByName("Ab7");
		Chord cA2 = Chord.getChordByName("G7");
		Chord cA3 = Chord.getChordByName("C");
		
		cA3.NOTENAMES = new String[] {"C", "E", "G"};
		Enharmonics.fillEnharmonics(cA2, cA3);
		Enharmonics.fillEnharmonics(cA1, cA2);
		
		Log.d(TAG, Arrays.toString(cA1.NOTENAMES) + cA1.toString());
		Log.d(TAG, Arrays.toString(cA2.NOTENAMES) + cA2.toString());
		Log.d(TAG, Arrays.toString(cA3.NOTENAMES) + cA3.toString());
		
		Chord cB1 = Chord.getChordByName("Ab7");
		Chord cB2 = Chord.getChordByName("C");
		Chord cB3 = Chord.getChordByName("G7");
		Chord cB4 = Chord.getChordByName("C");
		
		cB4.NOTENAMES = new String[] {"C", "E", "G"};
		Enharmonics.fillEnharmonics(cB3, cB4);
		Enharmonics.fillEnharmonics(cB2, cB3);
		Enharmonics.fillEnharmonics(cB1, cB2);
		
		Log.d(TAG, Arrays.toString(cB1.NOTENAMES) + cB1.toString());
		Log.d(TAG, Arrays.toString(cB2.NOTENAMES) + cB2.toString());
		Log.d(TAG, Arrays.toString(cB3.NOTENAMES) + cB3.toString());
		Log.d(TAG, Arrays.toString(cB4.NOTENAMES) + cB4.toString());
		
		for (int i = -25; i < 25; i++)
			Log.i(TAG, "Octavetest: " + i + " > " + Chord.TWELVETONE.octave(i));
		
		// Load layout
		setContentView(R.layout.activity_interactive);
		
		// Set up the keyboard
		keyboard = (TwelthKeyboardFragment)getFragmentManager().findFragmentById(R.id.kbFragment);
		keyboard.disableRhythmicMode();
		
		// Attach the tone controller to the keyboard
        toneController = (ToneControllerFragment) getFragmentManager().findFragmentById(R.id.toneControllerFragment);
        toneController.attachToneGenerator((HarmonicOvertoneSeriesGenerator) keyboard.getTrackGenerator());
        keyboard.linkView(toneController.getView());

		if(getClass().equals(BaseKeyboardActivity.class)) {
			toneController.getView().setVisibility(View.GONE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		// Set up lead sheet display
		//HorizontalListView listview = (HorizontalListView) findViewById(R.id.leadSheet);
        //listview.setAdapter(_adapter);
        // Test Code
//        SystemRecyclerView srv = (SystemRecyclerView)findViewById(R.id.systemRecyclerView);
//        SystemRecyclerView srv2 = (SystemRecyclerView)findViewById(R.id.systemRecyclerView2);
//        Score subject = Score.twinkleTwinkle();
//        Score.testScore(subject);
//        srv.setAdapter(new ScoreDataAdapter(subject));
//        srv2.setAdapter(new ScoreDataAdapter(subject));
//        srv.setViewBelow(srv2);
//        srv2.setViewAbove(srv);
	}
	
	// Handles all Volume key presses as media volume control
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
	public void onPause() {
		super.onPause();
		AudioTrackCache.releaseAll();
	}

	@Override
	public void onResume() {
        super.onResume();
	}
	
	
}

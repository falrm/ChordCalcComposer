package com.jonlatane.composer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.jonlatane.composer.audio.AudioTrackCache;
import com.jonlatane.composer.audio.generator.HarmonicOvertoneSeriesGenerator;
import com.jonlatane.composer.io.ToneControllerFragment;
import com.jonlatane.composer.io.TwelthKeyboardFragment;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.music.harmony.Enharmonics;
import com.jonlatane.composer.music.harmony.Key;
import com.jonlatane.composer.music.harmony.PitchSet;
import com.readystatesoftware.systembartint.SystemBarTintManager;

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
		themeWindow();
		
		//Debugging stuff - to be moved elsewhere
		PitchSet ps = PitchSet.toPitchSet(new String[]{"C3", "E3", "G3"});
		Enharmonics.fillEnharmonics(ps, Key.CMajor);
		String test1 = "";
		for(int nameInd = 0; nameInd < ps.noteNameCache.length; nameInd++) {
			test1 += ps.noteNameCache[nameInd] + " ";
		}
		Log.i(TAG, "fillEnharmonics test:" + test1);
		
		Log.d(TAG, Chord.getChordByName("C"+Chord.diminished+"7add11#13").toString());
		Chord cA1 = Chord.getChordByName("Ab7");
		Chord cA2 = Chord.getChordByName("G7");
		Chord cA3 = Chord.getChordByName("C");
		
		cA3.noteNameCache = new String[] {"C", "E", "G"};
		Enharmonics.fillEnharmonics(cA2, cA3);
		Enharmonics.fillEnharmonics(cA1, cA2);
		
		Log.d(TAG, Arrays.toString(cA1.noteNameCache) + cA1.toString());
		Log.d(TAG, Arrays.toString(cA2.noteNameCache) + cA2.toString());
		Log.d(TAG, Arrays.toString(cA3.noteNameCache) + cA3.toString());
		
		Chord cB1 = Chord.getChordByName("Ab7");
		Chord cB2 = Chord.getChordByName("C");
		Chord cB3 = Chord.getChordByName("G7");
		Chord cB4 = Chord.getChordByName("C");
		
		cB4.noteNameCache = new String[] {"C", "E", "G"};
		Enharmonics.fillEnharmonics(cB3, cB4);
		Enharmonics.fillEnharmonics(cB2, cB3);
		Enharmonics.fillEnharmonics(cB1, cB2);
		
		Log.d(TAG, Arrays.toString(cB1.noteNameCache) + cB1.toString());
		Log.d(TAG, Arrays.toString(cB2.noteNameCache) + cB2.toString());
		Log.d(TAG, Arrays.toString(cB3.noteNameCache) + cB3.toString());
		Log.d(TAG, Arrays.toString(cB4.noteNameCache) + cB4.toString());
		
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
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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

	protected void themeWindow() {
		if(Build.VERSION.SDK_INT >= 21) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(getResources().getColor(R.color.toolbar));
			window.setNavigationBarColor(getResources().getColor(R.color.toolbar));

			ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(
					getResources().getString(R.string.app_name),
					BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
					getResources().getColor(R.color.toolbar));
			setTaskDescription(taskDescription);
			//KitKat
		} else if(Build.VERSION.SDK_INT >= 19) {
			Window window = getWindow();
			window.setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setTintColor(getResources().getColor(R.color.toolbar));
		}
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

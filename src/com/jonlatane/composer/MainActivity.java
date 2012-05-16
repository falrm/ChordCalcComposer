package com.jonlatane.composer;

import android.app.*;
import android.content.*;
import android.graphics.*;
import java.util.Random;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.util.Log;

public class MainActivity extends Activity
{
	SheetMusicSurfaceView mySurfaceView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		try {
			super.onCreate(savedInstanceState);
			
			// Load resources into the model.
			Util.loadClefResources(getResources());
			
			// Set up the music viewing screen
			setContentView(R.layout.musicviewingscreen);
			final View v = getLayoutInflater().inflate(R.layout.musicviewingscreen,null);
			
			// Store the SheetMusicSurfaceView for threading
			mySurfaceView = (SheetMusicSurfaceView)v.findViewById(R.id.sheetmusic_surfaceview);
		} catch(Throwable e) {
			Log.e("Composer",Log.getStackTraceString(e));
		}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		mySurfaceView.onResumeMySurfaceView();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mySurfaceView.onPauseMySurfaceView();
	}
	
	
}

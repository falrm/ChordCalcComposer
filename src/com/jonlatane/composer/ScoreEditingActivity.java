package com.jonlatane.composer;

import java.util.TreeMap;

import com.jonlatane.composer.io.KeyboardIOHandler;
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
public class ScoreEditingActivity extends Activity implements SurfaceHolder.Callback
{
	private Score _score;
	private KeyboardIOHandler _myKbdIO;
	
	private static float XOFFSET = 20;
	private static final String TAG = "ScoreEditingActivity";

	/*
	 * This is the real unit of work in this activity.  A System is just what a system is on the page.  It has a starting point
	 * and points to the Score through its parent Activity.  The Activity manages its top based on the needed system heights.
	 * It also manages horizontal scrolling through the System's animationOffset and startingPoint variables.  By making
	 * animationOffset approach 1 or -1, we move everything right or left respectively.  Once they pass -1
	 */
	private class System {
		// Drawing-related stuff
		Rational startingPoint;
		float animationOffset;
		int systemTop;
		int systemNeededHeight;
		
		// Data
		private PhysicalStaff[] staves;
		private TreeMap<Integer,Rational> _touchCache;
		
		class PhysicalStaff {
			Staff _staff;
			
			// Drawing-related stuff
			int NEEDED_UPPER_SPACE;
			int YPOS;
			int NEEDED_LOWER_SPACE;
			
			void draw(Canvas c) {
				//c.drawLine(startX, startY, stopX, stopY, paint)
			}
		}
		
		void draw(Canvas c) {
			if(systemTop > c.getHeight())
				return;
			c.translate(XOFFSET, systemTop);
			
			// Do drawing
			
			c.translate(-XOFFSET, -systemTop);
		}
		
		/*
		 * Use the magic of trees to quickly find the thing that just got touched.
		 */
		/*Pair<Staff,Rational> whatsAt(int x, int y) {
			Staff myStaff = null;
			for(PhysicalStaff s : staves) {
				if(s.YPOS - s.NEEDED_UPPER_SPACE < y ) {
					myStaff = s._staff;
				} else {
					break;
				}
			}
		}*/
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//SurfaceView view = new SurfaceView(this);
		setContentView(R.layout.scoreeditingactivity);
		//setContentView(view);
		((SurfaceView)findViewById(R.id.editorSurfaceView)).getHolder().addCallback(this);
		
		//_myKbdIO = new KeyboardIOHandler(this);
		_myKbdIO.harmonicModeOn();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		tryDrawing(holder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int frmt, int w, int h) {
		tryDrawing(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private void tryDrawing(SurfaceHolder holder) {
		Log.i(TAG, "Trying to draw...");

		Canvas canvas = holder.lockCanvas();
		if (canvas == null) {
			Log.e(TAG, "Cannot draw onto the canvas as it's null");
		} else {
			drawMyStuff(canvas);
			holder.unlockCanvasAndPost(canvas);
		}
	}

	private void drawMyStuff(final Canvas canvas) {
		Log.i(TAG, "Drawing...");
		canvas.drawRGB(128, 255, 255);
	}

}

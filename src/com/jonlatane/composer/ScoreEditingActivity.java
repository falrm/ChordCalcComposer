package com.jonlatane.composer;

import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.harmony.*;
import com.jonlatane.composer.music.coverings.*;

import android.app.*;
import android.graphics.Canvas;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class ScoreEditingActivity extends Activity implements SurfaceHolder.Callback
{
	private Score _score;
	private static final String TAG = "ScoreEditingActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SurfaceView view = new SurfaceView(this);
		setContentView(view);
		view.getHolder().addCallback(this);
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
		canvas.drawRGB(255, 128, 128);
	}

}

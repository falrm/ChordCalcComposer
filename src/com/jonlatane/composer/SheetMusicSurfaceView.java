package com.jonlatane.composer;

import android.view.*;
import android.graphics.*;
import android.content.*;
import android.util.AttributeSet;
import com.jonlatane.composer.graphics.*;
import com.jonlatane.composer.music.*;
import com.jonlatane.composer.input.*;
import android.util.Log;

class SheetMusicSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
	Thread thread = null;
	Renderer renderer;
	InputEngine inputEngine;
	Canvas _c;
	int x = 0; int y = 0;
	volatile boolean running = false;
	
	public SheetMusicSurfaceView(Context context) {
		super(context);
		init();
	}
	
	public SheetMusicSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public SheetMusicSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
		init();
	}
	
	private void init() {
		renderer = new Renderer(WorkTest.odeToJoy(), Typeface.createFromAsset(getContext().getAssets(),"fonts/NoteHedz170.ttf"));
		inputEngine = new InputEngine(renderer);
		setOnTouchListener(inputEngine);
		getHolder().addCallback(this);
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("Composer","SurfaceCreated");
		onResumeMySurfaceView();
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("Composer","SurfaceDestroyed");
		onPauseMySurfaceView();
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.i("Composer","SurfaceChanged");
		onPauseMySurfaceView();
		onResumeMySurfaceView();
	}
	
	public void onResumeMySurfaceView() {
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void onPauseMySurfaceView() {
		boolean retry = true;
		running = false;
		while(retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		while(running) {
			try {
			if(getHolder().getSurface().isValid()) {
				Canvas canvas = getHolder().lockCanvas();
				if(canvas!=null) {
					renderer.render(canvas);
					getHolder().unlockCanvasAndPost(canvas);
				}
			}
			
			} catch(Throwable e) {
				Log.e("Composer",Log.getStackTraceString(e));
			}
		}
	}
}


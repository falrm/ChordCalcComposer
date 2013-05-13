/**
 * 
 */
package com.jonlatane.composer.scoredisplay;

import com.jonlatane.composer.music.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class is a SurfaceView representing a system in a musical score.  It scrolls horizontally
 * and adjusts its height based on its contents (i.e., hides empty staves and expands for high/low notes).
 * 
 * SystemSurfaceViews with their scrolling chained together may be displayed in a LinearLayout to represent
 * a full score with multiple lines.
 * 
 * @author Jon
 *
 */
public class SheetMusicSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private static final int STAFFHEIGHT = 80;
	private static final int MARGIN = 20;
	private static final Paint NORMALPAINT = new Paint();
	private static final Paint SELECTEDPAINT = new Paint();
	private static float SCALINGFACTOR = 1;
	
	static {
		NORMALPAINT.setARGB(255, 0, 0, 0);
		NORMALPAINT.setStyle(Paint.Style.STROKE);
		NORMALPAINT.setStrokeWidth(2);
		SELECTEDPAINT.setARGB(255, 0, 0, 255);
		SELECTEDPAINT.setStyle(Paint.Style.STROKE);
		SELECTEDPAINT.setStrokeWidth(2);
	}
	
	public static enum StaffNames { Full, Partial, None }
	
	/**
	 * @param context
	 */
	public SheetMusicSurfaceView(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public SheetMusicSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SheetMusicSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		getHolder().addCallback(this);
	}
	
	@Override
    protected void onDraw(Canvas canvas){
        //canvas.drawRect(new Rect(10,10,200,200), rectanglePaint);
		canvas.scale(SCALINGFACTOR, SCALINGFACTOR);
		canvas.drawColor(Color.WHITE);
        Log.w(this.getClass().getName(), "onDraw Called");
    }
	
	private void drawStaff(Canvas c, int k, int startX) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		setWillNotDraw(false);
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}
}

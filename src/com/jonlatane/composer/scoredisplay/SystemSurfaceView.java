/**
 * 
 */
package com.jonlatane.composer.scoredisplay;

import com.jonlatane.composer.music.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
public class SystemSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private static final int STAFFHEIGHT = 80;
	private static final int MARGIN = 20;
	private static boolean SHOWCHORDS = true;
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
	
	private SuperScore _score = null;
	
	private Rational _start = Rational.ZERO;
	private float _offset = 0;
	
	
	private RhythmMap<Float> _renderingWidths = new RhythmMap<Float>();
	
	// All these arrays are to be kept in sync with the staves in the Score.  They define the y-axis and divisions thereof and
	// let us render the staves once and draw everything else on top.  These numbers may adapt
	private Integer[] _staffTops;
	private Integer[] _staffCenters;
	private Paint[] _staffPaints;
	
	public static enum StaffNames { Full, Partial, None }
	
	/**
	 * @param context
	 */
	public SystemSurfaceView(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public SystemSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SystemSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		getHolder().addCallback(this);
	}
	
	public void initializeToScore(SuperScore s) {
		_score = s;
		_staffTops = new Integer[s.getStaves().length];
		_staffCenters = new Integer[s.getStaves().length];
		_staffPaints = new Paint[s.getStaves().length];
	}
	
	@Override
    protected void onDraw(Canvas canvas){
        //canvas.drawRect(new Rect(10,10,200,200), rectanglePaint);
		canvas.scale(SCALINGFACTOR, SCALINGFACTOR);
        Log.w(this.getClass().getName(), "On Draw Called");
    }
	
	private void drawStaff(Canvas c, int k, int startX) {
		if(_staffCenters[k] == 0)
			return;
		
		_staffPaints[k] = new Paint(NORMALPAINT);
		if(_staffCenters[k] < STAFFHEIGHT/2) {
			_staffPaints[k].setAlpha(Math.min(255,_staffCenters[k]*2*255/STAFFHEIGHT));
		}
		
		int centerCoord = _staffTops[k] + _staffCenters[k];
		int endX = c.getWidth() - MARGIN;
		c.drawLines(new float[]{ startX, centerCoord, endX, centerCoord,
						startX, centerCoord + STAFFHEIGHT/4, endX, centerCoord + STAFFHEIGHT/4,
						startX, centerCoord - STAFFHEIGHT/4, endX, centerCoord - STAFFHEIGHT/4,
						startX, centerCoord + STAFFHEIGHT/2, endX, centerCoord + STAFFHEIGHT/2,
						startX, centerCoord - STAFFHEIGHT/2, endX, centerCoord - STAFFHEIGHT/2},
				0, 5, _staffPaints[k]);
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

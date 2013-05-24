/**
 * 
 */
package com.jonlatane.composer.scoredisplay;

import java.util.HashMap;
import java.util.Map;

import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.harmony.Key;
import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.music.harmony.Chord;
import com.jonlatane.composer.scoredisplay.StaffSpec.VerticalStaffSpec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

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
public class ScoreDrawingSurface extends ViewGroup implements SurfaceHolder.Callback {
	private static final String TAG = "ScoreDrawer";
	

	private static final Paint NORMALPAINT = new Paint();
	private static final Paint SELECTEDPAINT = new Paint();
	
	private static final Map<Key,Integer> KEY_ACCIDENTALS = new HashMap<Key,Integer>();
	static {
		KEY_ACCIDENTALS.put(Key.CMajor, 0);
		KEY_ACCIDENTALS.put(Key.GMajor, 1);
		KEY_ACCIDENTALS.put(Key.DMajor, 2);
		KEY_ACCIDENTALS.put(Key.AMajor, 3);
		KEY_ACCIDENTALS.put(Key.EMajor, 4);
		KEY_ACCIDENTALS.put(Key.BMajor, 5);
		KEY_ACCIDENTALS.put(Key.FsMajor, 6);
		
		KEY_ACCIDENTALS.put(Key.GbMajor, -6);
		KEY_ACCIDENTALS.put(Key.DbMajor, -5);
		KEY_ACCIDENTALS.put(Key.AbMajor, -4);
		KEY_ACCIDENTALS.put(Key.EbMajor, -3);
		KEY_ACCIDENTALS.put(Key.BbMajor, -2);
		KEY_ACCIDENTALS.put(Key.FMajor, -1);
		

		KEY_ACCIDENTALS.put(Key.AMinor, 0);
		KEY_ACCIDENTALS.put(Key.EMinor, 1);
		KEY_ACCIDENTALS.put(Key.BMinor, 2);
		KEY_ACCIDENTALS.put(Key.FsMinor, 3);
		KEY_ACCIDENTALS.put(Key.CsMinor, 4);
		KEY_ACCIDENTALS.put(Key.GsMinor, 5);
		
		KEY_ACCIDENTALS.put(Key.EbMinor, -6);
		KEY_ACCIDENTALS.put(Key.BbMinor, -5);
		KEY_ACCIDENTALS.put(Key.FMinor, -4);
		KEY_ACCIDENTALS.put(Key.CMinor, -3);
		KEY_ACCIDENTALS.put(Key.GMinor, -2);
		KEY_ACCIDENTALS.put(Key.DMinor, -1);
	}
	
	private final ScoreLayout _parent;
	final SurfaceView _surface;
	private final SurfaceHolder _holder;
	
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
	 * A SystemHeader defines where the ScoreDrawingSurface will draw the staves on
	 * its canvas and how much space is needed for clefs, key signatures, and time signatures.
	 * 
	 * It may be transitioned towards a new
	 * 
	 * @author Jon Latane
	 *
	 */
	class SystemHeader extends LinearLayout {
		private double _transitionedAmount = 0d;
		private ScoreDelta _scoreDelta = null;
		private ScoreDelta _transScoreDelta = null;
		
		private int _layoutX, _layoutY;
		
		private class StaffHeader extends LinearLayout {
			private View _clefArea, _keySigArea, _timeSigArea;
			private StaffSpec.VerticalStaffSpec _staffSpec = null;
			private StaffSpec.VerticalStaffSpec _transStaffSpec = null;
			
			public StaffHeader(Context context) {
				super(context);
				setOrientation(HORIZONTAL);
				_clefArea = new View(context) {
					@Override 
					public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
						setMeasuredDimension((int) (StaffSpec.CLEF_WIDTH * _parent._scalingFactor), 50);
					}
				};
			}
			@Override
			public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				_clefArea.measure(widthMeasureSpec,heightMeasureSpec);
				_keySigArea.measure(widthMeasureSpec,heightMeasureSpec);
				_timeSigArea.measure(widthMeasureSpec,heightMeasureSpec);
				int width = _clefArea.getMeasuredWidth() + _keySigArea.getMeasuredWidth() + _timeSigArea.getMeasuredWidth();
			}
		}
		public SystemHeader(Context context) {
			super(context);
			setOrientation(VERTICAL);
		}
		
		/**
		 * To be used for animation.  When 0 <= f < 1, this SystemHeader's
		 * width (and what is
		 * @param f
		 * @param d
		 */
		public void transitionTo(double d, ScoreDelta delta) {
			_transitionedAmount = d;
		}
	}
	
	
	public ScoreDrawingSurface(Context context, ScoreLayout parent) {
		super(context);
		_parent = parent;
		_surface = new SurfaceView(getContext());
		_holder = _surface.getHolder();
		_holder.addCallback(this);
		addView(_surface,0);
	}
	
	public int systemHeaderWidth(ScoreDelta scoreD) {
		int result = StaffSpec.CLEF_WIDTH + StaffSpec.TIMESIGNATURE_WIDTH; 
		int maxNumAccidentals = 0;
		for(StaffDelta staffD : scoreD.STAVES) {
			Key k = staffD.ESTABLISHED.KEY;
		}
		return result;
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

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		_surface.measure(widthMeasureSpec,heightMeasureSpec);
		
		for(int i = 0; i < getChildCount(); i++) {
			
		}

        setMeasuredDimension(resolveSize(Integer.MAX_VALUE, widthMeasureSpec),
                resolveSize(Integer.MAX_VALUE, heightMeasureSpec));
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		_surface.layout(l, t, r, b);
	}
	
	@Override 
    protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
        Log.w(TAG, "onDraw Called in ViewGroup");
		Canvas c = _holder.lockCanvas();
			if(c != null) {
			c.drawColor(Color.WHITE);
			_holder.unlockCanvasAndPost(c);
		}
    }
}

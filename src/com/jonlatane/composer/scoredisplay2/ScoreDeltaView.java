package com.jonlatane.composer.scoredisplay2;

import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.View;

public class ScoreDeltaView extends ViewGroup {
	private final static Paint _black = new Paint();
	private final static Paint _blue = new Paint();
	private final static Paint _red = new Paint();
	private final static Paint _orange = new Paint();
	static{
		_black.setARGB(255, 0, 0, 0);
		_red.setARGB(255, 255, 0, 0);
		_blue.setARGB(255, 0, 0, 255);
		_orange.setARGB(255, 255, 140, 0);
	}
	
	final ScoreLayout _parent;
	private ScoreDelta _scoreDelta;
	
	public ScoreDeltaView(Context context, ScoreLayout parent) {
		super(context);
		_parent = parent;
	}
	
	public void setScoreDelta(ScoreDelta d) {
		_scoreDelta = d;
		for(int i = 0; i < d.STAVES.length; i++) {
			StaffDeltaView v = (StaffDeltaView) getChildAt(i);
			if( v == null ) {
				v = new StaffDeltaView(getContext());
				addView(v);
			}
			v.setStaffDelta(d.STAVES[i]);
		}
	}
	
	public ScoreDelta getScoreDelta() {
		return _scoreDelta;
	}

	public class StaffDeltaView extends View {
		private StaffDelta _staffDelta;
		public int SPACE_ABOVE = 0, SPACE_BELOW = 0,
					REQ_ABOVE = 0, REQ_BELOW = 0,
					WIDTH = 0, REQ_WIDTH = 0;
		
		public StaffDeltaView(Context context) {
			super(context);
		}
		
		public void setStaffDelta(StaffDelta d) {
			_staffDelta = d;
		}

		/**
		 * Draw to the given canvas and update REQ_ variables based on the
		 * needed dimensions discovered.
		 * 
		 * If requested layout parameters are not met, this method increments them
		 * and invalidates the layout.
		 * 
		 * @param c
		 * @param r
		 * @return an array containing the positions of the noteheads per voice
		 */
		@Override
		protected void onDraw(Canvas c) {
			super.onDraw(c);
			// Do invalidation stuff
			if(WIDTH < REQ_WIDTH) {
				WIDTH += 1;
				invalidate();
			}
			if(SPACE_ABOVE < REQ_ABOVE) {
				SPACE_ABOVE += 1;
				invalidate();
			}
			if(SPACE_BELOW < REQ_BELOW) {
				SPACE_BELOW += 1;
				invalidate();
			}
		}
		
		@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(WIDTH, SPACE_ABOVE + SPACE_BELOW);
		}
	}
	
	public void resolveWidths() {
		
	}
	
	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 0, height = 0;
		for(int i = 0; i < _scoreDelta.STAVES.length; i++) {
			StaffDeltaView sdv = (StaffDeltaView) getChildAt(i);
			sdv.measure(widthMeasureSpec, heightMeasureSpec);
			height += sdv.getMeasuredHeight();
			if(sdv.getMeasuredWidth() > width)
				width = sdv.getMeasuredWidth();
		}
		setMeasuredDimension(width, height);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int y = 0;
		for(int i = 0; i < _scoreDelta.STAVES.length; i++) {
			StaffDeltaView sdv = (StaffDeltaView) getChildAt(i);
			sdv.layout(l, t + y, r, t + y + sdv.SPACE_ABOVE + sdv.SPACE_BELOW);
			y += sdv.SPACE_ABOVE + sdv.SPACE_BELOW;
		}
	}
}

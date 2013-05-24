package com.jonlatane.composer.scoredisplay;

import java.util.Random;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.scoredisplay.StaffSpec.HorizontalStaffSpec;
import com.jonlatane.composer.scoredisplay.StaffSpec.VerticalStaffSpec;

/**
 * A ScoreDeltaView is a view for a ScoreDelta.  Visually, this corresponds to a "slice" of a
 * nicely-formatted score (where rhythms are aligned between all staves).
 * 
 * Much like a ScoreDelta is composed of StaffDeltas, a ScoreDeltaView is composed of StaffDeltaViews.
 * 
 * @author Jon Latane
 *
 */
class ScoreDeltaView extends LinearLayout {
	private final ScoreLayout _parent;
	Score.ScoreDelta _scoreDelta;
	private int _perfectWidth, _actualWidth;
	private HorizontalStaffSpec _perfectHorizontalStaffSpec, _actualHorizontalStaffSpec;
	
	
	private class WidthEvaluator implements TypeEvaluator<Integer> {
    	@Override
    	public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
	    	Integer result = startValue + (int)((endValue - startValue) * fraction);
	    	//Log.i(TAG,"Scaling to " + result + " for " + fraction + " " + startValue + " " + endValue);
    		setActualWidth(result);
    		return result;
    	}
    }
	
	public final WidthEvaluator EVALUATOR = new WidthEvaluator();
	
	public class StaffDeltaView extends LinearLayout {
		private Score.Staff.StaffDelta _staffDelta;
		TextView _upperLyricView;
		TextView _lowerLyricView;
		
		//These views all need to be synchronizable horizontally and vertically between Collections of StaffDeltaViews
		private View _accidentalArea, _noteHeadArea, _timeSigChangeArea, _keySigChangeArea;
		private LinearLayout _staffDivisionsDrawingArea;
		private StaffSpec.VerticalStaffSpec _perfectVerticalStaffSpec, _actualVerticalStaffSpec;
		
		private class VerticalStaffSpecEvaluator implements TypeEvaluator<StaffSpec.VerticalStaffSpec> {
			@Override
        	public VerticalStaffSpec evaluate(float fraction, StaffSpec.VerticalStaffSpec startValue, StaffSpec.VerticalStaffSpec endValue) {
				int above, below;
				
				above = (int)(startValue.ABOVE_CENTER_PX + (fraction*endValue.ABOVE_CENTER_PX));
				below = (int)(startValue.BELOW_CENTER_PX + (fraction*endValue.BELOW_CENTER_PX));

				StaffSpec.VerticalStaffSpec result = new StaffSpec.VerticalStaffSpec(above,below);
    	    	
        		requestLayout();
        		return result;
        	}
		}
		
		
		
		public StaffDeltaView(Context context) {
			super(context);
			setOrientation(VERTICAL);
			
			// Set up the area for accidentals, notes, time signature changes and key signature changes
			_staffDivisionsDrawingArea = new LinearLayout(context);
			_staffDivisionsDrawingArea.setOrientation(HORIZONTAL);
			
			_accidentalArea = new View(context);
			_noteHeadArea = new View(context);
			_timeSigChangeArea = new View(context);
			_keySigChangeArea = new View(context);
			
			_staffDivisionsDrawingArea.addView(_accidentalArea);
			_staffDivisionsDrawingArea.addView(_noteHeadArea);
			_staffDivisionsDrawingArea.addView(_timeSigChangeArea);
			_staffDivisionsDrawingArea.addView(_keySigChangeArea);
						
			// StaffSpecs
			_perfectVerticalStaffSpec = StaffSpec.VerticalStaffSpec.DEFAULT;
			_actualVerticalStaffSpec = StaffSpec.VerticalStaffSpec.DEFAULT;
			_perfectHorizontalStaffSpec = StaffSpec.HorizontalStaffSpec.DEFAULT;
			_actualHorizontalStaffSpec = StaffSpec.HorizontalStaffSpec.DEFAULT;
			
			// TextViews
			_upperLyricView = new TextView(getContext());
			_lowerLyricView = new TextView(getContext());
			
			_upperLyricView.setTextIsSelectable(true);
			_lowerLyricView.setTextIsSelectable(true);
			
			_upperLyricView.setText("upper");
			_lowerLyricView.setText("lower");
			
			// TODO remove this stuff.
			Button b = new Button(getContext());
			VerticalStaffSpec vss = VerticalStaffSpec.DEFAULT;
			LinearLayout.LayoutParams p = 
					new LinearLayout.LayoutParams(_actualWidth, vss.DEFAULT.ABOVE_CENTER_PX + vss.BELOW_CENTER_PX);
			
			addView(_upperLyricView);
			addView(b);
			addView(_lowerLyricView);
		}
		
		public void setStaffDelta(Score.Staff.StaffDelta d) {
    		_staffDelta = d;
    		
    		//Debugging stuff
    		_upperLyricView.setText(d.LOCATION.toMixedString() + 
    				(d.LOCATION.equals(_parent._score.getFine()) ? " (Fine)" : ""));
		}
		
		public StaffSpec.VerticalStaffSpec getPerfectVerticalStaffSpec() {
			return _perfectVerticalStaffSpec;
		}
		public StaffSpec.VerticalStaffSpec getActualVerticalStaffSpec() {
			return _actualVerticalStaffSpec;
		}
		public void setActualVerticalStaffSpec(StaffSpec.VerticalStaffSpec ss) {
			_actualVerticalStaffSpec = ss;
			requestLayout();
		}
		public void animateToActualVerticalStaffSpec(StaffSpec.VerticalStaffSpec ss) {
			ValueAnimator.ofObject(new VerticalStaffSpecEvaluator(), _actualVerticalStaffSpec, ss).start();
		}
		
		public StaffSpec.HorizontalStaffSpec getPerfectHorizontalStaffSpec() {
			return _perfectHorizontalStaffSpec;
		}
		public StaffSpec.HorizontalStaffSpec getActualHorizontalStaffSpec() {
			return _actualHorizontalStaffSpec;
		}
		public void setActualHorizontalStaffSpec(StaffSpec.HorizontalStaffSpec ss) {
			_actualHorizontalStaffSpec = ss;
			requestLayout();
		}
		
	}
	
	public ScoreDeltaView(Context context, ScoreLayout parent ) {
		super(context);
		setOrientation(VERTICAL);
		_parent = parent;
		_perfectWidth = 30 * Math.max(1, new Random().nextInt(5));
		_actualWidth = _perfectWidth;
	}
	
	public void setScoreDelta(ScoreDelta d) {
		_scoreDelta = d;
		for(int i = 0; i < getChildCount(); i++) {
			removeViewAt(i);
		}
		for(StaffDelta sd : d.STAVES) {
			StaffDeltaView sdv = new StaffDeltaView(getContext());
			sdv.setStaffDelta(sd);
			addView(sdv);
		}
		
		_perfectHorizontalStaffSpec = new HorizontalStaffSpec(d);
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int totalHeight = 0;
		for(int i=0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.measure(widthMeasureSpec, heightMeasureSpec);
			totalHeight += v.getMeasuredHeight();
		}
		setMeasuredDimension(_actualWidth, totalHeight);
	}
	
	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
	
	public int getPerfectWidth() {
		return _perfectWidth;
	}
	public int getActualWidth() {
		return _actualWidth;
	}
	public void setActualWidth(int i) {
		_actualWidth = i;
		requestLayout();
	}
}
package com.jonlatane.composer.scoredisplay;

import java.util.Random;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jonlatane.composer.music.Rational;
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
	public static final String TAG = "ScoreDeltaView";
	private final ScoreLayout _parent;
	Score.ScoreDelta _scoreDelta;
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
		private View _accidentalArea, _noteHeadArea, _clefChangeArea, _keySigChangeArea, _timeSigChangeArea;
		LinearLayout _staffArea;
		
		private VerticalStaffSpec _perfectVerticalStaffSpec, _actualVerticalStaffSpec;
				
		public StaffDeltaView(Context context) {
			super(context);
			setOrientation(VERTICAL);
			
			// These guys's width/height is all defined in the StaffSpecs.
			_accidentalArea = new View(context) {
				@Override
				public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
					setMeasuredDimension(_actualHorizontalStaffSpec.ACCIDENTAL_AREA_PX, _actualVerticalStaffSpec.getTotalHeight());
				}
			};
			_noteHeadArea = new View(context) {
				@Override
				public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
					setMeasuredDimension(_actualHorizontalStaffSpec.NOTEHEAD_AREA_PX, _actualVerticalStaffSpec.getTotalHeight());
				}
			};
			_clefChangeArea = new View(context) {
				@Override
				public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
					setMeasuredDimension(_actualHorizontalStaffSpec.CLEF_PX, _actualVerticalStaffSpec.getTotalHeight());
				}
			};
			_keySigChangeArea = new View(context) {
				@Override
				public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
					setMeasuredDimension(_actualHorizontalStaffSpec.KEYSIG_PX, _actualVerticalStaffSpec.getTotalHeight());
				}
			};
			_timeSigChangeArea = new View(context) {
				@Override
				public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
					setMeasuredDimension(_actualHorizontalStaffSpec.TIMESIG_PX, _actualVerticalStaffSpec.getTotalHeight());
				}
			};

			
			// Set up the area for accidentals, notes, time signature changes and key signature changes.
			// No need to override anything in it, as the above views determine its dimensions.
			_staffArea = new LinearLayout(context) {
				@Override
				public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
					for(int i = 0; i < getChildCount(); i++)
						getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
					setMeasuredDimension(_actualHorizontalStaffSpec.getTotalWidth(), _actualVerticalStaffSpec.getTotalHeight());
				}
			};
			_staffArea.setOrientation(HORIZONTAL);
			
			_staffArea.addView(_accidentalArea);
			_staffArea.addView(_noteHeadArea);
			_staffArea.addView(_clefChangeArea);
			_staffArea.addView(_keySigChangeArea);
			_staffArea.addView(_timeSigChangeArea);
						
			// StaffSpecs
			_perfectVerticalStaffSpec = StaffSpec.VerticalStaffSpec.DEFAULT;
			_actualVerticalStaffSpec = StaffSpec.VerticalStaffSpec.DEFAULT;
			_perfectHorizontalStaffSpec = StaffSpec.HorizontalStaffSpec.DEFAULT;
			_actualHorizontalStaffSpec = StaffSpec.HorizontalStaffSpec.DEFAULT;
			
			// TextViews
			_upperLyricView = new TextView(getContext());
			_lowerLyricView = new TextView(getContext());
			
			_upperLyricView.setTextIsSelectable(false);
			_lowerLyricView.setTextIsSelectable(false);
			
			_upperLyricView.setText("upper");
			_lowerLyricView.setText("lower");
						
			addView(_upperLyricView);
			addView(_staffArea);
			addView(_lowerLyricView);
		}
		
		//TODO this will be eliminated and is only here for debugging
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			int totalHeight = 0;
			for(int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				child.measure(widthMeasureSpec, heightMeasureSpec);
				totalHeight += child.getMeasuredHeight();
			}
			setMeasuredDimension(_actualHorizontalStaffSpec.getTotalWidth(), totalHeight);
		}
		
		public void setStaffDelta(Score.Staff.StaffDelta d) {
    		_staffDelta = d;
    		if(d.LOCATION.equals(Rational.ONE))
    			Log.i(TAG, "DEBUG");
    		_perfectVerticalStaffSpec = new VerticalStaffSpec(d);
    		_actualVerticalStaffSpec = VerticalStaffSpec.scale(_perfectVerticalStaffSpec, _parent.getScalingFactor());
    		_upperLyricView.setText(d.LOCATION.toMixedString());
		}
		
		public StaffSpec.VerticalStaffSpec getPerfectVerticalStaffSpec() {
			//return _perfectVerticalStaffSpec;
			return VerticalStaffSpec.scale(_perfectVerticalStaffSpec, _parent.getScalingFactor());
		}
		public StaffSpec.VerticalStaffSpec getActualVerticalStaffSpec() {
			return _actualVerticalStaffSpec;
		}
		public void setActualVerticalStaffSpec(StaffSpec.VerticalStaffSpec ss) {
			_actualVerticalStaffSpec = ss;
			requestLayout();
		}

		public Score.Staff.StaffDelta getStaffDelta() {
			return _staffDelta;
		}
	}
	
	public ScoreDeltaView(Context context, ScoreLayout parent ) {
		super(context);
		setOrientation(VERTICAL);
		_parent = parent;
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
		_actualHorizontalStaffSpec = HorizontalStaffSpec.scale(_perfectHorizontalStaffSpec, _parent.getScalingFactor());
	}
	
	/**
	 * Returns a number between 0 and 1 representing how "visible" this ScoreDeltaView is
	 * relative to how much space it needs to properly render.
	 * 
	 * @return a number between 0 and 1.
	 */
	public float getVisibilityRatio() {
		return Math.min( 1f,
				(float)(_actualHorizontalStaffSpec.getTotalWidth())
				/ (float)(getPerfectHorizontalStaffSpec().getTotalWidth()));
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int totalHeight = 0;
		for(int i=0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.measure(widthMeasureSpec, heightMeasureSpec);
			totalHeight += v.getMeasuredHeight();
		}
		setMeasuredDimension(getActualWidth(), totalHeight);
	}
	
	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	public VerticalStaffSpec[] getActualVerticalStaffSpecs() {
		VerticalStaffSpec[] result = new VerticalStaffSpec[getChildCount()];
		for(int i = 0; i < getChildCount(); i++) {
			result[i] = ((StaffDeltaView)getChildAt(i)).getActualVerticalStaffSpec();
		}
		return result;
	}
	public VerticalStaffSpec[] getPerfectVerticalStaffSpecs() {
		VerticalStaffSpec[] result = new VerticalStaffSpec[getChildCount()];
		for(int i = 0; i < getChildCount(); i++) {
			result[i] = ((StaffDeltaView)getChildAt(i)).getPerfectVerticalStaffSpec();
		}
		return result;
	}
	
	public void setActualVerticalStaffSpecs(VerticalStaffSpec[] specs) {
		for(int i = 0; i < getChildCount(); i++) {
			((StaffDeltaView)getChildAt(i)).setActualVerticalStaffSpec(specs[i]);
		}
	}
	
	public StaffSpec.HorizontalStaffSpec getPerfectHorizontalStaffSpec() {
		//return _perfectHorizontalStaffSpec;
		return HorizontalStaffSpec.scale(_perfectHorizontalStaffSpec, _parent.getScalingFactor());
	}
	public StaffSpec.HorizontalStaffSpec getActualHorizontalStaffSpec() {
		return _actualHorizontalStaffSpec;
	}
	public void setActualHorizontalStaffSpec(StaffSpec.HorizontalStaffSpec ss) {
		_actualHorizontalStaffSpec = ss;
		requestLayout();
	}
	
	public int getPerfectWidth() {
		return getPerfectHorizontalStaffSpec().getTotalWidth();
	}
	public int getActualWidth() {
		return _actualHorizontalStaffSpec.getTotalWidth();
	}
	public void setActualWidth(int targetWidth) {
		_actualHorizontalStaffSpec = getPerfectHorizontalStaffSpec().adaptToWidth(targetWidth);
		//requestLayout();
		/*for(int i = 0; i < getChildCount(); i++) {
			getChildAt(i).requestLayout();
			((StaffDeltaView)getChildAt(i))._staffArea.requestLayout();
		}*/
			
	}
}
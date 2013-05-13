/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jonlatane.composer.scoredisplay;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.LayoutTransition;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A ScoreLayout is a self-managing ViewGroup that renders {@link ScoreDeltaView}s
 * containing {@link ScoreDeltaView.StaffDeltaView}s which in turn interact with
 * the underlying Score when touched by the user.
 * 
 * The first child view in a ScoreLayout is a SurfaceView that is responsible for
 * rendering a background layer.  This layer predetermines the positions of staff lines
 * before the ScoreLayout lays out its contents and passes them to the ScoreLayout.  The
 * ScoreLayout then places its tiles atop the background.
 * 
 * During layout, the ScoreLayout notes where the noteheads of the Score are and where slurs,
 * stems, and beams should go.  This information is passed to the SurfaceView to draw these.
 * It should also tell the SurfaceView which lines we need to draw a KeySignature or TimeSignature
 * on (i.e., it changed somewhere in the previous line or at the beginning of this line)
 * and which just need the Clef to be drawn.
 * 
 * After everything is laid out, when onDraw is called in the SurfaceView (and the rest),
 * a pretty looking Score comes out.
 * 
 * The Caveat:
 *  
 * Because the ScoreLayout chooses these positions based on the position of the staff provided
 * by the SurfaceView in advance (without knowing what the notes are), it may render very 
 * high/low notes on the next staff down if we are not careful.
 * 
 * "Being careful:"
 * 
 * During layout, the ScoreLayout notes if tiles do not have enough space to render the note.
 * When this happens, this is communicated to the SurfaceView *by letting it know which system
 * it is on and nothing else*.  This would also be the time to let the SurfaceView know about
 * slurs but that's not done yet.
 * 
 * If this is the case, these changes
 */
public class ScoreLayout extends ViewGroup {
	private static final String TAG = "ScoreLayout";
    
    private Score _score;
    private SheetMusicSurfaceView _surface;
    private Rational _topLeft;
    private double _animationOffset = 0;
    
    private double _scalingFactor = 1d;
    
    Animator defaultAppearingAnim, defaultDisappearingAnim;
    Animator defaultChangingAppearingAnim, defaultChangingDisappearingAnim;
    
    public static class StaffSpec {
    	public StaffSpec(int top, int middle, int bottom) {
    		TOP = top;
    		MIDDLE_STAFF = middle;
    		BOTTOM = bottom;
    	}
    	public final int TOP;
    	public final int MIDDLE_STAFF;
    	public final int BOTTOM;
    }
    
    public static int DEFAULTSTAFFHEIGHT = 60;
    public static final StaffSpec DEFAULTSTAFFSPEC = new StaffSpec(0, 40, 80); 
    
    private class ScoreDeltaView extends LinearLayout {
    	private Score.ScoreDelta _scoreDelta;
    	private int _perfectWidth;
    	private int _actualWidth;
    	
    	
    	private class WidthEvaluator implements TypeEvaluator<Integer> {
    	    @SuppressLint("NewApi")
        	@Override
        	public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
    	    	Integer result = startValue + (int)((endValue - startValue) * fraction);
    	    	Log.i(TAG,"Scaling to " + result + " for " + fraction + " " + startValue + " " + endValue);
        		//Float num = (Float)super.evaluate(fraction, startValue, endValue);
        		setActualWidth(result);
        		requestLayout();
        		return result;
        	}
        }
    	
    	public final WidthEvaluator EVALUATOR = new WidthEvaluator();
    	
    	public class StaffDeltaView extends LinearLayout {
    		private Score.Staff.StaffDelta _staffDelta;
    		public StaffDeltaView(Context context) {
    			super(context);
    			setOrientation(VERTICAL);
    			TextView upperLyricView = new TextView(getContext());
    			TextView lowerLyricView = new TextView(getContext());
    			upperLyricView.setText("upper");
    			lowerLyricView.setText("lower");
    			
    			Button b = new Button(getContext());
    			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(_actualWidth, DEFAULTSTAFFSPEC.BOTTOM - DEFAULTSTAFFSPEC.TOP);
    			
    			addView(upperLyricView);
    			addView(b);
    			addView(lowerLyricView);
    		}
    		
    		public void setStaffDelta(Score.Staff.StaffDelta d) {
        		_staffDelta = d;    			
    		}
    		
    	}
    	
    	public ScoreDeltaView(Context context ) {
    		super(context);
    		setOrientation(VERTICAL);
			_perfectWidth = 30 * Math.max(1, new Random().nextInt(5));
			_actualWidth = _perfectWidth;
    	}
    	
    	public void setScoreDelta(Score.ScoreDelta d) {
    		_scoreDelta = d;
    		for(int i = 0; i < getChildCount(); i++) {
    			removeViewAt(i);
    		}
    		for(Score.Staff.StaffDelta sd : d.STAVES) {
    			StaffDeltaView sdv = new StaffDeltaView(getContext());
    			sdv.setStaffDelta(sd);
    			addView(sdv);
    		}
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
		}
    }

    public ScoreLayout(Context context) {
        super(context);
        init();
    }
    
    public ScoreLayout(Context c, AttributeSet a) {
    	super(c,a);
    	init();
    }
    public ScoreLayout(Context c, AttributeSet a, int d) {
    	super(c,a,d);
    	init();
    }
    

    public void init() {
    	
	    final LayoutTransition transitioner = new LayoutTransition();
	    setLayoutTransition(transitioner);
	    defaultAppearingAnim = transitioner.getAnimator(LayoutTransition.APPEARING);
	    defaultDisappearingAnim =
	            transitioner.getAnimator(LayoutTransition.DISAPPEARING);
	    defaultChangingAppearingAnim =
	            transitioner.getAnimator(LayoutTransition.CHANGE_APPEARING);
	    defaultChangingDisappearingAnim =
	            transitioner.getAnimator(LayoutTransition.CHANGE_DISAPPEARING);
	    
	    /*transitioner.setAnimator(LayoutTransition.APPEARING,  defaultAppearingAnim);
	    transitioner.setAnimator(LayoutTransition.DISAPPEARING, defaultDisappearingAnim);
	    transitioner.setAnimator(LayoutTransition.CHANGE_APPEARING, defaultChangingAppearingAnim);
	    transitioner.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, defaultChangingDisappearingAnim);*/
	    
	    //SurfaceView sv = new SurfaceView(getContext());
	    //addView(sv);
	    
	    _surface = new SheetMusicSurfaceView(getContext());
	    _surface.setParent(this);
	    addView(_surface);
	    
	    Score s = Score.twinkleTwinkle();
	    Score.ScoreDelta d = s.scoreDeltaAt(new Rational(1,1));
	    for(int i = 0; i < 100; i++) {
	    	ScoreDeltaView sdv = new ScoreDeltaView(getContext());
	    	sdv.setScoreDelta(d);
	    	addView(sdv,1);
	    }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(resolveSize(Integer.MAX_VALUE, widthMeasureSpec),
                resolveSize(Integer.MAX_VALUE, heightMeasureSpec));
    }

    private boolean _needsMoreViews = false;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
	    /*getLayoutTransition().setAnimator(LayoutTransition.APPEARING,  null);
	    getLayoutTransition().setAnimator(LayoutTransition.DISAPPEARING, null);
	    getLayoutTransition().setAnimator(LayoutTransition.CHANGE_APPEARING, null);
	    getLayoutTransition().setAnimator(LayoutTransition.CHANGE_DISAPPEARING, null);*/

        int myWidth = getMeasuredWidth();
        
        int x = 0;
        int y = 0;

        getChildAt(0).layout(l, t, r, b);
        
        // Lay out everything AS MEASURED as best as we can.
        int count = getChildCount();
        for (int index=1; index<count; index++) {
            final View child = getChildAt(index);

            int w = child.getMeasuredWidth();
            int h = child.getMeasuredHeight();
            
            int left = x;
            int top = y;
            
            // This View won't be visible to the user, remove this and all subsequent Views.
            if(top > getMeasuredHeight()) {
            	//for(int j = index; j < count; j++) {
            	//	removeViewAt(j);
            	//}
            	while(index < getChildCount())
            		removeViewAt(index );
            	break;
            }
            
            // There is room for this View horizontally, place it normally
            if(left + w < myWidth) {
            	child.layout(left, top, left+w, top+h);
            	x += w;
            
            // There is going to be a gap, we must place this view approximately
            // and scale views (up only) so this one will fit with an animation.
            } else {
            	// This ratio represents "proximity" to where the View should be drawn.
            	// 0 represents perfectly on the next line, 1 perfectly here.  Anywhere
            	// in between should correspond to positions in between for a transition
            	double d = (myWidth - left) / (double)w;
            	left = (int)(left * d);
            	top = (int)(top + (h * (1d-d)));
            	
            	child.layout(left, top, left+w, top+h);
            	
            	x = (int)(w * (1 - d));
            	y += h;
            }
            
            Log.i(TAG,"x=" + x);
        }
        
        if(y < getMeasuredHeight()) {
        	_needsMoreViews = true;
        }
        
	    /*getLayoutTransition().setAnimator(LayoutTransition.APPEARING,  defaultAppearingAnim);
	    getLayoutTransition().setAnimator(LayoutTransition.DISAPPEARING, defaultDisappearingAnim);
	    getLayoutTransition().setAnimator(LayoutTransition.CHANGE_APPEARING, defaultChangingAppearingAnim);
	    getLayoutTransition().setAnimator(LayoutTransition.CHANGE_DISAPPEARING, defaultChangingDisappearingAnim);*/
    }
    
    public void fixLayoutWidths() {
    	int myWidth = getMeasuredWidth();
    	int totalPerfectWidth = 0;

    	int rowStartIndex = 1;
    	
        LinkedHashMap<LinkedList<ScoreDeltaView>,Double> rows = new LinkedHashMap<LinkedList<ScoreDeltaView>,Double>();
    	LinkedList<ScoreDeltaView> row = new LinkedList<ScoreDeltaView>();
    	for(int i = rowStartIndex; i < getChildCount(); i++) {
    		ScoreDeltaView potentialRowMember = (ScoreDeltaView)getChildAt(i);
    		if(potentialRowMember == null)
    			break;
    		// Add element to the row
    		if(totalPerfectWidth + potentialRowMember.getPerfectWidth() < myWidth) {
    			row.add(potentialRowMember);
    			totalPerfectWidth += potentialRowMember.getPerfectWidth();
    		// End of row, figure out width
    		} else {
            	double perfectWidthFactor = (double)myWidth/(double)totalPerfectWidth;
            	rows.put(row,perfectWidthFactor);
            	row = new LinkedList<ScoreDeltaView>();
            	row.add(potentialRowMember);
            	totalPerfectWidth = potentialRowMember.getPerfectWidth();
    		}
    		Log.i(TAG,"Laid out " + i + " children");
    	}
    	
    	// Scale the last row to its perfect width
    	if(!row.isEmpty()) {
    		rows.put(row,1d);
    	}
    	
    	for(Map.Entry<LinkedList<ScoreDeltaView>,Double> e : rows.entrySet()) {
    		int rowWidth = 0;
    		for(ScoreDeltaView sdv : e.getKey()) {
    			int targetItemWidth = (int)(e.getValue() * sdv.getPerfectWidth());
    			// Because computer precision sucks, this kludge will extend the last element slightly.
    			if(sdv == e.getKey().getLast()) {
    				Log.i(TAG,"Adjusting last element");
    				targetItemWidth = myWidth - rowWidth;
    			}
    			rowWidth += targetItemWidth;

	        	ValueAnimator.ofObject(sdv.EVALUATOR,
	        			sdv.getActualWidth(), targetItemWidth).start();

    		}
    		Log.i(TAG,"Made row of width " + rowWidth);
    	}
    }
    
    public void addElementToBeginning() {
    	ScoreDeltaView slice = new ScoreDeltaView(getContext());
    	addView(slice, 1);
    }
    public void addElementToEnd() {
    	ScoreDeltaView slice = new ScoreDeltaView(getContext());
    	addView(slice,getChildCount());
    }
    public void removeFirstElement() {
    	removeViewAt(1);
    }
    public void removeLastElement() {
    	removeViewAt(getChildCount());
    }
}


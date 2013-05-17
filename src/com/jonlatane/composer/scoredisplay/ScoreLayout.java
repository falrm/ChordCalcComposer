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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.jonlatane.composer.R;
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
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
	
	private GestureDetector _gestureDetector;
	
    private SheetMusicSurfaceView _surface;
    
    private Score _score;
    private Rational _topLeft;
    private double _animationOffset = 0;
    
    private double _scalingFactor = 1d;
    
    Animator defaultAppearingAnim, defaultDisappearingAnim;
    Animator defaultChangingAppearingAnim, defaultChangingDisappearingAnim;
    
    public static class VerticalStaffSpec {
		public static final VerticalStaffSpec DEFAULT = new VerticalStaffSpec(0, 40, 80);
		public static int DEFAULTSTAFFHEIGHT = 60;
		
		public final int TOP, MIDDLE_STAFF, BOTTOM;
    	public VerticalStaffSpec(int top, int middle, int bottom) {
    		TOP = top;
    		MIDDLE_STAFF = middle;
    		BOTTOM = bottom;
    	}
    	
		public static VerticalStaffSpec best(VerticalStaffSpec ss1, VerticalStaffSpec ss2) {
			int top, middle, bottom;
			assert(ss1.TOP == 0 && ss2.TOP == 0);
			top = 0;
			middle = Math.max(ss1.MIDDLE_STAFF, ss2.MIDDLE_STAFF);
			bottom = middle + Math.max(ss1.BOTTOM - ss1.MIDDLE_STAFF, ss2.BOTTOM - ss2.MIDDLE_STAFF);
			return new ScoreLayout.VerticalStaffSpec(top, middle, bottom);
		}
    }
    
    public static class HorizontalStaffSpec {
		public static final HorizontalStaffSpec DEFAULT = new HorizontalStaffSpec(0, 20, 40);
		
		public final int LEFT, CENTER_NOTEHEAD, RIGHT;
    	public HorizontalStaffSpec(int top, int middle, int bottom) {
    		LEFT = top;
    		CENTER_NOTEHEAD = middle;
    		RIGHT = bottom;
    	}
    	
		public static HorizontalStaffSpec best(HorizontalStaffSpec ss1, HorizontalStaffSpec ss2) {
			int top, middle, bottom;
			assert(ss1.LEFT == 0 && ss2.LEFT == 0);
			top = 0;
			middle = Math.max(ss1.CENTER_NOTEHEAD, ss2.CENTER_NOTEHEAD);
			bottom = middle + Math.max(ss1.RIGHT - ss1.CENTER_NOTEHEAD, ss2.RIGHT - ss2.CENTER_NOTEHEAD);
			return new HorizontalStaffSpec(top, middle, bottom);
		}
    }
    
    private class ScoreDeltaView extends LinearLayout {
    	private Score.ScoreDelta _scoreDelta;
    	private int _perfectWidth, _actualWidth;
    	
    	
    	private class WidthEvaluator implements TypeEvaluator<Integer> {
    	    @SuppressLint("NewApi")
        	@Override
        	public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
    	    	Integer result = startValue + (int)((endValue - startValue) * fraction);
    	    	Log.i(TAG,"Scaling to " + result + " for " + fraction + " " + startValue + " " + endValue);
        		//Float num = (Float)super.evaluate(fraction, startValue, endValue);
        		setActualWidth(result);
        		return result;
        	}
        }
    	
    	public final WidthEvaluator EVALUATOR = new WidthEvaluator();
    	
    	public class StaffDeltaView extends LinearLayout {
    		private Score.Staff.StaffDelta _staffDelta;
    		private TextView _upperLyricView, _lowerLyricView;
    		private VerticalStaffSpec _perfectVerticalStaffSpec, _actualVerticalStaffSpec;
    		private HorizontalStaffSpec _perfectHorizontalStaffSpec, _actualHorizontalStaffSpec;
    		
			private class VerticalStaffSpecEvaluator implements TypeEvaluator<VerticalStaffSpec> {
				@Override
	        	public VerticalStaffSpec evaluate(float fraction, VerticalStaffSpec startValue, VerticalStaffSpec endValue) {
					int top, middle, bottom;
					
					top = (int)(startValue.TOP + (fraction * endValue.TOP));
					middle = (int)(startValue.MIDDLE_STAFF + (fraction * endValue.MIDDLE_STAFF));
					bottom = (int)(startValue.BOTTOM + (fraction * endValue.BOTTOM));

					VerticalStaffSpec result = new VerticalStaffSpec(top,middle,bottom);
	    	    	
	        		requestLayout();
	        		return result;
	        	}
			}
			
			private class HorizontalStaffSpecEvaluator implements TypeEvaluator<HorizontalStaffSpec> {
				@Override
	        	public HorizontalStaffSpec evaluate(float fraction, HorizontalStaffSpec startValue, HorizontalStaffSpec endValue) {
					int left, center, right;
					
					left = (int)(startValue.LEFT + (fraction * endValue.LEFT));
					center = (int)(startValue.CENTER_NOTEHEAD + (fraction * endValue.CENTER_NOTEHEAD));
					right = (int)(startValue.RIGHT + (fraction * endValue.RIGHT));

					HorizontalStaffSpec result = new HorizontalStaffSpec(left,center,right);
	    	    	
	        		requestLayout();
	        		return result;
	        	}
			}
    		
    		public StaffDeltaView(Context context) {
    			super(context);
    			setOrientation(VERTICAL);
    			_perfectVerticalStaffSpec = VerticalStaffSpec.DEFAULT;
    			_actualVerticalStaffSpec = VerticalStaffSpec.DEFAULT;
    			_perfectHorizontalStaffSpec = HorizontalStaffSpec.DEFAULT;
    			_actualHorizontalStaffSpec = HorizontalStaffSpec.DEFAULT;
    			
    			_upperLyricView = new TextView(getContext());
    			_lowerLyricView = new TextView(getContext());
    			_upperLyricView.setText("upper");
    			_lowerLyricView.setText("lower");
    			
    			
    			Button b = new Button(getContext());
    			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(_actualWidth, VerticalStaffSpec.DEFAULT.BOTTOM - VerticalStaffSpec.DEFAULT.TOP);
    			
    			addView(_upperLyricView);
    			addView(b);
    			addView(_lowerLyricView);
    		}
    		
    		public void setStaffDelta(Score.Staff.StaffDelta d) {
        		_staffDelta = d;
        		
        		//Debugging stuff
        		_upperLyricView.setText(d.LOCATION.toMixedString() + 
        				(d.LOCATION.equals(_score.getFine()) ? " (Fine)" : ""));
    		}
    		
    		public VerticalStaffSpec getPerfectVerticalStaffSpec() {
    			return _perfectVerticalStaffSpec;
    		}
    		public VerticalStaffSpec getActualVerticalStaffSpec() {
    			return _actualVerticalStaffSpec;
    		}
    		public void setActualVerticalStaffSpec(VerticalStaffSpec ss) {
    			_actualVerticalStaffSpec = ss;
    			requestLayout();
    		}
    		public void animateToActualVerticalStaffSpec(VerticalStaffSpec ss) {
    			ValueAnimator.ofObject(new VerticalStaffSpecEvaluator(), _actualVerticalStaffSpec, ss).start();
    		}
    		
    		public HorizontalStaffSpec getPerfectHorizontalStaffSpec() {
    			return _perfectHorizontalStaffSpec;
    		}
    		public HorizontalStaffSpec getActualHorizontalStaffSpec() {
    			return _actualHorizontalStaffSpec;
    		}
    		public void setActualHorizontalStaffSpec(HorizontalStaffSpec ss) {
    			_actualHorizontalStaffSpec = ss;
    			requestLayout();
    		}
    		public void animateToActualHorizontalStaffSpec(HorizontalStaffSpec ss) {
    			ValueAnimator.ofObject(new HorizontalStaffSpecEvaluator(), _actualHorizontalStaffSpec, ss).start();
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
			requestLayout();
		}
    }

    public ScoreLayout(Context context) {
        super(context);
        onCreate();
    }
    
    public ScoreLayout(Context c, AttributeSet a) {
    	super(c,a);
    	onCreate();
    }
    public ScoreLayout(Context c, AttributeSet a, int d) {
    	super(c,a,d);
    	onCreate();
    }
    

    public void onCreate() {
    	
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
	    
	    /*Score s = Score.twinkleTwinkle();
	    Score.ScoreDelta d = s.scoreDeltaAt(new Rational(1,1));
	    for(int i = 0; i < 100; i++) {
	    	ScoreDeltaView sdv = new ScoreDeltaView(getContext());
	    	sdv.setScoreDelta(d);
	    	addView(sdv,1);
	    }*/
	    openScore(Score.twinkleTwinkle());
    }
    
    public void openScore(Score s) {
    	_score = s;
    	Iterator<Score.ScoreDelta> itr = s.scoreIterator(Rational.ZERO);
    	int startIndex = 1;
    	while(itr.hasNext()) {
    		Score.ScoreDelta scd = itr.next();
    		ScoreDeltaView sdv = new ScoreDeltaView(getContext());
    		sdv.setScoreDelta(scd);
    		addView(sdv, startIndex++);
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
        }
        
        if(y < getMeasuredHeight()) {
        	_needsMoreViews = true;
        }
        
	    /**/
    }

	void enableTransitions() {
		getLayoutTransition().setAnimator(LayoutTransition.APPEARING,
				defaultAppearingAnim);
		getLayoutTransition().setAnimator(LayoutTransition.DISAPPEARING,
				defaultDisappearingAnim);
		getLayoutTransition().setAnimator(LayoutTransition.CHANGE_APPEARING,
				defaultChangingAppearingAnim);
		getLayoutTransition().setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
				defaultChangingDisappearingAnim);

	}

	void disableTransitions() {
		getLayoutTransition().setAnimator(LayoutTransition.APPEARING, null);
		getLayoutTransition().setAnimator(LayoutTransition.DISAPPEARING, null);
		getLayoutTransition().setAnimator(LayoutTransition.CHANGE_APPEARING,
				null);
		getLayoutTransition().setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
				null);
	}
    
    Float __prevMotionX = null;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	Log.i(TAG,"onTouchEvent");
    	if(event.getPointerCount() > 1) {
    		return false;
    	}
    	if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
    		__prevMotionX = event.getX();
    		return true;
    	} else if(event.getActionMasked() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1) {
    		disableTransitions();
    		if(__prevMotionX != null) {
    			int dx = (int) (event.getX() - __prevMotionX);

    			Log.i(TAG,"Move of x" + event.getX() + "," + __prevMotionX + " dx="+dx);
    			
    			//Scroll forwards (right to left swipe)
		        if (dx < 0) {
		            int removedWidth = 0;
		            while(true) {
		            	ScoreDeltaView sdv = (ScoreDeltaView)getChildAt(1);
		            	if(sdv == null || sdv._scoreDelta.LOCATION.compareTo(_score.getFine()) >= 0)
		            		break;
		            	if(removedWidth + sdv.getActualWidth() <= -dx) {
		            		removedWidth += sdv.getActualWidth();
		            		Log.i(TAG,"removing view");
		            		removeViewAt(1);
		            	} else {
		            		Log.i(TAG,"Breaking with removedWidth="+removedWidth);
		            		break;
		            	}
		            }
		            
		            
		            int leftToRemove = -dx - removedWidth;
		            Log.i(TAG,"leftToRemove=" + leftToRemove + " removedWidth=" + removedWidth);
		            ScoreDeltaView first = (ScoreDeltaView)getChildAt(1);
		            Log.i(TAG,"Shortening first view to " + (first.getActualWidth() - leftToRemove));
		            first.setActualWidth(first.getActualWidth() - leftToRemove);
		            
		        // Scroll backwards (left to right swipe)
		        } else if(dx > 0) {
		        	ScoreDeltaView first = (ScoreDeltaView)getChildAt(1);
		        	Rational startingPoint;
		        	if(first == null)
		        		startingPoint = _score.getFine();
		        	else
		        		startingPoint = first._scoreDelta.LOCATION;
		        	
		        	Iterator<Score.ScoreDelta> itr = _score.reverseScoreIterator(startingPoint, false);
		        	
		        	int leftToAdd = dx;
		        	
		        	while(true) {
		        		if(first.getActualWidth() < first.getPerfectWidth()) {
		        			int addable = first.getPerfectWidth() - first.getActualWidth();
		        			if(leftToAdd <= addable) {
		        				first.setActualWidth(first.getActualWidth() + leftToAdd);
		        				break;
		        			} else {
		        				first.setActualWidth(first.getActualWidth() + addable);
		        				leftToAdd -= addable;
		        			}
		        		} else {
		        			if(!itr.hasNext())
		        				break;
		        			Score.ScoreDelta scd = itr.next();
		        			ScoreDeltaView sdv = new ScoreDeltaView(getContext());
		        			sdv.setScoreDelta(scd);
		        			addView(sdv, 1);
		        			
		        			int addable = sdv.getPerfectWidth();
		        			if(leftToAdd <= addable) {
		        				sdv.setActualWidth(leftToAdd);
		        				break;
		        			} else {
		        				sdv.setActualWidth(addable);
		        				leftToAdd -= addable;
		        			}
		        		}
		        	}
		        }
    		}
    		requestLayout();
    		enableTransitions();
    		__prevMotionX = event.getX();
	        return true;
    	} else if(event.getActionMasked() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
    		fixLayout();
    	}
		return false;
	}
    
    /**
     * This map is useful for quite a few things.
     * @return
     */
    private LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,VerticalStaffSpec[]>> deriveRows() {
    	int myWidth = getMeasuredWidth();
    	int totalPerfectWidth = 0;

    	int rowStartIndex = 1;
    	
    	LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,VerticalStaffSpec[]>> result = new LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,VerticalStaffSpec[]>>();
    	LinkedList<ScoreDeltaView> row = new LinkedList<ScoreDeltaView>();
    	
    	VerticalStaffSpec[] bestStaffSpecs = new VerticalStaffSpec[((ScoreDeltaView)getChildAt(1))._scoreDelta.STAVES.length];
    	for(int j = 0; j < bestStaffSpecs.length; j++)
    		bestStaffSpecs[j] = VerticalStaffSpec.DEFAULT;
    	
    	for(int i = rowStartIndex; i < getChildCount(); i++) {
    		ScoreDeltaView potentialRowMember = (ScoreDeltaView)getChildAt(i);
    		if(potentialRowMember == null)
    			break;
    		// Add element to the row
    		if(totalPerfectWidth + potentialRowMember.getPerfectWidth() < myWidth) {
    			row.add(potentialRowMember);
    			totalPerfectWidth += potentialRowMember.getPerfectWidth();
    			for(int j = 0; j < potentialRowMember.getChildCount(); j++) {
    				ScoreDeltaView.StaffDeltaView staffDV = (ScoreDeltaView.StaffDeltaView) potentialRowMember.getChildAt(j);
    				bestStaffSpecs[j] = VerticalStaffSpec.best(staffDV.getPerfectVerticalStaffSpec(),bestStaffSpecs[j]);
    			}
    		// End of row, figure out width
    		} else {
            	double perfectWidthFactor = (double)myWidth/(double)totalPerfectWidth;
            	Pair<Double,VerticalStaffSpec[]> p = new Pair<Double,VerticalStaffSpec[]>(perfectWidthFactor,bestStaffSpecs);
            	result.put(row,p);
            	row = new LinkedList<ScoreDeltaView>();
            	for(int j = 0; j < bestStaffSpecs.length; j++)
            		bestStaffSpecs[j] = VerticalStaffSpec.DEFAULT;
            	row.add(potentialRowMember);
            	totalPerfectWidth = potentialRowMember.getPerfectWidth();
    		}
    		Log.i(TAG,"Laid out " + i + " children");
    	}
    	
    	// Scale the last row to its perfect width
    	if(!row.isEmpty()) {
    		result.put(row,new Pair<Double,VerticalStaffSpec[]>(1d, bestStaffSpecs));
    	}
    	
    	return result;
    }
    
    public void fixLayout() {
    	int myWidth = getMeasuredWidth();
    	
    	if(getChildAt(1) == null)
    		return;
    	
        LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,VerticalStaffSpec[]>> rows = deriveRows();
    	
    	for(Map.Entry<LinkedList<ScoreDeltaView>,Pair<Double,VerticalStaffSpec[]>> e : rows.entrySet()) {
    		int rowWidth = 0;
    		
    		boolean[] hasUpperLyrics = new boolean[e.getKey().getFirst()._scoreDelta.STAVES.length];
    		boolean[] hasLowerLyrics = new boolean[hasUpperLyrics.length];
    		
    		for(int i = 0; i < hasUpperLyrics.length;i++) {
    			hasUpperLyrics[i] = false;
    			hasLowerLyrics[i] = false;
    		}
    		for(ScoreDeltaView scoreDV : e.getKey()) {
    			int targetItemWidth = (int)(e.getValue().first * scoreDV.getPerfectWidth());
    			
    			// FIRST.  Make sure the width of the ScoreDelta is set.
    			// Because computer precision sucks, this kludge will extend the last element slightly.
    			if(scoreDV == e.getKey().getLast() && !scoreDV._scoreDelta.LOCATION.equals(_score.getFine())) {
    				Log.i(TAG,"Adjusting last element");
    				targetItemWidth = myWidth - rowWidth;
    			}
    			rowWidth += targetItemWidth;
    			
    			for(int i = 0; i < scoreDV.getChildCount(); i++) {
    				ScoreDeltaView.StaffDeltaView staffDV = (ScoreDeltaView.StaffDeltaView)scoreDV.getChildAt(i);
    				if(!staffDV._upperLyricView.getText().toString().trim().equals(""))
    					hasUpperLyrics[i] = true;
        			if(!staffDV._lowerLyricView.getText().toString().trim().equals(""))
    					hasLowerLyrics[i] = true;
    			}
    			
	        	ValueAnimator.ofObject(scoreDV.EVALUATOR,
	        			scoreDV.getActualWidth(), targetItemWidth).start();
	        	
	        	//

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


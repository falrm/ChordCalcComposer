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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.scoredisplay.ScoreDrawingSurface.SystemHeader;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * A ScoreLayout is a self-managing ViewGroup that renders {@link ScoreDeltaView}s
 * containing {@link ScoreDeltaView.StaffDeltaView}s which in turn interact with
 * the underlying Score when touched by the user.
 * 
 * The first child view in a ScoreLayout is a ScoreDrawingSurface that is responsible for
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
    
    Score _score;

    private ScoreDrawingSurface _surface;
    double _scalingFactor = 1d;
    
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
	    
	    _surface = new ScoreDrawingSurface(getContext(), this);
	    addView(_surface);
	    
	    Score s = Score.twinkleTwinkle();
	    Score.testScore(s);
	    openScore(s);
    }
    
    public void openScore(Score s) {
    	_score = s;
    	Iterator<Score.ScoreDelta> itr = s.scoreIterator(Rational.ZERO);
    	int startIndex = 1;
    	while(itr.hasNext() && startIndex < 500) {
    		Score.ScoreDelta scd = itr.next();
    		ScoreDeltaView sdv = new ScoreDeltaView(getContext(), this);
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int myWidth = getMeasuredWidth();
        
        int x = 0;
        int y = 0;
        
        int systemNumber = 1;

        //getChildAt(0).layout(l, t, r, b);
        _surface.layout(l, t, r, b);
        
        // Lay out everything AS MEASURED as best as we can.
        for (int index=1; index<getChildCount(); index++) {
            final ScoreDeltaView child = (ScoreDeltaView) getChildAt(index);

            int w = child.getMeasuredWidth();
            int h = child.getMeasuredHeight();
            
            int left = x;
            int top = y;
            
            // This View won't be visible to the user, remove this and all subsequent Views.
            if(top > getMeasuredHeight()) {
            	while(index < getChildCount())
            		removeViewAt(index );
            	break;
            }
            
            // There is room for this View horizontally, place it normally in the same system we've been
            // placing Views.
            if(left + w < myWidth) {
            	child.layout(left, top, left+w, top+h);
            	x += w;
            
            // There isn't room for this View in the system.  How close is appears to "being" in the next
            // system depends on how wide it is compared to how much space is left.
            } else {
            	// This ratio represents "proximity" to where the View should be drawn.
            	// 0 represents perfectly on the next line, 1 perfectly here.  Anywhere
            	// in between should correspond to positions in between for a transition
            	double d = (myWidth - left) / (double)w;
            	
            	while(_surface.getChildCount() < systemNumber + 1) {
            		_surface.addView(_surface.new SystemHeader(_surface.getContext()));
            	}
            	SystemHeader header = (SystemHeader)_surface.getChildAt(systemNumber);
            	int systemHeaderViewWidth;
            	
            	
            	
            	left = (int)(left * d);
            	top = (int)(top + (h * (1d-d)));
            	
            	child.layout(left, top, left+w, top+h);
            	
            	x = (int)(w * (1 - d));
            	y += h;
            	systemNumber += 1;
            }
            
        }
        
        _surface.invalidate();
    }

	void enableTransitions() {
		LayoutTransition t = getLayoutTransition();
		if(t == null) {
			t = new LayoutTransition();
			setLayoutTransition(t);
		}
			
		t.setAnimator(LayoutTransition.APPEARING,
				t.getAnimator(LayoutTransition.APPEARING));
		t.setAnimator(LayoutTransition.DISAPPEARING,
				t.getAnimator(LayoutTransition.DISAPPEARING));
		t.setAnimator(LayoutTransition.CHANGE_APPEARING,
				t.getAnimator(LayoutTransition.CHANGE_APPEARING));
		t.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
				t.getAnimator(LayoutTransition.CHANGE_DISAPPEARING));

	}

	void disableTransitions() {
		LayoutTransition t = getLayoutTransition();
		if(t == null) {
			return;
		}
		t.setAnimator(LayoutTransition.APPEARING, null);
		t.setAnimator(LayoutTransition.DISAPPEARING, null);
		t.setAnimator(LayoutTransition.CHANGE_APPEARING,
				null);
		t.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
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
		        	scrollLeftBy(-dx);

		        // Scroll backwards (left to right swipe)
		        } else if(dx > 0) {
		        	scrollRightBy(dx);
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
    
    
    private void scrollLeftBy(int dx) {
    	//Remove stuff from the top left
        int removedWidth = 0;
        while(true) {
        	ScoreDeltaView sdv = (ScoreDeltaView)getChildAt(1);
        	if(sdv == null || sdv._scoreDelta.LOCATION.compareTo(_score.getFine()) >= 0)
        		break;
        	if(removedWidth + sdv.getActualWidth() <= dx) {
        		removedWidth += sdv.getActualWidth();
        		Log.i(TAG,"removing view");
        		removeViewAt(1);
        	} else {
        		Log.i(TAG,"Breaking with removedWidth="+removedWidth);
        		break;
        	}
        }
        
        
        int leftToRemove = dx - removedWidth;
        Log.i(TAG,"leftToRemove=" + leftToRemove + " removedWidth=" + removedWidth);
        ScoreDeltaView first = (ScoreDeltaView)getChildAt(1);
        if(first._scoreDelta.LOCATION.compareTo(_score.getFine()) < 0) {
            Log.i(TAG,"Shortening first view to " + (first.getActualWidth() - leftToRemove));
            first.setActualWidth(first.getActualWidth() - leftToRemove);
        }
        
        //Add stuff to the bottom right
        ScoreDeltaView lastVisible = (ScoreDeltaView)getChildAt(getChildCount()-1);
        Iterator<ScoreDelta> itr = _score.scoreIterator(lastVisible._scoreDelta.LOCATION, false);
        int leftToAdd = dx;
        while(true) {
    		if(lastVisible.getActualWidth() < lastVisible.getPerfectWidth()) {
    			int addable = lastVisible.getPerfectWidth() - lastVisible.getActualWidth();
    			if(leftToAdd <= addable) {
    				lastVisible.setActualWidth(lastVisible.getActualWidth() + leftToAdd);
    				break;
    			} else {
    				lastVisible.setActualWidth(lastVisible.getActualWidth() + addable);
    				leftToAdd -= addable;
    			}
    		} else {
    			if(!itr.hasNext())
    				break;
    			Score.ScoreDelta scd = itr.next();
    			ScoreDeltaView sdv = new ScoreDeltaView(getContext(), this);
    			sdv.setScoreDelta(scd);
    			addView(sdv, getChildCount());
    			
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
    
    private void scrollRightBy(int dx) {
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
    			ScoreDeltaView sdv = new ScoreDeltaView(getContext(), this);
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
    
    /**
     * This map is useful for quite a few things.
     * @return
     */
    private LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,StaffSpec.VerticalStaffSpec[]>> deriveRows() {
    	int myWidth = getMeasuredWidth();
    	int totalPerfectWidth = 0;

    	int rowStartIndex = 1;
    	
    	LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,StaffSpec.VerticalStaffSpec[]>> result = new LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,StaffSpec.VerticalStaffSpec[]>>();
    	LinkedList<ScoreDeltaView> row = new LinkedList<ScoreDeltaView>();
    	
    	StaffSpec.VerticalStaffSpec[] bestStaffSpecs = new StaffSpec.VerticalStaffSpec[((ScoreDeltaView)getChildAt(1))._scoreDelta.STAVES.length];
    	for(int j = 0; j < bestStaffSpecs.length; j++)
    		bestStaffSpecs[j] = StaffSpec.VerticalStaffSpec.DEFAULT;
    	
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
    				bestStaffSpecs[j] = StaffSpec.VerticalStaffSpec.best(staffDV.getPerfectVerticalStaffSpec(),bestStaffSpecs[j]);
    			}
    		// End of row, figure out width
    		} else {
            	double perfectWidthFactor = (double)myWidth/(double)totalPerfectWidth;
            	Pair<Double,StaffSpec.VerticalStaffSpec[]> p = new Pair<Double,StaffSpec.VerticalStaffSpec[]>(perfectWidthFactor,bestStaffSpecs);
            	result.put(row,p);
            	row = new LinkedList<ScoreDeltaView>();
            	for(int j = 0; j < bestStaffSpecs.length; j++)
            		bestStaffSpecs[j] = StaffSpec.VerticalStaffSpec.DEFAULT;
            	row.add(potentialRowMember);
            	totalPerfectWidth = potentialRowMember.getPerfectWidth();
    		}
    		Log.i(TAG,"Laid out " + i + " children");
    	}
    	
    	// Scale the last row to its perfect width
    	if(!row.isEmpty()) {
    		result.put(row,new Pair<Double,StaffSpec.VerticalStaffSpec[]>(1d, bestStaffSpecs));
    	}
    	
    	return result;
    }
    
    public void fixLayout() {
    	int myWidth = getMeasuredWidth();
    	
    	if(getChildAt(1) == null)
    		return;
    	
        LinkedHashMap<LinkedList<ScoreDeltaView>,Pair<Double,StaffSpec.VerticalStaffSpec[]>> rows = deriveRows();
    	
    	for(Map.Entry<LinkedList<ScoreDeltaView>,Pair<Double,StaffSpec.VerticalStaffSpec[]>> e : rows.entrySet()) {
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
    
    @Override
    public boolean shouldDelayChildPressedState() {
    	return false;
    }
    
    public void removeFirstElement() {
    	removeViewAt(1);
    }
}


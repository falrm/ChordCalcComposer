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
import java.util.List;
import java.util.Map;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.ScoreDelta;
import com.jonlatane.composer.scoredisplay.ScoreDrawingSurface.SystemHeaderView;
import com.jonlatane.composer.scoredisplay.StaffSpec.HorizontalStaffSpec;
import com.jonlatane.composer.scoredisplay.StaffSpec.VerticalStaffSpec;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
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
	private static final double MAX_SCALE = 2, MIN_SCALE = .5;
    
    Score _score;

    private ScoreDrawingSurface _surface;
    private double SCALINGFACTOR = 1d;
    
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
	    disableTransitions();
	    
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

    /**
     * Returns a list of all the ScoreDeltaViews that fit in a row of the targetWidth based on their
     * actual widths.  
     * 
     * The second part of the result pair represents how much space was left.  For instance, for 3
     * ScoreDeltaViews of width 20 and a targetWidth of 50, result.first.size() = 2 and result.second = 10.
     * 
     * @param startingIndex
     * @param targetWidth
     * @return
     */
    private Pair< LinkedList<ScoreDeltaView>, Integer > getActualRow(int startingIndex, int targetWidth) {
    	assert(startingIndex < getChildCount());
    	
    	if(getChildCount() == 2)
    		Log.i(TAG, "Uhoh?");
    	
    	LinkedList<ScoreDeltaView> row = new LinkedList<ScoreDeltaView>();
    	int remainingWidth = targetWidth;
    	
    	for(int i = startingIndex; i < getChildCount(); i++) {
    		ScoreDeltaView sdv = (ScoreDeltaView) getChildAt(i);
    		if(remainingWidth - sdv.getActualWidth() >= 0) {
    			row.add(sdv);
    			remainingWidth -= sdv.getActualWidth();
    		} else {
    			break;
    		}
    	}
    	
    	return new Pair<LinkedList<ScoreDeltaView>, Integer>(row, remainingWidth);
    }
    
    private Pair< LinkedList<ScoreDeltaView>, Integer > getPerfectRow(int startingIndex, int targetWidth) {
    	assert(startingIndex < getChildCount());
    	
    	if(getChildCount() == 2)
    		Log.i(TAG, "Uhoh?");
    	
    	LinkedList<ScoreDeltaView> row = new LinkedList<ScoreDeltaView>();
    	int remainingWidth = targetWidth;
    	
    	for(int i = startingIndex; i < getChildCount(); i++) {
    		ScoreDeltaView sdv = (ScoreDeltaView) getChildAt(i);
    		if(remainingWidth - sdv.getPerfectWidth() >= 0) {
    			row.add(sdv);
    			remainingWidth -= sdv.getPerfectWidth();
    		} else {
    			break;
    		}
    	}
    	
    	return new Pair<LinkedList<ScoreDeltaView>, Integer>(row, remainingWidth);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	// USE ALL THE SPACE
        setMeasuredDimension(resolveSize(Integer.MAX_VALUE, widthMeasureSpec),
                resolveSize(Integer.MAX_VALUE, heightMeasureSpec));
        
        // This doesn't measure any of the SystemHeaderViews in _surface - that is done below.
        // It will, however, measure its SurfaceView at index 0.  Hence, we start with system number 1.
        _surface.measure(widthMeasureSpec, heightMeasureSpec);
        int systemNumber = 1;
        
        int effectiveWidthOfFirstRowMember = ((ScoreDeltaView) getChildAt(1)).getActualWidth();
        SystemHeaderView finalPartialHeader = null;
        // We will work a system at a time, setting rowStartIndex
        for (int rowStartIndex=1; rowStartIndex < getChildCount(); systemNumber++) {
        	// We must first compute how wide the system header will be based on the first two members of this row.
        	// Because the header's height is determined based on its width, it is not actually measured until
        	// after we have resolved the height of the row.
        	SystemHeaderView header = (SystemHeaderView)_surface.getChildAt(systemNumber);
        	if(header == null) {
        		header = _surface.new SystemHeaderView(_surface.getContext());
        		_surface.addView(header, systemNumber);
        	}
        	
        	ScoreDeltaView firstSDVInRow = (ScoreDeltaView)getChildAt(rowStartIndex);
        	ScoreDeltaView secondSDVInRow = (ScoreDeltaView)getChildAt(rowStartIndex + 1);
        	
        	header.setPartialDelta(firstSDVInRow);
        	header.setCompleteDelta(secondSDVInRow);
        	
        	//Log.i(TAG,"VSS headache " + effectiveWidthOfFirstRowMember + "," + firstSDVInRow.getActualWidth());
        	double firstRowMemberPartialVisibilityRatio;
        	if(rowStartIndex == 1) {
        		firstRowMemberPartialVisibilityRatio = Math.min(1d,(double)effectiveWidthOfFirstRowMember 
            			/ (double)firstSDVInRow.getPerfectWidth());
        	} else {
        		firstRowMemberPartialVisibilityRatio = (double)effectiveWidthOfFirstRowMember 
        			/ (double)firstSDVInRow.getActualWidth();
        	}
        	
        	header.setPartialVisibilityRatio( firstRowMemberPartialVisibilityRatio );
        	
        	// Now, we must resolve the height of the row.
        	// Note we use rowStartIndex + 1 to exclude the first row member (and subtract effectiveWidthOfFirstRowMember)
        	// Generally we will NOT affect the VerticalStaffSpec of the first view in a row.  The only
        	// exception is the very first row; the conditional for this is below.
        	Pair< LinkedList<ScoreDeltaView>, Integer> p = getActualRow( rowStartIndex + 1, 
        			getMeasuredWidth() - header.deriveLayoutWidth() - effectiveWidthOfFirstRowMember);
        	
        	// This gives us the row EXCLUDING the first element of it.
        	LinkedList<ScoreDeltaView> rowNoFirst = p.first;
        	//List<ScoreDeltaView> rowNoFirst = row.subList(1, row.size());
        	if(rowNoFirst.size() == 0) {
        		firstSDVInRow.measure(widthMeasureSpec, heightMeasureSpec);
        		break;
        	}
        	
        	VerticalStaffSpec[] rowInternalBestStaffSpec = VerticalStaffSpec.best(rowNoFirst);
        	
        	//Log.i(TAG,"Best for row " + systemNumber + ": " + rowInternalBestStaffSpec[0].toString());
        	
        	// Now adjust for the first element 
        	VerticalStaffSpec[] rowAdjustedStaffSpec = 
        			VerticalStaffSpec.influenceToBest(firstSDVInRow.getPerfectVerticalStaffSpecs(), rowInternalBestStaffSpec,
        					firstRowMemberPartialVisibilityRatio);
        	
        	//Log.i(TAG,"with First: " + rowAdjustedStaffSpec[0].toString() + "@" + firstRowMemberPartialVisibilityRatio);
        	
        	// Finally see if the remaining space is going to be filled with an incoming View.  In that
        	// case, it must also affect the overall row StaffSpec
        	ScoreDeltaView incoming = (ScoreDeltaView)getChildAt(rowStartIndex + rowNoFirst.size() + 1);
        	if(incoming != null) {
        		rowAdjustedStaffSpec = VerticalStaffSpec.influenceToBest(incoming.getPerfectVerticalStaffSpecs(), rowAdjustedStaffSpec, 
        				(double)p.second / (double)incoming.getActualWidth());
        	}
        	//Log.i(TAG,"with incoming: " + rowAdjustedStaffSpec[0].toString());
        	
        	// Apply our adjusted spec across the row
        	for(ScoreDeltaView sdv : rowNoFirst) {
        		//Log.i(TAG, "VSS set in row " + systemNumber + 
        		//		" to " + rowAdjustedStaffSpec[0].toString() + " at " + sdv._scoreDelta.LOCATION.toMixedString());
        		sdv.setActualVerticalStaffSpecs(rowAdjustedStaffSpec);
        		sdv.measure(widthMeasureSpec, heightMeasureSpec);
        	}
        	if(incoming != null) {
        		//Log.i(TAG,"measured incoming in system " + systemNumber);
        		ScoreDeltaView afterIncoming = (ScoreDeltaView)getChildAt(rowStartIndex + rowNoFirst.size() + 2);
        		VerticalStaffSpec[] customIncomingStaffSpec;
        		if(afterIncoming != null) {
        			customIncomingStaffSpec = VerticalStaffSpec.influenceLeftRight(
            				rowAdjustedStaffSpec, afterIncoming.getActualVerticalStaffSpecs(),
            				(double)(p.second) / (double)incoming.getActualWidth());
        		} else {
        			customIncomingStaffSpec = VerticalStaffSpec.influenceLeftRight(
            				rowAdjustedStaffSpec, incoming.getPerfectVerticalStaffSpecs(),
            				(double)(p.second) / (double)incoming.getActualWidth());
        		}
        		/*VerticalStaffSpec[] asdf = (ScoreDeltaView)getChildAt(rowStartIndex + rowNoFirst.size() + 2)
        		VerticalStaffSpec[] customIncomingStaffSpec = VerticalStaffSpec.influenceToBest(
        				rowAdjustedStaffSpec, incoming.getActualVerticalStaffSpec(),
        				(double)(p.second) / (double)incoming.getActualWidth());*/
	        	//incoming.setActualVerticalStaffSpecs(rowAdjustedStaffSpec);
        		incoming.setActualVerticalStaffSpecs(customIncomingStaffSpec);
	        	incoming.measure(widthMeasureSpec, heightMeasureSpec);
	        	finalPartialHeader = (SystemHeaderView) _surface.getChildAt(systemNumber+1);
	        	if(finalPartialHeader == null) {
	        		//Log.i(TAG,"Added new header for incoming");
	        		finalPartialHeader = _surface.new SystemHeaderView(getContext());
	        		_surface.addView(finalPartialHeader, systemNumber + 1);
	        	}
	        	finalPartialHeader.setPartialDelta(incoming);
        		finalPartialHeader.setPartialVisibilityRatio((double)(incoming.getActualWidth() - p.second)/(double)(incoming.getMeasuredWidth()));
        	} else {
        		finalPartialHeader = null;
        		for(int s = systemNumber + 1; s < _surface.getChildCount(); s++)
        			_surface.removeViewAt(s);
        	}
        	
        	// The first View in this is our boundary case.  The first View of every other row is set with that of
        	// the previous.
        	if(rowStartIndex == 1) {
        		firstSDVInRow.setActualVerticalStaffSpecs(rowAdjustedStaffSpec);
        		firstSDVInRow.measure(widthMeasureSpec, heightMeasureSpec);
        	}
        	
        	// Measure the header, which bases its life on the first and second members of this row.
        	header.measure(widthMeasureSpec, heightMeasureSpec);
        	
        	rowStartIndex += rowNoFirst.size() + 1;
        	
        	if(rowStartIndex < getChildCount())
        		effectiveWidthOfFirstRowMember = incoming.getActualWidth() - p.second;
        	//final ScoreDeltaView child = (ScoreDeltaView) getChildAt(rowStartIndex);
            //child.measure(widthMeasureSpec, heightMeasureSpec);
        }
        if(finalPartialHeader != null)
        	finalPartialHeader.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        _surface.layout(l, t, r, b);

        
        int systemNumber = 1;
        int effectiveWidthOfFirstRowMember = ((ScoreDeltaView) getChildAt(1)).getActualWidth();
        
        int left = 0, top = 0;
        // We will work a system at a time, setting rowStartIndex
        for (int rowStartIndex=1; rowStartIndex < getChildCount(); systemNumber++) {
        	SystemHeaderView header = (SystemHeaderView)_surface.getChildAt(systemNumber);
        	header.layout(0, top, header.getMeasuredWidth(), top + header.getMeasuredHeight());
        	left += header.getMeasuredWidth();
        	
        	ScoreDeltaView firstSDVInRow = (ScoreDeltaView)getChildAt(rowStartIndex);
        	//ScoreDeltaView secondSDVInRow = (ScoreDeltaView)getChildAt(rowStartIndex + 1);
        	Pair< LinkedList<ScoreDeltaView>, Integer> p = getActualRow( rowStartIndex + 1, 
        			getMeasuredWidth() - header.getMeasuredWidth() - effectiveWidthOfFirstRowMember);
        	
        	LinkedList<ScoreDeltaView> rowNoFirst = p.first;
        	
        	if(rowStartIndex == 1) {
        		firstSDVInRow.layout(left, top, 
            			left + firstSDVInRow.getMeasuredWidth(), top + firstSDVInRow.getMeasuredHeight());
        		left += firstSDVInRow.getMeasuredWidth();
        	} else {
        		left += effectiveWidthOfFirstRowMember;
        	}
        	
        	for(ScoreDeltaView v : rowNoFirst) {
        		v.layout(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
        		left += v.getMeasuredWidth();
        	}
        	
        	ScoreDeltaView incoming = (ScoreDeltaView)getChildAt(rowStartIndex + rowNoFirst.size() + 1);
        	if(incoming != null) {
        		double d = (double)p.second / (double)incoming.getMeasuredWidth();
        		//int incL = _surface.getChildAt(systemNumber+1).getMeasuredWidth()
        		//		+ (int)(left * d);
        		SystemHeaderView nextHeader = (SystemHeaderView) _surface.getChildAt(systemNumber+1);
        		//Log.i(TAG,"incoming layout..., nextHeader: " + nextHeader.getMeasuredWidth() + "," + nextHeader.getMeasuredHeight());
        		int incL = left - (int)((left - nextHeader.getMeasuredWidth()) * (1d-d));
        		int incT = (int)(top + (header.getMeasuredHeight() * (1d-d)));
        		incoming.layout(incL, incT, incL+incoming.getMeasuredWidth(), incT+incoming.getMeasuredHeight());
        	}
        	
        	// Prepare for the next time around the loop!
        	left = 0;
        	top += header.getMeasuredHeight();
        	rowStartIndex += rowNoFirst.size() + 1;
        	
        	if(rowStartIndex < getChildCount())
        		effectiveWidthOfFirstRowMember = incoming.getMeasuredWidth() - p.second;
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
    Long __prevMotionTime = null;
    Float __prevVelX = null;
    Double __prev2FingerDistance = null;
    LinkedList<AsyncTask<?,?,?>> __kineticOperations = new LinkedList<AsyncTask<?,?,?>>();
    @Override
    public boolean onTouchEvent(MotionEvent event) {    	
    	// 1 finger to scroll
    	if(event.getPointerCount() == 1) {
    		Log.i(TAG, "One finger");
			for(AsyncTask<?,?,?> t : __kineticOperations) {
				t.cancel(true);
			}
			__kineticOperations.clear();
    		switch(event.getActionMasked()) {
	    		case MotionEvent.ACTION_DOWN:
	    		case MotionEvent.ACTION_POINTER_DOWN:
		    		__prevMotionX = event.getX();
		    		__prevMotionTime = System.currentTimeMillis();
		    		return true;
	    		case MotionEvent.ACTION_MOVE:
		    		if(__prevMotionX != null) {
		    			float dxFloat = event.getX() - __prevMotionX;
		    			float dt_seconds = (float)(System.currentTimeMillis() - __prevMotionTime)/1000f;
			    		__prevMotionTime = System.currentTimeMillis();
		    			__prevVelX = dxFloat/dt_seconds;
		    			
		    			final int dx = (int) dxFloat;
		
		    			Log.i(TAG,"Move of x" + event.getX() + "," + __prevMotionX + " dx="+dx);
		    			
		    			//Scroll forwards (right to left swipe)
				        if (dx < 0) {
						   scrollLeftBy(-dx);
				        // Scroll backwards (left to right swipe)
				        } else if(dx > 0) {
				        	scrollRightBy(dx);
				        }
		    		}
		    		__prevMotionX = event.getX();
			        return true;
	    		case MotionEvent.ACTION_UP: 
	            case MotionEvent.ACTION_POINTER_UP: 
	            case MotionEvent.ACTION_CANCEL: 
		    		if(__prevVelX != null) {
		    			final float myVelX = __prevVelX;
		    			__kineticOperations.add( new AsyncTask<Object,Object,Object>() {
		    				boolean isCancelled = false;
		    				@Override
		    				protected void onCancelled() {
		    					isCancelled = true;
		    				}
		    				
							@Override
							protected Object doInBackground(Object... params) {
								float velX = myVelX;
								float deceleration;
								if(velX > 0)
									deceleration= 2000f*(float)getScalingFactor();
								else
									deceleration = -2000f*(float)getScalingFactor();
								long prevTime = System.currentTimeMillis();
								float accumDX = 0;
								int totalScrolled = 0;
								while(Math.abs(velX) > 0 && !isCancelled) {
									long now = System.currentTimeMillis();
									float dt_seconds = (float)(now - prevTime) / 1000f;
									prevTime = now;
									float dx = velX * dt_seconds - deceleration * dt_seconds * dt_seconds / 2;
									accumDX += dx;
									final int toScroll = ((int)accumDX) - totalScrolled;
									totalScrolled += toScroll;
									if(toScroll < 0) {
										post(new Runnable() {
											@Override
											public void run() {
												scrollLeftBy(-toScroll);
											}
										});
									} else if(toScroll > 0) {
										post(new Runnable() {
											@Override
											public void run() {
												scrollRightBy(toScroll);
											}
										});
									}
									velX -= deceleration * dt_seconds;
									Log.i(TAG,"Elastic"+velX+","+dx);
									if((deceleration > 0 && velX < 0) || (deceleration < 0 && velX > 0))
										break;
									try {
										Thread.sleep(10);
									} catch (InterruptedException e) {}
								}

								return null;
							}
							
							@Override
							protected void onPostExecute(Object result) {
								post(new Runnable() {
									@Override
									public void run() {
										fixLayout();
									}
								});
							}
		    				
		    			}.execute());
		    		}
		    		fixLayout();
		    		
		    		__prevVelX = null;
		    		__prevMotionX = null;
		    		__prevMotionTime = null;
		    	    __prev2FingerDistance = null;
	    	}
	    	
	    // 2 fingers to zoom
    	} else if(event.getPointerCount() == 2 ) {
    		Log.i(TAG,"Two Fingers");
    		double newDistance = Math.sqrt(( (event.getX(1)-event.getX(0)) * (event.getX(1)-event.getX(0)) ) + 
    										( (event.getY(1)-event.getY(0)) * (event.getY(1)-event.getY(0)) ));
    		if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
    			__prev2FingerDistance = newDistance;
    		} else if(event.getActionMasked() == MotionEvent.ACTION_MOVE) {
	    		disableTransitions();
	    		if(__prev2FingerDistance != null) {
	    			double distanceRatioToPrev = newDistance/__prev2FingerDistance;
	    			setScalingFactor(getScalingFactor() * distanceRatioToPrev);
	    			Log.i(TAG,"Zoomed to scaling factor" + getScalingFactor());
	    		}
	    		enableTransitions();
	    		__prev2FingerDistance = newDistance;
		        return true;
	    	} else if(event.getActionMasked() == MotionEvent.ACTION_UP) {
	    		__prev2FingerDistance = null;
	    		//__prevVelX = null;
	    	}
    	}
		return false;
	}
    
    
    private void scrollLeftBy(final int dx) {
		if(getChildCount() == 2)
    		return;
    	//Remove stuff from the top left
		int removedFromLeft = 0;
        //Remove as many views as possible.
        while(removedFromLeft + ((ScoreDeltaView)getChildAt(1)).getActualWidth() < dx && getChildCount() > 2) {
        	removedFromLeft += ((ScoreDeltaView)getChildAt(1)).getActualWidth();
		    removeViewAt(1);
        }
        //Shorten the first view to complete the job.
        if(getChildCount() > 2) {
	        int leftToRemove = dx - removedFromLeft;
	        ScoreDeltaView first = (ScoreDeltaView)getChildAt(1);
	        first.setActualWidth(first.getActualWidth() - leftToRemove);
	        removedFromLeft += leftToRemove;
        }
        
        
        //Add stuff to the bottom right
		ScoreDeltaView lastVisible = (ScoreDeltaView)getChildAt(getChildCount()-1);
        Iterator<ScoreDelta> itr = _score.scoreIterator(lastVisible._scoreDelta.LOCATION, false);
        int leftToAdd = removedFromLeft;
        while(itr.hasNext() && leftToAdd > 0) {
    		ScoreDelta d = itr.next();
    		ScoreDeltaView sdv = new ScoreDeltaView(getContext(), this);
			sdv.setScoreDelta(d);
			int addable = sdv.getPerfectWidth();
			if(leftToAdd <= addable) {
				sdv.setActualWidth(leftToAdd);
				addView(sdv,getChildCount());
				break;
			} else {
				sdv.setActualWidth(addable);
				addView(sdv,getChildCount());
				leftToAdd -= addable;
			}
        }
		requestLayout();
    }
    
    private void scrollRightBy(final int dx) {
    	final ScoreDeltaView first = (ScoreDeltaView)getChildAt(1);
    	Log.i(TAG,"Right scroll, first view at " +first._scoreDelta.LOCATION + 
    			":" + first.getPerfectWidth() + "," + first.getActualWidth());
    	
		int leftToAdd = dx;
    	if(first.getActualWidth() < first.getPerfectWidth()) {
    		int addable = first.getPerfectWidth() - first.getActualWidth();
    		int toAdd = Math.min(leftToAdd, addable);
    		Log.i(TAG,"Right scroll " + toAdd);
    		first.setActualWidth(first.getActualWidth() + toAdd);
    		leftToAdd -= toAdd;
    	}
    	
    	Log.i(TAG, "Right scroll must fill " + leftToAdd + " with new Views");
    	
    	Rational startingPoint = first._scoreDelta.LOCATION;
    	Iterator<Score.ScoreDelta> itr = _score.reverseScoreIterator(startingPoint, false);
    	while(itr.hasNext() && leftToAdd > 0) {
    		ScoreDelta d = itr.next();
    		ScoreDeltaView sdv = new ScoreDeltaView(getContext(), this);
        	Log.i(TAG,"Going backwards got " + d.LOCATION.toMixedString() + d.STAVES[0].VOICES[0].ESTABLISHED.NOTES);
			sdv.setScoreDelta(d);
			int addable = sdv.getPerfectWidth();
			if(leftToAdd <= addable) {
				sdv.setActualWidth(leftToAdd);
				addView(sdv,1);
				break;
			} else {
				sdv.setActualWidth(addable);
				addView(sdv,1);
				leftToAdd -= addable;
			}
    	}
    	
		requestLayout();
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
    	/*int myWidth = getMeasuredWidth();
    	
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
    	}*/
    }
    
    @Override
    public boolean shouldDelayChildPressedState() {
    	return true;
    }
    
    public void removeFirstElement() {
    	removeViewAt(1);
    }
    
    public double getScalingFactor() {
    	return SCALINGFACTOR;
    }
    
    public void setScalingFactor(double d) {
    	d = Math.min(MAX_SCALE, Math.max(MIN_SCALE, d));
    	double newToOld = d/SCALINGFACTOR;
    	for(int i = 1; i < getChildCount(); i++) {
    		ScoreDeltaView sdv = (ScoreDeltaView) getChildAt(i);
    		double actualToPerfect = sdv.getActualWidth() / sdv.getPerfectWidth();
    		if(actualToPerfect == 0) {
    			actualToPerfect = 1;
    		}
    		//Log.i(TAG,"Ratio " + newToOld + "*" +actualToPerfect + "=" + (newToOld*actualToPerfect));
    		//sdv.setActualWidth((int) (newToOld * actualToPerfect * sdv.getActualWidth()));
    		//Log.i(TAG, "Scaled HSS: " 
    		//		+ HorizontalStaffSpec.scale(sdv.getPerfectHorizontalStaffSpec(), newToOld * actualToPerfect).toString()
    		//		+ " @scale" + newToOld + "*" +actualToPerfect + "=" + (newToOld*actualToPerfect));
    		sdv.setActualHorizontalStaffSpec(
    				HorizontalStaffSpec.scale(sdv.getPerfectHorizontalStaffSpec(), newToOld * actualToPerfect)
    			);
    	}
    	SCALINGFACTOR = d;
    	requestLayout();
    }
}


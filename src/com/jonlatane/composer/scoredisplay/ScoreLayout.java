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

import java.util.Random;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * A ScoreLayout is a self-managing ViewGroup that renders {@link SystemSliceView}s
 * containing {@link SystemSliceView.ScoreDeltaView}s which in turn interact with
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
    int mCellWidth;
    int mCellHeight;
    
    private Score _score;
    private Rational _topLeft;
    private double _animationOffset = 0;
    
    Animator defaultAppearingAnim, defaultDisappearingAnim;
    Animator defaultChangingAppearingAnim, defaultChangingDisappearingAnim;
    
    private class SystemSliceView extends LinearLayout {
    	private Rational R;
    	private int widthMult = 150;
    	private int butHeight = 50;
    	int rand;
    	
    	public class ScoreDeltaView extends LinearLayout {
    		public ScoreDeltaView(Context context) {
    			super(context);
    		}
    		
    		public ScoreDeltaView(Context context, AttributeSet attrs) {
    			super(context, attrs);
    		}
    		
			public ScoreDeltaView(Context context, AttributeSet attrs, int defStyle) {
				super(context, attrs, defStyle);
				// TODO Auto-generated constructor stub
			}
    		
    	}
    	
    	public SystemSliceView(Context context ) {
    		super(context);
    		init();
    	}
    	
    	/*public SystemSliceView(Context context, AttributeSet attrs) {
    		super(context, attrs);
    	}
    	
		public SystemSliceView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			// TODO Auto-generated constructor stub
		}*/
		
		private void init() {
			rand = Math.max(1, new Random().nextInt(3));
			for(int i = 0; i < 2; i ++) {
				Button b = new Button(getContext());
			}
		}
    	
		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(resolveSize(rand*widthMult,widthMeasureSpec), 
					resolveSize(butHeight * 3, heightMeasureSpec));
		}
		
		@Override
		public void onLayout(boolean changed, int l, int t, int r, int b) {
			super.onLayout(changed, l, t, r, b);
			/*for(int i = 0; i < getChildCount(); i++) {
				Button button = (Button)getChildAt(i);
				ViewGroup.LayoutParams p = button.getLayoutParams();
				p.height = butHeight;
				p.width = r * widthMult;
				button.setLayoutParams(p);
			}*/
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
    	setCellWidth(100);
    	setCellHeight(50);
    	
	    final LayoutTransition transitioner = new LayoutTransition();
	    setLayoutTransition(transitioner);
	    defaultAppearingAnim = transitioner.getAnimator(LayoutTransition.APPEARING);
	    defaultDisappearingAnim =
	            transitioner.getAnimator(LayoutTransition.DISAPPEARING);
	    defaultChangingAppearingAnim =
	            transitioner.getAnimator(LayoutTransition.CHANGE_APPEARING);
	    defaultChangingDisappearingAnim =
	            transitioner.getAnimator(LayoutTransition.CHANGE_DISAPPEARING);
	    
	    transitioner.setAnimator(LayoutTransition.APPEARING,  defaultAppearingAnim);
	    transitioner.setAnimator(LayoutTransition.DISAPPEARING, defaultDisappearingAnim);
	    transitioner.setAnimator(LayoutTransition.CHANGE_APPEARING, defaultChangingAppearingAnim);
	    transitioner.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, defaultChangingDisappearingAnim);
	    
	    for(int i = 0; i < 15; i++) {
	    	addElementToBeginning();
	    }
    }
    
    public void setCellWidth(int px) {
        mCellWidth = px;
        requestLayout();
    }

    public void setCellHeight(int px) {
        mCellHeight = px;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //int cellWidthSpec = MeasureSpec.makeMeasureSpec(mCellWidth,
        //        MeasureSpec.AT_MOST);
        //int cellHeightSpec = MeasureSpec.makeMeasureSpec(mCellHeight,
        //        MeasureSpec.AT_MOST);

        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);
            //child.measure(cellWidthSpec, cellHeightSpec);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }
        // Use the size our parents gave us, but default to a minimum size to avoid
        // clipping transitioning children
        int minCount =  count > 3 ? count : 3;
        setMeasuredDimension(resolveSize(mCellWidth * minCount, widthMeasureSpec),
                resolveSize(mCellHeight * minCount, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cellWidth = mCellWidth;
        int cellHeight = mCellHeight;
        int columns = (r - l) / cellWidth;
        if (columns < 0) {
            columns = 1;
        }
        int x = 0;
        int y = 0;
        int i = 0;
        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);

            int w = child.getMeasuredWidth();
            int h = child.getMeasuredHeight();

            int left = x + ((cellWidth-w)/2);
            int top = y + ((cellHeight-h)/2);

            child.layout(left, top, left+w, top+h);
            if (i >= (columns-1)) {
                // advance to next row
                i = 0;
                x = 0;
                y += cellHeight;
            } else {
                i++;
                x += cellWidth;
            }
        }
    }
    
    public void addElementToBeginning() {
    	SystemSliceView slice = new SystemSliceView(getContext());
    	addView(slice, 0);
    }
    public void addElementToEnd() {
    	SystemSliceView slice = new SystemSliceView(getContext());
    	addView(slice,getChildCount());
    }
    public void removeFirstElement() {
    	removeViewAt(0);
    }
    public void removeLastElement() {
    	removeViewAt(getChildCount());
    }
}


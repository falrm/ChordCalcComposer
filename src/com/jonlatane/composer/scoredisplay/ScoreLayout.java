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
 * A layout that arranges its children in a grid.  The size of the
 * cells is set by the {@link #setCellSize} method and the
 * android:cell_width and android:cell_height attributes in XML.
 * The number of rows and columns is determined at runtime.  Each
 * cell contains exactly one view, and they flow in the natural
 * child order (the order in which they were added, or the index
 * in {@link #addViewAt}.  Views can not span multiple cells.
 *
 * <p>This class was copied from the FixedGridLayout Api demo; see that demo for
 * more information on using the layout.</p>
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


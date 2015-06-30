package com.jonlatane.composer.io;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.jonlatane.composer.NonDelayedHorizontalScrollView;

public class KeyboardScroller extends NonDelayedHorizontalScrollView {
	@SuppressWarnings("unused") private static String TAG = "KBScroller";
	//private KeyboardIOHandler _io = null;	
	
	public static int MARGIN = 40;
	
	public KeyboardScroller(Context context) {
		super(context);
		onCreate(context);
	}
	public KeyboardScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context);
	}
	
	public KeyboardScroller(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate(context);
	}
	
	void onCreate(Context c){
	}

	@Override public boolean onTouchEvent(MotionEvent event) {
		if(event.getX() < MARGIN || event.getX() > getWidth() - MARGIN)
			enableScrolling();
		
		if(_enableScrolling && event.getPointerCount() < 2)
			super.onTouchEvent(event);
		
		if(event.getActionMasked() == MotionEvent.ACTION_UP && event.getPointerCount() < 2)
			disableScrolling();
		return true;
	}
	@Override public boolean onInterceptTouchEvent(MotionEvent event) {
		if(event.getActionMasked() == MotionEvent.ACTION_DOWN 
				&& event.getX() < MARGIN || event.getX() > getWidth() - MARGIN)
			return true;
		return false;
	}
	
	/*
	 * onDraw and these types and variables are used to render the drop shadows on the right and left.
	 */
	private int _leftShadowAlpha = 255, _rightShadowAlpha = 255;
	private class LeftShadowEvaluator extends IntEvaluator {
	    private KeyboardScroller v;
	    public LeftShadowEvaluator(KeyboardScroller v) {
	        this.v = v;
	    }
		@Override
	    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
	        int num = (Integer)super.evaluate(fraction, startValue, endValue);
	        v._leftShadowAlpha = num;
	        return num;
	    }
	}
	private class RightShadowEvaluator extends IntEvaluator {
	    private KeyboardScroller v;
	    public RightShadowEvaluator(KeyboardScroller v) {
	        this.v = v;
	    }
		@Override
	    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
	        int num = (Integer)super.evaluate(fraction, startValue, endValue);
	        v._rightShadowAlpha = num;
	        return num;
	    }
	}
	@Override protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		if(canScrollHorizontally(-1))
			ValueAnimator.ofObject(new LeftShadowEvaluator(this), _leftShadowAlpha, 255).start();
		else
			ValueAnimator.ofObject(new LeftShadowEvaluator(this), _leftShadowAlpha, 255).start();
		if(canScrollHorizontally(1))
			ValueAnimator.ofObject(new RightShadowEvaluator(this), _rightShadowAlpha, 255).start();
		else
			ValueAnimator.ofObject(new RightShadowEvaluator(this), _rightShadowAlpha, 255).start();
	}
	
	private boolean _enableScrolling = false;
	public void enableScrolling() {
		_enableScrolling = true;
	}
	public void disableScrolling() {
		_enableScrolling = false;
	}
}
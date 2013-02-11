package com.jonlatane.composer.input;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class KeyboardScroller extends HorizontalScrollView {
	private String TAG = "KBScroller";
	private KeyboardIOHandler _io = null;
	public KeyboardScroller(Context context) {
		super(context);
	}
	public KeyboardScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public KeyboardScroller(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}
	
	public void setKeyboardIOHander(KeyboardIOHandler kio) {
		_io = kio;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = true;
		super.onTouchEvent(event);
		
		Log.i(TAG, "onTouchEvent Scroller");
		
		if(_io != null) {
			_io.catchRogues();
			
		}
		return result;
	}
}
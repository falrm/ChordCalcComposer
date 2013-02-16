package com.jonlatane.composer.input;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.HorizontalScrollView;

public class KeyboardScroller extends HorizontalScrollView {
	private String TAG = "KBScroller";
	private KeyboardIOHandler _io = null;
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
		  //Observe for a layout change
		  ViewTreeObserver viewTreeObserver = getViewTreeObserver();
		  if (viewTreeObserver.isAlive()) {
		    /*viewTreeObserver.addOnLayoutChangeListener(new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top,
						int right, int bottom, int oldLeft, int oldTop,
						int oldRight, int oldBottom) {
					_io.catchRogues();
					
				}
			});*/
		  }
	
			    
		}
	
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}
	
	public void setKeyboardIOHander(KeyboardIOHandler kio) {
		_io = kio;
	}

	public void disableScrolling() {
		setOnTouchListener( new OnTouchListener(){ 
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        return true; 
		    }
		});
	}
	
	public void enableScrolling() {
		setOnTouchListener(null);
	}
}
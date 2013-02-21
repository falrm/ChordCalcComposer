package com.jonlatane.composer.input;

import java.util.Iterator;
import java.util.LinkedList;

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
import android.widget.ToggleButton;

public class KeyboardScroller extends HorizontalScrollView {
	private static String TAG = "KBScroller";
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

	private int _scrollPosition = 0;
	private boolean _scrollingEnabled = true;
	private void disableScrolling() {
		Log.i(TAG, "Disabled Scrolling");
		_scrollPosition = getScrollX();
		setOnTouchListener( new OnTouchListener(){ 
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	logUpDown(event);
		    	smartToggleScrolling();
		        return true; 
		    }
		});
		_scrollPosition = getScrollX();
    	scrollTo(_scrollPosition, 0);
		_scrollingEnabled = false;
	}
	private void enableScrolling() {
		Log.i(TAG, "Enabled Scrolling");
		setOnTouchListener(null);
    	scrollTo(_scrollPosition, 0);
		_scrollingEnabled = true;
	}
		
	private boolean smartToggleScrolling() {
		if(!_scrollingEnabled) {
			if( getUpDownFrequency(1000) < 6.0 ) {
				enableScrolling();
			}
		} else {
			if( getUpDownFrequency(1000) > 0.25 ) {
				disableScrolling();
			}
		}
		
		return _scrollingEnabled;
	}
	
	private LinkedList<Long> previousUpDowns = new LinkedList<Long>();
	private float getUpDownFrequency(long window) {
		long now = System.currentTimeMillis();
		
		Iterator<Long> itr = previousUpDowns.iterator();
		int numPressesInWindow = 0;
		while(itr.hasNext()) {
			long l = itr.next();
			if( now - l > window ) {
				itr.remove();
			} else {
				numPressesInWindow++;
			}
		}
		if(numPressesInWindow < 2)
			return 0;
		return (float)numPressesInWindow/((float)window/1000);
	}
	private void logUpDown(MotionEvent e) {
		if(e.getActionMasked() == MotionEvent.ACTION_DOWN || e.getActionMasked() == MotionEvent.ACTION_UP) {
			previousUpDowns.add(System.currentTimeMillis());
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent e) {
			boolean result = super.onTouchEvent(e);
			//logUpDown(e);
			//smartToggleScrolling();
			return result;
		}
	}
package com.jonlatane.composer.io;

import com.jonlatane.composer.NonDelayedHorizontalScrollView;
import com.jonlatane.composer.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class KeyboardScroller extends NonDelayedHorizontalScrollView {
	private static String TAG = "KBScroller";
	private KeyboardIOHandler _io = null;	
	
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
	
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}
	
	public void setKeyboardIOHander(KeyboardIOHandler kio) {
		_io = kio;
	}

	@Override public boolean onTouchEvent(MotionEvent event) {
		if(event.getX() < MARGIN || event.getX() > getWidth() - MARGIN)
			enableScrolling();
		
		if(_enableScrolling && event.getPointerCount() < 2)
			super.onTouchEvent(event);
		
		if(event.getActionMasked() == event.ACTION_UP && event.getPointerCount() < 2)
			disableScrolling();
		return true;
	}
	@Override public boolean onInterceptTouchEvent(MotionEvent event) {
		boolean result = onTouchEvent(event);
		if(event.getActionMasked() == event.ACTION_DOWN 
				&& event.getX() < MARGIN || event.getX() > getWidth() - MARGIN)
			return true;
		return false;
	}
	
	private boolean _enableScrolling = false;
	public void enableScrolling() {
		_enableScrolling = true;
	}
	public void disableScrolling() {
		_enableScrolling = false;
	}
	
	// Scrolling handling.  When the user is pressing lots of keys, we may want to disable scrolling
	//
	/*private int _scrollPosition = 0;
	private boolean _scrollingEnabled = true;
	public void disableScrolling() {
		Log.i(TAG, "Disabled Scrolling");
		_scrollPosition = getScrollX();
		setOnTouchListener( new OnTouchListener(){ 
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	//logUpDown(event);
		    	//smartToggleScrolling();
		        return true; 
		    }
		});
		_scrollPosition = getScrollX();
    	scrollTo(_scrollPosition, 0);
		_scrollingEnabled = false;
	}
	public void enableScrolling() {
		Log.i(TAG, "Enabled Scrolling");
		setOnTouchListener(null);
    	scrollTo(_scrollPosition, 0);
		_scrollingEnabled = true;
	}
	boolean smartToggleScrolling() {
		Log.d(TAG,"smartToggleScrolling");
		if(!_scrollingEnabled) {
			if( getUpDownFrequency(1000) < 6.0 ) {
				//enableScrolling();
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
	void logUpDown(MotionEvent e) {
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
	}*/
}
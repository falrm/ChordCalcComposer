package com.jonlatane.composer.io;

import com.jonlatane.composer.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class KeyboardScroller extends HorizontalScrollView {
	private static String TAG = "KBScroller";
	private KeyboardIOHandler _io = null;
	private GestureDetector gd;
	
	
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
		final GestureDetector.OnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
			private float accumVelocityX = 0;
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if( e1 == null || e2 == null) {
					return true;
				}
				Log.i(TAG, "Fling found");
		        int dx = (int) (e2.getX() - e1.getX());
		        int dy = (int) (e2.getY() - e1.getY());
		        
		        // don't accept the fling if it's too short
		        // as it may conflict with a button push
		        if (Math.abs(dx) > findViewById(R.id.keyA0).getWidth() * 2 && Math.abs(velocityX) > Math.abs(velocityY)) {
		        	//_keyboardScroller.enableScrolling();

		        	enableScrolling();
		            fling((int) -(velocityX));
		            
		            new AsyncTask<Integer, Integer, Integer>() {
		                protected Integer doInBackground(Integer... urls) {
		                    try {
								Thread.sleep(700);
							} catch (InterruptedException e) {}
		                    return 0;
		                }

		                protected void onProgressUpdate(Integer... progress) {
		                    
		                }

		                protected void onPostExecute(Integer result) {
		                    disableScrolling();
		                }
		            }.execute(new Integer[]{0});
		            
		            //_keyboardScroller.disableScrolling();
		            return true;
		        } else {
		        	//accumVelocityX += velocityX;
		            return true;
		        }
		    }
		};
		gd = new GestureDetector(getContext(), gl);
		post(new Runnable() {
			public void run() {
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
				}
				smoothScrollTo(20 * findViewById(R.id.keyA0).getWidth(), 0);
			}
		});
	}
	
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}
	
	public void setKeyboardIOHander(KeyboardIOHandler kio) {
		_io = kio;
	}

	@Override public boolean onTouchEvent(MotionEvent event) {
		if(!_enableScrolling) {
			gd.onTouchEvent(event);
			return true;
		} else {
			return super.onTouchEvent(event);
		}
	}
	@Override public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(!_enableScrolling) {
			return gd.onTouchEvent(ev); 
		} else {
			return super.onInterceptTouchEvent(ev);
		}
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
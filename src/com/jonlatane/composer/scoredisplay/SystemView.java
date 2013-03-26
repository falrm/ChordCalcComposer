package com.jonlatane.composer.scoredisplay;

import java.util.*;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.LinearLayout;

import com.jonlatane.composer.music.*;
import com.jonlatane.composer.R;

/**
 * The SystemView is the core of music rendering.  Each system contains a pointer
 * to the Score.  At its heart, the SystemView is a vertical LinearLayout of StaffViews.
 * It also keeps its child StaffViews' scrolling and element width information synchronized.
 * @author Jon
 *
 */
public class SystemView extends LinearLayout {
	private SuperScore _score;
	private Rational _startingPoint;
	private StaffView[] _staves;
	
	public SystemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public StaffView newStaff() {
		SuperScore.Staff s = _score.newStaff();
		StaffView result = (StaffView)LayoutInflater.from(getContext()).inflate(R.layout.staffview, null);
		//StaffView result = new StaffView(getContext());
		result.setStaff(s);
		addView(result);
		return result;
	}
	
	private void setupStaffView( final StaffView sv ) {
		/*sv.getViewTreeObserver().addOnScrollChangedListener(new OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				for(StaffView v : _staves) {
					if( v != sv ) {
						//v.getViewTreeObserver().
						v.scrollTo(sv.getScrollX(),0);
						v.
					}
				}
			}
		});*/
		/*sv.setOnTouchListener(new OnTouchListener() {
			public boolean onTouchEvent(View v, MotionEvent event) {
			    switch (event.getAction()) {
			        case MotionEvent.ACTION_MOVE:
			            //lv2.onScrollY(lv.getScrollY());
			        	for( StaffView k : _staves )
			        		continue;
			            break;
			        default:
			            break;
			    }
			    return false;
			}
		});*/
	}
}

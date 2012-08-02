package com.jonlatane.composer.view;

import android.widget.*;
import android.content.*;
import android.util.*;
import android.view.*;

import com.jonlatane.composer.R;
import com.jonlatane.composer.music.*;

public class SegmentView extends HorizontalScrollView
{
	private LinearLayout _linear;
	private Segment _segment;

	public SegmentView(Context context) {
		super(context);
		init(context);
	}
	

	public SegmentView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init(context);
	}

	public SegmentView(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
		init(context);
	}
	
	void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.segmentview, this, true);
		_linear = (LinearLayout)findViewById(R.id.segmentLinearLayout);
	}
	
	public Segment getSegment() {
		return _segment;
	}
	
	LinearLayout getLayout() {
		return _linear;
	}
	
	// Uses the reported widths of this view and those after it in the array
	// to smooth scroll the views of arrays *after* it 
	private void syncScrolling(SegmentView[] arr, int myPosition) {
		assert(myPosition < arr.length);
		assert(arr[myPosition] == this);
		
		for(int i = myPosition; i < arr.length; i++) {
			SegmentView sv = arr[i];
			assert(sv.getLayout()==this.getLayout());
			
			int x = sv.getScrollX();
			int y = sv.getScrollY();
			int length = sv.getWidth();
			if(i+1<arr.length)
				arr[i+1].smoothScrollTo(x+length, y);
		}
	}
	
	// Uses the above assuming this is the first view in the array (for convenience)
	// So manage memory by resizing the array
	public void syncScrolling(SegmentView[] arr) {
		syncScrolling(arr, 0);
	}
	
	public  SegmentView spawnChild() {
		return new SegmentView(getContext());
	}
}

package com.jonlatane.composer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class NonDelayedScrollView extends ScrollView {
	public NonDelayedScrollView(Context context) {
		super(context);
	}
	public NonDelayedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public NonDelayedScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}
}
package com.jonlatane.composer.graphics;

import android.widget.ScrollView;
import android.content.*;
import android.util.*;

public class SystemScrollView extends ScrollView
{
	private SystemScrollView[] views;
	
	SystemScrollView(Context context) {
		super(context);
	}
	
	SystemScrollView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}
	
	SystemScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}
}

package com.jonlatane.composer.view;

import android.view.*;
import android.widget.*;
import android.content.*;
import android.util.*;

/*
* A scrollable view for generic other views (derived from functions on rhythmic objects)
*/
public class ScrollableRhythmView<K extends View> extends ScrollView
{
	public ScrollableRhythmView(Context context) {
		super(context);
	}

	public ScrollableRhythmView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	public ScrollableRhythmView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}
}

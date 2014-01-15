package com.jonlatane.composer.scoredisplay2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class StaffCenteredView extends View {
	public int CENTER = 50;

	public StaffCenteredView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public StaffCenteredView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public StaffCenteredView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public abstract int spaceNeededAbove();
	public abstract int spaceNeededBelow();
}

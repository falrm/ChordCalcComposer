package com.jonlatane.composer.view;

import android.view.*;
import android.widget.*;
import android.content.*;
import android.util.*;
import com.jonlatane.composer.music.*;
public class PitchSetView extends SurfaceView
{

	public PitchSetView(Context context) {
		super(context);
	}

	public PitchSetView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	public PitchSetView(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
	}
	
	private Rational location;
	private Voice voice;
	private void invariant() {
		
	}
}

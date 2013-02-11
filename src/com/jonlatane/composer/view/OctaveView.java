package com.jonlatane.composer.view;

import com.jonlatane.composer.R;
import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.harmony.*;

import android.util.*;
import android.view.*;
import android.content.*;
import android.widget.*;
import android.app.*;
import android.os.*;

public class OctaveView extends LinearLayout
{
	OctaveView(Context context) {
		super(context);
		init(context);
	}

	OctaveView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init(context);
	}

	OctaveView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
		init(context);
	}
	void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.octaveview, this, true);
	}

	public PitchSet getSelectedPitches() {
		PitchSet result = new PitchSet();
		//findViewById(R.id( //TODO
		
		return result;
	}

}

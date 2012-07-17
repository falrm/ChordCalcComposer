package com.jonlatane.composer.view;

import android.view.*;
import android.util.*;
import android.widget.*;
import android.content.*;

import java.util.*;

import com.jonlatane.composer.music.*;

public abstract class PitchMultiSelectorView extends View
{
	PitchMultiSelectorView(Context context) {
		super(context);
	}

	PitchMultiSelectorView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	PitchMultiSelectorView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}
	
	public abstract PitchSet getSelectedPitches();
	
	public Chord getSelectedChord() {
		return new Chord(getSelectedPitches());
	}
	public Scale getSelectedScale() {
		return new Scale(getSelectedPitches());
	}
}

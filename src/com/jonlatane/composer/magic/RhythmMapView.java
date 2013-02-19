package com.jonlatane.composer.magic;

import java.util.TreeMap;

import com.jonlatane.composer.music.*;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * A horizontal LinearLayout that expands regions according to a weighting function.
 * 
 * @author Jon
 *
 */
public class RhythmMapView extends LinearLayout {
	private RhythmMap<?> _data;
	private TreeMap<Rational,View> _contents;
	public RhythmMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

}

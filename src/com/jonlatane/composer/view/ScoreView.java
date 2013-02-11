package com.jonlatane.composer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.*;

import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.harmony.*;
import com.jonlatane.composer.music.coverings.*;

public class ScoreView extends SurfaceView {
	private Score _score;
	
	public ScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ScoreView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	void init() {
		_score = new Score();
	}
	
	
}

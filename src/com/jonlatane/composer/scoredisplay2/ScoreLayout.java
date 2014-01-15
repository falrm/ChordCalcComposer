package com.jonlatane.composer.scoredisplay2;

import com.jonlatane.composer.music.Score;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class ScoreLayout extends ViewGroup {
	private static final String TAG = "ScoreLayout";
	private static final float MAX_SCALE = 2, MIN_SCALE = .35f;
	
	Score SCORE;
    float SCALINGFACTOR = 1;
	SystemView[] HEADERS = new SystemView[10];

	public ScoreLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}

}

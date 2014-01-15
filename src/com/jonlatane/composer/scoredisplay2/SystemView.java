package com.jonlatane.composer.scoredisplay2;

import java.util.LinkedList;

import com.jonlatane.composer.scoredisplay2.ScoreDeltaView.StaffDeltaView;

import android.content.Context;
import android.view.*;

public class SystemView extends ViewGroup {
	final LinkedList<ScoreDeltaView> CONTENTS = new LinkedList<ScoreDeltaView>();
	final ScoreLayout _parent;
	// This offset puts the first element a little more to the right
	public int FIRST_ELEMENT_OFFSET = 0;
	public SystemView ABOVE = null;
	public SystemView BELOW = null;

	public SystemView(Context context, ScoreLayout parent) {
		this(context, parent, null, null);
	}
	
	public SystemView(Context context, ScoreLayout parent, SystemView above) {
		this(context, parent, above, null);
	}
	
	public SystemView(Context context, ScoreLayout parent, SystemView above, SystemView below) {
		super(context);
		_parent = parent;
		ABOVE = above;
		BELOW = below;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		
	}
}

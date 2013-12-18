package com.jonlatane.composer.scoredisplay2;

import java.util.LinkedList;

import com.jonlatane.composer.scoredisplay2.ScoreDeltaView.StaffDeltaView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class SystemHeaderView extends View {
	final LinkedList<ScoreDeltaView> CONTENTS = new LinkedList<ScoreDeltaView>();
	final ScoreLayout _parent;

	public SystemHeaderView(Context context, ScoreLayout parent) {
		super(context);
		_parent = parent;
	}

	public void resolveHeights() {
		int[] REQ_ABOVE = new int[_parent.SCORE.getNumStaves()], 
			REQ_BELOW = new int[_parent.SCORE.getNumStaves()];
		for(int i = 0; i < REQ_BELOW.length; i++) {
			REQ_ABOVE[i] = 0; REQ_BELOW[i] = 0;
		}
		for(ScoreDeltaView scoreDv : CONTENTS) {
			for(int i = 0; i < scoreDv.getScoreDelta().STAVES.length; i++) {
				StaffDeltaView staffDv = (StaffDeltaView) scoreDv.getChildAt(i);
				REQ_ABOVE[i] = staffDv.REQ_ABOVE;
				REQ_BELOW[i] = staffDv.REQ_BELOW;
				i++;
			}
		}
	}
}

package com.jonlatane.composer.scoredisplay2;

import com.jonlatane.composer.music.Score.Staff;
import com.jonlatane.composer.music.Score.Staff.Voice;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class StaffView extends View {
	protected Staff _staff;
	protected Voice _splitVoice = null;
	
	StaffView BELOW;
	
	public StaffView(Context context, Staff staff) {
		super(context);
		_staff = staff;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

	}
	
	public void split() {
		assert(_splitVoice == null);
		StaffView splitStaffView = this;
		int numSplitStaves = 1;
		for(Voice v : _staff.getVoices()) {
			splitStaffView._splitVoice = v;
			numSplitStaves += 1;
			if(numSplitStaves < _staff.getNumVoices()) {
				StaffView below = splitStaffView.BELOW;
				splitStaffView.BELOW = new StaffView(getContext(), _staff);
				splitStaffView.BELOW.BELOW = below;
			}
		}
	}
	
}

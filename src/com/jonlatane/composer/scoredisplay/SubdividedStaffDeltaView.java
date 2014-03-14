package com.jonlatane.composer.scoredisplay;

import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.music.Score.Staff.Voice.VoiceDelta;

import android.content.Context;
import android.view.*;

/**
 * This is the layout component.  It lays out VoiceDeltaViews
 * 
 * 
 * 
 * @author jonlatane
 *
 */
public class SubdividedStaffDeltaView extends ViewGroup {

	// A Heptatonic Third is, visually, the distance between two staff lines.
	public static final int 
			HEPTATONICTHIRD_PX = 10, 
			HEPTATONICSTEP_PX = HEPTATONICTHIRD_PX/2, 
			HALF_DEFAULTSTAFFHEIGHT_PX = HEPTATONICTHIRD_PX * 3; // Allocate an extra third
	
	private StaffDelta _staffDelta;
	private int _minimumWidth = 0;
	
	/**
	 * The rendering component.
	 * @author jonlatane
	 *
	 */
	public class VoiceDeltaView extends View {
		int _voiceID;
		int _minSpaceAboveStaffCenter = HALF_DEFAULTSTAFFHEIGHT_PX, 
			_minSpaceBelowStaffCenter = HALF_DEFAULTSTAFFHEIGHT_PX;
		public VoiceDeltaView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		public VoiceDelta getVoiceDelta() {
			return _staffDelta.VOICES[_voiceID];
		}
		public int getMinimumSpaceAboveStaffCenter() {
			return _minSpaceAboveStaffCenter;
		}
		public int getMinimumSpaceBelowStaffCenter() {
			return _minSpaceBelowStaffCenter;
		}
		// This is more to ensure we fit the API, but this method isn't useful to us for layout.
		@Override public int getMinimumHeight() {
			return getMinimumSpaceAboveStaffCenter() + getMinimumSpaceAboveStaffCenter();
		}
	}
	
	public SubdividedStaffDeltaView(Context context) {
		super(context);
	}

	public void setStaffDelta(StaffDelta sd) {
		
	}
	
	@Override
	public int getMinimumWidth() {
		return _minimumWidth;
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}
	@Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}

package com.jonlatane.composer;

import android.content.res.*;
import com.jonlatane.composer.music.Staff;
public class Util
{
	public static void loadClefResources(Resources res) {
		Staff.CLEF_OFFSETS.put( Staff.Clef.TREBLE, res.getInteger(R.integer.treble_clef_offset) );
		Staff.CLEF_OFFSETS.put( Staff.Clef.BASS, res.getInteger(R.integer.bass_clef_offset) );
	}
}

package com.jonlatane.composer.graphics;

import com.jonlatane.composer.music.*;

public class VoicePitchSetPair
{
	public PitchSet ps;
	public Voice v;
	
	public VoicePitchSetPair(Voice v, PitchSet ps) {
		this.ps = ps;
		this.v = v;
	}
}

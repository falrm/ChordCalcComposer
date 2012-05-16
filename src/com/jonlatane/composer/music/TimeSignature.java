package com.jonlatane.composer.music;

public class TimeSignature {
	public final int TOP, BOTTOM;
	public TimeSignature(int beatsPerMeasure, int noteTypeForBeat) {
		this.TOP = beatsPerMeasure;
		this.BOTTOM = noteTypeForBeat;
	}
}

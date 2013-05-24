package com.jonlatane.composer.music.coverings;

public class TimeSignature {
	public static final TimeSignature UNMETERED_QUARTERS = new TimeSignature(Integer.MAX_VALUE, 4);
	public static final TimeSignature UNMETERED_EIGHTHS = new TimeSignature(Integer.MAX_VALUE, 8);
	public static final TimeSignature UNMETERED_SIXTEENTHS = new TimeSignature(Integer.MAX_VALUE, 16);
	public final int TOP, BOTTOM;
	public TimeSignature(int beatsPerMeasure, int noteTypeForBeat) {
		this.TOP = beatsPerMeasure;
		this.BOTTOM = noteTypeForBeat;
	}
}

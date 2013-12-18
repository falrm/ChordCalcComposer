package com.jonlatane.composer.scoredisplay2;

import com.jonlatane.composer.music.coverings.Clef;

import android.graphics.Canvas;

public class DrawRoutines {
	
	/**
	 * Assumes values is sorted by pitch value (i.e., {"B4","C4"} is invalid).
	 * 
	 * @param c
	 * @param right
	 * @param center
	 * @param scalingFactor
	 * @param clef
	 * @param values
	 * @return
	 */
	public static float drawAccidentals(Canvas c, float right, int center, double scalingFactor, Clef clef, String[] values) {
		for(String s : values) {
			
		}
		return 0;
	}
	public static float drawNoteheads(Canvas c, float right, int center, double scalingFactor, String[] values) {
		return 0;
	}
}

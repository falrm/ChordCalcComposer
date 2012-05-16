package com.jonlatane.composer.graphics;

import com.jonlatane.composer.music.*;
import android.graphics.Canvas;
import android.graphics.Paint;

// This is an element that handles the rendering of pitchsets on all staves.  It should
// be cached in a map of Rationals -> RhythmElements
public class RhythmElement
{
	// Ratios apply to StaffContainer.getNoteWidth() for expansion and
	// contraction of notes
	private float targetWidthRatio;
	private float currentWidthRatio;
	
	private long _t;
	
	public RhythmElement() {
		this(1);
	}
	
	public RhythmElement(float targetWidthRatio) {
		this.targetWidthRatio = targetWidthRatio;
		this.currentWidthRatio = this.targetWidthRatio;
	}
	
	// Render the appropriate stuff onto the Staff
	public int render(Canvas c, StaffElement s) {
		return 0;
	}
	
	public void setTargetWidthRatio(float f) {
		targetWidthRatio = f;
	}
	
	public void setCurrentWidthRatio(float f) {
		currentWidthRatio = f;
	}
	
	public float getTargetWidthRatio() {
		return targetWidthRatio;
	}
	
	public float getCurrentWidthRatio() {
		long now = System.currentTimeMillis();
		long passed = now - _t;
		
		// TODO move currentWidthRatio towards targetWidthRatio
		
		return currentWidthRatio;
	}
	
	// TODO
	public void renderPitchSet(Canvas c, StaffElement s, Voice v, Rational beat, int staffDrawingPos) {
		StaffContainer sc = s._parent;
		Paint p = s.getPaint();
		
		// This represents the box we have to draw in
		int realX = sc.getX() + s.getX() + staffDrawingPos;
		int realY = sc.getY() + s.getY();
		int noteWidth = (int)(s._parent.getNoteWidth() * s._parent.getScalingFactor());
		
		// Figure out what PitchSet to render (if any) and whether we need
		// to render a tie (by storing the locations of the pitches and letting
		// the Staff do it at the same time as stems, beams and slurs).
		PitchSet ps = null;
		boolean renderTie = false;
		if( v.keySet().contains(beat) ) {
			ps = v.get(beat);
		} else if( !v.headMap(beat).isEmpty() ) {
			PitchSet tmpPS = v.get(v.headMap(beat).lastKey());
			if( tmpPS.subPulses.contains(beat) ) {
				ps = tmpPS;
				renderTie = true;
			}
		}
		
		if( ps != null ) {
			
		}
	}
}

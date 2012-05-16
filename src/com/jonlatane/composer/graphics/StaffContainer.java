package com.jonlatane.composer.graphics;

import com.jonlatane.composer.music.*;
import java.util.NavigableSet;
import android.graphics.Typeface;
import android.graphics.Canvas;
import java.util.Map;

// An interface for any visual element containing one or more staves. Contains all the stuff
// a staff needs to render in the container.
public interface StaffContainer
{
	public int getX();
	public int getY();
	public int getWidth();
	public int getHeight();
	
	public NavigableSet<Rational> getOverallRhythm();
	
	public float getScrollingOffset();
	
	public Rational getCurrentBeat();
	
	public void setScrollingOffset(float o);
	
	public void setCurrentBeat(Rational r);
	
	public void setVelocity(float f);
	
	public float getScalingFactor();
	public void setScalingFactor(float f);
	
	// This should always be used by the Staff.  Should always be related
	// to scaling factor, and represents a size in pixels.
	public int getNoteWidth();
	
	public void render(Canvas c);
	
	public Typeface getNoteTypeface();
	
	public void setNoteTypeface(Typeface t);
	
	// An internal map used by all staves in a container
	public Map<Rational,RhythmElement> getRhythmElementMap();
}

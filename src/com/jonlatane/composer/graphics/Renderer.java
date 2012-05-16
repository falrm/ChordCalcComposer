package com.jonlatane.composer.graphics;

import com.jonlatane.composer.music.*;
import android.graphics.*;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Iterator;
import android.util.Log;

public class Renderer {
	public int leftMargin;
	public int rightMargin;
	public float scalingFactor;
	public int noteWidth;
	public Typeface noteTypeface;
	
	// The RENDERER'S view of the overall rhythm, not the work's.  Equals the work's iff
	// all parts are shown.  This functionality has yet to be implemented.
	public TreeSet<Rational> overallRhythm;
	
	Work _work;
	
	// Main elements
	public MainStavesArea STAVES;
	
	
	private Renderer() {}
	public Renderer(Work work, Typeface noteTypeface) {
		this(work, 30, 30, 2, 40, noteTypeface, new Rational(1,1));
	}
	
	public Renderer(Work work, int leftMargin, int rightMargin, float scalingFactor, int noteWidth, Typeface noteTypeface, Rational currentBeat) {
		this.leftMargin=leftMargin;
		this.rightMargin=rightMargin;
		this.scalingFactor=scalingFactor;
		this.noteWidth = noteWidth;
		this.noteTypeface = noteTypeface;
		this.setWorkTo(work);
		this.STAVES = new MainStavesArea(this, leftMargin, 40, 1920, 1080, scalingFactor, currentBeat);
		this.STAVES.updateOverallRhythm();
		this.STAVES.setNoteTypeface(noteTypeface);
	}

	public void setWorkTo( Work work ) {
		_work = work;
		work.updateOverallRhythm();
		this.overallRhythm = work.getOverallRhythm();
	}
	
	public void render(Canvas c) {
		// Draw background
		c.drawColor(Color.WHITE);
		
		// Make sure margins are valid
		STAVES.setX(leftMargin);
		if(STAVES.getWidth() > c.getWidth() - rightMargin - leftMargin) {
			Log.i("Composer","reducing width");
			STAVES.setWidth(c.getWidth() - rightMargin - leftMargin);
		}
		if(STAVES.getHeight() > c.getHeight() - STAVES.getY() - 1)
			STAVES.setHeight(c.getHeight() - STAVES.getY() - 1);
			
		STAVES.render(c);
	}
}

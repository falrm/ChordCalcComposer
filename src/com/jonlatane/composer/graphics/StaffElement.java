package com.jonlatane.composer.graphics;

import android.graphics.*;
import java.math.*;
import com.jonlatane.composer.music.*;
import java.util.*;
import android.util.Log;
import android.content.res.AssetManager;


// After calling Render on a StaffElement, use getNextLineStartingPos()
// and getNextLineScrollingOffset() to set up the next line.  If nextLineStartingPos()
// returns null we've rendered to the end of the associated Staff (TODO: Staff not Part)
public class StaffElement {
	// For input/layout-related use
	//public TreeMap<Coordinate,VoicePitchSetPair> notePositions;
	
	// The space above and below the staff, for renderer to layout
	// ... what units to use? Synchronization unimportant as Staff
	// movement will be animated.
	public int topMargin;
	public int bottomMargin;
	
	// These coordinates are RELATIVE to the parent's X/Y coordinates
	private int _x, _y;
	
	private Paint _paint;
	private int width;
	StaffContainer _parent;
	
	// Music
	final Part part;
	final Staff staff = null;
	private Rational startingPosition;
	private boolean showTimeSignature;
	
	// These are used
	private Rational nextLineStartingPos = null;
	private float nextLineScrollingOffset = 0;
	
	private float scalingFactor;
	
	// Don't use it.  Included only for safety.
	private StaffElement() { part = new Part(); throw new Error("No part in staff"); }
	
	public StaffElement(StaffContainer parent, Part part) {
		this(parent,part,0,0);
	}
	
	public StaffElement(StaffContainer parent, Part part, int x, int y) {
		this(parent,part,x,y,new Paint());
	}
	
	public StaffElement(StaffContainer parent, Part part, int x, int y, Paint paint) {
		this(parent,part,x,y,paint,parent.getCurrentBeat());
	}
	
	public StaffElement(StaffContainer parent, Part part, int x, int y, Paint paint, Rational startingPosition ) {
		this(parent, part, x, y, paint, startingPosition, parent.getScalingFactor());
	}
	
	public StaffElement(StaffContainer parent, Part part, int x, int y, Paint paint, Rational startingPosition, float scalingFactor ) {
		this._x = x;
		this._y = y;
		this.part = part;
		this._parent = parent;
		this._paint = paint;
		this.scalingFactor = scalingFactor;
		this.width = this._parent.getWidth() - getX();
		this._paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		this._paint.setTypeface(_parent.getNoteTypeface());
		this._paint.setTextSize(13 * scalingFactor);
		setStartPosition(startingPosition);
		
	}
	
	public void setStartPosition(Rational pos) {
		this.startingPosition = pos;
	}
	
	public void setPosition(int x, int y) {
		this._x = x;
		this._y = y;
	}
	
	public Paint getPaint() {
		return _paint;
	}
	
	// Renders the staff to the end of the staff container (and a little past if scrolling).
	// Returns the next Rational at the end of the staff if there is one.
	public Rational render(Canvas c) {
		SortedSet<Rational> renderBeatsQueue = _parent.getOverallRhythm().tailSet(startingPosition);
		_paint.setTypeface(_parent.getNoteTypeface());
		
		// Prevent drawing past our parent's width
		if( width > _parent.getWidth() )
			width = _parent.getWidth();
		
		// Calculate our real x
		int x = _parent.getX() + getX();
		
		// Draw staff lines
		int y_current = _parent.getY() + getY();
		
		c.drawLine(x,y_current, x+width, y_current, _paint);
		y_current = y_current + Math.round( 10 * scalingFactor);
		c.drawLine(x,y_current, x+width, y_current, _paint);
		y_current = y_current + Math.round( 10 * scalingFactor);
		c.drawLine(x,y_current, x+width, y_current, _paint);
		y_current = y_current + Math.round( 10 * scalingFactor);
		c.drawLine(x,y_current, x+width, y_current, _paint);
		y_current = y_current + Math.round( 10 * scalingFactor);
		c.drawLine(x,y_current, x+width, y_current, _paint);
		
		// BEGIN DRAWING ELEMENTS
		int currentDrawingPosition = x;
		
		// Start with the clef
		renderClef(c, currentDrawingPosition);
		currentDrawingPosition = currentDrawingPosition + _parent.getNoteWidth();
		
		// Now the key signature
		
		// Now the time signature if we don't have a previous staff
		
		this.nextLineStartingPos = null;
		
		
		// Set our drawing position based on our scrolling offset and the width of the first rhythmelement
		RhythmElement firstRhythmElement;
		if(_parent.getRhythmElementMap().containsKey(startingPosition)) {
			firstRhythmElement = _parent.getRhythmElementMap().get(startingPosition);
		} else {
			firstRhythmElement = new RhythmElement();
			_parent.getRhythmElementMap().put(startingPosition,firstRhythmElement);
		}
		int firstBeatWidth = (int)(firstRhythmElement.getCurrentWidthRatio() * _parent.getNoteWidth());
		
		currentDrawingPosition = currentDrawingPosition + (int)(_parent.getScrollingOffset() * firstBeatWidth);
		
		// Render each voice
		int pitchSetDrawingPosition = currentDrawingPosition;
		for( Voice v: part ) {
			pitchSetDrawingPosition = currentDrawingPosition;
			
			int paintColor = _paint.getColor();
			
			// Fade the paint for the first note if we're scrolling forward.
			if(_parent.getScrollingOffset() < 0) {
				float alphaMultiplier =1-(_parent.getScrollingOffset()*_parent.getScrollingOffset());
				_paint.setColor(( (int)(alphaMultiplier*(paintColor>>>(6*4))) << (6*4) ) + ((paintColor << (2*4)) >>>(2*4)));
			}
			
			for( Rational r : renderBeatsQueue ) {
				RhythmElement re = null;
				if(_parent.getRhythmElementMap().containsKey(r)) {
					re = _parent.getRhythmElementMap().get(r);
				} else {
					re = new RhythmElement();
					_parent.getRhythmElementMap().put(r,re);
				}
				
				int beatWidth = (int)(_parent.getNoteWidth() * re.getCurrentWidthRatio());
				c.drawLine((float)_parent.getX()+_parent.getWidth(),(float)_parent.getY() + getY(),(float)_parent.getX()+_parent.getWidth(),(float)y_current,_paint);
				
				// Fade off the last beat and signal we're done by setting result
				if(pitchSetDrawingPosition + beatWidth >= _parent.getX() + _parent.getWidth()) {
					// k is the proportion of this beat that doesn't fit; 1-k the proportion that does
					float k = (float)((pitchSetDrawingPosition + beatWidth) - (_parent.getX() + _parent.getWidth())) /
								(float)(beatWidth);
					if( k < 0 ) k = 0;
					
					float alphaMultiplier = 1 - k;
					this.nextLineScrollingOffset = -1 * (1-k) * _parent.getScrollingOffset();
					//float alphaMultiplier = _parent.getScrollingOffset() * _parent.getScrollingOffset();
					_paint.setColor(( (int)(alphaMultiplier*(paintColor>>>(6*4))) << (6*4) ) + ((paintColor << (2*4)) >>>(2*4)));
					this.nextLineStartingPos = r;
				}
				
				PitchSet ps = v.get(r);
				
				//re.renderPitchSet(c,this,v,r,currentDrawingPosition);
				renderPitchSet(c,ps,pitchSetDrawingPosition);
				
				_paint.setColor(paintColor);
				pitchSetDrawingPosition = pitchSetDrawingPosition + beatWidth;
				
				if( this.nextLineStartingPos != null ) {
					break;
				}
			}
		}
		
		currentDrawingPosition = pitchSetDrawingPosition;
		
		/*if( currentDrawingPosition < _parent.getX() + _parent.getWidth() ) {
			width = currentDrawingPosition - getX() - _parent.getX();
		} else if( result == null && currentDrawingPosition > _parent.getX() + getX() + getWidth() ) {
			width = Math.min( currentDrawingPosition - _parent.getX() - getX(),
							_parent.getWidth() - getX());
		}*/
		
		return nextLineStartingPos;
	}
	
	private static final TreeMap<Integer,Integer> intervalsAboveC;
	static {
		intervalsAboveC = new TreeMap<Integer,Integer>();
		intervalsAboveC.put(0,0);
		intervalsAboveC.put(1,1);
		intervalsAboveC.put(2,1);
		intervalsAboveC.put(3,2);
		intervalsAboveC.put(4,2);
		intervalsAboveC.put(5,3);
		intervalsAboveC.put(6,4);
		intervalsAboveC.put(7,4);
		intervalsAboveC.put(8,5);
		intervalsAboveC.put(9,5);
		intervalsAboveC.put(10,6);
		intervalsAboveC.put(11,6);
	}
	private void renderPitchSet(Canvas c, PitchSet ps, int xPosition) {
		float middleC = _parent.getY() + getY() +(int)(54.6* scalingFactor);
		
		
		c.drawLine(xPosition,_parent.getY()+getY(),
				   xPosition, _parent.getY()+getY()+50*scalingFactor, _paint);
		if( ps != null ) {
			for(int i : ps) {
				int octavelessPitch = i % 12;
				int octave = i / 12;
				c.drawText("%",
						   xPosition + (_parent.getNoteWidth()/4),
						   middleC - (5*scalingFactor*intervalsAboveC.get(octavelessPitch))
						   		- (35*octave*scalingFactor),
						   _paint);
			}
		}
	}
	// Return 0 if the note should be expressed as a natural,
	// 1 if a sharp, 2 if double sharp,
	// -1 if flat, -1 if double flat.  Based on:
	// 0) chords in the score
	// 1) Key Signature 
	// 2) Previous notes in the same measure
	// 3) Notes in the measure (i.e., F# or Gb? previous/upcoming F# or G=> F#, previous/upcoming F or Gb?=>Gb)
	private int getEnharmonic(int pitch, Rational r) {
		return 0;
	}
	
	private void renderClef(Canvas c, int xPosition) {
		float textSize = _paint.getTextSize();
		_paint.setTextSize(80*scalingFactor);
		c.drawText("\u00AE", xPosition, _parent.getY() + getY() + (float)53.6*scalingFactor, _paint);
		_paint.setTextSize(textSize);
	}
	
	// Return width of Key Signature
	public int renderKeySignature(Canvas c, int xPosition) {
		return 0;
	}
	
	public int getX() {
		return _x;
	}
	
	public void setX(int x) {
		this._x = x;
	}
	
	public int getY() {
		return _y;
	}
	
	public void setY(int y) {
		this._y = y;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public void setShowTimeSignature(boolean b) {
		this.showTimeSignature = b;
	}
	
	public void setScalingFactor(float f) {
		this.scalingFactor = f;
		this._paint.setTextSize(13*this.scalingFactor);
	}
	
	public float getScalingFactor() {
		return this.scalingFactor;
	}
	
	public Part getPart() {
		return this.part;
	}
	
	public Rational getNextLineStartingPos() {
		return nextLineStartingPos;
	}
	
	public float getNextLineScrollingOffset() {
		return nextLineScrollingOffset;
	}
}

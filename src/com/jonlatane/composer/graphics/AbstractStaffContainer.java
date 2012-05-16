package com.jonlatane.composer.graphics;

import com.jonlatane.composer.music.*;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.Iterator;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.Paint;

import android.util.Log;

public abstract class AbstractStaffContainer implements StaffContainer {
	private Rational currentBeat;
	private float scrollingOffset;
	private float velocity;
	private int noteWidth;
	private float scalingFactor;
	private boolean snapScrolling;
	
	// Must be set at runtime after construction
	private Typeface noteTypeface;
	private int x,y, width,height;
	
	private long _t;
	
	public AbstractStaffContainer() {
		this(40,40,600,600);
	}
	
	
	public AbstractStaffContainer(int x, int y, int width,int height) {
		this(x,y,width,height,1);
	}
	
	public AbstractStaffContainer(int x, int y, int width,int height, float scalingFactor) {
		this(x,y,width,height,scalingFactor,new Rational(1,1));
		
	}
	
	public AbstractStaffContainer(int x, int y, int width, int height, float scalingFactor, Rational currentBeat) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		this.scrollingOffset = 0;
		
		this.velocity = 0;
		this.noteWidth = 40;
		this.snapScrolling = true;
		
		this._t = System.currentTimeMillis();
		
		this.scalingFactor = scalingFactor;
		this.currentBeat = currentBeat;
		
	}
	
	public float getScrollingOffset() {
		return scrollingOffset;
	}
	
	public void setScrollingOffset(float f) {
		scrollingOffset=f;
	}
	
	public Rational getCurrentBeat() {
		return currentBeat;
	}
	
	public abstract NavigableSet<Rational> getOverallRhythm();
	
	public void setCurrentBeat(Rational r) {
		if(r == null)
			Log.i("Composer","r was null");
		if( getOverallRhythm().contains(r) )
			currentBeat = r;
		else
			currentBeat = getOverallRhythm().lower(r);
	}
	
	public void setVelocity(float f) {
		this.velocity = f;
		updatePosition();
	}
	
	private synchronized void updatePosition() {
		long now = System.currentTimeMillis();
		float movement = (now - _t) * velocity;
		scrollingOffset = scrollingOffset + movement;
		
		if(velocity > 0) {
			velocity = Math.max(velocity - (now - _t)*(float).0001,0);
		}
		if(velocity < 0) {
			velocity = Math.min(velocity + (now - _t)*(float).0001,0);
		}
		
		normalizeScrollingOffset();
		
		if( snapScrolling ) {
			scrollingOffset = 4*scrollingOffset/5;
		}
		
		if( scrollingOffset < 0 && currentBeat.equals(getOverallRhythm().last()) ) {
			scrollingOffset = 0;
		}
		
		_t = now;
	}
	
	public void render(Canvas c) {
		Paint p = new Paint();
		c.drawLine(getX(),getY(),getX()+getWidth(),getY(),p);
		c.drawLine(getX(),getY()+getHeight(),getX()+getWidth(),getY()+getHeight(),p);
		
		updatePosition();
	}
	
	// Ensure scrolling offset is always between 0 and -1.
	private synchronized void normalizeScrollingOffset() {
		if(scrollingOffset > 0) {
			Rational targetBeat = null;
			// Scroll left to right, backward in time
			SortedSet<Rational> shrinkingSet = getOverallRhythm().headSet(getCurrentBeat());
			if(shrinkingSet.isEmpty()) {
				scrollingOffset = 0;
			} else {
				targetBeat = shrinkingSet.last();
			}
			
			while(scrollingOffset > 0) {
				if(shrinkingSet.isEmpty()) {
					scrollingOffset = 0;
					break;
				} else {
					targetBeat = shrinkingSet.last();
				}
				shrinkingSet = shrinkingSet.headSet(targetBeat);
				scrollingOffset = scrollingOffset - 1;
			}
			
			if( targetBeat != null )
				setCurrentBeat(targetBeat);
				
		} else if( scrollingOffset < -1 ) {
			// Scroll right to left, forward in time
			Rational targetBeat = null;
			Iterator<Rational> iter = getOverallRhythm().tailSet(getCurrentBeat(),false).iterator();
			while(scrollingOffset < -1) {
				if(iter.hasNext())
					targetBeat = iter.next();
				else
					break;
				scrollingOffset = scrollingOffset + 1;
			}
			
			if( targetBeat != null ) {
				setCurrentBeat( targetBeat );
			}
		}
		
		/*if( (getCurrentBeat().equals(getOverallRhythm().last()) && scrollingOffset < 0) ||
			scrollingOffset > 0) {
			scrollingOffset = 0;
		}*/
	}
	
	public void setScalingFactor(float f) {
		this.scalingFactor = f;
	}
	
	public float getScalingFactor() {
		return this.scalingFactor;
	}
	
	public int getNoteWidth() {
		return (int)(this.noteWidth * this.scalingFactor);
	}
	
	public Typeface getNoteTypeface() {
		return noteTypeface;
	}
	
	public void setNoteTypeface(Typeface t) {
		this.noteTypeface = t;
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int w) {
		this.width = w;
	}
	
	public int getHeight() {
		return height;
	}
	public void setHeight(int h) {
		this.height = h;
	}
	public void setSnapScrolling(boolean b) {
		this.snapScrolling = b;
	}
}

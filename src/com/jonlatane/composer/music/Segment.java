package com.jonlatane.composer.music;

import java.util.*;
import android.renderscript.*;
import android.content.*;


public interface Segment extends Comparable<Segment>, SortedSet<Rational>, Cloneable
{
	public Rational getStart();
	public Rational getEnd();
	public Rational getLength();
	
	public boolean spans(Rational r);
	public boolean spans(Segment s);
	
	public Segment tailSet(Rational r);
	public Segment headSet(Rational r);
	public Segment subSet(Rational r1, Rational r2);
	
	@Override
	public int hashCode();
	
	// the following methods should return cloned Segments (since Segments are Cloneable)
	public Segment clone();
	public Segment normalize();
	//public Segment symDiff(Segment s);
	//public Segment plus(Segment s);
	//public Segment minus(Segment s);
	//public Segment downbeats();
	//public segment downbeats(Rational r);
	//public Segment upbeats();
	//public Segment upbeats(Rational r);
	//public Segment decoratedDownbeats();
	//public Segment decoratedDownbeats(Rational r);
}

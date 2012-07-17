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
	
	@Override public Segment tailSet(Rational r);
	@Override public Segment headSet(Rational r);
	@Override public Segment subSet(Rational r1, Rational r2);
	
	@Override
	public int hashCode();
	
	// the following methods should return cloned Segments (since Segments are Cloneable)
	public Segment normalize();
	public Segment difference(Segment s);
}

package com.jonlatane.composer.music;

import java.util.*;
import android.renderscript.*;
import android.content.*;

// A collection of one or many Voices combined with a common start and end beat.  At each point
// in time we can retrieve a Map of each voice to its MusicalContext.
public interface Segment extends Comparable<Segment>, Iterable<Rational>, Cloneable
{
	public Rational getStart();
	public Rational getEnd();
	
	public Rational getLength();

	public boolean contains(Rational r);
	
	@Override
	public int hashCode();
	
	public int compareTo(Segment s);
	public Iterator<Rational> iterator();
	public Segment clone();
}

package com.jonlatane.composer.music;

import java.util.*;
import android.renderscript.*;
import android.content.*;

/**
 * A Segment is a half-open interval of the the real line with Rational endpoints, 
 * and a set of Rationals in those bounds.  Segments have a natural ordering based first
 * on their start, then their end, then their contents.  This is to facilitate quick access
 * through sorted structures of Coverings of segments.
 * 
 * @author Jon
 */
public interface Segment extends Comparable<Segment>, SortedSet<Rational>, Cloneable
{
	/**
	 * Return the start of the segment
	 * @return
	 */
	public Rational getStart();
	/**
	 * Return the end of the segment
	 * @return
	 */
	public Rational getEnd();
	/**
	 * Return the length of the segment (getStart().minus(getEnd()))
	 * @return
	 */
	public Rational getLength();
	
	/**
	 * Return true if and only if r is within the bounds of the segment (i.e., greater than or equal to the start,
	 * and strictly less than the end)
	 * @param r
	 * @return
	 */
	public boolean spans(Rational r);
	
	/**
	 * Return true if and only if s is completely within the bounds of this segment (i.e., s.getStart() and s.getEnd()
	 * are gte and lte getStart and getEnd respectively)
	 * @param s
	 * @return
	 */
	public boolean spans(Segment s);
	
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

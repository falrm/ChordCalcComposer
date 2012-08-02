package com.jonlatane.composer.music;

import java.util.*;

/**
 * A Covering is a mapping function from a set of Segments to objects of type K.
 * We may assume a few things about coverings such as whether they allow for intersection
 * (which lets us make some assumptions about efficiently implementing things).
 * 
 * 
 * @author Jon
 *
 * @param <K>
 */
public interface Covering<K>
{
	// generally a HashSet for efficiency
	public Set<K> getAllObjectsAt(Rational r);
	
	// for convenience, may be the above.first()
	public K getObjectAt(Rational r);
	
	// the value of: getObjectsAt(r).size() == 0
	public boolean contains(Rational r);
	
	// Merge in the contents of the given segment.  The behavior is dependent upon
	// implementation. If s is 
	//public CoveredSegment merge(CoveredSegment s);
	//public CoveredSegment mergeRelative(CoveredSegment s, Class c1);
	
	// assume o is Rational, Collection<Rational> or Segment
	//public boolean spans(Object o);
	//public boolean spans(Rational r);
	//public boolean spans(Segment s);
	//public boolean spans(Covering c);
	
	//getRhythm() == getRhythm(false);
	
	public Segment getRhythm();
	public Segment getRhythm(boolean segmentEndInclusive);
	public CoveredSegment getAsCoveredSegment();
	
	/**
	 * Add the given Segment to the domain of the covering, pointing to the given object.
	 * 
	 * @param object the object to be added to the Covering
	 * @param s the Segment to be added to the Covering
	 */
	public void add(K object, Segment s);
	
	/**
	 * Removes the segment from this Covering
	 * 
	 * @param s the segment to be removed
	 * @return true iff the segment was present a removed.
	 */
	public boolean remove(Segment s);
	
	/**
	 * This should first call remove(s).  It should then find any Segments in its domain
	 * that intersect s and shorten them so that that is not the case.  After calling this
	 * method, for any Rational r spanned by s, getObjectAt(r) should return null.
	 *  
	 * @param s the Segment to be cleared in this Covering.
	 */
	public void clear(Segment s);
	
	/**
	 * Returns a view of the set of segments in the domain of the Covering as a function from Segments to Ks
	 * @return
	 */
	public NavigableSet<? extends Segment> getSegments();
}

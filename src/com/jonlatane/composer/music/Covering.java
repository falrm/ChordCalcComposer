package com.jonlatane.composer.music;

import java.util.Set;

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
	public CoveredSegment merge(CoveredSegment s);
	public CoveredSegment mergeType(CoveredSegment s, Class c);
	public CoveredSegment mergeRelative(CoveredSegment s, Class c1, Class c2);
	
	// assume o is Rational, Collection<Rational> or Segment
	public boolean spans(Object o);
	
	public Set<Segment> getSegments();
	
	// Covering methods.  These may be implemented with varying data
	// structures for efficiency depending on what we want to assume
	// about the covering (mostly whether it's non-intersecting or not).
	// In all non-intersecting cases, the covering should preserve
	// its previous contents as best as possible.
	public void add(K object, Rational r);
	public void add(K object, Rational r, Rational length);
	public void add(K object, Segment s);
	
	public void clear(Segment s);
}

package com.jonlatane.composer.music;

import com.jonlatane.composer.music.harmony.*;
import java.util.*;

/**
 * A CoveredSegment is a @Segment along with a set of coverings.  Implementations
 * should assume one covering per class type for simplicity.
 * 
 * @author Jon
 *
 */
public interface CoveredSegment extends Segment {
	// return true if replacing another covering
	/**
	 * Adds the covering and, assuming the covering is of type cl
	 * 
	 * @param covering the covering
	 * @param cl the class of the covering (e.g., for a Covering<Chord>, Chord.class)
	 * @return true if a previous covering of the same class was replaced
	 */
	public boolean addCovering(Covering<?> covering, Class cl);
	
	/**
	 * Removes the covering of the specified type from the index.
	 * 
	 * @param cl the type of covering (e.g., Chord.class)
	 * @return true if a covering was removed
	 */
	public boolean removeCovering(Class cl);

	@Override public CoveredSegment tailSet(Rational r);
	@Override public CoveredSegment headSet(Rational r);
	@Override public CoveredSegment subSet(Rational r1, Rational r2);
	
	public Object getObjectOfTypeAt(Class c, Rational r);
	public Set<Object> getAllObjectsOfTypeAt(Class c, Rational r);
	
	// Because the covering has so much more information in it, we should update
	// how spanning works.  In the case of PitchSets, this will equate to sonority.
	// 
	public boolean spans(PitchSet o, Class c);
	public boolean spans(CoveredSegment s, Class c);
	
	// These apply amongst all Classes
	public boolean spans(Object o);
	public boolean spans(CoveredSegment s);
	
	// The SortedSet part of a Segment (i.e., the rhythm itself)
	// can be mutated and synchronized with Coverings by the methods below.
	// So a voice
	public void updateRhythmFromDefaultCovering();
	public void mergeRhythmToDefaultCovering();
	
	// These two change the behavior of the above and should call it.
	// Synchronization problems ahoy?
	public void setDefaultCovering(Class c);
	public void setDefaultCovering(Class c, boolean includeSegmentEndings);
	
	public Covering<?> getDefaultCovering();
	public Class getDefaultCoveringClass();
	public boolean getDefaultCoveringIncludesSegmentEndings();
	
	// These should be based on coverings
	public int getMeasureNumber(Rational r);
	public int getBeatOfMeasure(Rational r);
	public int getHarmonicSeparation(Class c1, Class c2);
	
	// Should work for all PitchSets, including Chords, Scales and Keys
	public int getMotion(Class c1);
	
	// Useful for generating closures
	public Rational positionOfLast(Class c);
	public Object getLast(Class c);
	
	CoveredSegment clone();
	CoveredSegment normalize();
	//@Override public CoveredSegment symDiff(Segment s);
	//@Override public CoveredSegment plus(Segment s);
	//@Override public CoveredSegment minus();
	//@Override public CoveredSegment downbeats();
	//@Override public CoveredSegment upbeats();
	//@Override public CoveredSegment decoratedDownbeats();
}

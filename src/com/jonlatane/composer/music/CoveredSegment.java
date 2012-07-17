package com.jonlatane.composer.music;

import java.util.*;

// A Covered Segment is the interface of a Voice, a Staff
public interface CoveredSegment extends Segment {

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
	
	// The following should have sane default behaviors that can be changed by the application
	// of a ClosureGenerator, defined below
	public Key getClosingKey();
	public Scale getClosingScale();
	public Chord getClosingChord();
	public PitchSet getClosingRealization();
	
	public void setClosureGenerator(ClosureGenerator g);
	
	public interface ClosureGenerator {
		public Key getClosingKey();
		public Scale getClosingScale();
		public Chord getClosingChord();
		public PitchSet getClosingRealization();
		
		public Covering<Key> getKeyProgression();
		public Covering<Scale> getScaleProgression();
		public Covering<Chord> getChordProgression();
		public Covering<PitchSet> getRealization();
		
		public Covering<?> 
	}
}

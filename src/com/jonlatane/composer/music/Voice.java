package com.jonlatane.composer.music;

import java.util.*;
import android.util.Log;

public class Voice extends Rhythm {
	private Chord.Modulus _modulus = new Chord.Modulus();
	private HashMap<Class,Covering<?>> _data = new HashMap<Class,Covering<?>>();
	
	
	public Voice() {
		// The use of HarmonicCovering types ensures we always have a key, chord, and scale.
		// Worst case (12 tone construction) they are all chromatic.  Best case, we can
		// assume a bit more.
		_data.put(Chord.class, new HarmonicCovering<Chord>() );
		_data.put(Scale.class, super.new HarmonicCovering<Scale>());
		_data.put(Key.class, super.new HarmonicCovering<Key>());
		
		// Our Realization information - the pitches and articulations to be played
		_data.put(PitchSet.class, _rhythm.new NonIntersectingCovering<PitchSet>());
		_data.put(Articulation.class, _rhythm.new NonIntersectingCovering<Articulation>());
		
		_data.put(TimeSignature.class, _rhythm.new NonIntersectingCovering<TimeSignature>());
	}
	
	public Covering<?> getCovering(Class s) {
		return _data.get(s);
	}
	
	public NonIntersectingCovering<PitchSet> getRealization() {
		return _data.get(PitchSet.class);
	}
	
	// get a hashmap for the objects of nonintersecting (and harmonic) segments
	public Set<?> getObjectsAt( Rational r ) {
		Set<?> result = new HashSet<?>();
		for(Map.Entry<Class,Covering<?>> e : _data.entrySet()) {
			if( ( e.getValue() instanceof Rhythm.HarmonicCovering ||
				e.getValue() instanceof Rhythm.NonIntersectingCovering) && 
				e.getValue().contains(r)) {
					Object obj = e.getValue().getObjectsAt(r).toArray()[0];
					result.put(e.getClass(),obj);
			}
		}
		
		return result;
	}
	
	public Set<Map.Entry<Covering<?>,?> getCoveringPairsAt( Rational r ) {
		Set<Map.Entry<Covering<?>,?> result = new HashSet<Map.Entry<Covering<?>,?>();
		
		return result;
	}
	
	public Iterator<Set<?>> iterator() {
		
	}
}

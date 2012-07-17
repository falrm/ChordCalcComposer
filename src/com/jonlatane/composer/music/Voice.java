package com.jonlatane.composer.music;

import java.util.*;
import com.jonlatane.composer.music.coverings.*;
import android.util.Log;

public class Voice extends Rhythm implements CoveredSegment {
	private Chord.Modulus _modulus = new Chord.Modulus();
	private HashMap<Class,com.jonlatane.composer.music.Covering<?>> _data = 
		new HashMap<Class,com.jonlatane.composer.music.Covering<?>>();
	
	class Segment extends Rhythm.Segment implements CoveredSegment {
		public Segment(Rational start, Rational end) {
			super(start,end);
		}
		
		public Rational harmonicDifference(Class s) {
			assert(s.equals(Chord.class) || s.equals(Scale.class) || s.equals(Key.class));
			Rational result = Rational.ZERO;
			HarmonicCovering harmony = (HarmonicCovering)_data.get(s);
			NonIntersectingCovering<PitchSet> realization = 
				(NonIntersectingCovering<PitchSet>)_data.get(PitchSet.class);
			
			return result;
		}
		
		
	}
	
	public Voice() {
		// The use of HarmonicCovering types ensures we always have a key, chord, and scale.
		// Worst case (12 tone construction) they are all chromatic.  Best case, we can
		// assume a bit more
		HarmonicCovering<Chord> chords = new HarmonicCovering<Chord>();
		_data.put(Chord.class, chords );
		_data.put(Scale.class, new HarmonicCovering<Scale>());
		_data.put(Key.class, new HarmonicCovering<Key>());
		
		// Our Realization information - the pitches and articulations to be played
		_data.put(PitchSet.class, new NonIntersectingCovering<PitchSet>());
		_data.put(Articulation.class, new NonIntersectingCovering<Articulation>());
		
		_data.put(TimeSignature.class, new NonIntersectingCovering<TimeSignature>());
	}
	
	public com.jonlatane.composer.music.Covering<?> getCovering(Class s) {
		return _data.get(s);
	}
	
	public NonIntersectingCovering<PitchSet> getRealization() {
		return (NonIntersectingCovering<PitchSet>)_data.get(PitchSet.class);
	}
	
	// gets everything at a point. Not very useful in real time but a good quick slice
	// of all
	public Set getObjectsAt( Rational r ) {
		Set result = new HashSet();
		for(Map.Entry<Class,com.jonlatane.composer.music.Covering<?>> e : _data.entrySet()) {
			if( ( e.getValue() instanceof Rhythm.HarmonicCovering ||
				e.getValue() instanceof Rhythm.NonIntersectingCovering) && 
				e.getValue().contains(r)) {
					result.addAll(e.getValue().getAllObjectsAt(r));
			}
		}
		
		return result;
	}
	
	public Object getFirstObjectOfTypeAt(Class c, Rational r) {
		assert(_data.containsKey(c));
		Set<?> objs = _data.get(c).getAllObjectsAt(r);
		Object result = null;
		if(!objs.isEmpty())
			result = objs.toArray()[0];
		return result;
	}
	
	synchronized void synchronize() {
		synchronize(_data.get(PitchSet.class));
	}
	
	synchronized void synchronize(com.jonlatane.composer.music.Covering c) {
		clear();
		for( com.jonlatane.composer.music.Segment s : c.getSegments() ) {
			add(s.getStart());
		}
	}
}

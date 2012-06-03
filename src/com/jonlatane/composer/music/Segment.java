package com.jonlatane.composer.music;

import java.util.TreeSet;
import java.util.Set;
import java.util.Collection;
import java.util.SortedSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import android.renderscript.*;
import android.content.*;

// A collection of one or many Voices combined with a common start and end beat.  At each point
// in time we can retrieve a Map of each voice to its MusicalContext.
public class Segment implements Iterable< Map<Rational,Map<Voice,Voice.MusicalContext>>.Entry >
{
	// Segment contains all beats from start up to (but excluding) end.
	public final Rational START;
	public final Rational END;
	private final Set<Voice> _voices;
	
	private Segment() {_voices=new TreeSet<Voice>();START=null;END=null;};

	public Segment(Rational start, Rational end) {
		this(null, start, end);
	}
	public Segment(Voice v, Rational start, Rational end) {
		this.START = start;
		this.END = end;
		
		this._voices = new TreeSet<Voice>();
		if( v != null )
			this._voices.add(v);
	}
	
	public Segment(Segment s) {
		this.START = s.START;
		this.END = s.END;
		this._voices = s.getVoices();
	}
	
	public Segment(Collection<Segment> c) {
		this(c.iterator().next());
		
		for( Segment s : c ) {
			assert( s.START == START && s.END == END );
			for(Voice v : s.getVoices())
				getVoices().add(v);
		}
	}
	
	public Set<Voice> getVoices() {
		return _voices;
	}
	
	public Rational getLength() {
		return END.minus(START);
	}

	public boolean contains(Rational r) {
		return (START.compareTo(r) <= 0) && (END.compareTo(r) > 0);
	}
	
	public Rhythm getRhythm() {
		Rhythm result = new Rhythm();
		for(Voice v : _voices) {
			result.addAll(v.getRhythm().subSet(START,END));
		}
		return result;
	}
	
	public Set<Voice> getVoicesRealizedAt(Rational r) {
		Set<Voice> result = new TreeSet<Voice>();
		for(Voice v : _voices) {
			if(v.getRhythm().contains(r))
				result.add(v);
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		int result = START.hashCode() ^ END.hashCode();
		
		for(Voice v : _voices)
			result = result ^ v.hashCode();
		
		return result;
	}
	
	public Segment union( Segment s ) {
		assert( s.START == START && s.END == END );
		Segment result = new Segment(s);
		for(Voice v : getVoices() ) {
			getVoices().add(v);
		}
		
		return result;
	}
	
	public Segment union(Collection<Segment> c) {
		Segment s = new Segment(c);
		return union(s);
	}
	
	public Map<Voice,Voice.MusicalContext> getContextMapAt(Rational r) {
		
	}
	
	public Iterator< Map<Rational,Map<Voice,Voice.MusicalContext>>.Entry > iterator() {
		return new Iterator< Map<Rational,Map<Voice,Voice.MusicalContext>>.Entry >() {
			
		};
	}
	
	public Rational relative(Rational r) {
		return r.minus(START);
	}
	
	public Segment freeSegment() {
		Segment result = new Segment(Rational.ZERO, relative(this.END));
		result.getVoices().clear();
		Map<Voice,Voice> cloneMap = new HashMap<Voice,Voice>();
		for(Voice v : getVoices()) {
			Voice vClone = new Voice();
			result.getVoices().add(vClone);
			cloneMap.put(v, vClone);
		}
		
		// Copy realization information
		for(Voice.RhythmMapPair rmp : this) {
			// rmp is a
			Rational r = rmp.BEAT;
			Map<Voice,Voice.MusicalContext> M = rmp.CONTEXT;
			for( Map.Entry<Voice,Voice.MusicalContext> e : M.entrySet() ) {
				Voice target = cloneMap.get(e.getValue());
				target.getRhythm().add( relative(rmp.BEAT) );
				
				target.getMusicalContextAt(rmp.BEAT).REALIZATION.setRealization( rmp.CONTEXT.get( e.getKey() ));
			}
		}
		
		return result;
	}
}

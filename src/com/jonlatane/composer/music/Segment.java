package com.jonlatane.composer.music;

import java.util.TreeSet;
import java.util.Set;
import java.util.Collection;
import java.util.SortedSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import android.renderscript.*;

// A collection of many Voices combined with a start and end beat.  Contains all
// elements of the voice
public class Segment implements Iterable<Segment.RhythmMapPair>
{
	// Segment contains all beats from start up to (but excluding) end.
	public final Rational start;
	public final Rational end;
	private final Set<Voice> _voices;
	
	private Segment() {_voices=new TreeSet<Voice>();start=null;end=null;};

	public Segment(Voice v, Rational start, Rational end) {
		this.start = start;
		this.end = end;
		
		this._voices = new TreeSet<Voice>();
		this._voices.add(v);
	}
	
	public Segment(Segment s) {
		this.start = s.start;
		this.end = s.end;
		this._voices = s.getVoices();
	}
	
	public Segment(Collection<Segment> c) {
		this(c.iterator().next());
		
		for( Segment s : c ) {
			assert( s.start == start && s.end == end );
			for(Voice v : s.getVoices())
				getVoices().add(v);
		}
	}
	
	public Set<Voice> getVoices() {
		return _voices;
	}
	
	public Rational getLength() {
		return end.minus(start);
	}

	public boolean contains(Rational r) {
		return (start.compareTo(r) <= 0) && (end.compareTo(r) > 0);
	}
	
	public Rhythm getRhythm() {
		Rhythm result = new Rhythm();
		for(Voice v : _voices) {
			result.addAll(v.getRhythm().subSet(start,end));
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
		int result = start.hashCode() ^ end.hashCode();
		
		for(Voice v : _voices)
			result = result ^ v.hashCode();
		
		return result;
	}
	
	public Segment union( Segment s ) {
		assert( s.start == start && s.end == end );
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
	
	public class RhythmMapPair {
		Rational BEAT;
		Map<Voice,Voice.MusicalContext> CONTEXT = new HashMap<Voice,Voice.MusicalContext>();
	}
	class SegmentIterator implements Iterator<RhythmMapPair> {
		Rhythm r = getRhythm();
		Iterator<Rational> itr;
		RhythmMapPair next;
		
		public SegmentIterator() {
			this(false);
		}
		public SegmentIterator( boolean reverse ) {
			next = new RhythmMapPair();
			if(reverse)
				itr = r.descendingIterator();
			else
				itr = r.iterator();
		}
		public boolean hasNext() {
			return itr.hasNext();
		}
		public RhythmMapPair next() {
			next.BEAT = itr.next();
			next.CONTEXT.clear();
			for(Voice v : getVoicesRealizedAt(next.BEAT)) {
				next.CONTEXT.put(v,v.getMusicalContextAt(next.BEAT));
			}
			return next;
		}
		public void remove() {
			
		}
	}
	
	public Iterator<RhythmMapPair> iterator() {
		return new SegmentIterator();
	}
	class DescendingIteratorSegment extends Segment {
		@Override
		public Iterator<RhythmMapPair> iterator() {
			return new SegmentIterator(true);
		}
	}
	public Iterator<RhythmMapPair> descendingIterator() {
		return new SegmentIterator(true);
	}
}

package com.jonlatane.composer.music;

import java.util.*;

public class Rhythm extends TreeSet<Rational> implements SortedSet<Rational>, Iterable<Rational>, Cloneable
{
	public Rhythm() {
		super();
	}
	public Rhythm(Collection<? extends Rational> c) {
		super(c);
	}
	
	/*
	* A Segment lies over top of a Rhythm and has deep cloning abilities.  They are our means
	* of moving rhythms around
	*/
	public class Segment implements com.jonlatane.composer.music.Segment {
		private final Rational _start, _end;
		public Segment(Rational start, Rational end) {
			assert( start.compareTo(first()) <= 0 );
			assert( end.compareTo(last()) >= 0 );
			
			_start = start;
			_end = end;
		}
		public Segment( Rhythm.Segment s ) {
			this(s._start, s._end);
		}
		
		public Rational getStart() {
			return _start;
		}
		
		public Rational getEnd() {
			return _end;
		}
		
		@Override
		public int compareTo(com.jonlatane.composer.music.Segment s) {
			int result = _start.compareTo(s.getStart());
			if( result == 0 )
				result = _end.compareTo(s.getEnd());
			return result;
		}

		public SortedSet<Rational> getSet() {
			return tailSet(_start).headSet(_end);
		}
		
		public Iterator<Rational> iterator() {
			return getSet().iterator();
		}
		
		public Rational getLength() {
			return getEnd().minus(getStart());
		}
		
		public boolean contains(Rational r) {
			return (r.compareTo(getStart()) >= 0) && (r.compareTo(getEnd()) < 0);
		}
		
		@Override
		public Rhythm.Segment clone() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add(r);
			}
			return Q.all();
		}
		
		//return a cloned segment that
		public Rhythm.Segment normalize() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add( r.minus(_start) );
			}
			return Q.all();
		}
		
	}
	
	public class Covering<K> extends TreeMap<Rhythm.Segment,K>
	implements com.jonlatane.composer.music.Covering<K> {	
		public boolean isComplete() {
			return false; //TODO?
		}
		public boolean isPartial() {
			return !isComplete();
		}
		public boolean isIntersecting() {
			return true; //TODO
		}
		
		public Set<K> getObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			
			Iterator<Rhythm.Segment> itr = headMap(tailSegment(r), true).descendingKeySet().iterator();
			while(itr.hasNext()) {
				Rhythm.Segment s = itr.next();
				if( s.getEnd().compareTo(r) > 0 )
					result.add( get(s) );
			}
			
			return result;
		}
		
		public boolean contains(Rational r) {
			boolean result = false;
			for( Segment s : keySet() ) {
				if( s.getStart().compareTo(r) > 0 )
					break;
				if( s.contains( r ) {
					result = true;
					break;
				}
			}
			return result;
		}
	}
	
	/*
	* Non-intersecting Coverings can be accessed a bit faster than other kinds
	*/
	public class NonIntersectingCovering<K> extends TreeMap<Rhythm.Segment,K>
	implements com.jonlatane.composer.music.Covering<K> {	

		public Set<K> getObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			
			Rhythm.Segment ceiling = ceilingKey(tailSegment(r));
			if( ceiling.getEnd().compareTo(r) > 0 )
				result.add(get(ceiling));
			
			return result;
		}
	}
	
	
	/*
	* Harmonic Coverings are assumed to be nonintersecting AND complete and can thus be
	* afforded faster access and ease of manipulation
	*/
	public class HarmonicCovering<K extends Chord> extends TreeMap<Rational,K>
	implements com.jonlatane.composer.music.Covering<K> {
		private final Chord.Modulus _m;
		
		public HarmonicCovering() {
			this(new Chord.Modulus());
		}
		public HarmonicCovering(Chord.Modulus m) {
			_m = m;
		}
		
		public Set<K> getObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			Rational segmentStart = lowerKey(r);
			if(segmentStart == null) {
				segmentStart = Rational.ZERO;
				put(segmentStart, (K)_m.chromatic());
			}
				
			result.add( get( segmentStart ) );

			return result;
		}
	}
	
	@Override
	public boolean add(Rational r) {
		assert( r.toDouble() >= 0.0 ) : "Rhythms must only contain positive Rationals";
		return super.add(r);
	}
	
	public Rhythm.Segment all() {
		return this.new Segment(Rational.ZERO, this.last());
	}
	
	public Rhythm.Segment segment(Rational start, Rational end) {
		return this.new Segment(start, end);
	}
	
	public Rhythm.Segment tailSegment(Rational start) {
		return this.new Segment(start, last());
	}
}

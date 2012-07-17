package com.jonlatane.composer.music;

import java.util.*;

public class Rhythm extends TreeSet<Rational> implements com.jonlatane.composer.music.Segment
{
	public Rhythm() {
		super();
	}
	public Rhythm(Collection<? extends Rational> c) {
		super(c);
	}
	
	/*
	* A Segment lies over top of a Rhythm and has deep cloning abilities.  They are our means
	* of moving rhythms around and doing cool stuff
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

		// returns the view of the overlying rhythm this segment represents
		private SortedSet<Rational> getSet() {
			return segmentView(getStart(),getEnd());
		}
		
		public Iterator<Rational> iterator() {
			return getSet().iterator();
		}
		
		public Rational getLength() {
			return getEnd().minus(getStart());
		}
		
		public boolean spans(Rational r) {
			return (r.compareTo(getStart()) >= 0) && (r.compareTo(getEnd()) < 0);
		}
		
		public boolean spans(Collection<Rational> c) {
			boolean result = true;
			for( Rational r : c ) {
				if(!spans(r)) {
					result = false;
					break;
				}
			}
			return result;
		}
		
		public boolean spans(com.jonlatane.composer.music.Segment s) {
			return spans(s.getStart()) && (spans(s.getEnd())||s.getEnd().equals(getEnd()));
		}
		
		//SortedSet methods...
		public boolean contains(Rational r) {
			return getSet().contains(r);
		}
		public boolean contains(Object o) {
			return getSet().contains(o);
		}
		
		public Rational last() {
			return getSet().last();
		}
		public Segment subSet(Rational r1, Rational r2) {
			return new Segment(r1, r2);
		}
		public Segment headSet(Rational r) {
			return new Segment(r, getEnd());
		}
		public Segment tailSet(Rational r) {
			return new Segment(r, getEnd());
		}
		
		public Object[] toArray() {
			return getSet().toArray();
		}
		public <T> T[] toArray(T[] a) {
			return getSet().toArray(a);
		}
		public boolean containsAll(Collection<?> c) {
			return getSet().containsAll(c);
		}
		public boolean addAll(Collection<Rational> c) {
			boolean result = false;
			for( Rational r : c ) {
				if( spans( r ) )
					result = result || add(r);
			}
			return result;
		}
		public boolean retainAll(Collection<?> c) {
			return retainAll(c);
		}
		public int size() {
			return size();
		}
		public  boolean removeAll(Collection<?> c) {
			return removeAll(c);
		}
		public Rational first() {
			return getSet().first();
		}
		public void clear() {
			getSet().clear();
		}
		public boolean add(Rational r) {
			return add(r);
		}
		public Comparator comparator() {
			return null;
		}
		public boolean remove(Object o) {
			return getSet().remove(o);
		}
		public boolean isEmpty() {
			return getSet().isEmpty();
		}
		
		// SEGMENT MANIPULATION COOLNESS! These should be efficient
		@Override
		public com.jonlatane.composer.music.Segment clone() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add(r);
			}
			return Q;
		}

		//return a cloned segment that starts at 0
		public com.jonlatane.composer.music.Segment normalize() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add( r.minus(_start) );
			}
			return Q;
		}
		public com.jonlatane.composer.music.Segment difference(Segment s) {
			com.jonlatane.composer.music.Segment result = clone();
			
			return result;
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
		
		public K getObjectAt(Rational r) {
			return ((K[])getAllObjectsAt(r).toArray())[0];
		}
		
		public Set<K> getAllObjectsAt(Rational r) {
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
				if( s.contains( r ) ) {
					result = true;
					break;
				}
			}
			return result;
		}
		
		public Set<com.jonlatane.composer.music.Segment> getSegments() {
			return (Set<com.jonlatane.composer.music.Segment>)keySet();
		}
	}
	
	/*
	* Non-intersecting Coverings can be accessed a bit faster than other kinds
	*/
	public class NonIntersectingCovering<K> extends TreeMap<Rhythm.Segment,K>
	implements com.jonlatane.composer.music.Covering<K> {	

		public Set<K> getAllObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			
			Rhythm.Segment ceiling = ceilingKey(tailSegment(r));
			if( ceiling.getEnd().compareTo(r) > 0 )
				result.add(get(ceiling));
			
			return result;
		}
		

		public boolean contains(Rational r) {
			boolean result = false;
			for( Segment s : keySet() ) {
				if( s.getStart().compareTo(r) > 0 )
					break;
				if( s.contains( r ) ) {
					result = true;
					break;
				}
			}
			return result;
		}
		
		public Set<com.jonlatane.composer.music.Segment> getSegments() {
			return (Set<com.jonlatane.composer.music.Segment>)keySet();
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
		
		public Set<K> getAllObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			Rational segmentStart = lowerKey(r);
			if(segmentStart == null) {
				segmentStart = Rational.ZERO;
				put(segmentStart, (K)_m.chromatic());
			}
				
			result.add( get( segmentStart ) );

			return result;
		}
		

		public boolean contains(Rational r) {
			return r.compareTo(Rational.ZERO) >= 0 &&
				r.compareTo(getEnd()) < 0;
		}
		

		public Set<com.jonlatane.composer.music.Segment> getSegments() {
			HashSet<com.jonlatane.composer.music.Segment> result = new HashSet<com.jonlatane.composer.music.Segment>();
			Set<Rational> keys = keySet();
			for(Rational i : keys) {
				Rational start = i;
				Rational end = higherKey(i);
				if( end != null ) {
					result.add( new Segment(start, end) );
				}
			}
			return result;
		}
	}
	
	@Override
	public boolean add(Rational r) {
		assert( r.toDouble() >= 0.0 ) : "Rhythms must only contain positive Rationals";
		return super.add(r);
	}
	
	private SortedSet<Rational> segmentView(Rational start, Rational end) {
		return subSet(start, end);
	}
	private com.jonlatane.composer.music.Segment asSegment() {
		return new Segment(Rational.ZERO, last());
	}
	public Rhythm.Segment tailSegment(Rational start) {
		return this.new Segment(start, last());
	}
	
	public Rational getStart() {
		return Rational.ZERO;
	}
	public Rational getEnd() {
		return last();
	}

	public Rational getLength() {
		return getEnd().minus(getStart());
	}

	public int compareTo(com.jonlatane.composer.music.Segment s) {
		return compareTo(s);
	}
	
	public Rhythm clone() {
		return new Rhythm(this);
	}
	
	public boolean contains(Rational r) {
		return super.contains(r);
	}
	
	public com.jonlatane.composer.music.Segment normalize() {
		return clone();
	}
	
	public Rational last() {
		return last();
	}
	
	public boolean spans(Rational r) {
		return (r.compareTo(getStart()) >= 0) && (r.compareTo(getEnd()) < 0);
	}

	public boolean spans(com.jonlatane.composer.music.Segment s) {
		return spans(s.getStart()) && (spans(s.getEnd())||s.getEnd().equals(getEnd()));
	}
}

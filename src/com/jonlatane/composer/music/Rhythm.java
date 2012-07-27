package com.jonlatane.composer.music;

import java.util.*;
import com.jonlatane.composer.music.harmony.*;

public class Rhythm extends TreeSet<Rational> implements Segment
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
	public class RhythmSegment implements Segment {
		private Rational _start, _end;
		public RhythmSegment(Rational start, Rational end) {
			setStart(start);
			setEnd(end);
		}
		public RhythmSegment( Segment s ) {
			this(s.getStart(), s.getEnd());
		}
		
		public Rational getStart() {
			return _start;
		}
		private void setStart(Rational start) {
			assert( start.compareTo(first()) <= 0 );
			_start = start;
		}
		public Rational getEnd() {
			return _end;
		}
		private void setEnd(Rational end) {
			assert( end.compareTo(last()) >= 0 );
			_end = end;
		}
		
		@Override
		public int compareTo(Segment s) {
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
		
		public boolean spans(Segment s) {
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
		public RhythmSegment subSet(Rational r1, Rational r2) {
			return new RhythmSegment(r1, r2);
		}
		public RhythmSegment headSet(Rational r) {
			return new RhythmSegment(r, getEnd());
		}
		public RhythmSegment tailSet(Rational r) {
			return new RhythmSegment(r, getEnd());
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
		public Segment clone() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add(r);
			}
			return Q;
		}

		//return a cloned segment that starts at 0
		public Segment normalize() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add( r.minus(_start) );
			}
			return Q;
		}
		public Segment difference(RhythmSegment s) {
			Segment result = clone();
			
			return result;
		}
	}
	
	public class RhythmCovering<K> extends TreeMap<RhythmSegment,K>
	implements Covering<K> {
		// Stored for efficiency
		private Segment _span;
		// In this case, will not change anything else (since this is not
		// non-intersecting.  The end of the object's Segment is the beginning
		// of the first Segment after r
		public void add(K obj, Rational r) {
			Rational segEnd = subMap(tailSegment(r), false, tailSegment(lastKey().getEnd()), true).firstKey().getStart();
			put(new RhythmSegment(r, segEnd), obj);
		}
		public void add(K obj, Rational start, Rational end) {
			put(new RhythmSegment(start, end), obj);
		}
		public void add(K obj, Segment s) {
			put(new RhythmSegment(s.getStart(),s.getEnd()),obj);
		}
		public void clear(Segment s) {
			SortedMap<RhythmSegment,K> descMap = headMap(new RhythmSegment(s));
			while( !descMap.isEmpty() ) {
				RhythmSegment descSeg = descMap.lastKey();
				if( descSeg.getEnd().compareTo(s.getStart()) >= 0)
					descSeg.setEnd(s.getStart());
				descMap = descMap.headMap(descMap.lastKey());
			}
			Iterator<RhythmSegment> itr = tailMap(new RhythmSegment(s),false).navigableKeySet().iterator();
			while(itr.hasNext()) {
				RhythmSegment next = itr.next();
				if(next.getStart().compareTo(s.getEnd()) >= 0)
					break;
				else
					next.setStart(s.getEnd());
			}	
		}
		public Segment getSegment(Segment s) {
			return new RhythmSegment(s);
		}
		public CoveredSegment getAsCoveredSegment(Rational start, Rational end) {
			CoveredSegment result = new Voice(/*all()*/);;//.subSet(start, end);
			//TODO
			return result;
		}
		private void updateSpan() {
			_span = new RhythmSegment(firstKey().getStart(), lastKey().getEnd());
		}
		public boolean spans(Object o) {
			if( o instanceof Rational) {
				return _span.spans((Rational)o);
			} else if( o instanceof Segment ) {
				return _span.spans((Segment)o);
			} else if( o instanceof RhythmCovering ) {
				return spans((RhythmCovering)o);
			}
		}
		private boolean spansCovering(RhythmCovering c) {
			return _span.spans(c._span);
		}
		public K getObjectAt(Rational r) {
			return ((K[])getAllObjectsAt(r).toArray())[0];
		}
		
		public Set<K> getAllObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			
			Iterator<Segment> itr = (Iterator<Segment>)headMap(tailSegment(r), true).descendingKeySet().iterator();
			while(itr.hasNext()) {
				Segment s = itr.next();
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
		
		public Segment getRhythm() {
			return getRhythm(false);
		}
		public Segment getRhythm(boolean endInclusive) {
			Segment result = new Rhythm();
			for(Segment s : keySet()) {
				result.add(s.getStart());
				if(endInclusive)
					result.add(s.getEnd());
			}
			return result;
		}
		
		public NavigableSet<Segment> getSegments() {
			return (NavigableSet<Segment>)navigableKeySet();
		}
	}
	
	/*
	* Non-intersecting Coverings can be accessed a bit faster than other kinds
	*/
	public class NonIntersectingCovering<K> extends TreeMap<Rhythm.RhythmSegment,K>
	implements Covering<K> {	

		public Set<K> getAllObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			
			Rhythm.RhythmSegment ceiling = ceilingKey(tailSegment(r));
			if( ceiling.getEnd().compareTo(r) > 0 )
				result.add(get(ceiling));
			
			return result;
		}
		

		public boolean contains(Rational r) {
			boolean result = false;
			for( RhythmSegment s : keySet() ) {
				if( s.getStart().compareTo(r) > 0 )
					break;
				if( s.contains( r ) ) {
					result = true;
					break;
				}
			}
			return result;
		}
		
		public Set<Segment> getSegments() {
			return (Set<Segment>)keySet();
		}
		public Segment getRhythm() {
			return getRhythm(false);
		}
		public Segment getRhythm(boolean segmentEndInclusive) {
			Rhythm result = new Rhythm();
			for( Segment s : keySet() ) {
				result.add(s.getStart());
				if( segmentEndInclusive )
					result.add(s.getEnd());
			}
			return result;
		}
		
	}
	
	
	/*
	* Harmonic Coverings are assumed to be nonintersecting AND complete and can thus be
	* afforded faster access and ease of manipulation
	*/
	public class HarmonicCovering<K extends Chord> extends TreeMap<Rational,K>
	implements Covering<K> {
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
		

		public Set<Segment> getSegments() {
			HashSet<Segment> result = new HashSet<Segment>();
			Set<Rational> keys = keySet();
			for(Rational i : keys) {
				Rational start = i;
				Rational end = higherKey(i);
				if( end != null ) {
					result.add( new RhythmSegment(start, end) );
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
	private Segment asSegment() {
		return new RhythmSegment(Rational.ZERO, last());
	}
	public Rhythm.RhythmSegment tailSegment(Rational start) {
		return this.new RhythmSegment(start, last());
	}
	public Segment getSegment(Rational start, Rational end) {
		return this.new RhythmSegment(start, end);
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

	public int compareTo(Segment s) {
		return compareTo(s);
	}
	
	public Rhythm clone() {
		return new Rhythm(this);
	}
	
	public boolean contains(Rational r) {
		return super.contains(r);
	}
	
	public Segment normalize() {
		return clone();
	}
	
	public Rational last() {
		return last();
	}
	
	public boolean spans(Rational r) {
		return (r.compareTo(getStart()) >= 0) && (r.compareTo(getEnd()) < 0);
	}

	public boolean spans(Segment s) {
		return spans(s.getStart()) && (spans(s.getEnd())||s.getEnd().equals(getEnd()));
	}
	
	public static final Segment ZERO=new Rhythm().new RhythmSegment(Rational.ZERO, new Rational(1,4096));
}

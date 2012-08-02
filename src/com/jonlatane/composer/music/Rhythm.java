package com.jonlatane.composer.music;

import java.lang.reflect.ParameterizedType;
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
	
	/**
	* A RhythmSegment is a view of a subset of a Rhythm implementing the Segment interface.  Operations on a RhythmSegment
	* directly affect the underlying Rhythm and are checked to ensure they fit within the bounds of the Segment.
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
			_start = start;
		}
		public Rational getEnd() {
			return _end;
		}
		private void setEnd(Rational end) {
			_end = end;
		}
		public Rational getLength() {
			return getEnd().minus(getStart());
		}
		
		public boolean spans(Rational r) {
			return (r.compareTo(getStart()) >= 0) && (r.compareTo(getEnd()) < 0);
		}
		public boolean spans(Segment s) {
			return spans(s.getStart()) && (spans(s.getEnd())||s.getEnd().equals(getEnd()));
		}
		
		public int compareTo(Segment s) {
			int result = _start.compareTo(s.getStart());
			if( result == 0 )
				result = _end.compareTo(s.getEnd());
			if( result == 0 ) {
				Iterator<Rational> myIterator = iterator();
				Iterator<Rational> sIterator = s.iterator();
				while( result == 0 ) {
					if( myIterator.hasNext() && sIterator.hasNext() ) {
						result = myIterator.next().compareTo(sIterator.next());
					} else if( !myIterator.hasNext() ) {
						result = -1;
					} else {
						result = 1;
					}
				}
			}
			return result;
		}

		// returns the view of the overlying rhythm this segment represents
		private SortedSet<Rational> getSet() {
			return segmentView(getStart(),getEnd());
		}
		
		// SortedSet methods implemented with getSet()
		public Iterator<Rational> iterator() {
			return getSet().iterator();
		}
		public boolean contains(Object o) {
			return getSet().contains(o);
		}
		public Rational first() {
			return getSet().first();
		}
		public Rational last() {
			return getSet().last();
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
		public boolean add(Rational r) {
			return getSet().add(r);
		}
		public boolean addAll(Collection<? extends Rational> c) {
			return getSet().addAll(c);
		}
		public boolean retainAll(Collection<?> c) {
			return getSet().retainAll(c);
		}
		public int size() {
			return getSet().size();
		}
		public boolean removeAll(Collection<?> c) {
			return getSet().removeAll(c);
		}
		public void clear() {
			getSet().clear();
		}
		public boolean remove(Object o) {
			return getSet().remove(o);
		}
		public boolean isEmpty() {
			return getSet().isEmpty();
		}
		public SortedSet<Rational> headSet(Rational r) {
			return getSet().headSet(r);
		}
		public SortedSet<Rational> tailSet(Rational r) {
			return getSet().tailSet(r);
		}
		public SortedSet<Rational> subSet(Rational r1, Rational r2) {
			return getSet().subSet(r1, r2);
		}
		
		// SEGMENT MANIPULATION COOLNESS! These should be as efficient as possible
		@Override
		public Segment clone() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add(r);
			}
			return Q.new RhythmSegment(this);
		}
		public Segment normalize() {
			Rhythm Q = new Rhythm();
			for( Rational r : this ) {
				Q.add( r.minus(getStart()) );
			}
			Q.add(getEnd());
			return Q;
		}
		
		public Segment difference(Segment s) {
			Segment result = clone();
			//TODO implement this
			return result;
		}
		
		@Override
		public Comparator<? super Rational> comparator() {
			return null;
		}
	}
	
	public class RhythmCovering<K> extends TreeMap<RhythmSegment,K>
	implements Covering<K> {
		private final Class _c = ((Class) ((ParameterizedType) getClass()
		        .getGenericSuperclass()).getActualTypeArguments()[0]);
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
		public CoveredSegment getAsCoveredSegment(Rational start, Rational end) {
			CoveredSegment result = new Voice();
			result.addCovering(this, _c);
			return result;
		}
		
		public Set<K> getAllObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			
			// Look backwards first
			Iterator<RhythmSegment> itr = headMap(longestSegmentStartingAt(r), true).descendingKeySet().iterator();
			while(itr.hasNext()) {
				Segment s = (Segment)itr.next();
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
		
		public NavigableSet<? extends Segment> getSegments() {
			return navigableKeySet();
		}
		@Override
		public K getObjectAt(Rational r) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public CoveredSegment getAsCoveredSegment() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public boolean remove(Segment s) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	/**
	* Non-intersecting Coverings can be accessed a bit faster than other kinds
	*/
	public class NonIntersectingCovering<K> extends RhythmCovering<K>
	implements Covering<K> {	
		
	}
	
	
	/**
	* Harmonic Coverings are assumed to be nonintersecting AND complete and can thus be
	* afforded faster access and ease of manipulation
	*/
	public class HarmonicCovering<K extends Chord> extends TreeMap<Rational,K>
	implements Covering<K> {
		private final Chord.Modulus _m;
		private final Class _c;
		public HarmonicCovering() {
			this(new Chord.Modulus());
		}
		public HarmonicCovering(Chord.Modulus m) {
			_m = m;
			_c = ((Class) ((ParameterizedType) getClass()
			        .getGenericSuperclass()).getActualTypeArguments()[0]);
		}
		public K getObjectAt(Rational r) {
			K result = null;
			Rational segmentStart = floorKey(r);
			if(segmentStart != null) {
				result = get(segmentStart);
			}
			
			return result;
		}
		public Set<K> getAllObjectsAt(Rational r) {
			HashSet<K> result = new HashSet<K>();
			result.add(getObjectAt(r));
			return result;
		}
		

		public boolean contains(Rational r) {
			return r.compareTo(Rational.ZERO) >= 0 &&
				r.compareTo(getEnd()) < 0;
		}
		
		public boolean remove(Segment s) {
			boolean result = false;
			if( keySet().contains(s.getStart()) && 
					(keySet().contains(s.getEnd()) || getEnd().equals(s.getEnd())) )
			{
				result = true;
				remove(s.getStart());
			}
			return result;
		}
		

		public NavigableSet<? extends Segment> getSegments() {
			TreeSet<Segment> result = new TreeSet<Segment>();
			for(Rational i : keySet()) {
				Rational start = i;
				Rational end = higherKey(i);
				if( end != null ) {
					result.add( new RhythmSegment(start, end) );
				} else if( start != getEnd() ) {
					result.add( new RhythmSegment(start, getEnd()));
				}
			}
			return result;
		}
		

		@Override
		public Segment getRhythm() {
			return getRhythm(false);
		}
		
		@Override
		public Segment getRhythm(boolean segmentEndInclusive) {
			Rhythm result = new Rhythm(keySet());
			if(segmentEndInclusive)
				result.add(getEnd()); //TODO Is this good?
			return result;
		}
		
		@Override
		public CoveredSegment getAsCoveredSegment() {
			Voice result = new Voice();
			result.addCovering(this, _c);
			return null;
		}
		@Override
		public void add(K object, Segment s) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void clear(Segment s) {
			// TODO Auto-generated method stub
			
		}
	}
	
	@Override
	public boolean add(Rational r) {
		assert( r.toDouble() >= 0.0 ) : "Rhythms must only contain positive Rationals";
		return super.add(r);
	}
	
	private SortedSet<Rational> segmentView(Rational start, Rational end) {
		return subSet(start, true, end, false);
	}
	
	public Rational getStart() {
		return Rational.ZERO;
	}
	public Rational getEnd() {
		return super.last();
	}
	public Rational getLength() {
		return getEnd().minus(getStart());
	}

	public int compareTo(Segment s) {
		//TODO
		return compareTo(s);
	}
	
	public Rhythm clone() {
		return new Rhythm(this);
	}
	public Segment normalize() {
		return clone();
	}
	
	public boolean contains(Rational r) {
		return super.contains(r) && !r.equals(getEnd());
	}
	
	public Rational last() {
		return lower(last());
	}
	
	public boolean spans(Rational r) {
		return (r.compareTo(getStart()) >= 0) && (r.compareTo(getEnd()) < 0);
	}
	public boolean spans(Segment s) {
		return spans(s.getStart()) && (spans(s.getEnd())||s.getEnd().equals(getEnd()));
	}

	
	static final RhythmSegment ZERO=new Rhythm().new RhythmSegment(Rational.ZERO, new Rational(1,Integer.MAX_VALUE));
	static RhythmSegment longestSegmentStartingAt(Rational r) {
		return new Rhythm().new RhythmSegment(r, new Rational(Integer.MAX_VALUE,1));
	}
	static RhythmSegment shortestSegmentStartingAt(Rational r) {
		return new Rhythm().new RhythmSegment(r, r.plus(new Rational(1, Integer.MAX_VALUE)));
	}
	
	/*public Rhythm.RhythmSegment tailSegment(Rational start) {
		return this.new RhythmSegment(start, last());
	}*/
}

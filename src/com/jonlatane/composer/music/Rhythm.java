package com.jonlatane.composer.music;

import java.util.*;

public class Rhythm extends TreeSet<Rational> implements SortedSet<Rational>, RhythmIterable
{
	public Rhythm() {
		super();
	}
	public Rhythm(Collection<? extends Rational> c) {
		super(c);
	}
	
	@Override
	public boolean add(Rational r) {
		assert( r.toDouble() >= 0.0 ) : "Rhythms must only contain positive Rationals";
		return super.add(r);
	}
	
	public Iterator<Rational> rhythmIterator( Rational start, boolean forward ) {
		if( forward ) {
			return tailSet(start).iterator();
		} else {
			return headSet(start, true).descendingIterator();
		}
	}
	
	public Iterator<Rational> rhythmIterator( Rational start ) {
		return rhythmIterator(start, true);
	}
	public Iterator<Rational> rhythmIterator() {
		return rhythmIterator(Rational.ZERO);
	}
}

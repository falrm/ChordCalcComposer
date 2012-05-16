package com.jonlatane.composer.music;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collection;

public class Rhythm extends TreeSet<Rational> implements SortedSet<Rational>
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
}

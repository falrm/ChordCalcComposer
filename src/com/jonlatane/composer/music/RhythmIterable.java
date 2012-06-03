package com.jonlatane.composer.music;

import java.util.*;

public interface RhythmIterable
{
	public Iterator<Rational> rhythmIterator( Rational start, boolean forward );

	public Iterator<Rational> rhythmIterator( Rational start );
	
	public Iterator<Rational> rhythmIterator();
	
	/*
	 * Provides a synchronized iterator for multiple RhythmIterable objects
	 */
	public class SynchronizedRhythmIterator implements Iterator<Rational> {
		Rhythm _beats = new Rhythm();
		Iterator<Rational> _itr;

		public SynchronizedRhythmIterator(Collection<RhythmIterable> c, Rational start, boolean forward) {
			for( RhythmIterable o: c ) {
				Iterator<Rational> itr  = o.rhythmIterator(start, forward);
				while( itr.hasNext() ) {
					_beats.add(itr.next());
				}
			}
			_itr = _beats.iterator();
		}

		public Rational next() {

		}

		public boolean hasNext() {

		}

		public void remove() {

		}
	}
}

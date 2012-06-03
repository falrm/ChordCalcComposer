package com.jonlatane.composer.music;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class WorkRealization extends Work
{
	public class Staff implements Iterable< Map<Rational,Map<Voice,Voice.MusicalContext>>.Pair >
	{
		private TreeSet<Segment> _segments;

		public Iterator< Map<Rational,Map<Voice,Voice.MusicalContext>>.Entry > iterator() {
			return new Iterator() {
				Iterator<Rational> beatIterator = getRealizedRhythm().iterator();
				public Map<Rational,Map<Voice,Voice.MusicalContext>>.Entry next() {
					Map<Rational,Map<Voice,Voice.MusicalContext>>.Entry result = 
						new Map<Rational,Map<Voice,Voice.MusicalContext>>.Entry() {
						@Override
						public Rational getKey() {
							
						}
						@Override
						public Map<Voice,Voice.MusicalContext> getValue {
							
						}
					};
					return result;
				}
				public boolean hasNext() {
					return beatIterator.hasNext();
				}
				public void remove() {

				}
			};
		}

		public Set<Segment> getSegments() {
			return _segments;
		}
	}
	private Set<Staff> _staves;
	
	public WorkRealization() {
		super();
		_staves = new TreeSet<Staff>();
	}
	
	public SortedSet<Rational> getRealizedRhythm() {
		TreeSet<Rational> result = new TreeSet<Rational>();
		for( Staff s : getStaves() ) {
			Iterator<Map<Voice, Segment.RhythmMapPair>> itr = s.iterator();
			while( itr.hasNext() ) {
				Map<Voice,Segment.RhythmMapPair> m = itr.next();
				for( Map.Entry<Voice,Segment.RhythmMapPair> e : m.entrySet() ) {
					result.add( e.getValue().BEAT );
				}
			}
		}
		return result;
	}
	
	public Set<Staff> getStaves() {
		return _staves;
	}
}

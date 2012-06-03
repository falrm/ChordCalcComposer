package com.jonlatane.composer.music;

import java.util.*;
import android.util.Log;
import android.graphics.drawable.*;

public class Voice implements Iterable< Map<Rational,Voice.MusicalContext>.Entry > {
	public class MusicalContext
	{
		private Rational location = null;
		public final RealizationContext REALIZATION;
		public final VoiceContext VOICE;
		public final WorkContext WORK;
		
		MusicalContext( float location ) {
			this(Rational.nearest(location));
		}
		
		MusicalContext( Rational location) {
			this( location, null );
		}
		
		MusicalContext( Rational location, RealizationContext rc ) {
			if( rc == null ) {
				rc = new RealizationContext();
			}
			this.location = location;
			this.VOICE = new VoiceContext();
			this.WORK = new WorkContext();
			this.REALIZATION = new RealizationContext(rc);
		}
		
		public Rational getLocation() {
			return location;
		}
		
		public class RealizationContext {
			public enum Articulation
			{
				STACCATO, MARCATO, TENUTO, LEGATO, SPICCATO;
			}
			
			private PitchSet _realization;
			private EnumSet<Articulation> _articulations;
			
			public RealizationContext() {
				this(new PitchSet());
			}
			
			public RealizationContext( PitchSet realization ) {
				this(realization, EnumSet.noneOf(Articulation.class));
			}
			public RealizationContext( PitchSet realization, Set<Articulation> articulations ) {
				setRealizationInCover(realization);
				setArticulationInCover(articulations);
			}
			public RealizationContext( RealizationContext rc ) {
				this(rc.getRealization(), rc.getArticulation());
			}
			
			public PitchSet getRealization() {
				return _realization;
			}
			public void setRealizationInCover(PitchSet ps) {
				_realization = ps;
			}
			public void setRealizationHere(PitchSet ps) {
				if( getLocation() != _realizationCover.floorKey( getLocation() ) ) {
					MusicalContext mc = new MusicalContext( getLocation() );
					_realizationCover.put( getLocation(), mc );
					mc.REALIZATION.setRealizationInCover( ps );
				} else {
					setRealizationInCover(ps);
				}
			}
			
			public Rational getRhythmicContext() {
				Rational lower = _rhythm.floor(getLocation());
				if( lower == null )
					lower = Rational.ZERO;
				
				Rational upper = _rhythm.higher(lower);
				if( upper == null )
					upper = lower;
				
				return upper.minus(lower);
			}

			public Set<Articulation> getArticulation() {
				return _articulations;
			}
			public void setArticulationInCover(Set<Articulation> articulations) {
				this._articulations = EnumSet.copyOf(articulations);
			}
			public void setArticulationHere(Set<Articulation> articulations) {
				if( getLocation() != _realizationCover.floorKey( getLocation() ) ) {
					MusicalContext mc = new MusicalContext( getLocation() );
					_realizationCover.put( getLocation(), mc );
					mc.REALIZATION.setArticulationInCover( articulations );
				} else {
					setArticulationInCover(articulations);
				}
			}
			
			public Segment getSegment() {
				Rational lower = _rhythm.floor(getLocation());
				if( lower == null )
					lower = Rational.ZERO;

				Rational upper = _rhythm.higher(lower);
				if( upper == null )
					upper = lower;
				
				return new Segment(Voice.this, lower, upper);
			}
		}

		public class VoiceContext {

			public Chord getChord() {
				return _chordCover.floorEntry(getLocation()).getValue();
			}
			public Scale getScale() {
				return _scaleCover.floorEntry(getLocation()).getValue();
			}
			public Key getKey() {
				return _keyCover.floorEntry(getLocation()).getvalue();
			}
			
			public void setChordInCover(Chord c) {
				_chordCover.floorEntry(getLocation()).setValue(c);
			}
			public void setScaleInCover(Scale s) {
				_scaleCover.floorEntry(getLocation()).setValue(s);
			}
			public void setKeyInCover(Key k) {
				_keyCover.floorEntry(getLocation()).setValue(k);
			}
			
			public void setChordHere(Chord c) {
				_chordCover.put(getLocation(), c);
			}
			public void setScaleHere( Scale s ) {

				_scaleCover.put(getLocation(), s);
			}
			public void setKeyHere(Key k) {
				_keyCover.put(getLocation(), k);
			}
		}

		public class WorkContext {
			Work _parent;
			public WorkContext() {
				this(null);
			}
			public WorkContext(Work w) {
				_parent = w;
			}
		}

		/*
		* Methods for comparing the realization context to the voice context
		*/
		public float getRealizationChordConformity() {
			float result = 0;
			float increment = 1/VOICE.getChord().MODULUS.OCTAVE_STEPS;
			for(Integer i : REALIZATION.getRealization()) {
				if(!VOICE.getChord().contains(i))
					result = result + increment;
			}
			return result;
		}
		public float getRealizationScaleConformity() {
			float result = 0;
			float increment = 1/VOICE.getScale().MODULUS.OCTAVE_STEPS;
			for(Integer i : REALIZATION.getRealization()) {
				if(!VOICE.getScale().contains(i))
					result = result + increment;
			}
			return result;
		}
		
		public PitchSet getRealizationScaleClasses() {
			PitchSet result = new PitchSet();
			//TODO
			return result;
		}
	}

	private Rhythm _rhythm;
	private Chord.Modulus _modulus;
	
	/*
	* Our harmonic context elements each cover the Voice!  All Chords and Scales
	* share our Voice's modulus.
	*/
	public class Cover<T> extends TreeMap<Segment, T>  {
		public Iterator< Map<Rational, >.Entry rhythmIterator {
			
		}
	}
	private TreeMap<Rational, MusicalContext> _realizationCover;
	private TreeMap<Rational, Chord> _chordCover;
	private TreeMap<Rational, Scale> _scaleCover;
	private TreeMap<Rational, Key> _keyCover;
	private void _coverInvariant() {
		assert(_rhythm.headSet(_rhythm.last()).equals(_realizationCover.keySet()))
			: "Realization cover does not fit rhythm";
		assert(_rhythm.headSet(_rhythm.last()).containsAll(_chordCover.keySet()))
			: "Chord cover does not fit rhythm";
		assert(_rhythm.headSet(_rhythm.last()).containsAll(_scaleCover.keySet()))
			: "Chord cover does not fit rhythm";
		for(Chord c : _chordCover.values()) {
			assert(c.MODULUS.equals(this._modulus)) 
				: "Inconsistend chord modulus in voice";
		}
		for(Scale s : _scaleCover.values()) {
			assert(s.MODULUS.equals(this._modulus))
				: "Inconsistent scale modulus in voice";
		}
	}
	
	public Voice() {
		this(new Chord.Modulus());
	}
	public Voice(Chord.Modulus m) {
		this._modulus = m;
		
		// The _rhythm and _realizationCover are tightly coupled - the keySet of _realizationCover
		// should equal _rhythm always.
		this._rhythm = new Rhythm() {
			@Override
			public boolean add(Rational r) {
				if(!_realizationCover.keySet().contains(r)) {
					_realizationCover.put(r, new MusicalContext(r));
				}
				return super.add(r);
			}
			
			@Override
			public boolean remove(Rational r) {
				if(_realizationCover.keySet().contains(r)) {
					_realizationCover.remove(r);
				}
				return super.remove(r);
			}
		};
		
		this._realizationCover = new TreeMap<Rational, MusicalContext>() {
			@Override
			public MusicalContext put(Rational r, MusicalContext mc) {
				getRhythm().add(r);
				mc.location = r;
				return super.put(r,mc);
			}
			@Override
			public MusicalContext remove(Rational r) {
				MusicalContext result = super.remove(r);
				getRhythm().remove(r);
				return result;
			}
		};
		
		
		this._chordCover = new TreeMap<Rational, Chord>() {
			@Override
			public Chord remove(Rational r) {
				assert( !r.equals(Rational.ZERO) ) : "Cannot remove starting chord";
				return super.remove(r);
			}
		};
		_chordCover.put(Rational.ZERO, new Chord(this._modulus));
		
		this._scaleCover = new TreeMap<Rational, Scale>(){
			@Override
			public Scale remove(Rational r) {
				assert( !r.equals(Rational.ZERO) ) : "Cannot remove starting scale";
				return super.remove(r);
			}
		};
		_scaleCover.put(Rational.ZERO, new Scale(this._modulus));
		
		this._keyCover = new TreeMap<Rational, Key>(){
			@Override
			public Chord remove(Rational r) {
				assert( !r.equals(Rational.ZERO) ) : "Cannot remove starting key";
				return super.remove(r);
			}
		};
		_keyCover.put(Rational.ZERO, new Key(this._modulus));
	}
	
	public SortedMap<Rational,Chord> getChords() {
		return _chordCover;
	}

	public SortedMap<Rational,Scale> getScales() {
		return _scaleCover;
	}
	
	public SortedMap<Rational,Key> getKeys() {
		return _keyCover;
	}
	

	public Rhythm getRhythm() {
		return _rhythm;
	}
	
	private Rhythm _realizedRhythm = new Rhythm();
	void updateRealizedRhythm() {
		_realizedRhythm.clear();
		_realizedRhythm.addAll( getRhythm() );
		_realizedRhythm.addAll(getChords().keySet());
		_realizedRhythm.addAll(getScales().keySet());
		_realizedRhythm.addAll(getKeys().keySet());
	}
	public Rhythm getRealizedRhythm() {
		return _realizedRhythm;
	}
	
	public void subdivide(Rational q, Segment s) {
		assert(s.getVoices().contains(this));
		assert(q.compareTo(Rational.ZERO) > 0);
		
		for( Rational i = s.START; i.compareTo(s.END) < 0; i = i.plus(q) ) {
			getRhythm().add(i);
		}
		
		updateRealizedRhythm();
		
	}
	
	public boolean definedOn(Rational r) {
		return r.compareTo(Rational.ZERO) > 0 && r.compareTo(getRhythm().last()) < 0;
	}
	
	public MusicalContext getMusicalContextAt(Rational r) {
		assert(r.compareTo(_rhythm.last()) < 0) 
			: "Number provided not in domain (rhythm) of voice";
		
		// Ensure that if the context is in the rhythm that it's also in the
		// realizationCover (otherwise, we add a rest with no articulaion)
		Rational floor = _rhythm.floor(r);
		if(!_realizationCover.keySet().contains(floor)) {
			_realizationCover.put(floor, new MusicalContext(floor));
		}
		
		MusicalContext result = _realizationCover.floorEntry(r).getValue();
		
		return result;
	}
	
	public Segment segment() {
		return segment(_rhythm.first(),_rhythm.last());
	}
	
	public Segment segment(Rational r1, Rational r2) {
		assert(r1.compareTo( r2 ) < 0 &&
				r1.compareTo( _rhythm.last() ) < 0 &&
				r2.compareTo( _rhythm.last() ) < 0);
		return new Segment(this, r1, r2);
	}
	
	public Iterator< Map<Rational,Voice.MusicalContext>.Entry > iterator() {
		return new Iterator< Map<Rational,Voice.MusicalContext>.Entry >() {
			Iterator<Rational> itr = getRealizedRhythm().iterator();
			public Map<Rational,Voice.MusicalContext>.Entry next() {
				return new Map<Rational,Voice.MusicalContext>.Entry() {
					@Override
					Rational myBeat = itr.next();
					public Rational getKey() {
						return myBeat;
					}
					
					@Override
					public Voice.MusicalContext getValue() {
						return getMusicalContextAt(myBeat);
					}
					
					@Override
					public void setValue(Voice.MusicalContext v) {
						getMusicalContextAt(myBeat).REALIZATION.setRealization( v.REALIZATION.getRealization() );
					}
				};
			}
			
			public boolean hasNext() {
				return itr.hasNext();
			}
			
			public void remove() {
				
			}
		};
	}
}

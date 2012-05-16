package com.jonlatane.composer.music;

import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.Vector;

public class Voice implements SortedMap<Rational, Voice.MusicalContext> {
	public class MusicalContext
	{
		private Rational location = null;
		private Float realLocation = null;
		private RealizationContext REALIZATION;
		private VoiceContext VOICE;
		private WorkContext WORK;
		
		MusicalContext( float location ) {
			this(Rational.nearest(location));
			realLocation = location;
		}
		
		MusicalContext( Rational location) {
			this.location = location;
			this.VOICE = new VoiceContext();
			this.WORK = new WorkContext();
			this.REALIZATION = new RealizationContext();
		}
		
		public Rational getRationalLocation() {
			return location;
		}
		
		public float getRealLocation() {
			if(realLocation != null) {
				return realLocation;
			} else {
				return (float)location.toDouble();
			}
		}
		
		public void reRealize( PitchSet realization ) {
			REALIZATION = new RealizationContext(realization, REALIZATION.getArticulation());
		}
		
		public void reRealize( PitchSet realization, Set<Articulation> articulations ) {
			REALIZATION = new RealizationContext(realization, articulations);
		}
		
		public void reRealize(RealizationContext original) {
			REALIZATION = new RealizationContext(original);
		}
		
		public class RealizationContext {
			public enum Articulation
			{
				STACCATO, MARCATO, TENUTO, LEGATO, SPICCATO;
			}
			
			public RealizationContext() {
				this(PitchSet.REST);
			}
			
			public RealizationContext( PitchSet realization ) {
				this(realization, EnumSet.noneOf(Articulation.class));
			}
			public RealizationContext( PitchSet realization, Set<Articulation> articulations ) {
				setRealization(realization);
				setArticulation(articulations);
			}
			public RealizationContext( RealizationContext rc ) {
				this(rc.getRealization(), rc.getArticulation());
			}
			
			private PitchSet _realization;
			public PitchSet getRealization() {
				return _realization;
			}
			public void setRealization(PitchSet ps) {
				_realization = ps;
			}
			
			public Rational getRhythmicContext() {
				Rational lower = _rhythm.floor(getRationalLocation());
				if( lower == null )
					lower = Rational.ZERO;
				
				Rational upper = _rhythm.higher(lower);
				if( upper == null )
					upper = lower;
				
				return upper.minus(lower);
			}

			private EnumSet<Articulation> _articulations = EnumSet.noneOf(Articulation.class);
			public Set<Articulation> getArticulation() {
				return _articulations;
			}
			public void setArticulation(Set<Articulation> articulations) {
				this._articulations = EnumSet.copyOf(articulations);
			}
			
			public Segment getSegment() {
				Rational lower = _rhythm.floor(getRationalLocation());
				if( lower == null )
					lower = Rational.ZERO;

				Rational upper = _rhythm.higher(lower);
				if( upper == null )
					upper = lower;
				
				return new Segment(Voice.this, lower, upper);
			}
		}

		public class VoiceContext {

			private Chord _chord;
			private Scale _scale;

			public VoiceContext() {
				/* Realize information from the voice here */
				_chord = _chordCover.floorEntry(getRationalLocation()).getValue();
				_scale = _scaleCover.floorEntry(getRationalLocation()).getValue();
			}
			
			private void invariant() {
				assert(_chord.MODULUS.OCTAVE_STEPS == _scale.MODULUS.OCTAVE_STEPS);
			}

			public Chord getChord() {
				return _chord;
			}
			public Scale getScale() {
				return _scale;
			}
		}

		public class WorkContext {
			public WorkContext() {
				/* Realize information from the parent work context map here */
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
			
		}
	}

	private Rhythm _rhythm;
	private Chord.Modulus _modulus;
	
	/*
	* Our harmonic context elements each cover the Voice!  All Chords and Scales
	* share our Voice's modulus.
	*/
	private TreeMap<Rational, MusicalContext> _realizationCover;
	private TreeMap<Rational, Chord> _chordCover;
	private TreeMap<Rational, Scale> _scaleCover;
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
		this._rhythm = new Rhythm();
	}
	
	public Rhythm getRhythm() {
		return _rhythm;
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
	
	// Copy the given Realization Context (notes and articulation) to the current
	// point in the voice
	public void reRealize(Rational r, MusicalContext.RealizationContext rc) {
		getMusicalContextAt(r).reRealize( rc );
	}
	
	public MusicalContext getMusicalContextAt(float f) {
		return getMusicalContextAt(Rational.nearest(f));
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
}

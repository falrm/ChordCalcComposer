package com.jonlatane.composer.music;

import java.util.*;
import android.util.Pair;
import com.jonlatane.composer.music.harmony.*;

public class Voice {
	private Meter _meter;
	private RhythmMap<PitchSet> _realization;
	private RhythmMap<Chord> _chords;
	private RhythmMap<Scale> _scales;
	private RhythmMap<Key> _keys;
	
	public Voice() {
		this(new Meter());
	}
	
	// Access point for Staves/Scores
	Voice(Meter m) {
		_meter = m;
		_realization = new RhythmMap<PitchSet>();
		_chords = new RhythmMap<Chord>();
		_scales = new RhythmMap<Scale>();
		_keys = new RhythmMap<Key>();
	}
	
	public RhythmMap<PitchSet> getRealization() {
		return _realization;
	}
	public RhythmMap<Chord> getChords() {
		return _chords;
	}
	public RhythmMap<Scale> getScales() {
		return _scales;
	}
	public RhythmMap<Key> getKeys() {
		return _keys;
	}
	
	// This is all to optimize sequential access to a voice.  Is is stupid?
	private Iterator<Map.Entry<Rational,PitchSet>> __notes;
	private Map.Entry<Rational,PitchSet> __nextNote;
	private Iterator<Map.Entry<Rational,Chord>> __chords;
	private Map.Entry<Rational,Chord> __nextChord;
	private Iterator<Map.Entry<Rational,Scale>> __scales;
	private Map.Entry<Rational,Scale> __nextScale;
	private Iterator<Map.Entry<Rational,Key>> __keys;
	private Map.Entry<Rational,Key> __nextKey;
	
	private Rational __previousResult;
	// Works like an internal, self-contained iterator.  Returns null if empty.
	public Pair<Rational, Collection<Object>> nextChangeAfter(Rational r) {
		HashSet<Object> resultRHS = new HashSet<Object>();
		if ((__previousResult == null) || (!r.equals(__previousResult))) {
			// we must initialize everything and do it the hard way
			__notes = _realization._data.tailMap(r, false).entrySet().iterator();
			if( __notes.hasNext() ) {
				__nextNote = __notes.next();
			} else {
				__nextNote = null;
			}
			__chords = _chords._data.tailMap(r, false).entrySet().iterator();
			if (__chords.hasNext()) {
				__nextChord = __chords.next();
			} else {
				__nextChord = null;
			}
			__scales = _scales._data.tailMap(r, false).entrySet().iterator();
			if (__scales.hasNext()) {
				__nextScale = __scales.next();
			} else {
				__nextScale = null;
			}
			__keys = _keys._data.tailMap(r, false).entrySet().iterator();
			if (__keys.hasNext()) {
				__nextKey = __keys.next();
			} else {
				__nextKey = null;
			}
		} //otherwise everything is in place.
		
		HashSet<Rational> h = new HashSet<Rational>();
		for(Rational check : new Rational[] {__nextNote.getKey(), __nextChord.getKey(), __nextScale.getKey(), __nextKey.getKey()}) {
			if( check != null ) {
				h.add(check);
			}
		}
		Rational next = Collections.min(h);
		
		if(next.equals( __nextNote.getKey())) {
			resultRHS.add(__nextNote.getValue());
			if( __notes.hasNext() ) {
				__nextNote = __notes.next();
			} else {
				__nextNote = null;
			}
		}
		if(next.equals( __nextChord.getKey())) {
			resultRHS.add(__nextChord.getValue());
			if (__chords.hasNext()) {
				__nextChord = __chords.next();
			} else {
				__nextChord = null;
			}
		}
		if(next.equals( __nextScale.getKey())) {
			resultRHS.add(__nextScale.getValue());
			if (__scales.hasNext()) {
				__nextScale = __scales.next();
			} else {
				__nextScale = null;
			}
		}
		if(next.equals( __nextKey.getKey())) {
			resultRHS.add(__nextKey.getValue());
			if (__keys.hasNext()) {
				__nextKey = __keys.next();
			} else {
				__nextKey = null;
			}
		}
		
		__previousResult = next;
		return new Pair<Rational,Collection<Object>>(next, resultRHS);
	}
	
	public interface VoicePoint {
		public PitchSet getTones();
		public Chord getChord();
		public Scale getScale();
		public Key getKey();
		public Rational getCurrentBeat();
	}
	
	public VoicePoint getPoint(Rational r) {
		final Rational rPrime = new Rational(r.numerator(), r.denominator());
		return new VoicePoint() {
			public PitchSet getTones(){
				return getRealization().getObjectAt(rPrime);
			}
			public Chord getChord(){
				return getChords().getObjectAt(rPrime);
			}
			public Scale getScale() {
				return getScales().getObjectAt(rPrime);
			}
			public Key getKey() {
				return getKeys().getObjectAt(rPrime);
			}
			public Rational getCurrentBeat() {
				return _meter.getBeatOf(rPrime);
			}

		};
		

	}
}

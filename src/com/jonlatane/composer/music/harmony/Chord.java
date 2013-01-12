package com.jonlatane.composer.music.harmony;

import java.util.*;
import java.util.regex.*;

/**
 * Chords work much like PitchSets, but they have a Modulus: the number of tones an octave is divided into. Ex: 
 * Given a 12-tone octave, a Chord with both 0 (C4) and 12 (C5) is identical to a Chord with only one of the two.
 * They also have a root.  @Chord.Inversion has a bass as well.  
 * 
 * Chords can be constructed from semantic information in Android Resources files.  Every chord
 * has a name.  The name and contents themselves are synchronized via updateName() and updateContents()
 * 
 */
public class Chord extends PitchSet {
 	/**
	* The Modulus of a chord represents the number of tones in its octave. It exists in its most basic
	* form at the Chord level.
	*/
	public static class Modulus {
		public final int OCTAVE_STEPS;
		public Modulus() {
			this(12);
		}
		public Modulus(int i) {
			this.OCTAVE_STEPS = i;
		}

		public int getPitchClass(int i) {
			return i % OCTAVE_STEPS;
		}
		public int hashCode() {
			return OCTAVE_STEPS;
		}
		
		private static final HashMap<Integer,Key> _chromatics = new HashMap<Integer,Key>();
		public Key chromatic() {
			Key result;
			if( _chromatics.containsKey(OCTAVE_STEPS) ) {
				result = _chromatics.get(OCTAVE_STEPS);
			} else {
				result = new Key(this);
				for(int i = 0; i < OCTAVE_STEPS; i++) {
					result.add(i);
				}
				_chromatics.put(this.OCTAVE_STEPS,result);
			}
			return result;
		}
	}	

	public static Modulus TWELVETONE = new Modulus();
	public final Modulus MODULUS;
 	private Integer root;
	String name;
	
	private Chord() {MODULUS=new Modulus();}

	public Chord(Modulus m) {
		this(Collections.EMPTY_SET, m);
	}
	
	public Chord(Collection<Integer> c) {
		this(c, new Modulus());
	}
	
	public Chord(Collection<Integer> c, Modulus m) {
		super(c);
		this.MODULUS = m;
	}
	
	public Chord(Chord c) {
		super(c);
		this.MODULUS = c.MODULUS;
	}
	
	/**
	 * Adds the class of the given pitch, based on Modulus, to the Chord
	 *
	 * @return true if the Chord was modified by the operation
	 */
	@Override
	public boolean add( Integer i ) {
		return super.add(MODULUS.getPitchClass(i));
	}
	
	/**
	* Adds the classes of the given pitches, based on Modulus, to the Chord
	*
	* @return true if the Chord was modified by the operation
	*/
	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		boolean result = false;
		for(Integer i : c) {
			result = result || add(i);
		}
		return result;
	}

	/**
	 * Determines if the pitch class of the given pitch is in the Scale
	 * 
	 * @return true if the Chord contains the class of the pitch
	 */
	public boolean contains( Integer i ) {
		return super.contains(MODULUS.getPitchClass(i));
	}
	
	public void setRoot(int i) {
		this.root = i;
	}
	
	public Integer getRoot() {
		return root;
	}
	
	void invariant() {
		assert( true );
	}
	
	public String toString(Key k) {
		assert(k.MODULUS.OCTAVE_STEPS == this.MODULUS.OCTAVE_STEPS);
		String result = "";
		
		if( this.MODULUS.OCTAVE_STEPS == 12 ) {
			//TODO
		} else {
			result = "Not12Tone";
		}
		return result;
	}
	
	/*
	* Convenience methods
	*/
	public boolean isTriad() {
		return size() == 3;
	}
	
	// For 12-tone systems.
	public int guessRoot() {
		assert(MODULUS.OCTAVE_STEPS == 12);
		int result = 0;
		int certainty = 0;
		
		//Here is some voodoo magic numbers crap.  We check
		//each note for how much we think it's root based on its internal structure
		for(Integer i : this) {
			int tmpCertainty = 0;
			// 3s
			if(contains(i + 3) || contains(i + 4)) {
				tmpCertainty += 5;
			}
			// 7s
			if(contains(i +10) || contains(i + 11)) {
				tmpCertainty += 4;
			}
			// 5s
			if(contains(i +10) || contains(i + 11)) { //dim/P
				tmpCertainty += 3;
			}
			// 9s
			if(contains(i + 1) || contains(i + 2)) { //dim/P
				tmpCertainty += 2;
			}
			// 11s, 13s
			if(contains(i + 1) || contains(i + 2)) { //dim/P
				tmpCertainty += 1;
			}
			
			// Update result if necessary
			if(tmpCertainty > certainty) {
				result = i;
				certainty = tmpCertainty;
			}
		}
		return result;
	}
	
	public static Chord getChordByName(String s) {
		Chord result = new Chord();
		// EX: F+M7#6#9
		Pattern p = Pattern.compile("(A|B|C|D|E|F|G)(#|b)?(M|m)(7)?((?:b|#\\d)");
		Matcher m = p.matcher(s);
		if(m.matches()) {
			for(int i = 0; i < m.groupCount(); i = i + 1) {
				
			}
		}
		return result;
	}
}

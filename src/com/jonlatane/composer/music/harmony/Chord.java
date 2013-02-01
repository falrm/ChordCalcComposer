package com.jonlatane.composer.music.harmony;

import java.util.*;
import java.util.regex.*;

import android.util.Log;
import android.util.Pair;
//import android.util.SparseArray;

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
 	private static final String TAG = "Chord";
	private static final long serialVersionUID = 6734588916693899323L;

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
			int result = i % OCTAVE_STEPS;
			if(result < 0)
				result += OCTAVE_STEPS;
			return result;
		}
		public int hashCode() {
			return OCTAVE_STEPS;
		}
		
		/*private static final SparseArray<Key> _chromatics = new SparseArray<Key>();
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
		}*/
	}	

	public static Modulus TWELVETONE = new Modulus();
	public final Modulus MODULUS;
 	private Integer _root;
	String name;
	
	public Chord() {
		super();
		MODULUS=new Modulus();
	}
	
	public Chord(Collection<Integer> c) {
		super();
		MODULUS=new Modulus();
		for(Integer i : c) {
			add(MODULUS.getPitchClass(i));
		}
	}
	
	public Chord(Chord c) {
		this.MODULUS = c.MODULUS;
		setRoot( c.getRoot() );
		for(Integer i : c) {
			add(MODULUS.getPitchClass(i));
		}
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
			result = result || add(MODULUS.getPitchClass(i));
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
		this._root = MODULUS.getPitchClass(i);
	}
	
	public Integer getRoot() {
		return _root;
	}
	
	
	/**
	 *  This utility function works in the case of major, minor, augmented, augmented and diminished triads with the EXCEPTION OF A FULLY DIMINISHED 7 CHORD.
	 *  Those are weird and need color tones named separately but will obviously point towards one of the diminished scales when we start adding voices.
	 * @param c
	 * @param root
	 * @return
	 */
	private static Pair<String, Integer> nameSixSevensColors(Chord c, Integer root) {
		String name = "";
		int certainty = 0;
		
		// b7 - this is very likely a 7, 7#5, -7, -7b5 or -7#5 chord and we have some naming conventions here based on the presence of color tones
		if (c.contains(root + 10)) {
			Log.i(TAG,"b7 is in the chord!");
			certainty += 8;

			// 13 and 11/9 present.  We can call it a 13 chord.
			if (c.contains(root + 9) && (c.contains(root + 5) || (c.contains(root + 2)))) {
				name = name + "13";
			// 13 and no 11/9.  It's a 6 chord
			} else if(c.contains(root+9)) {
				name = name + "7add6";
			// 11 present.  Call it an 11 chord, but only if there's a 3 in the chord.  Otherwise it may be a sus
			} else if (c.contains(root + 5) && (c.contains(root+3) || c.contains(root+4))) {
				name = name + "11";
			// 9 present
			} else if (c.contains(root + 2)) {
				name = name + "9";
			// boring old 7 chord
			} else {
				name = name + "7";
			}
			
			// name color tones
			// #11/b5
			if (c.contains(root + 6) && c.contains(root + 7)) {
				name = name + "#11";
			} else if(c.contains(root + 6)) {
				name = name + "b5";
			}
			
			// b13(b6)/#5
			if (c.contains(root + 8) && (c.contains(root + 7) || c.contains(root+6))) {
				name = name + "b13";
			} else if(c.contains(root+8)) {
				name = name + "#5";
			}
			
			// #9.  Avoid a b3 as this is already named
			if (c.contains(root + 3) && c.contains(root+4)) {
				name = name + "#9";
			}
			
			// #7 is very unlikely so make this less certain.
			if (c.contains(root + 11)) {
				name = name + "#7";
				certainty -= 4;
			}
			
			// b9 is the only damn easy thing to add
			if (c.contains(root + 1)) {
				name = name + "b9";
			}
		// No b7 and an M7 OR M6 (with no diminished triad underneath)- very likely a M7, M7#5, -M7 or -M7b5 chord, or corresponding 6 chords
		} else if (c.contains(root + 11) || (c.contains(root+9))) {
			Log.i(TAG,"M7 or M6 is in the chord!");
			// 13 and 11/9 present.  We can call it a 13 chord.
			if (c.contains(root + 9) && (c.contains(root + 5) || (c.contains(root + 2)))) {
				name = name + "M13";
			// 13 and no 11/9. It's a 6 chord
			} else if (c.contains(root + 9) && c.contains(root + 11)) {
				name = name + "M7add6";
			// 11 present. Call it an 11 chord, but only if there's a 3 in the chord. Otherwise it may be a sus
			} else if (c.contains(root + 5) && (c.contains(root + 3) || c.contains(root + 4))) {
				name = name + "M11";
			// 9 present
			} else if (c.contains(root + 2) && (c.contains(root + 3) || c.contains(root + 4))) {
				name = name + "M9";
			// boring old 7 chord
			} else if (c.contains(root + 11)){
				name = name + "M7";
			// 6 chord
			} else {
				name = name + "6";
			}

			// name color tones
			// #11/b5
			if (c.contains(root + 6) && c.contains(root + 7)) {
				name = name + "#11";
			} else if (c.contains(root + 6)) {
				name = name + "b5";
			}

			// b13/#5
			if (c.contains(root + 8) && c.contains(root + 7)) {
				name = name + "b13";
			} else if (c.contains(root + 8)) {
				name = name + "#5";
			}

			// #9. Avoid a b3 as this is already named
			if (c.contains(root + 3) && c.contains(root + 4)) {
				name = name + "#9";
			}

			// b9 is the only damn easy thing to add
			if (c.contains(root + 1)) {
				name = name + "b9";
			}
		// No 7 present, just list color tones
		} else {
			Log.i(TAG,"No 7 in the chord");
			// #11/b5
			if (c.contains(root + 6) && c.contains(root + 7)) {
				name = name + "#11";
			} else if (c.contains(root + 6)) {
				name = name + "b5";
			}
			
			if (c.contains(root + 5) && (c.contains(root + 4) || c.contains(root+3))) {
				name = name + "add11";
			}

			// b13(b6)/#5
			if (c.contains(root + 8) && c.contains(root + 7)) {
				name = name + "b13";
			} else if (c.contains(root + 8)) {
				name = name + "#5";
			}

			// #9. Avoid a b3 as this is already named
			if (c.contains(root + 3) && c.contains(root + 4)) {
				name = name + "#9";
			}
			
			if (c.contains(root + 2)) {
				name = name + "add9";
			}
			// b9 is always a b9.
			if (c.contains(root + 1)) {
				name = name + "b9";
			}
		}

		return new Pair<String, Integer>(name, certainty);
	}
	
	 
	
	
	/**
	 * Given the provided root, returns the name (excluding the root as it may be enharmonically named) of the chord and a "score" of how likely it is that
	 * the chord is the one guessed.
	 * 
	 * @param c
	 * @param root
	 * @return
	 */
	static Pair<String, Integer> guessName(Chord c, Integer root) {
		String rootName = "";
		String name = rootName;
		int certainty = 0;
		
		
		Log.i(TAG,"Guessing name for chord: " + c.toString() + "R:" + root);
		
		// Root - bonus points for having it!  Though maybe we can find shell voicings using the stuff below this, a 3 and a flat 7 are better than just the root
		if (c.contains(root)) {
			certainty += 10;
			Log.i(TAG,"Root is in the chord!");
		}
		
		// M3, with or without a 5
		if (c.contains(root + 4)) {
			Log.i(TAG,"M3 is in the chord!");
			certainty += 10;
			
			// M3P5 - major triad present
			if(c.contains(root+7)) {
				Log.i(TAG,"P5 is in the chord!");
				certainty += 8;
			// M3#5 - augmented chord
			} else if (c.contains(root + 8)) {
				Log.i(TAG,"#5 is in the chord!");
				certainty += 7;
				//name = name + "+";
			// M3b5 - unlikely that this is the root unless this is a colorful chord
			} else if(c.contains(root + 6)) {
				Log.i(TAG,"b5 is in the chord!");
				certainty = certainty + 1;
			}
			
			Pair<String, Integer> decorations = nameSixSevensColors(c, root);
			name = name + decorations.first;
			certainty += decorations.second;
			
			
		// m3 (and no M3 - if both are present we don't get here and it becomes a #9)
		} else if (c.contains(root + 3)) {
			Log.i(TAG,"m3 (no M3) is in the chord!");
			certainty += 10;
			
			// m3P5 - minor triad, name it
			if (c.contains(root + 7)) {
				Log.i(TAG,"m3P5 is in the chord!");
				certainty += 8;

				Pair<String, Integer> decorations = nameSixSevensColors(c, root);
				name = name + "-" + decorations.first;
				certainty += decorations.second;
				
			// m3#5 - minor augmented chord. unlikely, but name it
			} else if (c.contains(root + 8)) {
				Log.i(TAG,"m3#5 is in the chord!");
				certainty -= 4;

				Pair<String, Integer> decorations = nameSixSevensColors(c, root);
				name = name + "-" + decorations.first;
				certainty += decorations.second;

			// m3b5 - dim chord. FML
			} else if (c.contains(root + 6)) {
				Log.i(TAG,"m3b5 is in the chord!");
				// avoid naming, for instance, an Ab7 a C-3b5b6
				if(!c.contains(root + 8))
					certainty += 7;
				
				// d7-dim7 chord needs to be caught first - name color tones specially here because this is probably an octatonic scale
				if(c.contains(root + 9)) {
					name = name + "o7";
					certainty += 7;
					
					// b9
					if (c.contains(root + 1)) {
						name = name + "b9";
					}
					// add9
					if (c.contains(root + 2)) {
						name = name + "add9";
					}
					// b13(b6)
					if (c.contains(root + 8)) {
						name = name + "b13";
					}
					// add4
					if(c.contains(root+5)) {
						name = name + "add4";
					}
					
					// #7 is very unlikely so make this less certain.
					if (c.contains(root + 11)) {
						name = name + "add#7";
						certainty -= 4;
					}
					// b7 is very unlikely so make this less certain.
					if (c.contains(root + 10)) {
						name = name + "addb7";
						certainty -= 4;
					}
					
					
				// m3b5b7 (half-dim), name it
				} else if( c.contains(root + 10 )){
					// half-dim
					certainty += 7;
					
					Pair<String, Integer> decorations = nameSixSevensColors(c, root);
					name = name + "-" + decorations.first;
					certainty += decorations.second;
				
				// m3b5M7 - dim major 7 chord
				} else if( c.contains(root + 11 )){
					// half-dim
					certainty += 2;
					
					Pair<String, Integer> decorations = nameSixSevensColors(c, root);
					name = name + "-" + decorations.first;
					certainty += decorations.second;
				} else {
					Pair<String, Integer> decorations = nameSixSevensColors(c, root);
					name = name + "-" + decorations.first;
					certainty += decorations.second;
				}
			}

		// No 3 present - There is no third in this chord, let's look at other possibilities
		} else {
			// sus4
			if(c.contains(root+5)) {
				certainty += 7;
				
				Pair<String, Integer> decorations = nameSixSevensColors(c, root);
				name = name + decorations.first + "sus";
				certainty += decorations.second;
			// sus2
			} else if(c.contains(root + 2)) {
				certainty += 7;
				
				Pair<String, Integer> decorations = nameSixSevensColors(c, root);
				name = name + decorations.first + "sus2";
				certainty += decorations.second;
			// 5 chord
			} else if(c.contains(root+7)) {
				// P5b7 - enough to call it dominant in the absence of a third
				if(c.contains(root+10)) {
					//name = name + 
				}
				certainty += 8;
				name = name + "5";
			} else {
				Pair<String, Integer> decorations = nameSixSevensColors(c, root);
				name = name + "No345" + decorations.first;
				certainty += decorations.second;
			}
		}
		
		return new Pair<String, Integer>(name, certainty);
	}
	
	
	
	// For 12-tone systems.
	public static int guessRoot(Chord c) {
		int result = 0;
		int bestCertainty = 0;
		
		//Here is some voodoo magic numbers crap.  We check
		//each note for how much we think it's root based on its internal structure
		for(Integer i : c) {
			Pair<String, Integer> p = guessName(c, i);
			
			// Update result if necessary
			if(p.second > bestCertainty) {
				result = i;
				bestCertainty = p.second;
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

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
 * In array output from Chords in 1357 format, -1 is used to imply that the element is not present.  Be wary
 * as this is not filtered by @Modulus.getPitchClassOf .
 * 
 * Some of a Chord's more interesting functions include:
 * 
 * @schenkerianToInt(String) : convert a b7 to a 10, etc.
 * @to1357() : convert a Chord to an int[], e.g., [C, E, G, -1, D] for a Cadd9 chord
 * @getCharacteristic and @guessCharacteristic, for guessing the characteristic of a chord
 * 
 */
public class Chord extends PitchSet {
 	private static final String TAG = "Chord";
	private static final long serialVersionUID = 6734588916693899323L;
	public static final String diminished = "\u00B0";
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
		
		/**
		 * Return the pitch class of the given note.  For a 12 Modulus this means a number 0-11.
		 * For heptatonic work, it is 0-6.
		 * @param i a note, where C4 = 0, B4 = 11
		 * @return a number between 0 and OCTAVE_STEPS - 1
		 */
		public int mod(int i) {
			while(i < 0)
				i += OCTAVE_STEPS;
			return i % OCTAVE_STEPS;
		}
		
		/**
		 * Return the octave of a given note.  For 0-11, this is 4 (the middle octave),
		 * 5 for the next twelve and so on.
		 * 
		 * @param i a note, where C4 = 0, B4 = 11
		 * @return the octave of the note (4)
		 */
		public int octave(int i) {
			int octave = 4;
			int n = mod(i);
			while(i < n) {
				i += OCTAVE_STEPS;
				octave -= 1;
			}

			while(i > n) {
				i -= OCTAVE_STEPS;
				octave += 1;
			}
			return octave;
		}
		
		/**
		 * Returns the smallest distance between the two provided numbers
		 * @param i1
		 * @param i2
		 * @return
		 */
		public int distance(int i1, int i2) {
			int result1 = mod(i1-i2);
			int result2 = mod(i2-i1);
			return Math.min(result1, result2);
		}
		

		public static int absoluteHeptDistance(String s1, String s2) {
			int s1_octave = (int)s1.charAt(s1.length() - 1);
			int s2_octave = (int)s2.charAt(s2.length() - 1);
			int s1_heptClass = toHeptatonicNumber(s1.charAt(0));
			int s2_heptClass = toHeptatonicNumber(s2.charAt(0));
			
			return (s2_heptClass - s1_heptClass) + 7 * (s2_octave - s1_octave);
		}
		
		public int hashCode() {
			return OCTAVE_STEPS;
		}
		
		/**
		 * Convert the given character (a,b,c,d,e,f,g, upper or lower case) to the numers 0-6
		 * (heptatonic pitch classes).
		 * 
		 * @param c a letter a-g
		 * @return an int 0-6
		 */
		public static int toHeptatonicNumber(char c) {
			switch (c) {
			case 'A':
				return 5;
			case 'a':
				return 5;
			case 'B':
				return 6;
			case 'b':
				return 6;
			case 'C':
				return 0;
			case 'c':
				return 0;
			case 'D':
				return 1;
			case 'd':
				return 1;
			case 'E':
				return 2;
			case 'e':
				return 2;
			case 'F':
				return 3;
			case 'f':
				return 3;
			case 'G':
				return 4;
			case 'g':
				return 4;
			}
			return -1;
		}
		
		/**
		 * 
		 * @param i
		 * @return
		 */
		public static char toHeptatonicCharacter(int i) {
			i = HEPTATONIC.mod(i);
			switch(i) {
			case 0: return 'C';
			case 1: return 'D';
			case 2: return 'E';
			case 3: return 'F';
			case 4: return 'G';
			case 5: return 'A';
			case 6: return 'B';
			}
			return '#';
		}
	}	

	public static final Modulus TWELVETONE = new Modulus();
	public static final Modulus HEPTATONIC = new Modulus(7);
	public static final Chord NO_CHORD = new Chord();
	
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
			add(MODULUS.mod(i));
		}
	}
	
	public Chord(Chord c) {
		this.MODULUS = c.MODULUS;
		setRoot( c.getRoot() );
		for(Integer i : c) {
			add(MODULUS.mod(i));
		}
	}
	
	/**
	 * Adds the class of the given pitch, based on Modulus, to the Chord
	 *
	 * @return true if the Chord was modified by the operation
	 */
	@Override
	public boolean add( Integer i ) {
		return super.add(MODULUS.mod(i));
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
			result = result || add(MODULUS.mod(i));
		}
		return result;
	}

	@Override
	public boolean contains( Object o ) {
		boolean result = super.contains(MODULUS.mod((Integer)o));
		return result;
	}
	
	@Override
	public Integer lower(Integer i) {
		i = MODULUS.mod(i);
		Integer result = super.lower(i);
		if(result == null)
			result = super.lower(i + MODULUS.OCTAVE_STEPS);
		return result;
	}
	@Override
	public Integer floor(Integer i) {
		i = MODULUS.mod(i);
		Integer result = super.floor(i);
		if(result == null)
			result = super.floor(i + MODULUS.OCTAVE_STEPS);
		return result;
	}
	@Override
	public Integer ceiling(Integer i) {
		i = MODULUS.mod(i);
		Integer result = super.ceiling(i);
		if(result == null)
			result = super.ceiling(i - MODULUS.OCTAVE_STEPS);
		return result;
	}
	
	
	public void setRoot(Integer i) {
		if( i != null) {
			add(i);
			this._root = MODULUS.mod(i);
		}
		else
			this._root = null;
	}
	
	public Integer getRoot() {
		return _root;
	}
	
	/**
	 * "Completing" the chord means adding anything that is assumed by the chord's characteristic.  Essentially,
	 * this means adding P5s to voicings of chords that are missing them although other results may be possible.
	 * 
	 * A chord must have a defined root to have a completion.
	 * 
	 * @return
	 */
	public Chord completion() {
		Chord result = getChordByName(Key.CMajor.getNoteName(getRoot()) + getCharacteristic());
		return result;
	}
	
	/**
	 * Return a String naming the tones requested.  Voodoo.
	 * @param c
	 * @param root
	 * @param nameSoFar
	 * @param tones
	 * @return
	 */
	private static Pair<String,Integer> nameTones(Chord c, int root, int... tones) {
		String name = "";
		int certainty = 0;
		for(int m : tones)  {
			switch(m) {
				//color
				case 9: if(c.contains(root+2) /*&& !c.contains(root+5) && !c.contains(root+9)*/) {
							name += "(9)";
						} else if(c.contains(root+1)) {
							name += FLAT + "9";
						} else if(c.contains(root + 3) && c.contains(root+4)) {
							name += "#9";
						}
						break;
				//only look for flat 9 (for sus2/sus24 chords)
				case -9: if(c.contains(root+1)) {
							name += FLAT + "9";
						}
						break;
				
				//color
				case 11: if(c.contains(root+5) /*&& !c.contains(root+9)*/) {
							name += "(11)";
						} else if(c.contains(root+6)  && 
								(c.contains(root+7) || c.contains(root + 8) && c.contains(root+4))) {
							name += "#11";
						} // flat 11s are impossible as they will be seen as M3s
						break;
				//only look for sharp 11
				case -11: if(c.contains(root+6)) {
							name += "#11";
						}
						break;
								
				case 6: if(c.contains(root+9)) {
							name +="(6)";
						} else if(c.contains(root+8) && c.contains(root+7)) {
							name += FLAT + "6";
							certainty -= 5;
						}
						break;
				// only look for flat 6 (for dim chords)
				case -6: if(c.contains(root+8) && c.contains(root+7)) {
							name += "("+ FLAT+"6)";
							certainty -= 5;
						} else if(c.contains(root+8)) {
							name += FLAT + "6";
							certainty -= 5;
						}
						break;
				case 13: if(c.contains(root+9)) {
							name += "(13)";
						} else if(c.contains(root+8) && c.contains(root+7)) {
							name += FLAT + "13";
						} else if(c.contains(root+10) && c.contains(root+11)) {
							name += "#13";
						}
						break;

				// only for cases where we assume the chord is major like sus/5 chords
				case 7: if(c.contains(root+10)) {
							name += "7";
						} else if(c.contains(root+11)) {
							name += "M7";
						}
						break;
				
				// in case there is a -5
				case -7: if(c.contains(root+10)) {
							name += "7"+ FLAT+"5";
						} else if(c.contains(root+11)) {
							name += "M7"+ FLAT+"5";
						} else if(c.contains(root+9)) {
							name += diminished + "7";
						}
						break;
						
				// in case of sus chords
				case 5: if(c.contains(root+8)&&c.contains(root+9)) {
							name += "#5";
						} else if(c.contains(root+6)) {
							name += FLAT+"5";
						}
						break;
			}
				
		}
		Pair<String,Integer> result = new Pair<String, Integer>(name, certainty);
		return result;
	}
	
	private String _myChar = null;
	private Integer _myCharHC = null;
	/**
	 * Return a string like "" for a major chord, "-7" for a minor 7, "13" for a 13 chord, "-M7" for a minor-major
	 * seven chord, and so on.  For more information, see guessCharacteristic(), as this acts as a sort of
	 * wrapping layer atop it. This method returns the parser's best guess as to what kind of chord it is
	 * given its set root.  Examples include "M7", "-13b5", "+11b9"  If no root is set, returns null.
	 * 
	 * @return a String representation of this Chord's characteristic
	 */
	public String getCharacteristic() {
		if(getRoot() == null)
			return null;
		
		int hc = hashCode();
		
		if(_myCharHC == null || _myCharHC != hc) {
			_myCharHC = hc;
			_myChar = null;
		}
		if(_myChar == null) {
			_myChar = guessCharacteristic(this, getRoot()).first;
		}
		
		return _myChar;
	}
	
	/**
	 * Given the provided root, returns the name (excluding the root as it may be enharmonically named) of the
	 * chord and a "score" of how likely it is that the chord is the one guessed.
	 * 
	 * @param c the Chord to be inspected
	 * @param root the root note to inspect from
	 * @return
	 */
	static Pair<String, Integer> guessCharacteristic(Chord c, Integer root) {
		String name = "";
		int certainty = 0;
		
		
		Log.i(TAG,"Guessing name for chord: " + c.toString() + "R:" + root);
		
		// Root should be in the chord, but doesn't have to be necessarily.
		if (c.contains(root)) {
			certainty += 12;
			Log.i(TAG,"Root is in the chord!");
		}
		
		// M3
		if (c.contains(root + 4)) {
			Log.i(TAG,"M3 is in the chord!");
			certainty += 10;
			// M3P5 - major triad present
			if(c.contains(root+7)) {
				Log.i(TAG,"P5 is in the chord!");
				certainty += 8;
				
				//M3P5M7 (major 7 type chord)
				if(c.contains(root+11)) {
					certainty += 8;
					//M3P5M7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3P5M7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "M13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//M3P5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//M3P5M7M9, no M11, no M13
						} else {
							name += "M9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// M3P5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
						name += p.first;
						certainty += p.second;
					}
					
				//M3P5m7 (dominant 7)
				} else if(c.contains(root+10)) {
					certainty += 8;
					//M3P5m7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3P5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "13";
							Pair<String,Integer> p = nameTones(c,root,11);
							name += p.first;
							certainty += p.second;
						//M3P5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//M3P5m7M9, no M11, no M13
						} else {
							name += "9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// M3P5m7, no M9, no M11, no M13
					} else {
						name += "7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
						name += p.first;
						certainty += p.second;
					}
				//M3P5M6 (major 6 with no M7/m7 at all).  Slightly disfavor 6 chords under 7 chords.
				} else if(c.contains(root+9)){
					certainty += 6;
					name = name + "6";
					Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
				//M3P5 with no (non-flat)6 or any 7
				} else {
					//name = name + "M";
					certainty += 8;

					Pair<String,Integer> p = nameTones(c, root, -6, 9, 11);
					String colors = p.first;
					certainty += p.second;
					if(colors.length() > 0 && (colors.startsWith("#")||colors.charAt(0) == FLAT))
						name += "(" + colors + ")";
					else
						name += colors;
				}
			// M3+5 - augmented chord
			} else if (c.contains(root + 8)) {
				Log.i(TAG,"+5 is in the chord!");
				name += "+";
				certainty += 7;
				
				//M3+5M7 (major 7+5 type chord)
				if(c.contains(root+11)) {
					certainty += 7;
					//M3+5M7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3+5M7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "M13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//M3+5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//M3+5M7M9, no M11, no M13
						} else {
							name += "M9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// M3+5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
						name += p.first;
						certainty += p.second;
					}
					
				//M3+5m7 (dominant 7+5)
				} else if(c.contains(root+10)) {
					certainty += 6;
					//M3+5m7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3+5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//M3+5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//M3+5m7M9, no M11, no M13
						} else {
							name += "9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// M3+5m7, no M9, no M11, no M13
					} else {
						name += "7";
						Pair<String, Integer> p = nameTones(c, root, 9, 11, 13);
						name += p.first;
						certainty += p.second;
					}
				//M3+5M6 (major 6 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					certainty += 6;
					name = name + "M6";
					Pair<String, Integer> p = nameTones(c, root, 9, 11);
					name += p.first;
					certainty += p.second;
				// M3+5 No6or7
				} else {
					certainty += 7;
					Pair<String, Integer> p = nameTones(c, root, 9, 11);
					name += p.first;
					certainty += p.second;
				}
			// M3-5P11 - major flat five chord. We require a P11 to be present
			// because normally if there is no perfect 5 we'd rather call this a #11.  This is ugly so make it less certain
			} else if (c.contains(root + 6)) {
				Log.i(TAG, "-5 is in the chord!");
				certainty += 1;

				// M3-5M7 (major 7 type chord)
				if (c.contains(root + 11)) {
					certainty += 8;
					// M3-5M7M9
					if (c.contains(root + 2)) {
						// M3-5M7M9M13 (11 is optional to call it a 13 chord, as
						// the 11 is an avoid note)
						if (c.contains(root + 9)) {
							name += "M13"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
							// M3-5M7M9M11
						} else if (c.contains(root + 5)) {
							name += "M11"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 9);
							name += p.first;
							certainty += p.second;
							// M3-5M7M9, no M11, no M13
						} else {
							name += "M9"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 11, 13);
							name += p.first;
							certainty += p.second;
						}
						// M3-5M7, no M9, no M11, no M13
					} else {
						name += "M7"+ FLAT+"5";
						Pair<String,Integer> p = nameTones(c, root, 9, 11, 13);
							name += p.first;
							certainty += p.second;
					}

				// M3-5m7 (dominant 7 flat 5)
				} else if (c.contains(root + 10)) {
					certainty += 8;
					// M3-5m7M9
					if (c.contains(root + 2)) {
						// M3-5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if (c.contains(root + 9)) {
							name += "13"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
						// M3-5m7M9M11
						} else if (c.contains(root + 5)) {
							name += "11"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 9);
							name += p.first;
							certainty += p.second;
						// M3-5m7M9, no M11, no M13
						} else {
							name += "9"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 11, 13);
							name += p.first;
							certainty += p.second;
						}
					// M3-5m7, no M9, no M11, no M13
					} else {
						name += "7"+ FLAT+"5";
						Pair<String,Integer> p = nameTones(c, root, 9, 11, 13);
							name += p.first;
							certainty += p.second;
					}
				// M3-5M6 (major 6 with no M7/m7 at all)
				} else if (c.contains(root + 9)) {
					certainty += 6;
					name += "6"+ FLAT+"5";
					Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
				//M3-5 with no (non-flat)6 or any 7
				} else {
					name += "M"+ FLAT+"5";
					Pair<String,Integer> p = nameTones(c,root, -6, 9, 11);
							name += p.first;
							certainty += p.second;
				}
			// No 5 present (name as if there's a perfect 5)
			//M3x5
			} else {
				Log.i(TAG, "No5 is in the chord!");
				if(c.contains(root))
					certainty += 5;
				//M3x5M7 (major 7 type chord)
				if(c.contains(root+11)) {
					certainty += 8;
					//M3x5M7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3x5M7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "M13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//M3x5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//M3x5M7M9, no M11, no M13
						} else {
							name += "M9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// M3x5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
							name += p.first;
							certainty += p.second;
					}
					
				//M3x5m7 (dominant 7)
				} else if(c.contains(root+10)) {
					certainty += 8;
					//M3x5m7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3x5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//M3x5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//M3x5m7M9, no M11, no M13
						} else {
							name += "9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// M3x5m7, no M9, no M11, no M13
					} else {
						name += "7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
							name += p.first;
							certainty += p.second;
					}
				//M3x5M6 (major 6/13 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					certainty -= 1;
					name = name + "6";
					Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
				//M3x5 with no (non-flat)6 or any 7
				} else {
					//name += "M";

					Pair<String,Integer> p = nameTones(c, root, -6, 9, 11);
					String colors = p.first;
					certainty += p.second;
					if(colors.length() > 0  && (colors.startsWith("#")||colors.charAt(0) == FLAT))
						name += "M" + colors;
					else
						name += colors;
					//Pair<String,Integer> p = nameTones(c,root, -6, 9, 11);
							//name += p.first;
					certainty += p.second;
				}
			}
			
		// m3 (and no M3 - if both are present we don't get here and it becomes a #9)
		} else if (c.contains(root + 3)) {
			Log.i(TAG,"m3 is in the chord!");
			certainty += 10;
			// m3P5 - major triad present
			if(c.contains(root+7)) {
				name += "-";
				Log.i(TAG,"P5 is in the chord!");
				certainty += 8;
				
				//m3P5M7 (major 7 type chord)
				if(c.contains(root+11)) {
					certainty += 8;
					//m3P5M7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//m3P5M7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "M13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//m3P5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//m3P5M7M9, no M11, no M13
						} else {
							name += "M9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// m3P5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
							name += p.first;
							certainty += p.second;
					}
					
				//m3P5m7 (minor 7)
				} else if(c.contains(root+10)) {
					certainty += 8;
					//m3P5m7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//m3P5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//m3P5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//m3P5m7M9, no M11, no M13
						} else {
							name += "9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// m3P5m7, no M9, no M11, no M13
					} else {
						name += "7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
							name += p.first;
							certainty += p.second;
					}
				//m3P5M6 (major 6 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					name = name + "6";
					Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
				// m3P5 with no (non-flat)6 or any 7
				} else {
					Pair<String,Integer> p = nameTones(c, root, -6, 9, 11);
							name += p.first;
							certainty += p.second;
				}
			
			// m3-5 - diminished chord/triad present
			} else if (c.contains(root +6)) {
				Log.i(TAG, "-5 is in the chord!");
				certainty += 8;

				// m3-5M7 (diminished major 7 type chord)
				if (c.contains(root + 11)) {
					certainty += 5;
					name = name + diminished;
					// m3-5M7M9
					if (c.contains(root + 2)) {
						// m3-5M7M9M13 (11 is optional to call it a 13 chord, as
						// the 11 is an avoid note)
						if (c.contains(root + 9)) {
							name += "M13";
							Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
						// m3-5M7M9M11
						} else if (c.contains(root + 5)) {
							name += "M11";
							Pair<String,Integer> p = nameTones(c, root, 9);
							name += p.first;
							certainty += p.second;
						// m3-5M7M9, no M11, no M13
						} else {
							name += "M9";
							Pair<String,Integer> p = nameTones(c, root, 11, 13);
							name += p.first;
							certainty += p.second;
						}
						// m3-5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						Pair<String,Integer> p = nameTones(c, root, 9, 11, 13);
							name += p.first;
							certainty += p.second;
					}

				// m3-5m7 (minor 7 flat 5/half-dim)
				} else if (c.contains(root + 10)) {
					name += "-";
					certainty += 8;
					// m3-5m7M9
					if (c.contains(root + 2)) {
						// m3-5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if (c.contains(root + 9)) {
							name += "13"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
							// M3-5m7M9M11
						} else if (c.contains(root + 5)) {
							name += "11"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 9);
							name += p.first;
							certainty += p.second;
							// M3-5m7M9, no M11, no M13
						} else {
							name += "9"+ FLAT+"5";
							Pair<String,Integer> p = nameTones(c, root, 11, 13);
							name += p.first;
							certainty += p.second;
						}
						// M3-5m7, no M9, no M11, no M13
					} else {
						name += "7"+ FLAT+"5";
						Pair<String,Integer> p = nameTones(c, root, 9, 11, 13);
							name += p.first;
							certainty += p.second;
					}
				// m3-5-7 (fully diminished with no M7/m7 at all)
				} else if (c.contains(root + 9)) {
					name += diminished + "7";
					Pair<String,Integer> p = nameTones(c, root, -6, 9, 11);
							name += p.first;
							certainty += p.second;
				//m3-5 with no (non-flat)6 or any 7 (dim chord)
				} else {
					name += diminished;
					Pair<String,Integer> p = nameTones(c,root, -6, 9, 11);
							name += p.first;
							certainty += p.second;
				}
			// m3x5 No 5 present (name as if there's a major 5)
			} else {
				Log.i(TAG, "No5 is in the chord!");
				if(c.contains(root))
					certainty += 4;
				name += "-";
				//m3x5M7 (minor-major 7 type chord)
				if(c.contains(root+11)) {
					certainty += 8;
					//m3x5M7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//m3x5M7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "M13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//m3x5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//m3x5M7M9, no M11, no M13
						} else {
							name += "M9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// m3x5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
							name += p.first;
							certainty += p.second;
					}
					
				//m3x5m7 (minor 7)
				} else if(c.contains(root+10)) {
					certainty += 8;
					//m3x5m7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//m3x5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "13";
							Pair<String,Integer> p = nameTones(c,root,9,11);
							name += p.first;
							certainty += p.second;
						//m3x5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							Pair<String,Integer> p = nameTones(c,root,9);
							name += p.first;
							certainty += p.second;
						//m3x5m7M9, no M11, no M13
						} else {
							name += "9";
							Pair<String,Integer> p = nameTones(c,root,11,13);
							name += p.first;
							certainty += p.second;
						}
					// m3x5m7, no M9, no M11, no M13
					} else {
						name += "7";
						Pair<String,Integer> p = nameTones(c,root,9,11,13);
							name += p.first;
							certainty += p.second;
					}
				//m3x5M6 (major 6 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					certainty -= 1;
					name += "6";
					Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
				//m3x5b6x7 or m3x5x6x7.  Just need to name the 9, 11, and possibly b6.
				} else {
					Pair<String,Integer> p = nameTones(c,root,-6,9,11);
							name += p.first;
							certainty += p.second;
				}
			}

		// x3M2P4 - sus24 chord.  Only notes definable are the 13/6 and 7
		} else if(c.contains(root+2) && c.contains(root+5)) {
			certainty += 9;
			//TODO what about fifths?
			String seven = nameTones(c,root,7).first;
			name += seven;
			name += "sus24";
			Pair<String,Integer> p = nameTones(c, root, 13);
							name += p.first;
							certainty += p.second;
		// x3P4
		} else if(c.contains(root+5)) {
			certainty += 9;
			//TODO what about fifths?
			String seven = nameTones(c,root,7).first;
			name += seven;
			name += "sus4";
			Pair<String,Integer> p = nameTones(c, root, -9, 13);
							name += p.first;
							certainty += p.second;
		// x3M2
		} else if(c.contains(root+2)) {
			certainty += 9;
			//TODO what about fifths?
			String seven = nameTones(c,root,7).first;
			name += seven;
			if(name == "")
				name += "2";
			else
				name += "sus2";
			Pair<String,Integer> p = nameTones(c, root, -11, 13);
							name += p.first;
							certainty += p.second;
		// x2x3x4P5 - 5 chord, no M2, no M/m3, no P4
		} else if(c.contains(root+7)) {
			if(c.contains(root))
				certainty += 8;
			String seven = nameTones(c,root,7).first;
			name += seven;
			if(seven=="")
				name += "5";
			else
				name += "(no3)";
			Pair<String,Integer> p = nameTones(c, root, -9, -11, 13);
							name += p.first;
							certainty += p.second;
		// x2x3x4+5 - +5 chord, no M2, no M/m3, no P4
		} else if(c.contains(root+8)) {
			if(c.contains(root))
				certainty += 6;
			name += "+";
			String seven = nameTones(c,root,7).first;
			name += seven;
			name += "(no3)";
			Pair<String,Integer> p = nameTones(c, root, -9, -11, 13);
							name += p.first;
							certainty += p.second;

		// x2x3x4-5 - -5 chord, no M2, no M/m3, no P4
		} else if(c.contains(root+6)) {
			Log.i(TAG, "-5 is in the chord!");
			certainty += 7;

			// x3-5M7 (diminished major 7 type chord)
			if (c.contains(root + 11)) {
				certainty += 7;
				name = name + diminished + "(no3)";
				// m3-5M7M9
				if (c.contains(root + 2)) {
					// m3-5M7M9M13 (11 is optional to call it a 13 chord, as
					// the 11 is an avoid note)
					if (c.contains(root + 9)) {
						name += "M13";
						Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
					// m3-5M7M9M11
					} else if (c.contains(root + 5)) {
						name += "M11";
						Pair<String,Integer> p = nameTones(c, root, 9);
							name += p.first;
							certainty += p.second;
					// m3-5M7M9, no M11, no M13
					} else {
						name += "M9";
						Pair<String,Integer> p = nameTones(c, root, 11, 13);
							name += p.first;
							certainty += p.second;
					}
					// m3-5M7, no M9, no M11, no M13
				} else {
					name += "M7";
					Pair<String,Integer> p = nameTones(c, root, 9, 11, 13);
							name += p.first;
							certainty += p.second;
				}

			// x3-5m7 (NOT minor 7 flat 5/half-dim; we "hear" the M3 to make a Dom7b5 chord)
			} else if (c.contains(root + 10)) {
				certainty += 8;
				// m3-5m7M9
				if (c.contains(root + 2)) {
					// m3-5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
					if (c.contains(root + 9)) {
						name += "13"+ FLAT+"5";
						Pair<String,Integer> p = nameTones(c, root, 9, 11);
							name += p.first;
							certainty += p.second;
						// M3-5m7M9M11
					} else if (c.contains(root + 5)) {
						name += "11"+ FLAT+"5";
						Pair<String,Integer> p = nameTones(c, root, 9);
							name += p.first;
							certainty += p.second;
						// M3-5m7M9, no M11, no M13
					} else {
						name += "9"+ FLAT+"5";
						Pair<String,Integer> p = nameTones(c, root, 11, 13);
							name += p.first;
							certainty += p.second;
					}
					// M3-5m7, no M9, no M11, no M13
				} else {
					name += "7"+ FLAT+"5";
					Pair<String,Integer> p = nameTones(c, root, 9, 11, 13);
							name += p.first;
							certainty += p.second;
				}
				name += "(no3)";
			// x3-5-7 (heard as fully diminished with no M7/m7 at all)
			} else if (c.contains(root + 9)) {
				name += diminished + "7";
				Pair<String,Integer> p = nameTones(c, root, -6, 9, 11);
							name += p.first;
							certainty += p.second;
			//m3-5 with no (non-flat)6 or any 7 (dim chord)
			} else {
				name += diminished + "(no3)";
				Pair<String,Integer> p = nameTones(c,root, -6, 9, 11);
							name += p.first;
							certainty += p.second;
			}
		// x3 with no M/m3, M2, P4, or P/+/-5.  Harmonic series says we fill in the major chord
		// within a few overtones so just name the 7, possible b9, and possible M6/b7/M7 remaining
		} else {
			Pair<String,Integer> p = nameTones(c, root, 7, -9, 13);
							name += p.first;
							certainty += p.second;
		}
		
		//Adjust certainty for name complexity.
		//certainty -= name.length() + 2;
		
		return new Pair<String, Integer>(name, certainty);
	}
	
	/**
	 * Return the appropriate chord for the given name.  For instance, getChordByName("CM7-5b13")
	 * return a Chord with root [0] and [4], [6], [11] and [8] also present.
	 * 
	 * @param s any chord name
	 * @return
	 */
	public static Chord getChordByName(String s) {
		Chord result = new Chord();
		// EX: F+M7#6#9
		Pattern p = Pattern.compile("((?:A|B|C|D|E|F|G)(?:#|b|" + FLAT + ")?)" + //root name (1)
								"(-|\\+|" + diminished + "|2|sus2|sus|sus4|sus24|5?)" + // quality (2)
								"(M?)(6|7|9|11|13|)((?:\\(no3\\))?)" + // "color quality" (3)(4)(5)
								"((?:(?:add)?(?:b|#)?(?:-5|7|9|11|13))*)" ); // more color tones to be parsed later (6)
		Matcher m = p.matcher(s);
		if(m.matches()) {
			Log.d(TAG,m.group(1) + " | " +
					m.group(2) + " | " +
					m.group(3) + " | " +
					m.group(4) + " | " +
					m.group(5) + " | " +
					m.group(6));
			
			int root = Enharmonics.noteNameToInt(m.group(1));
			result.setRoot(root);
			
			// quality
			int two = -1;
			int three = -1;
			int four = -1;
			int five = -1;
			
			if(m.group(2) == "-") {
				three = root + 3;
			} else if(m.group(2).equals(diminished)) {
				three = root+3;
				five = root+6;
			} else if(m.group(2).equals("+")) {
				three = root+4;
				five = root+8;
			} else if(m.group(2).equals("2") || m.group(2).equals("sus2")) {
				two = root+2;
			} else if(m.group(2).equals("sus") || m.group(2).equals("sus4")) {
				four = root+5;
			} else if(m.group(2).equals("sus24")) {
				two = root+2;
				four = root+5;
			} else if(m.group(2).equals("") && m.group(5).equals("")) {
				three = root+4;
			}
			
			
			int six = -1;
			int seven = -1;
			
			// color quality
			String thirdsSpan = m.group(4);
			int colorQualityInterval;
			if(thirdsSpan.equals(""))
				colorQualityInterval = -1;
			else 
				colorQualityInterval = HEPTATONIC.mod(Integer.parseInt(thirdsSpan));
			switch(colorQualityInterval) {
				// 13 chord
				case 6:
					six = root + 8;
				// 11 chord (or 13 chord, but the conditional will skip the 11)
				case 4:
					if(colorQualityInterval == 4)
						four = root + 5;
				// 9 or 11 or 13 chord
				case 2:
					two = root + 2;
				// 7 chord.  Must decide major or minor.
				case 0: 
					if(five == root + 6)
						seven = root + 9;
					else if(m.group(3).equals("M"))
						seven = root + 11;
					else 
						seven = root + 10;
					break;
			}
			
			//colors
			Pattern p6 = Pattern.compile("(?:add)?((?:b|#)?((?:-5|6|7|9|11|13)))");
			Matcher m6 = p6.matcher(m.group(6));
			while(m6.find()) {
				int colorToneInterval = Integer.parseInt(m6.group(2));
				switch(colorToneInterval) {
					case -5:
						if(five == -1)
							five = root + 6;
						break;
					case 6:
						six = root + schenkerianToInt(m6.group(1));
						break;
					case 7:
						seven = root + schenkerianToInt(m6.group(1));
						break;
					case 9:
						two = root + schenkerianToInt(m6.group(1));
						break;
					case 11:
						four = root + schenkerianToInt(m6.group(1));
						break;
					case 13:
						six = root + schenkerianToInt(m6.group(1));
						break;
				}
			}
			
			if(m.group(5).equals("(no3)")) {
				three = -1;
			}
			
			//Add a P5 if one is not present
			if(five == -1) {
				five = root+7;
			}
			
			// Add tones to chord
			for(int i : new int[]{two, three, four, five, six, seven}) {
				if(i != -1)
					result.add(i);
			}
		}
		return result;
	}
	
	/**
	 * Convert a Schenkerian name (6, b7, #9, -5) to the number of half
	 * steps that interval is from its tonic. (here, these are 9, 10, 3, and 6).
	 * @param s
	 * @return
	 */
	public static int schenkerianToInt(String s) {
		int result = 0;
		Pattern p = Pattern.compile("(#|" + FLAT + "|-|\\+|)" + "(\\d+)");
		Matcher m = p.matcher(s);
		if( m.matches()) {
			int interval = HEPTATONIC.mod( Integer.parseInt(m.group(2)) );
			switch(interval) {
				//7
				case 0:
					result = 11;
					break;
				case 1:
					result = 0;
					break;
				// 2 or 9
				case 2:
					result = 2;
					break;
				case 3:
					result = 4;
					break;
				// 4 or 1
				case 4:
					result = 5;
					break;
				case 5:
					result = 7;
					break;
				// 6 or 13
				case 6:
					result = 9;
					break;
				default:
					break;
			}
			
			if(m.group(1).length() > 0) {
				if(m.group(1).charAt(0) == FLAT || m.group(1).charAt(0) == 'b') {
					result -= 1;
				} else if(m.group(1).charAt(0) == '#') {
					result += 1;
					
				// Augmented/diminished intervals 
				} else if(m.group(1).charAt(0) == '+' && (result == 7||result == 5)) {
					result += 1;
				} else if(m.group(1).charAt(0) == '+') {
					result += 2;
				} else if(m.group(1).charAt(0) == '-' && (result == 7||result == 5)) {
					result -= 1;
				} else if(m.group(1).charAt(0) == '-') {
					result -= 2;
				}
			}
		}
		
		return TWELVETONE.mod(result);
	}
	
	/**
	 * Throws a NullPointerException if root is not set on the Chord
	 * @return
	 */
	public Iterator<Integer> rootChordIterator(final boolean finite) {
		return new Iterator<Integer>() {
			Integer next = -1;
			@Override
			public boolean hasNext() {
				return !finite || next != getRoot();
			}

			@Override
			public Integer next() {
				int result;
				if(next == -1)
					next = getRoot();
				
				result = next;
				
				next = higher(result);
				if(next == null)
					next = ceiling(0);
				
				return result;
			}

			@Override
			public void remove() { }
			
		};
	}
		
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o instanceof Chord) {
			Chord c = (Chord)o;
			if(c.getRoot() == getRoot() && containsAll(c) && c.containsAll(this))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		Iterator<Integer> itr = rootChordIterator(true);
		
		int multiple = 1;
		while(itr.hasNext()) {
			result += multiple * itr.next();
			multiple = multiple * MODULUS.OCTAVE_STEPS;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return TAG + "{Mod" + MODULUS.OCTAVE_STEPS + "," + super.toString() + "}";
	}
	
	public String getRootEnharmonic() {
		return noteNameCache[headSet(getRoot()).size()];
	}
}

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
	protected static final String flat = "\u266D";
	private static final String diminished = "\u00B0";
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
		boolean result = super.contains(MODULUS.getPitchClass(i));
		Log.i(TAG,"Saying " + super.toString() + " contains " + MODULUS.getPitchClass(i) + " is " + result);
		return result;
	}
	
	public void setRoot(Integer i) {
		if( i != null)
			this._root = MODULUS.getPitchClass(i);
		else
			this._root = null;
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
	/*private static Pair<String, Integer> nameFiveSixSevensColors(Chord c, Integer root) {
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
	}*/
	
	/**
	 * Return a String naming the tones requested (
	 * @param c
	 * @param root
	 * @param nameSoFar
	 * @param tones
	 * @return
	 */
	private static String nameTones(Chord c, int root, int... tones) {
		String result = "";
		for(int m : tones)  {
			switch(m) {
				//color
				case 9: if(c.contains(root+2) && !c.contains(root+5) && !c.contains(root+9)) {
							result += "add9";
						} else if(c.contains(root+1)) {
							result += flat + "9";
						} else if(c.contains(root + 3) && c.contains(root+4)) {
							result += "#9";
						}
						break;
				//only look for flat 9 (for sus2/sus24 chords)
				case -9: if(c.contains(root+1)) {
							result += flat + "9";
						}
						break;
				
				//color
				case 11: if(c.contains(root+5) /*&& !c.contains(root+9)*/) {
							result += "add11";
						} else if(c.contains(root+6)  && 
								(c.contains(root+7) || c.contains(root + 8) && c.contains(root+4))) {
							result += "#11";
						} // flat 11s are impossible as they will be seen as M3s
						break;
				//only look for sharp 11
				case -11: if(c.contains(root+6)) {
							result += "#11";
						}
						break;
								
				case 6: if(c.contains(root+9)) {
							result +="add6";
						} else if(c.contains(root+8) && c.contains(root+7)) {
							result += flat + "6";
						}
						break;
				// only look for flat 6 (for dim chords)
				case -6: if(c.contains(root+8) && c.contains(root+7)) {
							result += "add"+flat+"6";
						} else if(c.contains(root+8)) {
							result += flat + "6";
						}
						break;
				case 13: if(c.contains(root+9)) {
							result += "add13";
						} else if(c.contains(root+8) && c.contains(root+7)) {
							result += flat + "13";
						} else if(c.contains(root+10) && c.contains(root+11)) {
							result += "#13";
						}
						break;

				// only for cases where we assume the chord is major like sus/5 chords
				case 7: if(c.contains(root+10)) {
							result += "7";
						} else if(c.contains(root+11)) {
							result += "M7";
						}
						break;
				
				// in case there is a -5
				case -7: if(c.contains(root+10)) {
							result += "7-5";
						} else if(c.contains(root+11)) {
							result += "M7-5";
						} else if(c.contains(root+9)) {
							result += diminished + "7";
						}
						break;
						
				// in case of sus chords
				case 5: if(c.contains(root+8)&&c.contains(root+9)) {
							result += "+";
						} else if(c.contains(root+6)) {
							result += diminished;
						}
						break;
			}
				
		}
		return result;
	}
	
	/**
	 * Given the provided root, returns the name (excluding the root as it may be enharmonically named) of the chord and a "score" of how likely it is that
	 * the chord is the one guessed.
	 * 
	 * @param c
	 * @param root
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
							name += nameTones(c,root,9,11);
						//M3P5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							name += nameTones(c,root,9);
						//M3P5M7M9, no M11, no M13
						} else {
							name += "M9";
							name += nameTones(c,root,11,13);
						}
					// M3P5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						name += nameTones(c,root,9,11,13);
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
							name += nameTones(c,root,9,11);
						//M3P5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							name += nameTones(c,root,9);
						//M3P5m7M9, no M11, no M13
						} else {
							name += "9";
							name += nameTones(c,root,11,13);
						}
					// M3P5m7, no M9, no M11, no M13
					} else {
						name += "7";
						name += nameTones(c,root,9,11,13);
					}
				//M3P5M6 (major 6 with no M7/m7 at all).  Slightly disfavor 6 chords under 7 chords.
				} else if(c.contains(root+9)){
					certainty += 6;
					name = name + "6";
					name += nameTones(c, root, 9, 11);
				//M3P5 with no (non-flat)6 or any 7
				} else {
					//name = name + "M";
					certainty += 8;
					String colors = nameTones(c,root, -6, 9, 11);
					if(colors.length() > 0  && (colors.startsWith("#")||colors.startsWith(flat)))
						name += "M" + colors;
					else
						name += colors;
				}
			// M3+5 - augmented chord
			} else if (c.contains(root + 8)) {
				Log.i(TAG,"+5 is in the chord!");
				name += "+";
				certainty += 9;
				
				//M3+5M7 (major 7+5 type chord)
				if(c.contains(root+11)) {
					certainty += 8;
					//M3+5M7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3+5M7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "M13";
							name += nameTones(c,root,9,11);
						//M3+5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							name += nameTones(c,root,9);
						//M3+5M7M9, no M11, no M13
						} else {
							name += "M9";
							name += nameTones(c,root,11,13);
						}
					// M3+5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						name += nameTones(c,root,9,11,13);
					}
					
				//M3+5m7 (dominant 7+5)
				} else if(c.contains(root+10)) {
					certainty += 8;
					//M3+5m7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3+5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "13";
							name += nameTones(c,root,9,11);
						//M3+5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							name += nameTones(c,root,9);
						//M3+5m7M9, no M11, no M13
						} else {
							name += "9";
							name += nameTones(c,root,11,13);
						}
					// M3+5m7, no M9, no M11, no M13
					} else {
						name += "7";
						name += nameTones(c,root,9,11,13);
					}
				//M3+5M6 (major 6 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					certainty += 6;
					name = name + "6";
					name += nameTones(c, root, 9, 11);
				//M3+5 No6or7
				} else {
					certainty += 7;
					name += nameTones(c, root, 9, 11);
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
							name += "M13-5";
							name += nameTones(c, root, 9, 11);
							// M3-5M7M9M11
						} else if (c.contains(root + 5)) {
							name += "M11-5";
							name += nameTones(c, root, 9);
							// M3-5M7M9, no M11, no M13
						} else {
							name += "M9-5";
							name += nameTones(c, root, 11, 13);
						}
						// M3-5M7, no M9, no M11, no M13
					} else {
						name += "M7-5";
						name += nameTones(c, root, 9, 11, 13);
					}

				// M3-5m7 (dominant 7 flat 5)
				} else if (c.contains(root + 10)) {
					certainty += 8;
					// M3-5m7M9
					if (c.contains(root + 2)) {
						// M3-5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if (c.contains(root + 9)) {
							name += "13-5";
							name += nameTones(c, root, 9, 11);
						// M3-5m7M9M11
						} else if (c.contains(root + 5)) {
							name += "11-5";
							name += nameTones(c, root, 9);
						// M3-5m7M9, no M11, no M13
						} else {
							name += "9-5";
							name += nameTones(c, root, 11, 13);
						}
					// M3-5m7, no M9, no M11, no M13
					} else {
						name += "7-5";
						name += nameTones(c, root, 9, 11, 13);
					}
				// M3-5M6 (major 6 with no M7/m7 at all)
				} else if (c.contains(root + 9)) {
					certainty += 6;
					name += "6-5";
					name += nameTones(c, root, 9, 11);
				//M3-5 with no (non-flat)6 or any 7
				} else {
					name += "M-5";
					name += nameTones(c,root, -6, 9, 11);
				}
			// No 5 present (name as if there's a major 5)
			} else {
				Log.i(TAG, "No5 is in the chord!");
				if(c.contains(root))
					certainty += 7;
				//M3x5M7 (major 7 type chord)
				if(c.contains(root+11)) {
					certainty += 8;
					//M3x5M7M9
					if(c.contains(root+2)) {
						certainty += 5;
						//M3x5M7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if(c.contains(root+9)) {
							name += "M13";
							name += nameTones(c,root,9,11);
						//M3x5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							name += nameTones(c,root,9);
						//M3x5M7M9, no M11, no M13
						} else {
							name += "M9";
							name += nameTones(c,root,11,13);
						}
					// M3x5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						name += nameTones(c,root,9,11,13);
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
							name += nameTones(c,root,9,11);
						//M3x5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							name += nameTones(c,root,9);
						//M3x5m7M9, no M11, no M13
						} else {
							name += "9";
							name += nameTones(c,root,11,13);
						}
					// M3x5m7, no M9, no M11, no M13
					} else {
						name += "7";
						name += nameTones(c,root,9,11,13);
					}
				//M3x5M6 (major 6/13 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					//certainty += 6;
					name = name + "6";
					name += nameTones(c, root, 9, 11);
				//M3x5 with no (non-flat)6 or any 7
				} else {
					//name += "M";

					String colors = nameTones(c,root, -6, 9, 11);
					if(colors.length() > 0  && (colors.startsWith("#")||colors.startsWith(flat)))
						name += "M" + colors;
					else
						name += colors;
					//name += nameTones(c,root, -6, 9, 11);
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
							name += nameTones(c,root,9,11);
						//m3P5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							name += nameTones(c,root,9);
						//m3P5M7M9, no M11, no M13
						} else {
							name += "M9";
							name += nameTones(c,root,11,13);
						}
					// m3P5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						name += nameTones(c,root,9,11,13);
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
							name += nameTones(c,root,9,11);
						//m3P5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							name += nameTones(c,root,9);
						//m3P5m7M9, no M11, no M13
						} else {
							name += "9";
							name += nameTones(c,root,11,13);
						}
					// m3P5m7, no M9, no M11, no M13
					} else {
						name += "7";
						name += nameTones(c,root,9,11,13);
					}
				//m3P5M6 (major 6 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					name = name + "6";
					name += nameTones(c, root, 9, 11);
				// m3P5 with no (non-flat)6 or any 7
				} else {
					name += nameTones(c, root, -6, 9, 11);
				}
			
			// m3-5 - diminished chord/triad present
			} else if (c.contains(root +6)) {
				Log.i(TAG, "-5 is in the chord!");
				certainty += 8;

				// m3-5M7 (diminished major 7 type chord)
				if (c.contains(root + 11)) {
					certainty += 7;
					name = name + diminished;
					// m3-5M7M9
					if (c.contains(root + 2)) {
						// m3-5M7M9M13 (11 is optional to call it a 13 chord, as
						// the 11 is an avoid note)
						if (c.contains(root + 9)) {
							name += "M13";
							name += nameTones(c, root, 9, 11);
						// m3-5M7M9M11
						} else if (c.contains(root + 5)) {
							name += "M11";
							name += nameTones(c, root, 9);
						// m3-5M7M9, no M11, no M13
						} else {
							name += "M9";
							name += nameTones(c, root, 11, 13);
						}
						// m3-5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						name += nameTones(c, root, 9, 11, 13);
					}

				// m3-5m7 (minor 7 flat 5/half-dim)
				} else if (c.contains(root + 10)) {
					name += "-";
					certainty += 8;
					// m3-5m7M9
					if (c.contains(root + 2)) {
						// m3-5m7M9M13 (11 is optional to call it a 13 chord, as the 11 is an avoid note)
						if (c.contains(root + 9)) {
							name += "13-5";
							name += nameTones(c, root, 9, 11);
							// M3-5m7M9M11
						} else if (c.contains(root + 5)) {
							name += "11-5";
							name += nameTones(c, root, 9);
							// M3-5m7M9, no M11, no M13
						} else {
							name += "9-5";
							name += nameTones(c, root, 11, 13);
						}
						// M3-5m7, no M9, no M11, no M13
					} else {
						name += "7-5";
						name += nameTones(c, root, 9, 11, 13);
					}
				// m3-5-7 (fully diminished with no M7/m7 at all)
				} else if (c.contains(root + 9)) {
					name += diminished + "7";
					name += nameTones(c, root, -6, 9, 11);
				//m3-5 with no (non-flat)6 or any 7 (dim chord)
				} else {
					name += diminished;
					name += nameTones(c,root, -6, 9, 11);
				}
			// m3x5 No 5 present (name as if there's a major 5)
			} else {
				Log.i(TAG, "No5 is in the chord!");
				if(c.contains(root))
					certainty += 8;
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
							name += nameTones(c,root,9,11);
						//m3x5M7M9M11
						} else if(c.contains(root+5)) {
							name += "M11";
							name += nameTones(c,root,9);
						//m3x5M7M9, no M11, no M13
						} else {
							name += "M9";
							name += nameTones(c,root,11,13);
						}
					// m3x5M7, no M9, no M11, no M13
					} else {
						name += "M7";
						name += nameTones(c,root,9,11,13);
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
							name += nameTones(c,root,9,11);
						//m3x5m7M9M11
						} else if(c.contains(root+5)) {
							name += "11";
							name += nameTones(c,root,9);
						//m3x5m7M9, no M11, no M13
						} else {
							name += "9";
							name += nameTones(c,root,11,13);
						}
					// m3x5m7, no M9, no M11, no M13
					} else {
						name += "7";
						name += nameTones(c,root,9,11,13);
					}
				//m3x5M6 (major 6 with no M7/m7 at all)
				} else if(c.contains(root+9)){
					name += "6";
					name += nameTones(c, root, 9, 11);
				//m3x5b6x7 or m3x5x6x7.  Just need to name the 9, 11, and possibly b6.
				} else {
					name += nameTones(c,root,-6,9,11);
				}
			}

		// x3M2P4 - sus24 chord.  Only notes definable are the 13/6 and 7
		} else if(c.contains(root+2) && c.contains(root+5)) {
			certainty += 9;
			String five = nameTones(c,root,5);
			if( five == diminished ) {
				
			} else if( five == "+" ) {
				
			} else {
				
			}
			name += nameTones(c,root,7);
			name += "sus24";
			name += nameTones(c, root, 13);
		// x3P4
		} else if(c.contains(root+5)) {
			certainty += 9;
			name += nameTones(c,root,7);
			name += "sus4";
			name += nameTones(c, root, -9, 13);
		// x3M2
		} else if(c.contains(root+2)) {
			certainty += 9;
			name += nameTones(c,root,7);
			if(name == "")
				name += "2";
			else
				name += "sus2";
			name += nameTones(c, root, -11, 13);
		// x3P5 - 5 chord, no M2, no M/m3, no P4
		} else if(c.contains(root+7)) {
			if(c.contains(root))
				certainty += 8;
			String seven = nameTones(c,root,7);
			name += seven;
			if(seven=="")
				name += "5";
			else
				name += "(no3)";
			name += nameTones(c, root, -9, -11, 13);
		// x3+5 - +5 chord, no M2, no M/m3, no P4
		} else if(c.contains(root+8)) {
			if(c.contains(root))
				certainty += 8;
			name += "+";
			name += nameTones(c,root,7);
			name += "(no3)";
			name += nameTones(c, root, -9, -11, 13);

		// x3-5 - -5 chord, no M2, no M/m3, no P4
		} else if(c.contains(root+6)) {
			if(c.contains(root))
				certainty += 8;
			name += nameTones(c,root,-7);
			name += "(no3)";
			name += nameTones(c, root, -9, 13);
		// x3 with no M/m3, M2, P4, or P/+/-5.  Just keep it major and
		// name the remaining possible tones.
		} else {
			//name += "(no345)";
			name += nameTones(c, root, 7, -9, -11, 13);
		}
		
		//Adjust certainty for name complexity.
		certainty -= name.length() + 2;
		
		return new Pair<String, Integer>(name, certainty);
	}
	
	
	public static Chord getChordByName(String s) {
		Chord result = new Chord();
		// EX: F+M7#6#9
		Pattern p = Pattern.compile("((?:A|B|C|D|E|F|G)(?:#|b))?(M|m)?(7|9|13)?((?:b|#\\d)");
		Matcher m = p.matcher(s);
		if(m.matches()) {
			for(int i = 0; i < m.groupCount(); i = i + 1) {
				
			}
		}
		return result;
	}
	
	public String getName(int root) {
		String result = "";
		
		
		
		return result;
	}
}

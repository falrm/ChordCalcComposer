package com.jonlatane.composer.music.harmony;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.Staff;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.music.Score.Staff.Voice;
import com.jonlatane.composer.music.harmony.Chord.Modulus;

import android.util.Log;
import android.util.Pair;

public class Enharmonics extends LinkedList<LinkedList<Integer[]>>{
	private static final long serialVersionUID = 5672524358L;
	public static String TAG = "VoiceLeading";
	private static Enharmonics DominantSeven = new Enharmonics();
	
	static {
		LinkedList<Integer[]> domSevenOnes = new LinkedList<Integer[]>();
		domSevenOnes.add(new Integer[] {0});
		domSevenOnes.add(new Integer[] {-7});
		domSevenOnes.add(new Integer[] {5});
		LinkedList<Integer[]> domSevenThrees = new LinkedList<Integer[]>();
		domSevenThrees.add(new Integer[] {0, 1});
		LinkedList<Integer[]> domSevenFives = new LinkedList<Integer[]>();
		domSevenFives.add(new Integer[] {0, 0, 2 });
		domSevenFives.add(new Integer[] {0, 0, -2 });
		LinkedList<Integer[]> domSevenSevens = new LinkedList<Integer[]>();
		domSevenSevens.add(new Integer[] {0, 0, 0, 2 });
		domSevenSevens.add(new Integer[] {0, 0, 0, -1 });
		DominantSeven.add(domSevenOnes);
		DominantSeven.add(domSevenThrees);
		DominantSeven.add(domSevenFives);
		DominantSeven.add(domSevenSevens);

	}

	public static final char FLAT = '\u266D';
	public static final char NATURAL = '\u266E';
	private Enharmonics() {
	}

	public static Enharmonics from(PitchSet ps1, Chord c1, PitchSet ps2, Chord c2) {
		Enharmonics result = new Enharmonics();
		//TODO
		int[] initialNoteMap = new int[ps1.size()];
		for(int i = 0; i < initialNoteMap.length; i++)
			initialNoteMap[i] = 0;
		return result;
	}
	
	/**
	 * Fill in the NOTENAMES field for ps2 (if ps2.NOTENAMES is null - otherwise assume the values are accurate).
	 * Use this to determine the note names for c1 and ps1 on the assumption that moving notes should change in
	 * the heptatonic scale in a "readable" way.
	 * 
	 * For instance, an Ab7 resolves to G in a German sixth.  It resolves to C in a doubly augmented German sixth.
	 * 
	 * You may specify a key to have c2 named after the key if its NOTENAMES field named after the key.
	 * 
	 * @param ps1 pitchset that happens over c1, to be enharmonically named if NOTENAMES = null
	 * @param c1 chord under ps1, to be enharmonically named if NOTENAMES = null (can be the same chord as c2Named)
	 * @param ps2 pitchset that happens over c2Named, to be enharmonically named if NOTENAMES = null
	 * @param c2Named chord with NOTENAMES != null and assumed to be "valid" (heptatonic, any double flat/sharp allowed) under ps2
	 * @param k a Key (only one is needed) that we can fall back on, if it is provided
	 */
	public static void fillEnharmonics(PitchSet ps1, Chord c1, PitchSet ps2, Chord c2Named, Key... k) {
		assert(c2Named.NOTENAMES != null);
		
		// If a key was provided, fill c2Named (in which case the variable name is a misnomer
		if(k!=null && k.length>0)
			fillEnharmonics(c2Named, k[0]);
		
		// Fill in ps2's notenames if needed
		fillEnharmonics(ps2,c1, k);
		
		// Fill in c1
		fillEnharmonics(c1, c2Named);
		
		//Fill in ps1
		fillEnharmonics(ps1, c1, k);
	}
	
	public static void fillEnharmonics(PitchSet ps, Chord cNamed, Key... k) {
		if(Chord.NO_CHORD.equals(cNamed)) {
			if(k.length == 0)
				throw new Error("");
			fillEnharmonics(ps, k[0]);
			return;
		}
		
		if(ps.NOTENAMES == null) {
			ps.NOTENAMES = new String[ps.size()];
			int idx = 0;
			for(int note : ps) {
				if(cNamed.contains(note)) {
					String noteName = cNamed.NOTENAMES[cNamed.headSet(cNamed.MODULUS.mod(note)).size()];
					ps.NOTENAMES[idx] = noteName;
				} else {
					String pitchClassName = null;
					
					// The nearest notes to this in the named chord and their distances
					int lower = cNamed.floor(note);
					int dL = cNamed.MODULUS.mod(note-lower);
					int upper = cNamed.ceiling(note);
					int dU = cNamed.MODULUS.mod(upper-note);
					
					// The upper note is closer, name from it by stepping down from it.
					if(dU < dL) {
						char upperLetter = cNamed.NOTENAMES[cNamed.headSet(upper).size()].charAt(0);
						int heptatonicClass = Chord.Modulus.toHeptatonicNumber(upperLetter);
						while(pitchClassName == null) {
							heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass - 1);
							char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
							pitchClassName = Enharmonics.tryToName(c, note, false);
						}
						
					// The lower note is closer, name from it
					} else if(dL < dU) {
						char lowerLetter = cNamed.NOTENAMES[cNamed.headSet(lower).size()].charAt(0);
						int heptatonicClass = Chord.Modulus.toHeptatonicNumber(lowerLetter);
						while(pitchClassName == null) {
							// Step the heptatonic class of the note up from where it was.
							heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass + 1);
							char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
							pitchClassName = Enharmonics.tryToName(c, note, false);
						}
					// They are the same distance from each other.  Do same as upper (i.e., prefer flats or naturals).
					} else {
						char upperLetter = cNamed.NOTENAMES[cNamed.headSet(upper).size()].charAt(0);
						int heptatonicClass = Chord.Modulus.toHeptatonicNumber(upperLetter);
						while(pitchClassName == null) {
							heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass - 1);
							char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
							pitchClassName = Enharmonics.tryToName(c, note, false);
						}
					}
					
					// Fallback to the Key if necessary.
					if(pitchClassName == null && k.length > 0) {
						ps.NOTENAMES[idx] = k[0].getNoteName(note) + Chord.TWELVETONE.octave(note);
					}
					
					ps.NOTENAMES[idx] = pitchClassName + Chord.TWELVETONE.octave(note);
				}
				
				idx++;
			}
		}
	}
	
	/**
	 * Given two chords, one of which has its NOTENAMES field filled out, fills in the field for
	 * the other chord.  It does so with a model that tries always name notes outside the second
	 * chord with names outside of the second chord while minimizing use of double accidentals.
	 * 
	 * @param c1 a chord, must have NOTENAMES set to null to have any effect
	 * @param c2Named a chord with NOTENAMES set to a valid set of values
	 */
	public static void fillEnharmonics(Chord c1, Chord c2Named) {
		assert(c2Named.NOTENAMES != null);
		assert(c2Named.MODULUS.OCTAVE_STEPS == c1.MODULUS.OCTAVE_STEPS);
		if(c1.NOTENAMES != null)
			return;
		
		int idx = 0;
		c1.NOTENAMES = new String[c1.size()];
		Log.i(TAG,"Solving" + c1.toString() + " to " + c2Named.toString() + " as " + Arrays.toString(c2Named.NOTENAMES));
		
		// Conveniently, iteration through Chords and PitchSets is in order.
		for(int pitchClass : c1) {
			if(pitchClass == 6)
				Log.i(TAG,"Dat F#");
			
			// Pitch Class is in the chord we resolve/move to and should have the same name it does in
			// the resolved chord
			if(c2Named.contains(pitchClass)) {
				String pitchClassName = c2Named.NOTENAMES[c2Named.headSet(c2Named.MODULUS.mod(pitchClass)).size()];
				c1.NOTENAMES[idx] = pitchClassName;
			
			// Pitch Class is *not* in the named chord.  Should not share a letter name if at all possible.
			} else {
				String pitchClassName = null;
				
				// The nearest notes to this in the named chord and their distances
				int lower = c2Named.floor(pitchClass);
				int dL = c2Named.MODULUS.mod(pitchClass-lower);
				int upper = c2Named.ceiling(pitchClass);
				int dU = c2Named.MODULUS.mod(upper-pitchClass);
				
				// The upper note is closer, name from it by stepping down from it.
				if(dU < dL) {
					char upperLetter = c2Named.NOTENAMES[c2Named.headSet(upper).size()].charAt(0);
					int heptatonicClass = Chord.Modulus.toHeptatonicNumber(upperLetter);
					while(pitchClassName == null) {
						heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass - 1);
						char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
						pitchClassName = Enharmonics.tryToName(c, pitchClass, false);
					}
					
				// The lower note is closer, name from it
				} else if(dL < dU) {
					char lowerLetter = c2Named.NOTENAMES[c2Named.headSet(lower).size()].charAt(0);
					int heptatonicClass = Chord.Modulus.toHeptatonicNumber(lowerLetter);
					while(pitchClassName == null) {
						// Step the heptatonic class of the note up from where it was.
						heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass + 1);
						char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
						pitchClassName = Enharmonics.tryToName(c, pitchClass, false);
					}
				// They are the same distance from each other.  Do same as upper.
				} else {
					char upperLetter = c2Named.NOTENAMES[c2Named.headSet(upper).size()].charAt(0);
					int heptatonicClass = Chord.Modulus.toHeptatonicNumber(upperLetter);
					while(pitchClassName == null) {
						heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass - 1);
						char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
						pitchClassName = Enharmonics.tryToName(c, pitchClass, false);
					}
				}
				c1.NOTENAMES[idx] = pitchClassName;
			}
			
			idx++;
		}
	}
	
	public static void fillEnharmonics(PitchSet ps, Key k) {
		if(ps.NOTENAMES == null) {
			ps.NOTENAMES = new String[ps.size()];
			int idx = 0;
			for(int note : ps) {
				ps.NOTENAMES[idx++] = k.getNoteName(note) + Chord.TWELVETONE.octave(note);
			}
		}
	}
	
	public static PitchSet noteNameToPitchSet(String noteName) {
		return new PitchSet(noteNameToInt(noteName));
	}

	/**
	 * Given a letter A-G/a-g, returns a String with the needed (double)flats/sharps to make them equivalent, with
	 * C = 0, D = 2, E = 4, F = 5, G = 7, A = 9, B = 11 (i.e., traditional twelve-tone system at C = 0).
	 * 
	 * Returns null if impossible.
	 * 
	 * i.e., tryToName('C', 0, true/false) = "C", tryToName('B', 0, true/false) = "B#", tryToName('A', -1, true) = "A##",
	 * and tryToName('A', -1, false) = null
	 * 
	 * @param heptatonicName a letter A-G
	 * @param targetTwelveTonePitchClass any numer (treated as C4=0 and so on, only its pitch class matters)
	 * @param doubleAccidentals true to enable double flats and sharps
	 * @return
	 */
	public static String tryToName(char heptatonicName, int targetTwelveTonePitchClass, boolean doubleAccidentals) {
		int s = Key.twelveToneInverse.get(heptatonicName);
		switch(Chord.TWELVETONE.mod(s - targetTwelveTonePitchClass)) {
			case 0: return "" + heptatonicName;
			case 11: return heptatonicName + "#";
			case 10: if(!doubleAccidentals) return null;
					 else return heptatonicName + "##";
			case 1: return new String(new char[] {heptatonicName, FLAT}); // heptatonicName + flat; 
			case 2: if(!doubleAccidentals) return null;
					else return new String(new char[] {heptatonicName, FLAT, FLAT});
			default: return null;
		}
	}

	/**
	 * Can convert any named note (C, Eb, Bbb, Db6) into the appropriate string representation.
	 * If no octave is provided we assume we are in the 4th octave (above middle C, in the numeric range 0-11).
	 * @param noteName
	 * @return
	 */
	public static int noteNameToInt(String noteName) {
		assert(noteName.length() <= 5);
		char[] chars = noteName.toCharArray();
		int result = Key.twelveToneInverse.get(chars[0]);
		
		for( int i = 1; i < chars.length; i = i+1) {
			if( chars[i] == 'b' || chars[i] == FLAT )
				result = result - 1;
			else if( chars[i] == '#' )
				result = result + 1;
			else if( chars[i] == '1' || chars[1] == '2' ||chars[1] == '3' ||chars[1] == '4' ||chars[1] == '5' ||chars[1] == '6' ||chars[1] == '7' ||chars[1] == '8' ||chars[1] == '9' ||chars[1] == '0' ){
				result = result + 12 * (Integer.parseInt(new String(new char[] {chars[i]})) - 4);
			}
		}
		
		return result;
	}

	/**
	 * Returns true if the note represented by the given is flat.
	 * 
	 * @param noteName ex. "F", "F#", "Eb4", "G" + Enharmonics.natural
	 * @return
	 */
	public boolean isFlat(String noteName) {
		if(noteName.length() > 1) {
			if(noteName.charAt(1) == 'b' || noteName.charAt(1) == FLAT) {
				if(noteName.length() > 2) {
					if(noteName.charAt(2) == 'b' || noteName.charAt(2) == FLAT)
						return false;
					else
						return true;
				} else
					return true;
			} else
				return false;
		} else
			return false;
	}
	
	/**
	 * Returns true if the note represented by the given is double-flat.
	 * 
	 * @param noteName ex. "F", "F#", "Eb4", "G" + Enharmonics.natural
	 * @return
	 */
	public boolean isDoubleFlat(String noteName) {
		if(noteName.length() > 2 
				&& (noteName.charAt(1) == 'b' || noteName.charAt(1) == FLAT)
				&& (noteName.charAt(2) == 'b' || noteName.charAt(2) == FLAT))
			return true;
		else return false;
	}
	
	/**
	 * Returns true if the note represented by the given is sharp.
	 * 
	 * @param noteName ex. "F", "F#", "Eb4", "G" + Enharmonics.natural
	 * @return
	 */
	public boolean isSharp(String noteName) {
		if(noteName.length() > 1) {
			if(noteName.charAt(1) == '#') {
				if(noteName.length() > 2) {
					if(noteName.charAt(2) == '#')
						return false;
					else
						return true;
				} else
					return true;
			} else
				return false;
		} else
			return false;
	}
	
	/**
	 * Returns true if the note represented by the given is double-sharp.
	 * 
	 * @param noteName ex. "F", "F#", "Eb4", "G" + Enharmonics.natural
	 * @return
	 */
	public boolean isDoubleSharp(String noteName) {
		if(noteName.length() > 2 
				&& (noteName.charAt(1) == '#')
				&& (noteName.charAt(2) == '#'))
			return true;
		else return false;
	}
	
	/**
	 * Fill in the enharmonics for a given chord using the given Key's way of naming notes.  Useful if
	 * you know the Chord fits in the key (i.e., a cadence).
	 * @param c
	 * @param k
	 */
	public static void fillEnharmonics(Chord c, Key k) {
		if(c.NOTENAMES == null) {
			c.NOTENAMES = new String[c.size()];
			int idx = 0;
			for(int pitchClass : c) {
				c.NOTENAMES[idx] = k.getNoteName(pitchClass);
			}
		}
	}

	
	public static Chord toNamedChord(PitchSet ps2Named) {
		Chord c = new Chord(ps2Named);
		c.NOTENAMES = new String[c.size()];
		int idx = 0;
		for(int n : ps2Named) {
			int noteNameIndex = c.headSet( Chord.TWELVETONE.mod(n) ).size();
			String pitchSetNoteName = ps2Named.NOTENAMES[idx];
			String chordNoteName = pitchSetNoteName.substring(0, pitchSetNoteName.length() - 2);
			c.NOTENAMES[noteNameIndex] = chordNoteName;
			idx++;
		}
		return c;
	}
	
	public static void fillEnharmonics(PitchSet ps1, PitchSet ps2Named) {
		Chord c1 = new Chord(ps1);
		Chord c2Named = toNamedChord(ps2Named);
		fillEnharmonics(c1, c2Named);
		fillEnharmonics(ps1, c1);
	}
}

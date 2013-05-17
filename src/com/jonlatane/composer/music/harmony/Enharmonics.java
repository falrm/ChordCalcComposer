package com.jonlatane.composer.music.harmony;

import java.util.Arrays;
import java.util.LinkedList;

import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.Staff;

import android.util.Log;

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
	public Enharmonics() {
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
	 */
	public static void fillEnharmonics(PitchSet ps1, Chord c1, PitchSet ps2, Chord c2Named, Key... k) {
		assert(c2Named.NOTENAMES != null);
		
		// If a key was provided, fill c2Named (in which case the variable name is a misnomer
		if(k!=null && k.length>0)
			fillEnharmonics(c2Named, k[0]);
		
		// Fill in ps2's notenames if needed
		fillEnharmonics(ps2,c1);
		
		// Fill in c1
		fillEnharmonics(c1, c2Named);
		
		//Fill in ps1
		fillEnharmonics(ps1,c1);
	}
	
	public static void fillEnharmonics(PitchSet ps, Chord cNamed) {
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
							pitchClassName = Key.tryToName(c, note, false);
						}
						
					// The lower note is closer, name from it
					} else if(dL < dU) {
						char lowerLetter = cNamed.NOTENAMES[cNamed.headSet(lower).size()].charAt(0);
						int heptatonicClass = Chord.Modulus.toHeptatonicNumber(lowerLetter);
						while(pitchClassName == null) {
							// Step the heptatonic class of the note up from where it was.
							heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass + 1);
							char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
							pitchClassName = Key.tryToName(c, note, false);
						}
					// They are the same distance from each other.  Do same as upper.
					} else {
						char upperLetter = cNamed.NOTENAMES[cNamed.headSet(upper).size()].charAt(0);
						int heptatonicClass = Chord.Modulus.toHeptatonicNumber(upperLetter);
						while(pitchClassName == null) {
							heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass - 1);
							char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
							pitchClassName = Key.tryToName(c, note, false);
						}
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
						pitchClassName = Key.tryToName(c, pitchClass, false);
					}
					
				// The lower note is closer, name from it
				} else if(dL < dU) {
					char lowerLetter = c2Named.NOTENAMES[c2Named.headSet(lower).size()].charAt(0);
					int heptatonicClass = Chord.Modulus.toHeptatonicNumber(lowerLetter);
					while(pitchClassName == null) {
						// Step the heptatonic class of the note up from where it was.
						heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass + 1);
						char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
						pitchClassName = Key.tryToName(c, pitchClass, false);
					}
				// They are the same distance from each other.  Do same as upper.
				} else {
					char upperLetter = c2Named.NOTENAMES[c2Named.headSet(upper).size()].charAt(0);
					int heptatonicClass = Chord.Modulus.toHeptatonicNumber(upperLetter);
					while(pitchClassName == null) {
						heptatonicClass = Chord.HEPTATONIC.mod(heptatonicClass - 1);
						char c = Chord.Modulus.toHeptatonicCharacter(heptatonicClass);
						pitchClassName = Key.tryToName(c, pitchClass, false);
					}
				}
				c1.NOTENAMES[idx] = pitchClassName;
			}
			
			idx++;
		}
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

}

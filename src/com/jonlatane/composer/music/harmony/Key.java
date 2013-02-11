package com.jonlatane.composer.music.harmony;

import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import java.util.*;

/**
 * This class is responsible for the work of enharmonics.  
 * @author Jon
 *
 */
public final class Key extends Scale
{
	private static final long serialVersionUID = 6430773851042936649L;
	private static final String TAG = "Key";
	private static final char[] heptatonicNotes = { 'C', 'D', 'E', 'F', 'G', 'A', 'B' };
	private static final SparseArray<Character> twelveToneNames = new SparseArray<Character>();
	private static final HashMap<Character,Integer> twelveToneInverse = new HashMap<Character,Integer>();
	private static final String[] majorKeys = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };
	private static final String[] minorKeys = { "C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B" };
	public static Key CMajor = new Key(new MajorScale(0));
	public static Key CChromatic = new Key(new ChromaticScale(0));
	
	static{
		twelveToneNames.put(0, 'C');
		twelveToneNames.put(2, 'D');
		twelveToneNames.put(4, 'E');
		twelveToneNames.put(5, 'F');
		twelveToneNames.put(7, 'G');
		twelveToneNames.put(9, 'A');
		twelveToneNames.put(11,'B');
		
		twelveToneInverse.put('C',0);
		twelveToneInverse.put('D',2);
		twelveToneInverse.put('E',4);
		twelveToneInverse.put('F',5);
		twelveToneInverse.put('G',7);
		twelveToneInverse.put('A',9);
		twelveToneInverse.put('B',11);
		twelveToneInverse.put('c',0);
		twelveToneInverse.put('d',2);
		twelveToneInverse.put('e',4);
		twelveToneInverse.put('f',5);
		twelveToneInverse.put('g',7);
		twelveToneInverse.put('a',9);
		twelveToneInverse.put('b',11);
	}
	
	private String _rootName;
	
	public Key() {
		super();
	}
	
	public Key(Chord c) {
		super(c);
		
		String blah = "Key constructed with Chord [";
		for(int i : c)
			blah += i + ",";
		blah += "] Root:" + c.getRoot();
		Log.i(TAG,blah);
		
		if(c.getClass().equals(Key.class)) {
			setRootName(((Key)c).getRootName());
		} else if( c instanceof Scale ) {
			Scale s = (Scale)c;
			if(s.isMajor()) {
				//Log.i(TAG,"Key is major" + (majorKeys.length));
				_rootName = majorKeys[c.getRoot()];
			}
			if(s.isMinor())
				_rootName = minorKeys[c.getRoot()];
		}
	}
	
	// Override these to be sure root and root name are consistent
	@Override
	public Integer getRoot() {
		int rootName = noteNameToInt(_rootName);
		assert(rootName == super.getRoot());
		return super.getRoot();
	}
	
	public String getRootName() {
		int rootName = noteNameToInt(_rootName);
		assert(rootName == super.getRoot());
		return _rootName;
	}
	
	/**
	 * Sets the root name if and only if the name matches the set root of the chord.  To change the root,
	 * use setRoot().
	 * @param str
	 * @return
	 */
	public boolean setRootName(String str) {
		if(noteNameToInt(str) == getRoot()) {
			_rootName = str;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the note name for the given note using its own root name.  Assumes this is a heptatonic key (i.e., major minor or modal)
	 * @param i
	 * @return
	 */
	public String getNoteName(Integer i) {
		String result = "";
		i = MODULUS.getPitchClass(i);
		/*String scaleContents = "[";
		for(Integer k : this) {
			scaleContents += k+",";
		}
		scaleContents += "]";
		Log.i(TAG,"Getting note name" +scaleContents + _rootName);*/
		int rootCharIndex = twelveToneInverse.get( _rootName.charAt(0) );
		
		Pair<Integer,Integer> p = degreeOf(i);
		if(p.first == p.second) {
			char letterName = heptatonicNotes[(rootCharIndex + p.first-1) % 7];
			result += letterName;
			if( i < twelveToneInverse.get(letterName)) {
				result += 'b';
			}
			if( i > twelveToneInverse.get(letterName)) {
				result += '#';
			}
		} else {
			Log.i(TAG,""+p.first+p.second);
			// represent it as a sharp/double-sharp/flat
			if( ((i-p.first) % 12) < ((p.second-i) % 12)) {
				char letterName = heptatonicNotes[(rootCharIndex + p.first-1) % 7];
				result += letterName;
				
				if( i < twelveToneInverse.get(letterName)) {
					result += 'b';
				}
				if( i > twelveToneInverse.get(letterName)) {
					result += '#';
				}
				if( i > (twelveToneInverse.get(letterName)-1)) {
					result += '#';
				}
			//represent as a flat/double-flat/sharp
			} else {
				char letterName = heptatonicNotes[(rootCharIndex + p.second - 1) % 12];
				result += letterName;
				
				if( i < twelveToneInverse.get(letterName)) {
					result += 'b';
				}
				// Double-flats and sharps are possible maybe?
				if( i < twelveToneInverse.get(letterName)-1) {
					result += 'b';
				}
				if( i > twelveToneInverse.get(letterName)) {
					result += '#';
				}
			}
			
		}
		Log.i(TAG,"Got note name " + result);
		return result;
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
		int  result = twelveToneInverse.get(chars[0]);
		
		for( int i = 1; i < chars.length; i = i+1) {
			if( chars[i] == 'b' )
				result = result - 1;
			else if( chars[i] == '#' )
				result = result + 1;
			else if( chars[i] == '1' || chars[1] == '2' ||chars[1] == '3' ||chars[1] == '4' ||chars[1] == '5' ||chars[1] == '6' ||chars[1] == '7' ||chars[1] == '8' ||chars[1] == '9' ||chars[1] == '0' ){
				result = result + 12 * (Integer.parseInt(new String(new char[] {chars[i]})) - 4);
			}
		}
		
		return result;
	}
	
	public static PitchSet noteNameToPitchSet(String noteName) {
		return new PitchSet(noteNameToInt(noteName));
	}
	
	public static Pair<String,Integer> guessName(Chord c, Integer root, Key k) {
		Pair<String,Integer> data = guessName(c, root);
		String rootName = k.getNoteName(root);
		return new Pair<String,Integer>(rootName+data.first, data.second);
	}
	
	public static Pair<String,Integer> guessNameInC(Chord c, Integer root) {
		//Log.i(TAG,"guessNameInC");
		return guessName(c, root, CMajor);
	}
	
	/**
	 * Guess the name of the chord without knowing its root (we will ignore the root specified).
	 * Assume the root is in the chord.
	 * 
	 * @param c
	 * @return
	 */
	public static Pair<String,Integer> guessName(Chord c, Key k) {
		Pair<String,Integer> result = new Pair<String,Integer>("", 0);
		for(int i : c) {
			Pair<String,Integer> contestant = guessName(c, i, k);
			if(contestant.second > result.second) {
				result = contestant;
			}
			Log.i(TAG, "Root " + i + "got " + contestant.first + " @ " + contestant.second);
		}
		return result;
	}
	
	public static Pair<String,Integer> guessNameInC(Chord c) {
		//Log.i(TAG, "Guessing chord name in C" + (Key.CMajor != null));
		return guessName(c, Key.CMajor);
	}
	
	/**
	 * Guess the name of the chord without knowing its root (we will ignore the root specified).
	 * Assume the root can be anything.
	 * 
	 * @param c
	 * @return
	 */
	public static Pair<String,Integer> guessNameIncludeShells(Chord c, Key k) {
		Pair<String,Integer> result = new Pair<String,Integer>("", 0);
		for(int i = 0; i < 12; i++) {
			Pair<String,Integer> contestant = guessName(c, i, k);
			if(contestant.second > result.second) {
				result = contestant;
			}
		}
		return result;
	}
	
	/**
	 * Guess the name of the chord without knowing its root (we will ignore the root specified).
	 * Assume the root is in the key.
	 * 
	 * @param c
	 * @return
	 */
	public static Pair<String,Integer> guessDiatonic(Chord c, Key k) {
		Pair<String,Integer> result = new Pair<String,Integer>("", 0);
		for(int i : k) {
			Pair<String,Integer> contestant = guessName(c, i, k);
			if(contestant.second > result.second) {
				result = contestant;
			}
		}
		return result;
	}
	
	public static TreeMap<Integer,List<String>> getRootLikelihoodsAndNames(Collection<Integer> inputRootCandidates, Chord c, Key k) {
		TreeMap<Integer,List<String>> result = new TreeMap<Integer,List<String>>();
		
		for( int n : inputRootCandidates) {
			Pair<String,Integer> candidate = guessName(c, n);
			if(c.getRoot() != null && n == c.getRoot()) {
				candidate = new Pair<String,Integer>(candidate.first, candidate.second + 1000);
			}
			List<String> bucket = result.get(candidate.second);
			if( bucket == null ) {
				bucket = new LinkedList<String>();
				result.put(candidate.second, bucket);
			}
			
			String rootName = k.getNoteName(n);
			bucket.add("[" + rootName + "] "+ candidate.first + " | ");
		}
		
		return result;
	}
	
	public static TreeMap<Integer,List<String>> getRootLikelihoodsAndNamesInC(Collection<Integer> inputRootCandidates, Chord c) {
		return getRootLikelihoodsAndNames(inputRootCandidates, c, CMajor);
	}

}

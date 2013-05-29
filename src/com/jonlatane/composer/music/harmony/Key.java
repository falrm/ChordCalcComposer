package com.jonlatane.composer.music.harmony;

import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A Key is just what we think it is, and is sort of where we tie together
 * all the harmonic things defined at lower levels.  We must always assume a Key
 * is a major or minor scale represented with 0-7 flats or sharps, which occur in
 * circle-of-fifths order.  That is what most of the logic below accomplishes.  This
 * provides us with 30 total keys to write in.
 * 
 * @author Jon
 *
 */
public final class Key extends Scale
{
	private static final long serialVersionUID = 6430773851042936649L;
	private static final String TAG = "Key";
	private static final char[] heptatonicNotes = { 'C', 'D', 'E', 'F', 'G', 'A', 'B' };
	private static final HashMap<Character,Integer> heptatonicInverse = new HashMap<Character,Integer>();
	private static final SparseArray<Character> twelveToneNames = new SparseArray<Character>();
	static final HashMap<Character,Integer> twelveToneInverse = new HashMap<Character,Integer>();
	
	// These two arrays provide us with a list of the names we personally prefer for each of the major and minor keys.
	// When a Key is constructed, it will assume this is the root name, though that can be overridden.
	private static final String[] majorKeys = { "C", "D"+Enharmonics.flat, "D", "E"+Enharmonics.flat, "E", "F", "G"+Enharmonics.flat, "G", "A"+Enharmonics.flat, "A", "B"+Enharmonics.flat, "B" };
	private static final String[] minorKeys = { "C", "C#", "D", "E"+Enharmonics.flat, "E", "F", "F#", "G", "G#", "A", "B"+Enharmonics.flat, "B" };
	
	// ALL THE MAJOR/MINOR KEYS.  The above arrays define their default names, but they may be overridden.
	public static final Key CMajor = new Key(new MajorScale(0));
	public static final Key CMinor = new Key(new NaturalMinorScale(0));
	
	public static final Key DbMajor = new Key(new MajorScale(1));
	public static final Key CsMajor = new Key(new MajorScale(1));
	public static final Key CsMinor = new Key(new NaturalMinorScale(1));
	
	public static final Key DMajor = new Key(new MajorScale(2));
	public static final Key DMinor = new Key(new NaturalMinorScale(2));
	
	public static final Key EbMajor = new Key(new MajorScale(3));
	public static final Key EbMinor = new Key(new NaturalMinorScale(3));
	public static final Key DsMinor = new Key(new NaturalMinorScale(3));
	
	public static final Key EMajor = new Key(new MajorScale(4));
	public static final Key EMinor = new Key(new NaturalMinorScale(4));
	
	public static final Key FMajor = new Key(new MajorScale(5));
	public static final Key FMinor = new Key(new NaturalMinorScale(5));
	
	public static final Key GbMajor = new Key(new MajorScale(6));
	public static final Key FsMajor = new Key(new MajorScale(6));
	public static final Key FsMinor = new Key(new NaturalMinorScale(6));
	
	public static final Key GMajor = new Key(new MajorScale(7));
	public static final Key GMinor = new Key(new NaturalMinorScale(7));
	
	public static final Key AbMajor = new Key(new MajorScale(8));
	public static final Key GsMinor = new Key(new NaturalMinorScale(8));
	public static final Key AbMinor = new Key(new NaturalMinorScale(8));
	
	public static final Key AMajor = new Key(new MajorScale(9));
	public static final Key AMinor = new Key(new NaturalMinorScale(9));
	
	public static final Key BbMajor = new Key(new MajorScale(10));
	public static final Key BbMinor = new Key(new NaturalMinorScale(10));
	public static final Key AsMinor = new Key(new NaturalMinorScale(10));
	
	public static final Key BMajor = new Key(new MajorScale(11));
	public static final Key CbMajor = new Key(new MajorScale(11));
	public static final Key BMinor = new Key(new NaturalMinorScale(11));
		
	public static final Key CChromatic = new Key(new ChromaticScale(0));
	
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
		
		heptatonicInverse.put('C',0);
		heptatonicInverse.put('D',1);
		heptatonicInverse.put('E',2);
		heptatonicInverse.put('F',3);
		heptatonicInverse.put('G',4);
		heptatonicInverse.put('A',5);
		heptatonicInverse.put('B',6);
		
		FsMajor.setRootName("F#");
		AbMinor.setRootName("A" + Enharmonics.flat);
		CbMajor.setRootName("C" + Enharmonics.flat);
		CsMajor.setRootName("C#");
		DsMinor.setRootName("D#");
		AsMinor.setRootName("A#");
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
		
		if(c.getClass().equals(Key.class) && ((Key)c).getRootName() != null) {
			setRootName(((Key)c).getRootName());
		} else if( c instanceof Scale ) {//c.getClass().(Scale.class) || c.getClass().equals(Scale.MajorScale.class)) {
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
		if(_rootName != null) {
			assert(Enharmonics.noteNameToInt(_rootName) == super.getRoot());
		}
		return super.getRoot();
	}
	
	public String getRootName() {
		int rootFromName = Enharmonics.noteNameToInt(_rootName);
		assert(rootFromName == super.getRoot());
		return _rootName;
	}
	
	/**
	 * Sets the root name if and only if the name matches the set root of the chord.  To change the root,
	 * use setRoot().
	 * @param str
	 * @return
	 */
	public boolean setRootName(String str) {
		if(Enharmonics.noteNameToInt(str) == getRoot()) {
			_rootName = str;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o.getClass().equals(Key.class)) {
			Key k = (Key)o;
			if(containsAll(k) && k.containsAll(this) && k.getRoot().equals(getRoot()) && k.getRootName().equals(getRootName()))
				return true;
			else
				return false;
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the note name for the given note using this Key's root name.  Assumes this is a 
	 * heptatonic key (i.e., major, minor or modal) that can be represented with only sharps,
	 * flats, double sharps and double flats.  Naturals will also be represented if the
	 * key assumes a note is flat.
	 * 
	 * Depending on this key, results will change accidentals dynamically.  For instance:
	 * 
	 *  Key | A | Ab
	 *  C   | A | Ab
	 *  C-  | An| A
	 * 
	 * @param i
	 * @return
	 */
	public String getNoteName(int i) {
		Log.i(TAG,"Getting note name for key" + toString() + " root name:" + _rootName);
		String result = "";
		i = MODULUS.mod(i);

		// This method works by careful interlocking of the heptatonic and twelve-tone systems.
		int rootCharIndex = heptatonicInverse.get( _rootName.charAt(0) );
		
		// Note that this is scale degrees and thus is assumed to be heptatonic (as per documentation)
		Pair<Integer,Integer> p = degreeOf(i);
		
		if(p.first == p.second) {
			char letterName = heptatonicNotes[(rootCharIndex + p.first-1) % 7];
			result += letterName;
			if( i < twelveToneInverse.get(letterName)) {
				result += Enharmonics.flat;
			}
			if( i > twelveToneInverse.get(letterName)) {
				result += '#';
			}
		} else {
			Log.i(TAG,"Trying to name note not in Key: " + p.first + ","+p.second
					+ ";" + TWELVETONE.distance( i, getDegree(p.first) ) +","+ TWELVETONE.distance( i, getDegree(p.second)));
			// represent it as a sharp/double-sharp/flat
			//if( TWELVETONE.mod(i-getDegree(p.first)) < TWELVETONE.mod(getDegree(p.second)-i) ) {
			if( TWELVETONE.distance( i, getDegree(p.first) ) < TWELVETONE.distance( i, getDegree(p.second) ) ) {
				char letterName = heptatonicNotes[HEPTATONIC.mod(rootCharIndex + p.first - 1)];
				result += letterName;
				
				// Check for flats
				if( i < twelveToneInverse.get(letterName)) {
					result += Enharmonics.flat;
				}
				
				// Check for sharps/double-sharps
				if( i > twelveToneInverse.get(letterName)) {
					result += '#';
				}
				if( i > (twelveToneInverse.get(letterName)+1)) {
					result += '#';
				}
			//represent as a flat/double-flat/sharp
			} else {
				char letterName = heptatonicNotes[HEPTATONIC.mod(rootCharIndex + p.second - 1)];
				result += letterName;
				
				// Check for sharps
				if( i > twelveToneInverse.get(letterName)) {
					result += '#';
				}
				
				// Check for flats/double flats
				if( i < twelveToneInverse.get(letterName)) {
					result += Enharmonics.flat;
				}
				if( i < twelveToneInverse.get(letterName)-1) {
					result += Enharmonics.flat;
				}
			}
			
		}
		Log.i(TAG,"Got note name " + result);
		return result;
	}
	
	public static Pair<String,Integer> guessName(Chord c, Integer root, Key k) {
		Pair<String,Integer> data = guessCharacteristic(c, root);
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
			Pair<String,Integer> candidate = guessCharacteristic(c, n);
			if(c.getRoot() != null && n == c.getRoot()) {
				candidate = new Pair<String,Integer>(candidate.first, candidate.second + 1000);
			}
			List<String> bucket = result.get(candidate.second);
			if( bucket == null ) {
				bucket = new LinkedList<String>();
				result.put(candidate.second, bucket);
			}
			
			String rootName = k.getNoteName(n);
			bucket.add(rootName + candidate.first);
		}
		
		return result;
	}
	
	public int[] getRootLikelihoods(Chord c, Key k) {
		int[] result = new int[12];
		for(int i = 0; i < 12; i++) {
			result[i] = guessCharacteristic(c, i).second;
		}
		
		return result;
	}
	
	public static TreeMap<Integer,List<String>> getRootLikelihoodsAndNamesInC(Collection<Integer> inputRootCandidates, Chord c) {
		return getRootLikelihoodsAndNames(inputRootCandidates, c, CMajor);
	}

}

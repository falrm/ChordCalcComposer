package com.jonlatane.composer.music.harmony;

import android.content.res.Resources;
import android.content.res.TypedArray;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.*;

/*
* This class is implemented atop Google Resource APIs.
* Other resource APIs may have easy-to-implement functional equivalents.
*/
public final class Key extends Scale
{
	private String _rootName;
	private static final HashMap<Integer,Character> twelveToneNames = new HashMap<Integer,Character>();
	private static final HashMap<Character,Integer> twelveToneInverse = new HashMap<Character,Integer>();
	private static final HashMap<>
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
	}
	public Key(Modulus m) {
		super(m);
	}

	public Key(Collection<Integer> c) {
		super(c);
	}

	public Key(Collection<Integer> c, Modulus m) {
		super(c,m);
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
	
	public String getNoteName(Integer i) {
		String result = "";
		
		// Ideally the note is in the scale
		if(contains(i)) {
			result += twelveToneInverse.get(i % 12);
		} else {
			// If not, check if it's within range of a note in the key
			Integer l = lower(i);
			if( l - i == 1 ) {
				
			}
		}
		
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
	
	
}

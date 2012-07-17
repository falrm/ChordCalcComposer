package com.jonlatane.composer.music;

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
	
	public String getRootName() {
		return _rootName;
	}
	
	public String getNoteName(int i) {
		return getNoteName(i, this);
	}
	
	public String getNoteName(int i, Scale s) {
		assert(MODULUS.OCTAVE_STEPS == 12);
		
		int pc = MODULUS.getPitchClass(i);
		
		int root = twelveToneInverse.get(getRootName().toCharArray()[0]);
		Integer lower = s.lower(pc);
		if( lower == null )
			lower = s.lower( pc + 12 );
		Integer upper = s.higher(pc);
		if( upper == null )
			upper = s.higher( pc - 12 );
		
		if( MODULUS.getPitchClass(upper - i) > MODULUS.getPitchClass(i-lower) ) {
			int current = root;
		}
		
		//TODO
		return null;
	}
	
	public static int noteNameToInt(String noteName) {
		assert(noteName.length() <= 2);
		char[] chars = noteName.toCharArray();
		int  result = twelveToneInverse.get(chars[0]);
		
		if(noteName.length()>1) {
			if( chars[1] == 'b' )
				result = result - 1;
			else if( chars[1] == '#' )
				result = result + 1;
		}
		
		return Chord.Modulus.TWELVETONE.getPitchClass(result);
	}
	
	
}

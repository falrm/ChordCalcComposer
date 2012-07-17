package com.jonlatane.composer.music;

import java.util.*;

// A staff is a pair of a transposition and a 
public class Staff extends Rhythm
{
	private Integer _transposition = 0;
	private final TreeSet<Voice.Segment> _data = new TreeSet<Voice.Segment>();
	
	public Chord inStaffKey(Chord c) {
		return c;
	}
	
	public int getTransposition() {
		return _transposition;
	}
}

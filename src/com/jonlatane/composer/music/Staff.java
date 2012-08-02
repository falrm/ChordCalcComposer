package com.jonlatane.composer.music;

import java.util.*;
import com.jonlatane.composer.music.harmony.*;

// A staff is a pair of a transposition and a 
public class Staff extends Rhythm
{
	private Integer _transposition = 0;
	private final TreeSet<Voice.VoiceSegment> _data = new TreeSet<Voice.VoiceSegment>();
	
	public Chord inStaffKey(Chord c) {
		return c;
	}
	
	public int getTransposition() {
		return _transposition;
	}
}

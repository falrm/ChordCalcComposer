package com.jonlatane.composer;

import java.util.Collection;
import java.util.Map;

import com.jonlatane.composer.music.*;
import com.jonlatane.composer.music.harmony.*;
public class MusicUtil
{
	public static Score odeToJoy() {
		Voice v = new Voice();
		Staff s = new Staff(v);
		Score result = new Score();
		result.addStaff(s);
		
		RhythmMap<PitchSet> realization = v.getRealization();
		realization.put(new Rational(1,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(2,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(3,1), Key.noteNameToPitchSet("F"));
		realization.put(new Rational(4,1), Key.noteNameToPitchSet("G"));
		realization.put(new Rational(5,1), Key.noteNameToPitchSet("G"));
		realization.put(new Rational(6,1), Key.noteNameToPitchSet("F"));
		realization.put(new Rational(7,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(8,1), Key.noteNameToPitchSet("D"));
		realization.put(new Rational(9,1), Key.noteNameToPitchSet("C"));
		realization.put(new Rational(10,1), Key.noteNameToPitchSet("C"));
		realization.put(new Rational(11,1), Key.noteNameToPitchSet("D"));
		realization.put(new Rational(12,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(13,1), Key.noteNameToPitchSet("E"));
		realization.put(new Rational(29,2), Key.noteNameToPitchSet("D"));
		realization.put(new Rational(15,1), Key.noteNameToPitchSet("D"));
		
		return result;
	}
	public static String outputContents(Score s) {
		String result = "";
		for( Pair<Rational, Map<Staff,Map<Voice,Collection<Object>>>> nextChange = s.nextChangeAfter(new Rational(0,1)); nextChange != null;
				nextChange = s.nextChangeAfter(nextChange.getLeft())) {
			Rational r = nextChange.getLeft();
			result += r + ":: ";
			for(Map.Entry<Staff, Map<Voice,Collection<Object>>> staff : nextChange.getRight().entrySet() ) {
				for(Map.Entry<Voice,Collection<Object>> voice : staff.getValue().entrySet() ) {
					result += voice.getKey().hashCode() + ":";
					for(Object o : voice.getValue()) {
						if(o.getClass().equals(PitchSet.class)) {
							
						}
					}
				}
			}
			result += "\n";
		}
		
		return result;
	}

}


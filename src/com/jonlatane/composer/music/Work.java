package com.jonlatane.composer.music;
import java.util.Vector;
import java.util.TreeSet;

public class Work extends Vector<Part> {
	private TreeSet<Rational> overallRhythm;
	public TreeSet<TreeSet<Part>> grandStaves;
	public Work() {
		super();
		overallRhythm = new TreeSet<Rational>();
		updateOverallRhythm();
	}
	
	public void updateOverallRhythm() {
		overallRhythm.clear();
		for(Part p : this) {
			p.updateOverallRhythm();
			overallRhythm.addAll(p.getOverallRhythm());
		}
	}
	
	public TreeSet<Rational> getOverallRhythm() {
		return overallRhythm;
	}
}

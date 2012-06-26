package com.jonlatane.composer.music;

import java.util.Set;

public interface Covering<K>
{
	// generally a HashSet for efficiency
	public Set<K> getObjectsAt(Rational r);
	
	// the value of: getObjectsAt(r).size() == 0
	public boolean contains(Rational r);
}

package com.jonlatane.composer.music;

import android.content.res.Resources;
import android.content.res.TypedArray;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/*
* This class is implemented atop Google Resource APIs.
* Other resource APIs may have easy-to-implement functional equivalents.
*/
public final class Key extends Scale
{
	private String _rootName;
	public static void initializeKeys(Resources rs) {
		//TODO p
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
}

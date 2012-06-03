package com.jonlatane.composer.view;

import android.widget.ListView;
import android.content.Context;
import android.util.AttributeSet;
import com.jonlatane.composer.music.*;

public class VoiceView extends ListView
{
	private Voice v;
	public VoiceView(Context context, Voice v) {
		super(context);
		this.v = v;
	}
}

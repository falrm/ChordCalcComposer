package com.jonlatane.composer.io;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jonlatane.composer.R;
import com.jonlatane.composer.VerticalSeekBar;

import java.util.Arrays;
import java.util.Collections;

public class ToneControllerFragment extends Fragment {
	public static final String TAG = "ToneControllerFragment";
	private ManagedToneGenerator _toneGenerator = null;


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        final View result = inflater.inflate(R.layout.tonecontroller, container, false);
        Button add = (Button)result.findViewById(R.id.addOvertone);
        add.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				addElement();
				writeOvertones();
			}
        });

        Button remove = (Button)result.findViewById(R.id.removeOvertone);
        remove.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				removeElement();
				writeOvertones();
			}
        });
        
        return result;
	}
	
	synchronized View addElement() {
		LinearLayout ll = (LinearLayout) getView().findViewById(R.id.toneControllerOvertoneList);
		View v = LayoutInflater.from(ll.getContext()).inflate(R.layout.tonecontroller_element, ll);
		v = ll.getChildAt(ll.getChildCount()-1);

		TextView text = (TextView)v.findViewById(R.id.overtoneNumber);
		text.setText(Integer.toString(ll.getChildCount() - 1));
		
		VerticalSeekBar sb = ((VerticalSeekBar)v.findViewById(R.id.seekBar));
		sb.setProgress(0);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				Log.i(TAG, "SB changed ");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.i(TAG, "SB touch stopped ");
				writeOvertones();
			}
		});
		return v;
		//ll.addView(v);
	}
	synchronized void removeElement() {
		LinearLayout ll = (LinearLayout) getView().findViewById(R.id.toneControllerOvertoneList);
		if(ll.getChildCount() > 1) {
			ll.removeViewAt(ll.getChildCount()-1);
		}
	}
	
	public void attachToneGenerator(ManagedToneGenerator g) {
		_toneGenerator = g;
		double max = Collections.max(Arrays.asList(g._overtones));
		
		LinearLayout ll = (LinearLayout) getView().findViewById(R.id.toneControllerOvertoneList);
		while(ll.getChildCount() > g._overtones.length)
			removeElement();
		
		for(int i = 0; i < g._overtones.length; i++) {
			if(ll.getChildCount() - 1 < i)
				addElement();
			VerticalSeekBar vsb = (VerticalSeekBar)ll.getChildAt(i).findViewById(R.id.seekBar);
			int progress = (int) (vsb.getMax() * g._overtones[i]/max);
			vsb.setProgress(progress);
		}
		
		writeOvertones();
	}
	
	synchronized void writeOvertones() {
		Log.i(TAG, "Writing overtones ");
		if(_toneGenerator != null) {
			_toneGenerator.releaseAllMyTracks();
			_toneGenerator._overtones = getOvertones();
		}
	}
	
	Double[] getOvertones() {
		LinearLayout ll = (LinearLayout)this.getView().findViewById(R.id.toneControllerOvertoneList);
		Double[] result = new Double[ll.getChildCount()];
		for(int i = 0; i < ll.getChildCount(); i++) {
			VerticalSeekBar vsb = (VerticalSeekBar)ll.getChildAt(i).findViewById(R.id.seekBar);
			result[i] = (double) vsb.getProgress();
		}
		return result;
	}

    public boolean toneControllerEnabled() {
        return getView().getTranslationY() == 0;
    }
    public void hideToneController() {
        final View v = getView();
        int netHeight = v.getHeight();
        v.animate().translationY(netHeight);
    }
    public void showToneController() {
        final View v = getView();
		v.animate().translationY(0);
    }

    public void toggleToneController() {
        if(!toneControllerEnabled())
            showToneController();
        else
            hideToneController();
    }
}

package com.jonlatane.composer.input;

import com.jonlatane.composer.R;
import com.jonlatane.composer.R.layout;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.*;
import android.view.*;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TwelthKeyboardFragment extends Fragment {
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.twelthkeyboard, container, false);
        
        //do stuff?
        
        return result;
    }
	//The system calls this when it's time for the fragment to draw its user interface for the first time. To draw a UI for your fragment, you must return a View from this method that is the root of your fragment's layout. You can return null if the fragment does not provide a UI.
	public void onPause() {
		super.onPause();
	}
}

package com.jonlatane.composer;

import android.inputmethodservice.InputMethodService;
import android.view.View;

import com.jonlatane.composer.io.TwelthKeyboardFragment;

/**
 * Yeah so this isn't possible until we can get a FragmentManager from an InputMethodService.  Come on Google! Please?
 *
 * Created by jonlatane on 7/4/15.
 */
public class ChordKeyboardIME extends InputMethodService {
    View rootView;

    @Override
    public View onCreateInputView() {
        TwelthKeyboardFragment fragment = new TwelthKeyboardFragment();
        rootView = getLayoutInflater().inflate(R.layout.ime_keyboard, null);
        return rootView;
    }

}

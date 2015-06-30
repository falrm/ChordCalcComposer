package com.jonlatane.composer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.view.StaffDeltaView;

/**
 * Created by jonlatane on 6/28/15.
 */
public class StaffDeltaViewActivity extends BaseKeyboardActivity {
    protected RelativeLayout rootView;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = (RelativeLayout) findViewById(R.id.interactiveRoot);

        // Hide the keyboard fragment asynchronously
//        keyboard.hideKeyboardFragment();
        getWindow().getDecorView().findViewById(android.R.id.content).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                keyboard.hideKeyboardFragment();
                getWindow().getDecorView().findViewById(android.R.id.content).removeOnLayoutChangeListener(this);
            }
        });

        StaffDeltaView staffDeltaView = new StaffDeltaView(this);
        Score subject = Score.twinkleTwinkle();
        Score.testScore(subject);
        StaffDelta staffDelta = subject.getStaff(1).staffDeltaAt(Rational.ONE);
        staffDeltaView.setStaffDelta(staffDelta);

        rootView.addView(staffDeltaView);
        staffDeltaView.requestLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chorddisplaymenu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //TODO Interact with UI elements here
            case R.id.toggleKeyboardAB:
                keyboard.toggleKeyboardFragment();
                break;
            case R.id.toggleFX:
                toneController.toggleToneController(keyboard.getView());
                break;
            default:
                break;
        }

        return true;
    }
}

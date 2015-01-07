package com.jonlatane.composer;

import android.app.Activity;
import android.os.Bundle;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;
import com.jonlatane.composer.scoredisplay2.StaffDeltaView;

/**
 * Created by jonlatane on 6/30/14.
 */
public class ScoreDisplay2Activity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scoredisplay2activity);
        
        StaffDeltaView sdv = (StaffDeltaView) findViewById(R.id.staffDeltaView);
        Score score = Score.twinkleTwinkle();
        Score.fillEnharmonics(score);
        Score.resolveTies(score);
        sdv.setBackgroundColor(0xFFFFFF);
        StaffDelta sd = score.scoreIterator(Rational.get(13)).next().STAVES[1];
        sdv.setStaffDelta(sd);
        sdv.setWidth(500);
        sdv.setHeightAboveStaffCenter(300);
        sdv.setHeightBelowStaffCenter(300);
    }
}

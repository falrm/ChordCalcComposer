package com.jonlatane.composer.scoredisplay2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;

/**
 * This is a base class for
 * Created by jonlatane on 4/16/14.
 */
public abstract class StaffElementBase extends ViewGroup {

    private float _scalingFactor;
    private StaffDelta _staffDelta;
    public StaffElementBase(Context context) {
        super(context);
    }

    public void setStaffDelta(StaffDelta d) {
        _staffDelta = d;
    }

    public Rational getLocation() {
        return _staffDelta.LOCATION;
    }

    public StaffDelta getStaffDelta() {
        return _staffDelta;
    };

    /**
     * Classes implementing StaffElementBase should reimplement this.  It should
     * reflect the minimum required width on screen given the provided scaling factor.
     *
     * @return
     */
    @Override public abstract int getMinimumWidth();

    /**
     * Implementing classes must override this for getMinimumHeight() to work.
     *
     * @return
     */
    public abstract int getMinimumSpaceAboveCenter();

    /**
     * Implementing classes must override this for getMinimumHeight() to work.
     *
     * @return
     */
    public abstract int getMinimumSpaceBelowCenter();

    /**
     * Subclasses should not
     * @return
     */
    @Override public int getMinimumHeight() {
        return getMinimumSpaceAboveCenter() + getMinimumSpaceBelowCenter();
    }


    public float getScalingFactor() {
        return _scalingFactor;
    }

    public void setScalingFactor(float _scalingFactor) {
        this._scalingFactor = _scalingFactor;
    }
}

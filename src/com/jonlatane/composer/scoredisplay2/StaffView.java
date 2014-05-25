package com.jonlatane.composer.scoredisplay2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.scoredisplay2.StaffView.*;

/**
 * A StaffView consists of a header and several staff elements.  The header's appearance is
 * determined by the first two staff elements.
 *
 * Created by jonlatane on 4/1/14.
 */
public class StaffView<H extends AbstractStaffHeader, E extends AbstractStaffElement> extends ViewGroup {
    private H _header;
    private Score _score;
    private Rational _startingPoint;
    private float _firstElementVisibility = 1;

    public StaffView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean q, int l, int t, int r, int b) {
        _header.setFirstElement( (E)getChildAt(0) );
        _header.setSecondElement( (E)getChildAt(1) );
        _header.setFirstElementVisibility( _firstElementVisibility );
        _header.layout(l, t, l + _header.getWidth(), b);

        E first = (E)getChildAt(0);
        int x = l + _header.getWidth() - (int)(first.getWidth() * (1.0 - _firstElementVisibility));

        for(int i = 0; i < getChildCount(); i++) {
            E element = (E)getChildAt(i);
            element.layout(x, t, x + element.getWidth(), b);
            x += element.getWidth();
        }
    }

    public abstract class AbstractStaffElement extends View {
        private Score.Staff.StaffDelta _staffDelta;
        public AbstractStaffElement(Context context) {
            super(context);
        }

        public void setStaffDelta(Score.Staff.StaffDelta d) {
            _staffDelta = d;
        }

        public Rational getLocation() {
            return _staffDelta.LOCATION;
        }
    }

    /**
     * A staff header, visually, is the area where an instrument is named and where the clef, key
     * signature, and (possibly) the time signature
     * Created by jonlatane on 4/1/14.
     */
    public class AbstractStaffHeader extends View {
        AbstractStaffElement _first, _second;
        float _firstElementVisibility = 1;
        boolean _useLongInstrumentName = false;
        int _widthToRender = 0;

        public AbstractStaffHeader(Context context) {
            super(context);
        }

        public void setFirstElement(AbstractStaffElement element) {
            _first = element;
        }
        public void setSecondElement(AbstractStaffElement element) {
            _second = element;
        }
        public void setFirstElementVisibility(float visibility) {
            _firstElementVisibility = visibility;
        }
        public int getWidthToRender() {
            return _widthToRender;
        }
        protected void setWidthToRender() {

        }
    }

}

package com.jonlatane.composer.music.rhythm;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.coverings.TimeSignature;

/**
 * Created by jonlatane on 1/6/15.
 */
public class Notehead {
    private int type;
    private int numberOfDots;
    private int duplet = 1;

    private boolean isTiedToNext;
    private boolean isTiedToPrevious;

    private boolean isStartOfPhrase;
    private boolean isMiddleOfPhrase;
    private boolean isEndOfPhrase;


    // Types
    private static final int DOUBLE_WHOLE = 0;
    private static final int WHOLE = 1;
    private static final int HALF = 2;
    private static final int QUARTER = 3;
    private static final int EIGHTH = 4;
    private static final int SIXTEENTH = 5;
    private static final int THIRTY_SECOND = 6;
    private static final int SIXTY_FOURTH = 7;
    private static final int ONE_TWENTY_EIGHTH = 8;
    private static final int TWO_FIFTY_SIXTH = 9;

    /**
     * @param type one of DOUBLE_WHOLE, WHOLE, HALF, etc.
     */
    public Notehead(int type) {
        this(type, 0, 1);
    }

    /**
     * @param type one of DOUBLE_WHOLE, WHOLE, HALF, etc.
     * @param numberOfDots number of dots. Default 0 for other constructors.
     */
    public Notehead(int type, int numberOfDots) {
        this(type, numberOfDots, 1);
    }

    /**
     * @param type one of DOUBLE_WHOLE, WHOLE, HALF, etc.
     * @param numberOfDots number of dots. Default 0 for other constructors.
     * @param duplet default "1". "2" represents a duplet (i.e. like 6/8 time), "3" a triplet, "4" a quadruplet, etc.
     */
    public Notehead(int type, int numberOfDots, int duplet) {
        this.type = type;
        this.numberOfDots = numberOfDots;
        this.duplet = duplet;
    }

    /**
     * Returns a single notehead for the rational duration under the TS requested.  e.g. in 4/4 this
     * the input (1, 3|4) will return a QUARTER note.  (3, 6/8) will return a dotted quarter.  Etc.
     * @param period
     * @param ts
     * @return
     */
    public static Notehead getNoteheadForPeriodInTimeSignature(Rational period, TimeSignature ts) {
        return null;
    }

}

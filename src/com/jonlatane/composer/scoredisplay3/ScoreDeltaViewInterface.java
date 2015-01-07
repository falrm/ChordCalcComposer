package com.jonlatane.composer.scoredisplay3;

/**
 * Created by jonlatane on 12/31/14.
 */
public interface ScoreDeltaViewInterface {
    public int getAdapterPosition();

    public void setAdapterPosition(int positionInAdapter);

    /**
     * Set the visibility of this view within its parent (0-1)
     * @param f
     * @return
     */
    public void setVisibility(float f);
}

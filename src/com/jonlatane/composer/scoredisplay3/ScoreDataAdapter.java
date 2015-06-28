package com.jonlatane.composer.scoredisplay3;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * While a Score is essentially a mapping Rationals -> ScoreDeltas, this concerns itself with
 * the mapping Integers -> (Rational, ScoreDelta) Pairs. Note to any view reading data from this:
 * the views at 0 and size() - 1 are beginning and ending bookends, respectively, contain no data
 * and are not expected to draw anything.  Your View may set them to any width it wants.
 * Created by jonlatane on 12/24/14.
 */
public class ScoreDataAdapter<ChildType extends View & ScoreDeltaViewInterface> extends RecyclerView.Adapter<ScoreDataAdapter.ScoreDeltaHolder> {
    public static String TAG = "ScoreDataAdapter";

    public static int BACKGROUND_COLOR_BASE = 0xFFFFFFFF;
    public static int FOREGROUND_COLOR_BASE = 0xFF000000;

    // For testing
    private static final int SMALLEST_CHILD_WIDTH = 50;
    private static final int BIGGEST_CHILD_WIDTH = 80;
    private static final int SMALLEST_CHILD_HEIGHT = 150;
    private static final int BIGGEST_CHILD_HEIGHT = 600;

    private Score score;
    private ArrayList<Score.ScoreDelta> scoreData;

    public ScoreDataAdapter(Score score) {
        this.score = score;
        syncScoreData();
    }

    //TODO expand this to handle
    public void syncScoreData() {
        ArrayList<Score.ScoreDelta> newData = new ArrayList<Score.ScoreDelta>(score.getOverallRhythm().size());
        Iterator<Score.ScoreDelta> itr = score.scoreIterator(Rational.ZERO);
        int idx = 0;
        while(itr.hasNext()) {
            newData.add(idx++, itr.next());
        }
        // add the bookend view
        newData.add(score.scoreDeltaAt(new Rational(Integer.MIN_VALUE)));
        scoreData = newData;
    }

    @Override
    public ScoreDeltaHolder onCreateViewHolder(ViewGroup container, int position) {
        //TODO make generics better-er; i.e. refactor out ScoreDeltaHolder and use View & ScoreDeltaViewInterface
        ChildType scoreDeltaView = (ChildType)new DummyScoreDeltaView(container.getContext());
        scoreDeltaView.setAdapterPosition(position);
        TextView scoreDeltaText = new TextView(container.getContext());
        scoreDeltaText.setTextSize(5);
        scoreDeltaView.setBackgroundColor(0xFFFFFFFF);
        scoreDeltaText.setTextColor(0xFF000000);
        ((DummyScoreDeltaView)scoreDeltaView).addView(scoreDeltaText);

        return new ScoreDeltaHolder(scoreDeltaText, scoreDeltaView, this, position);
    }


    @Override
    public void onBindViewHolder(ScoreDeltaHolder scoreDeltaHolder, int position) {
        ChildType scoreDeltaView = (ChildType) scoreDeltaHolder.getChildScoreDeltaView();
        scoreDeltaView.setAdapterPosition(position);

        Score.ScoreDelta scoreDelta = getScoreDeltaAt(position);//scoreData.get(position);

        int width, height;
        if(scoreDelta != null) {
            scoreDeltaHolder.getDummyTextView().setText(scoreDelta.toString());

            // Set width to a random that will be consistent so long as the ScoreDelta doesn't change
            Random rand = new Random(scoreDelta.toString().hashCode());
            width = SMALLEST_CHILD_WIDTH;
            width += rand.nextInt(BIGGEST_CHILD_WIDTH - SMALLEST_CHILD_WIDTH);
            height = SMALLEST_CHILD_HEIGHT;
            height += rand.nextInt(BIGGEST_CHILD_HEIGHT - SMALLEST_CHILD_HEIGHT);
        } else { //bookends!
            width=SMALLEST_CHILD_WIDTH;
            height=SMALLEST_CHILD_HEIGHT;
            scoreDeltaHolder.getDummyTextView().setText(null);
        }

        scoreDeltaView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
    }

    //BOOKEND-RELATED OVERRIDES AND TOOLS

    @Override
    public int getItemCount() {
        return scoreData.size() + 2;
    }

    /**
     * Returns null for beginning and ending bookends
     * @param position
     * @return
     */
    private Score.ScoreDelta getScoreDeltaAt(int position) {
        if(position == 0 || position == getItemCount() - 1) {
            return null;
        }
        return scoreData.get(position - 1);
    }

    public boolean isBookend(ChildType view) {
        return view.getAdapterPosition() == 0 || view.getAdapterPosition() == getItemCount() - 1;
    }

    public ChildType getBeginningBookend(SystemRecyclerView parent) {
        return (ChildType)parent.getViewAtAdapterPosition(0);
    }

    public ChildType getEndingBookend(SystemRecyclerView parent) {
        return (ChildType)parent.getViewAtAdapterPosition(getItemCount()-1);
    }

    public void setEndingBookendDimensions(int width, int height, SystemRecyclerView parent) {
        ChildType endingBookend = getEndingBookend(parent);
        if(endingBookend != null) {
            endingBookend.setVisibility(0f);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(width, height);
            endingBookend.setLayoutParams(params);
        }
    }

    public static class ScoreDeltaHolder<ChildType extends View & ScoreDeltaViewInterface> extends RecyclerView.ViewHolder implements View.OnClickListener {
        private int adapterPosition;
        private ScoreDataAdapter adapter;
        private Score.ScoreDelta scoreDelta;
        private TextView dummyTextView;
        private ChildType childScoreDeltaView;


        public ScoreDeltaHolder(TextView dummyTextView, ChildType scoreDeltaView, ScoreDataAdapter adapter, int adapterPosition) {
            super(scoreDeltaView);
            this.dummyTextView = dummyTextView;
            this.childScoreDeltaView = scoreDeltaView;
            itemView.setOnClickListener(this);
            this.adapter = adapter;
            this.adapterPosition = adapterPosition;
            if(scoreDeltaView instanceof ScoreDeltaViewInterface) {
                scoreDeltaView.setAdapterPosition(adapterPosition);
            }
        }

        public void setAdapterPosition(int adapterPosition) {
            this.adapterPosition = adapterPosition;
        }

        public int getAdapterPosition() {
            return adapterPosition;
        }

        public TextView getDummyTextView() {
            return dummyTextView;
        }

        public ChildType getChildScoreDeltaView() {
            return childScoreDeltaView;
        }

        @Override
        public void onClick(View v) {
            adapter.onItemHolderClick(this);
        }

        public void setScoreDelta(Score.ScoreDelta scoreDelta) {
            this.scoreDelta = scoreDelta;
        }

    }


    private void onItemHolderClick(ScoreDeltaHolder<ChildType> scoreDeltaHolder) {
    }
}
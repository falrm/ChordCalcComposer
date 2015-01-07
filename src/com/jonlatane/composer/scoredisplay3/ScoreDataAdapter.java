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
 * the mapping Integers -> (Rational, ScoreDelta) Pairs.
 * Created by jonlatane on 12/24/14.
 */
public class ScoreDataAdapter extends RecyclerView.Adapter<ScoreDataAdapter.ScoreDeltaHolder> {
    public static String TAG = "ScoreDataAdapter";
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

    public void syncScoreData() {
        ArrayList<Score.ScoreDelta> newData = new ArrayList<Score.ScoreDelta>(score.getOverallRhythm().size());
        Iterator<Score.ScoreDelta> itr = score.scoreIterator(Rational.ZERO);
        int idx = 0;
        while(itr.hasNext()) {
            newData.add(idx++, itr.next());
        }
        scoreData = newData;
    }

    @Override
    public ScoreDeltaHolder onCreateViewHolder(ViewGroup container, int position) {
        DummyScoreDeltaView scoreDeltaView = new DummyScoreDeltaView(container.getContext());
        scoreDeltaView.setAdapterPosition(position);
        TextView scoreDeltaText = new TextView(container.getContext());
        scoreDeltaText.setTextSize(5);
        scoreDeltaView.setBackgroundColor(0xFFFFFFFF);
        scoreDeltaText.setTextColor(0xFF000000);
        scoreDeltaView.addView(scoreDeltaText);
        return new ScoreDeltaHolder(scoreDeltaText, scoreDeltaView, this, position);
    }

    @Override
    public void onBindViewHolder(ScoreDeltaHolder scoreDeltaHolder, int position) {
        Score.ScoreDelta data = scoreData.get(position);
        TextView dummyTextView = scoreDeltaHolder.getDummyTextView();
        dummyTextView.setText(data.toString());

        // Set width to a random that will be consistent so long as the ScoreDelta doesn't change
        DummyScoreDeltaView scoreDeltaView = scoreDeltaHolder.getDummyScoreDeltaView();
        scoreDeltaView.setAdapterPosition(position);
        Random rand = new Random(data.toString().hashCode());
        int width = SMALLEST_CHILD_WIDTH;
        width += rand.nextInt(BIGGEST_CHILD_WIDTH - SMALLEST_CHILD_WIDTH);
        int height = SMALLEST_CHILD_HEIGHT;
        height += rand.nextInt(BIGGEST_CHILD_HEIGHT - SMALLEST_CHILD_HEIGHT);

        scoreDeltaView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
    }

    @Override
    public int getItemCount() {
        return scoreData.size();
    }

    public static class ScoreDeltaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private int adapterPosition;
        private ScoreDataAdapter adapter;
        private Score.ScoreDelta scoreDelta;
        private TextView dummyTextView;
        private DummyScoreDeltaView dummyScoreDeltaView;


        public ScoreDeltaHolder(TextView dummyTextView, DummyScoreDeltaView scoreDeltaView, ScoreDataAdapter adapter, int adapterPosition) {
            super(scoreDeltaView);
            this.dummyTextView = dummyTextView;
            this.dummyScoreDeltaView = scoreDeltaView;
            itemView.setOnClickListener(this);
            this.adapter = adapter;
            this.adapterPosition = adapterPosition;
            scoreDeltaView.setAdapterPosition(adapterPosition);
        }

        public int getAdapterPosition() {
            return adapterPosition;
        }

        public TextView getDummyTextView() {
            return dummyTextView;
        }

        public DummyScoreDeltaView getDummyScoreDeltaView() {
            return dummyScoreDeltaView;
        }

        @Override
        public void onClick(View v) {
            adapter.onItemHolderClick(this);
        }

        public void setScoreDelta(Score.ScoreDelta scoreDelta) {
            this.scoreDelta = scoreDelta;
        }

    }

    private void onItemHolderClick(ScoreDeltaHolder scoreDeltaHolder) {
    }
}
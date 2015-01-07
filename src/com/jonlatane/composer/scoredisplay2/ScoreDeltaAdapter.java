package com.jonlatane.composer.scoredisplay2;

import java.util.Iterator;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.jonlatane.composer.music.Rational;
import com.jonlatane.composer.music.Score;
import com.jonlatane.composer.music.Score.ScoreDelta;

public class ScoreDeltaAdapter implements ListAdapter {
	private Score mScore;
	
	public ScoreDeltaAdapter(Score s) {
		mScore = s;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mScore.getOverallRhythm().size();
	}

	@Override
	public Object getItem(int position) {
		//TODO this must be made more efficient
		Iterator<ScoreDelta> itr = mScore.scoreIterator(Rational.ZERO);
		for(int idx = 0; idx < position; idx++)
			itr.next();
		return itr.next();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return false;
	}

}

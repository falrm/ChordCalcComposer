package com.jonlatane.composer.scoredisplay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;
import com.jonlatane.composer.R;
import com.jonlatane.composer.music.*;

/**
 * 
 * @author Jon
 *
 */
public class StaffView extends HorizontalListView {
	private SuperScore _score;
	private SuperScore.Staff _staff;
	public StaffView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public boolean setStaff(SuperScore.Staff s) {
		boolean result = (s == _staff);
		_staff = s;
		return result;
	}
	
	private class StaffSectionView extends LinearLayout {

		public StaffSectionView(Context context) {
			super(context);
	        View.inflate(context, R.layout.staff_section_view, this);
		}
		
	}
	
	
	private BaseAdapter _adapter = new BaseAdapter() {  
		  
        @Override  
        public int getCount() {  
            return 0;//_dataObjects.length;  
        }  
  
        @Override  
        public Object getItem(int position) {  
            return null;  
        }
  
        @Override  
        public long getItemId(int position) {  
            return 0;  
        }  
  
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.staff_section_view, null);  
        	StaffSectionView retval = new StaffSectionView(parent.getContext());
            TextView title = (TextView) retval.findViewById(R.id.leadSheetItemChord);  
            //title.setText(_dataObjects[position]);  
              
            return retval;  
        }  
          
    };  
    
}

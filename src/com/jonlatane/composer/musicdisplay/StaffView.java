package com.jonlatane.composer.musicdisplay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
	private Score _score;
	private Staff _staff;
	
	public StaffView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
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
            View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, null);  
            TextView title = (TextView) retval.findViewById(R.id.leadSheetItemTV);  
            //title.setText(_dataObjects[position]);  
              
            return retval;  
        }  
          
    };  

}

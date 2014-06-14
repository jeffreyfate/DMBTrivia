package com.jeffthefate.dmbquiz;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.R.color;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class SetlistAdapter extends BaseExpandableListAdapter {
    
    private int groupCount = 0;
    private TreeMap<String, TreeMap<String, String>> parseMap;
    private List<Object> keyList;
    private Context context;
    
    public class ViewHolder {
        private TextView setText;
        private int id = -1;
        private boolean isSelected = false;
        
        public TextView getSetText() {
        	return setText;
        }
        
        public void setSetText(TextView setText) {
        	this.setText = setText;
        }
        
        public boolean getIsSelected() {
        	return isSelected;
        }
        
        public void setIsSelected(boolean isSelected) {
        	this.isSelected = isSelected;
        }
    }

    public SetlistAdapter(Context context,
    		TreeMap<String, TreeMap<String, String>> parseMap) {
        this.context = context;
        this.parseMap = parseMap;
        keyList = Arrays.asList(parseMap.keySet().toArray());
        groupCount = parseMap.size();
    }
    
    public String getSetlist(int groupPosition, int childPosition) {
    	TreeMap<String, String> childMap = parseMap.get(
				keyList.get(groupPosition));
    	return childMap.get(getChild(groupPosition, childPosition));
    }

	@Override
	public int getGroupCount() {
		return groupCount;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return parseMap.get(keyList.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return keyList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		TreeMap<String, String> childMap = parseMap.get(
				keyList.get(groupPosition));
		List<Object> childList = Arrays.asList(childMap.keySet().toArray());
		return childList.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return (long) groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return (long) childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.date_row, parent, false);
            holder = new ViewHolder();
            holder.setSetText((TextView) convertView.findViewById(R.id.SetText));
            if (getGroup(groupPosition) != null) {
                holder.id = (int) getGroupId(groupPosition);
            }
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
            if (getGroup(groupPosition) != null) {
                holder.id = (int) getGroupId(groupPosition);
            }
        }
        holder.getSetText().setTag(groupPosition);
        holder.getSetText().setText((CharSequence) getGroup(groupPosition));
        return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.setlist_row, parent, false);
            holder = new ViewHolder();
            holder.setSetText((TextView) convertView.findViewById(R.id.SetText));
            if (getChild(groupPosition, childPosition) != null) {
                holder.id = (int) getChildId(groupPosition, childPosition);
            }
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
            if (getChild(groupPosition, childPosition) != null) {
                holder.id = (int) getChildId(groupPosition, childPosition);
            }
        }
        holder.getSetText().setTag(childPosition);
        holder.getSetText().setText((CharSequence) getChild(groupPosition,
        		childPosition));
        if (SharedPreferencesSingleton.instance().getInt(
        		ResourcesSingleton.instance().getString(
        				R.string.selected_group_key), -1) == groupPosition &&
			SharedPreferencesSingleton.instance().getInt(
	        		ResourcesSingleton.instance().getString(
	        				R.string.selected_child_key), -1) == childPosition) {
        	convertView.setBackgroundColor(
        			ResourcesSingleton.instance().getColor(color.orange));
        }
        else {
        	convertView.setBackgroundColor(Color.BLACK);
        }
        return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}

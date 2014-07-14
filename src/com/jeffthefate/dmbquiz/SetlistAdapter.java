package com.jeffthefate.dmbquiz;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
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
        groupCount = parseMap.size();
    }
    
    public String getSetlist(int groupPosition, int childPosition) {
    	Entry<String, String> entry = getEntry(groupPosition, childPosition);
    	if (entry != null) {
    		return entry.getValue();
    	}
    	return null;
    }
    
    public String getSetlist(String key) {
    	Entry<String, String> entry = getEntry(key);
    	if (entry != null) {
    		return entry.getValue();
    	}
    	return null;
    }
    
    public String getKey(int groupPosition, int childPosition) {
    	Entry<String, String> entry = getEntry(groupPosition, childPosition);
    	if (entry != null) {
    		return entry.getKey();
    	}
    	return null;
    }
    
    public void setSelected(String key, ExpandableListView setlistListView) {
    	int i = -1;
    	int j = -1;
    	for (Entry<String, TreeMap<String, String>> entry :
				parseMap.entrySet()) {
    		i++;
			for (Entry<String, String> child : entry.getValue().entrySet()) {
				j++;
				if (child.getKey().equals(key)) {
					Log.d(Constants.LOG_TAG, "setSelected: " + i + " : " + j);
					setlistListView.setSelectedChild(i, j, true);
					return;
				}
			}
    	}
    }
    
    public boolean isFirst(String key) {
    	return parseMap.firstEntry().getValue().firstKey().equals(key);
    }
    
    public Entry<String, String> getEntry(int groupPosition,
    		int childPosition) {
    	TreeMap<String, String> childMap = parseMap.get(
				parseMap.keySet().toArray()[groupPosition]);
    	Set<Entry<String, String>> children = childMap.entrySet();
    	Iterator<Entry<String, String>> childrenIter = children.iterator();
    	Entry<String, String> child;
    	int i = 0;
    	while (childrenIter.hasNext()) {
    		child = childrenIter.next();
    		if (i == childPosition) {
    			return child;
    		}
    		i++;
    	}
    	return null;
    }
    
    public Entry<String, String> getEntry(String key) {
    	for (Entry<String, TreeMap<String, String>> entry :
    			parseMap.entrySet()) {
    		for (Entry<String, String> child : entry.getValue().entrySet()) {
    			if (child.getKey().equals(key)) {
    				return child;
    			}
    		}
    	}
    	return null;
    }

	@Override
	public int getGroupCount() {
		return groupCount;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return parseMap.get(parseMap.keySet().toArray()[groupPosition]).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return parseMap.keySet().toArray()[groupPosition];
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		TreeMap<String, String> childMap = parseMap.get(
				parseMap.keySet().toArray()[groupPosition]);
		Object[] array = childMap.keySet().toArray();
		return array[childPosition];
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

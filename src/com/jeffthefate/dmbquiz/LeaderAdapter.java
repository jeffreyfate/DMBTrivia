package com.jeffthefate.dmbquiz;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LeaderAdapter extends ArrayAdapter<String>  {
    
    private Context mContext;
    private String userId;
    private ArrayList<String> rankList;
    private ArrayList<String> userList;
    private ArrayList<String> scoreList;
    private ArrayList<String> userIdList;
    
    protected static class ViewHolder {
        TextView rank;
        TextView text1;
        TextView text2;
    }
    
    public LeaderAdapter(Context context, String userId,
            ArrayList<String> rankList, ArrayList<String> userList,
            ArrayList<String> scoreList, ArrayList<String> userIdList,
            int resource, String[] source, int[] dest) {
        super(context, resource, -1, userIdList);
        mContext = context;
        this.userId = userId;
        this.rankList = rankList;
        this.userList = userList;
        this.scoreList = scoreList;
        this.userIdList = userIdList;
    }
    
    @Override
    public int getCount() {
        if (userIdList != null)
            return userIdList.size();
        return 0;
    }

    @Override
    public String getItem(int position) {
        if (userIdList != null)
            return userIdList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (userIdList != null)
            return position;
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.row_standings, parent, false);
            holder = new ViewHolder();
            holder.rank = (TextView) convertView.findViewById(R.id.rank);
            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.rank.setTag(position);
        holder.text1.setTag(position);
        holder.text2.setTag(position);
        holder.rank.setText(rankList.get(position));
        holder.text1.setText(userList.get(position));
        holder.text2.setText(scoreList.get(position));
        if (userIdList.get(position).equals(userId)) {
        	holder.rank.setTextColor(Color.GREEN);
            holder.text1.setTextColor(Color.GREEN);
            holder.text2.setTextColor(Color.GREEN);
        }
        else {
        	holder.rank.setTextColor(Color.WHITE);
            holder.text1.setTextColor(Color.WHITE);
            holder.text2.setTextColor(Color.WHITE);
        }
        return convertView;
    }

}

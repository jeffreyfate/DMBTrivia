package com.jeffthefate.dmbquiz;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseObject;

public class QuestionAdapter extends BaseAdapter {
    
    private Context mContext;
    private List<ParseObject> mQuestions;
    
    private TextView mEmptyText;
    private ProgressBar mEmptyProgress;
    
    private View mConvertView;
    
    protected static class ViewHolder {
        TextView text1;
        TextView text2;
        int id;
    }
    
    public QuestionAdapter(Context context, List<ParseObject> questions) {
        mContext = context;
        mQuestions = questions;
    }
    
    @Override
    public int getCount() {
        if (mQuestions != null)
            return mQuestions.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mQuestions != null)
            return mQuestions.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.row, parent, false);
            holder = new ViewHolder();
            holder.text1 = (TextView)convertView.findViewById(R.id.RowTextView);
            holder.id = (int) getItemId(position);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
            if (holder.id != (int) getItemId(position)) {
                holder.id = (int) getItemId(position);
            }
        }
        holder.text1.setTag(position);
        holder.text1.setText(mQuestions.get(position).getString("question"));
        return convertView;
    }

}

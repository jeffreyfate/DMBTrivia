package com.jeffthefate.dmbquiz;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.fragment.FragmentQuiz;

public class CategoryAdapter extends ArrayAdapter  {
    
    private Context mContext;
    private ArrayList<String> mCategories;
    private RelativeLayout mSpeedListLayout;
    
    protected static class ViewHolder {
        TextView text1;
        int id;
    }
    
    private OnCategoryChangeListener mCallback;
    
    public interface OnCategoryChangeListener {
        public void setCategory(String category);
    }
    
    public CategoryAdapter(Context context, int resource,
            int textViewResourceId, ArrayList<String> objects,
            RelativeLayout categoryListLayout, FragmentQuiz fragment) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
        mCategories = objects;
        mSpeedListLayout = categoryListLayout;
        mCallback = (OnCategoryChangeListener) fragment;
    }
    
    @Override
    public int getCount() {
        if (mCategories != null)
            return mCategories.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mCategories != null)
            return mCategories.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mCategories != null)
            return position;
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.row_category, parent, false);
            convertView.setBackgroundResource(R.drawable.button);
            holder = new ViewHolder();
            holder.text1 = (TextView) convertView.findViewById(
                    android.R.id.text1);
            holder.id = (int) getItemId(position);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
            if (holder.id != (int) getItemId(position))
                holder.id = (int) getItemId(position);
        }
        holder.text1.setTag(position);
        holder.text1.setText(mCategories.get(position));
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.setCategory(mCategories.get(pos));
            }
        });
        return convertView;
    }
    
    public void setCategoryList(ArrayList<String> categoryList) {
        mCategories = categoryList;
        notifyDataSetChanged();
    }

}

package com.jeffthefate.dmbquiz;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.fragment.FragmentBase;

public class MenuAdapter extends BaseAdapter {
    
    protected Context context;
    protected ArrayList<MenuRow> menuItems;
    protected FragmentBase currFrag;
    
    private TextViewHolder textHolder;
    private CheckViewHolder checkHolder;
    
    private CheckedTextView soundText;
    private CheckedTextView notificationText;
    private CheckedTextView tipText;
    
    private RelativeLayout soundLayout;
    private RelativeLayout notificationLayout;
    private RelativeLayout tipLayout;
    
    private SharedPreferences sharedPrefs;
    
    protected static class TextViewHolder {
        TextView text;
        ImageView image;
    }
    
    protected static class CheckViewHolder {
        ImageView image;
    }
    
    public MenuAdapter(Context context, ArrayList<MenuRow> menuItems,
    		FragmentBase currFrag, SharedPreferences sharedPrefs) {
        this.context = context;
        this.menuItems = menuItems;
        this.currFrag = currFrag;
        this.sharedPrefs = sharedPrefs;
    }
    
    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }
    
    protected String getMenuName(int position) {
    	return ((MenuRow) getItem(position)).getMenuName();
    }
    
    protected int getMenuNumber(int position) {
    	return ((MenuRow) getItem(position)).getMenuNumber();
    }
    
    protected int getMenuImage(int position) {
    	return ((MenuRow) getItem(position)).getMenuImage();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
    	switch(position) {
    	case Constants.MENU_BACKGROUND:
    	case Constants.MENU_STATS:
    	case Constants.MENU_EXIT:
    	case Constants.MENU_LOGOUT:
    	case Constants.MENU_NAME:
    	case Constants.MENU_REPORT:
    	case Constants.MENU_SCREEN:
    	case Constants.MENU_FOLLOW:
    	case Constants.MENU_LIKE:
    		if (convertView == null || convertView.getTag() instanceof CheckViewHolder) {
        		convertView = LayoutInflater.from(context).inflate(
                        R.layout.row_menu_text, parent, false);
                textHolder = new TextViewHolder();
                textHolder.text = (TextView) convertView.findViewById(
                		R.id.TextMenu);
                textHolder.image = (ImageView) convertView.findViewById(
                		R.id.MenuImage);
                convertView.setTag(textHolder);
    		}
    		else
    			textHolder = (TextViewHolder) convertView.getTag();
    		textHolder.text.setTag(position);
    		textHolder.text.setText(getMenuName(position));
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                	switch (getMenuNumber(pos)) {
                	case Constants.MENU_STATS: 
                		currFrag.openStats();
                		break;
                	case Constants.MENU_BACKGROUND:
                		currFrag.switchBackground();
                		break;
                	case Constants.MENU_REPORT:
                		currFrag.report();
                		break;
                	case Constants.MENU_SCREEN:
                		currFrag.shareScreen();
                		break;
                	case Constants.MENU_EXIT:
                		currFrag.exit();
                		break;
                	case Constants.MENU_LOGOUT:
                		currFrag.logOut();
                		break;
                	case Constants.MENU_NAME:
                		currFrag.changeName();
                		break;
                	case Constants.MENU_FOLLOW:
                		currFrag.follow();
                		break;
                	case Constants.MENU_LIKE:
                		currFrag.like();
                		break;
                	default:
                		break;
                	}
                }
            });
            textHolder.image.setTag(position);
            textHolder.image.setImageResource(getMenuImage(position));
    		break;
    	case Constants.MENU_SOUND:
    		if (convertView == null || convertView.getTag() instanceof TextViewHolder) {
        		convertView = LayoutInflater.from(context).inflate(
                        R.layout.row_menu_check, parent, false);
        		checkHolder = new CheckViewHolder();
        		soundLayout = (RelativeLayout) convertView.findViewById(
        				R.id.ListItem);
        		soundText = (CheckedTextView) convertView.findViewById(
                		R.id.CheckMenu);
        		soundText.setChecked(sharedPrefs.getBoolean(
        				context.getString(R.string.sound_key), true));
        		checkHolder.image = (ImageView) convertView.findViewById(
                		R.id.MenuImage);
                convertView.setTag(checkHolder);
    		}
    		else
    			checkHolder = (CheckViewHolder) convertView.getTag();
    		if (soundText == null)
    			soundText = (CheckedTextView) convertView.findViewById(
                		R.id.CheckMenu);
    		if (soundLayout == null)
    			soundLayout = (RelativeLayout) convertView.findViewById(
        				R.id.ListItem);
    		soundText.setText(getMenuName(position));
    		soundText.setClickable(false);
    		soundLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                	soundText.toggle();
                	currFrag.toggleSounds();
                }
            });
            checkHolder.image.setTag(position);
            checkHolder.image.setImageResource(getMenuImage(position));
            break;
    	case Constants.MENU_NOTIFICATIONS:
    		if (convertView == null || convertView.getTag() instanceof TextViewHolder) {
        		convertView = LayoutInflater.from(context).inflate(
                        R.layout.row_menu_check, parent, false);
        		checkHolder = new CheckViewHolder();
        		notificationLayout = (RelativeLayout) convertView.findViewById(
        				R.id.ListItem);
        		notificationText = (CheckedTextView) convertView.findViewById(
                		R.id.CheckMenu);
        		notificationText.setChecked(sharedPrefs.getBoolean(
        				context.getString(R.string.notification_key), true));
        		checkHolder.image = (ImageView) convertView.findViewById(
                		R.id.MenuImage);
                convertView.setTag(checkHolder);
    		}
    		else
    			checkHolder = (CheckViewHolder) convertView.getTag();
    		if (notificationText == null)
    			notificationText = (CheckedTextView) convertView.findViewById(
                		R.id.CheckMenu);
    		if (notificationLayout == null)
    			notificationLayout = (RelativeLayout) convertView.findViewById(
        				R.id.ListItem);
    		notificationText.setText(getMenuName(position));
    		notificationText.setClickable(false);
    		notificationLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                	notificationText.toggle();
                	currFrag.toggleNotifications();
                }
            });
    		checkHolder.image.setTag(position);
    		checkHolder.image.setImageResource(getMenuImage(position));
            break;
    	case Constants.MENU_QUICKTIPS:
    		if (convertView == null || convertView.getTag() instanceof TextViewHolder) {
        		convertView = LayoutInflater.from(context).inflate(
                        R.layout.row_menu_check, parent, false);
        		checkHolder = new CheckViewHolder();
        		tipLayout = (RelativeLayout) convertView.findViewById(
        				R.id.ListItem);
        		tipText = (CheckedTextView) convertView.findViewById(
                		R.id.CheckMenu);
        		tipText.setChecked(sharedPrefs.getBoolean(
        				context.getString(R.string.quicktip_key), true));
        		checkHolder.image = (ImageView) convertView.findViewById(
                		R.id.MenuImage);
                convertView.setTag(checkHolder);
    		}
    		else
    			checkHolder = (CheckViewHolder) convertView.getTag();
    		if (tipText == null)
    			tipText = (CheckedTextView) convertView.findViewById(
                		R.id.CheckMenu);
    		if (tipLayout == null)
    			tipLayout = (RelativeLayout) convertView.findViewById(
        				R.id.ListItem);
    		tipText.setText(getMenuName(position));
    		tipText.setClickable(false);
    		tipLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                	tipText.toggle();
                	currFrag.toggleTips();
                }
            });
    		checkHolder.image.setTag(position);
    		checkHolder.image.setImageResource(getMenuImage(position));
    		break;
    	}
        return convertView;
    }

}

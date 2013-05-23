package com.jeffthefate.dmbquiz.fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.DatabaseHelper;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.activity.ActivityMain;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class FragmentPager extends FragmentBase {
    
    private ActivityMain activity;
    private ViewPager viewPager;
    private FragmentBase fragment;
    
    private String userId = null;
    private boolean loggedIn = false;
    private boolean inStats = false;
    
    public FragmentPager() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	this.activity = (ActivityMain) activity;
    	if (mCallback != null)
    		mCallback.setHomeAsUp(true);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = DatabaseHelperSingleton.instance().getCurrUser();
        if (userId != null) {
            loggedIn = DatabaseHelperSingleton.instance().getUserIntValue(
                    DatabaseHelper.COL_LOGGED_IN, userId) == 1 ? true : false;
            inStats = DatabaseHelperSingleton.instance().getUserIntValue(
                    DatabaseHelper.COL_IN_STATS, userId) == 1 ? true : false;
        }
        fragment = getFragmentForPager();
        viewPager = new ViewPager(activity);
        viewPager.setId("VP".hashCode());
        viewPager.setAdapter(new PagerAdapter(getChildFragmentManager(),
                fragment));

        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (mCallback != null) {
                    switch (position) {
                    case 0:
                        mCallback.slidingMenu().setTouchModeAbove(
                                SlidingMenu.TOUCHMODE_FULLSCREEN);
                        mCallback.setInSetlist(false);
                        break;
                    case 1:
                        mCallback.slidingMenu().setTouchModeAbove(
                                SlidingMenu.TOUCHMODE_NONE);
                        mCallback.setInSetlist(true);
                        break;
                    default:
                        mCallback.slidingMenu().setTouchModeAbove(
                                SlidingMenu.TOUCHMODE_MARGIN);
                        break;
                    }
                }
            }

        });
        //sInterpolator = new LinearInterpolator();
        sInterpolator = new AccelerateInterpolator();
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true); 
            FixedSpeedScroller scroller = new FixedSpeedScroller(
                    viewPager.getContext(), sInterpolator);
            scroller.setFixedDuration(200);
            mScroller.set(viewPager, scroller);
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }
    
    private AccelerateInterpolator sInterpolator;
    
    @Override
    public void onResume() {
        super.onResume();
        if (viewPager.getAdapter().getCount() > 1 && mCallback != null) {
            if (mCallback.getGoToSetlist() || mCallback.getInSetlist()) {
                viewPager.setCurrentItem(1);
                mCallback.slidingMenu().setTouchModeAbove(
                        SlidingMenu.TOUCHMODE_NONE);
                mCallback.setInSetlist(true);
            }
            else {
                viewPager.setCurrentItem(0);
                mCallback.slidingMenu().setTouchModeAbove(
                        SlidingMenu.TOUCHMODE_FULLSCREEN);
                mCallback.setInSetlist(false);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	return viewPager;
    }
    
    public class PagerAdapter extends FragmentPagerAdapter {

        private ArrayList<FragmentBase> mFragments;

        public PagerAdapter(FragmentManager fm, FragmentBase frag) {
            super(fm);
            mFragments = new ArrayList<FragmentBase>();
            mFragments.add(frag);
            mFragments.add(new FragmentSetlist());
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
        
        public ArrayList<FragmentBase> getFragmentList() {
            return mFragments;
        }

    }
    
    public void removeChildren(FragmentTransaction ft) {
        ft.setCustomAnimations(R.anim.zero_anim, R.anim.zero_anim,
                R.anim.zero_anim, R.anim.zero_anim);
        for (FragmentBase fragment :
                ((PagerAdapter)viewPager.getAdapter()).getFragmentList())
            ft.remove(fragment);
    }
    
    @Override
    public void resumeQuestion() {
        ((FragmentQuiz)((PagerAdapter) viewPager.getAdapter()).getFragmentList()
                .get(0)).resumeQuestion();
    }
    /*
    @Override
    public void setBackground(Bitmap background) {
        ((FragmentBase)((PagerAdapter) viewPager.getAdapter()).getFragmentList()
                .get(0)).setBackground(background);
    }
    */
    @Override
    public void setBackground(Bitmap background) {
        ((FragmentBase)((PagerAdapter) viewPager.getAdapter()).getFragmentList()
                .get(0)).setBackground(background);
    }
    
    @Override
    public Drawable getBackground() {
        return ((FragmentBase)((PagerAdapter) viewPager.getAdapter())
                .getFragmentList().get(0)).getBackground();
    }
    
    public FragmentBase getFragmentForPager() {
        if (!loggedIn)
            return new FragmentSplash();
        else {
            if (inStats)
                return new FragmentLeaders();
            else
                return new FragmentQuiz();
        }
    }
    
    @Override
    public int getPage() {
        return viewPager.getCurrentItem();
    }
    
    @Override
    public void setPage(int page) {
        viewPager.setCurrentItem(page, true);
    }
    
    public class FixedSpeedScroller extends Scroller {

        private int mDuration = 5000;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator,
                boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        public void setFixedDuration(int duration) {
            mDuration = duration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }
}
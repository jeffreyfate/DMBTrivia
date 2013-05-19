package com.jeffthefate.dmbquiz;

/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Helper class for showing cheat sheets (tooltips) for icon-only UI elements on long-press. This is
 * already default platform behavior for icon-only {@link android.app.ActionBar} items and tabs.
 * This class provides this behavior for any other such UI element.
 *
 * <p>Based on the original action bar implementation in <a href="https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/com/android/internal/view/menu/ActionMenuItemView.java">
 * ActionMenuItemView.java</a>.
 */
public class CheatSheetMenu {

    /**
     * Sets up a cheat sheet (tooltip) for the given view by setting its {@link
     * android.view.View.OnLongClickListener}. When the view is long-pressed, a {@link Toast} with
     * the given text will be shown either above (default) or below the view (if there isn't room
     * above it).
     *
     * @param view      The view to add a cheat sheet for.
     * @param textResId The string resource containing the text to show on long-press.
     */
    public static void setup(View view, final int textResId, int width,
    		int height, int location) {
    	showCheatSheet(view, ApplicationEx.getApp().getString(textResId),
    			width, height, location);
    }

    /**
     * Sets up a cheat sheet (tooltip) for the given view by setting its {@link
     * android.view.View.OnLongClickListener}. When the view is long-pressed, a {@link Toast} with
     * the given text will be shown either above (default) or below the view (if there isn't room
     * above it).
     *
     * @param view The view to add a cheat sheet for.
     * @param text The text to show on long-press.
     */
    public static void setup(View view, final CharSequence text, int width,
    		int height, int location) {
    	showCheatSheet(view, text, width, height, location);
    }

    /**
     * Internal helper method to show the cheat sheet toast.
     */
    private static boolean showCheatSheet(View view, CharSequence text,
    		int width, int height, int location) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        TextView toolTipText = (TextView) view.findViewById(R.id.ToolTipText);
        toolTipText.setText(text);
        Toast cheatSheet = new Toast(ApplicationEx.getApp());
        cheatSheet.setDuration(Toast.LENGTH_LONG);
        switch (location) {
        case Constants.QUICK_TIP_TOP:
        	cheatSheet.setGravity(Gravity.CENTER, 0, height/-5);
        	break;
        case Constants.QUICK_TIP_CENTER:
        	cheatSheet.setGravity(Gravity.CENTER, 0, 0);
        	break;
        case Constants.QUICK_TIP_BOTTOM:
        	cheatSheet.setGravity(Gravity.CENTER, 0, height/3);
        	break;
        case Constants.QUICK_TIP_LEFT:
        	cheatSheet.setGravity(Gravity.LEFT, 0, 0);
        	break;
        case Constants.QUICK_TIP_RIGHT:
        	cheatSheet.setGravity(Gravity.RIGHT, 0, 0);
        	break;
        case Constants.QUICK_TIP_LEFT | Constants.QUICK_TIP_TOP:
        	cheatSheet.setGravity(Gravity.LEFT, 0, height/-5);
        	break;
        case Constants.QUICK_TIP_RIGHT | Constants.QUICK_TIP_TOP:
        	cheatSheet.setGravity(Gravity.RIGHT, 0, height/-5);
        	break;
        case Constants.QUICK_TIP_LEFT | Constants.QUICK_TIP_BOTTOM:
        	cheatSheet.setGravity(Gravity.LEFT, 0, height/3);
        	break;
        case Constants.QUICK_TIP_RIGHT | Constants.QUICK_TIP_BOTTOM:
        	cheatSheet.setGravity(Gravity.RIGHT, 0, height/3);
        	break;
        default:
        	cheatSheet.setGravity(Gravity.CENTER, 0, 0);
        	break;
        }
        cheatSheet.setView(view);
        cheatSheet.show();
        return true;
    }
}
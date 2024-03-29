/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/

package com.applicake.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

import com.applicake.android.ui.R;

// TODO usage of NinePath
/**
 * Class representing simple tag widget
 * 
 * @author Lukasz Wisniewski
 */
public class TagButton extends Button {

    public static final String TAG = "TagButton";

    private String mText;

    // animation support
    boolean newPosition = true;
    int old_x;
    int old_y;

    public TagButton(Context context) {
        super(context);
        init(context, null);
    }

    public TagButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TagButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Sharable code between constructors
     * 
     * @param attrs
     * @param context
     */
    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagTheme);
        int background = a.getResourceId(R.styleable.TagTheme_tagDrawable, R.drawable.tag_default);
        setBackgroundResource(background);

        setTextColor(0xFFFFFFFF);

        mText = "";

        this.setFocusable(true);
        this.setOnFocusChangeListener(new OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                System.out.print("Focus changed!");
                if (v == TagButton.this && hasFocus) {
                    System.out.print("Tag got focused!");
                }
            }

        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.i(TAG, mText + ": dx = " + left + " dy = " + top);
        if (newPosition) {
            old_x = left;
            old_y = top;
            newPosition = false;
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    public void setText(String text) {
        mText = text;
        super.setText(mText);
    }

    /**
     * Generates translate animation on position change, must be executed from
     * parent within onLayout method
     * 
     * @param durationMillis
     * @return
     */
    public Animation createTranslateAnimation(long durationMillis) {
        if (old_x == getLeft() && old_y == getTop()) {
            return null;
        }

        int dx = getLeft() - old_x;
        int dy = getTop() - old_y;
        Animation a = new TranslateAnimation(-dx, 0, -dy, 0);
        a.setFillAfter(true);
        a.setDuration(durationMillis);

        old_x = getLeft();
        old_y = getTop();

        return a;
    }

}

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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.applicake.android.ui.R;

/**
 * Layout/container for TagButtons
 * 
 * @author Lukasz Wisniewski
 */
public class TagLayout extends ViewGroup implements OnTagChangeListener {

  public static final String TAG = "TagLayout";

  OnTagChangeListener mListener;

  Map<String, TagButton> mTagButtons;
  TextView mAreaHint;

  /**
   * Padding between buttons
   */
  int mPadding;

  /**
   * Animation turned on/off
   */
  boolean mAnimationEnabled;

  /**
   * Indicator whether an animation is ongoing or not
   */
  boolean mAnimating;

  /**
   * Container for TagButton animations
   */
  ArrayList<Animation> mAnimations;

  /**
   * Area hint resource
   */
  private int mAreaTextId = 0;

  public TagLayout(Context context) {
    super(context);
    init(context);
  }

  public TagLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagTheme);

    Drawable background = a.getDrawable(R.styleable.TagTheme_tagsBackground);
    if (background != null) {
      setBackgroundDrawable(background);
    }

    a.recycle();
  }

  /**
   * Sharable code between constructors
   * 
   * @param context
   */
  private void init(Context context) {
    mTagButtons = new TreeMap<String, TagButton>();
    mPadding = 5; // TODO get from xml layout
    mAnimationEnabled = false;
    mAnimating = false;

    mAnimations = new ArrayList<Animation>();
    // this.setFocusable(true);

    // Creating area hint
    mAreaHint = new TextView(context);
    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);
    mAreaHint.setVisibility(View.GONE);
    mAreaHint.setTextColor(0xff666666);
    mAreaHint.setGravity(Gravity.CENTER);
    mAreaHint.setTextSize(16);
    mAreaHint.setTypeface(mAreaHint.getTypeface(), Typeface.BOLD);
    this.addView(mAreaHint, params);
  }

  /**
   * Adds tag by creating button inside TagLayout
   * 
   * @param tag
   */
  public void addTag(final String tag) {
    final TagButton tagButton = new TagButton(this.getContext());
    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);
    tagButton.setText(tag);

    tagButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        removeTag(tag);
        if (mListener != null) {
          mListener.onTagRemoved(tag);
        }
      }

    });
    tagButton.setVisibility(View.INVISIBLE);

    mTagButtons.put(tag, tagButton);
    this.addView(tagButton, params);

    if (mAnimationEnabled) {
      Animation a = AnimationUtils.loadAnimation(this.getContext(), R.anim.tag_fadein);
      a.setAnimationListener(new AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
          tagButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

      });
      tagButton.startAnimation(a);
    }
  }

  /**
   * Removes TagButton from TagLayout
   * 
   * @param tag
   */
  private void removeTag(final String tag) {
    if (mAnimationEnabled) {
      TagButton tb = mTagButtons.get(tag);
      Animation a = AnimationUtils.loadAnimation(this.getContext(), R.anim.tag_fadeout);
      a.setAnimationListener(new AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
          mAnimating = false;
          TagLayout.this.requestLayout();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

      });
      mAnimating = true;
      tb.startAnimation(a);
    }

    removeView(mTagButtons.remove(tag));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    Log.i(TAG, "onMeasue()");

    int selfw = getMeasuredWidth();
    int selfh = getMeasuredHeight();

    int count = getChildCount();

    // Area hint (on when count equals 1)
    if (count == 1 && mAreaTextId > 0) {
      mAreaHint.setVisibility(View.VISIBLE);
    } else {
      mAreaHint.setVisibility(View.GONE);
    }

    for (int i = 0; i < count; i++) {
      View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        // LayoutParams lp = (LayoutParams) child.getLayoutParams();
        child.measure(MeasureSpec.makeMeasureSpec(selfw, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(selfh, MeasureSpec.AT_MOST));
      }
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    Log.i(TAG, "onLayout()");
    if (mAnimating) {
      return;
    }

    int selfw = getMeasuredWidth();
    int selfh = getMeasuredHeight();

    int x = mPadding;
    int y = mPadding;

    for (Map.Entry<String, TagButton> entry : mTagButtons.entrySet()) {
      TagButton child = entry.getValue();

      int cw = child.getMeasuredWidth();
      int ch = child.getMeasuredHeight();
      Log.i(TAG, "child(" + entry.getKey() + ") size - " + cw + "," + ch);

      // tag doesn't fit the row, move it to next one
      if (x + cw > selfw) {
        x = mPadding;
        y = y + ch + mPadding;
      }

      child.layout(x, y, x + cw, y + ch);

      if (mAnimationEnabled) {
        Animation a = child.createTranslateAnimation(400);
        if (a != null) {
          child.startAnimation(a);
        }
      }

      x = x + cw + mPadding;
    }

    // positioning AreaHint
    if (mAreaHint.getVisibility() == View.VISIBLE) {
      int cw = mAreaHint.getMeasuredWidth();
      int ch = mAreaHint.getMeasuredHeight();
      mAreaHint.layout((selfw - cw) / 2, (selfh - ch) / 2, (selfw + cw) / 2,
          (selfh + ch) / 2);
    }

  }

  /**
   * Set a listener that will receive a callback when a tag is removed from this
   * view. You don't need to implement onTagAdded here, as it won't be invoked
   * here.
   * 
   * @param listener
   */
  public void setTagRemovingListener(OnTagChangeListener listener) {
    this.mListener = listener;
  }

  /**
   * Enables/disables fancy animations
   * 
   * @param value
   */
  public void setAnimationsEnabled(boolean value) {
    this.mAnimationEnabled = value;
  }

  /**
   * Sets informative text which is displayed in the middle of TagLayout area
   * before any TagButton has been added
   * 
   * @param resid
   *          resource Id
   */
  public void setAreaHint(int resid) {
    mAreaTextId = resid;
    mAreaHint.setText(resid);
  }

  @Override
  public void onTagAdded(String tag) {
  }

  @Override
  public void onTagRemoved(String tag) {
    if (mTagButtons.containsKey(tag)) {
      removeTag(tag);
    }
  }

}

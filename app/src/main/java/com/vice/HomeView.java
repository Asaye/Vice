
/******************************************************/
/*****    Author: Asaye C. Dilbo               ********/
/*****    Email: asayechemeda@yahoo.com        ********/
/*****    Github: https://github.com/Asaye     ********/
/******************************************************/

package com.vice;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class HomeView extends LinearLayout {

        private ViewDragHelper mDragHelper;
        public static int ID = 0;
        public static int SCROLL_ID = 0;
        public static int LIMIT = 0;
        private int HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;

        public class DragHelperCallback extends ViewDragHelper.Callback {

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                LinearLayout layout = findViewById(ID);
                return child == layout;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (top > HEIGHT - 100) {
                    return HEIGHT - 100;
                } else if (top < 50 || top < (HEIGHT - LIMIT - 150)) {
                    return Math.max(50, HEIGHT - LIMIT - 150);
                } else {
                    return top;
                }
            }

            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                ScrollView scroll = findViewById(SCROLL_ID);
                if (scroll != null && scroll.getVisibility() == View.GONE) {
                    scroll.setVisibility(View.VISIBLE);
                }
            }
        }

        public HomeView(Context context) {
            this(context, null);
            mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        }

        public HomeView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
            mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        }

        public HomeView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return mDragHelper.shouldInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            mDragHelper.processTouchEvent(ev);
            return true;
        }
}
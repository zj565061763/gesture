package com.fanwe.gesture.test;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class ViewDragHelperFrameLayout extends FrameLayout
{
    public ViewDragHelperFrameLayout(Context context)
    {
        super(context);
        init();
    }

    public ViewDragHelperFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public ViewDragHelperFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private static final String TAG = ViewDragHelperFrameLayout.class.getSimpleName();

    private ViewDragHelper mViewDragHelper;

    private void init()
    {
        mViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx)
            {
                final int min = getPaddingLeft();
                final int max = getWidth() - getPaddingRight() - child.getWidth();
                return Math.min(Math.max(left, min), max);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy)
            {
                final int min = getPaddingTop();
                final int max = getHeight() - getPaddingBottom() - child.getHeight();
                return Math.min(Math.max(top, min), max);
            }

            @Override
            public void onViewCaptured(View capturedChild, int activePointerId)
            {
                super.onViewCaptured(capturedChild, activePointerId);
                Log.i(TAG, "onViewCaptured");
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel)
            {
                super.onViewReleased(releasedChild, xvel, yvel);
                Log.i(TAG, "onViewReleased:" + xvel + " " + yvel);
                mViewDragHelper.settleCapturedViewAt(getPaddingLeft(), getPaddingTop());
                invalidate();
            }

            @Override
            public void onViewDragStateChanged(int state)
            {
                super.onViewDragStateChanged(state);
                Log.e(TAG, "onViewDragStateChanged:" + state);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mViewDragHelper.processTouchEvent(event);
        return mViewDragHelper.getCapturedView() != null;
    }

    @Override
    public void computeScroll()
    {
        super.computeScroll();
        if (mViewDragHelper.continueSettling(false))
        {
            invalidate();
        }
    }
}

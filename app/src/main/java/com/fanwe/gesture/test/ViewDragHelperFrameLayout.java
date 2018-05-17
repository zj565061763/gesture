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
            public void onViewDragStateChanged(int state)
            {
                super.onViewDragStateChanged(state);
                Log.i(TAG, "onViewDragStateChanged:" + state);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        mViewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mViewDragHelper.processTouchEvent(event);
        return true;
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

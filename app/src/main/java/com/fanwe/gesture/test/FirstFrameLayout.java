package com.fanwe.gesture.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class FirstFrameLayout extends FrameLayout
{
    public FirstFrameLayout(Context context)
    {
        super(context);
    }

    public FirstFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FirstFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        boolean result = super.onInterceptTouchEvent(ev);
        EventLogger.e("First onInterceptTouchEvent " + result, ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean result = super.onTouchEvent(event);
        EventLogger.i("First onTouchEvent " + result, event);
        return result;
    }
}

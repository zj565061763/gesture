package com.sd.gesture.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class SecondFrameLayout extends FrameLayout
{
    public SecondFrameLayout(Context context)
    {
        super(context);
    }

    public SecondFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SecondFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        boolean result = super.onInterceptTouchEvent(ev);
        EventLogger.e("Second onInterceptTouchEvent " + result, ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean result = super.onTouchEvent(event);
        EventLogger.i("Second onTouchEvent " + result, event);
        return result;
    }
}

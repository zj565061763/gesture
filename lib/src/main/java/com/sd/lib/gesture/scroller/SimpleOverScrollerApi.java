package com.sd.lib.gesture.scroller;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

public class SimpleOverScrollerApi implements FScroller.ScrollerApi
{
    private final OverScroller mScroller;

    public SimpleOverScrollerApi(Context context)
    {
        mScroller = new OverScroller(context);
    }

    public SimpleOverScrollerApi(Context context, Interpolator interpolator)
    {
        mScroller = new OverScroller(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration)
    {
        mScroller.startScroll(startX, startY, dx, dy, duration);
    }

    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY)
    {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    @Override
    public boolean computeScrollOffset()
    {
        return mScroller.computeScrollOffset();
    }

    @Override
    public void abortAnimation()
    {
        mScroller.abortAnimation();
    }

    @Override
    public boolean isFinished()
    {
        return mScroller.isFinished();
    }

    @Override
    public int getCurrX()
    {
        return mScroller.getCurrX();
    }

    @Override
    public int getCurrY()
    {
        return mScroller.getCurrY();
    }
}

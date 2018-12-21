package com.sd.lib.gesture.scroller;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class SimpleScrollerApi implements FScroller.ScrollerApi
{
    private final Scroller mScroller;

    public SimpleScrollerApi(Context context)
    {
        mScroller = new Scroller(context);
    }

    public SimpleScrollerApi(Context context, Interpolator interpolator)
    {
        mScroller = new Scroller(context, interpolator);
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

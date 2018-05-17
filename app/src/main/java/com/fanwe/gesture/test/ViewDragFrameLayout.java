package com.fanwe.gesture.test;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.fanwe.lib.gesture.FGestureManager;
import com.fanwe.lib.gesture.FScroller;
import com.fanwe.lib.gesture.FTouchHelper;

public class ViewDragFrameLayout extends FrameLayout
{
    public ViewDragFrameLayout(Context context)
    {
        super(context);
    }

    public ViewDragFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ViewDragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    private static final String TAG = ViewDragFrameLayout.class.getSimpleName();

    private FGestureManager mGestureManager;
    private FScroller mScroller;

    private View mChild;

    private FScroller getScroller()
    {
        if (mScroller == null)
        {
            mScroller = new FScroller(new Scroller(getContext()));
            mScroller.setCallback(new FScroller.Callback()
            {
                @Override
                public void onScrollStateChanged(boolean isFinished)
                {
                    Log.e(TAG, "onScrollStateChanged isFinished:" + isFinished);

                    if (isFinished)
                    {
                        mChild = null;
                    }
                }

                @Override
                public void onScroll(int currX, int currY, int lastX, int lastY)
                {
                    ViewCompat.offsetLeftAndRight(mChild, currX - lastX);
                    ViewCompat.offsetTopAndBottom(mChild, currY - lastY);

                    Log.i(TAG, "onScroll:" + mChild.getLeft() + "," + mChild.getTop());
                }
            });
        }
        return mScroller;
    }

    private FGestureManager getGestureManager()
    {
        if (mGestureManager == null)
        {
            mGestureManager = new FGestureManager(new FGestureManager.Callback()
            {
                @Override
                public boolean onEventActionDown(MotionEvent event)
                {
                    mChild = FTouchHelper.findTopChildUnder(ViewDragFrameLayout.this, (int) event.getX(), (int) event.getY());
                    return mChild != null;
                }

                @Override
                public boolean shouldConsumeEvent(MotionEvent event)
                {
                    return mChild != null;
                }

                @Override
                public boolean onEventConsume(MotionEvent event)
                {
                    final int dx = (int) getGestureManager().getTouchHelper().getDeltaXFrom(FTouchHelper.EVENT_LAST);
                    final int dy = (int) getGestureManager().getTouchHelper().getDeltaYFrom(FTouchHelper.EVENT_LAST);

                    ViewCompat.offsetLeftAndRight(mChild, dx);
                    ViewCompat.offsetTopAndBottom(mChild, dy);

                    return true;
                }

                @Override
                public void onEventFinish(MotionEvent event, boolean hasConsumeEvent, VelocityTracker velocityTracker)
                {
                    if (hasConsumeEvent)
                    {
                        doScroll();
                    }
                }
            });
        }
        return mGestureManager;
    }

    private int getLeftAlignParentLeft(View view)
    {
        return getPaddingLeft();
    }

    private int getLeftAlignParentRight(View view)
    {
        return getWidth() - getPaddingRight() - view.getWidth();
    }

    private int getLeftAlignParentCenter(View view)
    {
        return (getLeftAlignParentRight(view) + getLeftAlignParentLeft(view)) / 2;
    }

    private void doScroll()
    {
        final int startX = mChild.getLeft();

        int endX = getLeftAlignParentLeft(mChild);
        if (startX >= getLeftAlignParentCenter(mChild))
        {
            endX = getLeftAlignParentRight(mChild);
        }

        final boolean scroll = getScroller().scrollToX(startX, endX, -1);
        if (scroll)
        {
            invalidate();
        }
    }

    private void doFling(VelocityTracker velocityTracker)
    {
        velocityTracker.computeCurrentVelocity(1000);

        final int startX = mChild.getLeft();
        final int velocityX = (int) velocityTracker.getXVelocity();
        final int minX = getLeftAlignParentLeft(mChild);
        final int maxX = getLeftAlignParentRight(mChild);

        final boolean fling = getScroller().flingX(startX, velocityX, minX, maxX);
        if (fling)
        {
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return getGestureManager().onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return getGestureManager().onTouchEvent(event);
    }

    @Override
    public void computeScroll()
    {
        super.computeScroll();
        if (getScroller().computeScrollOffset())
        {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        getScroller().setMaxScrollDistance(getHeight());
    }
}

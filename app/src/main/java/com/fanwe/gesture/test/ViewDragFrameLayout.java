package com.fanwe.gesture.test;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
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
                    if (isFinished)
                    {
                        mChild = null;
                    }
                }

                @Override
                public void onScroll(int dx, int dy)
                {
                    ViewCompat.offsetLeftAndRight(mChild, dx);
                    ViewCompat.offsetTopAndBottom(mChild, dy);
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
                    final View child = FTouchHelper.findTopChildUnder(ViewDragFrameLayout.this, (int) event.getX(), (int) event.getY());
                    if (child != null) mChild = child;
                    return child != null;
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
                    if (mChild != null)
                    {
                        final int startX = mChild.getLeft();
                        final int startY = mChild.getTop();
                        final int endX = getPaddingLeft();
                        final int endY = getPaddingTop();

                        final boolean isScroll = getScroller().scrollTo(startX, startY, endX, endY, -1);
                        if (isScroll)
                        {
                            invalidate();
                        }
                    }
                }
            });
        }
        return mGestureManager;
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

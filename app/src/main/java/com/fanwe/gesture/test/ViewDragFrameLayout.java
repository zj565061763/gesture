package com.fanwe.gesture.test;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.fanwe.lib.gesture.FGestureManager;
import com.fanwe.lib.gesture.FScroller;
import com.fanwe.lib.gesture.FTouchHelper;

import java.util.List;

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

    private void setChild(View child)
    {
        if (mChild != child)
        {
            mChild = child;
            Log.e(TAG, "setChild:" + child);
        }
    }

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
                        setChild(null);
                    }
                }

                @Override
                public void onScroll(int currX, int currY, int lastX, int lastY)
                {
                    final int dx = currX - lastX;
                    final int dy = currY - lastY;

                    offsetLeftAndRightLegal(mChild, dx);
                    offsetTopAndBottomLegal(mChild, dy);

                    Log.i(TAG, "onScroll:" + dx + "," + dy);
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
                public boolean shouldInterceptEvent(MotionEvent event)
                {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            final View child = findMaxTopChild(event);
                            setChild(child);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (mChild != null)
                            {
                                final int dx = (int) getGestureManager().getTouchHelper().getDeltaXFrom(FTouchHelper.EVENT_DOWN);
                                final int dy = (int) getGestureManager().getTouchHelper().getDeltaYFrom(FTouchHelper.EVENT_DOWN);
                                final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

                                if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop)
                                {
                                    return true;
                                }
                            }
                            break;
                    }
                    return false;
                }

                @Override
                public boolean onEventActionDown(MotionEvent event)
                {
                    if (mChild == null)
                    {
                        final View child = findMaxTopChild(event);
                        setChild(child);
                    }
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

                    offsetLeftAndRightLegal(mChild, dx);
                    offsetTopAndBottomLegal(mChild, dy);

                    return true;
                }

                @Override
                public void onEventFinish(MotionEvent event, boolean hasConsumeEvent, VelocityTracker velocityTracker)
                {
                    if (hasConsumeEvent)
                    {
                        doScroll();
                    } else
                    {
                        setChild(null);
                    }
                }
            });
        }
        return mGestureManager;
    }

    private View findMaxTopChild(MotionEvent event)
    {
        final List<View> list = FTouchHelper.findChildrenUnder(ViewDragFrameLayout.this, (int) event.getX(), (int) event.getY());

        View maxZ = null;
        for (View item : list)
        {
            if (maxZ == null)
            {
                maxZ = item;
            } else
            {
                if (ViewCompat.getZ(item) > ViewCompat.getZ(maxZ))
                {
                    maxZ = item;
                }
            }
        }
        return maxZ;
    }

    private void offsetLeftAndRightLegal(View view, int delta)
    {
        final int min = FTouchHelper.getLeftAlignParentLeft(this, mChild, true);
        final int max = FTouchHelper.getLeftAlignParentRight(this, mChild, true);

        delta = FTouchHelper.getLegalDelta(view.getLeft(), min, max, delta);
        ViewCompat.offsetLeftAndRight(view, delta);
    }

    private void offsetTopAndBottomLegal(View view, int delta)
    {
        final int min = FTouchHelper.getTopAlignParentTop(this, mChild, true);
        final int max = FTouchHelper.getTopAlignParentBottom(this, mChild, true);

        delta = FTouchHelper.getLegalDelta(view.getTop(), min, max, delta);
        ViewCompat.offsetTopAndBottom(view, delta);
    }

    private void doScroll()
    {
        final int startX = mChild.getLeft();

        final int alignLeft = FTouchHelper.getLeftAlignParentLeft(this, mChild, true);
        final int alignRight = FTouchHelper.getLeftAlignParentRight(this, mChild, true);

        final int endX = startX < (alignLeft + alignRight) / 2 ? alignLeft : alignRight;

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
        final int startY = mChild.getTop();

        final int velocityX = (int) velocityTracker.getXVelocity();
        final int velocityY = (int) velocityTracker.getYVelocity();

        final int minX = FTouchHelper.getLeftAlignParentLeft(this, mChild, true);
        final int maxX = FTouchHelper.getLeftAlignParentRight(this, mChild, true);

        final int minY = FTouchHelper.getTopAlignParentTop(this, mChild, true);
        final int maxY = FTouchHelper.getTopAlignParentBottom(this, mChild, true);

        final boolean fling = getScroller().fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
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

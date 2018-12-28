package com.sd.gesture.test;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.sd.lib.gesture.FGestureManager;
import com.sd.lib.gesture.FTouchHelper;

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
    private View mChild;

    private void setChild(View child)
    {
        if (getGestureManager().getState() == FGestureManager.State.Idle)
        {
            if (mChild != child)
            {
                mChild = child;
                Log.e(TAG, "setChild:" + child);
            }
        }
    }

    private FGestureManager getGestureManager()
    {
        if (mGestureManager == null)
        {
            mGestureManager = new FGestureManager(this, new FGestureManager.Callback()
            {
                private View mDownChild = null;

                @Override
                public boolean shouldInterceptEvent(MotionEvent event)
                {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            mDownChild = findMaxTopChild(event);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (mDownChild != null && canPull())
                            {
                                setChild(mDownChild);
                                return true;
                            }
                            break;
                    }
                    return false;
                }

                @Override
                public boolean onEventActionDown(MotionEvent event)
                {
                    setChild(findMaxTopChild(event));
                    return mChild != null;
                }

                @Override
                public boolean shouldConsumeEvent(MotionEvent event)
                {
                    return mChild != null;
                }

                @Override
                public void onEventConsume(MotionEvent event)
                {
                    final int dx = (int) getGestureManager().getTouchHelper().getDeltaX();
                    final int dy = (int) getGestureManager().getTouchHelper().getDeltaY();

                    offsetLeftAndRightLegal(mChild, dx);
                    offsetTopAndBottomLegal(mChild, dy);
                }

                @Override
                public void onEventFinish(VelocityTracker velocityTracker, MotionEvent event)
                {
                    mDownChild = null;

                    if (mGestureManager.getLifecycleInfo().hasConsumeEvent())
                    {
                        doScroll();
                    }
                }

                @Override
                public void onStateChanged(FGestureManager.State oldState, FGestureManager.State newState)
                {
                    Log.i(TAG, "onStateChanged:" + newState);

                    if (newState == FGestureManager.State.Consume)
                    {

                    } else if (newState == FGestureManager.State.Fling)
                    {
                        ViewCompat.postInvalidateOnAnimation(ViewDragFrameLayout.this);
                    } else if (newState == FGestureManager.State.Idle)
                    {
                        setChild(null);
                    }
                }

                @Override
                public void onScrollerCompute(int lastX, int lastY, int currX, int currY)
                {
                    final int dx = currX - lastX;
                    final int dy = currY - lastY;

                    offsetLeftAndRightLegal(mChild, dx);
                    offsetTopAndBottomLegal(mChild, dy);

                    Log.i(TAG, "onScrollerCompute:" + mChild.getLeft() + " , " + mChild.getTop());
                }
            });
            mGestureManager.setDebug(true);
        }
        return mGestureManager;
    }

    private boolean canPull()
    {
        final int dx = (int) getGestureManager().getTouchHelper().getDeltaXFromDown();
        final int dy = (int) getGestureManager().getTouchHelper().getDeltaYFromDown();
        final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop)
        {
            return true;
        }

        return false;
    }

    private View findMaxTopChild(MotionEvent event)
    {
        return FTouchHelper.findTopChildUnder(this, (int) event.getX(), (int) event.getY());
    }

    private void offsetLeftAndRightLegal(View view, int delta)
    {
        final int min = getLeftAlignParentLeft(this, mChild, true);
        final int max = getLeftAlignParentRight(this, mChild, true);

        delta = FTouchHelper.getLegalDelta(view.getLeft(), min, max, delta);
        ViewCompat.offsetLeftAndRight(view, delta);
    }

    private void offsetTopAndBottomLegal(View view, int delta)
    {
        final int min = getTopAlignParentTop(this, mChild, true);
        final int max = getTopAlignParentBottom(this, mChild, true);

        delta = FTouchHelper.getLegalDelta(view.getTop(), min, max, delta);
        ViewCompat.offsetTopAndBottom(view, delta);
    }

    private void doScroll()
    {
        final int startX = mChild.getLeft();

        final int alignLeft = getLeftAlignParentLeft(this, mChild, true);
        final int alignRight = getLeftAlignParentRight(this, mChild, true);

        final int endX = startX < (alignLeft + alignRight) / 2 ? alignLeft : alignRight;

        getGestureManager().getScroller().scrollToX(startX, endX, -1);
    }

    private void doFling(VelocityTracker velocityTracker)
    {
        velocityTracker.computeCurrentVelocity(1000);

        final int startX = mChild.getLeft();
        final int startY = mChild.getTop();

        final int velocityX = (int) velocityTracker.getXVelocity();
        final int velocityY = (int) velocityTracker.getYVelocity();

        final int minX = getLeftAlignParentLeft(this, mChild, true);
        final int maxX = getLeftAlignParentRight(this, mChild, true);

        final int minY = getTopAlignParentTop(this, mChild, true);
        final int maxY = getTopAlignParentBottom(this, mChild, true);

        getGestureManager().getScroller().fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }


    /**
     * 返回child和parent左边对齐时候，child的left
     *
     * @param parent
     * @param child
     * @param margin
     * @return
     */
    public static int getLeftAlignParentLeft(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getPaddingLeft();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align += ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).leftMargin;
        }
        return align;
    }

    /**
     * 返回child和parent右边对齐时候，child的left
     *
     * @param parent
     * @param child
     * @param margin
     * @return
     */
    public static int getLeftAlignParentRight(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getWidth() - parent.getPaddingRight() - child.getWidth();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align -= ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).rightMargin;
        }
        return align;
    }

    /**
     * 返回child和parent顶部对齐时候，child的top
     *
     * @param parent
     * @param child
     * @param margin
     * @return
     */
    public static int getTopAlignParentTop(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getPaddingTop();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align += ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).topMargin;
        }
        return align;
    }

    /**
     * 返回child和parent底部对齐时候，child的top
     *
     * @param parent
     * @param child
     * @param margin
     * @return
     */
    public static int getTopAlignParentBottom(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getHeight() - parent.getPaddingTop() - child.getHeight();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align -= ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
        }
        return align;
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
        if (getGestureManager().getScroller().computeScrollOffset())
            ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        getGestureManager().getScroller().setMaxScrollDistance(getHeight());
    }
}

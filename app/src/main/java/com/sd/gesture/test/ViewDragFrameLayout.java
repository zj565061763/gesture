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
import com.sd.lib.gesture.FScroller;
import com.sd.lib.gesture.FTouchHelper;

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
            if (getScroller().isFinished())
            {
                mChild = child;
                Log.e(TAG, "setChild:" + child);
            }
        }
    }

    private FScroller getScroller()
    {
        if (mScroller == null)
        {
            mScroller = new FScroller(getContext())
            {
                @Override
                protected void onScrollStart()
                {
                    Log.i(TAG, "onScrollStart");
                }

                @Override
                protected void onScrollCompute(int lastX, int lastY, int currX, int currY)
                {
                    final int dx = currX - lastX;
                    final int dy = currY - lastY;

                    Log.i(TAG, "onScrollCompute:" + dx + "," + dy);

                    offsetLeftAndRightLegal(mChild, dx);
                    offsetTopAndBottomLegal(mChild, dy);
                }

                @Override
                protected void onScrollFinish(boolean isAbort)
                {
                    Log.i(TAG, "onScrollFinish isAbort:" + isAbort);

                    setChild(null);
                }
            };
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
                                final int dx = (int) getGestureManager().getTouchHelper().getDeltaXFromDown();
                                final int dy = (int) getGestureManager().getTouchHelper().getDeltaYFromDown();
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
                    final int dx = (int) getGestureManager().getTouchHelper().getDeltaX();
                    final int dy = (int) getGestureManager().getTouchHelper().getDeltaY();

                    offsetLeftAndRightLegal(mChild, dx);
                    offsetTopAndBottomLegal(mChild, dy);

                    return true;
                }

                @Override
                public void onEventFinish(FGestureManager.FinishParams params, VelocityTracker velocityTracker, MotionEvent event)
                {
                    if (params.hasConsumeEvent)
                    {
                        doScroll();
                    } else
                    {
                        if (getScroller().isFinished())
                        {
                            setChild(null);
                        }
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

        final int minX = getLeftAlignParentLeft(this, mChild, true);
        final int maxX = getLeftAlignParentRight(this, mChild, true);

        final int minY = getTopAlignParentTop(this, mChild, true);
        final int maxY = getTopAlignParentBottom(this, mChild, true);

        final boolean fling = getScroller().fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        if (fling)
        {
            invalidate();
        }
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

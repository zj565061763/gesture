/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fanwe.lib.gesture;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * 触摸事件处理帮助类<br>
 */
public class FTouchHelper
{
    private boolean mIsDebug;

    /**
     * 最后一次ACTION_DOWN事件
     */
    public static final int EVENT_DOWN = 0;
    /**
     * 当前事件的上一次事件
     */
    public static final int EVENT_LAST = 1;

    private float mCurrentX;
    private float mCurrentY;
    private float mLastX;
    private float mLastY;

    private float mDownX;
    private float mDownY;

    private float mMoveX;
    private float mMoveY;

    private float mUpX;
    private float mUpY;

    private float mCancelX;
    private float mCancelY;

    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    /**
     * 处理触摸事件
     *
     * @param ev
     */
    public void processTouchEvent(MotionEvent ev)
    {
        mLastX = mCurrentX;
        mLastY = mCurrentY;

        mCurrentX = ev.getRawX();
        mCurrentY = ev.getRawY();

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mDownX = mCurrentX;
                mDownY = mCurrentY;
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = mCurrentX;
                mMoveY = mCurrentY;
                break;
            case MotionEvent.ACTION_UP:
                mUpX = mCurrentX;
                mUpY = mCurrentY;
                break;
            case MotionEvent.ACTION_CANCEL:
                mCancelX = mCurrentX;
                mCancelY = mCurrentY;
                break;
            default:
                break;
        }

        if (mIsDebug)
        {
            StringBuilder sb = getDebugInfo();
            Log.i(getClass().getSimpleName(), "event " + ev.getAction() + ":" + sb.toString());
        }
    }

    public float getCurrentX()
    {
        return mCurrentX;
    }

    public float getCurrentY()
    {
        return mCurrentY;
    }

    public float getLastX()
    {
        return mLastX;
    }

    public float getLastY()
    {
        return mLastY;
    }

    public float getDownX()
    {
        return mDownX;
    }

    public float getDownY()
    {
        return mDownY;
    }

    public float getMoveX()
    {
        return mMoveX;
    }

    public float getMoveY()
    {
        return mMoveY;
    }

    public float getUpX()
    {
        return mUpX;
    }

    public float getUpY()
    {
        return mUpY;
    }

    public float getCancelX()
    {
        return mCancelX;
    }

    public float getCancelY()
    {
        return mCancelY;
    }

    /**
     * 返回当前事件和指定事件之间的x轴方向增量
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public float getDeltaXFrom(int event)
    {
        switch (event)
        {
            case EVENT_DOWN:
                return mCurrentX - mDownX;
            case EVENT_LAST:
                return mCurrentX - mLastX;
            default:
                return 0;
        }
    }

    /**
     * 返回当前事件和指定事件之间的y轴方向增量
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public float getDeltaYFrom(int event)
    {
        switch (event)
        {
            case EVENT_DOWN:
                return mCurrentY - mDownY;
            case EVENT_LAST:
                return mCurrentY - mLastY;
            default:
                return 0;
        }
    }

    /**
     * 返回当前事件和指定事件之间的x轴方向夹角
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public double getDegreeXFrom(int event)
    {
        final float dx = getDeltaXFrom(event);
        if (dx == 0)
        {
            return 0;
        }
        final float dy = getDeltaYFrom(event);
        final float angle = Math.abs(dy) / Math.abs(dx);
        return Math.toDegrees(Math.atan(angle));
    }

    /**
     * 返回当前事件和指定事件之间的y轴方向夹角
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public double getDegreeYFrom(int event)
    {
        final float dy = getDeltaYFrom(event);
        if (dy == 0)
        {
            return 0;
        }
        final float dx = getDeltaXFrom(event);
        final float angle = Math.abs(dx) / Math.abs(dy);
        return Math.toDegrees(Math.atan(angle));
    }

    /**
     * 是否是点击事件
     *
     * @param event
     * @param context
     * @return
     */
    public boolean isClick(MotionEvent event, Context context)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            final long clickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
            final int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

            final long duration = event.getEventTime() - event.getDownTime();
            final int dx = (int) getDeltaXFrom(FTouchHelper.EVENT_DOWN);
            final int dy = (int) getDeltaYFrom(FTouchHelper.EVENT_DOWN);

            if (duration < clickTimeout && dx < touchSlop && dy < touchSlop)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回当前事件相对于指定事件是否向左移动
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public boolean isMoveLeftFrom(int event)
    {
        return getDeltaXFrom(event) < 0;
    }

    /**
     * 返回当前事件相对于指定事件是否向上移动
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public boolean isMoveTopFrom(int event)
    {
        return getDeltaYFrom(event) < 0;
    }

    /**
     * 返回当前事件相对于指定事件是否向右移动
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public boolean isMoveRightFrom(int event)
    {
        return getDeltaXFrom(event) > 0;
    }

    /**
     * 返回当前事件相对于指定事件是否向下移动
     *
     * @param event {@link #EVENT_DOWN} {@link #EVENT_LAST}
     * @return
     */
    public boolean isMoveBottomFrom(int event)
    {
        return getDeltaYFrom(event) > 0;
    }

    //----------static method start----------

    /**
     * 返回合理的增量
     *
     * @param current 当前值
     * @param min     最小值
     * @param max     最大值
     * @param delta   增量
     * @return
     */
    public static int getLegalDelta(int current, int min, int max, int delta)
    {
        if (delta == 0) return 0;

        final int future = current + delta;
        if (future < min)
        {
            delta += (min - future);
        } else if (future > max)
        {
            delta += (max - future);
        }
        return delta;
    }

    /**
     * 是否请求当前view的父view不要拦截事件
     *
     * @param view
     * @param disallowIntercept true-请求父view不要拦截，false-父view可以拦截
     */
    public static void requestDisallowInterceptTouchEvent(View view, boolean disallowIntercept)
    {
        final ViewParent parent = view.getParent();
        if (parent != null)
        {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /**
     * view是否处于某个坐标点下面，相对父布局的坐标
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    public static boolean isViewUnder(View view, int x, int y)
    {
        return x >= view.getLeft() && x < view.getRight()
                && y >= view.getTop() && y < view.getBottom();
    }

    /**
     * view是否处于某个坐标点下面，相对屏幕的坐标
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    public static boolean isViewUnderScreen(View view, int x, int y)
    {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        return x >= location[0] && x < location[0] + view.getWidth()
                && y >= location[1] && y < location[1] + view.getHeight();
    }

    /**
     * 找到parent中处于指定坐标下最顶部的child
     *
     * @param parent
     * @param x
     * @param y
     * @return
     */
    public static View findTopChildUnder(ViewGroup parent, int x, int y)
    {
        final int count = parent.getChildCount();
        for (int i = count - 1; i >= 0; i--)
        {
            final View child = parent.getChildAt(i);
            if (isViewUnder(child, x, y))
            {
                return child;
            }
        }
        return null;
    }

    public static int getLeftAlignParentLeft(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getPaddingLeft();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align += ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).leftMargin;
        }
        return align;
    }

    public static int getLeftAlignParentRight(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getWidth() - parent.getPaddingRight() - child.getWidth();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align -= ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).rightMargin;
        }
        return align;
    }

    public static int getTopAlignParentTop(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getPaddingTop();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align += ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).topMargin;
        }
        return align;
    }

    public static int getTopAlignParentBottom(ViewGroup parent, View child, boolean margin)
    {
        int align = parent.getHeight() - parent.getPaddingTop() - child.getHeight();
        if (margin && child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            align -= ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
        }
        return align;
    }

    /**
     * view是否已经滚动到最左边
     *
     * @param view
     * @return
     */
    public static boolean isScrollToLeft(View view)
    {
        return !view.canScrollHorizontally(-1);
    }

    /**
     * view是否已经滚动到最顶部
     *
     * @param view
     * @return
     */
    public static boolean isScrollToTop(View view)
    {
        return !view.canScrollVertically(-1);
    }

    /**
     * view是否已经滚动到最右边
     *
     * @param view
     * @return
     */
    public static boolean isScrollToRight(View view)
    {
        return !view.canScrollHorizontally(1);
    }

    /**
     * view是否已经滚动到最底部
     *
     * @param view
     * @return
     */
    public static boolean isScrollToBottom(View view)
    {
        return !view.canScrollVertically(1);
    }

    //----------static method end----------

    public StringBuilder getDebugInfo()
    {
        StringBuilder sb = new StringBuilder("\r\n")
                .append("Down:").append(mDownX).append(",").append(mDownY).append("\r\n")
                .append("Move:").append(mMoveX).append(",").append(mMoveY).append("\r\n")

                .append("Delta from down:").append(getDeltaXFrom(EVENT_DOWN)).append(",").append(getDeltaYFrom(EVENT_DOWN)).append("\r\n")
                .append("Delta from last:").append(getDeltaXFrom(EVENT_LAST)).append(",").append(getDeltaYFrom(EVENT_LAST)).append("\r\n")

                .append("Degree from down:").append(getDegreeXFrom(EVENT_DOWN)).append(",").append(getDegreeYFrom(EVENT_DOWN)).append("\r\n")
                .append("Degree from last:").append(getDegreeXFrom(EVENT_LAST)).append(",").append(getDegreeYFrom(EVENT_LAST)).append("\r\n");
        return sb;
    }
}

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

import android.widget.Scroller;

/**
 * 滚动帮助类
 */
public class FScroller
{
    private final Scroller mScroller;
    /**
     * 最大滚动距离
     */
    private int mMaxScrollDistance;
    /**
     * 最大滚动时长
     */
    private int mMaxScrollDuration = 600;
    /**
     * 最小滚动时长
     */
    private int mMinScrollDuration = 256;

    private int mLastX;
    private int mLastY;
    private boolean mIsFinished = true;

    private Callback mCallback;

    public FScroller(Scroller scroller)
    {
        if (scroller == null)
        {
            throw new NullPointerException("scroller is null");
        }
        mScroller = scroller;
    }

    /**
     * 设置回调
     *
     * @param callback
     */
    public final void setCallback(Callback callback)
    {
        mCallback = callback;
    }

    /**
     * 返回滚动是否结束
     *
     * @return true-滚动结束，false-滚动中
     */
    public final boolean isFinished()
    {
        /**
         * 这里直接返回当前对象保存的属性
         * 如果返回{@link FScroller#isFinished()}方法的返回值，则在通知{@link Callback#onScroll(int, int)}方法的时候，调用是否结束方法有可能返回true
         */
        return mIsFinished;
    }

    /**
     * 设置最大滚动距离
     *
     * @param distance
     */
    public final void setMaxScrollDistance(int distance)
    {
        mMaxScrollDistance = distance;
    }

    /**
     * 设置最大滚动时长
     *
     * @param duration
     */
    public final void setMaxScrollDuration(int duration)
    {
        mMaxScrollDuration = duration;
    }

    /**
     * 设置最小滚动时长
     *
     * @param duration
     */
    public final void setMinScrollDuration(int duration)
    {
        mMinScrollDuration = duration;
    }

    // scrollTo
    public final boolean scrollToX(int startX, int endX, int duration)
    {
        return scrollTo(startX, 0, endX, 0, duration);
    }

    public final boolean scrollToY(int startY, int endY, int duration)
    {
        return scrollTo(0, startY, 0, endY, duration);
    }

    public final boolean scrollTo(int startX, int startY, int endX, int endY, int duration)
    {
        final int dx = endX - startX;
        final int dy = endY - startY;

        return scrollDelta(startX, startY, dx, dy, duration);
    }

    // scrollDelta
    public final boolean scrollDeltaX(int startX, int dx, int duration)
    {
        return scrollDelta(startX, 0, dx, 0, duration);
    }

    public final boolean scrollDeltaY(int startY, int dy, int duration)
    {
        return scrollDelta(0, startY, 0, dy, duration);
    }

    /**
     * 所有此类的滚动扩展方法最终需要调用的方法
     *
     * @param startX
     * @param startY
     * @param dx
     * @param dy
     * @param duration
     * @return true-提交滚动任务成功
     */
    public final boolean scrollDelta(int startX, int startY, int dx, int dy, int duration)
    {
        final boolean scroll = dx != 0 || dy != 0;

        if (scroll)
        {
            mLastX = startX;
            mLastY = startY;

            if (duration < 0)
            {
                duration = getDuration(dx, dy);
            }

            mScroller.startScroll(startX, startY, dx, dy, duration);
            updateFinished();
        }
        return scroll;
    }

    public boolean flingX(int startX, int velocityX, int minX, int maxX)
    {
        return fling(startX, 0, velocityX, 0, minX, maxX, 0, 0);
    }

    public boolean flingY(int startY, int velocityY, int minY, int maxY)
    {
        return fling(0, startY, 0, velocityY, 0, 0, minY, maxY);
    }

    public boolean fling(int startX, int startY,
                         int velocityX, int velocityY,
                         int minX, int maxX,
                         int minY, int maxY)
    {
        final boolean fling = (startX > minX && startX < maxX && velocityX != 0)
                || (startY > minY && startY < maxY && velocityY != 0);

        if (fling)
        {
            mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            updateFinished();
        }
        return fling;
    }

    /**
     * 返回根据滚动距离和滚动速度算出的滚动时长
     *
     * @param dx x滚动距离
     * @param dy y滚动距离
     * @return
     */
    public final int getDuration(int dx, int dy)
    {
        return computeDuration(dx, dy, mMaxScrollDistance, mMaxScrollDuration, mMinScrollDuration);
    }

    /**
     * 计算滚动距离
     *
     * @return true-滚动中，false-滚动结束
     */
    public final boolean computeScrollOffset()
    {
        final boolean compute = mScroller.computeScrollOffset();

        final int currX = mScroller.getCurrX();
        final int currY = mScroller.getCurrY();

        if (compute)
        {
            if (currX != mLastX || currY != mLastY)
            {
                onScroll(currX, currY, mLastX, mLastY);
                if (mCallback != null) mCallback.onScroll(currX, currY, mLastX, mLastY);
            }
        }

        mLastX = currX;
        mLastY = currY;

        updateFinished();
        return compute;
    }

    /**
     * 停止滚动
     */
    public final void abortAnimation()
    {
        mScroller.abortAnimation();
        updateFinished();
    }

    private void updateFinished()
    {
        final boolean finish = mScroller.isFinished();
        if (mIsFinished != finish)
        {
            mIsFinished = finish;

            onScrollStateChanged(finish);
            if (mCallback != null) mCallback.onScrollStateChanged(finish);
        }
    }

    /**
     * 滚动状态变化回调
     *
     * @param isFinished true-滚动结束，false-滚动中
     */
    protected void onScrollStateChanged(boolean isFinished)
    {
    }

    /**
     * 调用{@link FScroller#computeScrollOffset()}后触发
     *
     * @param currX 当前x
     * @param currY 当前y
     * @param lastX 上一次的x
     * @param lastY 上一次的y
     */
    protected void onScroll(int currX, int currY, int lastX, int lastY)
    {
    }

    /**
     * 计算时长
     *
     * @param dx          x方向移动距离
     * @param dy          y方向移动距离
     * @param maxDistance 最大可以移动距离
     * @param maxDuration 最大时长
     * @param minDuration 最小时长
     * @return
     */
    public static int computeDuration(int dx, int dy, int maxDistance, int maxDuration, int minDuration)
    {
        maxDistance = Math.abs(maxDistance);
        if (maxDistance == 0)
        {
            return minDuration;
        }

        final float distance = (float) Math.sqrt(Math.abs(dx * dx) + Math.abs(dy * dy));
        final float disPercent = distance / maxDistance;
        final int duration = (int) ((disPercent * minDuration) + minDuration);
        return Math.min(duration, maxDuration);
    }

    public interface Callback
    {
        /**
         * 滚动状态变化回调
         *
         * @param isFinished true-滚动结束，false-滚动中
         */
        void onScrollStateChanged(boolean isFinished);

        /**
         * 调用{@link FScroller#computeScrollOffset()}后触发
         *
         * @param currX 当前x
         * @param currY 当前y
         * @param lastX 上一次的x
         * @param lastY 上一次的y
         */
        void onScroll(int currX, int currY, int lastX, int lastY);
    }
}

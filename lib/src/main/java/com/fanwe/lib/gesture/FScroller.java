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

    /**
     * 两次computeScrollOffset()之间x移动的距离
     */
    private int mDeltaX;
    /**
     * 两次computeScrollOffset()之间y移动的距离
     */
    private int mDeltaY;

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

    private void updateFinished()
    {
        final boolean isFinished = isFinished();
        if (mIsFinished != isFinished)
        {
            mIsFinished = isFinished;
            if (mCallback != null) mCallback.onScrollStateChanged(mIsFinished);
        }
    }

    public void setCallback(Callback callback)
    {
        mCallback = callback;
    }

    /**
     * 返回滚动是否结束
     *
     * @return true-滚动结束，false-滚动中
     */
    public boolean isFinished()
    {
        return mScroller.isFinished();
    }

    /**
     * 设置最大滚动距离
     *
     * @param maxScrollDistance
     */
    public void setMaxScrollDistance(int maxScrollDistance)
    {
        mMaxScrollDistance = maxScrollDistance;
    }

    /**
     * 设置最大滚动时长
     *
     * @param maxScrollDuration
     */
    public void setMaxScrollDuration(int maxScrollDuration)
    {
        mMaxScrollDuration = maxScrollDuration;
    }

    /**
     * 设置最小滚动时长
     *
     * @param minScrollDuration
     */
    public void setMinScrollDuration(int minScrollDuration)
    {
        mMinScrollDuration = minScrollDuration;
    }

    // scrollTo
    public boolean scrollToX(int startX, int endX, int duration)
    {
        return scrollTo(startX, 0, endX, 0, duration);
    }

    public boolean scrollToY(int startY, int endY, int duration)
    {
        return scrollTo(0, startY, 0, endY, duration);
    }

    public boolean scrollTo(int startX, int startY, int endX, int endY, int duration)
    {
        final int dx = endX - startX;
        final int dy = endY - startY;

        return scrollDelta(startX, startY, dx, dy, duration);
    }

    // scrollDelta
    public boolean scrollDeltaX(int startX, int dx, int duration)
    {
        return scrollDelta(startX, 0, dx, 0, duration);
    }

    public boolean scrollDeltaY(int startY, int dy, int duration)
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
    public boolean scrollDelta(int startX, int startY, int dx, int dy, int duration)
    {
        if (dx == 0 && dy == 0)
        {
            abortAnimation();
            return false;
        }

        mLastX = startX;
        mLastY = startY;

        if (duration < 0) duration = getDuration(dx, dy);
        mScroller.startScroll(startX, startY, dx, dy, duration);

        updateFinished();
        return true;
    }

    /**
     * 计算滚动距离
     *
     * @return true-滚动中，false-滚动结束
     */
    public boolean computeScrollOffset()
    {
        final boolean result = mScroller.computeScrollOffset();
        updateFinished();

        final int currX = mScroller.getCurrX();
        final int currY = mScroller.getCurrY();

        mDeltaX = currX - mLastX;
        mDeltaY = currY - mLastY;

        mLastX = currX;
        mLastY = currY;

        if (mDeltaX != 0 || mDeltaY != 0)
        {
            if (mCallback != null) mCallback.onScroll(mDeltaX, mDeltaY);
        }

        return result;
    }

    /**
     * 停止滚动
     */
    public void abortAnimation()
    {
        mScroller.abortAnimation();
        updateFinished();
    }

    /**
     * 两次computeScrollOffset()之间x移动的距离
     *
     * @return
     */
    public int getDeltaX()
    {
        return mDeltaX;
    }

    /**
     * 两次computeScrollOffset()之间y移动的距离
     *
     * @return
     */
    public int getDeltaY()
    {
        return mDeltaY;
    }

    /**
     * 返回根据滚动距离和滚动速度算出的滚动时长
     *
     * @param dx x滚动距离
     * @param dy y滚动距离
     * @return
     */
    public int getDuration(int dx, int dy)
    {
        final int duration = computeDuration(dx, dy, mMaxScrollDistance, mMaxScrollDuration, mMinScrollDuration);
        return duration;
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
         * @param dx x应该移动的距离
         * @param dy y应该移动的距离
         */
        void onScroll(int dx, int dy);
    }
}

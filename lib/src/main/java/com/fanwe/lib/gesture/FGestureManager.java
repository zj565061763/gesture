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
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

public class FGestureManager
{
    private final Context mContext;
    private FScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private boolean mHasConsumed;
    private final FTouchHelper mTouchHelper = new FTouchHelper()
    {
        @Override
        public void setTagIntercept(boolean tagIntercept)
        {
            super.setTagIntercept(tagIntercept);
            getCallback().onTagInterceptChanged(tagIntercept);
        }

        @Override
        public void setTagConsume(boolean tagConsume)
        {
            super.setTagConsume(tagConsume);
            getCallback().onTagConsumeChanged(tagConsume);
        }
    };

    private Callback mCallback;

    public FGestureManager(Context context)
    {
        mContext = context.getApplicationContext();
    }

    public void setCallback(Callback callback)
    {
        mCallback = callback;
    }

    public FTouchHelper getTouchHelper()
    {
        return mTouchHelper;
    }

    public FScroller getScroller()
    {
        if (mScroller == null)
        {
            mScroller = new FScroller(mContext);
        }
        return mScroller;
    }

    public VelocityTracker getVelocityTracker()
    {
        if (mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        return mVelocityTracker;
    }

    private void releaseVelocityTracker()
    {
        if (mVelocityTracker != null)
        {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 一次完整的按下到离开的触摸过程中，是否有消费过事件
     *
     * @return
     */
    public boolean hasConsumed()
    {
        return mHasConsumed;
    }

    private Callback getCallback()
    {
        if (mCallback == null)
        {
            mCallback = Callback.DEFAULT;
        }
        return mCallback;
    }

    /**
     * 外部调用
     *
     * @param event
     * @return
     */
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        if (mTouchHelper.isTagIntercept())
        {
            return true;
        }

        mTouchHelper.processTouchEvent(event);
        getVelocityTracker().addMovement(event);

        switch (event.getAction())
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                mHasConsumed = false;
                break;
            default:
                if (getCallback().shouldInterceptTouchEvent(event))
                {
                    mTouchHelper.setTagIntercept(true);
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * 外部调用
     *
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event)
    {
        mTouchHelper.processTouchEvent(event);
        getVelocityTracker().addMovement(event);

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                return getCallback().consumeDownEvent(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getCallback().onConsumeEventFinish(event);
                releaseVelocityTracker();
                mHasConsumed = false;
                break;
            default:
                if (mTouchHelper.isTagConsume())
                {
                    final boolean consume = getCallback().onConsumeEvent(event);
                    mTouchHelper.setTagConsume(consume);

                    if (consume)
                    {
                        mHasConsumed = true;
                    }
                } else
                {
                    final boolean shouldConsume = getCallback().shouldConsumeTouchEvent(event);
                    mTouchHelper.setTagConsume(shouldConsume);
                }
                break;
        }

        return mTouchHelper.isTagConsume();
    }

    /**
     * 是否是点击事件
     *
     * @param event
     * @return
     */
    public boolean isClick(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            final long clickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
            final int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

            final long duration = event.getEventTime() - event.getDownTime();
            final int dx = (int) getTouchHelper().getDeltaXFrom(FTouchHelper.EVENT_DOWN);
            final int dy = (int) getTouchHelper().getDeltaYFrom(FTouchHelper.EVENT_DOWN);

            if (duration < clickTimeout && dx < touchSlop && dy < touchSlop)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * 外部调用
     *
     * @return true-滚动还未结束
     */
    public boolean computeScroll()
    {
        final boolean computeScrollOffset = getScroller().computeScrollOffset();

        final int dx = getScroller().getDeltaX();
        final int dy = getScroller().getDeltaY();
        getCallback().onComputeScroll(dx, dy, !computeScrollOffset);
        return computeScrollOffset;
    }

    public interface Callback
    {
        /**
         * 是否开始拦截事件({@link #onInterceptTouchEvent(MotionEvent)}方法触发)
         *
         * @param event
         * @return
         */
        boolean shouldInterceptTouchEvent(MotionEvent event);

        /**
         * 是否需要拦截发生变化
         *
         * @param intercept
         */
        void onTagInterceptChanged(boolean intercept);

        /**
         * 是否需要消费按下事件，只有此方法返回true，才有后续的移动事件
         *
         * @param event
         * @return
         */
        boolean consumeDownEvent(MotionEvent event);

        /**
         * 是否开始消费事件
         *
         * @param event
         * @return
         */
        boolean shouldConsumeTouchEvent(MotionEvent event);

        /**
         * 是否需要消费发生变化
         *
         * @param consume
         */
        void onTagConsumeChanged(boolean consume);

        /**
         * 事件回调
         *
         * @param event
         * @return
         */
        boolean onConsumeEvent(MotionEvent event);

        /**
         * 事件结束回调，收到{@link MotionEvent#ACTION_UP}或者{@link MotionEvent#ACTION_CANCEL}
         *
         * @param event
         */
        void onConsumeEventFinish(MotionEvent event);

        /**
         * 计算滚动回调
         *
         * @param dx     x需要滚动的距离
         * @param dy     y需要滚动的距离
         * @param finish 滚动是否结束
         */
        void onComputeScroll(int dx, int dy, boolean finish);

        Callback DEFAULT = new SimpleCallback()
        {
            @Override
            public boolean consumeDownEvent(MotionEvent event)
            {
                return false;
            }

            @Override
            public boolean shouldConsumeTouchEvent(MotionEvent event)
            {
                return false;
            }

            @Override
            public boolean onConsumeEvent(MotionEvent event)
            {
                return false;
            }

            @Override
            public void onConsumeEventFinish(MotionEvent event)
            {
            }

            @Override
            public void onComputeScroll(int dx, int dy, boolean finish)
            {
            }
        };
    }

    public abstract static class SimpleCallback implements Callback
    {
        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent event)
        {
            return false;
        }

        @Override
        public void onTagInterceptChanged(boolean intercept)
        {
        }

        @Override
        public void onTagConsumeChanged(boolean consume)
        {
        }
    }
}

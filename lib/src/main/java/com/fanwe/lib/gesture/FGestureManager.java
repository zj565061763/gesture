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
import android.widget.Scroller;

public class FGestureManager
{
    public static final int STATE_IDLE = 0;
    public static final int STATE_CONSUME = 1;
    public static final int STATE_FLING = 2;

    private final Context mContext;

    private final FTouchHelper mTouchHelper;
    private final FTagHolder mTagHolder;
    private FScroller mScroller;

    private int mState = STATE_IDLE;

    private VelocityTracker mVelocityTracker;
    private boolean mHasConsumeEvent;

    private final Callback mCallback;

    public FGestureManager(Context context, Callback callback)
    {
        if (callback == null) throw new NullPointerException("callback is null");
        mCallback = callback;
        mContext = context.getApplicationContext();

        mTouchHelper = new FTouchHelper();
        mTagHolder = new FTagHolder()
        {
            @Override
            protected void onTagConsumeChanged(boolean tag)
            {
                super.onTagConsumeChanged(tag);
                updateStateIfNeed();
            }
        };
    }

    /**
     * 返回触摸帮助类
     *
     * @return
     */
    public FTouchHelper getTouchHelper()
    {
        return mTouchHelper;
    }

    /**
     * 返回滚动帮助类
     *
     * @return
     */
    public FScroller getScroller()
    {
        if (mScroller == null)
        {
            mScroller = new FScroller(new Scroller(mContext))
            {
                @Override
                protected void onScrollStateChanged(boolean isFinished)
                {
                    super.onScrollStateChanged(isFinished);
                    updateStateIfNeed();
                }
            };
        }
        return mScroller;
    }

    private VelocityTracker getVelocityTracker()
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
     * 返回当前的状态
     *
     * @return {@link #STATE_IDLE} {@link #STATE_CONSUME} {@link #STATE_FLING}
     */
    public int getState()
    {
        return mState;
    }

    private void updateStateIfNeed()
    {
        if (mTagHolder.isTagConsume())
        {
            setState(STATE_CONSUME);
        } else
        {
            if (getScroller().isFinished())
            {
                setState(STATE_IDLE);
            } else
            {
                setState(STATE_FLING);
            }
        }
    }

    private void setState(int state)
    {
        if (mState != state)
        {
            mState = state;
            mCallback.onStateChanged(state);
        }
    }

    private void reset()
    {
        mTagHolder.reset();
        mHasConsumeEvent = false;
        releaseVelocityTracker();
    }

    /**
     * 外部调用
     *
     * @param event
     * @return
     */
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        mTouchHelper.processTouchEvent(event);
        getVelocityTracker().addMovement(event);

        switch (event.getAction())
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;
            default:
                if (mCallback.shouldInterceptEvent(event))
                {
                    mTagHolder.setTagIntercept(true);
                }
                break;
        }

        return mTagHolder.isTagIntercept();
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
                return mCallback.consumeDownEvent();
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mHasConsumeEvent)
                {
                    mCallback.onConsumeEventFinish(event, getVelocityTracker());
                }
                mCallback.onEventFinish(event);
                reset();
                break;
            default:
                if (mTagHolder.isTagConsume())
                {
                    final boolean consume = mCallback.onConsumeEvent(event);
                    mTagHolder.setTagConsume(consume);

                    if (consume)
                    {
                        // 标识消费过事件
                        mHasConsumeEvent = true;
                    }
                } else
                {
                    mTagHolder.setTagConsume(mCallback.shouldConsumeEvent(event));
                }
                break;
        }

        return mTagHolder.isTagConsume();
    }

    public abstract static class Callback
    {
        /**
         * 是否开始拦截事件(由{@link #onInterceptTouchEvent(MotionEvent)}方法触发)
         *
         * @param event
         * @return
         */
        public boolean shouldInterceptEvent(MotionEvent event)
        {
            return false;
        }

        /**
         * 是否消费{@link MotionEvent#ACTION_DOWN}事件(由{@link #onTouchEvent(MotionEvent)}方法触发)
         * <br>
         * 注意，只有此方法返回了true，才有后续的移动等事件，默认返回true
         *
         * @return
         */
        public boolean consumeDownEvent()
        {
            return true;
        }

        /**
         * 是否开始消费事件(由{@link #onTouchEvent(MotionEvent)}方法触发)
         *
         * @param event
         * @return
         */
        public abstract boolean shouldConsumeEvent(MotionEvent event);

        /**
         * 事件回调
         *
         * @param event
         * @return
         */
        public abstract boolean onConsumeEvent(MotionEvent event);

        /**
         * 事件结束({@link MotionEvent#ACTION_UP}或者{@link MotionEvent#ACTION_CANCEL})，并且{@link #onConsumeEvent(MotionEvent)}方法消费过事件
         *
         * @param event
         * @param velocityTracker
         */
        public abstract void onConsumeEventFinish(MotionEvent event, VelocityTracker velocityTracker);

        /**
         * 事件结束({@link MotionEvent#ACTION_UP}或者{@link MotionEvent#ACTION_CANCEL})
         *
         * @param event
         */
        public void onEventFinish(MotionEvent event)
        {
        }

        /**
         * 状态变化回调
         *
         * @param state {@link #STATE_IDLE} {@link #STATE_CONSUME} {@link #STATE_FLING}
         */
        public void onStateChanged(int state)
        {
        }
    }
}

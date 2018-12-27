/*
 * Copyright (C) 2017 Sunday (https://github.com/zj565061763)
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
package com.sd.lib.gesture;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class FGestureManager
{
    private final FTouchHelper mTouchHelper = new FTouchHelper();
    private final TagHolder mTagHolder;
    private final FScroller mScroller;

    private State mState = State.Idle;

    private VelocityTracker mVelocityTracker;

    private boolean mHasConsumeEvent = false;
    private boolean mIsCancelTouchEvent = false;

    private final Callback mCallback;

    public FGestureManager(Context context, Callback callback)
    {
        if (callback == null)
            throw new NullPointerException("callback is null");
        mCallback = callback;

        mTagHolder = new TagHolder()
        {
            @Override
            protected void onTagInterceptChanged(boolean tag)
            {
                super.onTagInterceptChanged(tag);
            }

            @Override
            protected void onTagConsumeChanged(boolean tag)
            {
                if (tag)
                    setState(State.Drag);

                super.onTagConsumeChanged(tag);
            }
        };

        mScroller = new FScroller(context)
        {
            @Override
            protected void onScrollerStart()
            {
                setState(State.Fling);
                super.onScrollerStart();
            }

            @Override
            protected void onScrollerCompute(int lastX, int lastY, int currX, int currY)
            {
                mCallback.onScrollerCompute(lastX, lastY, currX, currY);
                super.onScrollerCompute(lastX, lastY, currX, currY);
            }

            @Override
            protected void onScrollerFinish(boolean isAbort)
            {
                if (!getTagHolder().isTagConsume())
                    setState(State.Idle);

                super.onScrollerFinish(isAbort);
            }
        };
    }

    public FTouchHelper getTouchHelper()
    {
        return mTouchHelper;
    }

    public TagHolder getTagHolder()
    {
        return mTagHolder;
    }

    public FScroller getScroller()
    {
        return mScroller;
    }

    public State getState()
    {
        return mState;
    }

    private void setState(State state)
    {
        if (state == null)
            throw new NullPointerException();

        final State old = mState;
        if (old != state)
        {
            mState = state;
            mCallback.onStateChanged(old, state);
        }
    }

    private VelocityTracker getVelocityTracker()
    {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
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
     * 设置取消触摸事件
     */
    public void setCancelTouchEvent()
    {
        if (mTagHolder.isTagConsume() || mTagHolder.isTagIntercept())
        {
            mIsCancelTouchEvent = true;
            mTagHolder.reset();
        }
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

        final int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
        {
            onEventFinish(event);
        } else
        {
            if (action == MotionEvent.ACTION_DOWN)
                onEventStart(event);

            if (!mIsCancelTouchEvent)
            {
                if (!mTagHolder.isTagIntercept())
                    mTagHolder.setTagIntercept(mCallback.shouldInterceptEvent(event));
            }
        }

        return mTagHolder.isTagIntercept() && !mIsCancelTouchEvent;
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

        final int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
        {
            onEventFinish(event);
        } else if (action == MotionEvent.ACTION_DOWN)
        {
            onEventStart(event);
            return mCallback.onEventActionDown(event) && !mIsCancelTouchEvent;
        } else
        {
            if (!mIsCancelTouchEvent)
            {
                if (!mTagHolder.isTagConsume())
                {
                    mTagHolder.setTagConsume(mCallback.shouldConsumeEvent(event));
                } else
                {
                    mCallback.onEventConsume(event);
                    mHasConsumeEvent = true;
                }
            }
        }

        return mTagHolder.isTagConsume() && !mIsCancelTouchEvent;
    }

    private void onEventStart(MotionEvent event)
    {

    }

    private void onEventFinish(MotionEvent event)
    {
        mTagHolder.reset();

        final FinishParams params = new FinishParams(mHasConsumeEvent, mIsCancelTouchEvent);
        mCallback.onEventFinish(params, getVelocityTracker(), event);

        mHasConsumeEvent = false;
        mIsCancelTouchEvent = false;
        releaseVelocityTracker();

        if (mState == State.Drag)
            setState(State.Idle);
    }

    public static class FinishParams
    {
        /**
         * 本次按下到结束的过程中{@link Callback#onEventConsume(MotionEvent)}方法是否消费过事件
         */
        public final boolean hasConsumeEvent;
        /**
         * 本次按下到结束的过程中是否调用过{@link #setCancelTouchEvent()}方法，取消事件
         */
        public final boolean isCancelTouchEvent;

        private FinishParams(boolean hasConsumeEvent, boolean isCancelTouchEvent)
        {
            this.hasConsumeEvent = hasConsumeEvent;
            this.isCancelTouchEvent = isCancelTouchEvent;
        }
    }

    public enum State
    {
        Idle,
        Drag,
        Fling
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
         * @param event
         * @return
         */
        public boolean onEventActionDown(MotionEvent event)
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
         */
        public abstract void onEventConsume(MotionEvent event);

        /**
         * 事件结束，收到{@link MotionEvent#ACTION_UP}或者{@link MotionEvent#ACTION_CANCEL}事件
         *
         * @param params          {@link FinishParams}
         * @param velocityTracker 速率计算对象，这里返回的对象还未进行速率计算，如果要获得速率需要先进行计算{@link VelocityTracker#computeCurrentVelocity(int)}
         * @param event           {@link MotionEvent#ACTION_UP}或者{@link MotionEvent#ACTION_CANCEL}
         */
        public abstract void onEventFinish(FinishParams params, VelocityTracker velocityTracker, MotionEvent event);

        public void onStateChanged(State oldState, State newState)
        {
        }

        public void onScrollerCompute(int lastX, int lastY, int currX, int currY)
        {
        }
    }

    //---------- TagHolder Start ----------

    public static class TagHolder
    {
        /**
         * 是否需要拦截事件标识(用于onInterceptTouchEvent方法)
         */
        private boolean mTagIntercept = false;
        /**
         * 是否需要消费事件标识(用于onTouchEvent方法)
         */
        private boolean mTagConsume = false;

        private Callback mCallback;

        private TagHolder()
        {
        }

        //---------- public method start ----------

        public void setCallback(Callback callback)
        {
            mCallback = callback;
        }

        public boolean isTagIntercept()
        {
            return mTagIntercept;
        }

        public boolean isTagConsume()
        {
            return mTagConsume;
        }

        //---------- public method end ----------

        /**
         * 设置是否需要拦截事件标识(用于onInterceptTouchEvent方法)
         *
         * @param tag
         */
        void setTagIntercept(boolean tag)
        {
            if (mTagIntercept != tag)
            {
                mTagIntercept = tag;
                onTagInterceptChanged(tag);
            }
        }

        /**
         * 设置是否需要消费事件标识(用于onTouchEvent方法)
         *
         * @param tag
         */
        void setTagConsume(boolean tag)
        {
            if (mTagConsume != tag)
            {
                mTagConsume = tag;
                onTagConsumeChanged(tag);
            }
        }

        void reset()
        {
            setTagIntercept(false);
            setTagConsume(false);
        }

        protected void onTagInterceptChanged(boolean tag)
        {
            if (mCallback != null)
                mCallback.onTagInterceptChanged(tag);
        }

        protected void onTagConsumeChanged(boolean tag)
        {
            if (mCallback != null)
                mCallback.onTagConsumeChanged(tag);
        }

        public interface Callback
        {
            void onTagInterceptChanged(boolean tag);

            void onTagConsumeChanged(boolean tag);
        }
    }

    //---------- TagHolder Start ----------
}

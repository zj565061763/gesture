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

import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.fanwe.lib.gesture.tag.FTagHolder;
import com.fanwe.lib.gesture.tag.TagHolder;

public class FGestureManager
{
    private final FTouchHelper mTouchHelper = new FTouchHelper();
    private final FTagHolder mTagHolder = new FTagHolder();

    private VelocityTracker mVelocityTracker;
    private boolean mHasConsumeEvent;

    private final Callback mCallback;

    public FGestureManager(Callback callback)
    {
        if (callback == null) throw new NullPointerException("callback is null");
        mCallback = callback;
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
     * 返回标识持有者
     *
     * @return
     */
    public TagHolder getTagHolder()
    {
        return mTagHolder;
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
                mTagHolder.reset();
                mHasConsumeEvent = false;
                releaseVelocityTracker();
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
                return mCallback.onEventActionDown(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTagHolder.reset();
                mCallback.onEventFinish(event, mHasConsumeEvent, getVelocityTracker());
                mHasConsumeEvent = false;
                releaseVelocityTracker();
                break;
            default:
                if (mTagHolder.isTagConsume())
                {
                    final boolean consume = mCallback.onEventConsume(event);
                    mTagHolder.setTagConsume(consume);

                    // 标识消费过事件
                    if (consume) mHasConsumeEvent = true;
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
         * @return
         */
        public abstract boolean onEventConsume(MotionEvent event);

        /**
         * 事件结束({@link MotionEvent#ACTION_UP}或者{@link MotionEvent#ACTION_CANCEL})
         *
         * @param event
         * @param hasConsumeEvent 本次按下到结束的过程中{@link #onEventConsume(MotionEvent)}方法是否消费过事件
         * @param velocityTracker
         */
        public abstract void onEventFinish(MotionEvent event, boolean hasConsumeEvent, VelocityTracker velocityTracker);
    }
}

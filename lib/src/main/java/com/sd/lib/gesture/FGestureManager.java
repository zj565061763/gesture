package com.sd.lib.gesture;

import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.sd.lib.gesture.tag.FTagHolder;
import com.sd.lib.gesture.tag.TagHolder;

public class FGestureManager
{
    private final FTouchHelper mTouchHelper = new FTouchHelper();
    private final FTagHolder mTagHolder = new FTagHolder();

    private VelocityTracker mVelocityTracker;
    private boolean mHasConsumeEvent = false;

    private boolean mCancelTouchEvent = false;

    private final Callback mCallback;

    public FGestureManager(Callback callback)
    {
        if (callback == null)
            throw new NullPointerException("callback is null");
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
        mCancelTouchEvent = true;
        mTagHolder.reset();
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

        boolean tagIntercept = false;

        final int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
        {
            onEventFinish(event);
        } else
        {
            if (action == MotionEvent.ACTION_DOWN)
                onEventStart(event);

            tagIntercept = mCallback.shouldInterceptEvent(event);
        }

        if (mCancelTouchEvent)
            tagIntercept = false;

        mTagHolder.setTagIntercept(tagIntercept);
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

        boolean tagConsume = false;

        final int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
        {
            onEventFinish(event);
        } else if (action == MotionEvent.ACTION_DOWN)
        {
            onEventStart(event);
            return mCallback.onEventActionDown(event);
        } else
        {
            if (mTagHolder.isTagConsume())
            {
                final boolean consume = mCallback.onEventConsume(event);

                // 标识消费过事件
                if (consume)
                    mHasConsumeEvent = true;

                tagConsume = consume;
            } else
            {
                tagConsume = mCallback.shouldConsumeEvent(event);
            }
        }

        if (mCancelTouchEvent)
            tagConsume = false;

        mTagHolder.setTagConsume(tagConsume);
        return mTagHolder.isTagConsume();
    }

    private void onEventStart(MotionEvent event)
    {
        mCancelTouchEvent = false;
    }

    private void onEventFinish(MotionEvent event)
    {
        mTagHolder.reset();
        mCallback.onEventFinish(mHasConsumeEvent, getVelocityTracker(), event);
        mHasConsumeEvent = false;
        releaseVelocityTracker();
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
         * @param hasConsumeEvent 本次按下到结束的过程中{@link #onEventConsume(MotionEvent)}方法是否消费过事件
         * @param velocityTracker 这里返回的对象还未进行速率计算，如果要获得速率需要先进行计算{@link VelocityTracker#computeCurrentVelocity(int)}
         * @param event
         */
        public abstract void onEventFinish(boolean hasConsumeEvent, VelocityTracker velocityTracker, MotionEvent event);
    }
}

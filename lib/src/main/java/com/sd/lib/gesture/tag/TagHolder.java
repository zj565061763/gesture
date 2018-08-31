package com.sd.lib.gesture.tag;

/**
 * 标识持有者
 */
public interface TagHolder
{
    /**
     * 设置回调
     *
     * @param callback
     */
    void setCallback(Callback callback);

    /**
     * 是否需要拦截事件标识(用于onInterceptTouchEvent方法)
     *
     * @return
     */
    boolean isTagIntercept();

    /**
     * 是否需要消费事件标识(用于onTouchEvent方法)
     *
     * @return
     */
    boolean isTagConsume();

    interface Callback
    {
        void onTagInterceptChanged(boolean tag);

        void onTagConsumeChanged(boolean tag);
    }
}

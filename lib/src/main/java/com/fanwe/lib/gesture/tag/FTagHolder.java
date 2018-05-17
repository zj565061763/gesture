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
package com.fanwe.lib.gesture.tag;

public class FTagHolder implements TagHolder
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

    @Override
    public final void setCallback(Callback callback)
    {
        mCallback = callback;
    }

    @Override
    public final boolean isTagIntercept()
    {
        return mTagIntercept;
    }

    @Override
    public final boolean isTagConsume()
    {
        return mTagConsume;
    }

    /**
     * 设置是否需要拦截事件标识(用于onInterceptTouchEvent方法)
     *
     * @param tagIntercept
     */
    public final void setTagIntercept(boolean tagIntercept)
    {
        if (mTagIntercept != tagIntercept)
        {
            mTagIntercept = tagIntercept;
            onTagInterceptChanged(tagIntercept);
            if (mCallback != null) mCallback.onTagInterceptChanged(tagIntercept);
        }
    }

    /**
     * 设置是否需要消费事件标识(用于onTouchEvent方法)
     *
     * @param tagConsume
     */
    public final void setTagConsume(boolean tagConsume)
    {
        if (mTagConsume != tagConsume)
        {
            mTagConsume = tagConsume;
            onTagConsumeChanged(tagConsume);
            if (mCallback != null) mCallback.onTagConsumeChanged(tagConsume);
        }
    }

    /**
     * 重置tag
     */
    public final void reset()
    {
        setTagIntercept(false);
        setTagConsume(false);
    }

    protected void onTagInterceptChanged(boolean tag)
    {
    }

    protected void onTagConsumeChanged(boolean tag)
    {
    }
}

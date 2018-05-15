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

/**
 * 触摸事件处理帮助类<br>
 */
public class FTagTouchHelper extends FTouchHelper
{
    /**
     * 是否需要拦截事件标识(用于onInterceptTouchEvent方法)
     */
    private boolean mTagIntercept = false;
    /**
     * 是否需要消费事件标识(用于onTouchEvent方法)
     */
    private boolean mTagConsume = false;

    @Override
    public void processTouchEvent(MotionEvent ev)
    {
        super.processTouchEvent(ev);

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetTag();
                break;
            default:
                break;
        }
    }

    /**
     * 重置tag
     */
    public void resetTag()
    {
        setTagIntercept(false);
        setTagConsume(false);
    }

    /**
     * 设置是否需要拦截事件标识(用于onInterceptTouchEvent方法)
     *
     * @param tagIntercept
     */
    public void setTagIntercept(boolean tagIntercept)
    {
        mTagIntercept = tagIntercept;
    }

    /**
     * 是否需要拦截事件标识(用于onInterceptTouchEvent方法)
     *
     * @return
     */
    public boolean isTagIntercept()
    {
        return mTagIntercept;
    }

    /**
     * 设置是否需要消费事件标识(用于onTouchEvent方法)
     *
     * @param tagConsume
     */
    public void setTagConsume(boolean tagConsume)
    {
        mTagConsume = tagConsume;
    }

    /**
     * 是否需要消费事件标识(用于onTouchEvent方法)
     *
     * @return
     */
    public boolean isTagConsume()
    {
        return mTagConsume;
    }
}

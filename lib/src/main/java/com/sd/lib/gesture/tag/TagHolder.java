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

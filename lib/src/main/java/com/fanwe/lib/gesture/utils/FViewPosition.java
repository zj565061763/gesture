package com.fanwe.lib.gesture.utils;

import android.view.View;

public class FViewPosition
{
    private int mLeft;
    private int mTop;

    public boolean save(View view)
    {
        if (view == null)
        {
            return false;
        }
        mLeft = view.getLeft();
        mTop = view.getTop();
        return true;
    }

    public int getLeft()
    {
        return mLeft;
    }

    public int getTop()
    {
        return mTop;
    }

    public boolean hasPosition()
    {
        return mLeft >= 0 && mTop >= 0;
    }

    public void reset()
    {
        mLeft = -1;
        mTop = -1;
    }

    public boolean layout(View view)
    {
        if (view == null)
        {
            return false;
        }
        if (!hasPosition())
        {
            return false;
        }

        view.layout(mLeft, mTop, mLeft + view.getWidth(), mTop + view.getHeight());
        return true;
    }
}

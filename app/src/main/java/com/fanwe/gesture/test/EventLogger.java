package com.fanwe.gesture.test;

import android.util.Log;
import android.view.MotionEvent;

public class EventLogger
{
    private static final String TAG = EventLogger.class.getSimpleName();

    public static void i(String msg, MotionEvent event)
    {
        Log.i(TAG, getLogString(msg, event));
    }

    public static void e(String msg, MotionEvent event)
    {
        Log.e(TAG, getLogString(msg, event));
    }

    private static String getLogString(String msg, MotionEvent event)
    {
        String eventString = "";
        if (event != null)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    eventString = "ACTION_DOWN";
                    break;
                case MotionEvent.ACTION_MOVE:
                    eventString = "ACTION_MOVE";
                    break;
                case MotionEvent.ACTION_UP:
                    eventString = "ACTION_UP";
                    break;
                case MotionEvent.ACTION_CANCEL:
                    eventString = "ACTION_CANCEL";
                    break;
                default:
                    eventString = "ACTION_DEFAULT:" + event.getAction();
                    break;
            }
        }
        return msg + " " + eventString;
    }

}

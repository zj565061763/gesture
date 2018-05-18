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
            final int actionMasked = event.getActionMasked();
            final int actionIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(actionIndex);

            switch (actionMasked)
            {
                case MotionEvent.ACTION_DOWN:
                    eventString = "ACTION_DOWN " + actionIndex + " " + pointerId + " (" + event.getX() + "," + event.getY() + ")";
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    eventString = "ACTION_POINTER_DOWN " + actionIndex + " " + pointerId + " (" + event.getX() + "," + event.getY() + ")";
                    break;
                case MotionEvent.ACTION_MOVE:
                    eventString = "ACTION_MOVE " + actionIndex + " " + pointerId + " (" + event.getX() + "," + event.getY() + ")";
                    break;
                case MotionEvent.ACTION_UP:
                    eventString = "ACTION_UP " + actionIndex + " " + pointerId + " (" + event.getX() + "," + event.getY() + ")";
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    eventString = "ACTION_POINTER_UP " + actionIndex + " " + pointerId + " (" + event.getX() + "," + event.getY() + ")";
                    break;
                case MotionEvent.ACTION_CANCEL:
                    eventString = "ACTION_CANCEL " + actionIndex + " " + pointerId + " (" + event.getX() + "," + event.getY() + ")";
                    break;
                default:
                    eventString = "ACTION_DEFAULT:" + actionMasked + " " + pointerId + " (" + event.getX() + "," + event.getY() + ")";
                    break;
            }
        }
        return msg + " " + eventString;
    }

}

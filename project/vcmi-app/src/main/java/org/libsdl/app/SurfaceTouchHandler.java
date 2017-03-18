package org.libsdl.app;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

import eu.vcmi.vcmi.settings.PointerModeSettingController;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.SharedPrefs;

/**
 * @author F
 */
class SurfaceTouchHandler implements View.OnTouchListener
{
    private static final int MOUSE_BTN_LEFT = 1;
    private static final int MOUSE_BTN_RIGHT = 3;
    private final boolean mPointerRelativeMode;
    /**
     * last ACTION_DOWN touch position, needed for relative pointer mode calculations
     */
    private final TouchPoint mCachedTouchPos = new TouchPoint();
    /**
     * last remembered real pointer position, needed for relative pointer mode calculations
     */
    private final TouchPoint mCachedRealPos = new TouchPoint();
    private final TouchPoint mCurrentPos = new TouchPoint();
    /**
     * used to disable main pointer UP action after we sent 2-finger right-click to prevent accidental left click afterwards
     */
    private boolean mSecondaryPointerActive = false;
    private int mWidth;
    private int mHeight;
    private float mRelativeSpeedMultiplier;

    SurfaceTouchHandler(final Context context)
    {
        final SharedPrefs prefs = new SharedPrefs(context);
        mPointerRelativeMode = prefs.loadEnum(SharedPrefs.KEY_POINTER_MODE, PointerModeSettingController.PointerMode.NORMAL)
                               == PointerModeSettingController.PointerMode.RELATIVE;
        mRelativeSpeedMultiplier = 2.0f;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (mWidth <= 0 || mHeight <= 0)
        {
            return false; // surface not ready yet, drop the event
        }

        final int touchDevId = event.getDeviceId();
        final int action = event.getActionMasked();

        switch (action)
        {
            case MotionEvent.ACTION_MOVE:
                if (mSecondaryPointerActive)
                {
                    break;
                }
                sendTouchEvent(event, touchDevId, action, MOUSE_BTN_LEFT);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() < 3) // ignore 3+ finger taps, only check for doubles
                {
                    mSecondaryPointerActive = false;
                    sendTouchEvent(event, touchDevId, MotionEvent.ACTION_UP, MOUSE_BTN_RIGHT);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() < 3) // ignore 3+ finger taps, only check for doubles
                {
                    mSecondaryPointerActive = true;
                    sendTouchEvent(event, touchDevId, MotionEvent.ACTION_DOWN, MOUSE_BTN_RIGHT);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:
                if (mSecondaryPointerActive)
                {
                    break;
                }

                sendTouchEvent(event, touchDevId, action, MOUSE_BTN_LEFT);
                break;

            case MotionEvent.ACTION_CANCEL:
                mSecondaryPointerActive = false;
                sendTouchEvent(event, touchDevId, MotionEvent.ACTION_UP, MOUSE_BTN_LEFT);
                break;

            default:
                break;
        }

        return true;
    }

    private void sendTouchEvent(final MotionEvent event, final int touchDevId, final int action, final int pressedButton)
    {
        mCurrentPos.mX = event.getX(0) / mWidth;
        mCurrentPos.mY = event.getY(0) / mHeight;
        handleRelativeModeTouch(action, pressedButton);
        SDLActivity.onNativeTouch(touchDevId, event.getPointerId(0), action, mCurrentPos.mX, mCurrentPos.mY, pressure(event), pressedButton);
    }

    private void handleRelativeModeTouch(final int action, final int pressedButton)
    {
        if (!mPointerRelativeMode)
        {
            return;
        }

        if (action == MotionEvent.ACTION_DOWN && pressedButton == MOUSE_BTN_LEFT)
        {
            final int cursorPos[] = new int[2];
            retrieveCursorPositions(cursorPos);
            Log.v("retrieved cursor position for relative mode: " + Arrays.toString(cursorPos));
            mCachedRealPos.mX = (float) cursorPos[0] / mWidth;
            mCachedRealPos.mY = (float) cursorPos[1] / mHeight;
        }

        if (pressedButton == MOUSE_BTN_RIGHT)
        {
            modifyCurrentPositionForRelativeMode();
        }
        else if (action == MotionEvent.ACTION_DOWN)
        {
            mCachedTouchPos.mX = mCurrentPos.mX;
            mCachedTouchPos.mY = mCurrentPos.mY;
            mCurrentPos.mX = mCachedRealPos.mX;
            mCurrentPos.mY = mCachedRealPos.mY;
        }
        else if (pressedButton == MOUSE_BTN_LEFT)
        {
            modifyCurrentPositionForRelativeMode();
        }
    }

    /**
     * updates current desired cursor position taking relative mode calculations into account
     */
    private void modifyCurrentPositionForRelativeMode()
    {
        mCurrentPos.mX = mCachedRealPos.mX + (mCurrentPos.mX - mCachedTouchPos.mX) * mRelativeSpeedMultiplier;
        mCurrentPos.mY = mCachedRealPos.mY + (mCurrentPos.mY - mCachedTouchPos.mY) * mRelativeSpeedMultiplier;
    }

    private float pressure(final MotionEvent ev)
    {
        final float p = ev.getPressure();
        if (p < 1.0f)
        {
            return p;
        }
        return 1.0f;
    }

    void onSurfaceUpdated(final int width, final int height)
    {
        mWidth = width;
        mHeight = height;
    }

    private native void retrieveCursorPositions(final int[] outValues);

    private static class TouchPoint
    {
        float mX;
        float mY;
    }
}

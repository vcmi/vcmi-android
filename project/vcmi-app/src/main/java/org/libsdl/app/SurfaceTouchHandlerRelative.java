package org.libsdl.app;

import android.view.MotionEvent;

import java.util.Arrays;

import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public class SurfaceTouchHandlerRelative extends SurfaceTouchHandler
{
    /**
     * last ACTION_DOWN touch position, needed for relative pointer mode calculations
     */
    private final TouchPoint mCachedTouchPos = new TouchPoint();
    /**
     * last remembered real pointer position, needed for relative pointer mode calculations
     */
    private final TouchPoint mCachedRealPos = new TouchPoint();

    private float mRelativeSpeedMultiplier;

    protected SurfaceTouchHandlerRelative()
    {
        mRelativeSpeedMultiplier = 2.0f;
    }

    private void handleRelativeModeTouch(final int action, final int pressedButton)
    {
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

    @Override
    protected void sendTouchEventInternal(final MotionEvent event, final int touchDevId, final int action, final int pressedButton)
    {
        handleRelativeModeTouch(action, pressedButton);
        super.sendTouchEventInternal(event, touchDevId, action, pressedButton);
    }
}

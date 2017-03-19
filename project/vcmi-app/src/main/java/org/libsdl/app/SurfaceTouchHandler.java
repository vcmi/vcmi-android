package org.libsdl.app;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import eu.vcmi.vcmi.settings.PointerModeSettingController;
import eu.vcmi.vcmi.util.SharedPrefs;

/**
 * @author F
 */
class SurfaceTouchHandler implements View.OnTouchListener
{
    protected static final int MOUSE_BTN_LEFT = 1;
    protected static final int MOUSE_BTN_RIGHT = 3;
    protected final TouchPoint mCurrentPos = new TouchPoint();
    protected int mWidth;
    protected int mHeight;
    /**
     * used to disable main pointer UP action after we sent 2-finger right-click to prevent accidental left click afterwards
     */
    private boolean mSecondaryPointerActive = false;

    protected SurfaceTouchHandler()
    {
    }

    public static SurfaceTouchHandler createSurfaceTouchHandler(final Context ctx)
    {
        final SharedPrefs prefs = new SharedPrefs(ctx);
        final boolean relativeMode = prefs.loadEnum(SharedPrefs.KEY_POINTER_MODE, PointerModeSettingController.PointerMode.NORMAL)
                                     == PointerModeSettingController.PointerMode.RELATIVE;
        if (relativeMode)
        {
            return new SurfaceTouchHandlerRelative(prefs);
        }
        return new SurfaceTouchHandler();
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
        sendTouchEventInternal(event, touchDevId, action, pressedButton);
    }

    protected void sendTouchEventInternal(final MotionEvent event, final int touchDevId, final int action, final int pressedButton)
    {
        SDLActivity.onNativeTouch(touchDevId, event.getPointerId(0), action, mCurrentPos.mX, mCurrentPos.mY, pressure(event), pressedButton);
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

    protected native void retrieveCursorPositions(final int[] outValues);

    protected static class TouchPoint
    {
        float mX;
        float mY;
    }
}

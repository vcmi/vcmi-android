package org.libsdl.app;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author F
 */
class SurfaceTouchHandler implements View.OnTouchListener
{
    private static final int MOUSE_BTN_LEFT = 1;
    private static final int MOUSE_BTN_RIGHT = 3;
    /**
     * used to disable main pointer UP action after we sent 2-finger right-click to prevent accidental left click afterwards
     */
    private boolean mIgnoreActionUp = false;

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        final int touchDevId = event.getDeviceId();
        final int action = event.getActionMasked();

        switch (action)
        {
            case MotionEvent.ACTION_MOVE:
                sendTouchEvent(event, touchDevId, action, MOUSE_BTN_LEFT);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() < 3) // ignore 3+ finger taps, only check for doubles
                {
                    mIgnoreActionUp = false;
                    sendTouchEvent(event, touchDevId, MotionEvent.ACTION_UP, MOUSE_BTN_RIGHT);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() < 3) // ignore 3+ finger taps, only check for doubles
                {
                    mIgnoreActionUp = true;
                    sendTouchEvent(event, touchDevId, MotionEvent.ACTION_DOWN, MOUSE_BTN_RIGHT);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIgnoreActionUp)
                {
                    break;
                }
                // fallthrough
            case MotionEvent.ACTION_DOWN:
                sendTouchEvent(event, touchDevId, action, MOUSE_BTN_LEFT);
                break;

            case MotionEvent.ACTION_CANCEL:
                mIgnoreActionUp = false;
                sendTouchEvent(event, touchDevId, MotionEvent.ACTION_UP, MOUSE_BTN_LEFT);
                break;

            default:
                break;
        }

        return true;
    }

    private void sendTouchEvent(final MotionEvent event, final int touchDevId, final int action, final int pressedButton)
    {
        final float x = event.getX(0) / SDLSurface.mWidth;
        final float y = event.getY(0) / SDLSurface.mHeight;
        SDLActivity.onNativeTouch(touchDevId, event.getPointerId(0), action, x, y, pressure(event), pressedButton);
    }

    private float pressure(final MotionEvent ev)
    {
        final float pressure = ev.getPressure();
        if (pressure < 1.0f)
        {
            return pressure;
        }
        return 1.0f;
    }
}

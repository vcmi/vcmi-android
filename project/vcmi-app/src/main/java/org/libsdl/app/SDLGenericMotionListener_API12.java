package org.libsdl.app;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

class SDLGenericMotionListener_API12 implements View.OnGenericMotionListener
{
    private float x;
    private float y;

    // Generic Motion (mouse hover, joystick...) events go here
    @Override
    public boolean onGenericMotion(View v, MotionEvent event)
    {
        int action;

        switch (event.getSource())
        {
            case InputDevice.SOURCE_JOYSTICK:
            case InputDevice.SOURCE_GAMEPAD:
            case InputDevice.SOURCE_DPAD:
                return SDLActivity.handleJoystickMotionEvent(event);

            case InputDevice.SOURCE_MOUSE:
                action = event.getActionMasked();
                switch (action)
                {
                    case MotionEvent.ACTION_SCROLL:
                        x = event.getAxisValue(MotionEvent.AXIS_HSCROLL, 0);
                        y = event.getAxisValue(MotionEvent.AXIS_VSCROLL, 0);
                        SDLActivity.onNativeMouse(0, action, x, y);
                        return true;

                    case MotionEvent.ACTION_HOVER_MOVE:
                        x = event.getX(0);
                        y = event.getY(0);

                        SDLActivity.onNativeMouse(0, action, x, y);
                        return true;

                    default:
                        break;
                }
                break;

            default:
                break;
        }

        // Event was not managed
        return false;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }
}

package org.libsdl.app;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import eu.vcmi.vcmi.NativeMethods;
import eu.vcmi.vcmi.util.Log;

/**
 * SDLSurface. This is what we draw on, so we need to know when it's created in order to do anything useful.
 * <p>
 * Because of this, that's where we set up the SDL thread
 */
class SDLSurface extends SurfaceView implements SurfaceHolder.Callback, View.OnKeyListener, SensorEventListener
{
    // Sensors
    protected static SensorManager mSensorManager;
    protected static Display mDisplay;
    private SurfaceTouchHandler mSurfaceTouchHandler;

    // Startup
    public SDLSurface(Context context)
    {
        super(context);
        getHolder().addCallback(this);
        mSurfaceTouchHandler = SurfaceTouchHandler.createSurfaceTouchHandler(context);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(mSurfaceTouchHandler);

        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        setOnGenericMotionListener(new SDLGenericMotionListener_API12());
    }

    public void handlePause()
    {
        enableSensor(Sensor.TYPE_ACCELEROMETER, false);
    }

    public void handleResume()
    {
        if (mSurfaceTouchHandler == null)
        {
            mSurfaceTouchHandler = SurfaceTouchHandler.createSurfaceTouchHandler(getContext());
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(mSurfaceTouchHandler);
        enableSensor(Sensor.TYPE_ACCELEROMETER, true);
    }

    public Surface getNativeSurface()
    {
        return getHolder().getSurface();
    }

    // Called when we have a valid drawing surface
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.v(this, "surfaceCreated()");
    }

    // Called when we lose the surface
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.v(this, "surfaceDestroyed()");
        // Call this *before* setting mIsSurfaceReady to 'false'
        handlePause();
        SDLActivity.mIsSurfaceReady = false;
        SDLActivity.onNativeSurfaceDestroyed();
    }


    // Called when the surface is resized
    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height)
    {
        Log.v(this, "surfaceChanged()");

        int sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565 by default
        switch (format)
        {
            case PixelFormat.A_8:
                Log.v(this, "pixel format A_8");
                break;
            case PixelFormat.LA_88:
                Log.v(this, "pixel format LA_88");
                break;
            case PixelFormat.L_8:
                Log.v(this, "pixel format L_8");
                break;
            case PixelFormat.RGBA_4444:
                Log.v(this, "pixel format RGBA_4444");
                sdlFormat = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
                break;
            case PixelFormat.RGBA_5551:
                Log.v(this, "pixel format RGBA_5551");
                sdlFormat = 0x15441002; // SDL_PIXELFORMAT_RGBA5551
                break;
            case PixelFormat.RGBA_8888:
                Log.v(this, "pixel format RGBA_8888");
                sdlFormat = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
                break;
            case PixelFormat.RGBX_8888:
                Log.v(this, "pixel format RGBX_8888");
                sdlFormat = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
                break;
            case PixelFormat.RGB_332:
                Log.v(this, "pixel format RGB_332");
                sdlFormat = 0x14110801; // SDL_PIXELFORMAT_RGB332
                break;
            case PixelFormat.RGB_565:
                Log.v(this, "pixel format RGB_565");
                sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565
                break;
            case PixelFormat.RGB_888:
                Log.v(this, "pixel format RGB_888");
                // Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
                sdlFormat = 0x16161804; // SDL_PIXELFORMAT_RGB888
                break;
            default:
                Log.v(this, "pixel format unknown " + format);
                break;
        }

        mSurfaceTouchHandler.onSurfaceUpdated(width, height);
        SDLActivity.onNativeResize(width, height, sdlFormat, mDisplay.getRefreshRate());
        Log.v(this, "Window size: " + width + "x" + height);


        boolean skip = false;
        SDLActivity singleton = SDLActivity.mHolder.singleton();
        if (singleton == null)
        {
            Log.w(this, "Lost context ref");
            return;
        }
        int requestedOrientation = singleton.getRequestedOrientation();

        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        {
            // Accept any
        }
        else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            if (width > height)
            {
                skip = true;
            }
        }
        else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        {
            if (width < height)
            {
                skip = true;
            }
        }

        // Special Patch for Square Resolution: Black Berry Passport
        if (skip)
        {
            double min = Math.min(width, height);
            double max = Math.max(width, height);

            if (max / min < 1.20)
            {
                Log.v(this, "Don't skip on such aspect-ratio. Could be a square resolution.");
                skip = false;
            }
        }

        if (skip)
        {
            Log.v(this, "Skip .. Surface is not ready.");
            return;
        }


        // Set mIsSurfaceReady to 'true' *before* making a call to handleResume
        SDLActivity.mIsSurfaceReady = true;
        SDLActivity.onNativeSurfaceChanged();


        if (SDLActivity.mSDLThread == null)
        {
            // This is the entry point to the C app.
            // Start up the C app thread and enable sensor input for the first time

            final Thread sdlThread = new Thread(new SDLMain(), "SDLThread");
            enableSensor(Sensor.TYPE_ACCELEROMETER, true);
            sdlThread.start();

            // Set up a listener thread to catch when the native thread ends
            SDLActivity.mSDLThread = new Thread(() -> sdlThreadFinishListener(sdlThread), "SDLThreadListener");
            SDLActivity.mSDLThread.start();
        }

        if (SDLActivity.mHasFocus)
        {
            SDLActivity.handleResume();
        }
    }

    private void sdlThreadFinishListener(final Thread sdlThread)
    {
        try
        {
            sdlThread.join();
        }
        catch (Exception e)
        {
        }
        finally
        {
            // Native thread has finished
            if (!SDLActivity.mExitCalledFromJava)
            {
                SDLActivity.handleNativeExit();
            }
        }
    }

    // Key events
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        // Dispatch the different events depending on where they come from
        // Some SOURCE_JOYSTICK, SOURCE_DPAD or SOURCE_GAMEPAD are also SOURCE_KEYBOARD
        // So, we try to process them as JOYSTICK/DPAD/GAMEPAD events first, if that fails we try them as KEYBOARD
        //
        // Furthermore, it's possible a game controller has SOURCE_KEYBOARD and
        // SOURCE_JOYSTICK, while its key events arrive from the keyboard source
        // So, retrieve the device itself and check all of its sources
        if (SDLActivity.isDeviceSDLJoystick(event.getDeviceId()))
        {
            // Note that we process events with specific key codes here
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                if (SDLActivity.onNativePadDown(event.getDeviceId(), keyCode) == 0)
                {
                    return true;
                }
            }
            else if (event.getAction() == KeyEvent.ACTION_UP)
            {
                if (SDLActivity.onNativePadUp(event.getDeviceId(), keyCode) == 0)
                {
                    return true;
                }
            }
        }

        if ((event.getSource() & InputDevice.SOURCE_KEYBOARD) != 0)
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                SDLActivity.onNativeKeyDown(keyCode);
                return true;
            }
            else if (event.getAction() == KeyEvent.ACTION_UP)
            {
                SDLActivity.onNativeKeyUp(keyCode);
                return true;
            }
        }

        if ((event.getSource() & InputDevice.SOURCE_MOUSE) != 0)
        {
            // on some devices key events are sent for mouse BUTTON_BACK/FORWARD presses
            // they are ignored here because sending them as mouse input to SDL is messy
            if ((keyCode == KeyEvent.KEYCODE_BACK) || (keyCode == KeyEvent.KEYCODE_FORWARD))
            {
                switch (event.getAction())
                {
                    case KeyEvent.ACTION_DOWN:
                    case KeyEvent.ACTION_UP:
                        // mark the event as handled or it will be handled by system
                        // handling KEYCODE_BACK by system will call onBackPressed()
                        return true;
                }
            }
        }

        return false;
    }

    // Sensor events
    public void enableSensor(int sensortype, boolean enabled)
    {
        // TODO: This uses getDefaultSensor - what if we have >1 accels?
        if (enabled)
        {
            mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(sensortype),
                SensorManager.SENSOR_DELAY_GAME, null);
        }
        else
        {
            mSensorManager.unregisterListener(this,
                mSensorManager.getDefaultSensor(sensortype));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // TODO
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float x, y;
            switch (mDisplay.getRotation())
            {
                case Surface.ROTATION_90:
                    x = -event.values[1];
                    y = event.values[0];
                    break;
                case Surface.ROTATION_270:
                    x = event.values[1];
                    y = -event.values[0];
                    break;
                case Surface.ROTATION_180:
                    x = -event.values[1];
                    y = -event.values[0];
                    break;
                default:
                    x = event.values[0];
                    y = event.values[1];
                    break;
            }
            SDLActivity.onNativeAccel(-x / SensorManager.GRAVITY_EARTH,
                y / SensorManager.GRAVITY_EARTH,
                event.values[2] / SensorManager.GRAVITY_EARTH);
        }
    }

    /**
     * Simple nativeInit() runnable
     */
    private class SDLMain implements Runnable
    {
        @Override
        public void run()
        {
            SDLActivity.nativeInit(SDLActivity.mHolder.singleton().getArguments());
            NativeMethods.initClassloader();
        }
    }
}

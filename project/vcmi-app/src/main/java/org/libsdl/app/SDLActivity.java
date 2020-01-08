package org.libsdl.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;

import eu.vcmi.vcmi.ActivityBase;
import eu.vcmi.vcmi.NativeMethods;
import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.ServerService;
import eu.vcmi.vcmi.util.LibsLoader;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.Utils;

/**
 * SDL Activity
 */
public class SDLActivity extends ActivityBase
{
    public static final int SERVER_MESSAGE_SERVER_READY = 1000;
    public static final int SERVER_MESSAGE_SERVER_KILLED = 1001;
    public static final String NATIVE_ACTION_CREATE_SERVER = "SDLActivity.Action.CreateServer";
    protected static final int COMMAND_USER = 0x8000;
    // Messages from the SDLMain thread
    static final int COMMAND_CHANGE_TITLE = 1;
    static final int COMMAND_UNUSED = 2;
    static final int COMMAND_TEXTEDIT_HIDE = 3;
    static final int COMMAND_SET_KEEP_SCREEN_ON = 5;
    // Keep track of the paused state
    public static boolean mIsPaused, mIsSurfaceReady, mHasFocus;
    public static boolean mExitCalledFromJava;
    /**
     * If shared libraries (e.g. SDL or the native application) could not be loaded.
     */
    public static boolean mBrokenLibraries;
    // If we want to separate mouse and touch events.
    //  This is only toggled in native code when a hint is set!
    public static boolean mSeparateMouseAndTouch;
    // Main components
    protected static SDLJoystickHandler mJoystickHandler;
    // This is what SDL runs in. It invokes SDL_main(), eventually
    protected static Thread mSDLThread;
    // Audio
    protected static AudioTrack mAudioTrack;
    protected static AudioRecord mAudioRecord;

    static ActivityHolder mHolder = new ActivityHolder();
    /**
     * Result of current messagebox. Also used for blocking the calling thread.
     */
    protected final int[] messageboxSelection = new int[1];
    final Messenger mClientMessenger = new Messenger(new IncomingServerMessageHandler(new OnServerRegisteredCallback()));
    /**
     * Id of current dialog.
     */
    protected int dialogs = 0;
    // Handler for the messages
    Handler commandHandler = new SDLCommandHandler();
    Messenger mServiceMessenger = null;
    boolean mIsServerServiceBound;
    /**
     * com.android.vending.expansion.zipfile.ZipResourceFile object or null.
     */
    private Object expansionFile;
    /**
     * com.android.vending.expansion.zipfile.ZipResourceFile's getInputStream() or null.
     */
    private Method expansionFileMethod;
    private ServiceConnection mServerServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className,
                                       IBinder service)
        {
            Log.i(this, "Service connection");
            mServiceMessenger = new Messenger(service);
            mIsServerServiceBound = true;

            try
            {
                Message msg = Message.obtain(null, ServerService.CLIENT_MESSAGE_CLIENT_REGISTERED);
                msg.replyTo = mClientMessenger;
                mServiceMessenger.send(msg);
            }
            catch (RemoteException ignored)
            {
            }
        }

        public void onServiceDisconnected(ComponentName className)
        {
            Log.i(this, "Service disconnection");
            mServiceMessenger = null;
        }
    };
    private View mProgressBar;

    public static void initialize()
    {
        // The static nature of the singleton and Android quirkyness force us to initialize everything here
        // Otherwise, when exiting the app and returning to it, these variables *keep* their pre exit values
        mHolder = new ActivityHolder();
        mJoystickHandler = null;
        mSDLThread = null;
        mAudioTrack = null;
        mAudioRecord = null;
        mExitCalledFromJava = false;
        mBrokenLibraries = false;
        mIsPaused = false;
        mIsSurfaceReady = false;
        mHasFocus = true;
    }

    /**
     * Called by onResume or surfaceCreated. An actual resume should be done only when the surface is ready. Note: Some Android variants may send
     * multiple surfaceChanged events, so we don't need to resume every time we get one of those events, only if it comes after surfaceDestroyed
     */
    public static void handleResume()
    {
        if (SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady && SDLActivity.mHasFocus)
        {
            SDLActivity.mIsPaused = false;
            SDLActivity.nativeResume();
            final SDLSurface surface = mHolder.surface();
            if (surface != null)
            {
                surface.handleResume();
            }
        }
    }

    // C functions we call
    public static native int nativeInit(Object[] arguments);

    public static native void nativeLowMemory();

    public static native void nativeQuit();

    public static native void nativePause();

    public static native void nativeResume();

    public static native void onNativeDropFile(String filename);

    public static native void onNativeResize(int x, int y, int format, float rate);

    public static native int onNativePadDown(int device_id, int keycode);

    public static native int onNativePadUp(int device_id, int keycode);

    public static native void onNativeJoy(int device_id, int axis,
                                          float value);

    public static native void onNativeHat(int device_id, int hat_id,
                                          int x, int y);

    public static native void onNativeKeyDown(int keycode);

    public static native void onNativeKeyUp(int keycode);

    public static native void onNativeKeyboardFocusLost();

    public static native void onNativeMouse(int button, int action, float x, float y);

    public static native void onNativeTouch(int touchDevId, int pointerFingerId,
                                            int action, float x,
                                            float y, float p, int mouseButtonIndex);

    public static native void onNativeAccel(float x, float y, float z);

    public static native void onNativeSurfaceChanged();

    public static native void onNativeSurfaceDestroyed();

    public static native int nativeAddJoystick(int device_id, String name,
                                               int is_accelerometer, int nbuttons,
                                               int naxes, int nhats, int nballs);

    public static native int nativeRemoveJoystick(int device_id);

    public static native String nativeGetHint(String name);

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean setActivityTitle(String title)
    {
        final SDLActivity singleton = mHolder.singleton();
        if (singleton == null)
        {
            return false;
        }
        // Called from SDLMain() thread and can't directly affect the view
        return singleton.sendCommand(COMMAND_CHANGE_TITLE, title);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean sendMessage(int command, int param)
    {
        final SDLActivity singleton = mHolder.singleton();
        if (singleton == null)
        {
            return false;
        }
        return singleton.sendCommand(command, Integer.valueOf(param));
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static Context getContext()
    {
        return mHolder.singleton();
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean showTextInput(int x, int y, int w, int h)
    {
        final SDLActivity singleton = mHolder.singleton();
        if (singleton == null)
        {
            return false;
        }
        // Transfer the task to the main thread as a Runnable
        return singleton.commandHandler.post(new ShowTextInputTask(x, y, w, h));
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static Surface getNativeSurface()
    {
        final SDLSurface surface = mHolder.surface();
        if (surface == null)
        {
            return null;
        }
        return surface.getNativeSurface();
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static int audioOpen(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames)
    {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

        Log.v("SDL audio: wanted "
              + (isStereo ? "stereo" : "mono")
              + " "
              + (is16Bit ? "16-bit" : "8-bit")
              + " "
              + (sampleRate / 1000f)
              + "kHz, "
              + desiredFrames
              + " frames buffer");

        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);

        if (mAudioTrack == null)
        {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                channelConfig, audioFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM);

            // Instantiating AudioTrack can "succeed" without an exception and the track may still be invalid
            // Ref: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/AudioTrack.java
            // Ref: http://developer.android.com/reference/android/media/AudioTrack.html#getState()

            if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED)
            {
                Log.e("Failed during initialization of Audio Track");
                mAudioTrack = null;
                return -1;
            }

            mAudioTrack.play();
        }

        Log.v("SDL audio: got "
              + ((mAudioTrack.getChannelCount() >= 2) ? "stereo" : "mono")
              + " "
              + ((mAudioTrack.getAudioFormat()
                  == AudioFormat.ENCODING_PCM_16BIT)
                 ? "16-bit"
                 : "8-bit")
              + " "
              + (mAudioTrack.getSampleRate()
                 / 1000f)
              + "kHz, "
              + desiredFrames
              + " frames buffer");

        return 0;
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioWriteShortBuffer(short[] buffer)
    {
        if (mAudioTrack == null)
        {
            return;
        }
        for (int i = 0; i < buffer.length; )
        {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0)
            {
                i += result;
            }
            else if (result == 0)
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    // Nom nom
                }
            }
            else
            {
                Log.w("SDL audio: error return from write(short)");
                return;
            }
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioWriteByteBuffer(byte[] buffer)
    {
        for (int i = 0; i < buffer.length; )
        {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0)
            {
                i += result;
            }
            else if (result == 0)
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    // Nom nom
                }
            }
            else
            {
                Log.w("SDL audio: error return from write(byte)");
                return;
            }
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static int captureOpen(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames)
    {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

        Log.v("SDL capture: wanted "
              + (isStereo ? "stereo" : "mono")
              + " "
              + (is16Bit ? "16-bit" : "8-bit")
              + " "
              + (sampleRate / 1000f)
              + "kHz, "
              + desiredFrames
              + " frames buffer");

        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);

        if (mAudioRecord == null)
        {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate,
                channelConfig, audioFormat, desiredFrames * frameSize);

            // see notes about AudioTrack state in audioOpen(), above. Probably also applies here.
            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            {
                Log.e("Failed during initialization of AudioRecord");
                mAudioRecord.release();
                mAudioRecord = null;
                return -1;
            }

            mAudioRecord.startRecording();
        }

        Log.v("SDL capture: got "
              + ((mAudioRecord.getChannelCount() >= 2) ? "stereo" : "mono")
              + " "
              + ((mAudioRecord.getAudioFormat()
                  == AudioFormat.ENCODING_PCM_16BIT)
                 ? "16-bit"
                 : "8-bit")
              + " "
              + (mAudioRecord.getSampleRate()
                 / 1000f)
              + "kHz, "
              + desiredFrames
              + " frames buffer");

        return 0;
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static int captureReadShortBuffer(short[] buffer, boolean blocking)
    {
        // !!! FIXME: this is available in API Level 23. Until then, we always block.  :(
        //return mAudioRecord.read(buffer, 0, buffer.length, blocking ? AudioRecord.READ_BLOCKING : AudioRecord.READ_NON_BLOCKING);
        return mAudioRecord.read(buffer, 0, buffer.length);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static int captureReadByteBuffer(byte[] buffer, boolean blocking)
    {
        // !!! FIXME: this is available in API Level 23. Until then, we always block.  :(
        //return mAudioRecord.read(buffer, 0, buffer.length, blocking ? AudioRecord.READ_BLOCKING : AudioRecord.READ_NON_BLOCKING);
        return mAudioRecord.read(buffer, 0, buffer.length);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioClose()
    {
        if (mAudioTrack != null)
        {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void captureClose()
    {
        if (mAudioRecord != null)
        {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    /**
     * This method is called by SDL using JNI.
     *
     * @return an array which may be empty but is never null.
     */
    public static int[] inputGetInputDeviceIds(int sources)
    {
        int[] ids = InputDevice.getDeviceIds();
        int[] filtered = new int[ids.length];
        int used = 0;
        for (int i = 0; i < ids.length; ++i)
        {
            InputDevice device = InputDevice.getDevice(ids[i]);
            if ((device != null) && ((device.getSources() & sources) != 0))
            {
                filtered[used++] = device.getId();
            }
        }
        return Arrays.copyOf(filtered, used);
    }

    // Joystick glue code, just a series of stubs that redirect to the SDLJoystickHandler instance
    public static boolean handleJoystickMotionEvent(MotionEvent event)
    {
        return mJoystickHandler.handleMotionEvent(event);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void pollInputDevices()
    {
        if (SDLActivity.mSDLThread != null)
        {
            mJoystickHandler.pollInputDevices();
        }
    }

    // Check if a given device is considered a possible SDL joystick
    public static boolean isDeviceSDLJoystick(int deviceId)
    {
        InputDevice device = InputDevice.getDevice(deviceId);
        // We cannot use InputDevice.isVirtual before API 16, so let's accept
        // only nonnegative device ids (VIRTUAL_KEYBOARD equals -1)
        if ((device == null) || (deviceId < 0))
        {
            return false;
        }
        int sources = device.getSources();
        return (((sources & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK) ||
                ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) ||
                ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
        );
    }

    /* The native thread has finished */
    public static void handleNativeExit()
    {
        final SDLActivity singleton = SDLActivity.mHolder.singleton();
        SDLActivity.mSDLThread = null;
        if (singleton != null)
        {
            singleton.finish();
        }
    }

    /**
     * Called by onPause or surfaceDestroyed. Even if surfaceDestroyed is the first to be called, mIsSurfaceReady should still be set to 'true' during
     * the call to onPause (in a usual scenario).
     */
    public void handlePause()
    {
        if (!SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady)
        {
            SDLActivity.mIsPaused = true;
            SDLActivity.nativePause();
            handlePause();
        }
    }

    /**
     * This method is called by SDL before starting the native application thread. It can be overridden to provide the arguments after the application
     * name. The default implementation returns an empty array. It never returns null.
     *
     * @return arguments for the native application.
     */
    protected String[] getArguments()
    {
        return new String[0];
    }

    // Setup
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(this, "Device: " + android.os.Build.DEVICE);
        Log.v(this, "Model: " + android.os.Build.MODEL);
        Log.v(this, "onCreate(): " + mHolder.singleton());
        super.onCreate(savedInstanceState);

        NativeMethods.setupCtx(this);

        SDLActivity.initialize();
        // So we can call stuff from static callbacks
        mHolder.setSingleton(this);

        // Load shared libraries
        String errorMsgBrokenLib = "";
        try
        {
            LibsLoader.loadClientLibs();
        }
        catch (UnsatisfiedLinkError e)
        {
            Log.e(this, "Broken", e);
            mBrokenLibraries = true;
            errorMsgBrokenLib = e.getMessage();
        }
        catch (Exception e)
        {
            Log.e(this, "Broken", e);
            mBrokenLibraries = true;
            errorMsgBrokenLib = e.getMessage();
        }

        if (mBrokenLibraries)
        {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("An error occurred while trying to start the application. Please try again and/or reinstall."
                                + System.getProperty("line.separator")
                                + System.getProperty("line.separator")
                                + "Error: " + errorMsgBrokenLib);
            dlgAlert.setTitle("SDL Error");
            dlgAlert.setPositiveButton("Exit",
                (dialog, id) ->
                {
                    // if this button is clicked, close current activity
                    finish();
                });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();

            return;
        }

        NativeMethods.initClassloader();

        // Set up the surface
        final SDLSurface surface = new SDLSurface(getApplication());
        mHolder.setSurface(surface);

        mJoystickHandler = new SDLJoystickHandler();

        final View outerLayout = getLayoutInflater().inflate(R.layout.activity_game, null, false);
        final ViewGroup layout = (ViewGroup) outerLayout.findViewById(R.id.game_outer_frame);
        mProgressBar = outerLayout.findViewById(R.id.game_progress);
        layout.addView(surface);
        mHolder.setLayout(layout);

        setContentView(outerLayout);
    }

    private void initService()
    {
        unbindServer();
        startService(new Intent(this, ServerService.class));
        bindService(new Intent(SDLActivity.this,
            ServerService.class), mServerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void hackCallNewIntentDirectly(final Intent intent)
    {
        onNewIntent(intent);
    }

    @Override
    protected void onNewIntent(final Intent intent)
    {
        Log.i(this, "Got new intent with action " + intent.getAction());
        if (NATIVE_ACTION_CREATE_SERVER.equals(intent.getAction()))
        {
            initService();
        }
    }

    // Events
    @Override
    protected void onPause()
    {
        Log.v(this, "onPause()");
        super.onPause();

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        handlePause();
    }

    @Override
    protected void onResume()
    {
        Log.v(this, "onResume()");
        super.onResume();

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        SDLActivity.handleResume();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig)
    {
        Log.d(this, "Config changed " + newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        Log.v(this, "onWindowFocusChanged(): " + hasFocus);

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        SDLActivity.mHasFocus = hasFocus;
        if (hasFocus)
        {
            SDLActivity.handleResume();
        }
    }

    @Override
    public void onLowMemory()
    {
        Log.v(this, "onLowMemory()");
        super.onLowMemory();

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        SDLActivity.nativeLowMemory();
    }

    @Override
    protected void onDestroy()
    {
        Log.v(this, "onDestroy()");

        if (SDLActivity.mBrokenLibraries)
        {
            super.onDestroy();
            // Reset everything in case the user re opens the app
            SDLActivity.initialize();
            return;
        }

        try
        {
            // since android can kill the activity unexpectedly (e.g. memory is low or device is inactive for some time), let's try creating
            // an autosave so user might be able to resume the game; this isn't a very good impl (we shouldn't really sleep here and hope that the
            // save is created, but for now it might suffice
            // (better solution: listen for game's confirmation that the save has been created -- this would allow us to inform the users
            // on the next app launch that there is an automatic save that they can use)
            if (NativeMethods.tryToSaveTheGame())
            {
                Thread.sleep(1000L);
            }
        }
        catch (final InterruptedException ignored)
        {
        }

        // Send a quit message to the application
        SDLActivity.mExitCalledFromJava = true;
        SDLActivity.nativeQuit();

        unbindServer();

        // Now wait for the SDL thread to quit
        if (SDLActivity.mSDLThread != null)
        {
            try
            {
                SDLActivity.mSDLThread.join();
            }
            catch (Exception e)
            {
                Log.v(this, "Problem stopping thread: " + e);
            }
            SDLActivity.mSDLThread = null;
        }

        stopService(new Intent(this, ServerService.class));

        super.onDestroy();
        // Reset everything in case the user re opens the app
        SDLActivity.initialize();
    }


    private void unbindServer()
    {
        Log.d(this, "Unbinding server " + mIsServerServiceBound);
        if (mIsServerServiceBound)
        {
            unbindService(mServerServiceConnection);
            mIsServerServiceBound = false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {

        if (SDLActivity.mBrokenLibraries)
        {
            return false;
        }

        int keyCode = event.getKeyCode();
        // Ignore certain special keys so they're handled by Android
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_CAMERA ||
            keyCode == 168 || /* API 11: KeyEvent.KEYCODE_ZOOM_IN */
            keyCode == 169 /* API 11: KeyEvent.KEYCODE_ZOOM_OUT */
            )
        {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * This method is called by SDL if SDL did not handle a message itself. This happens if a received message contains an unsupported command. Method
     * can be overwritten to handle Messages in a different class.
     *
     * @param command
     *     the command of the message.
     * @param param
     *     the parameter of the message. May be null.
     * @return if the message was handled in overridden method.
     */
    protected boolean onUnhandledMessage(int command, Object param)
    {
        return false;
    }

    // Send a message from the SDLMain thread
    boolean sendCommand(int command, Object data)
    {
        Message msg = commandHandler.obtainMessage();
        msg.arg1 = command;
        msg.obj = data;
        return commandHandler.sendMessage(msg);
    }

    /**
     * This method is called by SDL using JNI.
     *
     * @return result of getSystemService(name) but executed on UI thread.
     */
    public Object getSystemServiceFromUiThread(final String name)
    {
        final Object lock = new Object();
        final Object[] results = new Object[2]; // array for writable variables
        synchronized (lock)
        {
            runOnUiThread(() ->
            {
                synchronized (lock)
                {
                    results[0] = getSystemService(name);
                    results[1] = Boolean.TRUE;
                    lock.notify();
                }
            });
            if (results[1] == null)
            {
                try
                {
                    lock.wait();
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        return results[0];
    }

    public InputStream openAPKExpansionInputStream(String fileName) throws IOException
    {
        return null;
    }

    /**
     * This method is called by SDL using JNI. Shows the messagebox from UI thread and block calling thread. buttonFlags, buttonIds and buttonTexts
     * must have same length.
     *
     * @param buttonFlags
     *     array containing flags for every button.
     * @param buttonIds
     *     array containing id for every button.
     * @param buttonTexts
     *     array containing text for every button.
     * @param colors
     *     null for default or array of length 5 containing colors.
     * @return button id or -1.
     */
    public int messageboxShowMessageBox(
        final int flags,
        final String title,
        final String message,
        final int[] buttonFlags,
        final int[] buttonIds,
        final String[] buttonTexts,
        final int[] colors)
    {

        messageboxSelection[0] = -1;

        // sanity checks

        if ((buttonFlags.length != buttonIds.length) && (buttonIds.length != buttonTexts.length))
        {
            return -1; // implementation broken
        }

        // collect arguments for Dialog

        final Bundle args = new Bundle();
        args.putInt("flags", flags);
        args.putString("title", title);
        args.putString("message", message);
        args.putIntArray("buttonFlags", buttonFlags);
        args.putIntArray("buttonIds", buttonIds);
        args.putStringArray("buttonTexts", buttonTexts);
        args.putIntArray("colors", colors);

        // trigger Dialog creation on UI thread

        runOnUiThread(() -> showDialog(dialogs++, args));

        // block the calling thread

        synchronized (messageboxSelection)
        {
            try
            {
                messageboxSelection.wait();
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
                return -1;
            }
        }

        // return selected value

        return messageboxSelection[0];
    }

    @Override
    protected Dialog onCreateDialog(int ignore, Bundle args)
    {

        // TODO set values from "flags" to messagebox dialog

        // get colors

        int[] colors = args.getIntArray("colors");
        int backgroundColor;
        int textColor;
        int buttonBorderColor;
        int buttonBackgroundColor;
        int buttonSelectedColor;
        if (colors != null)
        {
            int i = -1;
            backgroundColor = colors[++i];
            textColor = colors[++i];
            buttonBorderColor = colors[++i];
            buttonBackgroundColor = colors[++i];
            buttonSelectedColor = colors[++i];
        }
        else
        {
            backgroundColor = Color.TRANSPARENT;
            textColor = Color.TRANSPARENT;
            buttonBorderColor = Color.TRANSPARENT;
            buttonBackgroundColor = Color.TRANSPARENT;
            buttonSelectedColor = Color.TRANSPARENT;
        }

        // create dialog with title and a listener to wake up calling thread

        final Dialog dialog = new Dialog(this);
        dialog.setTitle(args.getString("title"));
        dialog.setCancelable(false);
        dialog.setOnDismissListener(unused ->
        {
            synchronized (messageboxSelection)
            {
                messageboxSelection.notify();
            }
        });

        // create text

        TextView message = new TextView(this);
        message.setGravity(Gravity.CENTER);
        message.setText(args.getString("message"));
        if (textColor != Color.TRANSPARENT)
        {
            message.setTextColor(textColor);
        }

        // create buttons

        int[] buttonFlags = args.getIntArray("buttonFlags");
        int[] buttonIds = args.getIntArray("buttonIds");
        String[] buttonTexts = args.getStringArray("buttonTexts");

        final SparseArray<Button> mapping = new SparseArray<Button>();

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        for (int i = 0; i < buttonTexts.length; ++i)
        {
            Button button = new Button(this);
            final int id = buttonIds[i];
            button.setOnClickListener(v ->
            {
                messageboxSelection[0] = id;
                dialog.dismiss();
            });
            if (buttonFlags[i] != 0)
            {
                // see SDL_messagebox.h
                if ((buttonFlags[i] & 0x00000001) != 0)
                {
                    mapping.put(KeyEvent.KEYCODE_ENTER, button);
                }
                if ((buttonFlags[i] & 0x00000002) != 0)
                {
                    mapping.put(111, button); /* API 11: KeyEvent.KEYCODE_ESCAPE */
                }
            }
            button.setText(buttonTexts[i]);
            if (textColor != Color.TRANSPARENT)
            {
                button.setTextColor(textColor);
            }
            if (buttonBorderColor != Color.TRANSPARENT)
            {
                // TODO set color for border of messagebox button
            }
            if (buttonBackgroundColor != Color.TRANSPARENT)
            {
                Drawable drawable = button.getBackground();
                if (drawable == null)
                {
                    // setting the color this way removes the style
                    button.setBackgroundColor(buttonBackgroundColor);
                }
                else
                {
                    // setting the color this way keeps the style (gradient, padding, etc.)
                    drawable.setColorFilter(buttonBackgroundColor, PorterDuff.Mode.MULTIPLY);
                }
            }
            if (buttonSelectedColor != Color.TRANSPARENT)
            {
                // TODO set color for selected messagebox button
            }
            buttons.addView(button);
        }

        // create content

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.addView(message);
        content.addView(buttons);
        if (backgroundColor != Color.TRANSPARENT)
        {
            content.setBackgroundColor(backgroundColor);
        }

        // add content to dialog and return

        dialog.setContentView(content);
        dialog.setOnKeyListener((d, keyCode, event) ->
        {
            Button button = mapping.get(keyCode);
            if (button != null)
            {
                if (event.getAction() == KeyEvent.ACTION_UP)
                {
                    button.performClick();
                }
                return true; // also for ignored actions
            }
            return false;
        });

        return dialog;
    }

    public void displayProgress(final boolean show)
    {
        if (mProgressBar != null)
        {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * called from native code when content of an active text input has changed so that we should update overlay EditText
     * @param textContext current value of the focused ctextinput control
     */
    public static void notifyTextInputChanged(final String textContext)
    {
        if (mHolder == null)
        {
            return;
        }
        final DummyEdit edit = mHolder.edit();
        if (edit == null)
        {
            return;
        }
        edit.notifyContentChanged(textContext);
    }

    // Messagebox

    private interface IncomingServerMessageHandlerCallback
    {
        void unbindServer();
    }

    static final class ActivityHolder
    {
        private WeakReference<SDLActivity> mSingleton;
        private WeakReference<SDLSurface> mSurface;
        private WeakReference<DummyEdit> mTextEdit;
        private WeakReference<ViewGroup> mLayout;

        SDLActivity singleton()
        {
            if (mSingleton == null)
            {
                return null;
            }
            return mSingleton.get();
        }

        SDLSurface surface()
        {
            if (mSurface == null)
            {
                return null;
            }
            return mSurface.get();
        }

        DummyEdit edit()
        {
            if (mTextEdit == null)
            {
                return null;
            }
            return mTextEdit.get();
        }

        ViewGroup layout()
        {
            if (mLayout == null)
            {
                return null;
            }
            return mLayout.get();
        }

        void setSingleton(final SDLActivity singleton)
        {
            mSingleton = new WeakReference<>(singleton);
        }

        void setSurface(final SDLSurface singleton)
        {
            mSurface = new WeakReference<>(singleton);
        }

        void setEdit(final DummyEdit singleton)
        {
            mTextEdit = new WeakReference<>(singleton);
        }

        void setLayout(final ViewGroup singleton)
        {
            mLayout = new WeakReference<>(singleton);
        }
    }

    /**
     * A Handler class for Messages from native SDL applications. It uses current Activities as target (e.g. for the title). static to prevent
     * implicit references to enclosing object.
     */
    protected static class SDLCommandHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            Context context = getContext();
            if (context == null)
            {
                Log.e(this, "error handling message, getContext() returned null");
                return;
            }
            switch (msg.arg1)
            {
                case COMMAND_CHANGE_TITLE:
                    if (context instanceof Activity)
                    {
                        ((Activity) context).setTitle((String) msg.obj);
                    }
                    else
                    {
                        Log.e(this, "error handling message, getContext() returned no Activity");
                    }
                    break;
                case COMMAND_TEXTEDIT_HIDE:
                    final DummyEdit edit = mHolder.edit();
                    if (edit != null)
                    {
                        // Note: On some devices setting view to GONE creates a flicker in landscape.
                        // Setting the View's sizes to 0 is similar to GONE but without the flicker.
                        // The sizes will be set to useful values when the keyboard is shown again.
                        edit.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.mEditText.getWindowToken(), 0);
                    }
                    break;
                case COMMAND_SET_KEEP_SCREEN_ON:
                {
                    Window window = ((Activity) context).getWindow();
                    if (window != null)
                    {
                        if ((msg.obj instanceof Integer) && ((Integer) msg.obj != 0))
                        {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                        else
                        {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                    break;
                }
                default:
                    if ((context instanceof SDLActivity) && !((SDLActivity) context).onUnhandledMessage(msg.arg1, msg.obj))
                    {
                        Log.e(this, "error handling message, command is " + msg.arg1);
                    }
            }
        }
    }

    static class ShowTextInputTask implements Runnable
    {
        /*
         * This is used to regulate the pan&scan method to have some offset from
         * the bottom edge of the input region and the top edge of an input
         * method (soft keyboard)
         */
        static final int HEIGHT_PADDING = 15;

        public int x, y, w, h;

        ShowTextInputTask(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public void run()
        {
            DummyEdit edit = mHolder.edit();
            if (edit == null)
            {
                edit = new DummyEdit(getContext());
                mHolder.setEdit(edit);

                mHolder.layout().addView(edit, createEditLayoutParams(edit.getContext()));
            }
            else
            {
                edit.setLayoutParams(createEditLayoutParams(edit.getContext()));
            }

            edit.setVisibility(View.VISIBLE);
            edit.requestFocus();

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edit.mEditText, 0);
        }

        @NonNull
        private RelativeLayout.LayoutParams createEditLayoutParams(final Context context)
        {
            final Resources res = context.getResources();
            final int margin = (int) Utils.convertDpToPx(res, 20);
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) Utils.convertDpToPx(res, 50));
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.topMargin = margin;
            params.leftMargin = margin;
            params.topMargin = margin;
            return params;
        }
    }

    private static class IncomingServerMessageHandler extends Handler
    {
        private IncomingServerMessageHandlerCallback mCallback;

        IncomingServerMessageHandler(final IncomingServerMessageHandlerCallback callback)
        {
            mCallback = callback;
        }

        @Override
        public void handleMessage(Message msg)
        {
            Log.i(this, "Got server msg " + msg);
            switch (msg.what)
            {
                case SERVER_MESSAGE_SERVER_READY:
                    NativeMethods.notifyServerReady();
                    break;
                case SERVER_MESSAGE_SERVER_KILLED:
                    if (mCallback != null)
                    {
                        mCallback.unbindServer();
                    }
                    NativeMethods.notifyServerClosed();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class OnServerRegisteredCallback implements IncomingServerMessageHandlerCallback
    {
        @Override
        public void unbindServer()
        {
            SDLActivity.this.unbindServer();
        }
    }
}

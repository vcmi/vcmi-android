package eu.vcmi.vcmi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.libsdl.app.SDLActivity;

import java.io.File;
import java.lang.ref.WeakReference;

import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public class NativeMethods
{
    private static WeakReference<Context> ctxRef;
    private static WeakReference<Messenger> serverMessengerRef;

    public NativeMethods()
    {
    }

    public static native void initClassloader();

    public static native void createServer();

    public static native void notifyServerReady();

    public static native void notifyServerClosed();

    public static native boolean tryToSaveTheGame();

    public static void setupCtx(final Context ctx)
    {
        ctxRef = new WeakReference<>(ctx);
    }

    public static void setupMsg(final Messenger msg)
    {
        serverMessengerRef = new WeakReference<>(msg);
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static String dataRoot()
    {
        String root = new File(Environment.getExternalStorageDirectory(), Const.VCMI_DATA_ROOT_FOLDER_NAME).getAbsolutePath();
        Log.i("Accessing data root: " + root);
        return root;
    }

    // this path is visible only to this application; we can store base vcmi configs etc. there
    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static String internalDataRoot()
    {
        Context ctx = requireContext();
        String root = new File(ctx.getFilesDir(), Const.VCMI_DATA_ROOT_FOLDER_NAME).getAbsolutePath();
        Log.i("Accessing internal data root: " + root);
        return root;
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static String nativePath()
    {
        Context ctx = requireContext();
        Log.i("Accessing ndk path: " + ctx.getApplicationInfo().nativeLibraryDir);
        return ctx.getApplicationInfo().nativeLibraryDir;
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void startServer()
    {
        Log.i("Got server create request");
        Context ctx = requireContext();
        if (!(ctx instanceof SDLActivity))
        {
            Log.e("Unexpected context... " + ctx);
            return;
        }
        Intent intent = new Intent(ctx, SDLActivity.class);
        intent.setAction(SDLActivity.NATIVE_ACTION_CREATE_SERVER);
        // I probably do something incorrectly, but sending new intent to the activity "normally" breaks SDL events handling (probably detaches jnienv?)
        // so instead let's call onNewIntent directly, as out context SHOULD be SDLActivity anyway
        ((SDLActivity) ctx).hackCallNewIntentDirectly(intent);
//        ctx.startActivity(intent);
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void killServer()
    {
        Log.i("Got server close request");

        Context ctx = requireContext();
        ctx.stopService(new Intent(ctx, ServerService.class));

        Messenger messenger = requireServerMessenger();
        try
        {
            // we need to actually inform client about killing the server, beacuse it needs to unbind service connection before server gets destroyed
            messenger.send(Message.obtain(null, SDLActivity.SERVER_MESSAGE_SERVER_KILLED));
        }
        catch (RemoteException e)
        {
            Log.w("Connection with client process broken?");
        }
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void onServerReady()
    {
        Log.i("Got server ready msg");
        Messenger messenger = requireServerMessenger();

        try
        {
            messenger.send(Message.obtain(null, SDLActivity.SERVER_MESSAGE_SERVER_READY));
        }
        catch (RemoteException e)
        {
            Log.w("Connection with client process broken?");
        }
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void showProgress()
    {
        internalProgressDisplay(true);
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void hideProgress()
    {
        internalProgressDisplay(false);
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void notifyTextInputChanged(final String textContext)
    {
        final Context ctx = requireContext();
        if (!(ctx instanceof SDLActivity))
        {
            return;
        }
        ((Activity) ctx).runOnUiThread(() -> SDLActivity.notifyTextInputChanged(textContext));
    }

    private static void internalProgressDisplay(final boolean show)
    {
        final Context ctx = requireContext();
        if (!(ctx instanceof SDLActivity))
        {
            return;
        }
        ((SDLActivity) ctx).runOnUiThread(() -> ((SDLActivity) ctx).displayProgress(show));
    }

    private static Context requireContext()
    {
        Context ctx = ctxRef.get();
        if (ctx == null)
        {
            throw new RuntimeException("Broken context");
        }
        return ctx;
    }

    private static Messenger requireServerMessenger()
    {
        Messenger msg = serverMessengerRef.get();
        if (msg == null)
        {
            throw new RuntimeException("Broken server messenger");
        }
        return msg;
    }
}

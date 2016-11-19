package eu.vcmi.vcmi;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.libsdl.app.SDLActivity;

import java.io.File;
import java.lang.ref.WeakReference;

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
        Log.i("VCMI", "Accessing data root: " + root);
        return root;
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static String nativePath()
    {
        Context ctx = requireContext();
        Log.i("VCMI", "Accessing ndk path: " + ctx.getApplicationInfo().nativeLibraryDir);
        return ctx.getApplicationInfo().nativeLibraryDir;
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void startServer()
    {
        Log.i("VCMI", "Got server create request");
        Context ctx = requireContext();
        Intent intent = new Intent(ctx, SDLActivity.class);
        intent.setAction(SDLActivity.NATIVE_ACTION_CREATE_SERVER);
        ctx.startActivity(intent);
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void killServer()
    {
        Log.i("VCMI", "Got server close request");

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
            Log.w("VCMI", "Connection with client process broken?");
        }
    }

    @SuppressWarnings(Const.JNI_METHOD_SUPPRESS)
    public static void onServerReady()
    {
        Log.i("VCMI", "Got server ready msg");
        Messenger messenger = requireServerMessenger();

        try
        {
            messenger.send(Message.obtain(null, SDLActivity.SERVER_MESSAGE_SERVER_READY));
        }
        catch (RemoteException e)
        {
            Log.w("VCMI", "Connection with client process broken?");
        }
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

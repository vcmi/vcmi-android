package eu.vcmi.vcmi;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author F
 */
public class NativeMethods
{
    private static WeakReference<Context> ctxRef;

    public NativeMethods()
    {
    }

    public static native void initClassloader();

    public static native void createServer();

    public static void setupCtx(final Context ctx)
    {
        ctxRef = new WeakReference<>(ctx);
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
        Context ctx = ctxRef.get();
        if (ctx == null)
        {
            Log.e("", "Broken context");
            return "";
        }
        Log.i("VCMI", "Accessing ndk path: " + ctx.getApplicationInfo().nativeLibraryDir);
        return ctx.getApplicationInfo().nativeLibraryDir;
    }
}

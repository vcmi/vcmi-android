package eu.vcmi.vcmi.util;

/**
 * @author F
 */

public class Log
{
    private static final boolean LOGGING_ENABLED = true;

    private static void log(final int priority, final Object obj, final String msg)
    {
        if (LOGGING_ENABLED)
        {
            android.util.Log.println(priority, tag(obj), msg);
        }
    }

    private static String tag(final Object obj)
    {
        if (obj == null)
        {
            return "null";
        }
        return obj.getClass().getSimpleName();
    }

    public static void v(final Object obj, final String msg)
    {
        log(android.util.Log.VERBOSE, obj, msg);
    }

    public static void d(final Object obj, final String msg)
    {
        log(android.util.Log.DEBUG, obj, msg);
    }

    public static void i(final Object obj, final String msg)
    {
        log(android.util.Log.INFO, obj, msg);
    }

    public static void w(final Object obj, final String msg)
    {
        log(android.util.Log.WARN, obj, msg);
    }

    public static void e(final Object obj, final String msg)
    {
        log(android.util.Log.ERROR, obj, msg);
    }

    public static void e(final Object obj, final String msg, final Throwable e)
    {
        log(android.util.Log.ERROR, obj, msg + "\n" + android.util.Log.getStackTraceString(e));
    }
}

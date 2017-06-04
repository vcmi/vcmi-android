package eu.vcmi.vcmi.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @author F
 */

public final class Utils
{
    private static String sAppVersionCache;

    private Utils()
    {
    }

    public static String appVersionName(final Context ctx)
    {
        if (sAppVersionCache == null)
        {
            final PackageManager pm = ctx.getPackageManager();
            try
            {
                final PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                return sAppVersionCache = info.versionName;
            }
            catch (final PackageManager.NameNotFoundException e)
            {
                Log.e(ctx, "Couldn't resolve app version", e);
            }
        }
        return sAppVersionCache;
    }
}

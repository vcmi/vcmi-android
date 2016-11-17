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
            PackageManager pm = ctx.getPackageManager();
            try
            {
                PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                return sAppVersionCache = info.versionName;
            }
            catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        return sAppVersionCache;
    }
}

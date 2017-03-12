package eu.vcmi.vcmi.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * simple shared preferences wrapper
 *
 * @author F
 */
public class SharedPrefs
{
    public static final String KEY_POINTER_RELATIVE_MODE = "KEY_POINTER_RELATIVE_MODE";
    private static final String VCMI_PREFS_NAME = "VCMIPrefs";
    private final SharedPreferences mPrefs;

    public SharedPrefs(final Context ctx)
    {
        mPrefs = ctx.getSharedPreferences(VCMI_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(final String name, final int value)
    {
        mPrefs.edit().putInt(name, value).apply();
    }

    public int load(final String name, final int defaultValue)
    {
        return mPrefs.getInt(name, defaultValue);
    }

    public void save(final String name, final boolean value)
    {
        mPrefs.edit().putBoolean(name, value).apply();
    }

    public boolean load(final String name, final boolean defaultValue)
    {
        return mPrefs.getBoolean(name, defaultValue);
    }

    private <T> void log(final String key, final T value, final boolean saving)
    {

    }
}

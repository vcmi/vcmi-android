package eu.vcmi.vcmi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * simple shared preferences wrapper
 *
 * @author F
 */
public class SharedPrefs
{
    public static final String KEY_POINTER_MODE = "KEY_POINTER_MODE"; // [int]
    private static final String VCMI_PREFS_NAME = "VCMIPrefs";
    private final SharedPreferences mPrefs;

    public SharedPrefs(final Context ctx)
    {
        mPrefs = ctx.getSharedPreferences(VCMI_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(final String name, final int value)
    {
        mPrefs.edit().putInt(name, value).apply();
        log(name, value, true);
    }

    public int load(final String name, final int defaultValue)
    {
        return log(name, mPrefs.getInt(name, defaultValue), false);
    }

    public void save(final String name, final boolean value)
    {
        mPrefs.edit().putBoolean(name, value).apply();
        log(name, value, true);
    }

    public boolean load(final String name, final boolean defaultValue)
    {
        return log(name, mPrefs.getBoolean(name, defaultValue), false);
    }

    public <T extends Enum<T>> void saveEnum(final String name, final T value)
    {
        mPrefs.edit().putInt(name, value.ordinal()).apply();
        log(name, value, true);
    }

    public <T extends Enum<T>> T loadEnum(final String name, @NonNull final T defaultValue)
    {
        final int rawValue = mPrefs.getInt(name, defaultValue.ordinal());
        return (T) log(name, defaultValue.getClass().getEnumConstants()[rawValue], false);
    }

    private <T> T log(final String key, final T value, final boolean saving)
    {
        if (saving)
        {
            Log.v(this, "[prefs saving] " + key + " => " + value);
        }
        else
        {
            Log.v(this, "[prefs loading] " + key + " => " + value);
        }
        return value;
    }
}

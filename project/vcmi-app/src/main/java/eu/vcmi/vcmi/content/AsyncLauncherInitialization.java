package eu.vcmi.vcmi.content;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.lang.ref.WeakReference;

import eu.vcmi.vcmi.Const;
import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.SharedPrefs;

/**
 * @author F
 */
public class AsyncLauncherInitialization extends AsyncTask<Void, Void, AsyncLauncherInitialization.InitResult>
{
    public static final int PERMISSIONS_REQ_CODE = 123;
    private final WeakReference<ILauncherCallbacks> mCallbackRef;

    public AsyncLauncherInitialization(final ILauncherCallbacks callback)
    {
        mCallbackRef = new WeakReference<>(callback);
    }

    private InitResult init()
    {
        Log.d(this, "Starting init checks");
        InitResult initResult = handlePermissions();
        if (!initResult.mSuccess)
        {
            return initResult;
        }
        Log.d(this, "Permissions check passed");

        initResult = handleDataFoldersInitialization();
        if (!initResult.mSuccess)
        {
            return initResult;
        }
        Log.d(this, "Folders check passed");

        return initResult;
    }

    private InitResult handleDataFoldersInitialization()
    {
        final ILauncherCallbacks callbacks = mCallbackRef.get();
        if (callbacks == null)
        {
            return new InitResult(false, "Internal error");
        }
        final Context ctx = callbacks.ctx();
        final File baseDir = Environment.getExternalStorageDirectory();
        final File internalDir = ctx.getFilesDir();
        final File vcmiDir = new File(baseDir, Const.VCMI_DATA_ROOT_FOLDER_NAME);
        final File vcmiInternalDir = new File(internalDir, Const.VCMI_DATA_ROOT_FOLDER_NAME);
        Log.i(this, "Using " + vcmiDir.getAbsolutePath() + " as root vcmi dir");
        if (!vcmiDir.exists()) // we don't have root folder == new install (or deleted)
        {
            boolean allCreated = vcmiDir.mkdir();
            allCreated &= vcmiInternalDir.exists() || vcmiInternalDir.mkdir();

            if (allCreated)
            {
                return new InitResult(false, ctx.getString(R.string.launcher_error_vcmi_data_root_created,
                    Const.VCMI_DATA_ROOT_FOLDER_NAME, vcmiDir.getAbsolutePath()));
            }
            else
            {
                return new InitResult(false, ctx.getString(R.string.launcher_error_vcmi_data_root_failed, vcmiDir.getAbsolutePath()));
            }
        }

        final File testH3Data = new File(vcmiDir, "Data");
        if (!testH3Data.exists())
        {
            return new InitResult(false,
                ctx.getString(R.string.launcher_error_h3_data_missing, testH3Data.getAbsolutePath(), Const.VCMI_DATA_ROOT_FOLDER_NAME));
        }

        final File testVcmiData = new File(vcmiInternalDir, "Mods/vcmi/mod.json");
        if (!testVcmiData.exists() && !FileUtil.unpackVcmiDataToInternalDir(vcmiInternalDir, ctx.getAssets()))
        {
            return new InitResult(false, ctx.getString(R.string.launcher_error_vcmi_data_internal_missing));
        }

        if (callbacks.prefs().load(SharedPrefs.KEY_CURRENT_INTERNAL_ASSET_VERSION, -1) != FileUtil.CURRENT_INTERNAL_ASSETS_VERSION)
        {
            // there was an update to internal assets so we need to replace the existing ones
            if (!FileUtil.reloadVcmiDataToInternalDir(vcmiInternalDir, ctx.getAssets()))
            {
                return new InitResult(false, ctx.getString(R.string.launcher_error_vcmi_data_internal_update));
            }
            callbacks.prefs().save(SharedPrefs.KEY_CURRENT_INTERNAL_ASSET_VERSION, FileUtil.CURRENT_INTERNAL_ASSETS_VERSION);
        }

        return new InitResult(true, "");
    }

    private InitResult handlePermissions()
    {
        final ILauncherCallbacks callbacks = mCallbackRef.get();
        if (callbacks == null)
        {
            return new InitResult(false, "Internal error");
        }
        final Activity act = callbacks.ctx();
        try
        {
            if (ActivityCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                return new InitResult(true, "");
            }
        }
        catch (RuntimeException ignored)
        {
            return new InitResult(Build.VERSION.SDK_INT < Build.VERSION_CODES.M,
                act.getString(R.string.launcher_error_permission_broken));
        }

        ActivityCompat.requestPermissions(act, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQ_CODE);

        // if permissions failed, that means we started system permissions request; we'll wait for user action and will rerun init() after that
        // if it succeeds or call onInitError() otherwise
        InitResult initResult = new InitResult(false, "");
        initResult.mFailSilently = true;
        return initResult;
    }

    @Override
    protected InitResult doInBackground(final Void... params)
    {
        return init();
    }

    @Override
    protected void onPostExecute(final InitResult initResult)
    {
        final ILauncherCallbacks callbacks = mCallbackRef.get();
        if (callbacks == null)
        {
            return;
        }

        if (initResult.mSuccess)
        {
            callbacks.onInitSuccess();
        }
        else
        {
            callbacks.onInitFailure(initResult);
        }
    }

    public interface ILauncherCallbacks
    {
        Activity ctx();

        SharedPrefs prefs();

        void onInitSuccess();

        void onInitFailure(InitResult result);
    }

    public static final class InitResult
    {
        public final boolean mSuccess;
        public final String mMessage;
        public boolean mFailSilently;

        public InitResult(final boolean success, final String message)
        {
            mSuccess = success;
            mMessage = message;
        }

        @Override
        public String toString()
        {
            return String.format("success: %s (%s)", mSuccess, mMessage);
        }
    }
}

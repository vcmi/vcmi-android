package eu.vcmi.vcmi.content;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import eu.vcmi.vcmi.Const;
import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.LegacyConfigReader;
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
        InitResult initResult;

        if(Build.VERSION.SDK_INT < 24)
        {
            Log.d(this, "Starting init checks");
            initResult = handlePermissions();
            if (!initResult.mSuccess) {
                return initResult;
            }
            Log.d(this, "Permissions check passed");
        }

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

        final File vcmiDir = Const.getVcmiDataDir(ctx);

        final File internalDir = ctx.getFilesDir();
        final File vcmiInternalDir = new File(internalDir, Const.VCMI_DATA_ROOT_FOLDER_NAME);
        Log.i(this, "Using " + vcmiDir.getAbsolutePath() + " as root vcmi dir");

        if (!vcmiDir.exists()) // we don't have root folder == new install (or deleted)
        {
            boolean allCreated = vcmiDir.mkdir();
            allCreated &= vcmiInternalDir.exists() || vcmiInternalDir.mkdir();

            if (allCreated)
            {
                if (!tryToRetrieveH3DataFromLegacyDir(ctx, vcmiDir))
                {
                    return new InitResult(
                        false,
                        ctx.getString(
                            R.string.launcher_error_vcmi_data_root_created,
                            Const.VCMI_DATA_ROOT_FOLDER_NAME,
                            vcmiDir.getAbsolutePath()));
                }
                //else: we managed to copy the legacy data from old vcmi version (TODO should we tell the user that we moved the data?)
            }
            else // we can't really do anything more if, for some reason, we couldn't create root folders (read-only or corrupted filesystem?)
            {
                return new InitResult(false, ctx.getString(R.string.launcher_error_vcmi_data_root_failed, vcmiDir.getAbsolutePath()));
            }
        }

        if (!testH3DataFolder(vcmiDir))
        {
            // no h3 data present -> instruct user where to put it
            new InitResult(
                false,
                ctx.getString(
                    R.string.launcher_error_h3_data_missing,
                    vcmiDir.getAbsolutePath(),
                    Const.VCMI_DATA_ROOT_FOLDER_NAME));
        }

        final File testVcmiData = new File(vcmiInternalDir, "Mods/vcmi/mod.json");
        final boolean internalVcmiDataExisted = testVcmiData.exists();
        if (!internalVcmiDataExisted && !FileUtil.unpackVcmiDataToInternalDir(vcmiInternalDir, ctx.getAssets())) {
            // no h3 data present -> instruct user where to put it
            new InitResult(false,
                    ctx.getString(R.string.launcher_error_h3_data_missing, vcmiDir.getAbsolutePath(), Const.VCMI_DATA_ROOT_FOLDER_NAME));
        }

        final String previousInternalDataHash = callbacks.prefs().load(SharedPrefs.KEY_CURRENT_INTERNAL_ASSET_HASH, null);
        final String currentInternalDataHash = FileUtil.readAssetsStream(ctx.getAssets(), "internalDataHash.txt");
        if (currentInternalDataHash == null || previousInternalDataHash == null || !currentInternalDataHash.equals(previousInternalDataHash))
        {
            // we should update the data only if it existed previously (hash is bound to be empty if we have just created the data)
            if (internalVcmiDataExisted)
            {
                Log.i(this, "Internal data needs to be created/updated; old hash=" + previousInternalDataHash
                            + ", new hash=" + currentInternalDataHash);
                if (!FileUtil.reloadVcmiDataToInternalDir(vcmiInternalDir, ctx.getAssets()))
                {
                    return new InitResult(false, ctx.getString(R.string.launcher_error_vcmi_data_internal_update));
                }
            }
            callbacks.prefs().save(SharedPrefs.KEY_CURRENT_INTERNAL_ASSET_HASH, currentInternalDataHash);
        }

        return new InitResult(true, "");
    }

    private boolean testH3DataFolder(final File baseDir)
    {
        final File testH3Data = new File(baseDir, "Data");
        return testH3Data.exists();
    }

    private boolean tryToRetrieveH3DataFromLegacyDir(final Context ctx, final File vcmiRoot)
    {
        final LegacyConfigReader.Config config = LegacyConfigReader.load(ctx.getFilesDir());
        if (config == null) // it wasn't possible to correctly read the config
        {
            return false;
        }

        if (!testH3DataFolder(new File(config.mDataPath))) // make sure that folder that we found actually contains h3 data
        {
            Log.i(this, "Legacy folder doesn't contain valid H3 data");
            return false;
        }

        final List<File> copiedLegacyFiles = new ArrayList<>();
        final String[] userFolders = new String[] {"Data", "Saves", "Maps", "Mp3"};
        for (final String folder : userFolders)
        {
            final File targetPath = new File(vcmiRoot, folder);
            final File path = new File(config.mDataPath, folder);
            final String[] contents = path.list();
            if (contents == null)
            {
                continue;
            }

            for (final String filename : contents)
            {
                final File srcFile = new File(path, filename);
                final File dstFile = new File(targetPath, filename);
                Log.v(this, "Copying legacy data " + srcFile + " -> " + dstFile);
                if (!FileUtil.copyFile(srcFile, dstFile))
                {
                    Log.w(this, "Broke while copying " + srcFile);
                    return false; // TODO data might've been partially copied; should we inform the user about it?
                }
                copiedLegacyFiles.add(srcFile);
            }
        }

        final File settingsFile = new File(new File(config.mDataPath, "config"), "settings.json");
        if (settingsFile.exists())
        {
            final File settingsTargetFile = new File(new File(vcmiRoot, "config"), "settings.json");
            if (!FileUtil.copyFile(settingsFile, settingsTargetFile))
            {
                Log.w(this, "Broke while copying " + settingsFile); // not that important -> ignore
            }
        }

        for (final File copiedLegacyFile : copiedLegacyFiles)
        {
            if (!copiedLegacyFile.delete())
            {
                // ignore the error (we could display some info about it to the user, because it leaves unneeded files on the phone)
                Log.w(this, "Couldn't delete " + copiedLegacyFile);
            }
        }

        return true; // TODO should we try to delete everything else in the old directory?
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
        catch (final RuntimeException ignored)
        {
            return new InitResult(Build.VERSION.SDK_INT < Build.VERSION_CODES.M,
                act.getString(R.string.launcher_error_permission_broken));
        }

        ActivityCompat.requestPermissions(act, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQ_CODE);

        // if permissions failed, that means we started system permissions request; we'll wait for user action and will rerun init() after that
        // if it succeeds or call onInitError() otherwise
        final InitResult initResult = new InitResult(false, "");
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

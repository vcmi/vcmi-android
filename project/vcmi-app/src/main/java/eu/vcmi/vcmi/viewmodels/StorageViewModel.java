package eu.vcmi.vcmi.viewmodels;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.databinding.Bindable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.databinding.library.baseAdapters.BR;
import eu.vcmi.vcmi.ActivityLauncher;
import eu.vcmi.vcmi.ActivityStorage;
import eu.vcmi.vcmi.Const;
import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.Storage;
import eu.vcmi.vcmi.content.AsyncLauncherInitialization;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.IZipProgressReporter;
import eu.vcmi.vcmi.util.LegacyConfigReader;
import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public class StorageViewModel extends ObservableViewModel
{
    private ActivityStorage activity;

    private boolean useExternal;
    private String importProgress;
    private String errorMessage;
    private boolean isBusy;

    @Bindable
    public boolean getIsBusy(){ return isBusy; }

    @Bindable
    public boolean getUseExternal()
    {
        return useExternal;
    }

    @Bindable
    public String getImportProgress()
    {
        return importProgress;
    }

    @Bindable
    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setUseExternal(boolean val)
    {
        useExternal = val;
        notifyPropertyChanged(BR.useExternal);
    }

    public void setIsBusy(boolean val)
    {
        isBusy = val;
        notifyPropertyChanged(BR.isBusy);
    }

    public void setErrorMessage(String val)
    {
        errorMessage = val;
        notifyPropertyChanged(BR.errorMessage);
    }

    public void setImportProgress(String val)
    {
        importProgress = val;
        notifyPropertyChanged(BR.importProgress);
    }

    public StorageViewModel()
    {
        useExternal = Storage.getIsExternalStorageUsed();
        importProgress = "";
        errorMessage = "";
    }

    public void onUseExternalStorage()
    {
        errorMessage = "";

        try
        {
            Log.d(this, "Select external storage");

            boolean initResult = requestExternalStoragePermissions();
            if (initResult)
            {
                Log.d(this, "Permissions check passed");

                Storage.setExternalStorage(activity, true);
                setUseExternal(true);

                if (!createExternalStorageRootFolder())
                {
                    errorMessage = activity.getString(
                        R.string.launcher_error_vcmi_data_root_failed,
                        Storage.getVcmiDataDir(activity).getAbsolutePath());

                    return;
                }

                onTestExternalStorage();
            }
        }
        catch (IOException ex)
        {
            Log.e("Can not create storage settings", ex);
        }
        finally
        {
            notifyChange();
        }
    }

    public void onTestExternalStorage()
    {
        tryToRetrieveH3DataFromLegacyDir();
        testStorage();
    }

    public void onUseInternalStorage()
    {
        setErrorMessage("");

        try
        {
            Storage.setExternalStorage(activity, false);
            setUseExternal(false);
        }
        catch (IOException ex)
        {
            setErrorMessage("Can not create storage settings");
            Log.e("Can not create storage settings", ex);
        }
    }

    public void importVcmiDataToInternalStorage()
    {
        activity.selectZipFile();
    }

    public void setActivity(ActivityStorage activity)
    {
        this.activity = activity;
        setImportProgress(activity.getString(R.string.storage_btn_import_zip_description));
    }

    public void importFilesFromStream(Uri vcmiDataZip)
    {
        setIsBusy(true);
        new Thread(new UnzipTask(vcmiDataZip)).start();
    }

    private boolean createExternalStorageRootFolder()
    {
        File vcmiDir = Storage.getVcmiDataDir(activity);

        return vcmiDir.exists() || vcmiDir.mkdir();
    }

    private boolean tryToRetrieveH3DataFromLegacyDir()
    {
        final File vcmiRoot = Storage.getVcmiDataDir(activity);
        final LegacyConfigReader.Config config = LegacyConfigReader.load(activity.getFilesDir());
        if (config == null) // it wasn't possible to correctly read the config
        {
            return false;
        }

        if (!Storage.testH3DataFolder(new File(config.mDataPath))) // make sure that folder that we found actually contains h3 data
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

    private boolean requestExternalStoragePermissions()
    {
        try
        {
            int storageWritePermissions = ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (storageWritePermissions == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
        }
        catch (final RuntimeException ignored)
        {
            errorMessage = activity.getString(R.string.launcher_error_permission_broken);
            return false;
        }

        activity.requestStoragePermissions();

        return false;
    }

    private void testStorage()
    {
        setErrorMessage("");

        if(!Storage.testH3DataFolder(activity))
        {
            setErrorMessage(activity.getString(
                    R.string.launcher_error_h3_data_missing));

            return;
        }

        activity.startActivity(
                new Intent(activity, ActivityLauncher.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    private class UnzipTask implements Runnable, IZipProgressReporter
    {
        private Uri vcmiDataZip;

        public UnzipTask(Uri vcmiDataZip)
        {
            this.vcmiDataZip = vcmiDataZip;
        }

        @Override
        public void onPacking(File newFile)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setImportProgress("unpacking " + newFile.getName());
                }
            });
        }

        @Override
        public void run()
        {
            File vcmiDataDir = Storage.getVcmiDataDir(activity);
            Exception error = null;

            try (InputStream zipStream = activity.getContentResolver().openInputStream(vcmiDataZip))
            {
                FileUtil.unpackZipFile(zipStream, vcmiDataDir, this);
            }
            catch(Exception e)
            {
                error = e;
                Log.e("Can not parse zip file", e);
            }

            String errMessage = error == null ? "" : error.getMessage();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setIsBusy(false);
                    setErrorMessage(errMessage);
                    setImportProgress(
                            activity.getString(R.string.storage_btn_import_zip_description));

                    testStorage();
                }
            });
        }
    }
}
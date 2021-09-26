package eu.vcmi.vcmi;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import eu.vcmi.vcmi.util.FileUtil;

public class Storage
{
    private static boolean useExternalStorage = false;

    public static boolean getIsExternalStorageUsed() {
        return useExternalStorage;
    }

    public static File getVcmiDataDir(Context context)
    {
        File root;

        if (!useExternalStorage && Const.INTERNAL_STORAGE_AVAILABLE)
        {
            root = context.getDataDir();
        }
        else
        {
            root = Environment.getExternalStorageDirectory();
        }

        return new File(root, Const.VCMI_DATA_ROOT_FOLDER_NAME);
    }

    public static void initStorage(Context context)
    {
        if (!Const.INTERNAL_STORAGE_AVAILABLE)
        {
            useExternalStorage = true;
            return;
        }

        File storageSettings = getStorageSettingsFile(context);

        useExternalStorage = storageSettings.exists();
    }

    public static void setExternalStorage(Context context, boolean useExternalStorage)
            throws IOException
    {
        File storageSettings = getStorageSettingsFile(context);

        if (useExternalStorage != storageSettings.exists())
        {
            if (useExternalStorage)
            {
                storageSettings.createNewFile();

                FileUtil.write(storageSettings, Const.VCMI_DATA_ROOT_FOLDER_NAME);
            }
            else
            {
                storageSettings.delete();
            }
        }
    }

    public static boolean testH3DataFolder(Context context)
    {
        return testH3DataFolder(getVcmiDataDir(context));
    }

    public static boolean testH3DataFolder(final File baseDir)
    {
        final File testH3Data = new File(baseDir, "Data");
        return testH3Data.exists();
    }

    private static File getStorageSettingsFile(Context context)
    {
        return new File(context.getFilesDir(),"storage.config");
    }
}

package eu.vcmi.vcmi.util;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eu.vcmi.vcmi.Const;

/**
 * @author F
 */
public class FileUtil
{
    public static final int CURRENT_INTERNAL_ASSETS_VERSION = 4;

    public static String read(final File file) throws IOException
    {
        try (FileReader modConfigReader = new FileReader(file))
        {
            final char[] buffer = new char[4096];
            int currentRead;
            final StringBuilder content = new StringBuilder();
            while ((currentRead = modConfigReader.read(buffer, 0, 4096)) >= 0)
            {
                content.append(buffer, 0, currentRead);
            }
            return content.toString();
        }
    }

    public static void write(final File file, final String data) throws IOException
    {
        final FileWriter fw = new FileWriter(file, false);
        Log.v(null, "Saving data: " + data + " to " + file.getAbsolutePath());
        fw.write(data);
        fw.close();
    }

    private static boolean clearDirectory(final File dir)
    {
        for (final File f : dir.listFiles())
        {
            if (f.isDirectory() && !clearDirectory(f))
            {
                return false;
            }
            if (!f.delete())
            {
                return false;
            }
        }
        return true;
    }

    // (when internal data have changed)
    public static boolean reloadVcmiDataToInternalDir(final File vcmiInternalDir, final AssetManager assets)
    {
        return clearDirectory(vcmiInternalDir) && unpackVcmiDataToInternalDir(vcmiInternalDir, assets);
    }

    public static boolean unpackVcmiDataToInternalDir(final File vcmiInternalDir, final AssetManager assets)
    {
        try
        {
            int unpackedEntries = 0;
            final byte[] buffer = new byte[4096];
            final ZipInputStream is = new ZipInputStream(assets.open("internalData.zip"));
            ZipEntry zipEntry;
            while ((zipEntry = is.getNextEntry()) != null)
            {

                final String fileName = zipEntry.getName();
                final File newFile = new File(vcmiInternalDir, fileName);

                if (newFile.exists())
                {
                    Log.d("Already exists: " + newFile.getName());
                    continue;
                }
                else if (zipEntry.isDirectory())
                {
                    Log.v("Creating new dir: " + zipEntry);
                    if (!newFile.mkdirs())
                    {
                        Log.e("Couldn't create directory " + newFile.getAbsolutePath());
                        return false;
                    }
                    continue;
                }

                final File parentFile = new File(newFile.getParent());
                if (!parentFile.exists() && !parentFile.mkdirs())
                {
                    Log.e("Couldn't create directory " + parentFile.getAbsolutePath());
                    return false;
                }

                final FileOutputStream fos = new FileOutputStream(newFile, false);

                int currentRead;
                while ((currentRead = is.read(buffer)) > 0)
                {
                    fos.write(buffer, 0, currentRead);
                }

                fos.flush();
                fos.close();
                ++unpackedEntries;
            }
            Log.d("Unpacked data (" + unpackedEntries + " entries)");

            is.closeEntry();
            is.close();
            return true;
        }
        catch (final Exception e)
        {
            Log.e("Couldn't extract vcmi data to internal dir", e);
            return false;
        }
    }

    public static String configFileLocation()
    {
        return Environment.getExternalStorageDirectory() + "/" + Const.VCMI_DATA_ROOT_FOLDER_NAME + "/config/settings.json";
    }
}

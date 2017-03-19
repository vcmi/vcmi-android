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
    public static String read(final File file) throws IOException
    {
        try (FileReader modConfigReader = new FileReader(file))
        {
            char buffer[] = new char[4096];
            int totalRead = 0;
            int currentRead;
            final StringBuilder content = new StringBuilder();
            while ((currentRead = modConfigReader.read(buffer, 0, 4096)) >= 0)
            {
                content.append(buffer, 0, currentRead);
                totalRead += currentRead;
            }
            return content.toString();
        }
    }

    public static void write(final File file, final String data) throws IOException
    {
        FileWriter fw = new FileWriter(file, false);
        Log.v(null, "Saving data: " + data + " to " + file.getAbsolutePath());
        fw.write(data);
        fw.close();
    }

    public static boolean unpackVcmiDataToInternalDir(final File vcmiInternalDir, final AssetManager assets)
    {
        try
        {
            int unpackedEntries = 0;
            byte[] buffer = new byte[4096];
            final ZipInputStream is = new ZipInputStream(assets.open("internalData.zip"));
            ZipEntry zipEntry;
            while ((zipEntry = is.getNextEntry()) != null)
            {

                String fileName = zipEntry.getName();
                File newFile = new File(vcmiInternalDir, fileName);

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

                File parentFile = new File(newFile.getParent());
                if (!parentFile.exists() && !parentFile.mkdirs())
                {
                    Log.e("Couldn't create directory " + parentFile.getAbsolutePath());
                    return false;
                }

                FileOutputStream fos = new FileOutputStream(newFile, false);

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
        catch (Exception e)
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

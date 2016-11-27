package eu.vcmi.vcmi.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author F
 */
public class FileUtil
{
    public static String read(final File file) throws IOException
    {
        FileReader modConfigReader = new FileReader(file);
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

    public static void write(final File file, final String data) throws IOException
    {
        FileWriter fw = new FileWriter(file, false);
        Log.v(null, "Saving data: " + data + " to " + file.getAbsolutePath());
        fw.write(data);
        fw.close();
    }
}

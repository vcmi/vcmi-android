package eu.vcmi.vcmi.util;

import android.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
}

package org.libsdl.app;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by F on 10.11.2016.
 */

public class VCMIJavaHelpers {
    private static final String VCMI_DATA_ROOT_ENV_NAME = "VCMI_DATA_ROOT";
    private static final String VCMI_DATA_ROOT_FOLDER_NAME = "vcmi-data";
    public static boolean handleDataFoldersInitialization(final Context ctx)
    {
        if (ctx == null)
        {
            return false;
        }

        final File baseDir = Environment.getExternalStorageDirectory();
        final File vcmiDir = new File(baseDir, VCMI_DATA_ROOT_FOLDER_NAME);
        System.setProperty(VCMI_DATA_ROOT_ENV_NAME, vcmiDir.getAbsolutePath());
        Log.i("xx#", "Using " + vcmiDir.getAbsolutePath() + " as root vcmi dir");
        if (!vcmiDir.exists()) // we don't have root folder == new install (or deleted)
        {
            boolean allCreated = vcmiDir.mkdir();
//            if (allCreated)
//            {
//                allCreated = new File(vcmiDir, "DATA").mkdir();
//                allCreated &= new File(vcmiDir, "SOMETHING").mkdir();
//            }

            if (allCreated)
            {
                Toast.makeText(ctx, "TMP : initial start -- created dirs -- waiting for data", Toast.LENGTH_LONG).show();
                return false;
            }
            else
            {
                Toast.makeText(ctx, "TMP : initial start -- could not create data folders", Toast.LENGTH_LONG).show();
                return false;
            }
        }


        return true;
    }

    public static String dataRoot() {
        return new File(Environment.getExternalStorageDirectory(), VCMI_DATA_ROOT_FOLDER_NAME).getAbsolutePath();
    }
}

package eu.vcmi.vcmi;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * @author F
 */
public class Const
{
    public static final String JNI_METHOD_SUPPRESS = "unused"; // jni methods are marked as unused, because IDE doesn't understand jni calls
    // used to disable lint errors about try-with-resources being unsupported on api <19 (it is supported, because retrolambda backports it)
    public static final int SUPPRESS_TRY_WITH_RESOURCES_WARNING = Build.VERSION_CODES.KITKAT;

    public static final String VCMI_DATA_ROOT_FOLDER_NAME = "vcmi-data";

    public static final String VCMI_DATA_ZIP_FILE_NAME = "vcmi-data.zip";

    public static File getVcmiDataDir(Context context)
    {
        File root;

        if (Build.VERSION.SDK_INT >= 24) {
            root = context.getDataDir();
        } else {
            root = Environment.getExternalStorageDirectory();
        }

        return new File(root, Const.VCMI_DATA_ROOT_FOLDER_NAME);
    }
}

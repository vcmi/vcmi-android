package eu.vcmi.vcmi;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author F
 */
public class Const
{
    public static final String JNI_METHOD_SUPPRESS = "unused"; // jni methods are marked as unused, because IDE doesn't understand jni calls

    public static final String VCMI_DATA_ROOT_FOLDER_NAME = "vcmi-data";
}

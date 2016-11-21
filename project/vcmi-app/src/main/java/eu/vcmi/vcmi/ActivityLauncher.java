package eu.vcmi.vcmi;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;
import org.libsdl.app.SDLActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;


/**
 * @author F
 */
public class ActivityLauncher extends AppCompatActivity
{
    private static final int PERMISSIONS_REQ_CODE = 123;
    private static final List<ScreenRes> AVAILABLE_RESOLUTIONS = new ArrayList<>();
    private static final List<String> AVAILABLE_CODEPAGES = new ArrayList<>();

    static
    {
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(800, 600));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1024, 600));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1024, 768));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1280, 800));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1280, 960));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1280, 1024));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1366, 768));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1440, 900));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1600, 1200));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1680, 1050));
        AVAILABLE_RESOLUTIONS.add(new ScreenRes(1920, 1080));
        AVAILABLE_CODEPAGES.add("CP1250");
        AVAILABLE_CODEPAGES.add("CP1251");
        AVAILABLE_CODEPAGES.add("CP1252");
        AVAILABLE_CODEPAGES.add("GBK");
        AVAILABLE_CODEPAGES.add("GB2312");
    }

    private View mProgress;
    private View mBtnStart;
    private TextView mErrorMessage;
    private Config mConfig;
    private View mBtnRes;
    private View mBtnCodepage;
    private View mBtnMods;
    private IOnDialogEntryChosen<String> mCodepageChangedListener = new IOnDialogEntryChosen<String>()
    {
        @Override
        public void onDialogEntryChosen(final String codepage)
        {
            Log.i(this, "Changed codepage; saving config to file");
            if (mConfig != null)
            {
                mConfig.mCodepage = codepage;
                mConfig.save(new File(configFileLocation()));
                onConfigUpdated();
            }
        }
    };
    private IOnDialogEntryChosen<ScreenRes> mScreenResChangedListener = new IOnDialogEntryChosen<ScreenRes>()
    {
        @Override
        public void onDialogEntryChosen(final ScreenRes res)
        {
            Log.i(this, "Changed screen res; saving config to file");
            if (mConfig != null)
            {
                mConfig.mResolutionWidth = res.mWidth;
                mConfig.mResolutionHeight = res.mHeight;
                mConfig.save(new File(configFileLocation()));
                onConfigUpdated();
            }
        }
    };

    private String configFileLocation()
    {
        return Environment.getExternalStorageDirectory() + "/" + Const.VCMI_DATA_ROOT_FOLDER_NAME + "/config/settings.json";
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(this, "Starting launcher");
        setContentView(R.layout.activity_launcher);

        mProgress = findViewById(R.id.launcher_progress);
        mErrorMessage = (TextView) findViewById(R.id.launcher_error);
        mErrorMessage.setVisibility(View.GONE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((TextView) findViewById(R.id.launcher_version_info)).setText(getString(R.string.launcher_version, BuildConfig.VERSION_NAME));

        mBtnStart = initLauncherBtn(R.id.launcher_btn_start, new OnLauncherStartPressed(), getString(R.string.launcher_btn_start_title),
            getString(R.string.launcher_btn_start_subtitle));
        mBtnRes = initLauncherBtn(R.id.launcher_btn_res, new OnLauncherResPressed(), getString(R.string.launcher_btn_res_title),
            getString(R.string.launcher_btn_res_subtitle_unknown));
        mBtnMods = initLauncherBtn(R.id.launcher_btn_mods, new OnLauncherModsPressed(), getString(R.string.launcher_btn_mods_title),
            getString(R.string.launcher_btn_mods_subtitle));
        mBtnCodepage = initLauncherBtn(R.id.launcher_btn_cp, new OnLauncherCodepagePressed(), getString(R.string.launcher_btn_cp_title),
            getString(R.string.launcher_btn_cp_subtitle_unknown));

        mBtnStart.setVisibility(View.GONE);

        init(); // TODO move to async task (will definitely be needed to be on worker thread when unpacking vcmi-data to external storage is added)
    }

    private View initLauncherBtn(final int btnId, final View.OnClickListener clickListener, final String title, final String subtitle)
    {
        View btn = findViewById(btnId);
        btn.setOnClickListener(clickListener);
        ((TextView) btn.findViewById(R.id.inc_launcher_btn_main)).setText(title);
        ((TextView) btn.findViewById(R.id.inc_launcher_btn_sub)).setText(subtitle);
        return btn;
    }

    private void updateLauncherSubtitle(final View btn, final String text)
    {
        ((TextView) btn.findViewById(R.id.inc_launcher_btn_sub)).setText(text);
    }

    private void init()
    {
        Log.d(this, "Starting init checks");
        InitResult initResult = handlePermissions();
        if (!initResult.mSuccess)
        {
            if (!initResult.mFailSilently)
            {
                onInitError(initResult, false);
            }
            return;
        }
        Log.d(this, "Permissions check passed");

        initResult = handleDataFoldersInitialization();
        if (!initResult.mSuccess)
        {
            onInitError(initResult, false);
            return;
        }
        Log.d(this, "Folders check passed");

        loadConfigFile();

        onInitSuccess();
    }

    private void loadConfigFile()
    {
        try
        {
            final String settingsFileContent =
                FileUtil.read(new File(configFileLocation()));
            mConfig = Config.load(new JSONObject(settingsFileContent));
            onConfigUpdated();
        }
        catch (Exception e)
        {
            Log.e(this, "Could not load config file", e);
        }
    }

    private void onConfigUpdated()
    {
        updateLauncherSubtitle(mBtnCodepage, mConfig.mCodepage == null
                                             ? getString(R.string.launcher_btn_cp_subtitle_unknown)
                                             : getString(R.string.launcher_btn_cp_subtitle, mConfig.mCodepage));
        updateLauncherSubtitle(mBtnRes, mConfig.mResolutionWidth <= 0 || mConfig.mResolutionHeight <= 0
                                        ? getString(R.string.launcher_btn_res_subtitle_unknown)
                                        : getString(R.string.launcher_btn_res_subtitle, mConfig.mResolutionWidth, mConfig.mResolutionHeight));
    }

    private InitResult handlePermissions()
    {
        try
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                return new InitResult(true, "");
            }
        }
        catch (RuntimeException ignored)
        {
            return new InitResult(Build.VERSION.SDK_INT < Build.VERSION_CODES.M, getString(R.string.launcher_error_permission_broken));
        }

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQ_CODE);

        // if permissions failed, that means we started system permissions request; we'll wait for user action and will rerun init() after that
        // if it succeeds or call onInitError() otherwise
        InitResult initResult = new InitResult(false, "");
        initResult.mFailSilently = true;
        return initResult;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults)
    {
        if (requestCode == PERMISSIONS_REQ_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                init(); // retry init (we still need to check folders)
            }
            else
            {
                onInitError(new InitResult(false, getString(R.string.launcher_error_permissions)), true);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onInitSuccess()
    {
        mBtnStart.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
    }

    private void onInitError(final InitResult initResult, final boolean disableAccess)
    {
        Log.d(this, "Init failed with " + initResult);
        if (disableAccess)
        {
            Intent intent = new Intent(this, ActivityError.class);
            intent.putExtra(ActivityError.ARG_ERROR_MSG, initResult.mMessage);
            startActivity(intent);
            finish();
            return;
        }
        mProgress.setVisibility(View.GONE);
        mBtnStart.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(initResult.mMessage);
    }

    private InitResult handleDataFoldersInitialization()
    {
        final File baseDir = Environment.getExternalStorageDirectory();
        final File internalDir = getFilesDir();
        final File vcmiDir = new File(baseDir, Const.VCMI_DATA_ROOT_FOLDER_NAME);
        final File vcmiInternalDir = new File(internalDir, Const.VCMI_DATA_ROOT_FOLDER_NAME);
        Log.i(this, "Using " + vcmiDir.getAbsolutePath() + " as root vcmi dir");
        if (!vcmiDir.exists()) // we don't have root folder == new install (or deleted)
        {
            boolean allCreated = vcmiDir.mkdir();
            allCreated &= vcmiInternalDir.exists() || vcmiInternalDir.mkdir();

            if (allCreated)
            {
                return new InitResult(false, getString(R.string.launcher_error_vcmi_data_root_created,
                    Const.VCMI_DATA_ROOT_FOLDER_NAME, vcmiDir.getAbsolutePath()));
            }
            else
            {
                return new InitResult(false, getString(R.string.launcher_error_vcmi_data_root_failed, vcmiDir.getAbsolutePath()));
            }
        }

        final File testH3Data = new File(vcmiDir, "Data");
        if (!testH3Data.exists())
        {
            return new InitResult(false, getString(R.string.launcher_error_h3_data_missing, testH3Data.getAbsolutePath(), vcmiDir.getAbsolutePath() + "/Mp3"));
        }

        final File testVcmiData = new File(vcmiInternalDir, "Mods/vcmi/mod.json");
        if (!testVcmiData.exists() && !unpackVcmiDataToInternalDir(vcmiInternalDir))
        {
            return new InitResult(false, getString(R.string.launcher_error_vcmi_data_missing));
        }

        return new InitResult(true, "");
    }

    private boolean unpackVcmiDataToInternalDir(final File vcmiInternalDir)
    {
        try
        {
            byte[] buffer = new byte[4096];
            final ZipInputStream is = new ZipInputStream(getAssets().open("internalData.zip"));
            ZipEntry zipEntry;
            while ((zipEntry = is.getNextEntry()) != null)
            {

                String fileName = zipEntry.getName();
                File newFile = new File(vcmiInternalDir, fileName);

                System.out.println("Unzipping file: " + newFile.getAbsoluteFile());
                if (newFile.exists())
                {
                    Log.d(this, "Already exists");
                    continue;
                }
                else if (zipEntry.isDirectory())
                {
                    Log.d(this, "Creating new dir");
                    if (!newFile.mkdirs())
                    {
                        Log.e(this, "Couldn't create directory " + newFile.getAbsolutePath());
                        return false;
                    }
                    continue;
                }

                File parentFile = new File(newFile.getParent());
                if (!parentFile.exists() && !parentFile.mkdirs())
                {
                    Log.e(this, "Couldn't create directory " + parentFile.getAbsolutePath());
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
            }

            is.closeEntry();
            is.close();
            return true;
        }
        catch (Exception e)
        {
            Log.e(this, "Couldn't extract vcmi data to internal dir", e);
            return false;
        }
    }

    public interface IOnDialogEntryChosen<T>
    {
        void onDialogEntryChosen(final T entry);
    }

    private static final class InitResult
    {
        private final boolean mSuccess;
        private final String mMessage;
        private boolean mFailSilently;

        private InitResult(final boolean success, final String message)
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

    private static class ScreenRes
    {
        public int mWidth;
        public int mHeight;

        public ScreenRes(final int width, final int height)
        {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public String toString()
        {
            return mWidth + "x" + mHeight;
        }
    }

    public static class ScreenResChooserDialog extends DialogFragment
    {

        private IOnDialogEntryChosen<ScreenRes> mCallback;

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final CharSequence[] items = new CharSequence[AVAILABLE_RESOLUTIONS.size()];
            for (int i = 0; i < AVAILABLE_RESOLUTIONS.size(); ++i)
            {
                items[i] = AVAILABLE_RESOLUTIONS.get(i).toString();
            }
            builder.setTitle(R.string.launcher_btn_res_title)
                .setItems(items, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int index)
                    {
                        dialog.dismiss();
                        mCallback.onDialogEntryChosen(AVAILABLE_RESOLUTIONS.get(index));
                    }
                });
            return builder.create();
        }

        @Override
        public void onAttach(Context ctx)
        {
            super.onAttach(ctx);
            mCallback = ((ActivityLauncher) ctx).mScreenResChangedListener;
        }
    }

    public static class CodepageChooserDialog extends DialogFragment
    {

        private IOnDialogEntryChosen<String> mCallback;

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.launcher_btn_res_title)
                .setItems(AVAILABLE_CODEPAGES.toArray(new String[AVAILABLE_CODEPAGES.size()]), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int index)
                    {
                        dialog.dismiss();
                        mCallback.onDialogEntryChosen(AVAILABLE_CODEPAGES.get(index));
                    }
                });
            return builder.create();
        }

        @Override
        public void onAttach(Context ctx)
        {
            super.onAttach(ctx);
            mCallback = ((ActivityLauncher) ctx).mCodepageChangedListener;
        }
    }

    private class OnLauncherStartPressed implements View.OnClickListener
    {
        @Override
        public void onClick(final View view)
        {
            Log.i(this, "Launching main game");
            startActivity(new Intent(ActivityLauncher.this, SDLActivity.class));
        }
    }

    private class OnLauncherModsPressed implements View.OnClickListener
    {
        @Override
        public void onClick(final View view)
        {
            Log.i(this, "Starting mods activity");
            startActivity(new Intent(ActivityLauncher.this, ActivityMods.class));
        }
    }

    private class OnLauncherResPressed implements View.OnClickListener
    {
        @Override
        public void onClick(final View view)
        {
            Log.i(this, "Showing resolution dialog");
            new ScreenResChooserDialog().show(getSupportFragmentManager(), null);
        }
    }

    private class OnLauncherCodepagePressed implements View.OnClickListener
    {
        @Override
        public void onClick(final View view)
        {
            Log.i(this, "Showing codepage dialog");
            new CodepageChooserDialog().show(getSupportFragmentManager(), null);
        }
    }
}

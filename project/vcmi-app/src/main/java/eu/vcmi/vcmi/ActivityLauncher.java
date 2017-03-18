package eu.vcmi.vcmi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;
import org.libsdl.app.SDLActivity;

import java.io.File;

import eu.vcmi.vcmi.settings.CodepageSettingController;
import eu.vcmi.vcmi.settings.LauncherSettingController;
import eu.vcmi.vcmi.settings.ModsBtnController;
import eu.vcmi.vcmi.settings.PointerModeSettingController;
import eu.vcmi.vcmi.settings.ScreenResSettingController;
import eu.vcmi.vcmi.settings.StartGameController;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.SharedPrefs;


/**
 * @author F
 */
public class ActivityLauncher extends ActivityBase
{
    private static final int PERMISSIONS_REQ_CODE = 123;

    private View mProgress;
    private TextView mErrorMessage;
    private Config mConfig;
    private LauncherSettingController<ScreenResSettingController.ScreenRes, Config> mCtrlScreenRes;
    private LauncherSettingController<String, Config> mCtrlCodepage;
    private LauncherSettingController<PointerModeSettingController.PointerMode, SharedPrefs> mCtrlPointerMode;
    private LauncherSettingController<Void, Void> mCtrlStart;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) // only clear the log if this is initial onCreate and not config change
        {
            Log.init();
        }
        Log.i(this, "Starting launcher");
        setContentView(R.layout.activity_launcher);

        mProgress = findViewById(R.id.launcher_progress);
        mErrorMessage = (TextView) findViewById(R.id.launcher_error);
        mErrorMessage.setVisibility(View.GONE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((TextView) findViewById(R.id.launcher_version_info)).setText(getString(R.string.launcher_version, BuildConfig.VERSION_NAME));
        mConfig = new Config();

        initSettings();
        init(); // TODO move to async task (will definitely be needed to be on worker thread when unpacking vcmi-data to external storage is added)
    }

    private void initSettings()
    {
        mCtrlStart =
            new StartGameController(this, v -> startActivity(new Intent(ActivityLauncher.this, SDLActivity.class))).init(R.id.launcher_btn_start);
        new ModsBtnController(this, v -> startActivity(new Intent(ActivityLauncher.this, ActivityMods.class))).init(R.id.launcher_btn_mods);
        mCtrlScreenRes = new ScreenResSettingController(this).init(R.id.launcher_btn_res, mConfig);
        mCtrlCodepage = new CodepageSettingController(this).init(R.id.launcher_btn_cp, mConfig);
        mCtrlPointerMode = new PointerModeSettingController(this).init(R.id.launcher_btn_pointer_mode, mPrefs);

        mCtrlStart.hide();
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
                FileUtil.read(new File(FileUtil.configFileLocation()));
            mConfig = Config.load(new JSONObject(settingsFileContent));
            onConfigUpdated();
        }
        catch (Exception e)
        {
            Log.e(this, "Could not load config file", e);
            mConfig = new Config();
        }
    }

    private void onConfigUpdated()
    {
        mCtrlScreenRes.updateConfig(mConfig);
        mCtrlCodepage.updateConfig(mConfig);
        mCtrlPointerMode.updateConfig(mPrefs);
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
        mCtrlStart.show();
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
        mCtrlStart.hide();
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
            return new InitResult(false,
                getString(R.string.launcher_error_h3_data_missing, testH3Data.getAbsolutePath(), Const.VCMI_DATA_ROOT_FOLDER_NAME));
        }

        final File testVcmiData = new File(vcmiInternalDir, "Mods/vcmi/mod.json");
        if (!testVcmiData.exists() && !FileUtil.unpackVcmiDataToInternalDir(vcmiInternalDir, getAssets()))
        {
            return new InitResult(false, getString(R.string.launcher_error_vcmi_data_missing));
        }

        return new InitResult(true, "");
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
}

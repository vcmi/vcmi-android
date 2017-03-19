package eu.vcmi.vcmi;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.annimon.stream.Stream;

import org.json.JSONObject;
import org.libsdl.app.SDLActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.vcmi.vcmi.content.AsyncLauncherInitialization;
import eu.vcmi.vcmi.settings.CodepageSettingController;
import eu.vcmi.vcmi.settings.LauncherSettingController;
import eu.vcmi.vcmi.settings.ModsBtnController;
import eu.vcmi.vcmi.settings.PointerModeSettingController;
import eu.vcmi.vcmi.settings.PointerMultiplierSettingController;
import eu.vcmi.vcmi.settings.ScreenResSettingController;
import eu.vcmi.vcmi.settings.StartGameController;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.SharedPrefs;


/**
 * @author F
 */
public class ActivityLauncher extends ActivityBase implements AsyncLauncherInitialization.ILauncherCallbacks
{
    private View mProgress;
    private TextView mErrorMessage;
    private Config mConfig;
    private LauncherSettingController<ScreenResSettingController.ScreenRes, Config> mCtrlScreenRes;
    private LauncherSettingController<String, Config> mCtrlCodepage;
    private LauncherSettingController<PointerModeSettingController.PointerMode, SharedPrefs> mCtrlPointerMode;
    private LauncherSettingController<Void, Void> mCtrlStart;
    private List<LauncherSettingController<?, ?>> mActualSettings = new ArrayList<>();
    private LauncherSettingController<Float, SharedPrefs> mCtrlPointerMulti;

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
        initSettingsGui();
        new AsyncLauncherInitialization(this).execute((Void) null);
    }

    private void initSettingsGui()
    {
        mCtrlStart =
            new StartGameController(this, v -> startActivity(new Intent(ActivityLauncher.this, SDLActivity.class))).init(R.id.launcher_btn_start);
        new ModsBtnController(this, v -> startActivity(new Intent(ActivityLauncher.this, ActivityMods.class))).init(R.id.launcher_btn_mods);
        mCtrlScreenRes = new ScreenResSettingController(this).init(R.id.launcher_btn_res, mConfig);
        mCtrlCodepage = new CodepageSettingController(this).init(R.id.launcher_btn_cp, mConfig);
        mCtrlPointerMode = new PointerModeSettingController(this).init(R.id.launcher_btn_pointer_mode, mPrefs);
        mCtrlPointerMulti = new PointerMultiplierSettingController(this).init(R.id.launcher_btn_pointer_multi, mPrefs);

        mActualSettings.clear();
        mActualSettings.add(mCtrlCodepage);
        mActualSettings.add(mCtrlScreenRes);
        mActualSettings.add(mCtrlPointerMode);
        mActualSettings.add(mCtrlPointerMulti);

        mCtrlStart.hide();
    }


    private void loadConfigFile()
    {
        try
        {
            final String settingsFileContent =
                FileUtil.read(new File(FileUtil.configFileLocation()));
            mConfig = Config.load(new JSONObject(settingsFileContent));
        }
        catch (Exception e)
        {
            Log.e(this, "Could not load config file", e);
            mConfig = new Config();
        }
        onConfigUpdated();
    }

    private void onConfigUpdated()
    {
        if (mCtrlScreenRes != null)
        {
            mCtrlScreenRes.updateConfig(mConfig);
        }
        if (mCtrlCodepage != null)
        {
            mCtrlCodepage.updateConfig(mConfig);
        }
        if (mCtrlPointerMode != null)
        {
            mCtrlPointerMode.updateConfig(mPrefs);
        }
        if (mCtrlPointerMulti != null)
        {
            mCtrlPointerMulti.updateConfig(mPrefs);
        }
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults)
    {
        if (requestCode == AsyncLauncherInitialization.PERMISSIONS_REQ_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                new AsyncLauncherInitialization(this).execute((Void) null); // retry init (we still need to check folders)
            }
            else
            {
                onInitFailure(new AsyncLauncherInitialization.InitResult(false, getString(R.string.launcher_error_permissions)), true);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public Activity ctx()
    {
        return this;
    }

    @Override
    public void onInitSuccess()
    {
        loadConfigFile();
        mCtrlStart.show();
        mErrorMessage.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public void onInitFailure(final AsyncLauncherInitialization.InitResult result)
    {
        if (result.mFailSilently)
        {
            return;
        }
        onInitFailure(result, false);
    }

    private void onInitFailure(final AsyncLauncherInitialization.InitResult initResult, final boolean disableAccess)
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
        Stream.of(mActualSettings).forEach(LauncherSettingController::hide);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(initResult.mMessage);
    }

}

package eu.vcmi.vcmi;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;
import org.libsdl.app.SDLActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.vcmi.vcmi.content.AsyncLauncherInitialization;
import eu.vcmi.vcmi.settings.CodepageSettingController;
import eu.vcmi.vcmi.settings.DoubleConfig;
import eu.vcmi.vcmi.settings.LauncherSettingController;
import eu.vcmi.vcmi.settings.ModsBtnController;
import eu.vcmi.vcmi.settings.MusicSettingController;
import eu.vcmi.vcmi.settings.PointerModeSettingController;
import eu.vcmi.vcmi.settings.PointerMultiplierSettingController;
import eu.vcmi.vcmi.settings.ScreenResSettingController;
import eu.vcmi.vcmi.settings.SoundSettingController;
import eu.vcmi.vcmi.settings.StartGameController;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.SharedPrefs;


/**
 * @author F
 */
public class ActivityLauncher extends ActivityBase
{
    private View mProgress;
    private TextView mErrorMessage;
    private Config mConfig;
    private LauncherSettingController<ScreenResSettingController.ScreenRes, Config> mCtrlScreenRes;
    private LauncherSettingController<String, Config> mCtrlCodepage;
    private LauncherSettingController<PointerModeSettingController.PointerMode, DoubleConfig> mCtrlPointerMode;
    private LauncherSettingController<Void, Void> mCtrlStart;
    private LauncherSettingController<Float, SharedPrefs> mCtrlPointerMulti;
    private LauncherSettingController<Integer, Config> mCtrlSoundVol;
    private LauncherSettingController<Integer, Config> mCtrlMusicVol;
    private final List<LauncherSettingController<?, ?>> mActualSettings = new ArrayList<>();
    private final AsyncLauncherInitialization.ILauncherCallbacks mInitCallbacks = new AsyncLauncherInitialization.ILauncherCallbacks()
    {
        @Override
        public Activity ctx()
        {
            return ActivityLauncher.this;
        }

        @Override
        public SharedPrefs prefs()
        {
            return mPrefs;
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
            ActivityLauncher.this.onInitFailure(result, false);
        }
    };

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
        new AsyncLauncherInitialization(mInitCallbacks).execute((Void) null);
    }

    @Override
    public void onBackPressed()
    {
        saveConfig();
        super.onBackPressed();
    }

    private void initSettingsGui()
    {
        mCtrlStart =
            new StartGameController(this, v -> onLaunchGameBtnPressed()).init(R.id.launcher_btn_start);
        new ModsBtnController(this, v -> startActivity(new Intent(ActivityLauncher.this, ActivityMods.class))).init(R.id.launcher_btn_mods);
        mCtrlScreenRes = new ScreenResSettingController(this).init(R.id.launcher_btn_res, mConfig);
        mCtrlCodepage = new CodepageSettingController(this).init(R.id.launcher_btn_cp, mConfig);
        mCtrlPointerMode = new PointerModeSettingController(this).init(R.id.launcher_btn_pointer_mode, new DoubleConfig(mConfig, mPrefs));
        mCtrlPointerMulti = new PointerMultiplierSettingController(this).init(R.id.launcher_btn_pointer_multi, mPrefs);
        mCtrlSoundVol = new SoundSettingController(this).init(R.id.launcher_btn_volume_sound, mConfig);
        mCtrlMusicVol = new MusicSettingController(this).init(R.id.launcher_btn_volume_music, mConfig);

        mActualSettings.clear();
        mActualSettings.add(mCtrlCodepage);
        mActualSettings.add(mCtrlScreenRes);
        mActualSettings.add(mCtrlPointerMode);
        mActualSettings.add(mCtrlPointerMulti);
        mActualSettings.add(mCtrlSoundVol);
        mActualSettings.add(mCtrlMusicVol);

        mCtrlStart.hide();
    }

    private void onLaunchGameBtnPressed()
    {
        saveConfig();
        startActivity(new Intent(ActivityLauncher.this, SDLActivity.class));
    }

    private void saveConfig()
    {
        if (mConfig == null)
        {
            return;
        }

        try
        {
            mConfig.save(new File(FileUtil.configFileLocation()));
        }
        catch (final Exception e)
        {
            Toast.makeText(this, getString(R.string.launcher_error_config_saving_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
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
        updateCtrlConfig(mCtrlScreenRes, mConfig);
        updateCtrlConfig(mCtrlCodepage, mConfig);
        updateCtrlConfig(mCtrlPointerMode, new DoubleConfig(mConfig, mPrefs));
        updateCtrlConfig(mCtrlPointerMulti, mPrefs);
        updateCtrlConfig(mCtrlSoundVol, mConfig);
        updateCtrlConfig(mCtrlMusicVol, mConfig);
    }

    private <TSetting, TConf> void updateCtrlConfig(final LauncherSettingController<TSetting, TConf> ctrl, final TConf config)
    {
        if (ctrl != null)
        {
            ctrl.updateConfig(config);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults)
    {
        if (requestCode == AsyncLauncherInitialization.PERMISSIONS_REQ_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                new AsyncLauncherInitialization(mInitCallbacks).execute((Void) null); // retry init (we still need to check folders)
            }
            else
            {
                onInitFailure(new AsyncLauncherInitialization.InitResult(false, getString(R.string.launcher_error_permissions)), true);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

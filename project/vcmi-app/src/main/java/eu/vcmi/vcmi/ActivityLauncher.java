package eu.vcmi.vcmi;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.libsdl.app.SDLActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import eu.vcmi.vcmi.settings.UpdateVcmiFilesController;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.SharedPrefs;


/**
 * @author F
 */
public class ActivityLauncher extends ActivityWithToolbar
{
    private final List<LauncherSettingController<?, ?>> mActualSettings = new ArrayList<>();
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
    private LauncherSettingController<Void, Void> mCtrlStorage;
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
        Storage.initStorage(this);
        setContentView(R.layout.activity_launcher);
        initToolbar(R.string.launcher_title, true);

        mProgress = findViewById(R.id.launcher_progress);
        mErrorMessage = (TextView) findViewById(R.id.launcher_error);
        mErrorMessage.setVisibility(View.GONE);

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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if (item.getItemId() == R.id.menu_launcher_about)
        {
            startActivity(new Intent(this, ActivityAbout.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSettingsGui()
    {
        mCtrlStart = new StartGameController(this, v -> onLaunchGameBtnPressed()).init(R.id.launcher_btn_start);
        new ModsBtnController(this, v -> startActivity(new Intent(ActivityLauncher.this, ActivityMods.class))).init(R.id.launcher_btn_mods);
        mCtrlScreenRes = new ScreenResSettingController(this).init(R.id.launcher_btn_res, mConfig);
        mCtrlCodepage = new CodepageSettingController(this).init(R.id.launcher_btn_cp, mConfig);
        mCtrlPointerMode = new PointerModeSettingController(this).init(R.id.launcher_btn_pointer_mode, new DoubleConfig(mConfig, mPrefs));
        mCtrlPointerMulti = new PointerMultiplierSettingController(this).init(R.id.launcher_btn_pointer_multi, mPrefs);
        mCtrlSoundVol = new SoundSettingController(this).init(R.id.launcher_btn_volume_sound, mConfig);
        mCtrlMusicVol = new MusicSettingController(this).init(R.id.launcher_btn_volume_music, mConfig);
        mCtrlStorage = new UpdateVcmiFilesController(this, v -> onSetupStorage()).init(R.id.launcher_btn_storage);

        mActualSettings.clear();
        mActualSettings.add(mCtrlCodepage);
        mActualSettings.add(mCtrlScreenRes);
        mActualSettings.add(mCtrlPointerMode);
        mActualSettings.add(mCtrlPointerMulti);
        mActualSettings.add(mCtrlSoundVol);
        mActualSettings.add(mCtrlMusicVol);

        mCtrlStart.hide(); // start is initially hidden, until we confirm that everything is okay via AsyncLauncherInitialization
    }

    private void onSetupStorage()
    {
        startActivity(new Intent(this, ActivityStorage.class));
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
            mConfig.save(new File(FileUtil.configFileLocation(Storage.getVcmiDataDir(this))));
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
            Storage.initStorage(this);

            final String settingsFileContent = FileUtil.read(
                    new File(FileUtil.configFileLocation(Storage.getVcmiDataDir(this))));

            mConfig = Config.load(new JSONObject(settingsFileContent));
        }
        catch (final Exception e)
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

    private <TSetting, TConf> void updateCtrlConfig(
            final LauncherSettingController<TSetting, TConf> ctrl,
            final TConf config)
    {
        if (ctrl != null)
        {
            ctrl.updateConfig(config);
        }
    }

    private void onInitFailure(final AsyncLauncherInitialization.InitResult initResult, final boolean disableAccess)
    {
        Log.d(this, "Init failed with " + initResult);
        if (disableAccess)
        {
            final Intent intent = new Intent(this, ActivityError.class);
            intent.putExtra(ActivityError.ARG_ERROR_MSG, initResult.mMessage);
            startActivity(intent);
            finish();
            return;
        }
        mProgress.setVisibility(View.GONE);
        mCtrlStart.hide();
        for (LauncherSettingController<?, ?> setting: mActualSettings) {
            setting.hide();
        }
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(initResult.mMessage);
    }
}

package eu.vcmi.vcmi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.libsdl.app.SDLActivity;

import java.io.File;

import eu.vcmi.vcmi.util.Log;


/**
 * @author F
 */
public class ActivityLauncher extends AppCompatActivity
{
    private static final int PERMISSIONS_REQ_CODE = 123;
    private View mProgress;
    private View mBtnStart;
    private TextView mErrorMessage;

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
        initLauncherBtn(R.id.launcher_btn_res, new OnLauncherResPressed(), getString(R.string.launcher_btn_res_title),
            getString(R.string.launcher_btn_res_subtitle));
        initLauncherBtn(R.id.launcher_btn_mods, new OnLauncherModsPressed(), getString(R.string.launcher_btn_mods_title),
            getString(R.string.launcher_btn_mods_subtitle));
        initLauncherBtn(R.id.launcher_btn_cp, new OnLauncherCodepagePressed(), getString(R.string.launcher_btn_cp_title),
            getString(R.string.launcher_btn_cp_subtitle));

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

        onInitSuccess();
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
        final File vcmiDir = new File(baseDir, Const.VCMI_DATA_ROOT_FOLDER_NAME);
        Log.i(this, "Using " + vcmiDir.getAbsolutePath() + " as root vcmi dir");
        if (!vcmiDir.exists()) // we don't have root folder == new install (or deleted)
        {
            boolean allCreated = vcmiDir.mkdir();

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
            return new InitResult(false, getString(R.string.launcher_error_h3_data_missing, testH3Data.getAbsolutePath()));
        }

        final File testVcmiData = new File(vcmiDir, "Mods/vcmi/mod.json");
        if (!testVcmiData.exists())
        {
            return new InitResult(false, getString(R.string.launcher_error_vcmi_data_missing, vcmiDir.getAbsolutePath()));
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
        }
    }

    private class OnLauncherCodepagePressed implements View.OnClickListener
    {
        @Override
        public void onClick(final View view)
        {
            Log.i(this, "Showing codepage dialog");
        }
    }
}

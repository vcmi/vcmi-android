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
import android.widget.Toast;

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

        ((TextView) findViewById(R.id.launcher_version_info)).setText("Current launcher version: " + BuildConfig.VERSION_NAME);

        mBtnStart = findViewById(R.id.launcher_btn_start);
        mBtnStart.setVisibility(View.GONE);
        mBtnStart.setOnClickListener(new OnLauncherStartPressed());
        ((TextView) mBtnStart.findViewById(R.id.inc_launcher_btn_main)).setText("Start VCMI");
        ((TextView) mBtnStart.findViewById(R.id.inc_launcher_btn_sub)).setText("Current VCMI version: xyz");

        View btnMods = findViewById(R.id.launcher_btn_mods);
        btnMods.setOnClickListener(new OnLauncherModsPressed());
        ((TextView) btnMods.findViewById(R.id.inc_launcher_btn_main)).setText("Mods");
        ((TextView) btnMods.findViewById(R.id.inc_launcher_btn_sub)).setText("Currently active: X, available: Y");

        View btnRes = findViewById(R.id.launcher_btn_res);
        btnRes.setOnClickListener(new OnLauncherResPressed());
        ((TextView) btnRes.findViewById(R.id.inc_launcher_btn_main)).setText("Change game native resolution");
        ((TextView) btnRes.findViewById(R.id.inc_launcher_btn_sub)).setText("Currently: 800x600");

        init();
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
            return new InitResult(Build.VERSION.SDK_INT < Build.VERSION_CODES.M, "Could not resolve permissions correctly");
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
                onInitError(new InitResult(false, "This application needs write permissions to use the content stored in external storage"), true);
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
//            if (allCreated)
//            {
//                allCreated = new File(vcmiDir, "DATA").mkdir();
//                allCreated &= new File(vcmiDir, "SOMETHING").mkdir();
//            }

            if (allCreated)
            {
                Toast.makeText(this, "TMP : initial start -- created dirs -- waiting for data", Toast.LENGTH_LONG).show();
                return new InitResult(false, "");
            }
            else
            {
                Toast.makeText(this, "TMP : initial start -- could not create data folders", Toast.LENGTH_LONG).show();
                return new InitResult(false, "");
            }
        }

        // TODO handle checking if user data is present

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
}

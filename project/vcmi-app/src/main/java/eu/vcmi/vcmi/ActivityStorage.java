package eu.vcmi.vcmi;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import eu.vcmi.vcmi.databinding.ActivityStorageBinding;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.viewmodels.StorageViewModel;


/**
 * @author F
 */
public class ActivityStorage extends AppCompatActivity
{
    private static final int PICK_VCMI_ZIP_FILE = 2;
    private static final int PICK_SAVES_FOLDER = 3;
    public static final int PERMISSIONS_REQ_CODE = 123;

    StorageViewModel vm;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.i(this, "Starting storage activity");

        vm = new ViewModelProvider(
                this,
                new ViewModelProvider.NewInstanceFactory())
                .get(StorageViewModel.class);

        ActivityStorageBinding binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_storage);

        vm.setActivity(this);
        binding.setVm(vm);
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

    public void selectZipFile()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");

        startActivityForResult(intent, PICK_VCMI_ZIP_FILE);
    }

    public void selectSavesFolder()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        intent.putExtra(
            DocumentsContract.EXTRA_INITIAL_URI,
            Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "vcmi-data")));

        startActivityForResult(intent, PICK_SAVES_FOLDER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData)
    {
        if (requestCode == PICK_VCMI_ZIP_FILE && resultCode == Activity.RESULT_OK)
        {
            Uri uri = null;
            if (resultData != null)
            {
                uri = resultData.getData();

                vm.importFilesFromStream(uri);
            }

            return;
        }
        else if(requestCode == PICK_SAVES_FOLDER && resultCode == Activity.RESULT_OK)
        {
            Uri uri = null;
            if (resultData != null)
            {
                uri = resultData.getData();

                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

                getContentResolver().takePersistableUriPermission(uri, takeFlags);

                vm.exportSaves(uri);
            }

            return;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    vm.onUseExternalStorage();
                }
                return;
        }
    }

    public void requestStoragePermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQ_CODE);
        }
    }
}

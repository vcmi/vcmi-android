package eu.vcmi.vcmi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.vcmi.vcmi.content.ModsAdapter;
import eu.vcmi.vcmi.mods.VCMIMod;
import eu.vcmi.vcmi.mods.VCMIModsRepo;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public class ActivityMods extends AppCompatActivity
{
    private static final boolean ENABLE_REPO_DOWNLOADING = false;
    private static final String REPO_URL = "http://download.vcmi.eu/mods/repository/repository.json";
    private VCMIModsRepo mRepo;
    private RecyclerView mRecycler;

    private VCMIMod mModContainer;
    private TextView mErrorMessage;
    private View mProgress;
    private ModsAdapter mModsAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mods);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Detected mods");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRepo = new VCMIModsRepo();

        mProgress = findViewById(R.id.mods_progress);

        mErrorMessage = (TextView) findViewById(R.id.mods_error_text);
        mErrorMessage.setVisibility(View.GONE);

        mRecycler = (RecyclerView) findViewById(R.id.mods_recycler);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecycler.setVisibility(View.GONE);

        new AsyncLoadLocalMods().execute((Void) null);
    }

    private void loadLocalModData() throws IOException, JSONException
    {
        final String dataRoot = Environment.getExternalStorageDirectory() + "/" + Const.VCMI_DATA_ROOT_FOLDER_NAME;
        final String internalDataRoot = getFilesDir() + "/" + Const.VCMI_DATA_ROOT_FOLDER_NAME;
        final String configPath = dataRoot + "/config/modSettings.json";
        final File modConfigFile = new File(configPath);
        if (!modConfigFile.exists())
        {
            Log.w(this, "We don't have mods config");
            handleNoData();
            return;
        }

        JSONObject jsonObject = new JSONObject(FileUtil.read(modConfigFile));
        JSONObject activeMods = jsonObject.getJSONObject("activeMods");
        final JSONArray names = activeMods.names();
        final Map<String, VCMIMod> localMods = new HashMap<>();
        for (int i = 0; i < names.length(); ++i)
        {
            String name = names.getString(i).toLowerCase(Locale.US);
            localMods.put(name, VCMIMod.buildFromConfigJson(name, activeMods.getJSONObject(name)));
        }

        final File modsRoot = new File(dataRoot + "/Mods");
        final File internalModsRoot = new File(internalDataRoot + "/Mods");
        if (!modsRoot.exists() && !internalModsRoot.exists())
        {
            Log.w(this, "We don't have mods folders");
            handleNoData();
            return;
        }
        final List<File> topLevelModsFolders = new ArrayList<>();
        final File[] modsFiles = modsRoot.listFiles();
        final File[] internalModsFiles = internalModsRoot.listFiles();
        if (modsFiles != null && modsFiles.length > 0)
        {
            Collections.addAll(topLevelModsFolders, modsFiles);
        }
        if (internalModsFiles != null && internalModsFiles.length > 0)
        {
            Collections.addAll(topLevelModsFolders, internalModsFiles);
        }
        Log.i(this, "Loaded mods: " + localMods);
        mModContainer = VCMIMod.createContainer(localMods, topLevelModsFolders);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_mods, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_mods_download_repo)
        {
            Log.i(this, "Should download repo now...");
            if (ENABLE_REPO_DOWNLOADING)
            {
                mRepo.init(REPO_URL, new OnModsRepoInitialized()); // disabled because the json is broken anyway
            }
            else
            {
                Snackbar.make(findViewById(R.id.mods_data_root), "Loading repo is disabled for now, because .json can't be parsed anyway",
                    Snackbar.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleNoData()
    {
        mProgress.setVisibility(View.GONE);
        mRecycler.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText("Could not load local mods list");
    }

    private class OnModsRepoInitialized implements VCMIModsRepo.IOnModsRepoDownloaded
    {
        @Override
        public void onSuccess()
        {
            Log.i(this, "Initialized mods repo");
            // TODO update dataset
        }

        @Override
        public void onError(final int code)
        {
            Log.i(this, "Mods repo error: " + code);
        }
    }

    private class AsyncLoadLocalMods extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(final Void... params)
        {
            try
            {
                loadLocalModData();
            }
            catch (IOException e)
            {
                Log.e(this, "Loading local mod data failed", e);
            }
            catch (JSONException e)
            {
                Log.e(this, "Parsing local mod data failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid)
        {
            if (mModContainer == null || !mModContainer.hasSubmods())
            {
                handleNoData();
            }
            else
            {
                mProgress.setVisibility(View.GONE);
                mRecycler.setVisibility(View.VISIBLE);
                mModsAdapter = new ModsAdapter(mModContainer.submods(), new OnAdapterItemAction());
                mRecycler.setAdapter(mModsAdapter);
            }
        }
    }

    private class OnAdapterItemAction implements ModsAdapter.IOnItemAction
    {
        @Override
        public void onItemPressed(final VCMIMod mod, final RecyclerView.ViewHolder vh)
        {
            Log.i(this, "Mod pressed: " + mod);
            // TODO show/hide submods
        }

        @Override
        public void onDownloadPressed(final VCMIMod mod, final RecyclerView.ViewHolder vh)
        {
            Log.i(this, "Mod download pressed: " + mod);
        }
    }
}

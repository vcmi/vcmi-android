package eu.vcmi.vcmi.mods;

import android.text.TextUtils;

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

import eu.vcmi.vcmi.BuildConfig;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public class VCMIMod
{
    private final Map<String, VCMIMod> mSubmods;
    public boolean mActive;
    public String mId;
    public String mName;
    public String mDesc;
    public String mVersion;
    public String mAuthor;
    public String mContact;
    public String mModType;
    public String mArchiveUrl;
    public long mSize;

    private VCMIMod()
    {
        mSubmods = new HashMap<>();
    }

    public static VCMIMod buildFromRepoJson(final String id, final JSONObject obj)
    {
        final VCMIMod mod = new VCMIMod();
        mod.mId = id;
        mod.mName = obj.optString("name");
        mod.mDesc = obj.optString("description");
        mod.mVersion = obj.optString("version");
        mod.mAuthor = obj.optString("author");
        mod.mContact = obj.optString("contact");
        mod.mModType = obj.optString("modType");
        mod.mArchiveUrl = obj.optString("download");
        mod.mSize = obj.optLong("size");
        return mod;
    }

    public static VCMIMod buildFromConfigJson(final String id, final JSONObject obj) throws JSONException
    {
        final VCMIMod mod = new VCMIMod();
        mod.mId = id;
        mod.mActive = obj.optBoolean("active");

        final JSONObject submods = obj.optJSONObject("mods");
        if (submods != null)
        {
            final JSONArray names = submods.names();
            for (int i = 0; i < names.length(); ++i)
            {
                final String submodName = names.getString(i);
                mod.mSubmods.put(submodName, VCMIMod.buildFromConfigJson(submodName, submods.getJSONObject(submodName)));
            }
        }
        return mod;
    }

    public static VCMIMod createContainer(final Map<String, VCMIMod> localMods, final List<File> modsList) throws IOException, JSONException
    {
        final VCMIMod mod = new VCMIMod();
        loadSubmods(localMods, modsList);
        mod.mSubmods.putAll(localMods);
        return mod;
    }

    private static void loadSubmods(final Map<String, VCMIMod> localMods, final List<File> modsList) throws IOException, JSONException
    {
        for (final File f : modsList)
        {
            if (!f.isDirectory())
            {
                Log.w("VCMI", "Non-directory encountered in mods dir: " + f.getName());
                continue;
            }
            String dirName = f.getName().toLowerCase(Locale.US);
            if (!localMods.containsKey(dirName))
            {
                Log.w("VCMI", "Unknown folder encountered in mods dir: " + f.getName());
                continue;
            }
            if (!localMods.get(dirName).updateFromModInfo(f))
            {
                Log.w("VCMI", "Error during mod info initialization...");
            }
        }
    }

    public boolean updateFromModInfo(final File modPath) throws IOException, JSONException
    {
        final File modInfoFile = new File(modPath, "mod.json");
        if (!modInfoFile.exists())
        {
            Log.w(this, "Mod info doesn't exist");
            return false;
        }
        final JSONObject modInfoContent = new JSONObject(FileUtil.read(modInfoFile));
        mName = modInfoContent.optString("name");
        mDesc = modInfoContent.optString("description");
        mVersion = modInfoContent.optString("version");
        mAuthor = modInfoContent.optString("author");
        mContact = modInfoContent.optString("contact");
        mModType = modInfoContent.optString("modType");
        File submodsDir = new File(modPath, "Mods");
        if (submodsDir.exists())
        {
            final List<File> submodsFiles = new ArrayList<>();
            Collections.addAll(submodsFiles, submodsDir.listFiles());
            loadSubmods(mSubmods, submodsFiles);
        }
        return true;
    }

    @Override
    public String toString()
    {
        if (!BuildConfig.DEBUG)
        {
            return "";
        }
        return String.format("mod:[id:%s,active:%s,submods:[%s]]", mId, mActive, TextUtils.join(",", mSubmods.values()));
    }

    public boolean hasSubmods()
    {
        return !mSubmods.isEmpty();
    }

    public List<VCMIMod> submods()
    {
        final ArrayList<VCMIMod> ret = new ArrayList<>();
        ret.addAll(mSubmods.values());
        return ret;
    }
}

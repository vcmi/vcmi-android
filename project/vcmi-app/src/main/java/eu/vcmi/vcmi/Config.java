package eu.vcmi.vcmi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public class Config
{
    public String mCodepage;
    public int mResolutionWidth;
    public int mResolutionHeight;
    public boolean mSwipeEnabled;
    private JSONObject mRawObject;

    private boolean mIsModified;

    public void updateCodepage(final String s)
    {
        mCodepage = s;
        mIsModified = true;
    }

    public void updateResolution(final int x, final int y)
    {
        mResolutionWidth = x;
        mResolutionHeight = y;
        mIsModified = true;
    }

    public void updateSwipe(final boolean b)
    {
        mSwipeEnabled = b;
        mIsModified = true;
    }

    private static JSONObject accessGeneralNode(final JSONObject baseObj)
    {
        return baseObj.optJSONObject("general");
    }

    private static JSONObject accessScreenResNode(final JSONObject baseObj)
    {
        final JSONObject video = baseObj.optJSONObject("video");
        if (video != null)
        {
            return video.optJSONObject("screenRes");
        }
        return null;
    }

    public static Config load(final JSONObject obj)
    {
        Log.v("VCMI", "loading config from json: " + obj.toString());
        final Config config = new Config();
        final JSONObject general = accessGeneralNode(obj);
        if (general != null)
        {
            config.mCodepage = general.optString("encoding");
        }
        final JSONObject screenRes = accessScreenResNode(obj);
        if (screenRes != null)
        {
            config.mResolutionWidth = screenRes.optInt("width");
            config.mResolutionHeight = screenRes.optInt("height");
        }
        config.mRawObject = obj;
        return config;
    }

    public void save(final File location) throws IOException, JSONException
    {
        if (!needsSaving(location))
        {
            Log.d(this, "Config doesn't need saving");
            return;
        }
        try
        {
            final String configString = toJson();
            FileUtil.write(location, configString);
            Log.v(this, "Saved config: " + configString);
        }
        catch (final Exception e)
        {
            Log.e(this, "Could not save config", e);
            throw e;
        }
    }

    private boolean needsSaving(final File location)
    {
        return mIsModified || !location.exists();
    }

    private String toJson() throws JSONException
    {
        final JSONObject generalNode = accessGeneralNode(mRawObject);
        final JSONObject screenResNode = accessScreenResNode(mRawObject);

        final JSONObject root = new JSONObject();
        final JSONObject general = generalNode == null ? new JSONObject() : generalNode;
        final JSONObject video = new JSONObject();
        final JSONObject screenRes = screenResNode == null ? new JSONObject() : screenResNode;
        if (mCodepage != null)
        {
            general.put("encoding", mCodepage);
        }
        general.put("swipe", mSwipeEnabled);
        root.put("general", general);

        if (mResolutionHeight > 0 && mResolutionWidth > 0)
        {
            screenRes.put("width", mResolutionWidth);
            screenRes.put("height", mResolutionHeight);
            video.put("screenRes", screenRes);
            root.put("video", video);
        }
        return root.toString();
    }
}

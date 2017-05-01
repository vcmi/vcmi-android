package eu.vcmi.vcmi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

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
    private JSONObject mRawObject;

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

    public void save(final File location)
    {
        try
        {
            FileUtil.write(location, toJson());
        }
        catch (final Exception e)
        {
            Log.e(this, "Could not save config", e);
        }
    }

    private String toJson() throws JSONException
    {
        final JSONObject videoNode = accessGeneralNode(mRawObject);
        final JSONObject screenResNode = accessScreenResNode(mRawObject);

        JSONObject root = new JSONObject();
        JSONObject general = new JSONObject();
        JSONObject video = new JSONObject();
        JSONObject screenRes = new JSONObject();
        if (mCodepage != null)
        {
            general.put("encoding", mCodepage);
        }
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

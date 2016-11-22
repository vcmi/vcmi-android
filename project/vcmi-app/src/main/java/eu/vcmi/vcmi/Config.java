package eu.vcmi.vcmi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;

import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public class Config
{
    public String mCodepage;
    public int mResolutionWidth;
    public int mResolutionHeight;

    public static Config load(final JSONObject obj)
    {
        Log.v("VCMI", "loading config from json: " + obj.toString());
        Config config = new Config();
        JSONObject general = obj.optJSONObject("general");
        if (general != null)
        {
            config.mCodepage = general.optString("encoding");
        }
        JSONObject video = obj.optJSONObject("video");
        if (video != null)
        {
            JSONObject screenRes = video.optJSONObject("screenRes");
            if (screenRes != null)
            {
                config.mResolutionWidth = screenRes.optInt("width");
                config.mResolutionHeight = screenRes.optInt("height");
            }
        }
        return config;
    }

    public void save(final File location)
    {
        try
        {
            FileWriter fw = new FileWriter(location, false);
            String jsonConfig = toJson();
            Log.v(this, "Saving config: " + jsonConfig);
            fw.write(jsonConfig);
            fw.close();
        }
        catch (Exception e)
        {
            Log.e(this, "Could not save config", e);
        }
    }

    private String toJson() throws JSONException
    {
        JSONObject root = new JSONObject();
        JSONObject general = new JSONObject();
        JSONObject video = new JSONObject();
        JSONObject screenRes = new JSONObject();
        if (mCodepage != null)
        {
            general.put("encoding", mCodepage);
        }
        if (mResolutionHeight > 0 && mResolutionWidth > 0)
        {
            screenRes.put("width", mResolutionWidth);
            screenRes.put("height", mResolutionHeight);
        }
        video.put("screenRes", screenRes);
        root.put("general", general);
        root.put("video", video);
        return root.toString();
    }
}
